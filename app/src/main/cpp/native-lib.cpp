\
        #include <jni.h>
        #include <vector>
        #include <cmath>
        #include <android/log.h>
        #define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "native-lib", __VA_ARGS__)
        #define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "native-lib", __VA_ARGS__)

        extern "C"
        JNIEXPORT jbyteArray JNICALL
        Java_com_example_flamapp_NativeBridge_processFrame(JNIEnv *env, jobject /* this */, jbyteArray input_, jint width, jint height) {
            jbyte *input = env->GetByteArrayElements(input_, NULL);
            if (!input) {
                return NULL;
            }
            int w = width;
            int h = height;
            int frameSize = w * h;
            // NV21: Y plane first frameSize bytes. We'll use Y as grayscale
            unsigned char *y_plane = (unsigned char *)input;

            // Prepare output RGBA buffer (4 bytes per pixel)
            int outSize = frameSize * 4;
            std::vector<unsigned char> outBuf(outSize);

            // Simple Sobel kernels
            int gx[3][3] = {{-1,0,1},{-2,0,2},{-1,0,1}};
            int gy[3][3] = {{-1,-2,-1},{0,0,0},{1,2,1}};

            // For each pixel excluding border
            for (int y = 1; y < h-1; ++y) {
                for (int x = 1; x < w-1; ++x) {
                    int sumX = 0;
                    int sumY = 0;
                    for (int ky = -1; ky <= 1; ++ky) {
                        for (int kx = -1; kx <= 1; ++kx) {
                            int yy = y + ky;
                            int xx = x + kx;
                            int val = y_plane[yy * w + xx] & 0xFF;
                            sumX += gx[ky+1][kx+1] * val;
                            sumY += gy[ky+1][kx+1] * val;
                        }
                    }
                    int mag = (int) std::sqrt((double)(sumX*sumX + sumY*sumY));
                    if (mag > 255) mag = 255;
                    if (mag < 0) mag = 0;
                    unsigned char edge = (unsigned char) mag;
                    int idx = (y * w + x) * 4;
                    outBuf[idx + 0] = edge; // R
                    outBuf[idx + 1] = edge; // G
                    outBuf[idx + 2] = edge; // B
                    outBuf[idx + 3] = 255;  // A
                }
            }
            // set borders to black
            for (int x = 0; x < w; ++x) {
                int top = (0 * w + x) * 4;
                int bot = ((h-1) * w + x) * 4;
                outBuf[top+0] = outBuf[top+1] = outBuf[top+2] = 0; outBuf[top+3]=255;
                outBuf[bot+0] = outBuf[bot+1] = outBuf[bot+2] = 0; outBuf[bot+3]=255;
            }
            for (int y = 0; y < h; ++y) {
                int left = (y * w + 0) * 4;
                int right = (y * w + (w-1)) * 4;
                outBuf[left+0] = outBuf[left+1] = outBuf[left+2] = 0; outBuf[left+3]=255;
                outBuf[right+0] = outBuf[right+1] = outBuf[right+2] = 0; outBuf[right+3]=255;
            }

            // Create jbyteArray to return
            jbyteArray outArray = env->NewByteArray(outSize);
            env->SetByteArrayRegion(outArray, 0, outSize, (jbyte*)outBuf.data());

            env->ReleaseByteArrayElements(input_, input, JNI_ABORT);
            return outArray;
        }

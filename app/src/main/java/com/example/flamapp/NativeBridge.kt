package com.example.flamapp

object NativeBridge {
    init { System.loadLibrary("native-lib") }
    external fun processFrame(input: ByteArray, width: Int, height: Int): ByteArray
}

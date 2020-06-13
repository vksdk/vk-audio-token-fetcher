package com.petersamokhin.vksdk.audiotokenfetcher.utils

import java.math.BigInteger
import java.nio.ByteOrder
import java.security.SecureRandom

private val CHAR_ARRAY = "0123456789abcdef".toCharArray()
private val secureRandom = SecureRandom()

fun ByteArray.toHexString(byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN): String {
    val buffer = CharArray(size * 2)
    var index: Int

    for (i in indices) {
        index = if (byteOrder === ByteOrder.BIG_ENDIAN) i else size - i - 1

        buffer[i shl 1] = CHAR_ARRAY[(this[index].toInt() shr 4 and 0xF)]
        buffer[(i shl 1) + 1] = CHAR_ARRAY[this[index].toInt() and 0xF]
    }
    return String(buffer)
}

fun generateRandomHexToken(byteLength: Int): String {
    val token = ByteArray(byteLength)
    secureRandom.nextBytes(token)
    return BigInteger(1, token).toString(16)
}
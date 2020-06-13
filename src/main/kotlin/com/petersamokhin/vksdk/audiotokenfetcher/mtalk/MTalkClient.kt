package com.petersamokhin.vksdk.audiotokenfetcher.mtalk

import com.petersamokhin.vksdk.audiotokenfetcher.data.error.MtalkCheckInException
import com.petersamokhin.vksdk.audiotokenfetcher.utils.toHexString
import java.net.Socket
import javax.net.ssl.SSLContext

internal class MTalkClient {
    private val sslSocketFactory by lazy {
        SSLContext.getDefault().socketFactory
    }

    fun checkInWith(androidId: Long, securityToken: Long) {
        val sslSocket = sslSocketFactory.createSocket(
            Socket(SERVICE_HOST, SERVICE_PORT),
            SERVICE_HOST,
            SERVICE_PORT,
            true
        )

        val outputStream = sslSocket.getOutputStream()
        val inputStream = sslSocket.getInputStream()

        outputStream.also {
            it.write(buildRequestBodyArray(androidId, securityToken))
        }

        inputStream.use {
            it.skip(1)

            it.read().also { responseCode ->
                if (responseCode != SUCCESS_RESPONSE_CODE) {
                    throw MtalkCheckInException(
                        responseCode
                    )
                }
            }
        }

        outputStream.close()
        sslSocket.close()
    }

    private fun buildRequestBodyArray(
        androidId: Long, securityToken: Long
    ): ByteArray {
        val rawId = androidId.toString().toByteArray()
        val rawToken = securityToken.toString().toByteArray()

        val idLen = writeVarint(rawId.size)
        val hexIdRaw = (HEX_ID_PREFIX + rawId.toHexString()).toByteArray()

        val msg = byteArrayOf(
            *messageBytesPrefix,
            *idLen, *rawId,
            0x22,
            *idLen, *rawId,
            0x2a,
            *writeVarint(rawToken.size), *rawToken,
            0x32,
            *writeVarint(hexIdRaw.size), *hexIdRaw,
            *messageBytesSuffix
        )

        return byteArrayOf(
            0x29, 0x02,
            *writeVarint(msg.size),
            *msg
        )
    }

    private fun writeVarint(value: Int): ByteArray {
        var longValue = value.toLong()
        val list = mutableListOf<Long>()

        while (true) {
            longValue = if (longValue and 0x7FL.inv() == 0L) {
                list.add(longValue)
                break
            } else {
                list.add(longValue and 0x7F or 0x80)
                longValue ushr 7
            }
        }
        return list.map(Long::toByte).toByteArray()
    }

    companion object {
        private const val SERVICE_HOST = "mtalk.google.com"
        private const val SERVICE_PORT = 5228

        private const val SUCCESS_RESPONSE_CODE = 3

        private const val HEX_ID_PREFIX = "android-"

        private val messageBytesPrefix = byteArrayOf(
            0x0a, 0x0a, 0x61, 0x6e, 0x64, 0x72,
            0x6f, 0x69, 0x64, 0x2d, 0x31, 0x39,
            0x12, 0x0f, 0x6d, 0x63, 0x73, 0x2e,
            0x61, 0x6e, 0x64, 0x72, 0x6f, 0x69,
            0x64, 0x2e, 0x63, 0x6f, 0x6d, 0x1a
        )

        private val messageBytesSuffix = byteArrayOf(
            0x42, 0x0b, 0x0a, 0x06, 0x6e, 0x65,
            0x77, 0x5f, 0x76, 0x63, 0x12, 0x01,
            0x31, 0x60, 0x00, 0x70, 0x01, 0x80.toByte(),
            0x01, 0x02, 0x88.toByte(), 0x01, 0x01
        )
    }
}
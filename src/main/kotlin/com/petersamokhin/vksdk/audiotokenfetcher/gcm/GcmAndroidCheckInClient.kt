package com.petersamokhin.vksdk.audiotokenfetcher.gcm

import com.petersamokhin.vksdk.audiotokenfetcher.data.model.gcm.CheckInResponse
import com.petersamokhin.vksdk.audiotokenfetcher.utils.generateRandomHexToken
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.content.ByteArrayContent
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.formUrlEncode
import kotlinx.serialization.protobuf.ProtoBuf

internal class GcmAndroidCheckInClient(
    private val httpClient: HttpClient
) {
    suspend fun checkIn() = httpClient.post<ByteArray> {
        url(CHECK_IN_URL)
        checkInHeaders.forEach(::header)
        body = ByteArrayContent(
            bytes = requestBodyBytes,
            contentType = CONTENT_TYPE
        )
    }.decodeCheckInResponse()

    suspend fun getReceipt(authData: CheckInResponse, userId: Int?): ByteArray = httpClient.post {
        url(RECEIPT_URL)
        header(
            HttpHeaders.Authorization,
            "AidLogin ${authData.androidId}:${authData.securityToken}"
        )
        checkInHeaders.forEach(::header)
        body = TextContent(
            buildGetReceiptRequestBody(authData.androidId, userId),
            ContentType.Application.FormUrlEncoded
        )
    }

    private fun ByteArray.decodeCheckInResponse(): CheckInResponse =
        ProtoBuf.load(CheckInResponse.serializer(), this)

    private fun buildGetReceiptRequestBody(
        deviceId: Long,
        userId: Int?
    ): String = mutableListOf(
        "X-scope" to "GCM",
        "X-osv" to "23",
        "X-subtype" to "54740537194",
        "X-X-subtype" to "54740537194",
        "X-subscription" to "54740537194",
        "X-X-subscription" to "54740537194",
        "sender" to "54740537194",
        "app_ver" to "445",
        "X-app_ver" to "445",
        "X-appid" to generateRandomHexToken(11),
        "X-gmsv" to "13283005",
        "X-cliv" to "iid-10084000",
        "X-app_ver_name" to "52.1 lite",
        "app" to "com.perm.kate_new_6",
        "device" to "$deviceId",
        "cert" to "966882ba564c2619d55d0a9afd4327a38c327456",
        "info" to "w8LuNo60zr8UUO6eTSP7b7U4vzObdhY",
        "gcm_ver" to "13283005",
        "plat" to "0",
        "X-messenger2" to "1"
    ).also {
        if (userId != null) {
            it.add("X-scope" to "id$userId")
            it.add("X-kid" to "|ID|2|")
            it.add("X-X-kid" to "|ID|2|")
        } else {
            it.add("X-kid" to "|ID|1|")
            it.add("X-X-kid" to "|ID|1|")
        }
    }.formUrlEncode()

    companion object {
        private const val CHECK_IN_URL = "https://android.clients.google.com/checkin"
        private const val RECEIPT_URL = "https://android.clients.google.com/c2dm/register3"

        private const val USER_AGENT = "Android-GCM/1.5 (generic_x86 KK)"
        private val CONTENT_TYPE = ContentType.parse("application/x-protobuffer")
        private const val EXPECT_HEADER_NAME = "Expect"
        private const val EMPTY_STRING = ""

        private val checkInHeaders = mapOf(
            HttpHeaders.UserAgent to USER_AGENT,
            EXPECT_HEADER_NAME to EMPTY_STRING
        )

        private val requestBodyBytes = byteArrayOf(
            16, 0, 26, 42, 49, 45, 57, 50, 57, 97, 48, 100, 99, 97, 48, 101, 101, 101,
            53, 53, 53, 49, 51, 50, 56, 48, 49, 55, 49, 97, 56, 53, 56, 53, 100, 97,
            55, 100, 99, 100, 51, 55, 48, 48, 102, 56, 34, -29, 1, 10, -65, 1, 10, 69,
            103, 101, 110, 101, 114, 105, 99, 95, 120, 56, 54, 47, 103, 111, 111, 103,
            108, 101, 95, 115, 100, 107, 95, 120, 56, 54, 47, 103, 101, 110, 101, 114,
            105, 99, 95, 120, 56, 54, 58, 52, 46, 52, 46, 50, 47, 75, 75, 47, 51, 48, 55,
            57, 49, 56, 51, 58, 101, 110, 103, 47, 116, 101, 115, 116, 45, 107, 101, 121,
            115, 18, 6, 114, 97, 110, 99, 104, 117, 26, 11, 103, 101, 110, 101, 114, 105,
            99, 95, 120, 56, 54, 42, 7, 117, 110, 107, 110, 111, 119, 110, 50, 14, 97, 110,
            100, 114, 111, 105, 100, 45, 103, 111, 111, 103, 108, 101, 64, -123, -75, -122,
            6, 74, 11, 103, 101, 110, 101, 114, 105, 99, 95, 120, 56, 54, 80, 19, 90, 25,
            65, 110, 100, 114, 111, 105, 100, 32, 83, 68, 75, 32, 98, 117, 105, 108, 116,
            32, 102, 111, 114, 32, 120, 56, 54, 98, 7, 117, 110, 107, 110, 111, 119, 110,
            106, 14, 103, 111, 111, 103, 108, 101, 95, 115, 100, 107, 95, 120, 56, 54, 112,
            0, 16, 0, 50, 6, 51, 49, 48, 50, 54, 48, 58, 6, 51, 49, 48, 50, 54, 48, 66, 11,
            109, 111, 98, 105, 108, 101, 58, 76, 84, 69, 58, 72, 0, 50, 5, 101, 110, 95, 85,
            83, 56, -16, -76, -33, -90, -71, -102, -72, -125, -114, 1, 82, 15, 51, 53, 56,
            50, 52, 48, 48, 53, 49, 49, 49, 49, 49, 49, 48, 90, 0, 98, 16, 65, 109, 101,
            114, 105, 99, 97, 47, 78, 101, 119, 95, 89, 111, 114, 107, 112, 3, 122, 28, 55,
            49, 81, 54, 82, 110, 50, 68, 68, 90, 108, 49, 122, 80, 68, 86, 97, 97, 101, 69,
            72, 73, 116, 100, 43, 89, 103, 61, -96, 1, 0, -80, 1, 0
        )
    }
}
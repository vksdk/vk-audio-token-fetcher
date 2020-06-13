package com.petersamokhin.vksdk.audiotokenfetcher

import com.petersamokhin.vksdk.audiotokenfetcher.data.error.VkInvalidTokenResponseException
import com.petersamokhin.vksdk.audiotokenfetcher.data.model.vk.VkAccessTokenData
import com.petersamokhin.vksdk.audiotokenfetcher.data.model.vk.VkAccessTokenResponse
import com.petersamokhin.vksdk.audiotokenfetcher.data.model.vk.VkRefreshedAccessTokenResponse
import com.petersamokhin.vksdk.audiotokenfetcher.gcm.GcmAndroidCheckInClient
import com.petersamokhin.vksdk.audiotokenfetcher.mtalk.MTalkClient
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import io.ktor.http.formUrlEncode
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.parse

/**
 * VK Token fetcher
 *
 * Uses https://github.com/microg approach to do the GMS check-in.
 * `receipt` is required for the access token confirmation for VK.
 * This confirmation is required for accessing e.g. private VK Audio API.
 *
 * @param httpClient ktor HTTP client
 * @param json Kotlin serialization Json wrapper
 */
class VkTokenFetcher(
    private val httpClient: HttpClient,
    private val json: Json
) {
    /**
     * Retrieve the confirmed VK access token
     *
     * @param username VK login, usually the phone number
     * @param password VK password
     * @param scopes Scopes string, e.g. "audio,offline"
     *
     * @return VK Access token and user ID
     */
    suspend fun getToken(
        username: String,
        password: String,
        scopes: String
    ): VkAccessTokenData {
        val gcl = GcmAndroidCheckInClient(httpClient)
        val mTalkClient = MTalkClient()

        val authData = gcl.checkIn()
        mTalkClient.checkInWith(authData)

        val badToken = getNonRefreshedToken(username, password, scopes)

        gcl.getReceipt(authData, null)

        val finalResponse = gcl.getReceipt(authData, badToken.requireUserId())

        val receipt = String(finalResponse).split(RECEIPT_DELIMITER)[1]
        val refreshedToken = refreshToken(badToken.requireAccessToken(), receipt)

        return VkAccessTokenData(
            refreshedToken.requireToken(),
            badToken.requireUserId()
        )
    }

    @OptIn(ImplicitReflectionSerializer::class)
    private suspend fun getNonRefreshedToken(
        username: String,
        password: String,
        scopes: String
    ): VkAccessTokenResponse = httpClient.get<String> {
        url(buildString {
            append(VK_API_BASE_URL_GET_TOKEN)
            append('?')
            append(
                listOf(
                    "grant_type" to "password",
                    "client_id" to "$KATE_MOBILE_CLIENT_ID",
                    "client_secret" to KATE_MOBILE_CLIENT_SECRET,
                    "username" to username,
                    "password" to password,
                    "v" to "$VK_API_V",
                    "scope" to scopes
                ).formUrlEncode()
            )
        })
        header(HttpHeaders.UserAgent, KATE_MOBILE_USER_AGENT)
    }.let {
        json.parse<VkAccessTokenResponse>(it).also { response ->
            if (!response.isValid()) {
                throw VkInvalidTokenResponseException("access_token", it)
            }
        }
    }

    @OptIn(ImplicitReflectionSerializer::class)
    private suspend fun refreshToken(
        badToken: String,
        receipt: String
    ) = httpClient.get<String> {
        url(buildString {
            append(VK_API_BASE_URL_REFRESH_TOKEN)
            append('?')
            append(
                listOf(
                    "access_token" to badToken,
                    "receipt" to receipt,
                    "v" to "$VK_API_V"
                ).formUrlEncode()
            )
        })
        header(HttpHeaders.UserAgent, KATE_MOBILE_USER_AGENT)
    }.let {
        json.parse<VkRefreshedAccessTokenResponse>(it).also { response ->
            if (!response.isValid()) {
                throw VkInvalidTokenResponseException("refresh_token", it)
            }
        }
    }

    companion object {
        private const val VK_API_BASE_URL_GET_TOKEN = "https://oauth.vk.com/token"
        private const val VK_API_BASE_URL_REFRESH_TOKEN = "https://api.vk.com/method/auth.refreshToken"
        private const val VK_API_V = 5.103

        private const val KATE_MOBILE_CLIENT_ID = 2685278
        private const val KATE_MOBILE_CLIENT_SECRET = "lxhD8OD7dMsqtXIm5IUY"
        private const val KATE_MOBILE_USER_AGENT =
            "KateMobileAndroid/61.1 lite-469 (Android 6.0.1; SDK 23; armeabi-v7a; samsung SM-G935F; ru)"

        private const val RECEIPT_DELIMITER = "|ID|2|:"
    }
}
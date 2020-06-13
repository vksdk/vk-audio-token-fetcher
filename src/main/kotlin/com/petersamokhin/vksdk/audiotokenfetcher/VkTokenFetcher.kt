package com.petersamokhin.vksdk.audiotokenfetcher

import com.petersamokhin.vksdk.audiotokenfetcher.data.error.VkInvalidTokenResponseException
import com.petersamokhin.vksdk.audiotokenfetcher.data.model.gcm.GcmCredentials
import com.petersamokhin.vksdk.audiotokenfetcher.data.model.vk.VkAccessTokenData
import com.petersamokhin.vksdk.audiotokenfetcher.data.model.vk.VkTokenAndUserData
import com.petersamokhin.vksdk.audiotokenfetcher.data.model.vk.VkTokenResponseWrapper
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
    private val gcl = GcmAndroidCheckInClient(httpClient)
    private val mTalkClient = MTalkClient()

    /**
     * Retrieve the confirmed VK access token.
     * All of the steps handled automatically.
     *
     * You can do the steps manually if you want to e.g. cache credentials.
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
        val (androidId, securityToken) = getUnconfirmedGcmCredentials()

        mTalkCheckIn(androidId, securityToken)

        val badToken = getUnconfirmedToken(username, password, scopes)

        val receipt = getReceiptForUser(androidId, securityToken, badToken.requireUserId())
        val refreshedToken = confirmToken(badToken.requireAccessToken(), receipt)

        return VkAccessTokenData(
            refreshedToken.requireToken(),
            badToken.requireUserId()
        )
    }

    suspend fun getUnconfirmedGcmCredentials(): GcmCredentials {
        val authData = gcl.checkIn()

        return GcmCredentials(
            androidId = authData.androidId,
            securityToken = authData.securityToken
        )
    }

    fun mTalkCheckIn(androidId: Long, securityToken: Long) {
        mTalkClient.checkInWith(androidId, securityToken)
    }

    suspend fun getReceiptForUser(
        androidId: Long,
        securityToken: Long,
        userId: Int
    ): String {
        // two steps required
        gcl.getReceipt(androidId, securityToken, null)

        return String(
            gcl.getReceipt(
                androidId = androidId,
                securityToken = securityToken,
                userId = userId
            )
        ).split(RECEIPT_DELIMITER)[1]
    }

    @OptIn(ImplicitReflectionSerializer::class)
    suspend fun getUnconfirmedToken(
        username: String,
        password: String,
        scopes: String
    ) = httpClient.get<String> {
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
        json.parse<VkTokenAndUserData>(it).also { response ->
            if (!response.isValid()) {
                throw VkInvalidTokenResponseException("access_token", it)
            }
        }
    }

    @OptIn(ImplicitReflectionSerializer::class)
    suspend fun confirmToken(
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
        json.parse<VkTokenResponseWrapper>(it).also { response ->
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
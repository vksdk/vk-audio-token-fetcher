package com.petersamokhin.vksdk.audiotokenfetcher.data.model.vk

import com.petersamokhin.vksdk.audiotokenfetcher.data.error.VkInvalidTokenResponseException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class VkAccessTokenResponse(
    @SerialName("access_token")
    val accessToken: String? = null,

    @SerialName("user_id")
    val userId: Int? = null
) {
    fun isValid() = userId != null && userId > 0 && !accessToken.isNullOrEmpty()

    fun requireAccessToken() = accessToken ?: throw VkInvalidTokenResponseException("access_token")

    fun requireUserId() = userId ?: throw VkInvalidTokenResponseException("access_token")
}
package com.petersamokhin.vksdk.audiotokenfetcher.data.model.vk

import com.petersamokhin.vksdk.audiotokenfetcher.data.error.VkInvalidTokenResponseException
import kotlinx.serialization.Serializable

@Serializable
data class VkTokenResponseWrapper(
    val response: VkTokenWrapper? = null
) {
    fun isValid() = !response?.token.isNullOrEmpty()

    fun requireToken() = response?.token ?: throw VkInvalidTokenResponseException("refresh_token")
}
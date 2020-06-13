package com.petersamokhin.vksdk.audiotokenfetcher.data.model.vk

import kotlinx.serialization.Serializable

@Serializable
data class VkTokenWrapper(
    val token: String? = null
)
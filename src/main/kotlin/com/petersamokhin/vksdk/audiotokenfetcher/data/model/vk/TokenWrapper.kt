package com.petersamokhin.vksdk.audiotokenfetcher.data.model.vk

import kotlinx.serialization.Serializable

@Serializable
internal data class TokenWrapper(
    val token: String? = null
)
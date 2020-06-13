package com.petersamokhin.vksdk.audiotokenfetcher.data.error

class VkInvalidTokenResponseException(
    type: String, str: String = ""
): IllegalStateException(
    "Bad VK $type response $str"
)
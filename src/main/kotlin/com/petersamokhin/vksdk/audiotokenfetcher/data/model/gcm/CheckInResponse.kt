package com.petersamokhin.vksdk.audiotokenfetcher.data.model.gcm

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kotlinx.serialization.protobuf.ProtoIntegerType
import kotlinx.serialization.protobuf.ProtoType

@Serializable
internal data class CheckInResponse @OptIn(ExperimentalSerializationApi::class) constructor(
    @ProtoNumber(7)
    @ProtoType(ProtoIntegerType.FIXED)
    val androidId: Long,

    @ProtoNumber(8)
    @ProtoType(ProtoIntegerType.FIXED)
    val securityToken: Long
)
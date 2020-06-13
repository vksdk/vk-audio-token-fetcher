package com.petersamokhin.vksdk.audiotokenfetcher.data.model.gcm

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import kotlinx.serialization.protobuf.ProtoNumberType
import kotlinx.serialization.protobuf.ProtoType

@Serializable
internal data class CheckInResponse(
    @ProtoId(7)
    @ProtoType(ProtoNumberType.FIXED)
    val androidId: Long,

    @ProtoId(8)
    @ProtoType(ProtoNumberType.FIXED)
    val securityToken: Long
)
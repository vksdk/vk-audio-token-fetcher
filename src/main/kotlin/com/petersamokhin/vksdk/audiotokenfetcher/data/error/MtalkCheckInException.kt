package com.petersamokhin.vksdk.audiotokenfetcher.data.error

class MtalkCheckInException(code: Int): IllegalStateException("Bad response code: $code")
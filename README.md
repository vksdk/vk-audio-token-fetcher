# VK Audio token fetcher

Unofficial tool for accessing private VK APIs, e.g. to access audio.

## Example
```kotlin
val vkTokenFetcher = VkTokenFetcher(
    httpClient = HttpClient(OkHttp),
    json = Json { /* ... */ }
)

val login = "+79991234567"
val password = "vk == **it"
val scopes = "audio,offline"

val (token, userId) = vkTokenFetcher.getToken(
    username = login,
    password = password,
    scopes = scopes
)
```

And then, you can use [VK Kotlin SDK](https://github.com/vksdk/vk-sdk-kotlin) to access API with obtained token.

## 3rd party
- Kotlin ([serialization](https://github.com/Kotlin/kotlinx.serialization))
- [ktor](https://github.com/ktorio/ktor) client

Also, thanks to [microG](https://github.com/microg).

## Disclaimer
License: see the [license](https://raw.githubusercontent.com/vksdk/vk-audio-token-fetcher/master/LICENSE).

For educational use only. There is no liability for the repository authors.

The main purpose of this library to be able to access the private VK audio APIs, which must not be private,
because there is a lot of audio media content which is not licensed but volunteered to the community by the authors,
but VK adds own advertisement and other limitations, earns money on this, and gives nothing to the authors.
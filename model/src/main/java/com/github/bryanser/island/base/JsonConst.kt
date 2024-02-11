package com.github.bryanser.island.base

import kotlinx.serialization.json.Json

val globalJson = Json {
    ignoreUnknownKeys = true
    allowStructuredMapKeys = true
}
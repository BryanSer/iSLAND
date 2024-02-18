package com.github.bryanser.island.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class PlayerData(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID
) {
}
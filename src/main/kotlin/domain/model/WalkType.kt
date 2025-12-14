package com.upet.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class WalkType {
    A_TO_B,
    TIME,
    DISTANCE,
    PREDEFINED
}
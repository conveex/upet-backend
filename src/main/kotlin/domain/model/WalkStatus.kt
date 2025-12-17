package com.upet.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class WalkStatus {
    PENDING,
    ACCEPTED,
    STARTED,
    WALKER_FINISHED,
    COMPLETED,
    CANCELLED,
    REVIEW_PENDING,
    INACTIVE
}
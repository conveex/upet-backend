package com.upet.walks

import kotlinx.serialization.Serializable

@Serializable
data class WalkActiveListEnvelope(
    val success: Boolean,
    val message: String,
    val walks: List<WalkSummaryResponse>
)

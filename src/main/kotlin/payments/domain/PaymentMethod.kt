package com.upet.payments.domain

import kotlinx.datetime.LocalDateTime
import java.util.UUID

data class PaymentMethod(
    val id: UUID,
    val code: String,
    val displayName: String,
    val description: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
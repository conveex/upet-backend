package com.upet.payments

import kotlinx.serialization.Serializable

@Serializable
data class UserPaymentMethodResponse(
    val id: String,                  // id fila client/walker_payment_methods
    val paymentMethodId: String,     // id de payment_methods
    val code: String,                // CASH, TRANSFER, etc.
    val displayName: String,
    val description: String?,
    val extraDetails: String?
)

@Serializable
data class AddUserPaymentMethodRequest(
    val paymentMethodId: String,
    val extraDetails: String? = null
)

@Serializable
data class UserPaymentMethodsEnvelope(
    val success: Boolean,
    val message: String,
    val methods: List<UserPaymentMethodResponse>
)
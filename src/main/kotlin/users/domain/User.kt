package com.upet.users.domain

import com.upet.domain.model.UserStatus
import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val name: String,
    val phone: String?,
    val mainAddress: String?,
    val photoUrl: String?,
    val emailVerified: Boolean,
    val fcmToken: String?,
    val isClient: Boolean,
    val isWalker: Boolean,
    val isAdmin: Boolean,
    val status: UserStatus
)
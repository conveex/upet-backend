package com.upet.auth

import at.favre.lib.crypto.bcrypt.BCrypt

object PasswordService {

    private val hasher = BCrypt.withDefaults()
    private val verifier = BCrypt.verifyer()

    fun hash(password: String): String {
        return hasher.hashToString(12, password.toCharArray())
    }

    fun verify(plain: String, hash: String): Boolean {
        return verifier.verify(plain.toCharArray(), hash).verified
    }
}
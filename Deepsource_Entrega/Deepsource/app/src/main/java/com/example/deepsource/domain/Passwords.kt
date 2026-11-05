package com.example.deepsource.domain

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class PasswordDigest(val salt: ByteArray, val hash: ByteArray, val iterations: Int)

object Passwords {
    const val ITERATIONS = 210_000
    fun validate(password: String): String? = when {
        password.length !in 8..128 -> "Usa entre 8 y 128 caracteres"
        password.none(Char::isLetter) || password.none(Char::isDigit) -> "Incluye al menos una letra y un número"
        else -> null
    }
    fun create(password: String): PasswordDigest {
        require(validate(password)==null) { validate(password).orEmpty() }
        val salt=ByteArray(16).also {SecureRandom().nextBytes(it)}
        return PasswordDigest(salt,derive(password,salt,ITERATIONS),ITERATIONS)
    }
    fun verify(password: String, digest: PasswordDigest): Boolean =
        MessageDigest.isEqual(digest.hash,derive(password,digest.salt,digest.iterations))
    private fun derive(password: String,salt: ByteArray,iterations: Int):ByteArray {
        val spec=PBEKeySpec(password.toCharArray(),salt,iterations,256)
        return try {SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded}
        finally {spec.clearPassword()}
    }
}

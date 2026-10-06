package com.mylockapp.data

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PinHasher {
    private const val ITERATIONS = 150_000

    fun newSalt(): ByteArray = ByteArray(16).also { SecureRandom().nextBytes(it) }

    fun hash(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, 256)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }

    fun encode(b: ByteArray): String = Base64.encodeToString(b, Base64.NO_WRAP)
    fun decode(s: String): ByteArray = Base64.decode(s, Base64.NO_WRAP)
    fun constantTimeEquals(a: ByteArray, b: ByteArray) = MessageDigest.isEqual(a, b)
}

package com.example.util

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Password hashing utilities.
 * New credentials use PBKDF2 with a per-password random salt. Older SHA-256
 * and plaintext records remain verifiable only so successful logins can be
 * upgraded without forcing an immediate database reset.
 */
object SecurityUtils {
    private const val CURRENT_PREFIX = "pbkdf2"
    private const val ITERATIONS = 210_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16

    // Compatibility only: this was the fixed salt used by older app versions.
    private const val LEGACY_PASSWORD_SALT = "BARAYA_STUDIO_AUTH_SALT_2026_@v1"
    private val random = SecureRandom()

    fun hashPassword(password: String): String {
        require(password.isNotEmpty()) { "Password must not be empty" }
        val salt = ByteArray(SALT_LENGTH_BYTES).also(random::nextBytes)
        val algorithm = preferredPbkdf2Algorithm()
        val derivedKey = deriveKey(password, salt, ITERATIONS, algorithm)
        return listOf(
            CURRENT_PREFIX,
            algorithm.removePrefix("PBKDF2WithHmac" ).lowercase(),
            ITERATIONS.toString(),
            salt.toHex(),
            derivedKey.toHex()
        ).joinToString("\$")
    }

    fun verifyPassword(rawPassword: String, storedPasswordOrHash: String): Boolean {
        if (rawPassword.isBlank() || storedPasswordOrHash.isBlank()) return false

        if (storedPasswordOrHash.startsWith("${CURRENT_PREFIX}\$")) {
            return verifyPbkdf2(rawPassword, storedPasswordOrHash)
        }

        // Legacy salted SHA-256. Retained only for a one-time login migration.
        val legacyHash = legacySha256(rawPassword)
        if (storedPasswordOrHash.matches(Regex("[a-fA-F0-9]{64}"))) {
            return constantTimeEquals(legacyHash, storedPasswordOrHash.lowercase())
        }

        // Legacy plaintext records are accepted once, then upgraded at login.
        return constantTimeEquals(rawPassword, storedPasswordOrHash)
    }

    fun needsUpgrade(storedPassword: String): Boolean =
        !storedPassword.startsWith("${CURRENT_PREFIX}\$")

    /** Identifies publicly known seed credentials from previous app builds. */
    fun isKnownInsecureDefaultPassword(storedPassword: String): Boolean {
        val knownDefaults = listOf("password123", "2026", "MohamedBaraya@2026")
        return knownDefaults.any { default ->
            storedPassword == default || constantTimeEquals(legacySha256(default), storedPassword.lowercase())
        }
    }

    fun isPasswordValid(password: String): Boolean = password.trim().length >= 8

    private fun verifyPbkdf2(rawPassword: String, encoded: String): Boolean {
        return try {
            val parts = encoded.split('$')
            if (parts.size != 5 || parts[0] != CURRENT_PREFIX) return false

            val algorithmSuffix = parts[1]
            val algorithm = when (algorithmSuffix) {
                "sha256" -> "PBKDF2WithHmacSHA256"
                "sha1" -> "PBKDF2WithHmacSHA1"
                else -> return false
            }
            val iterations = parts[2].toIntOrNull() ?: return false
            if (iterations !in 100_000..1_000_000) return false

            val salt = parts[3].fromHex() ?: return false
            val expected = parts[4].fromHex() ?: return false
            if (salt.size < 16 || expected.size != KEY_LENGTH_BITS / 8) return false

            val actual = deriveKey(rawPassword, salt, iterations, algorithm)
            MessageDigest.isEqual(expected, actual)
        } catch (_: Exception) {
            false
        }
    }

    private fun preferredPbkdf2Algorithm(): String = try {
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        "PBKDF2WithHmacSHA256"
    } catch (_: Exception) {
        // Supported on older Android versions where the SHA-256 PBKDF2 provider may be absent.
        "PBKDF2WithHmacSHA1"
    }

    private fun deriveKey(
        password: String,
        salt: ByteArray,
        iterations: Int,
        algorithm: String
    ): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
        return try {
            SecretKeyFactory.getInstance(algorithm).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun legacySha256(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest((password + LEGACY_PASSWORD_SALT).toByteArray(Charsets.UTF_8))
        return digest.toHex()
    }

    private fun constantTimeEquals(first: String, second: String): Boolean =
        MessageDigest.isEqual(first.toByteArray(Charsets.UTF_8), second.toByteArray(Charsets.UTF_8))

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it.toInt() and 0xff) }

    private fun String.fromHex(): ByteArray? {
        if (isEmpty() || length % 2 != 0 || !matches(Regex("[a-fA-F0-9]+"))) return null
        return ByteArray(length / 2) { index ->
            substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }
    }
}

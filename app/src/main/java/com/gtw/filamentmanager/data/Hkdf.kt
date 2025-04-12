package com.gtw.filamentmanager.data

import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * A Kotlin implementation of the HMAC-based Extract-and-Expand Key Derivation Function (HKDF)
 * as defined in RFC 5869.
 *
 * This class provides methods to derive one or more subkeys from a master secret using HKDF.
 * It supports SHA-256 and SHA-512 as the underlying hash algorithms.
 */
class Hkdf private constructor(
    private val macAlgorithm: String,
) {

    /**
     * Performs the HKDF-Extract step.
     *
     * @param salt The salt (non-secret random value).
     * @param masterKey The master key.
     * @return The pseudo-random key (PRK).
     * @throws InvalidKeyException If the salt or masterKey is invalid.
     * @throws NoSuchAlgorithmException If the MAC algorithm is not available.
     */
    @Throws(InvalidKeyException::class, NoSuchAlgorithmException::class)
    private fun extract(salt: ByteArray, masterKey: ByteArray): ByteArray {
        val mac = Mac.getInstance(macAlgorithm)
        mac.init(SecretKeySpec(salt, macAlgorithm))
        return mac.doFinal(masterKey)
    }

    /**
     * Derives one or more subkeys of a specific length from the pseudo-random key.
     *
     * @param info Contextual information used to bind the derived key to a specific usage.
     * @param keyLength The desired length of each derived key in bytes.
     * @param numKeys The number of keys to derive.
     * @return The derived subkey, or a list of derived subkeys.
     * @throws InvalidKeyException If the info is invalid.
     * @throws NoSuchAlgorithmException If the MAC algorithm is not available.
     */
    @Throws(InvalidKeyException::class, NoSuchAlgorithmException::class)
    fun deriveKey(
        masterKey: ByteArray,
        salt: ByteArray,
        info: ByteArray,
        keyLength: Int,
        numKeys: Int = 1
    ): List<ByteArray> {
        val digestLength = getDigestLength()
        val prk = extract(salt, masterKey)
        // Maximum output length, based on the standard.
        val outputLength = keyLength * numKeys
        require(outputLength <= (255 * digestLength)) { "Requested key length $outputLength is too long, maximum allowed is ${255 * digestLength}" }
        // Expand the pseudo-random key into a sequence of blocks.
        val t = expand(prk, info, outputLength)
        return (0 until numKeys).map {
            t.copyOfRange(it * keyLength, (it + 1) * keyLength)
        }.toList()
    }

    /**
     * Expands the pseudo-random key into a sequence of blocks.
     *
     * @param prk The pseudo-random key.
     * @param info Contextual information used to bind the derived key to a specific usage.
     * @param outputLength The requested length of the output key, in bytes.
     * @return The concatenated blocks.
     * @throws InvalidKeyException If the info is invalid.
     * @throws NoSuchAlgorithmException If the MAC algorithm is not available.
     */
    @Throws(InvalidKeyException::class, NoSuchAlgorithmException::class)
    private fun expand(prk: ByteArray, info: ByteArray, outputLength: Int): ByteArray {
        val mac = Mac.getInstance(macAlgorithm)
        mac.init(SecretKeySpec(prk, macAlgorithm))
        val digestLength = getDigestLength()
        val t = mutableListOf<ByteArray>()
        var tLength = 0
        var n = 1
        var previousResult = ByteArray(0)
        while (tLength < outputLength) {
            // Concatenate T(i-1) || info || i.
            val currentInput = previousResult + info + byteArrayOf(n.toByte())
            val result = mac.doFinal(currentInput)
            t.add(result)
            previousResult = result
            tLength += digestLength
            n++
        }
        // Concatenate all blocks and return the required part.
        return t.reduce { acc, bytes -> acc + bytes }
    }

    /**
     * Get the output length for the underlying hash function, based on the MAC algorithm.
     *
     * @return the hash output length in bytes.
     * @throws NoSuchAlgorithmException if the algorithm is not supported.
     */
    @Throws(NoSuchAlgorithmException::class)
    private fun getDigestLength(): Int {
        val mac = Mac.getInstance(macAlgorithm)
        return mac.macLength
    }

    companion object {
        /**
         * Creates an instance of the HKDF, using the provided hash algorithm.
         *
         * @param hashAlgorithm the name of the hash algorithm, for example "HmacSHA256" or "HmacSHA512"
         * @return a Hkdf instance.
         * @throws NoSuchAlgorithmException if the algorithm is not supported.
         */
        @Throws(NoSuchAlgorithmException::class)
        fun getInstance(hashAlgorithm: String): Hkdf {
            return Hkdf(hashAlgorithm)
        }
    }
}
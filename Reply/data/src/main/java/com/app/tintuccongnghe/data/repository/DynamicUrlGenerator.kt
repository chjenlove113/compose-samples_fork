package com.app.tintuccongnghe.data.repository

import android.net.Uri
import com.app.tintuccongnghe.domain.models.SigConfig
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object DynamicUrlGenerator {

    fun generateUrl(
        baseUrl: String,
        sigConfig: SigConfig?,
        customParams: Map<String, String>
    ): String {
        if (sigConfig == null || sigConfig.secretSalt.isEmpty()) {
            val builder = Uri.parse(baseUrl).buildUpon()
            for ((k, v) in customParams) {
                if (v.isNotEmpty()) {
                    builder.appendQueryParameter(k, v)
                }
            }
            return builder.build().toString()
        }

        val ctime = (System.currentTimeMillis() / 1000).toString()
        
        val allParams = mutableMapOf<String, String>().apply {
            put("ctime", ctime)
            if (sigConfig.version.isNotEmpty()) put("version", sigConfig.version)
            if (sigConfig.apiKey.isNotEmpty()) put("apiKey", sigConfig.apiKey)
            putAll(customParams)
        }

        val filteredParams = allParams.filterKeys { key ->
            sigConfig.whitelistParams.contains(key)
        }.filterValues { value ->
            !value.isNullOrEmpty()
        }

        val sortedParams = filteredParams.toSortedMap()

        val paramString = sortedParams.entries.joinToString(separator = "") { (k, v) ->
            "$k=$v"
        }
        val stringToSign = sigConfig.pathname + paramString

        val signature = hmacSha256Hex(sigConfig.secretSalt, stringToSign)

        val builder = Uri.parse(baseUrl).buildUpon()
        for ((k, v) in sortedParams) {
            builder.appendQueryParameter(k, v)
        }
        // Ensure apiKey and version are always included in the query parameters if present
        if (sigConfig.apiKey.isNotEmpty() && !sortedParams.containsKey("apiKey")) {
            builder.appendQueryParameter("apiKey", sigConfig.apiKey)
        }
        if (sigConfig.version.isNotEmpty() && !sortedParams.containsKey("version")) {
            builder.appendQueryParameter("version", sigConfig.version)
        }
        builder.appendQueryParameter("sig", signature)

        return builder.build().toString()
    }

    private fun hmacSha256Hex(key: String, data: String): String {
        return try {
            val secretKeySpec = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA256")
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(secretKeySpec)
            val bytes = mac.doFinal(data.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            ""
        }
    }
}

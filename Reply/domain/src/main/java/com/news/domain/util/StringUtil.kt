package com.news.domain.util

import android.util.Patterns

fun isDomainAbsentSimple(urlString: String, domainName: String): Boolean {
    // Checks if the URL doesn't start with the domain prefix
    return !urlString.startsWith("https://$domainName") &&
            !urlString.startsWith("http://$domainName")
}

fun isValidUrl(url: String): Boolean {
    return Patterns.WEB_URL.matcher(url).matches()
}
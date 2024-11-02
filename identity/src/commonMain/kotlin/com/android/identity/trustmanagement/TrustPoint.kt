package com.android.identity.trustmanagement

import com.android.identity.cbor.CborMap
import com.android.identity.cbor.DataItem
import com.android.identity.crypto.X509Cert

/**
 * Class used for the representation of a trusted CA [X509Cert], a name
 * suitable for display and an icon to display the certificate.
 *
 * @param certificate an X509 certificate
 * @param displayName a name suitable for display of the X509 certificate
 * @param displayIcon an icon that represents
 */
data class TrustPoint(
    val certificate: X509Cert,
    val displayName: String? = null,
    val displayIcon: ByteArray? = null
) {
    /**
     * Converts the trust point to a [DataItem].
     */
    fun toDataItem(): DataItem {
        val trustPointBuilder = CborMap.builder().apply {
            put("certificate", certificate.toDataItem())
            displayName?.let { put("displayName", it) }
            displayIcon?.let { put("displayIcon", it) }
        }
        return trustPointBuilder.end().build()
    }

    companion object {
        /**
         * Creates a [TrustPoint] from a [DataItem].
         *
         * @param dataItem must have been encoded with [toDataItem].
         */
        fun fromDataItem(dataItem: DataItem): TrustPoint {
            val certificate = X509Cert.fromDataItem(dataItem["certificate"])

            val displayName = dataItem.getOrNull("displayName")?.asTstr
            val displayIcon = dataItem.getOrNull("displayIcon")?.asBstr

            return TrustPoint(certificate, displayName, displayIcon)
        }
    }
}
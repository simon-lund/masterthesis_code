package com.android.identity.appsupport.ui.consent

import com.android.identity.cbor.CborMap
import com.android.identity.cbor.DataItem
import com.android.identity.trustmanagement.TrustPoint

/**
 * Details about the Relying Party requesting data.
 *
 * TODO: also add appId.
 *
 * @property trustPoint if the verifier is in a trust-list, the [TrustPoint] indicating this
 * @property websiteOrigin set if the verifier is a website, for example https://gov.example.com
 */
data class  ConsentRelyingParty(
    val trustPoint: TrustPoint?,
    val websiteOrigin: String? = null,
) {
    /**
     * Converts the relying party to a [DataItem].
     */
    fun toDataItem(): DataItem {
        val relyingPartyBuilder = CborMap.builder().apply {
            trustPoint?.let { put("trustPoint", it.toDataItem()) }
            websiteOrigin?.let { put("websiteOrigin", it) }
        }
        return relyingPartyBuilder.end().build()
    }

    companion object {
        /**
         * Creates a [ConsentRelyingParty] from a [DataItem].
         *
         * @param dataItem must have been encoded with [toDataItem].
         */
        fun fromDataItem(dataItem: DataItem): ConsentRelyingParty {
            val trustPoint = dataItem.getOrNull("trustPoint")?.let { TrustPoint.fromDataItem(it) }
            val websiteOrigin = dataItem.getOrNull("websiteOrigin")?.asTstr
            return ConsentRelyingParty(trustPoint, websiteOrigin)
        }
    }
}
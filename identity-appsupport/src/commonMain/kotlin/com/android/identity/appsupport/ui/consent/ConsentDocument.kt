package com.android.identity.appsupport.ui.consent

import com.android.identity.cbor.CborMap
import com.android.identity.cbor.DataItem

/**
 * Details with the document that is being presented in the consent dialog.
 *
 * @property name the name of the document e.g. "Erika's Driving License"
 * @property description the description e.g. "Driving License" or "Government-Issued ID"
 * @property cardArt the card art for the document
 */
data class ConsentDocument(
    val name: String,
    val description: String,
    val cardArt: ByteArray
) {
    /**
     * Converts the consent document to a [DataItem].
     */
    fun toDataItem(): DataItem {
        val documentBuilder = CborMap.builder().apply {
            put("name", name)
            put("description", description)
            put("cardArt", cardArt)
        }
        return documentBuilder.end().build()
    }

    companion object {
        /**
         * Creates a [ConsentDocument] from a [DataItem].
         *
         * @param dataItem must have been encoded with [toDataItem].
         */
        fun fromDataItem(dataItem: DataItem): ConsentDocument {
            val name = dataItem["name"].asTstr
            val description = dataItem["description"].asTstr
            val cardArt = dataItem["cardArt"].asBstr
            return ConsentDocument(name, description, cardArt)
        }
    }
}
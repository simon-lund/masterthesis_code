package com.android.identity.appsupport.ui.consent

import com.android.identity.cbor.CborMap
import com.android.identity.cbor.DataItem
import com.android.identity.documenttype.DocumentAttribute

/**
 * Base class used for representing items in the consent prompt.
 *
 * @param displayName the text to display in consent prompt for the requested field.
 * @param attribute a [DocumentAttribute], if the data element is well-known.
 */
open class ConsentField(
    open val displayName: String,
    open val attribute: DocumentAttribute?
) {
    /**
     * Converts the consent field to a [DataItem].
     */
    fun toDataItem(): DataItem {
        return CborMap.builder().apply {
            put("displayName", displayName)
            attribute?.let { put("attribute", it.toDataItem()) }
        }.end().build()
    }

    companion object {
        /**
         * Creates a [ConsentField] from a [DataItem].
         *
         * @param dataItem must have been encoded with [toDataItem].
         */
        fun fromDataItem(dataItem: DataItem): ConsentField {
            val displayName = dataItem["displayName"].asTstr
            val attribute = dataItem.getOrNull("attribute")?.let { DocumentAttribute.fromDataItem(it) }
            return ConsentField(displayName, attribute)
        }
    }

}
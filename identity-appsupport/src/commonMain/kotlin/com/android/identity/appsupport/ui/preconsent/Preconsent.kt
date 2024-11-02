package com.android.identity.appsupport.ui.preconsent

import com.android.identity.appsupport.ui.consent.ConsentDocument
import com.android.identity.appsupport.ui.consent.ConsentField
import com.android.identity.appsupport.ui.consent.ConsentRelyingParty
import com.android.identity.cbor.CborMap
import com.android.identity.cbor.DataItem
import com.android.identity.util.UUID

/**
 * Details with the document that is being displayed in the pre-consent list.
 *
 * @property id an identifier for the preconsent document, automatically generated if not provided
 * @property document the document whose fields are shared with the relying party
 * @property relyingParty the relying party that will receive the shared fields
 * @property consentFields the fields that will be shared with the relying party
 */
data class Preconsent(
    val id: String = UUID.randomUUID().toString(),
    val document: ConsentDocument,
    val relyingParty: ConsentRelyingParty,
    val consentFields: List<ConsentField>,
) {
    /**
     * Converts the preconsent to a [DataItem].
     */
    fun toDataItem(): DataItem {
        val preconsentBuilder = CborMap.builder().apply {
            put("id", id)
            put("document", document.toDataItem())
            put("relyingParty", relyingParty.toDataItem())

            val consentFieldsArray = putArray("consentFields")
            for (field in consentFields) {
                consentFieldsArray.add(field.toDataItem())
            }
        }
        return preconsentBuilder.end().build()
    }

    companion object {
        /**
         * Creates a [Preconsent] from a [DataItem].
         *
         * @param dataItem must have been encoded with [toDataItem].
         */
        fun fromDataItem(dataItem: DataItem): Preconsent {
            val id = dataItem["id"].asTstr
            val document = ConsentDocument.fromDataItem(dataItem["document"])
            val relyingParty = ConsentRelyingParty.fromDataItem(dataItem["relyingParty"])

            val consentFields = dataItem["consentFields"].asArray.map { ConsentField.fromDataItem(it) }

            return Preconsent(id, document, relyingParty, consentFields)
        }
    }
}


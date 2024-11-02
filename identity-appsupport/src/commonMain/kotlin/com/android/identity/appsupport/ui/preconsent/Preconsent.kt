package com.android.identity.appsupport.ui.preconsent

import com.android.identity.appsupport.ui.consent.ConsentDocument
import com.android.identity.appsupport.ui.consent.ConsentField
import com.android.identity.appsupport.ui.consent.ConsentRelyingParty
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
)
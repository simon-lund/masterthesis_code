package com.android.identity.appsupport.ui.preconsent

import com.android.identity.appsupport.ui.consent.ConsentDocument
import com.android.identity.appsupport.ui.consent.ConsentField
import com.android.identity.appsupport.ui.consent.ConsentRelyingParty

/**
 * Details with the document that is being displayed in the pre-consent list.
 *
 * @property document the document whose fields are shared with the relying party
 * @property relyingParty the relying party that will receive the shared fields
 * @property consentFields the fields that will be shared with the relying party
 */
data class Preconsent(
    val document: ConsentDocument,
    val relyingParty: ConsentRelyingParty,
    val consentFields: List<ConsentField>,
)
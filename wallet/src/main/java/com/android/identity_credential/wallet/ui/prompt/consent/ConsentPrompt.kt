package com.android.identity_credential.wallet.ui.prompt.consent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.FragmentActivity
import com.android.identity.appsupport.ui.consent.ConsentField
import com.android.identity.appsupport.ui.consent.ConsentModalBottomSheet
import com.android.identity.appsupport.ui.consent.ConsentDocument
import com.android.identity.appsupport.ui.consent.ConsentRelyingParty
import com.android.identity_credential.wallet.ui.theme.IdentityCredentialTheme
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Show the Consent prompt
 *
 * Async function that renders the Consent Prompt (Composable) from a Dialog Fragment.
 * Returns a [Boolean] identifying that user tapped on Confirm or Cancel button.
 */
suspend fun showConsentPrompt(
    activity: FragmentActivity,
    document: ConsentDocument,
    relyingParty: ConsentRelyingParty,
    consentFields: List<ConsentField>,
    preconsentEnabled: Boolean,
    isPreconsentAllowed: Boolean,
    addedFields: List<ConsentField>
): Pair<Boolean, Boolean> =
    suspendCancellableCoroutine { continuation ->
        // new instance of the ConsentPrompt bottom sheet dialog fragment but not shown yet
        val consentPrompt = ConsentPrompt(
            consentFields = consentFields,
            document = document,
            relyingParty = relyingParty,
            preconsentEnabled = preconsentEnabled,
            isPreconsentAllowed = isPreconsentAllowed,
            addedFields = addedFields,
            onConsentPromptResult = continuation::resume
        )
        // show the consent prompt fragment
        consentPrompt.show(activity.supportFragmentManager, "consent_prompt")
    }

/**
 * ConsentPrompt Dialog Fragment class that renders Consent prompt via Composition for the purposes
 * of running Consent Prompt synchronously and return the prompt's result as a [Boolean] rather than
 * deal with callback hell.
 *
 * Extends [BottomSheetDialogFragment] and shows up as an overlay above the current UI.
 */
class ConsentPrompt(
    private val consentFields: List<ConsentField>,
    private val document: ConsentDocument,
    private val relyingParty: ConsentRelyingParty,
    private val preconsentEnabled: Boolean,
    private val isPreconsentAllowed: Boolean,
    private val addedFields: List<ConsentField>,
    // First is consent, second is pre-consent
    private val onConsentPromptResult:  (Pair<Boolean, Boolean>) -> Unit
) : BottomSheetDialogFragment() {
    /**
     * Define the composable [ConsentModalBottomSheet] and issue callbacks to [onConsentPromptResult]
     * based on which button is tapped.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(requireContext()).apply {
            setContent {
                IdentityCredentialTheme {
                    // TODO: use sheetGesturesEnabled=false when available instead of confirmValueChanged
                    //  hack - see https://issuetracker.google.com/issues/288211587 for details
                    //
                    val sheetState = rememberModalBottomSheetState(
                        skipPartiallyExpanded = true,
                        confirmValueChange = { it != SheetValue.Hidden }
                    )

                    // define the ConsentPromptComposable (and show)
                    ConsentModalBottomSheet(
                        sheetState = sheetState,
                        consentFields = consentFields,
                        document = document,
                        relyingParty = relyingParty,
                        preconsentEnabled = preconsentEnabled,
                        isPreconsentAllowed = isPreconsentAllowed,
                        addedFields = addedFields,
                        // user accepted to send requested credential data
                        onConfirm = { setupPreConsent ->
                            // notify that the user tapped on the 'Confirm' button
                            onConsentPromptResult.invoke(Pair(true, setupPreConsent))
                            dismiss()
                        },
                        // user declined submitting data to requesting party
                        onCancel = {
                            // notify that the user tapped on the 'Cancel' button
                            onConsentPromptResult.invoke(Pair(false, false))
                            dismiss()
                        }
                    )
                }
            }
        }
}
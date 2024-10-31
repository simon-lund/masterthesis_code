package com.android.identity.appsupport.ui.preconsent

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.identity.appsupport.ui.consent.*
import identitycredential.identity_appsupport.generated.resources.Res
import identitycredential.identity_appsupport.generated.resources.preconsent_modal_bottom_sheet_button_delete
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource


/**
 * Bottom sheet for managing a pre-consent. Adapted from [ConsentModalBottomSheet]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreconsentBottomSheet(
    preconsent: Preconsent,
    sheetState: SheetState,
    onDelete: (id: String) -> Unit
) {
    // If a relying party is untrusted (i.e. trustPoint is null), throw an error
    // This should never happen in prod, because users can only set up pre-consents with trusted parties
    preconsent.relyingParty.trustPoint!!

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = { scope.launch { sheetState.hide() } },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            // Details displayed in the bottom sheet
            RelyingPartySection(preconsent.relyingParty)
            DocumentSection(preconsent.document)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusGroup()
                    .verticalScroll(scrollState)
                    .weight(0.9f, false)
            ) {
                RequestSection(preconsent.consentFields, preconsent.relyingParty)
            }

            // Delete action
            // TODO: translate texts
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = { scope.launch { sheetState.hide(); onDelete(preconsent.id) } }
                ) {
                    Text(stringResource(Res.string.preconsent_modal_bottom_sheet_button_delete))
                }
            }
        }
    }
}
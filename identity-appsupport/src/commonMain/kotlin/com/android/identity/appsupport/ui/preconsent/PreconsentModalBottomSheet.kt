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
import kotlinx.coroutines.launch


/**
 * Bottom sheet for managing a pre-consent. Adapted from [ConsentModalBottomSheet]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreconsentBottomSheet(
    document: ConsentDocument,
    relyingParty: ConsentRelyingParty,
    consentFields: List<ConsentField>,
    sheetState: SheetState,
    onDelete: () -> Unit,
) {
    // If a relying party is untrusted (i.e. trustPoint is null), throw an error
    // This should never happen in prod, because users can only set up pre-consents with trusted parties
    relyingParty.trustPoint!!

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
            RelyingPartySection(relyingParty)
            DocumentSection(document)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusGroup()
                    .verticalScroll(scrollState)
                    .weight(0.9f, false)
            ) {
                RequestSection(consentFields, relyingParty)
            }

            // Delete action
            // TODO: translate texts
            // TODO: add confirm dialog
            // TODO: implement button section
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = { scope.launch { sheetState.hide(); onDelete() } },
                ) {
                    // TODO: add translations
                    Text("Delete")
                }
            }
        }
    }
}
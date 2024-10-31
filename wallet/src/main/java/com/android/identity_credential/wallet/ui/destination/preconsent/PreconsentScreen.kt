package com.android.identity_credential.wallet.ui.destination.preconsent


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.android.identity.appsupport.ui.preconsent.Preconsent
import com.android.identity_credential.wallet.R
import com.android.identity_credential.wallet.navigation.WalletDestination
import com.android.identity_credential.wallet.ui.ScreenWithAppBarAndBackButton


/**
 * Screen for managing pre-consents.
 */
@Composable
fun PreconsentScreen(
    onNavigate: (String) -> Unit,
) {
    // TODO: get preconsent list from data store or smth for preconsents
    // TODO: Connect PRe-Consent Screen
    // TODO: The unexpanded card shows the relying Party name, the document name and the number of fields.
    // TODO: on click of a card, show a bottom sheet with more details about the preconsent
    // TODO: check if scrollable works and add scrollable stuff if required
    // TODO: should be sorted by card name
    // TODO: if time permits, implement section list by card
    // TODO: add info box
    val preconsents = emptyList<Preconsent>()

    ScreenWithAppBarAndBackButton(
        title = stringResource(id = R.string.preconsent_screen_title),
        onBackButtonClick = { onNavigate(WalletDestination.PopBackStack.route) },
        scrollable = true
    ) {
        if (preconsents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.preconsent_screen_no_preconsents),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        }
    }







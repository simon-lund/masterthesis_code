package com.android.identity_credential.wallet.ui.destination.preconsent


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.identity.appsupport.ui.preconsent.Preconsent
import com.android.identity.appsupport.ui.preconsent.PreconsentBottomSheet
import com.android.identity.appsupport.ui.preconsent.PreconsentList
import com.android.identity.appsupport.ui.preconsent.PreconsentStore
import com.android.identity_credential.wallet.R
import com.android.identity_credential.wallet.navigation.WalletDestination
import com.android.identity_credential.wallet.ui.ScreenWithAppBarAndBackButton
import kotlinx.coroutines.launch


/**
 * Screen for managing pre-consents.
 * Wire up the [preconsentStore] to the UI composed of a list of pre-consents and a bottom sheet to view and delete individual pre-consents.
 *
 * @param preconsentStore the store for pre-consents
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreconsentScreen(
    preconsentStore: PreconsentStore,
    onNavigate: (String) -> Unit,
) {

    val preconsents = preconsentStore.preconsents
    var currentPreconsent by remember { mutableStateOf<Preconsent?>(null) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

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
        } else {
            Row {
                Text(
                    text = stringResource(id = R.string.preconsent_screen_info),
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            PreconsentList(preconsents = preconsents, onPreconsentClick = {
                scope.launch {
                    currentPreconsent = it
                    sheetState.show()
                }
            })
            if (sheetState.isVisible && currentPreconsent != null) {
                PreconsentBottomSheet(
                    preconsent = currentPreconsent!!,
                    sheetState = sheetState,
                    onDelete = { id ->
                        scope.launch {
                            sheetState.hide()
                            preconsentStore.delete(id)
                            currentPreconsent = null
                        }
                    }
                )
            }
        }
    }
}







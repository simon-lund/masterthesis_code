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
 * @param onNavigate callback to navigate to another screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreconsentScreen(
    onNavigate: (String) -> Unit,
) {
    val preconsentStore = remember { PreconsentStore.getInstance() }
    var preconsents by remember { mutableStateOf(preconsentStore.preconsents) }
    var currentPreconsent by remember { mutableStateOf<Preconsent?>(null) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    ScreenWithAppBarAndBackButton(
        title = stringResource(id = R.string.preconsent_screen_title),
        onBackButtonClick = { onNavigate(WalletDestination.PopBackStack.route) },
        scrollable = false
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
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
            ) {
                Row {
                    Text(
                        text = stringResource(id = R.string.preconsent_screen_info),
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
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
                            preconsents = preconsents.filter { it.id != id }
                            currentPreconsent = null
                        }
                    }
                )
            }
            }
        }
    }
}







package com.android.identity.appsupport.ui.preconsent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import identitycredential.identity_appsupport.generated.resources.Res
import identitycredential.identity_appsupport.generated.resources.preconsent_list_details_icon_description
import identitycredential.identity_appsupport.generated.resources.preconsent_list_num_shared_fields_info
import org.jetbrains.compose.resources.stringResource


/**
 * List of pre-consents.
 */
@Composable
fun PreconsentList(
    preconsents: List<Preconsent>,
    onPreconsentClick: (Preconsent) -> Unit,
) {
    LazyColumn {
        itemsIndexed(preconsents) { index, item ->
            PreconsentListItem(
                cardName = item.document.description,
                verifierName = item.relyingParty.trustPoint?.displayName
                    ?: throw IllegalStateException("Display name is null"),
                numberOfFields = item.consentFields.size,
                onClick = { onPreconsentClick(item) }
            )
            if (index < preconsents.size - 1) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                )
            }
        }
    }
}


/**
 * List item of a pre-consent in the pre-consent list.
 */
@Composable
private fun PreconsentListItem(
    cardName: String,
    verifierName: String,
    numberOfFields: Int,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp, 16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = cardName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = verifierName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                }
                Text(
                    text = stringResource(Res.string.preconsent_list_num_shared_fields_info, numberOfFields),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Indicator that item is expandable
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = stringResource(Res.string.preconsent_list_details_icon_description),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
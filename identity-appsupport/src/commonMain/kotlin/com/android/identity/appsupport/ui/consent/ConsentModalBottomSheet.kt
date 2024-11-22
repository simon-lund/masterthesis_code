package com.android.identity.appsupport.ui.consent

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.android.identity.appsupport.ui.getOutlinedImageVector
import identitycredential.identity_appsupport.generated.resources.*
import identitycredential.identity_appsupport.generated.resources.Res
import identitycredential.identity_appsupport.generated.resources.consent_modal_bottom_sheet_button_cancel
import identitycredential.identity_appsupport.generated.resources.consent_modal_bottom_sheet_button_more
import identitycredential.identity_appsupport.generated.resources.consent_modal_bottom_sheet_button_share
import identitycredential.identity_appsupport.generated.resources.consent_modal_bottom_sheet_card_art_description
import identitycredential.identity_appsupport.generated.resources.consent_modal_bottom_sheet_data_element_icon_description
import identitycredential.identity_appsupport.generated.resources.consent_modal_bottom_sheet_headline_share_with_known_requester
import identitycredential.identity_appsupport.generated.resources.consent_modal_bottom_sheet_headline_share_with_unknown_requester
import identitycredential.identity_appsupport.generated.resources.consent_modal_bottom_sheet_share_and_stored_by_known_requester
import identitycredential.identity_appsupport.generated.resources.consent_modal_bottom_sheet_share_and_stored_by_unknown_requester
import identitycredential.identity_appsupport.generated.resources.consent_modal_bottom_sheet_share_with_known_requester
import identitycredential.identity_appsupport.generated.resources.consent_modal_bottom_sheet_share_with_unknown_requester
import identitycredential.identity_appsupport.generated.resources.consent_modal_bottom_sheet_verifier_icon_description
import identitycredential.identity_appsupport.generated.resources.consent_modal_bottom_sheet_wallet_privacy_policy
import identitycredential.identity_appsupport.generated.resources.consent_modal_bottom_sheet_warning_icon_description
import identitycredential.identity_appsupport.generated.resources.consent_modal_bottom_sheet_warning_verifier_not_in_trust_list
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.stringResource
import kotlin.math.min

/**
 * A [ModalBottomSheet] used for obtaining the user's consent when presenting credentials.
 *
 * @param sheetState a [SheetState] for state.
 * @param consentFields the list of consent fields to show.
 * @param document details about the document being presented.
 * @param relyingParty a structure for conveying who is asking for the information.
 * @param preconsentEnabled whether pre-consent mechanism is enabled.
 * @param isPreconsentAllowed whether a user can set up pre-consent.
 * @param addedFields if not empty, a pre-consent exists but the relying party additionally requires the listed fields.
 * These fields are highlighted in the list of fields in the consent form. (Invariant: if not empty, [isPreconsentAllowed] must be true)
 * @param onConfirm called when the sheet is dismissed.
 * @param onCancel called when the user presses the "Share" button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsentModalBottomSheet(
    sheetState: SheetState,
    consentFields: List<ConsentField>,
    document: ConsentDocument,
    relyingParty: ConsentRelyingParty,
    preconsentEnabled: Boolean,
    isPreconsentAllowed: Boolean,
    addedFields: List<ConsentField>,
    onConfirm: (setupPreConsent: Boolean) -> Unit,
    onCancel: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Checkbox state for setting up pre-consent
    var setupPreConsent by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = { onCancel() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            RelyingPartySection(relyingParty)

            DocumentSection(document)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusGroup()
                    .verticalScroll(scrollState)
                    .weight(0.9f, false)
            ) {
                RequestSection(
                    consentFields = consentFields,
                    relyingParty = relyingParty,
                    addedFields = if (preconsentEnabled) addedFields else emptyList()
                )
            }
            if (preconsentEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                PreconsentArea(
                    state = setupPreConsent,
                    isPreconsentAllowed = isPreconsentAllowed,
                    addedFields = addedFields,
                    onChange = { setupPreConsent = it })
            }
            ButtonSection(
                scope = scope,
                sheetState = sheetState,
                onConfirm = { onConfirm(setupPreConsent) },
                onCancel = onCancel,
                scrollState = scrollState
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun RelyingPartySection(relyingParty: ConsentRelyingParty) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (relyingParty.trustPoint != null) {
            if (relyingParty.trustPoint.displayIcon != null) {
                val rpBitmap = remember {
                    relyingParty.trustPoint.displayIcon!!.decodeToImageBitmap()
                }
                Icon(
                    modifier = Modifier.size(80.dp).padding(bottom = 16.dp),
                    bitmap = rpBitmap,
                    contentDescription = stringResource(Res.string.consent_modal_bottom_sheet_verifier_icon_description),
                    tint = Color.Unspecified
                )
            }
            if (relyingParty.trustPoint.displayName != null) {
                Text(
                    text = relyingParty.trustPoint.displayName!!,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        } else if (relyingParty.websiteOrigin != null) {
            Text(
                text = stringResource(
                    Res.string.consent_modal_bottom_sheet_headline_share_with_known_requester,
                    relyingParty.websiteOrigin
                ),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        } else {
            Text(
                text = stringResource(Res.string.consent_modal_bottom_sheet_headline_share_with_unknown_requester),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PreconsentArea(
    state: Boolean,
    isPreconsentAllowed: Boolean = false,
    addedFields: List<ConsentField> = emptyList(),
    onChange: (Boolean) -> Unit = {}
) {
    @Composable
    fun TextWithSubtext(text: String, subtext: String) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }

    var modifier = Modifier
        .fillMaxWidth()
        .clip(shape = RoundedCornerShape(16.dp))
        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
    if (isPreconsentAllowed) {
        modifier = modifier.clickable(
            onClick = { onChange(!state) },
            indication = ripple(),
            interactionSource = remember { MutableInteractionSource() },
        )
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when {
                isPreconsentAllowed && addedFields.isEmpty() -> {
                    Checkbox(checked = state, onCheckedChange = onChange)
                    TextWithSubtext(
                        text = stringResource(Res.string.consent_modal_bottom_sheet_preconsent_new),
                        subtext = stringResource(Res.string.consent_modal_bottom_sheet_preconsent_new_description)
                    )
                }

                isPreconsentAllowed && addedFields.isNotEmpty() -> {
                    Checkbox(checked = state, onCheckedChange = onChange)
                    TextWithSubtext(
                        text = stringResource(Res.string.consent_modal_bottom_sheet_preconsent_update),
                        subtext = stringResource(Res.string.consent_modal_bottom_sheet_preconsent_update_description)
                    )
                }

                else -> {
                    TextWithSubtext(
                        text = stringResource(Res.string.consent_modal_bottom_sheet_preconsent_not_allowed),
                        subtext = stringResource(Res.string.consent_modal_bottom_sheet_preconsent_not_allowed_description)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ButtonSection(
    scope: CoroutineScope,
    sheetState: SheetState,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    scrollState: ScrollState,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = {
                scope.launch {
                    sheetState.hide()
                    onCancel()
                }
            }) {
                Text(text = stringResource(Res.string.consent_modal_bottom_sheet_button_cancel))
            }

            Button(
                onClick = {
                    if (!scrollState.canScrollForward) {
                        onConfirm()
                    } else {
                        scope.launch {
                            val step = (scrollState.viewportSize * 0.9).toInt()
                            scrollState.animateScrollTo(
                                min(
                                    scrollState.value + step,
                                    scrollState.maxValue
                                )
                            )
                        }
                    }
                }
            ) {
                if (scrollState.canScrollForward) {
                    Text(text = stringResource(Res.string.consent_modal_bottom_sheet_button_more))
                } else {
                    Text(text = stringResource(Res.string.consent_modal_bottom_sheet_button_share))
                }
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun DocumentSection(document: ConsentDocument) {
    Column(
        modifier = Modifier
            .padding(vertical = 2.dp)
            .fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLowest),
            horizontalAlignment = Alignment.Start,
        ) {
            Row(
                modifier = Modifier.padding(16.dp)
            ) {
                val cartArtBitmap = remember {
                    document.cardArt.decodeToImageBitmap()
                }
                Icon(
                    modifier = Modifier.size(50.dp),
                    bitmap = cartArtBitmap,
                    contentDescription = stringResource(Res.string.consent_modal_bottom_sheet_card_art_description),
                    tint = Color.Unspecified
                )
                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = document.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = document.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun RequestSection(
    consentFields: List<ConsentField>,
    relyingParty: ConsentRelyingParty,
    addedFields: List<ConsentField> = emptyList()
) {
    val useColumns = consentFields.size > 5
    val (storedFields, notStoredFields) = consentFields.partition {
        it is MdocConsentField && it.intentToRetain
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (notStoredFields.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = if (relyingParty.trustPoint?.displayName != null) {
                            stringResource(
                                Res.string.consent_modal_bottom_sheet_share_with_known_requester,
                                relyingParty.trustPoint.displayName!!
                            )
                        } else if (relyingParty.websiteOrigin != null) {
                            stringResource(
                                Res.string.consent_modal_bottom_sheet_share_with_known_requester,
                                relyingParty.websiteOrigin
                            )
                        } else {
                            stringResource(Res.string.consent_modal_bottom_sheet_share_with_unknown_requester)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                DataElementGridView(notStoredFields, useColumns, addedFields)
            }
            if (storedFields.size > 0) {
                if (notStoredFields.size > 0) {
                    HorizontalDivider(modifier = Modifier.padding(8.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = if (relyingParty.trustPoint?.displayName != null) {
                            stringResource(
                                Res.string.consent_modal_bottom_sheet_share_and_stored_by_known_requester,
                                relyingParty.trustPoint.displayName!!
                            )
                        } else if (relyingParty.websiteOrigin != null) {
                            stringResource(
                                Res.string.consent_modal_bottom_sheet_share_and_stored_by_known_requester,
                                relyingParty.websiteOrigin
                            )
                        } else {
                            stringResource(Res.string.consent_modal_bottom_sheet_share_and_stored_by_unknown_requester)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                DataElementGridView(storedFields, useColumns, addedFields)
            }
        }
    }
    Spacer(modifier = Modifier.height(2.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // TODO: When we upgrade to a newer version of compose-ui we can use
            //  AnnotatedString.fromHtml() and clicking the links will also work.
            //  See https://issuetracker.google.com/issues/139326648 for details.
            val annotatedLinkString = buildAnnotatedString {
                val str = stringResource(Res.string.consent_modal_bottom_sheet_wallet_privacy_policy)
                val startIndex = 4
                val endIndex = startIndex + 31
                append(str)
                addStyle(
                    style = SpanStyle(
                        color = Color(0xff64B5F6),
                        textDecoration = TextDecoration.Underline
                    ), start = startIndex, end = endIndex
                )
            }
            Text(
                text = annotatedLinkString,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
    if (relyingParty.trustPoint == null) {
        Box(
            modifier = Modifier.padding(0.dp, 8.dp, 0.dp, 0.dp)
        ) {
            WarningCard(
                stringResource(Res.string.consent_modal_bottom_sheet_warning_verifier_not_in_trust_list)
            )
        }
    }
}

@Composable
private fun DataElementGridView(
    consentFields: List<ConsentField>,
    useColumns: Boolean,
    addedFields: List<ConsentField> = emptyList()
) {
    if (!useColumns) {
        for (consentField in consentFields) {
            Row(modifier = Modifier.fillMaxWidth()) {
                DataElementView(
                    consentField = consentField,
                    modifier = Modifier.weight(1.0f),
                    isAddedField = addedFields.contains(consentField)
                )
            }
        }
    } else {
        var n = 0
        while (n <= consentFields.size - 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DataElementView(
                    consentField = consentFields[n],
                    modifier = Modifier.weight(1.0f),
                    isAddedField = addedFields.contains(consentFields[n])
                )
                DataElementView(
                    consentField = consentFields[n + 1],
                    modifier = Modifier.weight(1.0f),
                    isAddedField = addedFields.contains(consentFields[n + 1])
                )
            }
            n += 2
        }
        if (n < consentFields.size) {
            Row(modifier = Modifier.fillMaxWidth()) {
                DataElementView(
                    consentField = consentFields[n],
                    modifier = Modifier.weight(1.0f),
                    isAddedField = addedFields.contains(consentFields[n])
                )
            }
        }
    }
}

/**
 * Individual view for a DataElement.
 */
@Composable
private fun DataElementView(
    modifier: Modifier,
    consentField: ConsentField,
    isAddedField: Boolean = false
) {
    val _modifier = if (isAddedField) {
        modifier.background(Color(0xffFFEE8C))
    } else {
        modifier
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = _modifier.padding(8.dp)
    ) {
        if (consentField.attribute?.icon != null) {
            Icon(
                imageVector = consentField.attribute!!.icon!!.getOutlinedImageVector(),
                contentDescription = stringResource(Res.string.consent_modal_bottom_sheet_data_element_icon_description),
                tint = if (isAddedField) Color.Black else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = consentField.displayName,
            fontWeight = FontWeight.Normal,
            style = MaterialTheme.typography.bodySmall,
            color = if (isAddedField) {
                Color.Black
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}


@Composable
private fun WarningCard(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.errorContainer),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
        ) {
            Icon(
                modifier = Modifier.padding(end = 16.dp),
                imageVector = Icons.Outlined.Warning,
                contentDescription = stringResource(Res.string.consent_modal_bottom_sheet_warning_icon_description),
                tint = MaterialTheme.colorScheme.onErrorContainer
            )

            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

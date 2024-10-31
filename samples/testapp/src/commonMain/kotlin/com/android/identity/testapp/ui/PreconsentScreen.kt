package com.android.identity.testapp.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.android.identity.appsupport.ui.consent.ConsentDocument
import com.android.identity.appsupport.ui.consent.ConsentRelyingParty
import com.android.identity.appsupport.ui.consent.MdocConsentField
import com.android.identity.appsupport.ui.preconsent.PreconsentBottomSheet
import com.android.identity.appsupport.ui.preconsent.Preconsent
import com.android.identity.appsupport.ui.preconsent.PreconsentList
import com.android.identity.cbor.Cbor
import com.android.identity.cbor.CborMap
import com.android.identity.crypto.Algorithm
import com.android.identity.crypto.X509Cert
import com.android.identity.documenttype.DocumentTypeRepository
import com.android.identity.documenttype.knowntypes.DrivingLicense
import com.android.identity.mdoc.request.DeviceRequestGenerator
import com.android.identity.mdoc.request.DeviceRequestParser
import com.android.identity.trustmanagement.TrustPoint
import com.android.identity.util.UUID
import identitycredential.samples.testapp.generated.resources.Res
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi

private const val IACA_CERT_PEM =
    """
-----BEGIN CERTIFICATE-----
MIICujCCAj+gAwIBAgIQWlUtc8+HqDS3PvCqXIlyYDAKBggqhkjOPQQDAzA5MSowKAYDVQQDDCFP
V0YgSWRlbnRpdHkgQ3JlZGVudGlhbCBURVNUIElBQ0ExCzAJBgNVBAYTAlpaMB4XDTI0MDkxNzE2
NTEzN1oXDTI5MDkxNzE2NTEzN1owOTEqMCgGA1UEAwwhT1dGIElkZW50aXR5IENyZWRlbnRpYWwg
VEVTVCBJQUNBMQswCQYDVQQGEwJaWjB2MBAGByqGSM49AgEGBSuBBAAiA2IABJUHWyr1+ZlNvYEv
sR/1y2uYUkUczBqXTeTwiyRyiEGFFnZ0UR+gNKC4grdCP4F/dA+TWTduy2NlRmog5IByPSdwlvfW
B2f+Tf+MdbgZM+1+ukeaCgDhT/ZwgCoTNgvjyKOCAQowggEGMB0GA1UdDgQWBBQzCQV8RylodOk8
Yq6AwLDQhC7fUDAfBgNVHSMEGDAWgBQzCQV8RylodOk8Yq6AwLDQhC7fUDAOBgNVHQ8BAf8EBAMC
AQYwTAYDVR0SBEUwQ4ZBaHR0cHM6Ly9naXRodWIuY29tL29wZW53YWxsZXQtZm91bmRhdGlvbi1s
YWJzL2lkZW50aXR5LWNyZWRlbnRpYWwwEgYDVR0TAQH/BAgwBgEB/wIBADBSBgNVHR8ESzBJMEeg
RaBDhkFodHRwczovL2dpdGh1Yi5jb20vb3BlbndhbGxldC1mb3VuZGF0aW9uLWxhYnMvaWRlbnRp
dHktY3JlZGVudGlhbDAKBggqhkjOPQQDAwNpADBmAjEAil9jZ+deFSg1/ESWDEuA3gSU43XCO2t4
MirhUlQqSRYlOVBlD0sel7tyuiSPxEldAjEA1eTa/5yCZ67jjg6f2gbbJ8ZzMbff+DlHy77+wXIS
b35NiZ8FdVHgC2ut4fDQTRN4
-----END CERTIFICATE-----        
    """

@OptIn(ExperimentalResourceApi::class)
suspend fun loadPreconsents(): List<Preconsent> {
    val consentDocument = ConsentDocument(
        name = "Erika's Driving License",
        description = "Driving License",
        cardArt = Res.readBytes("files/utopia_driving_license_card_art.png")
    )

    val relyingParty = ConsentRelyingParty(
        trustPoint = TrustPoint(
            certificate = X509Cert.fromPem(IACA_CERT_PEM),
            displayName = "Utopia Brewery",
            displayIcon = Res.readBytes("files/utopia-brewery.png")
        ),
        websiteOrigin = null,
    )

    var preconsents = mutableListOf<Preconsent>()
    for (request in DrivingLicense.getDocumentType().sampleRequests) {
        val namespacesToRequest = mutableMapOf<String, Map<String, Boolean>>()
        for (ns in request.mdocRequest!!.namespacesToRequest) {
            val dataElementsToRequest = mutableMapOf<String, Boolean>()
            for ((de, intentToRetain) in ns.dataElementsToRequest) {
                dataElementsToRequest[de.attribute.identifier] = intentToRetain
            }
            namespacesToRequest[ns.namespace] = dataElementsToRequest
        }
        val encodedSessionTranscript = Cbor.encode(CborMap.builder().put("doesn't", "matter").end().build())
        val encodedDeviceRequest = DeviceRequestGenerator(encodedSessionTranscript)
            .addDocumentRequest(
                request.mdocRequest!!.docType,
                namespacesToRequest,
                null,
                null,
                Algorithm.UNSET,
                null
            ).generate()
        val deviceRequest = DeviceRequestParser(encodedDeviceRequest, encodedSessionTranscript)
            .parse()

        val docTypeRepo = DocumentTypeRepository()
        docTypeRepo.addDocumentType(DrivingLicense.getDocumentType())
        val consentFields = MdocConsentField.generateConsentFields(
            deviceRequest.docRequests[0],
            docTypeRepo,
            null
        )

        // Push to list of preconsents
        preconsents.add(
            Preconsent(
                id = UUID.randomUUID().toString(),
                document = consentDocument,
                relyingParty = relyingParty,
                consentFields = consentFields
            )
        )
    }

    return preconsents
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreconsentScreen() {
    var preconsents by remember { mutableStateOf(emptyList<Preconsent>()) }
    var currentPreconsent by remember { mutableStateOf<Preconsent?>(null) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    LaunchedEffect(Unit) {
        preconsents = loadPreconsents()
    }

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
                    preconsents = preconsents.filter { it.id != id }
                    currentPreconsent = null
                }
            }
        )
    }
}
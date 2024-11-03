package com.android.identity_credential.wallet.presentation

import androidx.fragment.app.FragmentActivity
import com.android.identity.android.securearea.AndroidKeystoreKeyUnlockData
import com.android.identity.android.securearea.AndroidKeystoreSecureArea
import com.android.identity.android.securearea.UserAuthenticationType
import com.android.identity.android.securearea.cloud.CloudKeyLockedException
import com.android.identity.android.securearea.cloud.CloudKeyUnlockData
import com.android.identity.android.securearea.cloud.CloudSecureArea
import com.android.identity.appsupport.ui.consent.ConsentDocument
import com.android.identity.cbor.Cbor
import com.android.identity.credential.Credential
import com.android.identity.credential.SecureAreaBoundCredential
import com.android.identity.crypto.Algorithm
import com.android.identity.document.Document
import com.android.identity.document.NameSpacedData
import com.android.identity.issuance.DocumentExtensions.documentConfiguration
import com.android.identity.mdoc.credential.MdocCredential
import com.android.identity.mdoc.mso.MobileSecurityObjectParser
import com.android.identity.mdoc.mso.StaticAuthDataParser
import com.android.identity.mdoc.response.DocumentGenerator
import com.android.identity.mdoc.util.MdocUtil
import com.android.identity.sdjwt.SdJwtVerifiableCredential
import com.android.identity.securearea.KeyLockedException
import com.android.identity.securearea.KeyUnlockData
import com.android.identity.securearea.software.SoftwareKeyInfo
import com.android.identity.securearea.software.SoftwareKeyUnlockData
import com.android.identity.securearea.software.SoftwareSecureArea
import com.android.identity.trustmanagement.TrustPoint
import com.android.identity.util.Logger
import com.android.identity_credential.wallet.R
import com.android.identity_credential.wallet.ui.prompt.biometric.showBiometricPrompt
import com.android.identity.appsupport.ui.consent.ConsentField
import com.android.identity.appsupport.ui.consent.ConsentRelyingParty
import com.android.identity.appsupport.ui.consent.MdocConsentField
import com.android.identity.appsupport.ui.consent.VcConsentField
import com.android.identity.appsupport.ui.preconsent.PreconsentStore
import com.android.identity_credential.wallet.ui.prompt.consent.showConsentPrompt
import com.android.identity_credential.wallet.ui.prompt.passphrase.showPassphrasePrompt

const val TAG = "PresentmentFlow"
const val MAX_PASSPHRASE_ATTEMPTS = 3

private suspend fun showPresentmentFlowImpl(
    activity: FragmentActivity,
    consentFields: List<ConsentField>,
    document: ConsentDocument,
    relyingParty: ConsentRelyingParty,
    credential: SecureAreaBoundCredential,
    signAndGenerate: (KeyUnlockData?) -> ByteArray
): ByteArray {
    //  if so -> check if preconsent is valid
    //  if not setup -> show consent prompt and check if user wants to setup preconsent
    //  if so setup pre-consent

    val preconsentStore = PreconsentStore.getInstance()

    // *** Preconsent Logic ***
    // 1. Check if relying party is trusted
    // 2. Check if a preconsent is set up for the relying party
    // 3. Check if the preconsent is valid

    // 1. Check if relying party is trusted (if no, show consent prompt but no option to set up preconsent, else continue)
    // 2. Check if preconsent is set up for the relying party (if no, show consent prompt with option to set up preconsent, else continue)
    // 3. Check if preconsent is valid (if no, show consent prompt but with information that preconsent is set up however outdated, else skip consent prompt)
    //   (3. must be handled in the showConsentPrompt function)
    val isTrusted = relyingParty.trustPoint != null
    if(isTrusted) {
        val existingPreconsent = preconsentStore.preconsents.find {
            // Compare the relevant document fields
            val documentNameEquals = document.name == it.document.name
            val documentDescriptionEquals = document.description == it.document.description

            // Compare the TrustPoint's certificate of the relying party
            val encodedCertificate = relyingParty.trustPoint?.certificate?.encodedCertificate!!
            val certificateEquals =
                encodedCertificate.contentEquals(it.relyingParty.trustPoint?.certificate?.encodedCertificate)

            documentNameEquals && documentDescriptionEquals && certificateEquals
        }

        if (existingPreconsent != null) {
            // We have to check the validity of the preconsent
            // If consent fields were removed, we update the preconsent right away and skip consent
            // (removing things is good, and no consent is needed)


            // If consent fields were added, we show the consent prompt and highlight the added fields in the list
            // (adding things is bad, and consent is needed)

        }
    }

    // TODO: enable preconsent if trusted
    // TODO: show preconsent setup if not set up
    // TODO: skip if preconsent and nothing changed
    // always show the Consent Prompt first
    showConsentPrompt(
        activity = activity,
        document = document,
        relyingParty = relyingParty,
        consentFields = consentFields,
    ).let { resultSuccess ->
        // throw exception if user canceled the Prompt
        if (!resultSuccess){
            throw UserCanceledPromptException()
        }
    }

    // initially null and updated when catching a KeyLockedException in the while-loop below
    var keyUnlockData: KeyUnlockData? = null
    var remainingPassphraseAttempts = MAX_PASSPHRASE_ATTEMPTS

    while (true) {
        try {
            return signAndGenerate(keyUnlockData)
        }
        // if KeyLockedException is raised show the corresponding Prompt to unlock
        // the auth key for a Credential's Secure Area
        catch (e: KeyLockedException) {
            when (credential.secureArea) {
                // show Biometric prompt
                is AndroidKeystoreSecureArea -> {
                    val unlockData =
                        AndroidKeystoreKeyUnlockData(credential.alias)
                    val cryptoObject =
                        unlockData.getCryptoObjectForSigning(Algorithm.ES256)

                    // update KeyUnlockData to be used on the next loop iteration
                    keyUnlockData = unlockData

                    val successfulBiometricResult = showBiometricPrompt(
                        activity = activity,
                        title = activity.resources.getString(R.string.presentation_biometric_prompt_title),
                        subtitle = activity.resources.getString(R.string.presentation_biometric_prompt_subtitle),
                        cryptoObject = cryptoObject,
                        userAuthenticationTypes = setOf(
                            UserAuthenticationType.BIOMETRIC,
                            UserAuthenticationType.LSKF
                        ),
                        requireConfirmation = false
                    )
                    // if user cancelled or was unable to authenticate, throw IllegalStateException
                    check(successfulBiometricResult) { "Biometric Unsuccessful" }
                }

                // show Passphrase prompt
                is SoftwareSecureArea -> {
                    // enforce a maximum number of attempts
                    if (remainingPassphraseAttempts == 0) {
                        throw IllegalStateException("Error! Reached maximum number of Passphrase attempts.")
                    }
                    remainingPassphraseAttempts--

                    val softwareKeyInfo =
                        credential.secureArea.getKeyInfo(credential.alias) as SoftwareKeyInfo
                    val constraints = softwareKeyInfo.passphraseConstraints!!
                    val title =
                        if (constraints.requireNumerical)
                            activity.resources.getString(R.string.passphrase_prompt_pin_title)
                        else
                            activity.resources.getString(R.string.passphrase_prompt_passphrase_title)
                    val content =
                        if (constraints.requireNumerical) {
                            activity.resources.getString(R.string.passphrase_prompt_pin_content)
                        } else {
                            activity.resources.getString(
                                R.string.passphrase_prompt_passphrase_content
                            )
                        }
                    val passphrase = showPassphrasePrompt(
                        activity = activity,
                        constraints = constraints,
                        title = title,
                        content = content,
                    )

                    // if passphrase is null then user canceled the prompt
                    if (passphrase == null) {
                        throw UserCanceledPromptException()
                    }

                    keyUnlockData = SoftwareKeyUnlockData(passphrase)
                }

                // Shows Wallet PIN/Passphrase prompt or Biometrics/LSKF, depending
                is CloudSecureArea -> {
                    if (keyUnlockData == null) {
                        keyUnlockData = CloudKeyUnlockData(
                            credential.secureArea as CloudSecureArea,
                            credential.alias,
                        )
                    }

                    when ((e as CloudKeyLockedException).reason) {
                        CloudKeyLockedException.Reason.WRONG_PASSPHRASE -> {
                            // enforce a maximum number of attempts
                            if (remainingPassphraseAttempts == 0) {
                                throw IllegalStateException("Error! Reached maximum number of Passphrase attempts.")
                            }
                            remainingPassphraseAttempts--

                            val constraints = (credential.secureArea as CloudSecureArea).passphraseConstraints
                            val title =
                                if (constraints.requireNumerical)
                                    activity.resources.getString(R.string.passphrase_prompt_csa_pin_title)
                                else
                                    activity.resources.getString(R.string.passphrase_prompt_csa_passphrase_title)
                            val content =
                                if (constraints.requireNumerical) {
                                    activity.resources.getString(R.string.passphrase_prompt_csa_pin_content)
                                } else {
                                    activity.resources.getString(
                                        R.string.passphrase_prompt_csa_passphrase_content
                                    )
                                }
                            val passphrase = showPassphrasePrompt(
                                activity = activity,
                                constraints = constraints,
                                title = title,
                                content = content,
                            )

                            // if passphrase is null then user canceled the prompt
                            if (passphrase == null) {
                                throw UserCanceledPromptException()
                            }
                            (keyUnlockData as CloudKeyUnlockData).passphrase = passphrase
                        }

                        CloudKeyLockedException.Reason.USER_NOT_AUTHENTICATED -> {
                            val successfulBiometricResult = showBiometricPrompt(
                                activity = activity,
                                title = activity.resources.getString(R.string.presentation_biometric_prompt_title),
                                subtitle = activity.resources.getString(R.string.presentation_biometric_prompt_subtitle),
                                cryptoObject = (keyUnlockData as CloudKeyUnlockData).cryptoObject,
                                userAuthenticationTypes = setOf(
                                    UserAuthenticationType.BIOMETRIC,
                                    UserAuthenticationType.LSKF
                                ),
                                requireConfirmation = false
                            )
                            // if user cancelled or was unable to authenticate, throw IllegalStateException
                            check(successfulBiometricResult) { "Biometric Unsuccessful" }
                        }
                    }
                }

                // for secure areas not yet implemented
                else -> {
                    throw IllegalStateException("No prompts implemented for Secure Area ${credential.secureArea.displayName}")
                }
            }
        }
    }
}

suspend fun showMdocPresentmentFlow(
    activity: FragmentActivity,
    consentFields: List<ConsentField>,
    document: ConsentDocument,
    relyingParty: ConsentRelyingParty,
    credential: MdocCredential,
    encodedSessionTranscript: ByteArray,
): ByteArray {
    return showPresentmentFlowImpl(
        activity,
        consentFields,
        document,
        relyingParty,
        credential
    ) { keyUnlockData: KeyUnlockData? ->
        mdocSignAndGenerate(consentFields, credential, encodedSessionTranscript!!, keyUnlockData)
    }
}

suspend fun showSdJwtPresentmentFlow(
    activity: FragmentActivity,
    consentFields: List<ConsentField>,
    document: ConsentDocument,
    relyingParty: ConsentRelyingParty,
    credential: SecureAreaBoundCredential,
    nonce: String,
    clientId: String,
): ByteArray {
    return showPresentmentFlowImpl(
        activity,
        consentFields,
        document,
        relyingParty,
        credential
    ) { keyUnlockData: KeyUnlockData? ->
        val sdJwt = SdJwtVerifiableCredential.fromString(
            String(credential.issuerProvidedData, Charsets.US_ASCII))

        val requestedAttributes = consentFields.map { (it as VcConsentField).claimName }.toSet()
        Logger.i(
            TAG, "Filtering requested attributes (${requestedAttributes.joinToString()}) " +
                    "from disclosed attributes (${sdJwt.disclosures.joinToString { it.key }})")
        val filteredSdJwt = sdJwt.discloseOnly(requestedAttributes)
        Logger.i(TAG, "Remaining disclosures: ${filteredSdJwt.disclosures.joinToString { it.key }}")
        if (filteredSdJwt.disclosures.isEmpty()) {
            // This is going to cause problems with the encoding and decoding. We should
            // cancel the submission, since we can't fulfill any of the requested
            // information.
            // TODO: Handle this cancellation better.
            Logger.e(TAG, "No disclosures remaining.")
        }

        filteredSdJwt.createPresentation(
            credential.secureArea,
            credential.alias,
            keyUnlockData,
            Algorithm.ES256,
            nonce!!,
            clientId!!
        ).toString().toByteArray(Charsets.US_ASCII)
    }
}

private fun mdocSignAndGenerate(
    consentFields: List<ConsentField>,
    credential: SecureAreaBoundCredential,
    encodedSessionTranscript: ByteArray,
    keyUnlockData: KeyUnlockData?
): ByteArray {
    // create the document generator for the suitable Document (of DocumentRequest)
    val documentGenerator =
        createDocumentGenerator(
            consentFields = consentFields,
            document = credential.document,
            credential = credential,
            sessionTranscript = encodedSessionTranscript
        )
    // try signing the data of the document (or KeyLockedException is thrown)
    documentGenerator.setDeviceNamespacesSignature(
        NameSpacedData.Builder().build(),
        credential.secureArea,
        credential.alias,
        keyUnlockData,
        Algorithm.ES256
    )
    // increment the credential's usage count since it just finished signing the data successfully
    credential.increaseUsageCount()
    // finally add the document to the response generator and generate the bytes
    return documentGenerator.generate()
}

private fun createDocumentGenerator(
    consentFields: List<ConsentField>,
    document: Document,
    credential: Credential,
    sessionTranscript: ByteArray,
): DocumentGenerator {
    val staticAuthData = StaticAuthDataParser(credential.issuerProvidedData).parse()
    val mergedIssuerNamespaces = MdocUtil.mergeIssuerNamesSpaces(
        getNamespacesAndDataElements(consentFields),
        document.documentConfiguration.mdocConfiguration!!.staticData,
        staticAuthData
    )

    val issuerAuthCoseSign1 = Cbor.decode(staticAuthData.issuerAuth).asCoseSign1
    val encodedMsoBytes = Cbor.decode(issuerAuthCoseSign1.payload!!)
    val encodedMso = Cbor.encode(encodedMsoBytes.asTaggedEncodedCbor)
    val mso = MobileSecurityObjectParser(encodedMso).parse()

    val documentGenerator = DocumentGenerator(
        mso.docType,
        staticAuthData.issuerAuth,
        sessionTranscript
    )
    documentGenerator.setIssuerNamespaces(mergedIssuerNamespaces)
    return documentGenerator
}

private fun getNamespacesAndDataElements(
    consentFields: List<ConsentField>
): Map<String, List<String>> {
    val ret = mutableMapOf<String, MutableList<String>>()
    for (field in consentFields) {
        field as MdocConsentField
        val listOfDataElements = ret.getOrPut(field.namespaceName) { mutableListOf() }
        listOfDataElements.add(field.dataElementName)
    }
    return ret
}

class UserCanceledPromptException : Exception()
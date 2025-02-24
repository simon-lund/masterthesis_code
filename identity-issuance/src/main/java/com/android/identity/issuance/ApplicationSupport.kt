package com.android.identity.issuance

import com.android.identity.flow.annotation.FlowInterface
import com.android.identity.flow.annotation.FlowMethod
import com.android.identity.flow.client.FlowNotifiable
import com.android.identity.securearea.KeyAttestation

/**
 * Server-side support functionality for the wallet mobile app. This is needed even if the
 * full-blown wallet server is not used.
 */
@FlowInterface
interface ApplicationSupport : FlowNotifiable<LandingUrlNotification> {
    /**
     * Creates a "landing" absolute URL suitable for web redirects. When a landing URL is
     * navigated to, [LandingUrlNotification] is sent to the client.
     */
    @FlowMethod
    suspend fun createLandingUrl(): String

    /**
     * Returns the query portion of the URL which was actually used when navigating to a landing
     * URL, or null if navigation did not occur yet.
     *
     * [landingUrl] URL of the landing page as returned by [createLandingUrl].
     */
    @FlowMethod
    suspend fun getLandingUrlStatus(landingUrl: String): String?

    /**
     * Creates OAuth JWT client assertion based on the mobile-platform-specific [KeyAttestation].
     */
    @FlowMethod
    suspend fun createJwtClientAssertion(
        clientAttestation: KeyAttestation,
        targetIssuanceUrl: String
    ): String

    /**
     * Creates OAuth JWT key attestation based on the given list of mobile-platform-specific
     * [KeyAttestation]s.
     */
    @FlowMethod
    suspend fun createJwtKeyAttestation(
        keyAttestations: List<KeyAttestation>,
        nonce: String
    ): String
}
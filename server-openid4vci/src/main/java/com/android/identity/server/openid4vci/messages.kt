package com.android.identity.server.openid4vci

import com.android.identity.cbor.DataItem
import com.android.identity.cbor.annotation.CborSerializable
import com.android.identity.crypto.EcPrivateKey
import com.android.identity.crypto.EcPublicKey
import com.android.identity.document.NameSpacedData
import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//------------ JSON-formatted replies from various OpenID4VCI servlets

@Serializable
data class ParResponse(
    @SerialName("request_uri") val requestUri: String,
    @SerialName("expires_in") val expiresIn: Int
)

@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("c_nonce") val cNonce: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: Int,
    @SerialName("c_nonce_expires_in") val cNonceExpiresIn: Int,
    @SerialName("token_type") val tokenType: String
)

//--------------- authorization session data stored in the database ------------

@CborSerializable
data class IssuanceState(
    val clientId: String,
    val dpopKey: EcPublicKey,
    var redirectUri: String?,
    var codeChallenge: ByteString?,
    var dpopNonce: ByteString? = null,
    var cNonce: ByteString? = null,
    var pidReadingKey: EcPrivateKey? = null,
    var credentialData: NameSpacedData? = null
) {
    companion object
}

/**
 * Types of opaque session ids for client-server communication.
 */
enum class OpaqueIdType {
    PAR_CODE,
    AUTHORIZATION_STATE,
    ISSUER_STATE,
    REDIRECT,
    ACCESS_TOKEN,
    REFRESH_TOKEN,
    PID_READING
}

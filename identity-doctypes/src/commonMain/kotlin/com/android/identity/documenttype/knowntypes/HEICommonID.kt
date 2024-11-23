package com.android.identity.documenttype.knowntypes

import com.android.identity.cbor.*
import com.android.identity.documenttype.DocumentAttributeType
import com.android.identity.documenttype.DocumentType
import com.android.identity.documenttype.Icon

/**
 * Object containing metadata for the HEI Common ID Document Type.
 * Adapted from DrvingLicense.kt and EUPersonalID.kt
 */
object HEICommonID {
    const val DOCTYPE = "edu.hei.commonid.1"
    const val EDUPERSON_NAMESPACE = "edu.hei.commonid.1.eduperson"
    const val SCHAC_NAMESPACE = "edu.hei.commonid.1.schac"
    const val EXTRA_NAMESPACE = "edu.hei.commonid.1.extra"

    /**
     * Build the HEI Common ID Document Type.
     */
    fun getDocumentType(): DocumentType {
        return DocumentType.Builder("HEI Common ID")
            .addMdocDocumentType(DOCTYPE)
            .addVcDocumentType("HEICommonIDCredential")
            .addAttribute(
                DocumentAttributeType.Picture,
                "portrait",
                "Photo of Holder",
                "A reproduction of the mDL holder’s portrait.",
                mandatory = true,
                EDUPERSON_NAMESPACE,
                Icon.ACCOUNT_BOX,
                null // TODO: include img_erika_portrait.jpg
            )
            .addAttribute(
                DocumentAttributeType.Date,
                "portrait_capture_date",
                "Portrait Image Timestamp",
                "Date when portrait was taken",
                false,
                EDUPERSON_NAMESPACE,
                Icon.TODAY,
                SampleData.portraitCaptureDate.toDataItemFullDate()
            )
            .addAttribute(
                DocumentAttributeType.String,
                "sn",
                "Family Name",
                "Last name, surname, or primary identifier, of the mDL holder.",
                mandatory = true,
                EDUPERSON_NAMESPACE,
                Icon.PERSON,
                SampleData.FAMILY_NAME.toDataItem()
            )
            .addAttribute(
                DocumentAttributeType.String,
                "givenNames",
                "Given Names",
                "First name(s), other name(s), or secondary identifier, of the mDL holder",
                mandatory = true,
                EDUPERSON_NAMESPACE,
                Icon.PERSON,
                SampleData.GIVEN_NAME.toDataItem()
            )
            .addAttribute(
                DocumentAttributeType.String,
                "email",
                "Email",
                "Email address of the ID holder",
                mandatory = false,
                EDUPERSON_NAMESPACE,
                Icon.PERSON,
                SampleData.MAIL.toDataItem()
            )
            .addAttribute(
                DocumentAttributeType.StringOptions(Options.EDUPERSON_AFFILIATION),
                "eduPersonAffiliation",
                "Group Affiliation",
                "Group affiliation of the ID holder",
                mandatory = false,
                EDUPERSON_NAMESPACE,
                Icon.PERSON,
                SampleData.EDUPERSON_AFFILIATION.toDataItem()
            )
            .addAttribute(
                DocumentAttributeType.String,
                "eduPersonEntitlement",
                "Entitlement",
                "Entitlement of the ID holder",
                mandatory = false,
                EDUPERSON_NAMESPACE,
                Icon.PERSON,
                SampleData.EDUPERSON_ENTITLEMENT.toDataItem()
            )
            .addAttribute(
                DocumentAttributeType.String,
                "schacHomeOrganization",
                "Home Organization",
                "Home organization of the ID holder",
                mandatory = false,
                SCHAC_NAMESPACE,
                Icon.PERSON,
                SampleData.SCHAC_HOME_ORGANIZATION.toDataItem()
            )
            .addAttribute(
                DocumentAttributeType.String,
                "schacPersonalUniqueCode",
                "Personal Unique Code",
                "Personal unique code of the ID holder",
                mandatory = false,
                SCHAC_NAMESPACE,
                Icon.PERSON,
                SampleData.SCHAC_PERSONAL_UNIQUE_CODE.toDataItem()
            )
            .addAttribute(
                DocumentAttributeType.Date,
                "schacDateOfBirth",
                "Date of Birth",
                "Day, month and year on which the mDL holder was born. If unknown, approximate date of birth.",
                mandatory = true,
                SCHAC_NAMESPACE,
                Icon.TODAY,
                SampleData.birthDate.toDataItemFullDate()
            )
            .addAttribute(
                DocumentAttributeType.Number,
                "schacYearOfBirth",
                "Year of Birth",
                "The year when the mDL holder was born",
                mandatory = false,
                SCHAC_NAMESPACE,
                Icon.TODAY,
                SampleData.AGE_BIRTH_YEAR.toDataItem()
            )
            .addAttribute(
                DocumentAttributeType.String,
                "schacPlaceOfBirth",
                "Place of Birth",
                "Country and municipality or state/province where the mDL holder was born",
                mandatory = false,
                SCHAC_NAMESPACE,
                Icon.PLACE,
                SampleData.BIRTH_PLACE.toDataItem()
            )
            .addAttribute(
                DocumentAttributeType.String,
                "schacMotherTonge",
                "Mother Tongue",
                "The language spoken by the mDL holder as a first language",
                mandatory = false,
                SCHAC_NAMESPACE,
                Icon.LANGUAGE,
                SampleData.SCHAC_MOTHER_TONGUE.toDataItem()
            )
            .addAttribute(
                DocumentAttributeType.IntegerOptions(Options.SCHAC_GENDER),
                "schacGender",
                "Gender",
                "Gender of the mDL holder",
                mandatory = false,
                SCHAC_NAMESPACE,
                Icon.PERSON,
                SampleData.SCHAC_GENDER.toDataItem()
            )
            .addAttribute(
                DocumentAttributeType.Date,
                "schacExpiryDate",
                "Expiry Date",
                "Date when mDL expires",
                mandatory = true,
                SCHAC_NAMESPACE,
                Icon.CALENDAR_CLOCK,
                SampleData.expiryDate.toDataItemFullDate()
            )

            // Additional attributes
            .addAttribute(
                DocumentAttributeType.Date,
                "issue_date",
                "Date of Issue",
                "Date when mDL was issued",
                mandatory = true,
                EXTRA_NAMESPACE,
                Icon.DATE_RANGE,
                SampleData.issueDate.toDataItemFullDate()
            )
            .addAttribute(
                DocumentAttributeType.String,
                "administrative_number",
                "Administrative Number",
                "An audit control number assigned by the issuing authority",
                mandatory = false,
                EXTRA_NAMESPACE,
                Icon.NUMBERS,
                SampleData.ADMINISTRATIVE_NUMBER.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Boolean,
                "age_over_16",
                "Older Than 16 Years",
                "Indication whether the mDL holder is as old or older than 16",
                mandatory = false,
                EXTRA_NAMESPACE,
                Icon.TODAY,
                SampleData.AGE_OVER_16.toDataItem()
            )
            .addAttribute(
                DocumentAttributeType.Boolean,
                "age_over_18",
                "Older Than 18 Years",
                "Indication whether the mDL holder is as old or older than 18",
                mandatory = false,
                EXTRA_NAMESPACE,
                Icon.TODAY,
                SampleData.AGE_OVER_18.toDataItem()
            )
            .addAttribute(
                DocumentAttributeType.Boolean,
                "age_over_21",
                "Older Than 21 Years",
                "Indication whether the mDL holder is as old or older than 21",
                mandatory = false,
                EXTRA_NAMESPACE,
                Icon.TODAY,
                SampleData.AGE_OVER_21.toDataItem()
            )
            .addAttribute(
                DocumentAttributeType.Boolean,
                "age_over_25",
                "Older Than 25 Years",
                "Indication whether the mDL holder is as old or older than 25",
                mandatory = false,
                EXTRA_NAMESPACE,
                Icon.TODAY,
                SampleData.AGE_OVER_25.toDataItem()
            )
            .addAttribute(
                DocumentAttributeType.Boolean,
                "age_over_60",
                "Older Than 60 Years",
                "Indication whether the mDL holder is as old or older than 60",
                mandatory = false,
                EXTRA_NAMESPACE,
                Icon.TODAY,
                SampleData.AGE_OVER_60.toDataItem()
            )
            .addAttribute(
                DocumentAttributeType.Boolean,
                "age_over_62",
                "Older Than 62 Years",
                "Indication whether the mDL holder is as old or older than 62",
                mandatory = false,
                EXTRA_NAMESPACE,
                Icon.TODAY,
                SampleData.AGE_OVER_62.toDataItem()
            )
            .addAttribute(
                DocumentAttributeType.Boolean,
                "age_over_65",
                "Older Than 65 Years",
                "Indication whether the mDL holder is as old or older than 65",
                mandatory = false,
                EXTRA_NAMESPACE,
                Icon.TODAY,
                SampleData.AGE_OVER_65.toDataItem()
            )
            .addAttribute(
                DocumentAttributeType.Boolean,
                "age_over_68",
                "Older Than 68 Years",
                "Indication whether the mDL holder is as old or older than 68",
                mandatory = false,
                EXTRA_NAMESPACE,
                Icon.TODAY,
                SampleData.AGE_OVER_68.toDataItem()
            )

            // Sample requests
            .addSampleRequest(
                id = "age_over_18",
                displayName ="Age Over 18",
                mdocDataElements = mapOf(
                    EXTRA_NAMESPACE to mapOf(
                        "age_over_18" to false,
                    )
                ),
            )
            .addSampleRequest(
                id = "age_over_21",
                displayName ="Age Over 21",
                mdocDataElements = mapOf(
                    EXTRA_NAMESPACE to mapOf(
                        "age_over_21" to false,
                    )
                ),
            )
            .addSampleRequest(
                id = "age_over_18_and_portrait",
                displayName ="Age Over 18 + Portrait",
                mdocDataElements = mapOf(
                    EDUPERSON_NAMESPACE to mapOf(
                        "portrait" to false
                    ),
                    EXTRA_NAMESPACE to mapOf(
                        "age_over_18" to false,
                    )
                ),
            )
            .addSampleRequest(
                id = "age_over_21_and_portrait",
                displayName ="Age Over 21 + Portrait",
                mdocDataElements = mapOf(
                    EDUPERSON_NAMESPACE to mapOf(
                        "portrait" to false
                    ),
                    EXTRA_NAMESPACE to mapOf(
                        "age_over_21" to false,
                    )
                ),
            )
            .addSampleRequest(
                id = "mandatory",
                displayName = "Mandatory Data Elements",
                mdocDataElements = mapOf(
                    EDUPERSON_NAMESPACE to mapOf(
                        "portrait" to false,
                        "sn" to false,
                        "givenNames" to false,
                    ),
                    SCHAC_NAMESPACE to mapOf(
                        "schacDateOfBirth" to false,
                        "schacExpiryDate" to false,
                    ),
                    EXTRA_NAMESPACE to mapOf(
                        "issue_date" to false
                    )
                )
            )
            .addSampleRequest(
                id = "full",
                displayName ="All Data Elements",
                mdocDataElements = mapOf(
                    EDUPERSON_NAMESPACE to mapOf(),
                    SCHAC_NAMESPACE to mapOf(),
                    EXTRA_NAMESPACE to mapOf(),
                )
            )
            .build()
    }
}
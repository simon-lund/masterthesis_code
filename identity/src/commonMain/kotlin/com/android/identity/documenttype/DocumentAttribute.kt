/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.identity.documenttype

import com.android.identity.cbor.CborMap
import com.android.identity.cbor.DataItem

/**
 * Class containing the metadata of an attribute/data element/claim of a Document Type
 *
 * @param type the datatype of this attribute.
 * @param identifier the identifier of this attribute.
 * @param displayName the name suitable for display of the attribute.
 * @param description a description of the attribute.
 * @param icon the icon for the attribute, if available.
 * @param sampleValue a sample value for the attribute, if available.
 * @param preconsentAllowed whether this attribute can be preconsented to.
 */
class DocumentAttribute(
    val type: DocumentAttributeType,
    val identifier: String,
    val displayName: String,
    val description: String,
    val icon: Icon?,
    val sampleValue: DataItem?,
    val preconsentAllowed: Boolean = false
) {
    /**
     * Converts the attribute to a [DataItem].
     */
    fun toDataItem(): DataItem {
        return CborMap.builder().apply {
            put("type", type.toDataItem())
            put("identifier", identifier)
            put("displayName", displayName)
            put("description", description)
            put("preconsentAllowed", preconsentAllowed)
            icon?.let { put("icon", it.name) }
            sampleValue?.let { put("sampleValue", it) }
        }.end().build()
    }

    companion object {
        /**
         * Creates a [DocumentAttribute] from a [DataItem].
         *
         * @param dataItem must have been encoded with [toDataItem].
         */
        fun fromDataItem(dataItem: DataItem): DocumentAttribute {
            return DocumentAttribute(
                type = DocumentAttributeType.fromDataItem(dataItem["type"]),
                identifier = dataItem["identifier"].asTstr,
                displayName = dataItem["displayName"].asTstr,
                description = dataItem["description"].asTstr,
                icon = dataItem.getOrNull("iconName")?.let { Icon.valueOf(it.asTstr) },
                sampleValue = dataItem.getOrNull("sampleValue"),
                preconsentAllowed = dataItem["preconsentAllowed"].asBoolean
            )
        }
    }
}
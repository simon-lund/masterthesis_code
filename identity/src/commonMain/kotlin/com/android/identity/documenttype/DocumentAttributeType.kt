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
 * Enumeration of the different types of Document Attributes
 *
 * Attributes in documents can have different types and this
 * enumeration contains a type-system generic enough to be
 * used across various document formats. This is useful for
 * wallet and reader user interfaces which wants to provide UI
 * for inputting or displaying documents attributes.
 */
sealed class DocumentAttributeType {
    object Blob : DocumentAttributeType()
    object String : DocumentAttributeType()
    object Number : DocumentAttributeType()
    object Date : DocumentAttributeType()
    object DateTime : DocumentAttributeType()
    object Picture : DocumentAttributeType()
    object Boolean : DocumentAttributeType()
    object ComplexType : DocumentAttributeType()
    class StringOptions(val options: List<StringOption>) : DocumentAttributeType()
    class IntegerOptions(val options: List<IntegerOption>) : DocumentAttributeType()

    /**
     * Converts the type to a [DataItem].
     */
    fun toDataItem(): DataItem {
        return CborMap.builder().apply {
            when (this@DocumentAttributeType) {
                is Blob -> put("type", "Blob")
                is String -> put("type", "String")
                is Number -> put("type", "Number")
                is Date -> put("type", "Date")
                is DateTime -> put("type", "DateTime")
                is Picture -> put("type", "Picture")
                is Boolean -> put("type", "Boolean")
                is ComplexType -> put("type", "ComplexType")
                is StringOptions -> put("type", "StringOptions")
                is IntegerOptions -> put("type", "IntegerOptions")
            }

            // Type is either StringOptions or IntegerOptions, we also need to add the options
            if (this@DocumentAttributeType is StringOptions || this@DocumentAttributeType is IntegerOptions) {
                val optionsArray = putArray("options")
                // TODO: Add options to the array
            }
        }.end().build()
    }

    companion object {
        /**
         * Creates a [DocumentAttributeType] from a [DataItem].
         *
         * @param dataItem must have been encoded with [toDataItem].
         */
        fun fromDataItem(dataItem: DataItem): DocumentAttributeType {
            return when(dataItem["type"].asTstr) {
                "Blob" -> Blob
                "String" -> String
                "Number" -> Number
                "Date" -> Date
                "DateTime" -> DateTime
                "Picture" -> Picture
                "Boolean" -> Boolean
                "ComplexType" -> ComplexType
                "StringOptions" ->  {
                    StringOptions(emptyList())
                }
                "IntegerOptions" -> {
                    IntegerOptions(emptyList())
                }
                else -> throw IllegalArgumentException("Unknown type")
            }

        }
    }
}
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
 * Class that represents a combination of a string value
 * and a name suitable for display presentation.
 *
 * @param value a string value.
 * @param displayName a name suitable for display of the value.
 */
data class StringOption(
    val value: String?,
    val displayName: String
) {
    /**
     * Show only the [displayName] in the toString function.
     */
    override fun toString(): String = displayName

    /**
     * Converts the [StringOption] to a [DataItem].
     */
    fun toDataItem(): DataItem {
        return CborMap.builder().apply {
            value?.let { put("value", it) }
            put("displayName", displayName)
        }.end().build()
    }

    companion object {
        /**
         * Converts a [DataItem] to a [StringOption].
         */
        fun fromDataItem(dataItem: DataItem): StringOption {
            return StringOption(
                value = dataItem.getOrNull("value")?.asTstr,
                displayName = dataItem["displayName"].asTstr
            )
        }
    }
}
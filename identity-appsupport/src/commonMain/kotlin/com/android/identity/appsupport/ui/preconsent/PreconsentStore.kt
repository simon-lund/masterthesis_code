package com.android.identity.appsupport.ui.preconsent

import com.android.identity.cbor.Cbor
import com.android.identity.storage.StorageEngine
import com.android.identity.util.Logger

private const val PRECONSENT_ITEM_PREFIX = "PRECONSENT_"
private const val LOGGER_TAG = "PreconsentStore"

/**
 * Store for preconsents.
 *
 * Preconsents are stored in the format: PRECONSENT_<id> -> <preconsent> (CBOR encoded).
 * The store is backed by a [StorageEngine]. Preconsents are loaded from storage on initialization.
 *
 * @param storageEngine The storage engine to use.
 */
class PreconsentStore(
    private val storageEngine: StorageEngine,
) {
    var preconsents: List<Preconsent> = emptyList()
        private set

    companion object {
        private var instance: PreconsentStore? = null

        fun getInstance(): PreconsentStore {
            return instance!!
        }

        fun createInstance(storageEngine: StorageEngine): PreconsentStore {
            instance?.let { throw IllegalStateException("PreconsentStore already initialized") }
            instance = PreconsentStore(storageEngine)
            return instance!!
        }
    }

    init {
        storageEngine
            .enumerate()
            .filter {
                // Only load preconsents, i.e. keys starting with the preconsent prefix.
                it.startsWith(PRECONSENT_ITEM_PREFIX)
            }.forEach {
                // Since we store preconsents in total, data might be outdated and cause lookup errors (e.g. removed enum values).
                // Therefore, we catch any exceptions and remove the preconsent if it fails to load.
                try {
                    preconsents += deserializePreconsent(storageEngine[it]!!)
                } catch (e: Exception) {
                    Logger.e(LOGGER_TAG, "Failed to load preconsent from storage with key $it", e)
                    delete(it.removePrefix(PRECONSENT_ITEM_PREFIX))
                }
            }
    }

    /**
     * Serialize a preconsent to a byte array.
     */
    private fun serializePreconsent(preconsent: Preconsent): ByteArray {
        return Cbor.encode(preconsent.toDataItem())
    }

    /**
     * Deserialize a preconsent from a byte array.
     */
    private fun deserializePreconsent(data: ByteArray): Preconsent {
        return Preconsent.fromDataItem(Cbor.decode(data))
    }

    /**
     * Add a preconsent to the repository.
     *
     * @param preconsent The preconsent to add.
     *
     * @throws IllegalArgumentException if the relying party is untrusted.
     * @throws IllegalArgumentException if the preconsent already exists.
     */
    fun add(preconsent: Preconsent) {
        preconsent.relyingParty.trustPoint ?: throw IllegalArgumentException("Untrusted relying party is not allowed")

        val key = PRECONSENT_ITEM_PREFIX + preconsent.id
        storageEngine[key]?.let { throw IllegalArgumentException("Preconsent already exists") }

        val data = serializePreconsent(preconsent)
        storageEngine.put(key, data)

        // Update "cache" of preconsents.
        preconsents += preconsent
    }

    /**
     * Update a preconsent in the repository.
     * This will update the preconsent with the same ID as the provided preconsent.
     *
     * @param preconsent The preconsent to update.
     *
     * @throws IllegalArgumentException if the relying party is untrusted.
     * @throws IllegalArgumentException if the preconsent does not exist.
     */
    fun update(preconsent: Preconsent) {
        preconsent.relyingParty.trustPoint ?: throw IllegalArgumentException("Untrusted relying party is not allowed")

        val key = PRECONSENT_ITEM_PREFIX + preconsent.id
        storageEngine[key] ?: throw IllegalArgumentException("Preconsent does not exist")

        val data = serializePreconsent(preconsent)
        storageEngine.put(key, data)

        // Update "cache" of preconsents.
        preconsents = preconsents.map { if (it.id == preconsent.id) preconsent else it }
    }

    /**
     * Delete a preconsent from the repository.
     *
     * @param preconsentId The ID of the preconsent to delete.
     */
    fun delete(preconsentId: String) {
        val key = PRECONSENT_ITEM_PREFIX + preconsentId
        storageEngine.delete(key)

        // Update "cache" of preconsents.
        preconsents = preconsents.filter { it.id != preconsentId }
    }
    // TODO: Implement lookup/check method.
}
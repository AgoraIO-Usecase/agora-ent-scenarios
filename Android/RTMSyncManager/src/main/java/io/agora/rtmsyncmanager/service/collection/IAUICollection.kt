package io.agora.rtmsyncmanager.service.collection

/**
 * Interface for handling various collection operations.
 */
interface IAUICollection {

    /**
     * Subscribes to value changes.
     * @param closure The closure to be called when a value changes.
     */
    fun subscribeValueWillChange(closure: ((publisherId: String, valueCmd: String?, value: Map<String, Any>) -> Map<String, Any>?)?)

    /**
     * Subscribes to additions.
     * @param closure The closure to be called when an addition occurs.
     */
    fun subscribeWillAdd(closure: ((publisherId: String, valueCmd: String?, value: Map<String, Any>) -> AUICollectionException?)?)

    /**
     * Subscribes to updates.
     * @param closure The closure to be called when an update occurs.
     */
    fun subscribeWillUpdate(closure: ((publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>) -> AUICollectionException?)?)

    /**
     * Subscribes to merges.
     * @param closure The closure to be called when a merge occurs.
     */
    fun subscribeWillMerge(closure: ((publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>) -> AUICollectionException?)?)

    /**
     * Subscribes to removals.
     * @param closure The closure to be called when a removal occurs.
     */
    fun subscribeWillRemove(closure: ((publisherId: String, valueCmd: String?, value: Map<String, Any>) -> AUICollectionException?)?)

    /**
     * Subscribes to calculations.
     * @param closure The closure to be called when a calculation occurs.
     */
    fun subscribeWillCalculate(closure: ((publisherId: String, valueCmd: String?, value: Map<String, Any>, cKey: List<String>, cValue: Int, cMin: Int, cMax: Int) -> AUICollectionException?)?)

    /**
     * Subscribes to attribute changes.
     * @param closure The closure to be called when an attribute changes.
     */
    fun subscribeAttributesDidChanged(closure: ((channelName: String, observeKey: String, value: AUIAttributesModel) -> Unit)?)

    /**
     * Subscribes to attribute sets.
     * @param closure The closure to be called when an attribute is set.
     */
    fun subscribeAttributesWillSet(closure: ((channelName: String, observeKey: String, valueCmd: String?, value: AUIAttributesModel) -> AUIAttributesModel)?)

    /**
     * Retrieves the metadata.
     * @param callback The callback to be called with the retrieved metadata.
     */
    fun getMetaData(callback: ((error: AUICollectionException?, value: Any?) -> Unit)?)

    /**
     * Retrieves the local metadata.
     * @return The local metadata.
     */
    fun getLocalMetaData(): AUIAttributesModel?

    /**
     * Releases the collection.
     */
    fun release()
}

/**
 * Interface for handling various map collection operations.
 */
interface IAUIMapCollection : IAUICollection {

    /**
     * Updates the metadata.
     * @param valueCmd The command for the update.
     * @param value The new value.
     * @param callback The callback to be called when the update is complete.
     */
    fun updateMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        callback: ((error: AUICollectionException?) -> Unit)?
    )

    /**
     * Merges the metadata.
     * @param valueCmd The command for the merge.
     * @param value The value to be merged.
     * @param callback The callback to be called when the merge is complete.
     */
    fun mergeMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        callback: ((error: AUICollectionException?) -> Unit)?
    )

    /**
     * Adds to the metadata.
     * @param valueCmd The command for the addition.
     * @param value The value to be added.
     * @param callback The callback to be called when the addition is complete.
     */
    fun addMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        callback: ((error: AUICollectionException?) -> Unit)?
    )

    /**
     * Removes from the metadata.
     * @param valueCmd The command for the removal.
     * @param callback The callback to be called when the removal is complete.
     */
    fun removeMetaData(
        valueCmd: String?,
        callback: ((error: AUICollectionException?) -> Unit)?
    )

    /**
     * Calculates the metadata.
     * @param valueCmd The command for the calculation.
     * @param key The key for the calculation.
     * @param value The value for the calculation.
     * @param min The minimum value for the calculation.
     * @param max The maximum value for the calculation.
     * @param callback The callback to be called when the calculation is complete.
     */
    fun calculateMetaData(
        valueCmd: String?,
        key: List<String>,
        value: Int,
        min: Int,
        max: Int,
        callback: ((error: AUICollectionException?) -> Unit)?
    )

    /**
     * Cleans the metadata.
     * @param callback The callback to be called when the cleaning is complete.
     */
    fun cleanMetaData(callback: ((error: AUICollectionException?) -> Unit)?)
}

/**
 * Interface for handling various list collection operations.
 */
interface IAUIListCollection : IAUICollection {

    /**
     * Updates the metadata.
     * @param valueCmd The command for the update.
     * @param value The new value.
     * @param filter The filter for the update.
     * @param callback The callback to be called when the update is complete.
     */
    fun updateMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>? = null,
        callback: ((error: AUICollectionException?) -> Unit)?
    )

    /**
     * Merges the metadata.
     * @param valueCmd The command for the merge.
     * @param value The value to be merged.
     * @param filter The filter for the merge.
     * @param callback The callback to be called when the merge is complete.
     */
    fun mergeMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>? = null,
        callback: ((error: AUICollectionException?) -> Unit)?
    )

    /**
     * Adds to the metadata.
     * @param valueCmd The command for the addition.
     * @param value The value to be added.
     * @param filter The filter for the addition.
     * @param callback The callback to be called when the addition is complete.
     */
    fun addMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>? = null,
        callback: ((error: AUICollectionException?) -> Unit)?
    )

    /**
     * Removes from the metadata.
     * @param valueCmd The command for the removal.
     * @param filter The filter for the removal.
     * @param callback The callback to be called when the removal is complete.
     */
    fun removeMetaData(
        valueCmd: String?,
        filter: List<Map<String, Any>>? = null,
        callback: ((error: AUICollectionException?) -> Unit)?
    )

    /**
     * Calculates the metadata.
     * @param valueCmd The command for the calculation.
     * @param key The key for the calculation.
     * @param value The value for the calculation.
     * @param min The minimum value for the calculation.
     * @param max The maximum value for the calculation.
     * @param filter The filter for the calculation.
     * @param callback The callback to be called when the calculation is complete.
     */
    fun calculateMetaData(
        valueCmd: String?,
        key: List<String>,
        value: Int,
        min: Int,
        max: Int,
        filter: List<Map<String, Any>>? = null,
        callback: ((error: AUICollectionException?) -> Unit)?
    )

    /**
     * Cleans the metadata.
     * @param callback The callback to be called when the cleaning is complete.
     */
    fun cleanMetaData(callback: ((error: AUICollectionException?) -> Unit)?)
}
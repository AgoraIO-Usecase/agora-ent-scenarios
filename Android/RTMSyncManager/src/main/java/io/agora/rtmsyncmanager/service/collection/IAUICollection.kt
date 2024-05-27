package io.agora.rtmsyncmanager.service.collection

/**
 * IAUICollection
 *
 * @constructor Create empty IAUICollection
 */
interface IAUICollection {

    /**
     * Subscribe will add
     *
     * @param closure
     */
    fun subscribeWillAdd(closure: ((publisherId: String, valueCmd: String?, value: Map<String, Any>) -> AUICollectionException?)?)

    /**
     * Subscribe will update
     *
     * @param closure
     */
    fun subscribeWillUpdate(closure: ((publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>) -> AUICollectionException?)?)

    /**
     * Subscribe will merge
     *
     * @param closure
     */
    fun subscribeWillMerge(closure: ((publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>) -> AUICollectionException?)?)

    /**
     * Subscribe will remove
     *
     * @param closure
     */
    fun subscribeWillRemove(closure: ((publisherId: String, valueCmd: String?, value: Map<String, Any>) -> AUICollectionException?)?)

    /**
     * Subscribe will calculate
     *
     * @param closure
     */
    fun subscribeWillCalculate(closure: ((publisherId: String, valueCmd: String?, value: Map<String, Any>, cKey: List<String>, cValue: Int, cMin: Int, cMax: Int) -> AUICollectionException?)?)

    /**
     * Subscribe attributes did changed
     *
     * @param closure
     */
    fun subscribeAttributesDidChanged(closure: ((channelName: String, observeKey: String, value: AUIAttributesModel) -> Unit)?)

    /**
     * Subscribe attributes will set
     *
     * @param closure
     */
    fun subscribeAttributesWillSet(closure: ((channelName: String, observeKey: String, valueCmd: String?, value: AUIAttributesModel) -> AUIAttributesModel)?)


    /**
     * Get meta data
     *
     * @param callback
     */
    fun getMetaData(callback: ((error: AUICollectionException?, value: Any?) -> Unit)?)

    /**
     * Release
     *
     */
    fun release()
}

interface IAUIMapCollection : IAUICollection {
    /**
     * Update meta data
     *
     * @param valueCmd
     * @param value
     * @param filter
     * @param callback
     */
    fun updateMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        callback: ((error: AUICollectionException?) -> Unit)?
    )

    /**
     * Merge meta data
     *
     * @param valueCmd
     * @param value
     * @param filter
     * @param callback
     */
    fun mergeMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        callback: ((error: AUICollectionException?) -> Unit)?
    )

    /**
     * Add meta data
     *
     * @param valueCmd
     * @param value
     * @param filter
     * @param callback
     */
    fun addMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        callback: ((error: AUICollectionException?) -> Unit)?
    )

    /**
     * Remove meta data
     *
     * @param valueCmd
     * @param filter
     * @param callback
     */
    fun removeMetaData(
        valueCmd: String?,
        callback: ((error: AUICollectionException?) -> Unit)?
    )

    /**
     * Calculate meta data
     *
     * @param valueCmd
     * @param key
     * @param value
     * @param min
     * @param max
     * @param filter
     * @param callback
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
     * Clean meta data
     *
     * @param callback
     */
    fun cleanMetaData(callback: ((error: AUICollectionException?) -> Unit)?)
}

interface IAUIListCollection : IAUICollection {
    /**
     * Update meta data
     *
     * @param valueCmd
     * @param value
     * @param filter
     * @param callback
     */
    fun updateMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>? = null,
        callback: ((error: AUICollectionException?) -> Unit)?
    )

    /**
     * Merge meta data
     *
     * @param valueCmd
     * @param value
     * @param filter
     * @param callback
     */
    fun mergeMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>? = null,
        callback: ((error: AUICollectionException?) -> Unit)?
    )

    /**
     * Add meta data
     *
     * @param valueCmd
     * @param value
     * @param filter
     * @param callback
     */
    fun addMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>? = null,
        callback: ((error: AUICollectionException?) -> Unit)?
    )

    /**
     * Remove meta data
     *
     * @param valueCmd
     * @param filter
     * @param callback
     */
    fun removeMetaData(
        valueCmd: String?,
        filter: List<Map<String, Any>>? = null,
        callback: ((error: AUICollectionException?) -> Unit)?
    )

    /**
     * Calculate meta data
     *
     * @param valueCmd
     * @param key
     * @param value
     * @param min
     * @param max
     * @param filter
     * @param callback
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
     * Clean meta data
     *
     * @param callback
     */
    fun cleanMetaData(callback: ((error: AUICollectionException?) -> Unit)?)
}
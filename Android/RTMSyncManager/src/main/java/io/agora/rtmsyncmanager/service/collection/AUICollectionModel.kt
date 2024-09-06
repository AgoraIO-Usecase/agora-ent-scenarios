package io.agora.rtmsyncmanager.service.collection

import androidx.annotation.IntDef

/**
 * Constants representing the different types of collection operations.
 */
const val AUICollectionOperationTypeAdd = 0 // Addition
const val AUICollectionOperationTypeUpdate = 1 // Update, replaces the root node of the passed map
const val AUICollectionOperationTypeMerge = 2 // Merge, replaces each child node of the passed map
const val AUICollectionOperationTypeRemove = 3 // Removal
const val AUICollectionOperationTypeClean = 4 // Cleans the corresponding scene's key/value, effectively removing all information about this collection from the RTM metadata
const val AUICollectionOperationTypeCalculate = 10 // Calculation, increment/decrement

/**
 * Annotation for the collection operation type.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@IntDef(
    AUICollectionOperationTypeAdd,
    AUICollectionOperationTypeUpdate,
    AUICollectionOperationTypeMerge,
    AUICollectionOperationTypeRemove,
    AUICollectionOperationTypeClean,
    AUICollectionOperationTypeCalculate,
)
annotation class AUICollectionOperationType

/**
 * Constants representing the different types of collection messages.
 */
const val AUICollectionMessageTypeNormal = 1
const val AUICollectionMessageTypeReceipt = 2

/**
 * Annotation for the collection message type.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@IntDef(
    AUICollectionMessageTypeNormal,
    AUICollectionMessageTypeReceipt
)
annotation class AUICollectionMessageType

/**
 * Data class representing the payload of a collection message.
 */
data class AUICollectionMessagePayload(
    @AUICollectionOperationType val type: Int = AUICollectionOperationTypeUpdate,
    val dataCmd: String?,
    val data: Map<String, Any>?,
    val filter: Any? = null
)

/**
 * Data class representing a collection message.
 */
data class AUICollectionMessage(
    val channelName: String?,
    @AUICollectionMessageType val messageType: Int = AUICollectionMessageTypeNormal,
    val uniqueId: String?,
    val sceneKey: String?,
    val payload: AUICollectionMessagePayload?
)

/**
 * Data class representing a collection calculation value.
 */
data class AUICollectionCalcValue(
    val value: Int,
    val min: Int,
    val max: Int
)

/**
 * Data class representing a collection calculation data.
 */
data class AUICollectionCalcData(
    val key: List<String>,
    val value: AUICollectionCalcValue,
)

/**
 * Data class representing a collection error.
 */
data class AUICollectionError(
    val code: Int = 0,
    val reason: String = "",
)

/**
 * Class representing a model of attributes.
 */
class AUIAttributesModel {
    private var attributes: Any? = null

    /**
     * Constructor for creating an AUIAttributesModel from a list of maps.
     */
    constructor(list: List<Map<String, Any>>){
        attributes = list
    }

    /**
     * Constructor for creating an AUIAttributesModel from a map.
     */
    constructor(map: Map<String, Any>){
        attributes = map
    }

    /**
     * Retrieves the attributes as a map.
     */
    fun getMap() = attributes as? Map<String, Any>

    /**
     * Retrieves the attributes as a list of maps.
     */
    fun getList() = attributes as? List<Map<String, Any>>

    /**
     * Returns a string representation of the AUIAttributesModel.
     */
    override fun toString(): String {
        return "AUIAttributesModel(attributes=$attributes)"
    }
}
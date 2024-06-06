package io.agora.rtmsyncmanager.service.collection

import android.util.Log
import org.json.JSONObject

object AUICollectionUtils {

    fun mergeMap(origMap: Map<String, Any>, newMap: Map<String, Any>): Map<String, Any> {
        val resultMap = HashMap<String, Any>(origMap)
        newMap.forEach { (k, v) ->
            val dic = v as? Map<String, Any>
            if (dic != null) {
                val origDic = mutableMapOf<String, Any>()
                if (resultMap[k] is JSONObject) {
                    val json = resultMap[k] as JSONObject
                    val keys = json.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        origDic[key] = json.get(key)
                    }
                } else if (resultMap[k] is Map<*, *>) {
                    val map = resultMap[k] as Map<*, *>
                    map.keys.forEach { key ->
                        if (key !is String) return@forEach
                        origDic[key] = map[key] as Any
                    }
                }
                val newDic = mergeMap(origDic, dic)
                resultMap[k] = newDic
            } else {
                resultMap[k] = v
            }
        }
        return resultMap
    }

    fun getItemIndexes(array: List<Map<String, Any>>, filter: List<Map<String, Any>>?): List<Int>? {
        val indexes = mutableListOf<Int>()
        if (filter == null) {
            array.indices.forEach { index ->
                indexes.add(index)
            }
            return if (indexes.isEmpty()) null else indexes
        }
        array.forEachIndexed { index, value ->
            filter.forEach { filterItem ->
                var match = 0
                for (entry in filterItem) {
                    val k = entry.key
                    val v = entry.value
                    if(isMatchFilter(k, value, v)){
                        match += 1
                    }
                }
                if (match == filterItem.keys.size) {
                    indexes.add(index)
                    return@forEach
                }
            }
        }
        return if (indexes.isEmpty()) null else indexes
    }

    private fun isMatchFilter(key:String, itemValue: Map<String, Any>, filterValue: Any?): Boolean {
        val valueV = itemValue[key]
        if(valueV == filterValue){
            return true
        }

        val valueMap = valueV as? Map<String, Any>
        val filterMap = filterValue as? Map<String, Any>
        if(valueMap != null && filterMap != null){
            filterMap.keys.firstOrNull()?.let { filterVKey ->
                return isMatchFilter(filterVKey, valueMap, filterMap[filterVKey])
            }
        }
        return false
    }


    @Throws(AUICollectionException::class)
    fun calculateMap(origMap: Map<String, Any>,
                     key: List<String>,
                     value: Int,
                     min: Int,
                     max: Int): Map<String, Any> {
        val _origMap = origMap.toMutableMap()
        if (key.size > 1) {
            val curKey = key.firstOrNull() ?: ""
            val subKey = key.subList(1, key.size)

            val subValue = _origMap[curKey] as? MutableMap<String, Any>
                ?: throw AUICollectionException.ErrorCode.calculateMapFail.toException()

            val newMap: Map<String, Any> = try {
                calculateMap(subValue, subKey, value, min, max)
            } catch (e: AUICollectionException) {
                throw e
            }

            _origMap[curKey] = newMap
            return _origMap
        }

        val curKey = key.firstOrNull() ?: return emptyMap()
        val subValue = _origMap[curKey] as? Long
            ?: throw AUICollectionException.ErrorCode.calculateMapFail.toException()

        val curValue = subValue + value
        if (curValue > max || curValue < min) {
            throw AUICollectionException.ErrorCode.calculateMapOutOfRange.toException()
        }

        _origMap[curKey] = curValue
        return _origMap
    }
}
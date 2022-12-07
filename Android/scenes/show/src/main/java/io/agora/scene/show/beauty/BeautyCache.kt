package io.agora.scene.show.beauty

object BeautyCache {

    private val cacheItemValueMap = mutableMapOf<Int, Float>() // key: itemId
    private val cacheItemOperation = mutableMapOf<Int, List<Int>>()


    fun getItemValue(itemId: Int): Float = cacheItemValueMap[itemId] ?: 0.0f

    fun getLastOperationItemId(groupId: Int): Int {
        return cacheItemOperation[groupId]?.lastOrNull() ?: groupId
    }

    fun clean(){
        cacheItemValueMap.clear()
        cacheItemOperation.clear()
    }


    internal fun cacheItemValue(groupId: Int, itemId: Int, value: Float) {
        if (itemId == groupId) {
            cacheItemValueMap.keys.toList().forEach {
                if ((it and groupId) > 0) {
                    cacheItemValueMap.remove(it)
                }
            }
            return
        }
        cacheItemValueMap[itemId] = value
    }

    internal fun cacheOperation(groupId: Int, itemId: Int) {
        if (itemId == groupId) {
            cacheItemOperation.remove(groupId)
            return
        }
        cacheItemOperation[groupId] = arrayListOf<Int>().apply {
            cacheItemOperation[groupId]?.let { addAll(it) }
            add(itemId)
        }
    }

    internal fun restoreByOperation(processor: IBeautyProcessor) {
        cacheItemOperation[GROUP_ID_BEAUTY]?.let { list ->
            list.lastOrNull()?.let {
                processor.setFaceBeautify(it, cacheItemValueMap[it] ?: 0.0f)
            }
        }
        cacheItemOperation[GROUP_ID_FILTER]?.let { list ->
            list.lastOrNull()?.let {
                processor.setFilter(it, cacheItemValueMap[it] ?: 0.0f)
            }
        }
        cacheItemOperation[GROUP_ID_EFFECT]?.let { list ->
            list.lastOrNull()?.let {
                processor.setEffect(it, cacheItemValueMap[it] ?: 0.0f)
            }
        }
        cacheItemOperation[GROUP_ID_STICKER]?.let { list ->
            list.lastOrNull()?.let {
                processor.setSticker(it)
            }
        }
    }
}
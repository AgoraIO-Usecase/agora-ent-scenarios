package io.agora.scene.show.beauty

object BeautyCache {

    private val defaultItemValueMap = mapOf(

        Pair(ITEM_ID_ADJUST_SHARPEN, 0.5f),
        Pair(ITEM_ID_ADJUST_CLARITY, 1.0f),

        Pair(ITEM_ID_BEAUTY_SMOOTH, 0.55f),
        Pair(ITEM_ID_BEAUTY_WHITEN, 0.2f),
        Pair(ITEM_ID_BEAUTY_OVERALL, 0.4f),
        Pair(ITEM_ID_BEAUTY_EYE, 0.3f),

        Pair(ITEM_ID_EFFECT_BAIXI, 0.5f),
        Pair(ITEM_ID_EFFECT_TIANMEI, 0.5f),
        Pair(ITEM_ID_EFFECT_CWEI, 0.5f),
        Pair(ITEM_ID_EFFECT_YUANQI, 0.5f),

        Pair(ITEM_ID_FILTER_CREAM, 0.4f),
        Pair(ITEM_ID_FILTER_MAKALONG, 0.4f),
        Pair(ITEM_ID_FILTER_OXGEN, 0.4f),
        Pair(ITEM_ID_FILTER_WUYU, 0.4f),
        Pair(ITEM_ID_FILTER_Po9, 0.4f),
        Pair(ITEM_ID_FILTER_LOLITA, 0.4f),
        Pair(ITEM_ID_FILTER_MITAO, 0.4f),
        Pair(ITEM_ID_FILTER_YINHUA, 0.4f),
        Pair(ITEM_ID_FILTER_BEIHAIDAO, 0.4f),
        Pair(ITEM_ID_FILTER_S3, 0.4f),
    )
    // key: itemId
    private val cacheItemValueMap = mutableMapOf<Int, Float>()
    private val cacheItemOperation = mutableMapOf<Int, ArrayList<Int>>()

    init {
        reset()
    }

    fun getItemValueWithDefault(itemId: Int): Float = cacheItemValueMap[itemId] ?: defaultItemValueMap[itemId] ?: 0.0f

    fun getLastOperationItemId(groupId: Int): Int {
        return cacheItemOperation[groupId]?.lastOrNull() ?: groupId
    }

    fun getItemValue(itemId: Int): Float = cacheItemValueMap[itemId] ?: 0.0f

    fun getItemValue(itemId: Int, def: Float): Float = cacheItemValueMap[itemId] ?: def

    internal fun reset(){
        cacheItemValueMap.apply {
            clear()
            defaultItemValueMap.entries.forEach { entry ->
                put(entry.key, entry.value)
            }
        }
        cacheItemOperation.apply {
            clear()
            // 设置默认选中项
            put(GROUP_ID_BEAUTY, arrayListOf(ITEM_ID_BEAUTY_SMOOTH))
            put(GROUP_ID_ADJUST, arrayListOf(ITEM_ID_ADJUST_NONE))
            put(GROUP_ID_FILTER, arrayListOf(ITEM_ID_FILTER_NONE))
            put(GROUP_ID_EFFECT, arrayListOf(ITEM_ID_EFFECT_NONE))
            put(GROUP_ID_STICKER, arrayListOf(ITEM_ID_STICKER_NONE))
            put(GROUP_ID_VIRTUAL_BG, arrayListOf(ITEM_ID_VIRTUAL_BG_NONE))
        }
    }

    internal fun resetGroupValue(groupId: Int){
        cacheItemValueMap.keys.toList().forEach {
            if ((it and groupId) > 0) {
                cacheItemValueMap.remove(it)
            }
        }
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
            cacheItemOperation[groupId]?.let {
                it.remove(itemId)
                addAll(it)
            }
            add(itemId)
        }
    }

    internal fun restoreByOperation(processor: IBeautyProcessor) {
        cacheItemOperation[GROUP_ID_BEAUTY]?.let { list ->
            list.lastOrNull()?.let {
                processor.setFaceBeautify(it, getItemValue(it))
            }
        }
        cacheItemOperation[GROUP_ID_FILTER]?.let { list ->
            list.lastOrNull()?.let {
                processor.setFilter(it, getItemValue(it))
            }
        }
        cacheItemOperation[GROUP_ID_EFFECT]?.let { list ->
            list.lastOrNull()?.let {
                processor.setEffect(it, getItemValue(it))
            }
        }
        cacheItemOperation[GROUP_ID_ADJUST]?.let { list ->
            list.lastOrNull()?.let {
                processor.setAdjust(it, getItemValue(it))
            }
        }
        cacheItemOperation[GROUP_ID_STICKER]?.let { list ->
            list.lastOrNull()?.let {
                processor.setSticker(it)
            }
        }
    }
}
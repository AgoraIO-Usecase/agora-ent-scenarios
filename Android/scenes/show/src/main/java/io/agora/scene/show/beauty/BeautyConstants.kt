package io.agora.scene.show.beauty

const val GROUP_ID_BEAUTY = 0x00000010 // 美颜
const val ITEM_ID_BEAUTY_NONE       = GROUP_ID_BEAUTY
const val ITEM_ID_BEAUTY_SMOOTH     = GROUP_ID_BEAUTY + 1 // 磨皮
const val ITEM_ID_BEAUTY_WHITEN     = GROUP_ID_BEAUTY + 2 // 美白
const val ITEM_ID_BEAUTY_OVERALL    = GROUP_ID_BEAUTY + 3 // 瘦脸
const val ITEM_ID_BEAUTY_CHEEKBONE  = GROUP_ID_BEAUTY + 4 // 瘦颧骨
const val ITEM_ID_BEAUTY_JAWBONE    = GROUP_ID_BEAUTY + 5 // 下颌骨
const val ITEM_ID_BEAUTY_EYE        = GROUP_ID_BEAUTY + 6 // 大眼
const val ITEM_ID_BEAUTY_TEETH      = GROUP_ID_BEAUTY + 7 // 美牙
const val ITEM_ID_BEAUTY_FOREHEAD   = GROUP_ID_BEAUTY + 8 // 额头
const val ITEM_ID_BEAUTY_NOSE       = GROUP_ID_BEAUTY + 9 // 瘦鼻
const val ITEM_ID_BEAUTY_MOUTH      = GROUP_ID_BEAUTY + 10 // 嘴形
const val ITEM_ID_BEAUTY_CHIN       = GROUP_ID_BEAUTY + 11 // 下巴
const val ITEM_ID_BEAUTY_BRIGHT_EYE       = GROUP_ID_BEAUTY + 12 // 亮眼
const val ITEM_ID_BEAUTY_REMOVE_DARK_CIRCLES       = GROUP_ID_BEAUTY + 13 // 祛黑眼圈
const val ITEM_ID_BEAUTY_REMOVE_NASOLABIAL_FOLDS        = GROUP_ID_BEAUTY + 14 // 祛法令纹
const val ITEM_ID_BEAUTY_REDDEN       = GROUP_ID_BEAUTY + 15 // 红润


const val GROUP_ID_FILTER = GROUP_ID_BEAUTY shl 4 // 滤镜
const val ITEM_ID_FILTER_NONE       = GROUP_ID_FILTER
const val ITEM_ID_FILTER_CREAM      = GROUP_ID_FILTER + 1 // 奶油
const val ITEM_ID_FILTER_MAKALONG   = GROUP_ID_FILTER + 2 // 马卡龙
const val ITEM_ID_FILTER_OXGEN      = GROUP_ID_FILTER + 3 // 氧气
const val ITEM_ID_FILTER_WUYU       = GROUP_ID_FILTER + 4 // 物语
const val ITEM_ID_FILTER_Po9        = GROUP_ID_FILTER + 5 // 海边人物
const val ITEM_ID_FILTER_LOLITA     = GROUP_ID_FILTER + 6 // 洛丽塔
const val ITEM_ID_FILTER_MITAO      = GROUP_ID_FILTER + 7 // 蜜桃
const val ITEM_ID_FILTER_YINHUA     = GROUP_ID_FILTER + 8 // 樱花
const val ITEM_ID_FILTER_BEIHAIDAO  = GROUP_ID_FILTER + 9 // 北海道
const val ITEM_ID_FILTER_S3         = GROUP_ID_FILTER + 10 // 旅途


const val GROUP_ID_EFFECT = GROUP_ID_FILTER shl 4 // 特效
const val ITEM_ID_EFFECT_NONE       = GROUP_ID_EFFECT
const val ITEM_ID_EFFECT_BAIXI      = GROUP_ID_EFFECT + 1 //白皙
const val ITEM_ID_EFFECT_TIANMEI    = GROUP_ID_EFFECT + 2 // 甜美
const val ITEM_ID_EFFECT_CWEI       = GROUP_ID_EFFECT + 3 // C位
const val ITEM_ID_EFFECT_YUANQI     = GROUP_ID_EFFECT + 4 // 元气

const val GROUP_ID_STICKER = GROUP_ID_EFFECT shl 4 // 贴纸
const val ITEM_ID_STICKER_NONE      = GROUP_ID_STICKER
const val ITEM_ID_STICKER_HUAHUA      = GROUP_ID_STICKER + 1 // 鼻涕
const val ITEM_ID_STICKER_WOCHAOTIAN  = GROUP_ID_STICKER + 2 // 周年狂欢

const val GROUP_ID_ADJUST = GROUP_ID_STICKER shl 4 // 调整
const val ITEM_ID_ADJUST_NONE      = GROUP_ID_ADJUST
const val ITEM_ID_ADJUST_CONTRAST = GROUP_ID_ADJUST + 1// 对比度
const val ITEM_ID_ADJUST_SATURATION = GROUP_ID_ADJUST + 2 // 饱和度
const val ITEM_ID_ADJUST_SHARPEN = GROUP_ID_ADJUST + 3 // 锐化
const val ITEM_ID_ADJUST_CLARITY = GROUP_ID_ADJUST + 4 // 清晰度

package io.agora.scene.cantata.ui.widget.rankList

data class RankItem constructor(
    var rank: Int = 0,
    var userName: String? = null,
    var avatar: String? = null,
    var score: Int = 0
)
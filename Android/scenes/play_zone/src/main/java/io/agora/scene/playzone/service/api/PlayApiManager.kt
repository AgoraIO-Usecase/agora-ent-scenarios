package io.agora.scene.playzone.service.api

import io.agora.scene.playzone.R
import io.agora.scene.playzone.hall.GameVendor

class PlayApiManager {

    private val tag = "PlayApiManager"

    //--------------------------------------- 本地构建游戏列表-----------------------------------------
    fun getGameList(vendor: GameVendor, completion: (error: Exception?, list: List<PlayGameListModel>?) -> Unit) {
        when (vendor) {
            GameVendor.GroupPlay -> {
                completion.invoke(null, buildGroupPlayList())
            }

            GameVendor.YYGame -> {
                completion.invoke(null, buildYYGameList())
            }

            else -> {
                completion.invoke(null, buildSubGameList())
            }
        }
    }

    /** 忽然游戏列表  */
    private fun buildSubGameList(): List<PlayGameListModel> {
        // 休闲娱乐
        val leisureAndEntertainment = PlayGameListModel(
            gameType = PlayGameType.leisure_and_entertainment,
            gameList = mutableListOf(
                buildGameModel(1468434637562912769L, "数字转轮", R.drawable.play_zone_ic_szzl),
                buildGameModel(1468090257126719572L, "短道速滑", R.drawable.play_zone_ic_ddsh),
                buildGameModel(1468434723902660610L, "石头剪刀布", R.drawable.play_zone_ic_stjdb),
                buildGameModel(1472142640866779138L, "排雷兵", R.drawable.play_zone_ic_plb),
                buildGameModel(1518058459630743553L, "云蹦迪", R.drawable.play_zone_ic_ybd),
                buildGameModel(1739914495960793090L, "美式8球", R.drawable.play_zone_ic_msbq),
                buildGameModel(1582551621189419010L, "桌球", R.drawable.play_zone_ic_zq),
                buildGameModel(1716668321022017537L, "蛇梯", R.drawable.play_zone_ic_st),
                buildGameModel(1664525565526667266L, "怪物消消乐", R.drawable.play_zone_ic_gwxxl),
                buildGameModel(1689904909564116994L, "对战消消乐", R.drawable.play_zone_ic_dzxxl),
                buildGameModel(1734504890293981185L, "连连看", R.drawable.play_zone_ic_dzxxl),
                buildGameModel(1680881367829176322L, "跳一跳", R.drawable.play_zone_ic_tyt),
                buildGameModel(1704460412809043970L, "欢乐大富翁", R.drawable.play_zone_ic_hldfw),
                buildGameModel(1777154372100497410L, "Carrom", R.drawable.play_zone_ic_carrom),

            )
        )


        // 语音互动
        val voiceInteractions = PlayGameListModel(
            gameType = PlayGameType.voice_interaction,
            gameList = mutableListOf(
                buildGameModel(1472142747708284929L, "狼人杀", R.drawable.play_zone_ic_lrs),
                buildGameModel(1599672757949743105L, "谁是卧底", R.drawable.play_zone_ic_sswd),
                buildGameModel(1461228410184400899L, "你画我猜", R.drawable.play_zone_ic_nhwc),
                buildGameModel(1468434504892882946L, "你说我猜", R.drawable.play_zone_ic_nswc),
                buildGameModel(1468091457989509190L, "数字炸弹", R.drawable.play_zone_ic_szzd),
                buildGameModel(1559736844916183041L, "太空杀", R.drawable.play_zone_ic_tks),
            )
        )

        // 实时竞技
        val realtimeCompetitions = PlayGameListModel(
            gameType = PlayGameType.realtime_competition,
            gameList = mutableListOf(
                buildGameModel(1461228379255603251L, "飞镖达人", R.drawable.play_zone_ic_fbdr),
                buildGameModel(1468434401847222273L, "扫雷", R.drawable.play_zone_ic_sl),
                buildGameModel(1461227817776713818L, "碰碰我最强", R.drawable.play_zone_ic_ppwzq),
                buildGameModel(1585556944972623874L, "魔法大乐斗", R.drawable.play_zone_ic_mfdld),
                buildGameModel(1572529974711259138L, "武道大会", R.drawable.play_zone_ic_wddh),
                buildGameModel(1769608118379003905L, "疯狂找茬", R.drawable.play_zone_ic_fkzc),
            )
        )

        // 经典棋牌
        val classicBoardGameList = PlayGameListModel(
            gameType = PlayGameType.classic_board_games,
            gameList = mutableListOf(
                buildGameModel(1468180338417074177L, "飞行棋", R.drawable.play_zone_ic_fxq),
                buildGameModel(1461297789198663710L, "黑白棋", R.drawable.play_zone_ic_hbq),
                buildGameModel(1676069429630722049L, "五子棋", R.drawable.play_zone_ic_wzq),
                buildGameModel(1557194487352053761L, "TeenPatti", R.drawable.play_zone_ic_teen_patti),
                buildGameModel(1537330258004504578L, "多米诺骨牌", R.drawable.play_zone_ic_dmngp),
                buildGameModel(1533746206861164546L, "Okey101", R.drawable.play_zone_ic_ok101),
                buildGameModel(1472142559912517633L, "UMO", R.drawable.play_zone_ic_umo),
                buildGameModel(1472142695162044417L, "台湾麻将", R.drawable.play_zone_ic_twmj),
                buildGameModel(1557194155570024449L, "德州扑克", R.drawable.play_zone_ic_dz),
                buildGameModel(1759471374694019074L, "Baloot", R.drawable.play_zone_ic_blt),
            )
        )

        // Party Game
        val partyGameList = PlayGameListModel(
            gameType = PlayGameType.party_games,
            gameList = mutableListOf(
                buildGameModel(1490944230389182466L, "友尽闯关", R.drawable.play_zone_ic_picopark),
                buildGameModel(1559030313895714818L, "麻将对对碰", R.drawable.play_zone_ic_mjddp),
                buildGameModel(1564814666005299201L, "科学计算器", R.drawable.play_zone_ic_kxjsq),
                buildGameModel(1564814818015264770L, "疯狂购物", R.drawable.play_zone_ic_fkgw),
                buildGameModel(1572529757165293569L, "连线激斗", R.drawable.play_zone_ic_lxjd),
            )
        )
        return mutableListOf(
            leisureAndEntertainment,
            voiceInteractions,
            realtimeCompetitions,
            classicBoardGameList,
            partyGameList
        )
    }


    private fun buildGameModel(gameId: Long, gameName: String, gamePic: Int): PlayGameInfoModel {
        val model: PlayGameInfoModel = PlayGameInfoModel().apply {
            this.gameId = gameId
            this.gameName = gameName
            this.gamePic = gamePic
        }
        return model
    }

    private fun buildWebGameModel(gameUrl: String, gameName: String, gamePic: Int): PlayGameInfoModel {
        val model: PlayGameInfoModel = PlayGameInfoModel().apply {
            this.gameUrl = gameUrl
            this.gameName = gameName
            this.gamePic = gamePic
        }
        return model
    }

    /**
     * 元游游戏列表
     *
     * @return
     */
    private fun buildYYGameList(): List<PlayGameListModel> {
        // 休闲娱乐
        val leisureAndEntertainment = PlayGameListModel(
            gameType = PlayGameType.leisure_and_entertainment,
            gameList = mutableListOf(
                buildWebGameModel("http://yygame.mmopk.net/923/index.html ", "桌球", R.drawable.play_zone_yy_zq),
            )
        )

        // 经典棋牌
        val classicBoardGameList = PlayGameListModel(
            gameType = PlayGameType.classic_board_games,
            gameList = mutableListOf(
                buildWebGameModel("http://yygame.mmopk.net/924_room/index.html", "斗地主", R.drawable.play_zone_yy_ddz),
                buildWebGameModel("http://yygame.mmopk.net/930/index.html", "掼蛋", R.drawable.play_zone_yy_gd),
                buildWebGameModel("http://yygame.mmopk.net/925/index.html", "五子棋", R.drawable.play_zone_yy_wzq),
            )
        )

        return mutableListOf(leisureAndEntertainment, classicBoardGameList)
    }

    /**
     * 群玩游戏列表
     *
     * @return
     */
    private fun buildGroupPlayList(): List<PlayGameListModel> {
        // 休闲娱乐
        val leisureAndEntertainment = PlayGameListModel(
            gameType = PlayGameType.leisure_and_entertainment,
            gameList = mutableListOf(
                buildWebGameModel(
                    "https://demo.grouplay.cn/room?name=&gameType=17&roomType=17",
                    "飞行棋",
                    R.drawable.play_zone_qw_fxq
                ),
                buildWebGameModel(
                    "https://demo.grouplay.cn/room?name=&gameType=27&roomType=27",
                    "蛇梯",
                    R.drawable.play_zone_qw_st
                ),
                buildWebGameModel(
                    "https://demo.grouplay.cn/room?name=&gameType=3&roomType=3",
                    "猜成语",
                    R.drawable.play_zone_qw_cyjl
                ),
                buildWebGameModel(
                    "https://demo.grouplay.cn/room?name=&gameType=2&roomType=2",
                    "猜图片",
                    R.drawable.play_zone_qw_ctp
                ),
            )
        )

        return mutableListOf(leisureAndEntertainment)
    }
}
//
//  GameApiManager.swift
//  InteractiveJoy
//
//  Created by qinhui on 2024/7/30.
//

import Foundation

enum GameVendor {
    case huRan, yuanyou, qunwan
}

class GameApiManager {
    func getGameList(vendor: GameVendor, completion:(NSError?, [GameModel]?) -> Void) {
        var result = [GameModel]()
        switch vendor {
        case .huRan:
            result = buildHuRanGameList()
            break
        case .yuanyou:
            result = buildYuanYouGameList()
            break
        case .qunwan:
            result = buildQunWanGameList()
            break
        }
        
        completion(nil, result)
    }
    
    ///忽然游戏列表
    private func buildHuRanGameList() -> [GameModel] {
        return [
            GameModel(gameSection: "休闲娱乐", games: [
                Game(gameId: 1468434637562912769, gameName: "数字转轮", gamePic: "play_zone_ic_szzl"),
                Game(gameId: 1468090257126719572, gameName: "短道速滑", gamePic: "play_zone_ic_ddsh"),
                Game(gameId: 1468434723902660610, gameName: "石头剪刀布", gamePic: "play_zone_ic_stjdb"),
                Game(gameId: 1472142640866779138, gameName: "排雷兵", gamePic: "play_zone_ic_plb"),
                Game(gameId: 1518058459630743553, gameName: "云蹦迪", gamePic: "play_zone_ic_ybd"),
                Game(gameId: 1739914495960793090, gameName: "美式8球", gamePic: "play_zone_ic_msbq"),
                Game(gameId: 1582551621189419010, gameName: "桌球", gamePic: "play_zone_ic_zq"),
                Game(gameId: 1716668321022017537, gameName: "蛇梯", gamePic: "play_zone_ic_st"),
                Game(gameId: 1664525565526667266, gameName: "怪物消消乐", gamePic: "play_zone_ic_gwxxl"),
                Game(gameId: 1689904909564116994, gameName: "对战消消乐", gamePic: "play_zone_ic_dzxxl"),
                Game(gameId: 1734504890293981185, gameName: "连连看", gamePic: "play_zone_ic_llk"),
                Game(gameId: 1680881367829176322, gameName: "跳一跳", gamePic: "play_zone_ic_tyt"),
                Game(gameId: 1704460412809043970, gameName: "欢乐大富翁", gamePic: "play_zone_ic_hldfw"),
                Game(gameId: 1777154372100497410, gameName: "Carrom", gamePic: "play_zone_ic_carrom"),
            ]),
            GameModel(gameSection: "语音互动", games: [
                Game(gameId: 1472142747708284929, gameName: "狼人杀", gamePic: "play_zone_ic_lrs"),
                Game(gameId: 1599672757949743105, gameName: "谁是卧底", gamePic: "play_zone_ic_sswd"),
                Game(gameId: 1461228410184400899, gameName: "你画我猜", gamePic: "play_zone_ic_nhwc"),
                Game(gameId: 1468434504892882946, gameName: "你说我猜", gamePic: "play_zone_ic_nswc"),
                Game(gameId: 1468091457989509190, gameName: "数字炸弹", gamePic: "play_zone_ic_szzd"),
                Game(gameId: 1559736844916183041, gameName: "太空杀", gamePic: "play_zone_ic_tks"),
            ]),
            GameModel(gameSection: "实时竞技", games: [
                Game(gameId: 1461228379255603251, gameName: "飞镖达人", gamePic: "play_zone_ic_fbdr"),
                Game(gameId: 1468434401847222273, gameName: "扫雷", gamePic: "play_zone_ic_sl"),
                Game(gameId: 1461227817776713818, gameName: "碰碰我最强", gamePic: "play_zone_ic_ppwzq"),
                Game(gameId: 1585556944972623874, gameName: "魔法大乐斗", gamePic: "play_zone_ic_mfdld"),
                Game(gameId: 1572529974711259138, gameName: "武道大会", gamePic: "play_zone_ic_wddh"),
                Game(gameId: 1769608118379003905, gameName: "疯狂找茬", gamePic: "play_zone_ic_fkzc"),
            ]),
            GameModel(gameSection: "经典棋牌", games: [
                Game(gameId: 1468180338417074177, gameName: "飞行棋", gamePic: "play_zone_ic_fxq"),
                Game(gameId: 1461297789198663710, gameName: "黑白棋", gamePic: "play_zone_ic_hbq"),
                Game(gameId: 1676069429630722049, gameName: "五子棋", gamePic: "play_zone_ic_wzq"),
                Game(gameId: 1557194487352053761, gameName: "TeenPatti", gamePic: "play_zone_ic_teen_patti"),
                Game(gameId: 1537330258004504578, gameName: "多米诺骨牌", gamePic: "play_zone_ic_dmngp"),
                Game(gameId: 1533746206861164546, gameName: "Okey101", gamePic: "play_zone_ic_ok101"),
                Game(gameId: 1472142559912517633, gameName: "UMO", gamePic: "play_zone_ic_umo"),
                Game(gameId: 1472142695162044417, gameName: "台湾麻将", gamePic: "play_zone_ic_twmj"),
                Game(gameId: 1557194155570024449, gameName: "德州扑克", gamePic: "play_zone_ic_dz"),
                Game(gameId: 1759471374694019074, gameName: "Baloot", gamePic: "play_zone_ic_blt"),
            ]),
            GameModel(gameSection: "Party Gam", games: [
                Game(gameId: 1490944230389182466, gameName: "友尽闯关", gamePic: "play_zone_ic_picopark"),
                Game(gameId: 1559030313895714818, gameName: "麻将对对碰", gamePic: "play_zone_ic_mjddp"),
                Game(gameId: 1564814666005299201, gameName: "科学计算器", gamePic: "play_zone_ic_kxjsq"),
                Game(gameId: 1564814818015264770, gameName: "疯狂购物", gamePic: "play_zone_ic_fkgw"),
                Game(gameId: 1572529757165293569, gameName: "连线激斗", gamePic: "play_zone_ic_lxjd"),
            ]),
        ]
    }
    
    ///元游游戏列表
    private func buildYuanYouGameList() -> [GameModel] {
        return [
            GameModel(gameSection: "休闲娱乐", games: [
                Game(gameName: "桌球", gamePic: "play_zone_yy_zq", gameUrl: "http://yygame.mmopk.net/923/index.html"),
            ]),
            GameModel(gameSection: "休闲娱乐", games: [
                Game(gameName: "斗地主", gamePic: "play_zone_yy_ddz", gameUrl: "http://yygame.mmopk.net/924_room/index.html"),
                Game(gameName: "掼蛋", gamePic: "play_zone_yy_gd", gameUrl: "http://yygame.mmopk.net/930/index.html"),
                Game(gameName: "数字转轮", gamePic: "play_zone_yy_wzq", gameUrl: "http://yygame.mmopk.net/925/index.html"),
            ]),
        ]
    }
    
    ///群玩游戏列表
    private func buildQunWanGameList() -> [GameModel] {
        return [
            GameModel(gameSection: "休闲娱乐", games: [
                Game(gameName: "飞行棋", gamePic: "play_zone_qw_fxq", gameUrl: "https://demo.grouplay.cn/room?name=&gameType=17&roomType=17"),
                Game(gameName: "蛇梯", gamePic: "play_zone_qw_st", gameUrl: "https://demo.grouplay.cn/room?name=&gameType=27&roomType=27"),
                Game(gameName: "猜成语", gamePic: "play_zone_qw_cyjl", gameUrl: "https://demo.grouplay.cn/room?name=&gameType=3&roomType=3"),
                Game(gameName: "猜图片", gamePic: "play_zone_qw_ctp", gameUrl: "https://demo.grouplay.cn/room?name=&gameType=2&roomType=2")
            ]),
        ]
    }
}

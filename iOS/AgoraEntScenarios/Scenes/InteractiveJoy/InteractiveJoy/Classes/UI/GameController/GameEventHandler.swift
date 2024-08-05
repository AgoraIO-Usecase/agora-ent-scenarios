//
//  gameEventHandler.swift
//  InteractiveJoy
//
//  Created by qinhui on 2024/7/26.
//

import Foundation

protocol GameEventHandlerDelegate: NSObjectProtocol {
    func onPlayerCaptainChanged(userId: String, model: MGCommonPlayerCaptainModel)
}

class GameEventHandler: SudGameBaseEventHandler {
    private func safeAreaInsets() -> UIEdgeInsets {
        if #available(iOS 11.0, *) {
            return UIApplication.shared.keyWindow?.safeAreaInsets ?? .zero
        }
        return .zero
    }
    //是否为房主
    var isOwner: Bool = false
    
    //玩家列表
    var playerSet: Set<Int64> = []
    
    //机器人列表
    var robotInfoList: [PlayRobotInfo] = []
    
    weak var delegate: GameEventHandlerDelegate?
    
    override func onGetGameCfg() -> GameCfgModel {
        let gameCfgModel = GameCfgModel.default()
        // 可以在此根据自身应用需要配置游戏，例如配置声音
        // You can configure the game according to your application needs here, such as configuring the sound
        gameCfgModel.gameSoundVolume = 100
        // ...
        return gameCfgModel
    }

    override func onGetGameViewInfo() -> GameViewInfoModel {
        // 应用根据自身布局需求在此配置游戏显示视图信息
        // The application configures the game display view information here according to its layout requirements

        // 屏幕安全区
        // Screen Safety zone
        let safeArea = safeAreaInsets()
        // 状态栏高度
        // Status bar height
        let statusBarHeight = safeArea.top == 0 ? 20 : safeArea.top

        let m = GameViewInfoModel()
        let gameViewRect = loadConfigModel.gameView.bounds

        // 游戏展示区域
        // Game display area
        m.view_size.width = Int(gameViewRect.size.width) ?? 0
        m.view_size.height = Int(gameViewRect.size.height) ?? 0
        // 游戏内容布局安全区域，根据自身业务调整顶部间距
        // Game content layout security area, adjust the top spacing according to their own business
        // 顶部间距
        // top spacing
        m.view_game_rect.top = Int(statusBarHeight + 80) ?? 0
        // 左边
        // Left
        m.view_game_rect.left = 0
        // 右边
        // Right
        m.view_game_rect.right = 0
        // 底部安全区域
        // Bottom safe area
        m.view_game_rect.bottom = Int(safeArea.bottom + 100) ?? 0
        return m
    }
    override func onGetCode(_ userId: String, result: @escaping (String) -> Void) {
        // 获取加载游戏的code,此处请求自己服务端接口获取code并回调返回即可
        // Get the code of loading the game, here request your server interface to get the code and callback return
        guard !userId.isEmpty else {
            print("用户ID不能为空")
            return
        }

        // 以下是当前demo向demo应用服务获取code的代码
        // The following is the code that demo obtains the code from demo application service

        // 此接口为QuickStart样例请求接口
        // This interface is a QuickStart sample request interface
        let getCodeUrl = "https://mgp-hello.sudden.ltd/login/v3"
        let dicParam: [String: Any] = ["user_id": userId]
        postHttpRequest(withURL: getCodeUrl, param: dicParam, success: { rootDict in
            guard let dic = rootDict["data"] as? [String: Any],
                  let code = dic["code"] as? String else {
                return
            }
            // 这里的code用于登录游戏sdk服务器
            // The code here is used to log in to the game sdk server
            result(code)
        }, failure: { error in
            print("login game server error: \(error.localizedDescription)")
        })
    }
    
    // 基础接口请求
    // Basic interface request
    private func postHttpRequest(withURL api: String, param: [String: Any], success: @escaping ([String: Any]) -> Void, failure: @escaping (Error) -> Void) {
        guard let url = URL(string: api) else { return }
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        if let bodyData = try? JSONSerialization.data(withJSONObject: param, options: []) {
            request.httpBody = bodyData
        }

        let session = URLSession.shared
        let dataTask = session.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                if let error = error {
                    failure(error)
                    return
                }
                guard let data = data,
                      let responseObject = try? JSONSerialization.jsonObject(with: data, options: []) as? [String: Any] else {
                    return
                }
                success(responseObject)
            }
        }
        dataTask.resume()
    }

    // MARK: - 游戏生命周期回调 Game life cycle callback
    // 游戏开始
    override func onGameStarted() {
        print("Game load finished")
    }

    override func onGameDestroyed() {
        print("Game destroyed")
    }
    
    ///玩家装备变化
    ///玩家: 加入状态  MG_COMMON_PLAYER_IN
    override func onPlayerMGCommonPlayer(in handle: any ISudFSMStateHandle, userId: String, model: MGCommonPlayerInModel) {
        handle.success(sudFSMMGDecorator.handleMGSuccess())
        guard let userId = Int64(userId) else {return}
        if model.isIn {
            playerSet.insert(userId)
        } else {
            playerSet.remove(userId)
        }
    }
    
    ///队长状态
    override func onPlayerMGCommonPlayerCaptain(_ handle: any ISudFSMStateHandle, userId: String, model: MGCommonPlayerCaptainModel) {
        handle.success(sudFSMMGDecorator.handleMGSuccess())
        self.delegate?.onPlayerCaptainChanged(userId: userId, model: model)
    }
    
    override func onGameLoadingProgress(_ stage: Int32, retCode: Int32, progress: Int32) {
        if retCode == 0, progress == 100, self.isOwner {
            sudFSTAPPDecorator.notifyAppComonSelf(in: true, seatIndex: 0, isSeatRandom: true, teamId: 1)
        }
    }
}

extension GameEventHandler {
    func supportRobots(gameId: Int64) -> Bool {
        // TeenPatti, 德州扑克, 友尽闯关 暂时不支持机器人
        let invalidGameIds: Set<Int64> = [1557194487352053761, 1557194155570024449, 1490944230389182466]
        return !invalidGameIds.contains(gameId)
    }
    
    func addRobot() {
        guard let playRobot = findRobot() else { return }
        var aiPlayerList = [AIPlayerInfoModel]()
        var aiPlayerModel = AppCommonGameAddAIPlayersModel()
        aiPlayerModel.isReady = 1
        var aiPlayer = AIPlayerInfoModel()
        aiPlayer.level = playRobot.level
        if let owner = playRobot.owner {
            aiPlayer.userId = owner.userId
            aiPlayer.name = owner.userName
            aiPlayer.avatar = owner.userAvatar
        }
        aiPlayer.gender = playRobot.gender
        aiPlayerList.append(aiPlayer)
        aiPlayerModel.aiPlayers = aiPlayerList
        
        sudFSTAPPDecorator.notifyAppCommonGameAddAIPlayers(aiPlayerModel)
    }
    
    func findRobot() -> PlayRobotInfo? {
        for robot in robotInfoList {
            guard let owner = robot.owner, let ownerId = Int64(owner.userId) else { continue }
            if !playerSet.contains(ownerId) {
                return robot
            }
        }
        
        return nil
    }
    
}

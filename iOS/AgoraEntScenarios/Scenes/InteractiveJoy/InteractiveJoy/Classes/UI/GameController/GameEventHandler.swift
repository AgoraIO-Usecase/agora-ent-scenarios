//
//  gameEventHandler.swift
//  InteractiveJoy
//
//  Created by qinhui on 2024/7/26.
//

import Foundation

class GameEventHandler: SudGameBaseEventHandler {
    private func safeAreaInsets() -> UIEdgeInsets {
        if #available(iOS 11.0, *) {
            return UIApplication.shared.keyWindow?.safeAreaInsets ?? .zero
        }
        return .zero
    }
    
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

   
    // MARK: - 游戏相关事件状态回调通知，以下回调指令根据业务需求实现相应与游戏交互功能，可选指令，根据自身应用需要实现与游戏交互
    // Game-related event status callback notification. The following callback commands can interact with the game according to business requirements. Optional commands can interact with the game according to their own application needs

    // 更多指令支持参阅 https://docs.sud.tech/zh-CN/app/Client/MGFSM/CommonStateGame.html
    // Support more instructions refer to https://docs.sud.tech/en-US/app/Client/MGFSM/CommonStateGame.html

    // 游戏: 准备按钮点击状态   MG_COMMON_SELF_CLICK_READY_BTN
    // Game: Ready button click status MG_COMMON_SELF_CLICK_READY_BTN
    override func onGameMGCommonSelfClickReadyBtn(_ handle: any ISudFSMStateHandle, model: MGCommonSelfClickReadyBtn) {
        handle.success(sudFSMMGDecorator.handleMGSuccess())
    }
    
    // 游戏: 结算界面再来一局按钮点击状态   MG_COMMON_SELF_CLICK_GAME_SETTLE_AGAIN_BTN
    // Game: Settlement interface again to a button click status MG_COMMON_SELF_CLICK_GAME_SETTLE_AGAIN_BTN
    override func onGameMGCommonSelfClickGameSettleAgainBtn(_ handle: any ISudFSMStateHandle, model: MGCommonSelfClickGameSettleAgainBtn) {
        handle.success(sudFSMMGDecorator.handleMGSuccess())
    }
    
    // 游戏: 开始游戏按钮点击状态   MG_COMMON_SELF_CLICK_START_BTN
    // Game: Start game button by clicking status MG_COMMON_SELF_CLICK_START_BTN
    override func onGameMGCommonSelfClickStartBtn(_ handle: any ISudFSMStateHandle, model: MGCommonSelfClickStartBtn) {
        handle.success(sudFSMMGDecorator.handleMGSuccess())
    }
 
    // 通用状态-游戏
    // 游戏: 公屏消息状态    MG_COMMON_PUBLIC_MESSAGE
    // General status - Game
    // Game: public screen message status MG_COMMON_PUBLIC_MESSAGE
    override func onGameMGCommonPublicMessage(_ handle: any ISudFSMStateHandle, model: MGCommonPublicMessageModel) {
        handle.success(sudFSMMGDecorator.handleMGSuccess())
    }
    

    // 游戏: 关键词状态    MG_COMMON_KEY_WORD_TO_HIT
    // Game: Keyword status MG_COMMON_KEY_WORD_TO_HIT
    override func onGameMGCommonKeyWord(toHit handle: any ISudFSMStateHandle, model: MGCommonKeyWrodToHitModel) {
        handle.success(sudFSMMGDecorator.handleMGSuccess())
    }

    // 游戏: 游戏状态   MG_COMMON_GAME_STATE
    // Game: Game state MG_COMMON_GAME_STATE
    
    override func onGameMGCommonGameState(_ handle: ISudFSMStateHandle, model: MGCommonGameState) {
        handle.success(sudFSMMGDecorator.handleMGSuccess())
    }

    // 游戏: ASR状态(开启和关闭语音识别状态)   MG_COMMON_GAME_ASR
    // Game: ASR status (On and off speech recognition status) MG_COMMON_GAME_ASR
    override func onGameMGCommonGameASR(_ handle: ISudFSMStateHandle, model: MGCommonGameASRModel) {
        // 语音采集 || 停止采集
        handle.success(sudFSMMGDecorator.handleMGSuccess())
    }

    // 玩家状态变化
    // 玩家: 加入状态  MG_COMMON_PLAYER_IN
    // Player status changes
    // Player: Adds status MG_COMMON_PLAYER_IN
    override func onPlayerMGCommonPlayer(in handle: any ISudFSMStateHandle, userId: String, model: MGCommonPlayerInModel) {
        handle.success(sudFSMMGDecorator.handleMGSuccess())
    }

    // 玩家: 准备状态  MG_COMMON_PLAYER_READY
    // Player: Ready status MG_COMMON_PLAYER_READY
    override func onPlayerMGCommonPlayerReady(_ handle: ISudFSMStateHandle, userId: String, model: MGCommonPlayerReadyModel) {
        handle.success(sudFSMMGDecorator.handleMGSuccess())
    }

    // 玩家: 队长状态  MG_COMMON_PLAYER_CAPTAIN
    // Player: Captain status MG_COMMON_PLAYER_CAPTAIN
    override func onPlayerMGCommonPlayerCaptain(_ handle: ISudFSMStateHandle, userId: String, model: MGCommonPlayerCaptainModel) {
        handle.success(sudFSMMGDecorator.handleMGSuccess())
    }

    // 玩家: 游戏状态  MG_COMMON_PLAYER_PLAYING
    // Player: Game status MG_COMMON_PLAYER_PLAYING
    override func onPlayerMGCommonPlayerPlaying(_ handle: ISudFSMStateHandle, userId: String, model: MGCommonPlayerPlayingModel) {
        handle.success(sudFSMMGDecorator.handleMGSuccess())
    }

    // 你画我猜: 作画中状态  MG_DG_PAINTING
    // You paint me guess: painting state MG_DG_PAINTING
    override func onPlayerMGDGPainting(_ handle: ISudFSMStateHandle, userId: String, model: MGDGPaintingModel) {
        handle.success(sudFSMMGDecorator.handleMGSuccess())
    }

    // 游戏: 麦克风状态   MG_COMMON_GAME_SELF_MICROPHONE
    // Game: Microphone status MG_COMMON_GAME_SELF_MICROPHONE
    override func onGameMGCommonGameSelfMicrophone(_ handle: ISudFSMStateHandle, model: MGCommonGameSelfMicrophone) {
        handle.success(sudFSMMGDecorator.handleMGSuccess())
    }

    // 游戏: 耳机（听筒，扬声器）状态   MG_COMMON_GAME_SELF_HEADEPHONE
    // Game: Headset (handset, speaker) status MG_COMMON_GAME_SELF_HEADEPHONE
    override func onGameMGCommonGameSelfHeadphone(_ handle: ISudFSMStateHandle, model: MGCommonGameSelfHeadphone) {
        handle.success(sudFSMMGDecorator.handleMGSuccess())
    }

}

//
//  CantataMainViewController.swift
//  Cantata
//
//  Created by CP on 2023/8/29.
//

import Foundation
import AgoraCommon
import AgoraRtcKit
import LSTPopView
import AUIKitCore

private let kChartIds = [3, 4, 2, 6]
let kListPageCount: Int = 10

@objcMembers
public class CantataMainViewController: BaseViewController{

    private var RtcKit: AgoraRtcEngineKit!
    
    @objc public var roomModel: VLRoomListModel?
    @objc public var selSongArray: [VLRoomSelSongModel]?
    @objc public var seatsArray: [VLRoomSeatModel]?
    
    private var ktvApi: KTVApiImpl!
    private var singerRole: KTVSingRole = .audience
    private var isRoomOwner: Bool = false
    private var isEarOn: Bool = false
    private var isNowMicMuted: Bool = false
    private var isEnterSeatNotFirst: Bool = false
    private var rtcDataStreamId: Int = 0
    private var isOnMicSeat: Bool = false
    private var chorusMicView: ChorusMicView!
    private var topView: VLKTVTopView!
    private var botView: VLBottomView!
    private var lrcControlView: DHCLRCControl!
    private var isBrodCaster: Bool = false
    
    private var isSearchMode: Bool = false
    private var searchKeyWord: String?
    
    //歌曲列表相关
    private lazy var jukeBoxView: AUIJukeBoxView = AUIJukeBoxView()
    //歌曲查询列表
    private var searchMusicList: [AUIMusicModel]?
    //点歌列表
    private var musicListMap: [Int: [AUIMusicModel]] = [:]
    //已点列表
    private var addedMusicList: [AUIChooseMusicModel] = [] {
        didSet {
            if let topSong = addedMusicList.first,
                topSong.userId == selSongArray?.first?.userNo,
                !topSong.isPlaying {
//                self.updatePlayStatus(songCode: topSong.songCode, playStatus: .playing) { error in
//
//                }
            }
            self.jukeBoxView.selectedSongCount = addedMusicList.count
        }
    }
    private var chooseSongList: [AUIChooseMusicModel] = []
    /// 已点歌曲key的map
    private var addedMusicSet: NSMutableSet = NSMutableSet()
    
    /// 可置顶歌曲key
    private var pinEnableSet: NSMutableSet = NSMutableSet()
    
    /// 可删除歌曲key
    private var deleteEnableSet: NSMutableSet = NSMutableSet()
    
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        isRoomOwner = VLUserCenter.user.ifMaster
        
        subscribeServiceEvent()
        layoutUI()
        loadRtc()
    }

    public override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        UIViewController.popGestureClose(self)
    }

    public override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)

    }

    public override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        UIViewController.popGestureOpen(self)
        leaveRtcChannel()
        UIApplication.shared.isIdleTimerDisabled = false
    }

    public override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        AgoraRtcEngineKit.destroy()
    }

}

extension CantataMainViewController {
    private func layoutUI() {
        /**
         1.头部布局
         2.歌词组件
         3.麦位
         4.底部点歌和麦克风
         */
        
        view.backgroundColor = .white
        AUIThemeManager.shared.switchTheme(themeName: "Light")
        
        let bgView = UIImageView(frame: self.view.bounds)
        bgView.image = UIImage.sceneImage(name: "dhc_main_bg", bundleName: "DHCResource")
        view.addSubview(bgView)
        
        //头部布局
        topView = VLKTVTopView(frame: CGRect(x: 0, y: ZStatusBarHeight, width: ScreenWidth, height: 60), withDelegate: self)
        view.addSubview(topView)

        let mainBgView = UIImageView(frame: CGRect(x: 0, y: topView.frame.maxY, width: ScreenWidth, height: 520))
        mainBgView.contentMode = .scaleAspectFill
        mainBgView.image = UIImage.sceneImage(name: "dhc_bg", bundleName: "DHCResource")
        mainBgView.isUserInteractionEnabled = true
        view.addSubview(mainBgView)
        
        lrcControlView = DHCLRCControl(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 520))
        lrcControlView.delegate = self
        lrcControlView.backgroundColor = .clear
        mainBgView.addSubview(lrcControlView)
        
        //麦位
        chorusMicView = ChorusMicView(frame: CGRect(x: 0, y: 140, width: ScreenWidth, height: 320), topMicCount: 8)
        mainBgView.addSubview(chorusMicView)
        chorusMicView.delegate = self
        chorusMicView.backgroundColor = .clear
        if let seatsArray = self.seatsArray {
            chorusMicView.seatArray = seatsArray
        }
        
        botView = VLBottomView(frame: CGRect(x: 0, y: ScreenHeight - CGFloat(ZBottombarHeight) - 50, width: ScreenWidth, height: 50))
        botView.delegate = self
        view.addSubview(botView)
        
        jukeBoxView.aui_size = CGSize(width: ScreenWidth, height: 562)
        jukeBoxView.backgroundColor = .white
        
        jukeBoxView.uiDelegate = self
    }
    
    private func loadRtc() {
        RtcKit = AgoraRtcEngineKit.sharedEngine(withAppId: AppContext.shared.appId, delegate: self)
        RtcKit.setAudioProfile(.musicHighQuality)
        RtcKit.setAudioScenario(.gameStreaming)
        RtcKit.setChannelProfile(.liveBroadcasting)
        RtcKit.enableAudioVolumeIndication(50, smooth: 10, reportVad: true)
        RtcKit.enableAudio()
        RtcKit.setEnableSpeakerphone(true)
  
        isNowMicMuted = checkAudicMicIsMuted()
        isBrodCaster = checkIsOnMicSeat()
        
        setupContentInspectConfig()
        
        let config = AgoraDataStreamConfig.init()
        config.ordered = false
        config.syncWithAudio = false
        RtcKit.createDataStream(&rtcDataStreamId, config: config)
        RtcKit.setClientRole(isNowMicMuted ? .broadcaster : .audience)
        
        loadKtvApi()
        
        let ret = RtcKit.joinChannel(byToken: VLUserCenter.user.agoraRTCToken, channelId: roomModel?.roomNo ?? "", uid: UInt(VLUserCenter.user.id) ?? 0 , mediaOptions: updateChannelMediaOption())
        
        let model = getCurrentUserMicSeat()
        if let currentModel = model {
            checkEnterSeatAudioAuthorized()
            isNowMicMuted = model?.isAudioMuted == 1
        } else {
            isNowMicMuted = true
        }
        
        botView.updateMicState(!isNowMicMuted)
    }
    
    private func loadKtvApi() {
        let exChannelToken = VLUserCenter.user.agoraPlayerRTCToken
        let apiConfig = KTVApiConfig(appId: AppContext.shared.appId, rtmToken: VLUserCenter.user.agoraRTMToken, engine: RtcKit, channelName: self.roomModel?.roomNo ?? "", localUid: Int(VLUserCenter.user.id) ?? 0, chorusChannelName: "\(self.roomModel?.roomNo)_ex", chorusChannelToken: exChannelToken, type: .normal, maxCacheSize: 10)
        self.ktvApi = KTVApiImpl(config: apiConfig)
        self.ktvApi.renewInnerDataStreamId()
        self.ktvApi.setLrcView(view: lrcControlView)
        self.ktvApi.setMicStatus(isOnMicOpen: !self.isNowMicMuted)
        self.ktvApi.addEventHandler(ktvApiEventHandler: self)
    }
    
    private func setupContentInspectConfig() {
        let config = AgoraContentInspectConfig()
        
        let dic: [String: Any] = [
            "userNo": VLUserCenter.user.id ?? "unknown",
            "sceneName": "ktv",
        ]
        
        do {
            let jsonData = try JSONSerialization.data(withJSONObject: dic, options: [])
            let jsonStr = String(data: jsonData, encoding: .utf8)
            config.extraInfo = jsonStr
        } catch {
            print("Failed to serialize JSON: \(error)")
        }
        
        let module = AgoraContentInspectModule()
        module.interval = 30
        module.type = .moderation
        config.modules = [module]
        
        self.RtcKit.enableContentInspect(true, config: config)
        
        // 添加音频鉴黄接口
        NetworkManager.shared.voiceIdentify(channelName: self.roomModel?.roomNo ?? "", channelType: 1, sceneType: .ktv) { msg in
            // cp todo
            //KTVLogInfo("voiceIdentify success: \(msg)")
        }
    }
    
    private func updateChannelMediaOption() -> AgoraRtcChannelMediaOptions {

        let option = AgoraRtcChannelMediaOptions()
        option.clientRoleType = (isBrodCaster ? .broadcaster : .audience)
        option.publishMicrophoneTrack = isBrodCaster && !isNowMicMuted
        option.publishCustomAudioTrack = false
        option.channelProfile = .liveBroadcasting
        option.autoSubscribeAudio = true
      // cp todo  option.publishMediaPlayerId = ktvApi.getMediaPlayer().getMediaPlayerId()
        option.enableAudioRecordingOrPlayout = true
        return option
    }
    
    private func leaveRtcChannel() {
        RtcKit.leaveChannel()
    }
}

//订阅消息模块
extension CantataMainViewController {
    func subscribeServiceEvent() {
        AppContext.ktvServiceImp()?.unsubscribeAll()

        AppContext.ktvServiceImp()?.subscribeUserListCountChanged {[weak self] count in
            // TODO
            self?.setRoomUsersCount(count)
        }

        AppContext.ktvServiceImp()?.subscribeSeatListChanged {[weak self] status, seatModel in
            guard let self = self else {return}
            AgoraEntAuthorizedManager.checkMediaAuthorized(parent: self) { granted in
                guard granted else { return }
                guard let model = self.getUserSeatInfo(with: UInt(seatModel.seatIndex)) else {
                    assertionFailure("model == nil")
                    return
                }

                if status == .created || status == .updated {
                    // 上麦消息 / 是否打开视频 / 是否静音
                    model.reset(with: seatModel)
                } else if status == .deleted {
                    // 下麦消息

                    // 下麦重置占位模型
                    model.reset(with: nil)
                }

                self.seatsArray?[model.seatIndex] = model
                self.isOnMicSeat = self.checkIsOnMicSeat()
                self.isBrodCaster = self.checkIsOnMicSeat()
                
                if let currentModel = self.getCurrentUserMicSeat() {
                    self.isNowMicMuted = currentModel.isAudioMuted == 1
                } else {
                    self.isNowMicMuted = true
                }
                
                self.botView.updateMicState(!self.isNowMicMuted)
        
                //更新单个micView
                self.chorusMicView.updateMics(with: model)

                //更新RTC身份
                //上麦主播，下麦观众 更新当前观众即可
                self.updateRTCOption(with: model)
            }
        }

        AppContext.ktvServiceImp()?.subscribeRoomStatusChanged {[weak self] status, roomInfo in
            guard let self = self else {return}
            if status == .updated {
                // 切换背景

                // mv bg / room member count did changed
                let selBgModel = VLKTVSelBgModel()
                selBgModel.imageName = "ktv_mvbg\(roomInfo.bgOption)"
                selBgModel.isSelect = true
               // self.choosedBgModel = selBgModel
            } else if status == .deleted {
                // 房主关闭房间
                if roomInfo.creatorNo == VLUserCenter.user.id {
                    let mes = "连接超时，房间已解散"
                    VLKTVAlert.shared().showKTVToast(withFrame: UIScreen.main.bounds, image: UIImage.sceneImage(name: "empty", bundleName: "DHCResource")!, message: mes, buttonTitle: "ktv_confirm".toSceneLocalization()) {[weak self] flag, text in
                        guard let self = self else {return}
                        VLKTVAlert.shared().dismiss()
                        self.leaveRoom()
                    }
                    return
                }

                self.popForceLeaveRoom()
            }
        }

        // callback if choose song list did changed
        AppContext.ktvServiceImp()?.subscribeChooseSongChanged {[weak self] status, songInfo, songArray in
            guard let self = self else {return}
            // update in-ear monitoring
            self.checkInEarMonitoring()
            
            if status == .deleted {
                let success = self.removeSelSong(songNo: Int(songInfo.songNo ?? "")!, sync: false)
                if !success {
                    self.selSongArray = songArray
                    // cp todo
                    KTVLog.info(text: "removeSelSongWithSongNo fail, reload it")
                }
            } else {
                let song = self.selSong(with: songInfo.songNo ?? "")
                // cp todo
                //KTVLog.info(text: "song did updated: \(song.name) status: \(songInfo.status)")
                self.selSongArray = songArray
            }
        }

        AppContext.ktvServiceImp()?.subscribeNetworkStatusChanged {[weak self] status in
            guard let self = self else {return}
            if status != .open {
                // [VLToast toast:[NSString stringWithFormat:@"network changed: %ld", status]]
                return
            }
            self.subscribeServiceEvent()
           // self._fetchServiceAllData()
        }

        AppContext.ktvServiceImp()?.subscribeRoomWillExpire {
            let isOwner = self.roomModel?.creatorNo == VLUserCenter.user.id
            let mes = isOwner ? "您已体验超过20分钟，当前房间已过期，请退出重新创建房间" : "当前房间已过期,请退出"
            VLKTVAlert.shared().showKTVToast(withFrame: UIScreen.main.bounds, image: UIImage.sceneImage(name: "empty", bundleName: "DHCResource")!, message: mes, buttonTitle: "ktv_confirm".toSceneLocalization()) {[weak self] flag, text in
                guard let self = self else {return}
                VLKTVAlert.shared().dismiss()
                self.leaveRoom()
            }
        }
    }

    func setRoomUsersCount(_ userCount: UInt) {
        if let model = self.roomModel {
            self.roomModel!.roomPeopleNum = String(userCount)
            self.topView.listModel = model
        }
    }

    /// 根据麦位索引获取麦位
    /// - Parameter seatIndex: 麦位索引
    /// - Returns: 对应的麦位信息模型
    func getUserSeatInfo(with seatIndex: UInt) -> VLRoomSeatModel? {
        guard let seatsArray = self.seatsArray else {return nil}
        for model in seatsArray {
            if model.seatIndex == seatIndex {
                return model
            }
        }
        return nil
    }
    
    private func checkInEarMonitoring() {
        if singerRole != .audience {
            self.RtcKit.enable(inEarMonitoring: isEarOn, includeAudioFilters: .builtInAudioFilters)
        }
    }
    
    private func leaveRoom() {
        AppContext.ktvServiceImp()?.leaveRoom(completion: {[weak self] error in
            
            guard let self = self else {return}
            //遍历导航的所有子控制器

            for vc in self.navigationController?.children ?? [] {
                if String(describing: type(of: vc)) == "DHCVLOnLineListVC" {
                    self.navigationController?.popToViewController(vc, animated: true)
                    break
                }
            }
            
        })
    }
    
    private func popForceLeaveRoom() {
        VLKTVAlert.shared().showKTVToast(withFrame: UIScreen.main.bounds,
                                         image: UIImage.sceneImage(name: "empty")!,
                                         message: "room_has_close".toSceneLocalization(),
                                         buttonTitle: "confirm".toSceneLocalization()) { [weak self] (flag, text) in
            guard let weakSelf = self else { return }
            
            for vc in weakSelf.navigationController?.children ?? [] {
                if String(describing: type(of: vc)) == "VLOnLineListVC" {
                    weakSelf.navigationController?.popToViewController(vc, animated: true)
                    break
                }
            }
            
            VLKTVAlert.shared().dismiss()
        }
    }
    
    //下麦
    func leaveSeat(with seatModel: VLRoomSeatModel, completion:@escaping ((Error?) -> Void)) {
        if seatModel.rtcUid == VLUserCenter.user.id {
            if seatModel.isVideoMuted == 1 {
                self.RtcKit.stopPreview()
            }
        }
        
        let inputModel = KTVOutSeatInputModel()
        inputModel.userNo = seatModel.userNo
        inputModel.userId = seatModel.rtcUid
        inputModel.userName = seatModel.name
        inputModel.userHeadUrl = seatModel.headUrl
        inputModel.seatIndex = seatModel.seatIndex
        
        AppContext.ktvServiceImp()?.leaveSeat(with: inputModel, completion: { err in
            completion(err)
        })
    }

    //上麦
    private func enterSeatWithModel(_ index:Int, completion:@escaping ((Error?) -> Void)) {
        let seatModel = KTVOnSeatInputModel()
        seatModel.seatIndex = UInt(index)
        AppContext.ktvServiceImp()?.enterSeat(with: seatModel, completion: { err in
            completion(err)
        })
        checkEnterSeatAudioAuthorized()
    }
    
    private func updateRTCOption(with model: VLRoomSeatModel) {
        //如果是自己就更新 否则不用管
        if model.userNo == VLUserCenter.user.id {
           // if !isRoomOwner {
                let option = AgoraRtcChannelMediaOptions()
                option.clientRoleType = model.rtcUid != nil ? .broadcaster : .audience
                option.publishMicrophoneTrack = isNowMicMuted == false
                self.RtcKit.updateChannel(with: option)
           // }
        }
        
        if !isBrodCaster {
            let option = AgoraRtcChannelMediaOptions()
            option.clientRoleType = .audience
            option.publishMicrophoneTrack = false
            self.RtcKit.updateChannel(with: option)
        }
    }
    
    private func getSeatModel(with index: Int) -> VLRoomSeatModel? {
        return self.seatsArray?.first { $0.seatIndex == index } ?? nil
    }
    
    private func checkEnterSeatAudioAuthorized() {
        if !isEnterSeatNotFirst {
            return
        }
        
        isEnterSeatNotFirst = true
        AgoraEntAuthorizedManager.checkAudioAuthorized(parent: self)
    }
    
    private func checkIsOnMicSeat() -> Bool {
        guard let seatsArray = self.seatsArray else {return false}
        for model in seatsArray {
            if model.rtcUid == VLUserCenter.user.id {
                return true
            }
        }
        return false
    }
    
    private func checkAudicMicIsMuted() -> Bool {
        guard let seatsArray = self.seatsArray else {return false}
        for model in seatsArray {
            if model.rtcUid == VLUserCenter.user.id {
                return model.isAudioMuted == 1
            }
        }
        return false
    }
    
    private func getCurrentUserMicSeat() -> VLRoomSeatModel? {
        guard let seatsArray = self.seatsArray else {return nil}
        for model in seatsArray {
            if model.userNo == VLUserCenter.user.id {
                return model
            }
        }
        return nil
    }
    
    func removeSelSong(songNo: Int, sync: Bool) -> Bool {
        var removed: VLRoomSelSongModel?
        let isTopSong = selSongArray?.first?.songNo == String(songNo)
        
        if isTopSong {
            stopPlaySong()
        }
        
        var updatedList = self.selSongArray?.filter { evaluatedObject in
            if evaluatedObject.songNo == String(songNo) {
                removed = evaluatedObject
                return false
            }
            return true
        }
        
        if let removed = removed {
            //did remove
            self.selSongArray = updatedList

            if sync {
                let inputModel = KTVRemoveSongInputModel()
                inputModel.songNo = removed.songNo
                inputModel.objectId = removed.objectId
                AppContext.ktvServiceImp()?.removeSong(with: inputModel) { error in
                    if let error = error {
                        // cp todo
                       // KTVLogInfo("deleteSongEvent fail: \(removed.songName) \(error.code)")
                    }
                }
            }
            
            return true
        } else {
            return false
        }
    }
        
    private func stopPlaySong() {
        ktvApi.switchSingerRole(newRole: .audience) { state, reason in
            
        }
    }
    
    private func selSong(with songNo: String) -> VLRoomSelSongModel? {
        var song: VLRoomSelSongModel?
        self.selSongArray?.forEach { obj in
            if obj.songNo == songNo {
                song = obj
                return
            }
        }
        
        return song
    }
    
}

//加载RTC模块
extension CantataMainViewController: AgoraRtcEngineDelegate {
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
        
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, receiveStreamMessageFromUid uid: UInt, streamId: Int, data: Data) {
        
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, reportAudioVolumeIndicationOfSpeakers speakers: [AgoraRtcAudioVolumeInfo], totalVolume: Int) {
        
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, networkQuality uid: UInt, txQuality: AgoraNetworkQuality, rxQuality: AgoraNetworkQuality) {
        if uid == Int(VLUserCenter.user.id ?? "") ?? 0 {
            if txQuality == .excellent || txQuality == .good {
                // Good quality
                self.topView.setNetworkQuality(0)
            } else if txQuality == .poor || txQuality == .bad {
                // Bad quality
                self.topView.setNetworkQuality(1)
            } else if txQuality == .vBad || txQuality == .down {
                // Barely usable
                self.topView.setNetworkQuality(2)
            } else {
                // Unknown or detecting
                self.topView.setNetworkQuality(3)
            }
        }
    }
    
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, tokenPrivilegeWillExpire token: String) {
        NetworkManager.shared.generateToken(channelName: roomModel?.roomNo ?? "", uid: VLUserCenter.user.id, tokenType: .token006, type: .rtc) {[weak self] token  in
            guard let self = self, let token = token else {return}
            self.RtcKit.renewToken(token)
        }
        
        //mcc renew token
        NetworkManager.shared.generateToken(channelName: roomModel?.roomNo ?? "", uid: VLUserCenter.user.id, tokenType: .token006, type: .rtm) {[weak self] token  in
            guard let self = self, let token = token else {return}
            // cp todo
        }
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, localAudioStats stats: AgoraRtcLocalAudioStats) {
        
    }
}

extension CantataMainViewController: DHCGameDelegate {
    public func didGameEventChanged(with event: DHCGameEvent) {
        
    }
}

//ktvapi的event handler
extension CantataMainViewController: KTVApiEventHandlerDelegate {
    public func onSingingScoreResult(score: Float) {
        
    }
    
    public func onSingerRoleChanged(oldRole: KTVSingRole, newRole: KTVSingRole) {
        
    }
    
    public func onChorusChannelAudioVolumeIndication(speakers: [AgoraRtcAudioVolumeInfo], totalVolume: Int) {
        
    }
    
    public func onMusicPlayerStateChanged(state: AgoraMediaPlayerState, error: AgoraMediaPlayerError, isLocal: Bool) {
        
    }
    
    public func onTokenPrivilegeWillExpire() {
        
    }
    
    
}

extension CantataMainViewController: ChorusMicViewDelegate {
    func didChorusMicViewClicked(with index: Int) {
        print("\(index)clicked")
        /*
         1.判断当前麦位信息
         2.判断是否是自己
         3.判断是上下卖(房主可以强制用户下麦，用户可以自己上下卖，观众可以上下卖)
         */
        let realIndex = index - 1000
        if let seatModel = self.seatsArray?[realIndex] {//麦位不为空 要么房主 要么卖上观众
            if isRoomOwner {
                //如果不是自己就强制下麦，自己点击无反应
                if realIndex != 0 && seatModel.userNo?.count ?? 0 > 0{
                    popDropLineView(with: seatModel)
                }
            } else {
                //如果是自己就下麦
                if seatModel.userNo == VLUserCenter.user.id {
                    popDropLineView(with: seatModel)
                } else {
                    //如果自己不在麦位上就上麦 自己在麦上就不反应
                    if !checkIsOnMicSeat() {
                        enterSeatWithModel(realIndex) { err in
                            
                        }
                    }
                }
            }
                
        } else {
            if !isRoomOwner {
                //如果自己不在麦位上就上麦 自己在麦上就不反应
                if let seatModel = getSeatModel(with: realIndex) {
                    if !checkIsOnMicSeat() {
                        enterSeatWithModel(realIndex) { err in
                            
                        }
                    }
                }
            }
        }
    }
}

//头部视图代理
extension CantataMainViewController {
    public func onVLKTVTopView(_ view: VLKTVTopView, closeBtnTapped sender: Any) {
        let title = VLUserCenter.user.ifMaster ? "ktv_disband_room".toSceneLocalization(bundleName: "DHCResource") : "ktv_exit_room".toSceneLocalization(bundleName: "DHCResource")
        let message = VLUserCenter.user.ifMaster ? "ktv_confirm_disband_room".toSceneLocalization(bundleName: "DHCResource") : "ktv_confirm_exit_room".toSceneLocalization(bundleName: "DHCResource")
        let array = ["ktv_cancel".toSceneLocalization(bundleName: "DHCResource"), "ktv_confirm".toSceneLocalization(bundleName: "DHCResource")]
        VLAlert.shared().show(withFrame: UIScreen.main.bounds, title: title, message: message, placeHolder: "", type: .ALERTYPENORMAL, buttonTitles: array) {[weak self] (flag, text) in
            if flag {
                self?.leaveRoom()
            }
            VLAlert.shared().dismiss()
        }
    }

    public func onVLKTVTopView(_ view: VLKTVTopView, moreBtnTapped sender: Any) {
        let dialog = AUiMoreDialog(frame: UIScreen.main.bounds)

        if let keyWindow = UIApplication.shared.keyWindow {
            keyWindow.addSubview(dialog)
            keyWindow.bringSubviewToFront(dialog)
            dialog.show()
        }
    }
}

//底部视图代理
extension CantataMainViewController: VLBottomViewDelegate {
    public func didBottomChooseSong() {
        AUICommonDialog.show(contentView: jukeBoxView, theme: AUICommonDialogTheme())
    }
    
    public func didBottomViewAudioStateChangeTo(enable: Bool) {
        //开关麦位
        if self.isNowMicMuted {
            AgoraEntAuthorizedManager.checkAudioAuthorized(parent: self) { flag in
                
            }
        }
        self.isNowMicMuted = !self.isNowMicMuted;
        AppContext.ktvServiceImp()?.updateSeatAudioMuteStatus(with: self.isNowMicMuted, completion: { err in
            
        })
    }

}

//主要处理各种数组
extension CantataMainViewController {
    
}

//弹窗相关
extension CantataMainViewController {
    private func popDropLineView(with seatModel: VLRoomSeatModel) {
        LSTPopView.popDropLineView(withParentView: self.view, with: seatModel, withDelegate: self)
    }
    
    public func onVLDropOnLineView(_ view: VLDropOnLineView, action seatModel: VLRoomSeatModel?) {
        guard let seatModel = seatModel else {return}
        leaveSeat(with: seatModel) { err in
            LSTPopView.getPopView(withCustomView: view).dismiss()
        }
    }

}

extension CantataMainViewController: AUIJukeBoxViewDelegate {
    
    //这个用户应该是无感知的
    public func cleanSearchText(view: AUIJukeBoxView) {
        self.searchMusicList = nil
        self.searchKeyWord = nil
    }
    
    public func search(view: AUIJukeBoxView, text: String, completion: @escaping ([AUIJukeBoxItemDataProtocol]?)->()) {
        self.searchKeyWord = text
        self.searchMusic(keyword: text, page: 1, pageSize: kListPageCount, completion: {[weak self] error, list in
            guard let self = self else {return}
            if let err = error {
                AUIToast.show(text:err.localizedDescription)
                return
            }
            self.searchMusicList = list ?? []
            completion(list)
        })
    }
    
    public func onSegmentedChanged(view: AUIJukeBoxView, segmentIndex: Int) -> Bool {
        return false
    }
    
    public func onTabsDidChanged(view: AUIJukeBoxView, tabIndex: Int) -> Bool {
        return false
    }
    
    public func onSelectSong(view: AUIJukeBoxView, tabIndex: Int, index: Int) {
        guard let model = searchMusicList == nil ? musicListMap[tabIndex]?[index] : searchMusicList?[index] else {return}
        let inputModel = KTVChooseSongInputModel()
        inputModel.songNo = model.songCode
        inputModel.songName = model.name
        inputModel.singer = model.singer
        AppContext.ktvServiceImp()?.chooseSong(with: inputModel, completion: { err in
            if err == nil {
                let addModel: AUIChooseMusicModel = AUIChooseMusicModel()
                addModel.songCode = model.songCode
                addModel.name = model.name
                addModel.singer = model.singer
                addModel.poster = model.poster
                addModel.duration = model.duration
                self.addedMusicList.append(addModel)
                self._notifySongDidAdded(song: addModel)
                self.jukeBoxView.addedMusicTableView.reloadData()
                self.jukeBoxView.allMusicTableView.reloadData()
            }
        })
    }
    
    public func onRemoveSong(view: AUIJukeBoxView, index: Int) {
        let song = self.addedMusicList[index]
        let removeModel = KTVRemoveSongInputModel()
        removeModel.songNo = song.songCode
        
        guard let selSongArray = self.selSongArray else {return}
        for i in selSongArray {
            if i.songNo == song.songCode {
                removeModel.objectId = i.objectId
                AppContext.ktvServiceImp()?.removeSong(with: removeModel, completion: { err in
                    if err == nil {
                        self.addedMusicList.remove(at: index)
                        self._notifySongDidRemove(song: song)
                        self.jukeBoxView.addedMusicTableView.reloadData()
                        self.jukeBoxView.allMusicTableView.reloadData()
                    }
                })
                return
            }
        }
    }
    
    public func onNextSong(view: AUIJukeBoxView, index: Int) {
        AUIAlertView.theme_defaultAlert()
            .isShowCloseButton(isShow: false)
            .title(title: aui_localized("switchToNextSong"))
            .rightButton(title: "确认")
            .rightButtonTapClosure(onTap: {[weak self] text in
                guard let self = self else { return }
                self.onRemoveSong(view: view, index: index)
            })
            .leftButton(title: "取消")
            .show()
    }
    
    public func onPinSong(view: AUIJukeBoxView, index: Int) {
        let song = self.addedMusicList[index]
        self.pinSong(songCode: song.songCode, completion: { error in
            guard let err = error else {return}
            AUIToast.show(text:err.localizedDescription)
        })
    }
    
    public func songIsSelected(view: AUIJukeBoxView, songCode: String) -> Bool {
        return addedMusicSet.contains(songCode)
    }
    
    public func pingEnable(view: AUIJukeBoxView, songCode: String) -> Bool {
        return pinEnableSet.contains(songCode)
    }
    
    public func deleteEnable(view: AUIJukeBoxView, songCode: String) -> Bool {
        return deleteEnableSet.contains(songCode)
    }
    
    public func getSearchMusicList(view: AUIJukeBoxView) -> [AUIJukeBoxItemDataProtocol]? {
        return self.searchMusicList
    }
    
    public func getMusicList(view: AUIJukeBoxView, tabIndex: Int) -> [AUIJukeBoxItemDataProtocol] {
        return self.musicListMap[tabIndex] ?? []
    }
    
    public func getSelectedSongList(view: AUIJukeBoxView) -> [AUIJukeBoxItemSelectedDataProtocol] {
        return self.addedMusicList
    }
    
    public func onRefreshMusicList(view: AUIJukeBoxView, tabIndex: Int, completion: @escaping ([AUIJukeBoxItemDataProtocol]?)->()) {
        let idx = tabIndex
        aui_info("onRefreshMusicList tabIndex: \(idx)", tag: "AUIJukeBoxViewBinder")
        self.getMusicList(chartId: kChartIds[idx],
                                           page: 1,
                                           pageSize: kListPageCount,
                                           completion: {[weak self] error, list in
            guard let self = self else {return}
            if let err = error {
                AUIToast.show(text:err.localizedDescription)
                return
            }
            self.musicListMap[idx] = list ?? []
            completion(list)
        })
    }
    
    public func onLoadMoreMusicList(view: AUIJukeBoxView, tabIndex: Int, completion: @escaping ([AUIJukeBoxItemDataProtocol]?)->()) {
        
        //如果searchMusicList为空，表示为id搜索，否则为关键字搜索
        if let searchList = self.searchMusicList, let keyword = self.searchKeyWord {
            let page = 1 + searchList.count / kListPageCount
            self.searchMusic(keyword: keyword, page: page, pageSize: kListPageCount, completion: {[weak self] error, list in
                guard let self = self else {return}
                if let err = error {
                    AUIToast.show(text:err.localizedDescription)
                    return
                }
                self.searchMusicList? += list ?? []
                completion(list)
            })
            return
        }

        let idx = tabIndex
        let musicListCount = musicListMap[idx]?.count ?? 0
        let page = 1 + musicListCount / kListPageCount
        if musicListCount % kListPageCount > 0 {
            //no more data
            completion(nil)
            return
        }
        aui_info("onLoadMoreMusicList tabIndex: \(idx) page: \(page)", tag: "AUIJukeBoxViewBinder")
        self.getMusicList(chartId: kChartIds[idx],
                                           page: page,
                                           pageSize: kListPageCount,
                                           completion: {[weak self] error, list in
            guard let self = self else {return}
            if let err = error {
                AUIToast.show(text:err.localizedDescription)
                return
            }
            if let list = list {
                let musicList = self.musicListMap[idx] ?? []
                self.musicListMap[idx] = musicList + list
            }
            completion(list)
        })
    }
    
    public func onRefreshAddedMusicList(view: AUIJukeBoxView, completion: @escaping ([AUIJukeBoxItemSelectedDataProtocol]?) -> ()) {
        aui_info("onRefreshAddedMusicList", tag: "AUIJukeBoxViewBinder")
        //暂时不需要上拉刷新
    }

    public func pinSong(songCode: String, completion: AUICallback?) {
        aui_info("pinSong: \(songCode)", tag: "AUIMusicServiceImpl")
        var model = VLRoomSelSongModel()
        guard let selSongArray = self.selSongArray else {return}
        for i in selSongArray {
            if i.songNo == songCode {
                let model =  KTVMakeSongTopInputModel()
                model.objectId = i.objectId
                model.songNo = i.songNo
                AppContext.ktvServiceImp()?.pinSong(with: model, completion: { err in
                    
                })
                break
            }
        }
    }
    
    public func searchMusic(keyword: String,
                            page: Int,
                            pageSize: Int,
                            completion: @escaping AUIMusicListCompletion) {
        aui_info("searchMusic with keyword: \(keyword)", tag: "AUIMusicServiceImpl")
        let jsonOption = "{\"needLyric\":true,\"pitchType\":1}"
        self.ktvApi.searchMusic(keyword: keyword,
                                page: page,
                                pageSize: pageSize,
                                jsonOption: jsonOption) { requestId, status, collection in
            aui_info("searchMusic with keyword: \(keyword) status: \(status.rawValue) count: \(collection.count)", tag: "AUIMusicServiceImpl")
            guard status == .OK else {
                //TODO:
                DispatchQueue.main.async {
                    completion(nil, nil)
                }
                return
            }
            
            var musicList: [AUIMusicModel] = []
            collection.musicList.forEach { music in
                let model = AUIMusicModel()
                model.songCode = "\(music.songCode)"
                model.name = music.name
                model.singer = music.singer
                model.poster = music.poster
                model.releaseTime = music.releaseTime
                model.duration = music.durationS
                musicList.append(model)
            }
            
            DispatchQueue.main.async {
                completion(nil, musicList)
            }
        }
    }

    public func getMusicList(chartId: Int,
                             page: Int,
                             pageSize: Int,
                             completion: @escaping AUIMusicListCompletion) {
        aui_info("getMusicList with chartId: \(chartId)", tag: "AUIMusicServiceImpl")
        let jsonOption = "{\"needLyric\":true,\"pitchType\":1}"
        self.ktvApi.searchMusic(musicChartId: chartId,
                                page: page,
                                pageSize: pageSize,
                                jsonOption: jsonOption) { requestId, status, collection in
            aui_info("getMusicList with chartId: \(chartId) status: \(status.rawValue) count: \(collection.count)", tag: "AUIMusicServiceImpl")
            guard status == .OK else {
                //TODO:
                DispatchQueue.main.async {
                    completion(nil, nil)
                }
                return
            }
            
            var musicList: [AUIMusicModel] = []
            collection.musicList.forEach { music in
                let model = AUIMusicModel()
                model.songCode = "\(music.songCode)"
                model.name = music.name
                model.singer = music.singer
                model.poster = music.poster
                model.releaseTime = music.releaseTime
                model.duration = music.durationS
                musicList.append(model)
            }
            
            DispatchQueue.main.async {
                completion(nil, musicList)
            }
        }
    }
    
    //将选择歌曲的模型转换为UIKit的模型 进行数据的传输显示
    public func getAllChooseSongList(completion: AUIChooseSongListCompletion?) {
        aui_info("getAllChooseSongList", tag: "AUIMusicServiceImpl")
        
        var songList = [AUIChooseMusicModel]()
        guard let selSongArray = self.selSongArray else {return}
        for i in selSongArray {
            let model = AUIChooseMusicModel()
            model.songCode = i.songNo ?? ""
            model.createAt = i.createAt
            model.singer = i.singer ?? ""
            model.poster = i.imageUrl ?? ""
            model.name = i.songName ?? ""
            model.createAt = i.createAt
            model.pinAt = i.pinAt
            model.status =  i.status == .idle ? 0 : 1
            let owner = AUIUserThumbnailInfo()
            owner.userId = i.userNo ?? ""
            owner.userName = i.name ?? ""
            model.owner = owner
            songList.append(model)
        }
        
        self.chooseSongList = songList
        completion?(nil, self.chooseSongList)
//        self.rtmManager.getMetadata(channelName: self.channelName) { error, map in
//            aui_info("getAllChooseSongList error: \(error?.localizedDescription ?? "success")", tag: "AUIMusicServiceImpl")
//            if let error = error {
//                //TODO: error
//                completion?(error, nil)
//                return
//            }
//            let kChooseSongKey = "song"
//            guard let jsonStr = map?[kChooseSongKey] else {
//                //TODO: error
//                completion?(nil, nil)
//                return
//            }
//
//            self.chooseSongList = NSArray.yy_modelArray(with: AUIChooseMusicModel.self, json: jsonStr) as? [AUIChooseMusicModel] ?? []
//            completion?(nil, self.chooseSongList)
//        }
    }
    
    public func _notifySongDidAdded(song: AUIChooseMusicModel) {
        addedMusicSet.add(song.songCode)
        if isRoomOwner ?? false == true {
            deleteEnableSet.add(song.songCode)
            pinEnableSet.add(song.songCode)
        } else if song.owner?.userId == VLUserCenter.user.id {
            deleteEnableSet.add(song.songCode)
        }
    }
    public func _notifySongDidRemove(song: AUIChooseMusicModel) {
        addedMusicSet.remove(song.songCode)
        deleteEnableSet.remove(song.songCode)
        pinEnableSet.remove(song.songCode)
    }
}

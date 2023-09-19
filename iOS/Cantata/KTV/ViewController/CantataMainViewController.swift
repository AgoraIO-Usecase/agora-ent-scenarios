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

@objcMembers
public class CantataMainViewController: BaseViewController{

    private var RtcKit: AgoraRtcEngineKit!
    
    @objc public var roomModel: VLRoomListModel?
    @objc public var selSongArray: [VLRoomSelSongModel]? {
        didSet {
            if let newSongs = self.selSongArray {
                if let oldSongs = oldValue {
                    let oldFirst = oldSongs.first
                    let newFirst = newSongs.first
                    if oldFirst?.songNo != newFirst?.songNo{
                        loadAndPlaySong()
                    }
                } else {
                    if newSongs.count > 0 {
                        loadAndPlaySong()
                    }
                }
            }
        }
    }
    @objc public var seatsArray: [VLRoomSeatModel]?
    
    var ktvApi: KTVApiImpl!
    private var singerRole: KTVSingRole = .audience
    public var isRoomOwner: Bool = false
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
    private var settingView: VLKTVSettingView!
    public var searchKeyWord: String?
    private var loadMusicCallBack:((Bool, String)->Void)?
    private var connection: AgoraRtcConnection?
    //歌曲列表相关
    public lazy var jukeBoxView: AUIJukeBoxView = AUIJukeBoxView()
    //歌曲查询列表
    public var searchMusicList: [AUIMusicModel]?
    //点歌列表
    public var musicListMap: [Int: [AUIMusicModel]] = [:]
    //已点列表
    public var addedMusicList: [AUIChooseMusicModel] = [] {
        didSet {
            if let topSong = addedMusicList.first,
                topSong.userId == selSongArray?.first?.userNo,
               !topSong.isPlaying {
                var top = topSong
                top.status = AUIPlayStatus.playing.rawValue
                self.addedMusicList[0] = top
                self.jukeBoxView.addedMusicTableView.reloadData()
            }
            self.jukeBoxView.selectedSongCount = addedMusicList.count
        }
    }
    public var chooseSongList: [AUIChooseMusicModel] = []
    /// 已点歌曲key的map
    public var addedMusicSet: NSMutableSet = NSMutableSet()
    
    /// 可置顶歌曲key
    public var pinEnableSet: NSMutableSet = NSMutableSet()
    
    /// 可删除歌曲key
    public var deleteEnableSet: NSMutableSet = NSMutableSet()
    
    private var playoutVolume: Int = 0 {
        didSet {
            ktvApi.getMusicPlayer()?.adjustPlayoutVolume(Int32(playoutVolume))
            ktvApi.getMusicPlayer()?.adjustPublishSignalVolume(Int32(playoutVolume))
           // settingView.setAccValue(Float(playoutVolume) /  100.0)
        }
    }
    
    
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
        refreshChoosedSongList()
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
        lrcControlView.controlState = .noSong
        lrcControlView.backgroundColor = .clear
        mainBgView.addSubview(lrcControlView)
        
        //麦位
        chorusMicView = ChorusMicView(frame: CGRect(x: 0, y: 140, width: ScreenWidth, height: 320), topMicCount: 8)
        mainBgView.addSubview(chorusMicView)
        chorusMicView.delegate = self
        chorusMicView.backgroundColor = .clear
        chorusMicView.isHidden = true
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
        RtcKit.delegate = self
  
        isNowMicMuted = checkAudicMicIsMuted()
        isBrodCaster = checkIsOnMicSeat()
        
        setupContentInspectConfig()
        
        let config = AgoraDataStreamConfig.init()
        config.ordered = false
        config.syncWithAudio = false
        RtcKit.createDataStream(&rtcDataStreamId, config: config)
        RtcKit.setClientRole(isNowMicMuted ? .broadcaster : .audience)
        
        loadKtvApi()
        
        guard let roomNo = roomModel?.roomNo else {return}
        var realChannelId: String = roomNo
        realChannelId = "\(realChannelId)_ad"
    
        let connection = AgoraRtcConnection(channelId: realChannelId, localUid: Int(VLUserCenter.user.id) ?? 0)
        self.connection = connection
        let ret = RtcKit.joinChannelEx(byToken: VLUserCenter.user.audienceChannelToken, connection: connection, delegate: self, mediaOptions: updateChannelMediaOption())
        
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
        let rtcToken = VLUserCenter.user.agoraRTCToken
        guard let roomNo = roomModel?.roomNo else {return}
        let apiConfig = KTVApiConfig(appId: AppContext.shared.appId, rtmToken: VLUserCenter.user.agoraRTMToken, engine: RtcKit, channelName: roomNo, localUid: Int(VLUserCenter.user.id) ?? 0, chorusChannelName: "\(roomNo)", chorusChannelToken: rtcToken, type: .cantata, maxCacheSize: 10, musicType: .mcc, isDebugMode: false)
        let giantConfig = GiantChorusConfiguration(audienceChannelToken: VLUserCenter.user.audienceChannelToken, musicStreamUid: 2023, musicChannelToken: exChannelToken, topN: 6)
        self.ktvApi = KTVApiImpl(config: apiConfig, giantConfig: giantConfig)
        self.ktvApi.renewInnerDataStreamId()
        self.ktvApi.setLrcView(view: lrcControlView)
        self.ktvApi.setMicStatus(isOnMicOpen: !self.isNowMicMuted)
        self.ktvApi.addEventHandler(ktvApiEventHandler: self)
    }
    
    private func getMusicChannelToken() -> String{
        let apiManager = ApiManager()
        let token = apiManager.fetchCloudToken()
        return token
    }
    
    private func loadAndPlaySong() {
        
        chorusMicView.isHidden = false
        
        guard let model = self.selSongArray?.first else {return}
        markSong(with: model)
        
        let role = getUserSingRole()
        
        if isRoomOwner {
            if VLUserCenter.user.id == model.userNo {
                self.lrcControlView.controlState = .ownerSing
            } else {
                self.lrcControlView.controlState = .ownerChorus
            }
        } else {
            if VLUserCenter.user.id == model.userNo {
                self.lrcControlView.controlState = .chorusSing
            } else {
                self.lrcControlView.controlState = .joinChorus
            }
        }
        
        //获取合唱用户
        guard let seatsArray = self.seatsArray else {return}
        let count = getChorusNum(with: seatsArray)
        lrcControlView.setChoursNum(with: count)
        
        let config = KTVSongConfiguration()
        config.autoPlay = (role == .audience || role == .coSinger) ? false : true
        config.mode = (role == .audience || role == .coSinger) == true ? .loadLrcOnly : .loadMusicAndLrc
        config.mainSingerUid = Int(model.userNo ?? "") ?? 0
        config.songIdentifier = model.songNo ?? ""
        
        self.loadMusicCallBack = {[weak self] flag, songCode in
            
        }
        
        self.ktvApi.loadMusic(songCode: Int(model.songNo ?? "") ?? 0, config: config, onMusicLoadStateListener: self)
        self.ktvApi.switchSingerRole2(newRole: role) { state, reason in
            if state != .success {
                print("switch failed:\(role)----\(state.rawValue)")
            }
        }
    }
    
    private func checkChorus() {
        guard let conn = self.connection else {return}
//        if self.RtcKit.getConnectionStateEx(conn) != .connected {
//            VLToast.toast("加入合唱失败，reson:连接已断开")
//            return
//        }
        
        let model = self.getCurrentUserMicSeat()
        //没有上麦需要先上麦
        if model == nil {
            guard let seatArray = self.seatsArray else {return}
            
            for i in 1..<seatArray.count {
                let seat = seatArray[i]
                let rtcUid = seat.rtcUid ?? ""
                if rtcUid == "" {
                // cp todo    KTVLogError("before enterSeat error")
                    self.lrcControlView.controlState = .beforeJoinChorus
                    self.enterSeat(withIndex: i) {[weak self] error in
                        guard let self = self else {return}
                        if let error = error {
                            // cp todo  KTVLogError("enterSeat error: \(error.localizedDescription)")
                            self.lrcControlView.controlState = .joinChorus
                            return
                        }
                        //加入合唱
                        self.joinChorus()
                    }
                    
                    return
                }
            }
            
            VLToast.toast("麦位已满，请在他人下麦后重试")
            return
        }
        joinChorus()
    }
    
    private func joinChorus() {
        
        guard let selSongArray = self.selSongArray, let model = selSongArray.first as? VLRoomSelSongModel else {
            return
        }

        let role: KTVSingRole = .coSinger
        let songConfig = KTVSongConfiguration()
        songConfig.autoPlay = false
        songConfig.mode = .loadMusicOnly
        songConfig.mainSingerUid = Int(model.userNo ?? "0") ?? 0
        songConfig.songIdentifier = model.songNo ?? ""

        self.loadMusicCallBack = { [weak self] isSuccess, songCode in
            guard let weakSelf = self else {
                return
            }

            print("before switch role, load music success")
            
            weakSelf.ktvApi.switchSingerRole2(newRole: role) { state, reason in
                if state == .fail && reason != .noPermission {
                    weakSelf.lrcControlView.controlState = .joinChorus
                    VLToast.toast(String(format: "join chorus fail: %ld", reason.rawValue))
                // cp todo    KTVLogInfo("join chorus fail")
                    // TODO: error toast?
                    return
                }
                
                weakSelf.lrcControlView.controlState = .chorusSing
                
                weakSelf.isNowMicMuted = role == .audience
                
                let inputModel = KTVJoinChorusInputModel()
                inputModel.isChorus = true
                inputModel.songNo = model.songNo
                AppContext.ktvServiceImp()?.joinChorus(with: inputModel) { error in
                    // completion block
                }

                // 开麦
                AppContext.ktvServiceImp()?.updateSeatAudioMuteStatus(with: false) { error in
                    // completion block
                }
            }
        }

        if let songCode = Int(model.songNo ?? "0") {
        // cp todo    KTVLogInfo("before songCode:%li", songCode)
            self.ktvApi.loadMusic(songCode: songCode, config: songConfig, onMusicLoadStateListener: self)
        }
    }
    
    func enterSeat(withIndex index: Int, completion: @escaping (Error?) -> Void) {
        let inputModel = KTVOnSeatInputModel()
        inputModel.seatIndex = UInt(index)
        
        AppContext.ktvServiceImp()?.enterSeat(with: inputModel) { error in
            completion(error)
        }
        
        self.checkEnterSeatAudioAuthorized()
    }
    
    private func leaveChorus() {
        //先下麦然后切换角色
        AppContext.ktvServiceImp()?.coSingerLeaveChorus { [weak self] error in
            guard let weakSelf = self else { return }
            
            weakSelf.stopPlaySong()
            weakSelf.isNowMicMuted = true

            AppContext.ktvServiceImp()?.updateSeatAudioMuteStatus(with: true) { error in
                // completion block
            }
        }
    }
    
    private func getUserSingRole() -> KTVSingRole {
        guard let songModel = selSongArray?.first else {
            return .audience
        }
        
        let currentSongIsJoinSing = getCurrentUserMicSeat()?.chorusSongCode == songModel.chorusSongId()
        let currentSongIsSongOwner = songModel.isSongOwner()
        
        guard let seatArray = self.seatsArray else {return .audience}
        let currentSongIsChorus = getChorusNum(with: seatArray) > 0
        
        if currentSongIsSongOwner {
            return .leadSinger
        } else if currentSongIsJoinSing {
            return .coSinger
        } else {
            return .audience
        }
    }
    
    func getOnMicUserCount() -> Int {
        var num = 0
        if let seatsArray = self.seatsArray {
            for model in seatsArray {
                if let rtcUid = model.rtcUid {
                    num += 1
                }
            }
        }
        return num
    }
    
    private func getChorusNum(with seatArray: [VLRoomSeatModel]) -> Int {
        var chorusNum = 0
        guard let topSong = selSongArray?.first else {
            return chorusNum
        }
        
        for seat in seatArray {
            if seat.chorusSongCode == topSong.chorusSongId() {
                chorusNum += 1
            }
        }
        
        return chorusNum
    }
    
    private func markSong(with model: VLRoomSelSongModel) {
        if model.status == .playing {
            return;
        }
        AppContext.ktvServiceImp()?.markSongDidPlay(with: model, completion: { _ in
            
        })
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
        option.clientRoleType = .audience
        option.publishMicrophoneTrack = false
        option.publishCustomAudioTrack = false
        option.channelProfile = .liveBroadcasting
        option.autoSubscribeAudio = true
        option.enableAudioRecordingOrPlayout = true
        return option
    }
    
    private func leaveRtcChannel() {
        RtcKit.leaveChannel()
    }
}

//ktvapi模块
extension CantataMainViewController: IMusicLoadStateListener {
    public func onMusicLoadFail(songCode: Int, reason: KTVLoadSongFailReason) {
        if let loadMusicCallBack = self.loadMusicCallBack {
            loadMusicCallBack(false, "\(songCode)")
            self.loadMusicCallBack = nil
        }
    }
    
    public func onMusicLoadProgress(songCode: Int, percent: Int, status: AgoraMusicContentCenterPreloadStatus, msg: String?, lyricUrl: String?) {
        
    }
    
    public func onMusicLoadSuccess(songCode: Int, lyricUrl: String) {
        if let loadMusicCallBack = self.loadMusicCallBack {
            loadMusicCallBack(true, "\(songCode)")
            self.loadMusicCallBack = nil
        }
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
                guard let seatsArray = self.seatsArray else {return}
                let count = self.getChorusNum(with: seatsArray)
                self.lrcControlView.setChoursNum(with: count == 0 ? 1 : count)
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
    
    func refreshChoosedSongList() {
        AppContext.ktvServiceImp()?.getChoosedSongsList {[weak self] (error, songArray) in
            guard let self = self else {return}
            if let error = error {
                return
            }

            self.selSongArray = songArray
        }
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
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
        
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
        if event == .join {
            checkChorus()
        }
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


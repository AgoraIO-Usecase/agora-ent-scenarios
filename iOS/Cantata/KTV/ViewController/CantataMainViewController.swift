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

class CantataMainViewController: BaseViewController{

    private var RtcKit: AgoraRtcEngineKit!
    
    @objc public var roomModel: VLRoomListModel?
    @objc public var selSongArray: [VLRoomSelSongModel]? {
        didSet {
            if let newSongs = self.selSongArray, let controlView = lrcControlView, let chorusView = chorusMicView {
                
                //需要更新点歌台歌曲列表
                updateSongView(with: newSongs)
                
                if newSongs.count == 0 {
                    controlView.controlState = .noSong
                    chorusView.isHidden = true
                    return
                }
                
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
    @objc public var seatsArray: [VLRoomSeatModel]? {
        didSet {
            let seatModel = getCurrentUserMicSeat()
            self.isNowMicMuted = seatModel?.isAudioMuted == 0 ? false : true
        }
    }
    
    var ktvApi: KTVApiImpl!
    public var singerRole: KTVSingRole = .audience
    public var isRoomOwner: Bool = false
    private var isEarOn: Bool = false
    
    private var isEnterSeatNotFirst: Bool = false
    private var rtcDataStreamId: Int = 0
    private var isOnMicSeat: Bool = false
    private var chorusMicView: ChorusMicView!
    private var topView: VLKTVTopView!
    private var botView: VLBottomView!
    private var lrcControlView: DHCLRCControl!
    private var isBrodCaster: Bool = false
    private var settingView: VLKTVSettingView?
    public var searchKeyWord: String?
    private var loadMusicCallBack:((Bool, String)->Void)?
    private var connection: AgoraRtcConnection?
    //沉浸模式
    private var isIMMode: Int = 0
    
    private var isNowMicMuted: Bool = false {
        didSet {
            guard let _ = self.ktvApi, let _ = self.RtcKit, let _ = self.botView else {return}
            if oldValue != isNowMicMuted {
                ktvApi.setMicStatus(isOnMicOpen: !isNowMicMuted)
                RtcKit.adjustRecordingSignalVolume(isNowMicMuted ? 0 : 100)
                botView.updateMicState(!isNowMicMuted)
            }
        }
    }
    
    let isIPhonex: Bool = {
        if #available(iOS 11.0, *),
            let window = UIApplication.shared.delegate?.window,
            window?.safeAreaInsets.bottom ?? 0 > 0 {
            return true
        }
        return false
    }()
    
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
                let isSongOwner = VLUserCenter.user.id == topSong.userId
                var top = topSong
                top.status = AUIPlayStatus.playing.rawValue
                top.switchable = isSongOwner
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
    
    //专门记录点歌成功前的set
    public var beforeAddSet: NSMutableSet = NSMutableSet()
    
    /// 可删除歌曲key
    public var deleteEnableSet: NSMutableSet = NSMutableSet()
    private var cosingerDegree: Int = 0
    private var isPause: Bool = false
    private var earValue: Double = 0
    private var soundValue: Int = 0
    private var playoutVolume: Int = 0 {
        didSet {
            self.ktvApi.getMusicPlayer()?.adjustPlayoutVolume(Int32(playoutVolume))
            self.ktvApi.getMusicPlayer()?.adjustPublishSignalVolume(Int32(playoutVolume))
            self.settingView?.setAccValue(Float(playoutVolume) / 100.0)
        }
    }
    public override func viewDidLoad() {
        super.viewDidLoad()
        
        isRoomOwner = VLUserCenter.user.ifMaster
        if isRoomOwner {
            guard let roomNo = roomModel?.roomNo else {return}
            ApiManager.shared.fetchStartCloud(mainChannel: roomNo, cloudRtcUid: 232425)
        }
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
        AUICommonDialog.hidden()
        leaveRtcChannel()
        UIApplication.shared.isIdleTimerDisabled = false
    }

    public override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        if isRoomOwner {
            ApiManager.shared.fetchStopCloud()
        }
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
        
        let kStatusBarHeight: CGFloat = isIPhonex ? 44.0 : 20

        //头部布局
        topView = VLKTVTopView(frame: CGRect(x: 0, y: Int(kStatusBarHeight), width: Int(ScreenWidth), height: 60), with: self)
        view.addSubview(topView)
        if let model = self.roomModel {
            topView.listModel = model
        }

        let mainY = Int(kStatusBarHeight) + 60
        var bottomSafeAreaInset: CGFloat = 0.0

        if #available(iOS 11.0, *),
            let window = UIApplication.shared.delegate?.window {
            bottomSafeAreaInset = window?.safeAreaInsets.bottom ?? 0
        }
        
        let mainBgView = UIImageView(frame: CGRect(x: 0, y: mainY , width: Int(ScreenWidth), height: Int(self.view.bounds.height) - 60 - mainY - Int(bottomSafeAreaInset)))
        mainBgView.contentMode = .scaleAspectFill
        mainBgView.image = UIImage.sceneImage(name: "dhc_bg", bundleName: "DHCResource")
        mainBgView.isUserInteractionEnabled = true
        view.addSubview(mainBgView)
        
        lrcControlView = DHCLRCControl(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: mainBgView.bounds.size.height))
        lrcControlView.delegate = self
        lrcControlView.lrcDelegate = self
        lrcControlView.controlState = .noSong
        lrcControlView.backgroundColor = .clear
        mainBgView.addSubview(lrcControlView)
        
        //麦位
        chorusMicView = ChorusMicView(frame: CGRect(x: 0, y: mainBgView.bounds.size.height - 350 - 50, width: ScreenWidth, height: 340), topMicCount: 8)
        mainBgView.addSubview(chorusMicView)
        chorusMicView.delegate = self
        chorusMicView.backgroundColor = .clear
        chorusMicView.isHidden = true
        
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
        RtcKit.enableAudio()
        RtcKit.setEnableSpeakerphone(true)
        RtcKit.delegate = self
  
      //  isNowMicMuted = checkAudicMicIsMuted()
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
        
//        let model = getCurrentUserMicSeat()
//        if let currentModel = model {
//            checkEnterSeatAudioAuthorized()
//            isNowMicMuted = model?.isAudioMuted == 1
//        } else {
//            isNowMicMuted = true
//        }
//
        botView.updateMicState(!isNowMicMuted)
    }
    
    private func loadKtvApi() {
        let exChannelToken = VLUserCenter.user.agoraPlayerRTCToken
        let rtcToken = VLUserCenter.user.agoraRTCToken
        guard let roomNo = roomModel?.roomNo else {return}
        let apiConfig = KTVApiConfig(appId: AppContext.shared.appId, rtmToken: VLUserCenter.user.agoraRTMToken, engine: RtcKit, channelName: "\(roomNo)_ad", localUid: Int(VLUserCenter.user.id) ?? 0, chorusChannelName: "\(roomNo)", chorusChannelToken: rtcToken, type: .cantata, maxCacheSize: 10, musicType: .mcc, isDebugMode: false)
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
        self.isIMMode = 0
        guard let model = self.selSongArray?.first else {return}
        markSong(with: model)
        
        let role: KTVSingRole = model.userNo == VLUserCenter.user.id ? .leadSinger : .audience

        let seatModel = getCurrentUserMicSeat()
        if seatModel == nil {
            guard let seatArray = self.seatsArray else {return}
            
            for i in 0..<seatArray.count {
                let seat = seatArray[i]
                let rtcUid = seat.rtcUid ?? ""
                if rtcUid == "" && role == .leadSinger {
                    self.enterSeat(withIndex: i) {[weak self] error in
                        guard let self = self else {return}
                        if let error = error {
                            return
                        }
                    }
                    break
                }
            }
        }
        
        if VLUserCenter.user.id == model.userNo {
            self.lrcControlView.controlState = .ownerSing
            self.checkEnterSeatAudioAuthorized()
        } else {
            self.lrcControlView.controlState = .joinChorus
        }

        //获取合唱用户
        guard let seatsArray = self.seatsArray else {return}
        let count = self.getChorusSingerArray(with: seatsArray).count
        lrcControlView.setChoursNum(with: count)
        lrcControlView.resetStatus()
        self.chorusMicView.seatArray = self.makeChorusArray()

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
    
    public func checkChorus() {
        guard let conn = self.connection else {return}
//        if self.RtcKit.getConnectionStateEx(conn) != .connected {
//            VLToast.toast("加入合唱失败，reson:连接已断开")
//            return
//        }
        
        let model = self.getCurrentUserMicSeat()
        self.lrcControlView.controlState = .beforeJoinChorus
        //没有上麦需要先上麦
        if model == nil {
            guard let seatArray = self.seatsArray else {return}
            
            for i in 1..<seatArray.count {
                let seat = seatArray[i]
                let rtcUid = seat.rtcUid ?? ""
                if rtcUid == "" {
                // cp todo    KTVLogError("before enterSeat error")
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
            if !isSuccess {
                weakSelf.lrcControlView.controlState = .joinChorus
                return
            }

            print("before switch role, load music success")
            
            weakSelf.ktvApi.switchSingerRole2(newRole: role) { state, reason in
                if state == .fail && reason != .noPermission {
                    DispatchQueue.main.async {
                        weakSelf.lrcControlView.controlState = .joinChorus
                    }
                    
                    VLToast.toast(String(format: "join chorus fail: %ld", reason.rawValue))
                // cp todo    KTVLogInfo("join chorus fail")
                    // TODO: error toast?
                    return
                }
                DispatchQueue.main.async {
                    weakSelf.lrcControlView.controlState = .chorusSing
                }
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
    }
    
    private func leaveChorus() {
        //先下麦然后切换角色
        AppContext.ktvServiceImp()?.coSingerLeaveChorus { [weak self] error in
            guard let weakSelf = self else { return }
            
            weakSelf.stopPlaySong()
            weakSelf.isNowMicMuted = true
            weakSelf.lrcControlView.controlState = .joinChorus
            AppContext.ktvServiceImp()?.updateSeatAudioMuteStatus(with: true) { error in
                // completion block
            }
        }
    }
    
    private func leaveSeat() {
        guard let seatModel = getCurrentUserMicSeat() else {return}
        
        leaveSeat(with: seatModel) { err in
            
        }
    }
    
    private func removeSongAndReloadStatus() {
        self.isPause = false
        // 如果是点歌者下麦就切歌
        let chorusArray = self.makeChorusArray()
        let model = self.getCurrentUserMicSeat()
        if model?.userNo == chorusArray.first?.userNo {
            self.stopPlaySong()
            self.lrcControlView.setScore(with: 0)
            self.removeCurrentSong()
        }
        self.ktvApi.switchSingerRole2(newRole: .audience, stateCallBack: { state, reason in
            self.singerRole = .audience
        })
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
        self.ktvApi.removeEventHandler(ktvApiEventHandler: self)
        self.ktvApi.cleanCache()
        self.ktvApi = nil
        self.loadMusicCallBack = nil
        RtcKit.leaveChannel()
    }
    
    private func makeChorusArray() -> [VLRoomSeatModel] {
        //生成麦位展示的数据
        guard let seatArray = self.seatsArray, let topSong = self.selSongArray?.first else {return []}
        var chorusArray = getChorusSingerArray(with: seatArray)
        let topSeat = chorusArray.first
        //如果当前歌曲是房主演唱，房主就是第一位，否则第一位就是演唱者，然后其他人顺延
        if topSong.userNo != topSeat?.userNo {
           // let ownerSeat = seatArray.filter { $0.userNo == topSong.userNo && isStringValid(topSong.userNo) && isStringValid($0.userNo) }
            chorusArray = seatArray.filter { $0.userNo != topSong.userNo}
            //chorusArray.insert(contentsOf: ownerSeat, at: 0)
        }
        //如果chorusArray的个数小于8就补齐8个
        if chorusArray.count < 8 {
            let count = 8 - chorusArray.count
            for i in 0..<count {
                chorusArray.append(VLRoomSeatModel())
            }
        }
        return chorusArray
    }
}

//ktvapi模块
extension CantataMainViewController: IMusicLoadStateListener {
    public func onMusicLoadFail(songCode: Int, reason: KTVLoadSongFailReason) {
        if let loadMusicCallBack = self.loadMusicCallBack {
            loadMusicCallBack(false, "\(songCode)")
            self.loadMusicCallBack = nil
        }
        if self.singerRole == .soloSinger || self.singerRole == .leadSinger {
            self.lrcControlView.updateLoadingView(with: 100)
        }
    }
    
    public func onMusicLoadProgress(songCode: Int, percent: Int, status: AgoraMusicContentCenterPreloadStatus, msg: String?, lyricUrl: String?) {
        if self.singerRole == .soloSinger || self.singerRole == .leadSinger {
            self.lrcControlView.updateLoadingView(with: status == .OK ? 100 : percent)
        }
    }
    
    public func onMusicLoadSuccess(songCode: Int, lyricUrl: String) {
        if let loadMusicCallBack = self.loadMusicCallBack {
            loadMusicCallBack(true, "\(songCode)")
            self.loadMusicCallBack = nil
        }
        if self.singerRole == .soloSinger || self.singerRole == .leadSinger {
            self.lrcControlView.updateLoadingView(with: 100)
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
//            AgoraEntAuthorizedManager.checkMediaAuthorized(parent: self) { granted in
//                guard granted else { return }
                var preSongCode = String()
                guard let model = self.getUserSeatInfo(with: UInt(seatModel.seatIndex)) else {
                    assertionFailure("model == nil")
                    return
                }

                if status == .created {
                    model.reset(with: seatModel)
                } else if( status == .updated) {
                    model.reset(with: seatModel)
                } else if status == .deleted {
                    // 下麦消息
                    // 下麦重置占位模型
                    if let preUserNo = model.userNo, let preCode = model.chorusSongCode {
                        //如果自己不是房主 并且之前是合唱 但是现在不是合唱了 需要主动退出合唱
                        if VLUserCenter.user.id == preUserNo {
                            if self.isStringValid(preCode) == true {
                                self.leaveSeat()
                                self.removeSongAndReloadStatus()
                                self.lrcControlView.controlState = .joinChorus
                            }
                        }
                    }
                    
                    model.reset(with: nil)
                    
                    self.cosingerDegree = 0
                    self.lrcControlView.setScore(with: 0)
                }
                
//                var seatIndex = 0
//                if status == .deleted {
//                    let chorusArray = self.makeChorusArray()
//                    for (index,value) in chorusArray.enumerated() {
//                        if value.userNo == model.userNo {
//                            seatIndex = index
//                        }
//                    }
//                }

                self.seatsArray?[model.seatIndex] = model
                
                if status == .updated && self.singerRole == .audience {//
                    let totalScore = self.seatsArray?.reduce(0, { (result, seatModel) -> Int in
                        return result + seatModel.score
                    })
                    self.lrcControlView.setScore(with: totalScore ?? 0)
                }
                
//                self.isOnMicSeat = self.checkIsOnMicSeat()
//                self.isBrodCaster = self.checkIsOnMicSeat()
//
//                if let currentModel = self.getCurrentUserMicSeat() {
//                    self.isNowMicMuted = currentModel.isAudioMuted == 1
//                } else {
//                    self.isNowMicMuted = true
//                }
                
                if status == .created {
                    
                } else if status == .updated {
                    
                } else if status == .deleted {
                    
                }
                
                self.botView.updateMicState(!self.isNowMicMuted)
//                if status == .deleted {
//                    self.chorusMicView.releaseMic(with: seatIndex)
//                } else {
                    let realSeatArray = self.makeChorusArray()
                    self.chorusMicView.seatArray = realSeatArray
               // }

                //更新RTC身份
                //上麦主播，下麦观众 更新当前观众即可
               // self.updateRTCOption(with: model)
                guard let seatsArray = self.seatsArray else {return}
                let count = self.getChorusSingerArray(with: seatsArray).count
                self.lrcControlView.setChoursNum(with: count)
            }
     //   }

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
                if songArray.count == 0 {
                    self.chorusMicView.isHidden = true
                    self.lrcControlView.controlState = .noSong
                }
                
                //如果删除的是当前歌曲就需要下麦切换成观众
                guard let selSongArray = self.selSongArray, let topSong = selSongArray.first else {return}
                if songInfo.songNo == topSong.songNo && self.singerRole != .audience{
                    guard let leaveModel = getCurrentUserMicSeat() else {return}
                    
                    leaveSeat(with: leaveModel) { err in
                        
                    }
                    
                    self.ktvApi.switchSingerRole2(newRole: .audience, stateCallBack: { state, reason in
                        self.singerRole = .audience
                    })
                
                }
                
                let success = self.removeSelSong(songNo: Int(songInfo.songNo ?? "")!, sync: false)
                if !success {
                    self.selSongArray = songArray
                    // cp todo
                    KTVLog.info(text: "removeSelSongWithSongNo fail, reload it")
                }
                
                self.lrcControlView.resetStatus()

            } else {
                let song = self.selSong(with: songInfo.songNo ?? "")
                // cp todo
                //KTVLog.info(text: "song did updated: \(song.name) status: \(songInfo.status)")
                self.selSongArray = songArray
                
                if status == .created {
                } else if status == .updated {
                    if songInfo.musicEnded == true {
                        DispatchQueue.main.async {
                            self.chorusMicView.isHidden = true
                            self.showResultView()
                        }
                    }
                }
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
    
    private func updateSongView(with selSongArray: [VLRoomSelSongModel]) {
        /*
         1.先删除之前的所有数据
         2.更新数据源
         3.更新UI
         */
        self.addedMusicList.removeAll()
        self.addedMusicSet.removeAllObjects()
        self.deleteEnableSet.removeAllObjects()
        self.pinEnableSet.removeAllObjects()
        for songInfo in selSongArray {
            let addModel: AUIChooseMusicModel = AUIChooseMusicModel()
            addModel.songCode = songInfo.songNo ?? ""
            addModel.name = songInfo.songName ?? ""
            addModel.singer = songInfo.singer ?? ""
            addModel.poster = songInfo.imageUrl ?? ""
            
            let owner = AUIUserThumbnailInfo()
            owner.userId = songInfo.userNo ?? ""
            addModel.owner = owner
            
            self.addedMusicList.append(addModel)
            self._notifySongDidAdded(song: addModel)
        }
        
        self.jukeBoxView.addedMusicTableView.reloadData()
        self.jukeBoxView.allMusicTableView.reloadData()
    }

    func setRoomUsersCount(_ userCount: UInt) {
        if let model = self.roomModel {
            self.roomModel!.roomPeopleNum = String(userCount)
            if let _ = topView {
                self.topView.listModel = model
            }
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
                print("subVC:\(String(describing: type(of: vc)))")
                if String(describing: type(of: vc)) == "DHCVLOnLineListVC" {
                    weakSelf.navigationController?.popToViewController(vc, animated: true)
                    break
                }
            }
            
            VLKTVAlert.shared().dismiss()
        }
    }
    
    //下麦
    func leaveSeat(with seatModel: VLRoomSeatModel, completion:@escaping ((Error?) -> Void)) {

        let inputModel = KTVOutSeatInputModel()
        inputModel.userNo = seatModel.userNo
        inputModel.userId = seatModel.rtcUid
        inputModel.userName = seatModel.name
        inputModel.userHeadUrl = seatModel.headUrl
        inputModel.seatIndex = seatModel.seatIndex

        AppContext.ktvServiceImp()?.leaveSeatWithoutRemoveSong(with: inputModel, completion: { err in
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
//        if isEnterSeatNotFirst {
//            return
//        }
//
//        isEnterSeatNotFirst = true
        AgoraEntAuthorizedManager.checkAudioAuthorized(parent: self) { flag in
            if flag {
                AppContext.ktvServiceImp()?.updateSeatAudioMuteStatus(with: false, completion: { err in
                    
                })
            }
        }
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
    
    public func getCurrentUserMicSeat() -> VLRoomSeatModel? {
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
    
    private func removeCurrentSong(){
        removeSong(0)
    }
        
    private func stopPlaySong() {
        self.isPause = false
        ktvApi.switchSingerRole2(newRole: .audience) { state, reason in
            
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
    
    private func showChorusListView() {
        let VC: DHCShowChoruserView = DHCShowChoruserView()
        VC.dataSource = getChorusList()
        VC.leaveBlock = {[weak self] userNo in
            /**
             1.如果是主唱下麦就是切歌
             2.非主唱就是先下麦 然后自己退出合唱
             */
            guard let topSong = self?.selSongArray?.first, let self = self, let seatArray = self.seatsArray else {return}
            if topSong.userNo == userNo {
                //切歌
                self.stopPlaySong()
                self.removeCurrentSong()
            } else {
                //让他下麦 然后用户自己监听自己的麦位 退出合唱
                guard let seatModel = seatArray.filter{$0.userNo == userNo}.first else {return}
                leaveSeat(with: seatModel) { err in
                    
                }
            }
        }
        let presentView: DHCPresentView = DHCPresentView.shared
        presentView.showView(with: CGRect(x: 0, y: 0, width: ScreenWidth, height: 500), vc: VC, maxHeight: 500)
        view.addSubview(presentView)
    }
    
    private func getChorusList() -> [ChorusShowModel] {
        guard let seatArray = self.seatsArray, let topSong = selSongArray?.first else { return [] }
        let array = getChorusSingerArray(with: seatArray)
        let songOwnerArray = seatArray.filter { $0.userNo == topSong.userNo && isStringValid(topSong.userNo) && isStringValid($0.userNo) }
        var models = [ChorusShowModel]()
        for (index,value) in array.enumerated() {
            let model = ChorusShowModel(headIcon: value.headUrl ?? "", name: value.name ?? "", num: value.score, isMaster: songOwnerArray.first?.userNo == value.userNo, level: index, userNo: value.userNo ?? "", isRoomOwner: isRoomOwner)
            models.append(model)
        }
        return models
    }
    
    private func getChorusSingerArray(with seatArray: [VLRoomSeatModel]) -> [VLRoomSeatModel] {
        var singerSeatArray: [VLRoomSeatModel] = []
        
        guard let topSong = selSongArray?.first, let seatArray = self.seatsArray else {
            return singerSeatArray
        }
        
        if selSongArray?.count == 0 {
            return singerSeatArray
        }
        
        let predicate = NSPredicate(format: "chorusSongCode == %@", topSong.chorusSongId())
        var chorusArray = Array(seatArray.dropFirst())
        let matchedSeats = chorusArray.filter { predicate.evaluate(with: $0) }

//        let flag = checkIfSongOwner(with: 0) == true || checkIfCosinger(with: 0)
//        if flag, let firstSeat = seatArray.first {
//            singerSeatArray.append(firstSeat)
//        }
        
      //  singerSeatArray.append(contentsOf: seatArray.filter { $0.isOwner && !$0.isMaster })
        //判断第一首歌点歌用户和麦位进行匹配
        let validSeats = seatArray.filter { $0.userNo == topSong.userNo && isStringValid(topSong.userNo) && isStringValid($0.userNo) }
        if let mainSeat = validSeats.first {
            singerSeatArray.append(mainSeat)
        }
        singerSeatArray.append(contentsOf: matchedSeats)

        return singerSeatArray
    }
    
    private func checkIfCosinger(with index: Int) -> Bool {
        guard let selSongArray = self.selSongArray, let topSong = selSongArray.first, let seatArray = self.seatsArray else {
            return false
        }
        
        let predicate = NSPredicate(format: "chorusSongCode == %@", topSong.chorusSongId())
        let isMatched = seatArray.enumerated().contains { (offset, model) in
            offset == index && model.chorusSongCode == topSong.chorusSongId()
        }
        
        return isMatched
    }
    
    private func checkIfSongOwner(with index: Int) -> Bool {
        var flag = false
        guard let topSong = selSongArray?.first, let seatArray = self.seatsArray else {
            return false
        }
        let seat = seatArray[index]
        return seat.userNo == seat.userNo
    }
    
    private func changeToNextSong() {
        let title = "切换歌曲"
        let message = "切换下一首歌曲？"
        let array = ["取消", "确定"]
        VLAlert.shared().show(withFrame: UIScreen.main.bounds, title: title, message: message, placeHolder: "", type: ALERTYPE.ALERTYPENORMAL, buttonTitles: array) {[weak self] (flag, text) in
            guard let self = self, let selSongArray =  self.selSongArray else {return}
            if flag {
                if selSongArray.count >= 1 {
                    self.stopPlaySong()
                    self.lrcControlView.setScore(with: 0)
                    self.removeCurrentSong()
                }
                self.leaveSeat()
            }
            VLAlert.shared().dismiss()
        }
    }
    
    private func showResultView() {
        
        //将麦位按照分数排序
        guard let seatsArray = self.seatsArray, let songArray = self.selSongArray else {return}
        let sortedSeatsArray = seatsArray.sorted { $0.score > $1.score }
        var rankModels = [SubRankModel]()
        for (index, value) in sortedSeatsArray.enumerated() {
            let subModel = SubRankModel()
            subModel.userName = value.name
            subModel.poster = value.headUrl
            subModel.index = index
            subModel.score = value.score
            rankModels.append(subModel)
        }
        let totalScore = sortedSeatsArray.reduce(0, { (result, seatModel) -> Int in
            return result + seatModel.score
        })
        self.lrcControlView.controlState = .nextSong
        //获取即将演唱的歌曲信息
        var musicStr = ""
        if songArray.count >= 2 {
            let nextSong = songArray[1]
            musicStr = "\(nextSong.songName ?? "")-\(nextSong.singer ?? "")"
        }
        self.lrcControlView.setResultData(with: totalScore, models: rankModels, musicStr: musicStr, isRoomOwner: isRoomOwner)
    }
    
    func isStringValid(_ str: String?) -> Bool {
        if let string = str, !string.isEmpty {
            return true
        } else {
            return false
        }
    }
    
    private func showSettingView() {
        let popView = LSTPopView.popSettingView(withParentView: self.view, settingView: self.settingView, withDelegate: self)
        self.settingView = popView.currCustomView as? VLKTVSettingView
        self.settingView?.setIspause(self.isPause)
        self.settingView?.setIMMode(Int32(isIMMode))
    }
}

//加载RTC模块
extension CantataMainViewController: AgoraRtcEngineDelegate {
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
        
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, receiveStreamMessageFromUid uid: UInt, streamId: Int, data: Data) {
        guard let ktvApi = self.ktvApi else {return}
        ktvApi.didKTVAPIReceiveStreamMessageFrom(uid: NSInteger(uid), streamId: streamId, data: data)
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
        if event == .join {//加入合唱
            checkChorus()
        } else if event == .showChorus {//显示演唱者列表
            showChorusListView()
        } else if event == .next {//切歌
            changeToNextSong()
        } else if event == .play {//播放
            self.ktvApi.getMusicPlayer()?.play()
            self.isPause = false
        } else if event == .pause {//暂停
            self.ktvApi.getMusicPlayer()?.pause()
            self.isPause = true
        } else if event == .effect {//调音
            self.showSettingView()
        } else if event == .acc {//伴奏
            self.ktvApi.getMusicPlayer()?.selectMultiAudioTrack(1, publishTrackIndex: 1)
        } else if event == .origin {//原唱
            self.ktvApi.getMusicPlayer()?.selectMultiAudioTrack(0, publishTrackIndex: 1)
        } else if event == .leave {//退出合唱
            leaveSeat()
            self.lrcControlView.controlState = .joinChorus
            self.ktvApi.switchSingerRole2(newRole: .audience, stateCallBack: { state, reason in
                self.singerRole = .audience
            })
        } else if event == .resultNext {
            self.stopPlaySong()
            self.lrcControlView.setScore(with: 0)
            self.removeCurrentSong()
            self.leaveSeat()
        }
    }
}

extension CantataMainViewController: DHCLrcControlDelegate {
    
    public func didLrcViewDragedTo(pos: Int, score: Int, totalScore: Int) {
        self.ktvApi.getMusicPlayer()?.seek(toPosition: pos)
    }
    
    public func didLrcViewScorllFinished(with score: Int, totalScore: Int, lineScore: Int, lineIndex: Int) {
        if self.singerRole == .audience {return}
        var realScore = 0
        if self.singerRole == .soloSinger || self.singerRole == .leadSinger {
           //发送分数到服务端
            realScore = score
        } else if self.singerRole == .coSinger {
            //发送分数到服务端
            self.cosingerDegree += lineScore
            realScore = self.cosingerDegree
        }
        lrcControlView.setScore(with: realScore)
        AppContext.ktvServiceImp()?.updateSeatScoreStatus(with: realScore, completion: { err in
            if err == nil {
            }
        })
    }
    
    
}

//ktvapi的event handler
extension CantataMainViewController: KTVApiEventHandlerDelegate {
    public func onSingingScoreResult(score: Float) {
        
    }
    
    public func onSingerRoleChanged(oldRole: KTVSingRole, newRole: KTVSingRole) {
        self.singerRole = newRole
    }
    
    public func onChorusChannelAudioVolumeIndication(speakers: [AgoraRtcAudioVolumeInfo], totalVolume: Int) {
        
    }
    
    public func onMusicPlayerStateChanged(state: AgoraMediaPlayerState, error: AgoraMediaPlayerError, isLocal: Bool) {
        if isLocal && singerRole == .leadSinger || singerRole == .soloSinger {
            if state == .playBackCompleted || state == .playBackAllLoopsCompleted {
                //展示结算界面
                self.ktvApi.stopSing()
                let model = KTVRemoveSongInputModel()
                if let topsong = self.selSongArray?.first {
                    model.objectId = topsong.objectId
                    model.songNo = topsong.songNo
                    AppContext.ktvServiceImp()?.updateSongEndStatus(with: true, inputModel: model, completion: { err in
                        
                    })
                }
            }
        }
        
        if(self.singerRole == .coSinger){
            self.isPause = (isLocal && state == .paused);
        }
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
        let chorusArray = self.makeChorusArray()
        if realIndex >= chorusArray.count {
            //如果自己不在麦位上就上麦 自己在麦上就不反应
//            if let seatModel = getSeatModel(with: realIndex) {
//                if !checkIsOnMicSeat() {
//                    enterSeatWithModel(realIndex) { err in
//
//                    }
//                }
//            }
            return
        }
        
        let seatModel = chorusArray[realIndex]

        //如果是自己就下麦
        if seatModel.userNo == VLUserCenter.user.id {
            popDropLineView(with: seatModel)
            self.lrcControlView.controlState = .joinChorus
        }
//        } else {
//            //如果自己不在麦位上就上麦 自己在麦上就不反应, 还要判断这个麦位上面有没有人
//            let flag = (seatModel.rtcUid == "" || seatModel.rtcUid == nil)
//            if !checkIsOnMicSeat() && flag {
//                enterSeatWithModel(realIndex) { err in
//
//                }
//            }
//        }
    }
}

//头部视图代理
extension CantataMainViewController: VLKTVTopViewDelegate {
    
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
            self.RtcKit.adjustRecordingSignalVolume(self.isNowMicMuted ? 0 : 100)
        })
    }

}

//弹窗相关
extension CantataMainViewController: VLDropOnLineViewDelegate {
    private func popDropLineView(with seatModel: VLRoomSeatModel) {
        LSTPopView.popDropLineView(withParentView: self.view, with: seatModel, withDelegate: self)
    }
    
    public func onVLDrop(_ view: VLDropOnLineView, action seatModel: VLRoomSeatModel?) {
        guard let seatModel = seatModel else {return}
        
        leaveSeat(with: seatModel) { err in
            LSTPopView.getPopView(withCustomView: view).dismiss()
        }
        
        removeSongAndReloadStatus()
        
//        self.isPause = false
//        // 如果是点歌者下麦就切歌
//        let chorusArray = self.makeChorusArray()
//        let model = self.getCurrentUserMicSeat()
//        if model?.userNo == chorusArray.first?.userNo {
//            self.stopPlaySong()
//            self.lrcControlView.setScore(with: 0)
//            self.removeCurrentSong()
//        }
//        self.ktvApi.switchSingerRole2(newRole: .audience, stateCallBack: { state, reason in
//            self.singerRole = .audience
//        })
    }

}

extension CantataMainViewController: VLKTVSettingViewDelegate {
    func settingViewSettingChanged(_ setting: VLKTVSettingModel!, valueDidChangedType type: VLKTVValueDidChangedType) {
        if type == .typeEar {
            showEarSettingView()
        } else if type == .typeSound {
            let value = Int(setting.soundValue * 100)
            if self.soundValue != value {
                self.RtcKit.adjustRecordingSignalVolume(value)
                self.soundValue = value;
            }
        } else if type == .typeAcc {
            let value = setting.accValue * 100
            if(self.playoutVolume != Int(value)){
                self.playoutVolume = Int(value);
            }
        } else if type == .typeRemoteValue {
            let value = Int(setting.remoteVolume)
            self.RtcKit.adjustPlaybackSignalVolume(value)
        } else if type == .typeIMMode {
            let value = Int(setting.imMode)
            self.isIMMode = value
            self.RtcKit.adjustPlaybackSignalVolume(value == 1 ? 0 : 30)
        }
    }

    private func showEarSettingView() {
        LSTPopView.popEarSettingView(withParentView: self.view, isEarOn: self.isEarOn, vol: CGFloat(self.earValue), withDelegate: self)
    }
    
    public func onVLKTVEarSettingViewValueChanged(_ value: Double) {
        //耳返音量
        if self.earValue == value {
            return
        }
        self.earValue = value
        self.RtcKit.setInEarMonitoringVolume(Int(value))
    }
    
    public func onVLKTVEarSettingViewSwitchChanged(_ flag:Bool) {
        //耳返开关
        self.isEarOn =  flag
        self.RtcKit.enable(inEarMonitoring: flag, includeAudioFilters: .none)
    }
    
}


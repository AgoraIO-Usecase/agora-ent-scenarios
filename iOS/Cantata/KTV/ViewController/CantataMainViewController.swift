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
                var usefullSongs = newSongs.filter { (model) -> Bool in
                    return model.songNo != nil
                }
                if usefullSongs.count == 0 {return}
                updateSongView(with: usefullSongs)
                
                if usefullSongs.count == 0 {
                    controlView.controlState = .noSong
                    chorusView.isHidden = true
                    return
                }
                
                if let topSong = newSongs.first {
                    if topSong.musicEnded == true {
                        //结算时刻点歌不做歌曲更新
                        return
                    }
                }
                
                if let oldSongs = oldValue {
                    let oldFirst = oldSongs.first
                    let newFirst = usefullSongs.first
                    if ((oldFirst?.songNo != newFirst?.songNo) || (oldFirst?.status == .playing && newFirst?.status == .idle))
                    {
                        loadAndPlaySong()
                    }
                } else {
                    if usefullSongs.count > 0 {
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
    private var selectedEffectIndex: Int = 0
    private var isEnterSeatNotFirst: Bool = false
    private var rtcDataStreamId: Int = 0
    private var isOnMicSeat: Bool = false
    private var chorusMicView: ChorusMicView!
    private var topView: DHCVLKTVTopView!
    private var botView: VLBottomView!
    private var lrcControlView: DHCLRCControl!
    private var isBrodCaster: Bool = false
    private var settingView: DHCVLKTVSettingView?
    public var searchKeyWord: String?
    private var loadMusicCallBack:((Bool, String)->Void)?
    private var connection: AgoraRtcConnection?
    //沉浸模式
    private var isIMMode: Int = 0
    private var isLeavingChorus: Bool = false
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
                top.switchEnable = isSongOwner
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
    private var earValue: Double = 100
    private var soundValue: Int = 100
    private var isNetWorkBad: Bool = false
    
    private var testBtn: UIButton!
    private var isDumpAble:Bool = false
    private var timeManager: TimerManager = TimerManager()
    private var playoutVolume: Int = 50 {
        didSet {
            self.ktvApi.getMusicPlayer()?.adjustPlayoutVolume(Int32(playoutVolume))
            self.ktvApi.getMusicPlayer()?.adjustPublishSignalVolume(Int32(playoutVolume))
            self.settingView?.setAccValue(Float(playoutVolume) / 100.0)
        }
    }
    
    private var scoreMap: [String: ScoreModel] = [:]
    public override func viewDidLoad() {
        super.viewDidLoad()
        layoutUI()
        
        isRoomOwner = VLUserCenter.user.ifMaster
        if isRoomOwner == true {
            self.timeManager.startTimer(withTarget: self, andSelector: #selector(giveupRoom))
            KTVLog.info(text: "ROOM owner start timer")
        }
        
        addDebugLogic()
        if isRoomOwner == true {
            guard let roomNo = roomModel?.roomNo else {return}
            ApiManager.shared.fetchStartCloud(mainChannel: roomNo, cloudRtcUid: 232425)
        }
        subscribeServiceEvent()
        loadRtc()
    }
    
    private func addDebugLogic() {

        if AppContext.shared.isDebugMode {
            // 如果开启了debug模式
            let debugBtn = UIButton(frame: CGRect(x: ScreenWidth - 100, y: ScreenHeight - 200, width: 80, height: 80))
            debugBtn.backgroundColor = UIColor.blue
            debugBtn.layer.cornerRadius = 40
            debugBtn.layer.masksToBounds = true
            debugBtn.setTitleColor(UIColor.white, for: .normal)
            debugBtn.setTitle("Debug", for: .normal)
            debugBtn.addTarget(self, action: #selector(showDebug), for: .touchUpInside)
            self.view.addSubview(debugBtn)
        }
    }

    @objc func showDebug() {
        let presentView = LSTPopView.popDebugView(withParentView: self.view, isDebugMode: self.isDumpAble, with: self)
        
    }
    
    @objc private func giveupRoom() {
        let topSong = self.selSongArray?.first
        if topSong != nil {
            return
        }
        if self.isRoomOwner == false {return}
        let mes = "因长时间未点歌，您的房间已解散，请重新创建房间"
        DispatchQueue.main.async {
            VLKTVAlert.shared().showKTVToast(withFrame: UIScreen.main.bounds, image: UIImage.sceneImage(name: "empty", bundleName: "DHCResource")!, message: mes, buttonTitle: "ktv_confirm".toSceneLocalization()) {[weak self] flag, text in
                guard let self = self else {return}
                VLKTVAlert.shared().dismiss()
                self.timeManager.stopTimer()
                KTVLog.info(text: "ROOM owner stop timer")
                self.leaveRoom()
            }
        }
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
            self.timeManager.stopTimer()
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
        topView = DHCVLKTVTopView(frame: CGRect(x: 0, y: Int(kStatusBarHeight), width: Int(ScreenWidth), height: 60), with: self)
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
        lrcControlView.skipCallBack = {[weak self] time, flag in
            guard let self = self else {return}
            let seekTime = flag ? (self.ktvApi.getMusicPlayer()?.getDuration() ?? 800) - 800 : time
            self.ktvApi.seekSing(time: seekTime)
        }
        mainBgView.addSubview(lrcControlView)
        
        //麦位
        chorusMicView = ChorusMicView(frame: CGRect(x: 0, y: mainBgView.bounds.size.height - 350 - 50, width: ScreenWidth, height: 340), topMicCount: 8)
        mainBgView.addSubview(chorusMicView)
        chorusMicView.delegate = self
        chorusMicView.backgroundColor = .clear
        chorusMicView.isHidden = true
        
        botView = VLBottomView(frame: CGRect(x: 0, y: ScreenHeight - CGFloat(ZBottombarHeight) - 50, width: ScreenWidth, height: 50))
        botView.delegate = self
        botView.audioBtn.isUserInteractionEnabled = false
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
        botView.updateMicState(true)
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
        scoreMap.removeAll()
        chorusMicView.isHidden = false
        lrcControlView.resetShowOnce()
        self.isIMMode = 0
        guard let model = self.selSongArray?.first else {return}
        markSong(with: model)
        
        let role: KTVSingRole = model.userNo == VLUserCenter.user.id ? .leadSinger : .audience

        //获取合唱用户
        guard let seatsArray = self.seatsArray else {return}
        lrcControlView.setChoursNum(with: seatsArray.count)
        lrcControlView.resetStatus()
        chorusMicView.isHidden = true
        lrcControlView.noSongLabel.isHidden = true
        lrcControlView.hideBotView()
        
        self.chorusMicView.seatArray = self.makeChorusArray()

        let config = KTVSongConfiguration()
        config.autoPlay = (role == .audience || role == .coSinger) ? false : true
        config.mode = (role == .audience || role == .coSinger) == true ? .loadLrcOnly : .loadMusicAndLrc
        config.mainSingerUid = Int(model.userNo ?? "") ?? 0
        config.songIdentifier = model.songNo ?? ""
        
        self.loadMusicCallBack = {[weak self] flag, songCode in
            guard let self = self else {return}
            
            DispatchQueue.main.async {
                self.lrcControlView.noSongLabel.isHidden = false
                self.chorusMicView.isHidden = false
            }

            if flag == false {
                DispatchQueue.main.async {
                    if VLUserCenter.user.id == model.userNo {
                        self.lrcControlView.controlState = .ownerSing
                    } else {
                        self.lrcControlView.controlState = self.isRoomOwner ? .ownerChorus : .joinChorus
                    }
                }
                return
            }

            let seatModel = self.getCurrentUserMicSeat()
            if seatModel == nil {
                if role == .leadSinger {
                    self.enterSeat(withIndex:0) {[weak self] error in
                        guard let self = self else {return}
                        if let error = error {
                            return
                        }
                    }
                }
            }
            if role == .leadSinger {
                DispatchQueue.main.async {
                    self.lrcControlView.skipBtn.isHidden = false
                    self.botView.updateMicState(false)
                }
            }
            isLeavingChorus = false
            
            DispatchQueue.main.async {
                if VLUserCenter.user.id == model.userNo {
                    self.lrcControlView.controlState = .ownerSing
                    //self.checkEnterSeatAudioAuthorized()
                } else {
                    self.lrcControlView.controlState = self.isRoomOwner ? .ownerChorus : .joinChorus
                }
            }
        }
        
        self.ktvApi?.loadMusic(songCode: Int(model.songNo ?? "") ?? 0, config: config, onMusicLoadStateListener: self)
        self.ktvApi.switchSingerRole2(newRole: role) { state, reason in
            if state != .success {
                print("switch failed:\(role)----\(state.rawValue)")
            }
        }
        

    }
    
    public func checkChorus() {
 //       guard let conn = self.connection else {return}
//        if self.RtcKit.getConnectionStateEx(conn) != .connected {
//            VLToast.toast("加入合唱失败，reson:连接已断开")
//            return
//        }
        
        
        self.lrcControlView.controlState = .beforeJoinChorus
        joinChorus()
    }
    
    private func joinChorus() {
        
        guard let selSongArray = self.selSongArray, let model = selSongArray.first as? VLRoomSelSongModel else {
            return
        }
        
        self.RtcKit.setParameters("{\"rtc.use_audio4\": true}")

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
                weakSelf.lrcControlView.controlState = weakSelf.isRoomOwner ? .ownerChorus : .joinChorus
                return
            }
            
            //没有上麦需要先上麦
            let seatModel = weakSelf.getCurrentUserMicSeat()
            if seatModel == nil {
                weakSelf.enterSeat(withIndex: 0) {[weak self] error in
                            guard let self = self else {return}
                            if let error = error {
                                // cp todo  KTVLogError("enterSeat error: \(error.localizedDescription)")
                                self.lrcControlView.controlState = weakSelf.isRoomOwner ? .ownerChorus : .joinChorus
                                return
                            }
                        }
            }

            print("before switch role, load music success")
            
            weakSelf.ktvApi.switchSingerRole2(newRole: role) { state, reason in
                if state == .fail && reason != .noPermission {
                    DispatchQueue.main.async {
                        weakSelf.lrcControlView.controlState = weakSelf.isRoomOwner ? .ownerChorus : .joinChorus
                        weakSelf.chorusMicView.isHidden = false
                    }
                    
                    VLToast.toast(String(format: "join chorus fail: %ld", reason.rawValue))
                // cp todo    KTVLogInfo("join chorus fail")
                    // TODO: error toast?
                    return
                }
                DispatchQueue.main.async {
                    weakSelf.lrcControlView.controlState = self?.isRoomOwner == true ? .ownerChorusSing : .chorusSing
                }
                weakSelf.isNowMicMuted = role == .audience
                
                let inputModel = KTVJoinChorusInputModel()
                inputModel.isChorus = true
                inputModel.songNo = model.songNo
                AppContext.ktvServiceImp()?.joinChorus(with: inputModel) { error in
                    // completion block
                }
                
                //加入频道成功。更新自己的分数
                let scoreModel = weakSelf.scoreMap[VLUserCenter.user.id ?? ""]
                weakSelf.lrcControlView.setScore(with: scoreModel?.score ?? 0)
                weakSelf.chorusMicView.isHidden = false
                // 开麦
//                AppContext.ktvServiceImp()?.updateSeatAudioMuteStatus(with: false) { error in
//                    // completion block
//                }
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
            self.lrcControlView.controlState = self.isRoomOwner ? .ownerChorus : .joinChorus
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

        var micArray = [VLRoomSeatModel]()
        let topSeat = seatArray.first
        //如果当前歌曲不是第一位演唱 那么第一位就是点歌人，然后其他人顺延
        var hostArray = seatArray.filter { $0.userNo == topSong.userNo}
        var chorusArray = seatArray.filter { $0.userNo != topSong.userNo}
        
        micArray.append(contentsOf: hostArray)
        micArray.append(contentsOf: chorusArray)
        
        //如果chorusArray的个数小于8就补齐8个
        if micArray.count < 8 {
            let count = 8 - micArray.count
            for i in 0..<count {
                micArray.append(VLRoomSeatModel())
            }
        }
        return micArray
    }
}

//ktvapi模块
extension CantataMainViewController: IMusicLoadStateListener {
    public func onMusicLoadFail(songCode: Int, reason: KTVLoadSongFailReason) {
        if let loadMusicCallBack = self.loadMusicCallBack {
            loadMusicCallBack(false, "\(songCode)")
            self.loadMusicCallBack = nil
        }
        
        if self.isNetWorkBad {
            VLToast.toast("网络中断，请切歌重试", duration: 5.0)
        }
        
        DispatchQueue.main.async {
            if reason == .noLyricUrl {//歌词加载失败
                self.lrcControlView.retryBtn.isHidden = false
            } else if reason == .cancled || reason == .musicPreloadFail {//歌曲加载失败 切歌
                
            }
            
            if self.singerRole == .soloSinger || self.singerRole == .leadSinger {
                self.lrcControlView.updateLoadingView(with: 100)
            }
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
            guard let self = self, let userNo = seatModel.userNo else {return}
//            AgoraEntAuthorizedManager.checkMediaAuthorized(parent: self) { granted in
//                guard granted else { return }
                var preSongCode = String()

                if status == .created {
                    self.seatsArray?.append(seatModel)
                    //先要判断map里面有没有 如果没有就添加新的 如果有就更新
                    if !scoreMap.keys.contains(userNo) && userNo.count > 0 {
                        let scoreModel = ScoreModel(name: seatModel.name ?? "", score: 0, headUrl: seatModel.headUrl ?? "")
                        self.scoreMap.updateValue(scoreModel, forKey: userNo)
                    }
                } else if( status == .updated) {
                    //如果没有这个userNo就是新增
                    var flag = false
                    if let seatsArray = self.seatsArray {
                        for i in seatsArray {
                            if i.userNo == seatModel.userNo {
                                flag = true
                            }
                        }
                    }
                    if flag == true {
                        //更新麦位数据
                        self.updateModel(withId: seatModel)
                        
                        if var scoreModel = self.scoreMap[userNo] {
                            scoreModel.score = seatModel.score
                            self.scoreMap.updateValue(scoreModel, forKey: userNo)
                        }
                        
                        //如果观众的scoreMap没有这个麦位说明他是中途加入的 需要更新scoreMap
                        if !scoreMap.keys.contains(userNo) && userNo.count > 0 && self.singerRole == .audience {
                            let scoreModel = ScoreModel(name: seatModel.name ?? "", score: 0, headUrl: seatModel.headUrl ?? "")
                            self.scoreMap.updateValue(scoreModel, forKey: userNo)
                        }
                        
                    } else {
                        self.seatsArray?.append(seatModel)
                        if !scoreMap.keys.contains(userNo) && userNo.count > 0 {
                            let scoreModel = ScoreModel(name: seatModel.name ?? "", score: 0, headUrl: seatModel.headUrl ?? "")
                            self.scoreMap.updateValue(scoreModel, forKey: userNo)
                        }
                    }

                } else if status == .deleted {
                    // 下麦消息
                    if VLUserCenter.user.id == seatModel.userNo && self.isLeavingChorus == false && self.singerRole == .coSinger {
                        DispatchQueue.main.async {
                            VLToast.toast("您已被踢下麦")
                            self.leaveSeat()
                            self.singerRole = .audience
                            self.lrcControlView.controlState = self.isRoomOwner ? .ownerChorus : .joinChorus
                            self.ktvApi.switchSingerRole2(newRole: .audience) { _, _ in

                            }
                        }
                    }
                    
                    if seatModel.userNo == VLUserCenter.user.id {
                        self.botView.updateMicState(true)
                    }
                    
//                    if let topMusic = self.selSongArray?.first, topMusic.status == .playing, seatModel.userNo == topMusic.userNo {
//                        return
//                    }
                    
                    if let seatArray = self.seatsArray,let index = seatArray.firstIndex(where: { $0.userNo == seatModel.userNo }) {
                        self.seatsArray?.remove(at: index)
                    }
                    //如果是下麦的合唱用户
                    if seatModel.userNo == VLUserCenter.user.id {
                        self.cosingerDegree = 0
                        self.lrcControlView.setScore(with: 0)
                    }
                }
                
                if status == .updated && self.singerRole == .audience {//
                    KTVLog.info(text: "观众分数更新")
                    let totalScore = self.scoreMap.values.map({ $0.score }).reduce(0, +)
                    self.lrcControlView.setScore(with: totalScore ?? 0)
                    KTVLog.info(text: "观众分数更新完毕")
                }
                
                let currentSeat = getCurrentUserMicSeat()
                self.botView.updateMicState(currentSeat == nil || currentSeat?.isAudioMuted == 1)
                if status == .deleted && seatModel.userNo == VLUserCenter.user.id {
                    self.botView.updateMicState(true)
                }

                let realSeatArray = self.makeChorusArray()
                self.chorusMicView.seatArray = realSeatArray
              
                //更新RTC身份
                //上麦主播，下麦观众 更新当前观众即可
                guard let seatsArray = self.seatsArray else {return}
                self.lrcControlView.setChoursNum(with: seatsArray.count)
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
            
            if status == .created && isRoomOwner == true {
                if self.selSongArray?.count == 0 {
                    KTVLog.info(text: "ROOM owner stop timer")
                    self.timeManager.stopTimer() //有人点歌 需要关闭
                }
            }
            
            if status == .deleted {
                if songArray.count == 0 {
                    self.chorusMicView.isHidden = true
                    self.lrcControlView.controlState = .noSong
                }
                
                guard let selSongArray = self.selSongArray, let topSong = selSongArray.first, let leaveModel = getCurrentUserMicSeat() else { return }
                if let songNo = songInfo.songNo, songNo == topSong.songNo {
                    if self.singerRole != .audience {
                        self.stopPlaySong()
                        self.singerRole = .audience
                        self.isLeavingChorus = true
                        self.ktvApi.switchSingerRole2(newRole: .audience, stateCallBack: { state, reason in
                            // 这里可以处理状态回调
                        })
                        leaveSeat(with: leaveModel) { err in
                            // 这里可以处理离开座位后的回调
                        }
                    }
                    self.lrcControlView.resetStatus()
                }
                let success = self.removeSelSong(songNo: Int(songInfo.songNo ?? "")!, sync: false)
                self.selSongArray = songArray
                
                if self.selSongArray?.count == 0 && isRoomOwner == true{
                    KTVLog.info(text: "ROOM owner restart timer:\(self.singerRole)")
                    self.timeManager.restartTimer()
                }

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
                            self.ktvApi.stopSing()
                            self.showResultView()
                        }
                    }
                }
            }
        }

        AppContext.ktvServiceImp()?.subscribeNetworkStatusChanged {[weak self] status in
            guard let self = self else {return}
            self.isNetWorkBad = status != .open
            if status != .open {
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
    
    private func updateModel(withId seatModel: VLRoomSeatModel) {
        guard let seatsArray = self.seatsArray else {return}
        if let index = seatsArray.firstIndex(where: { $0.userNo == seatModel.userNo }) {
            // 找到模型并修改相应属性
            self.seatsArray?[index] = seatModel
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
            guard let songNo = songInfo.songNo, let songName = songInfo.songName else {return}
            addModel.songCode = songNo
            addModel.name = songName
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
    func getUserSeatInfo(with userNo: String?) -> VLRoomSeatModel? {
        guard let seatsArray = self.seatsArray else {return nil}
        for model in seatsArray {
            if model.userNo == userNo {
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
        leaveSeat()
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

        AppContext.ktvServiceImp()?.leaveSeatWithoutRemoveSong(with: seatModel, completion: { err in
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
       // checkEnterSeatAudioAuthorized()
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
        self.ktvApi?.stopSing()
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
            if let error = error, songArray?.count == 0{
                return
            }

            self.selSongArray = songArray
            //判断一下如果在麦位上就给他加入合唱
            if let micSeat = self.getCurrentUserMicSeat() {
                self.chorusMicView.seatArray = self.seatsArray
                self.checkChorus()
            }
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
                    DHCPresentView.shared.dismiss()
                }
            }
        }
        let presentView: DHCPresentView = DHCPresentView.shared
        presentView.showView(with: CGRect(x: 0, y: 0, width: ScreenWidth, height: 500), vc: VC, maxHeight: 500)
        view.addSubview(presentView)
    }
    
    private func getChorusList() -> [ChorusShowModel] {
        guard let seatArray = self.seatsArray, let topSong = selSongArray?.first else { return [] }
        //let array = getChorusSingerArray(with: seatArray)
        let array = seatArray
        let songOwnerArray = seatArray.filter { $0.userNo == topSong.userNo && isStringValid(topSong.userNo) && isStringValid($0.userNo) }
        var models = [ChorusShowModel]()
        for (index,value) in array.enumerated() {
            let scoreModel: ScoreModel = scoreMap[value.userNo ?? ""] ?? ScoreModel(name: "", score: 0, headUrl: "")
            let model = ChorusShowModel(headIcon: value.headUrl ?? "", name: value.name ?? "", num: scoreModel.score, isMaster: songOwnerArray.first?.userNo == value.userNo, level: index, userNo: value.userNo ?? "", isRoomOwner: isRoomOwner)
            models.append(model)
        }
        //把models按照num进行高到低排序
        models.sort { (model1, model2) -> Bool in
            return model1.num > model2.num
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
                    self.botView.updateMicState(true)
                    self.lrcControlView.setScore(with: 0)
                    self.removeCurrentSong()
                }
            }
            VLAlert.shared().dismiss()
        }
    }
    
    private func showResultView() {
        
        //将麦位按照分数排序
        guard let songArray = self.selSongArray else {return}
        
        //结果列表通过scoreMap来展示
        let scoreModels = scoreMap.values.sorted { $0.score > $1.score }
        var rankModels = [SubRankModel]()
        for (index, value) in scoreModels.enumerated() {
            let subModel = SubRankModel()
            subModel.userName = value.name
            subModel.poster = value.headUrl
            subModel.index = index
            subModel.score = value.score
            rankModels.append(subModel)
        }
        let totalScore = scoreModels.reduce(0, { (result, scoreModel) -> Int in
            return result + scoreModel.score
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
        if let string = str, !string.isEmpty, string.count > 0 {
            return true
        } else {
            return false
        }
    }
    
    private func showSettingView() {
        let popView = LSTPopView.popSettingView(withParentView: self.view, settingView: self.settingView, withDelegate: self)
        self.settingView = popView.currCustomView as? DHCVLKTVSettingView
        self.settingView?.setIsEarOn(self.isEarOn)
        self.settingView?.setIspause(self.isPause)
        self.settingView?.setIMMode((Int32(isIMMode) != 0))
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
            self.isLeavingChorus = false
            checkChorus()
        } else if event == .showChorus {//显示演唱者列表
            showChorusListView()
        } else if event == .next {//切歌
            if isNetWorkBad {
                VLToast.toast("网络异常，请检查网络")
                return
            }
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
            self.isLeavingChorus = true
            leaveSeat()
            self.ktvApi.switchSingerRole2(newRole: .audience, stateCallBack: { state, reason in
                self.singerRole = .audience
            })
            //退出合唱 更新为观众的分数
            let totalScore = self.scoreMap.values.reduce(0, { (result, scoreModel) -> Int in
                return result + scoreModel.score
            })
            self.lrcControlView.setScore(with: totalScore)
        } else if event == .resultNext {
            self.stopPlaySong()
            self.lrcControlView.setScore(with: 0)
            self.removeCurrentSong()
            self.leaveSeat()
        } else if event == .retryLrc {
            //歌词重试
            self.lrcControlView.retryBtn.isHidden = true
            self.loadAndPlaySong()
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
//            self.cosingerDegree += lineScore
//            realScore = self.cosingerDegree
            //先获取记录的score
            if scoreMap.keys.contains(VLUserCenter.user.id ?? "") {
                if let scoreModel: ScoreModel = scoreMap[VLUserCenter.user.id ?? ""] {
                    let score = scoreModel.score
                    realScore = score + lineScore
                }
            }
        }
        lrcControlView.setScore(with: realScore)
        self.ktvApi.setSingingScore(score: lineScore)
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
        DispatchQueue.main.async {
            self.botView.audioBtn.isUserInteractionEnabled = self.singerRole != .audience
            self.lrcControlView.isMainSinger = (self.singerRole == .leadSinger || self.singerRole == .soloSinger)
        }
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
        
        if state == .playing {
            if self.singerRole == .leadSinger || self.singerRole == .soloSinger {
                self.lrcControlView.showPreludeEnd()
            }
        } else if state == .paused {
            if self.singerRole == .leadSinger || self.singerRole == .soloSinger {
                self.lrcControlView.hideSkipView(flag: true)
            }
        }
    }
    
    public func onTokenPrivilegeWillExpire() {
        
    }
    
    
}

extension CantataMainViewController: ChorusMicViewDelegate {
    func didChorusMicViewClicked(with index: Int) {
        print("\(index)clicked")
        
        return
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
extension CantataMainViewController: DHCVLKTVTopViewDelegate {
    
    public func onVLKTVTopView(_ view: DHCVLKTVTopView, closeBtnTapped sender: Any) {
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

    public func onVLKTVTopView(_ view: DHCVLKTVTopView, moreBtnTapped sender: Any) {
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
        
        if self.isEarOn && self.isNowMicMuted {
            self.isEarOn = false
            self.settingView?.setIsEarOn(false)
            self.RtcKit.enable(inEarMonitoring: self.isEarOn, includeAudioFilters: .none)
        }
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

extension CantataMainViewController: DHCVLKTVSettingViewDelegate, VLEarSettingViewViewDelegate{
    func settingViewEffectChoosed(_ effectIndex: Int) {
        self.selectedEffectIndex = effectIndex
        let effects: [AgoraAudioEffectPreset] = [
            .off,
            .roomAcousticsChorus,
            .roomAcousticsKTV,
            .roomAcousVocalConcer,
            .roomAcousStudio,
            .roomAcousPhonograph,
            .roomAcousSpatial,
            .roomAcousEthereal,
            .styleTransformationPopular,
            .styleTransformationRnb
        ]
        self.RtcKit.setAudioEffectPreset(effects[effectIndex])
    }
    
    func settingViewSettingChanged(_ setting: VLKTVSettingModel!, valueDidChangedType type: DHCVLKTVValueDidChangedType) {
        if type == DHCVLKTVValueDidChangedTypeEar {
            showEarSettingView()
        } else if type == DHCVLKTVValueDidChangedTypeSound {
            let value = Int(setting.soundValue)
            if self.soundValue != value {
                self.RtcKit.adjustRecordingSignalVolume(value)
                self.soundValue = value;
            }
        } else if type == DHCVLKTVValueDidChangedTypeAcc {
            let value = Int(setting.accValue)
            if(self.playoutVolume != Int(value)){
                self.playoutVolume = Int(value);
            }
        } else if type == DHCVLKTVValueDidChangedTypeRemoteValue {
            let value = Int(setting.remoteVolume)
            self.RtcKit.adjustPlaybackSignalVolume(value)
        } else if type == DHCVLKTVValueDidChangedTypeIMMode {
            let value = Int(setting.imMode)
            self.isIMMode = value
        }
    }
    
    private func showEarSettingView() {
        LSTPopView.popEarSettingView(withParentView: self.view, isEarOn: self.isEarOn, vol: CGFloat(self.earValue), with: self)
    }
    
    public func onVLKTVEarSettingViewValueChanged(_ value: Double) {
        //耳返音量
        if self.earValue == value {
            return
        }
        self.earValue = value
        self.RtcKit.setInEarMonitoringVolume(Int(value))
    }
    
    func onVLKTVEarSettingViewSwitchChanged(_ flag: Bool) {
        self.isEarOn = flag;
        self.settingView?.setIsEarOn(flag)
        self.RtcKit.enable(inEarMonitoring: flag, includeAudioFilters: .none)
    }
    
}

extension CantataMainViewController: DHCDebugViewDelegate {
    func didDumpModeChanged(_ enable: Bool) {
        self.isDumpAble = enable
        let key = "dump enable"
        let status = !KTVDebugInfo.getSelectedStatus(forKey: key)
        KTVDebugInfo.setSelectedStatus(status, forKey: key)
        KTVDebugManager.reLoadParamAll()
    }
    
    func didExportLog(with path: String) {
        let activityController = UIActivityViewController(activityItems: [URL(fileURLWithPath: path, isDirectory: true)], applicationActivities: nil)
        activityController.modalPresentationStyle = .fullScreen
        self.present(activityController, animated: true, completion: nil)
    }
    
    func didParamsSet(with key: String, value: String) {
        if value.lowercased() == "true" || value.lowercased() == "false" || value.lowercased() == "yes" || value.lowercased() == "no" {
            let flag = value.lowercased() == "true" || value.lowercased() == "yes"
            let params: String
            if flag {
                params = "{\"\(key)\":true"
            } else {
                params = "{\"\(key)\":false"
            }
            self.RtcKit.setParameters(params)
        } else if let num = Int(value) {
            let params = "{\"\(key)\":\(num)"
            self.RtcKit.setParameters(params)
        } else {
            let params = "{\"\(key)\":\"\(value)\""
            self.RtcKit.setParameters(params)
        }
    }
}

struct ScoreModel {
    var name: String
    var score: Int
    var headUrl: String
}

class TimerManager {
    var workItem: DispatchWorkItem?
    var target: AnyObject?
    var selector: Selector?
    
    func startTimer(withTarget target: AnyObject, andSelector selector: Selector) {
        self.target = target
        self.selector = selector
        
        workItem = DispatchWorkItem { [weak self] in
            self?.timerAction()
        }
        DispatchQueue.global().asyncAfter(deadline: .now() + 300, execute: workItem!)
    }
    
    func stopTimer() {
        workItem?.cancel()
        workItem = nil
    }
    
    func restartTimer() {
        guard let target = target, let selector = selector else {
            return
        }
        
        stopTimer()  // 先停止之前的计时器
        startTimer(withTarget: target, andSelector: selector)  // 然后启动新的计时器
    }
    
    @objc func timerAction() {
        target?.perform(selector)
    }
}



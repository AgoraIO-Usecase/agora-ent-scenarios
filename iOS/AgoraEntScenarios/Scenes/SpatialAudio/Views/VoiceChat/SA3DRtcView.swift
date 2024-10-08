//
//  AgoraChatRoom3DRtcView.swift
//  VoiceChat4Swift
//
//  Created by CP on 2022/8/30.
//

import UIKit
import AgoraRtcKit
import AgoraCommon
class SA3DRtcView: UIView {
    private var collectionView: UICollectionView!
    private let vIdentifier = "3D"
    private let nIdentifier = "normal"
    private var rtcUserView: SA3DMoveUserView = .init()

    private var lastPoint: CGPoint = .zero
    private var lastPrePoint: CGPoint = .init(x: UIScreen.main.bounds.size.width / 2.0, y: 275)
    private var lastCenterPoint: CGPoint = .init(x: UIScreen.main.bounds.size.width / 2.0, y: 275)
    private var lastMovedPoint: CGPoint = .init(x: UIScreen.main.bounds.size.width / 2.0, y: 275)

    private var panGesture: UIPanGestureRecognizer?
    private var lastTime: CFAbsoluteTime = CFAbsoluteTimeGetCurrent()
    private lazy var redSpatialParams = AgoraSpatialAudioParams()
    private lazy var blueSpatialParams = AgoraSpatialAudioParams()
    private var isPlaying: Bool = false
    private var isPlayerSetted: Bool = false
    
    public var clickBlock: ((Int) -> Void)?
    
    public var rtcKit: SARTCManager?

    public var micInfos: [SARoomMic]? {
        didSet {
            guard let _ = collectionView else {
                return
            }
            collectionView.reloadData()
            
            guard let mic = micInfos?.first else { return }
            rtcUserView.user = mic.member
            panGesture?.isEnabled = mic.member?.uid == VLUserCenter.user.id
        }
    }

    init(rtcKit: SARTCManager?) {
        super.init(frame: .zero)
        self.rtcKit = rtcKit
        layoutUI()
        rtcKit?.setupSpatialAudio()
        rtcKit?.playerDelegate = self
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func playMusic(isPlay: Bool) {
        rtcKit?.playMusic(with: .Spatical, isPlay: isPlay)
    }
    
    private func setupMeidaPlayerSpatial() {
        if (isPlayerSetted) { return }
        if let playerId = rtcKit?.blueMediaPlayer?.getMediaPlayerId(),
           let micInfo = micInfos?[3],
           let pos = micInfo.pos {// blue
            print("spatial pos: blue: \(micInfo.mic_index) \(micInfo.pos ?? [0 ,0 ,0])")
            rtcKit?.setMediaPlayerPositionInfo(playerId: Int(playerId),
                                               position: pos,
                                               forward: micInfo.forward ?? [0 ,0 ,0])
        } else {
            return
        }
        if let playerId = rtcKit?.redMediaPlayer?.getMediaPlayerId(),
           let micInfo = micInfos?[6],
           let pos = micInfo.pos {// red
            print("spatial pos: red: \(micInfo.mic_index) \(micInfo.pos ?? [0 ,0 ,0])")
            rtcKit?.setMediaPlayerPositionInfo(playerId: Int(playerId),
                                               position: pos,
                                               forward: micInfo.forward ?? [0 ,0 ,0])
        } else {
            return
        }
        isPlayerSetted = true
    }
    
    //因为麦位顺序的特殊性 需要对数据进行调整
    private func getRealIndex(with index: Int) -> Int {//4表示中间的用户
        let realIndexs: [Int] = [3, 1, 0, 4, 5, 6, 2]
        return realIndexs[index]
    }
    
    public func updateVolume(with index: Int, vol: Int) {
        let realIndex: Int = getRealIndex(with: index)
        let indexPath = IndexPath(item: realIndex, section: 0)
        DispatchQueue.main.async {[weak self] in
            if realIndex != 3 {
                if let cell = self?.collectionView.cellForItem(at: indexPath) as? SA3DUserCollectionViewCell {
                    cell.refreshVolume(vol: vol)
                }
            } else {
                //更新可移动view的数据
                guard let micInfos = self?.micInfos else { return }
                let micInfo = micInfos[0]
                micInfo.member?.volume = vol
                self?.rtcUserView.volume = vol
            }
        }
    }

    public func updateUser(_ mic: SARoomMic) {
        // 更新micinfos数组
        let info = micInfos?[mic.mic_index]
        mic.pos = info?.pos
        mic.forward = info?.forward
        mic.right = info?.right
        micInfos?[mic.mic_index] = mic
        
        // 更新空间音频位置
        self.updateSpatialPos(with: mic)
        
        let realIndex: Int = getRealIndex(with: mic.mic_index)
        let indexPath = IndexPath(item: realIndex, section: 0)
        if realIndex != 3 {
            DispatchQueue.main.async {[weak self] in
                guard let cell = self?.collectionView.cellForItem(at: indexPath) as? SA3DUserCollectionViewCell else { return }
                cell.refreshUser(with: mic)
            }
        }
    }
    
    public func updateAlienMic(with type: SARtcType.ALIEN_TYPE) {
        DispatchQueue.main.async {[weak self] in
            guard let red = self?.collectionView.cellForItem(at: IndexPath(item: 2, section: 0)) as? SA3DUserCollectionViewCell,
                  let blue = self?.collectionView.cellForItem(at: IndexPath(item: 4, section: 0)) as? SA3DUserCollectionViewCell
            else {
                return
            }
            if type == .red {
                red.refreshVolume(vol: 60)
                blue.refreshVolume(vol: 0)
            } else if type == .blue  {
                red.refreshVolume(vol: 0)
                blue.refreshVolume(vol: 60)
            } else if type == .blueAndRed {
                red.refreshVolume(vol: 60)
                blue.refreshVolume(vol: 60)
            } else if (type == .none || type == .ended) {
                red.refreshVolume(vol: 0)
                blue.refreshVolume(vol: 0)
            }
        }
    }

    @discardableResult
    func updateCenterUserPosition() -> [Double] {
        guard let micInfo = micInfos?.first else { return [] }
        let pos = viewCenterPostion(view: rtcUserView)
        let realPosition = calcuRealPositon(angle: rtcUserView.angle)
        micInfo.pos = pos
        micInfo.forward = realPosition.0
        if micInfo.member?.uid == VLUserCenter.user.id {
            micInfo.right = realPosition.1
            rtcKit?.updateSpetialPostion(position: pos,
                                         axisForward: realPosition.0,
                                         axisRight: realPosition.1,
                                         axisUp: [0, 0, 1])
        } else {
            rtcKit?.updateRemoteSpetialPostion(uid: micInfo.member?.uid ?? "0",
                                               position: pos,
                                               forward: realPosition.0)
        }
        let p = realPosition.0
        return [Double(p.x), Double(p.y), Double(p.z)]
    }
    
    private func layoutUI() {
        let flowLayout = UICollectionViewFlowLayout()
        flowLayout.itemSize = CGSize(width: bounds.size.width / 4.0, height: 120)
        flowLayout.minimumLineSpacing = 0
        flowLayout.minimumInteritemSpacing = 0
        flowLayout.scrollDirection = .vertical

        let collectionView = UICollectionView(frame: .zero, collectionViewLayout: flowLayout)
        collectionView.register(SA3DUserCollectionViewCell.self, forCellWithReuseIdentifier: vIdentifier)
        collectionView.register(UICollectionViewCell.self, forCellWithReuseIdentifier: nIdentifier)
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.backgroundColor = .clear

        self.collectionView = collectionView
        addSubview(collectionView)
        self.collectionView.snp.makeConstraints { make in
            make.edges.equalTo(self)
        }

        addSubview(rtcUserView)
        rtcUserView.tag = 200
        rtcUserView.tapClickBlock = {[weak self] in
            guard let clickBlock = self?.clickBlock else {return}
            clickBlock(200)
        }
        rtcUserView.snp.makeConstraints { make in
            make.center.equalTo(self)
            make.width.height.equalTo(150)
        }
        
        panGesture = UIPanGestureRecognizer(target: self, action: #selector(pan(pan:)))
        rtcUserView.addGestureRecognizer(panGesture!)
    }
    
    func setAirAbsorb(isRed: Bool, isBlue: Bool, isOpen: Bool) {
        if isRed {
            redSpatialParams.enable_air_absorb = isOpen
            rtcKit?.redMediaPlayer?.setSpatialAudioParams(redSpatialParams)
        }
        if isBlue {
            blueSpatialParams.enable_air_absorb = isOpen
            rtcKit?.blueMediaPlayer?.setSpatialAudioParams(blueSpatialParams)
        }
    }
    func setVoiceBlur(isRed: Bool, isBlue: Bool, isOpen: Bool) {
        if isRed {
            redSpatialParams.enable_blur = isOpen
            rtcKit?.redMediaPlayer?.setSpatialAudioParams(redSpatialParams)
        }
        if isBlue {
            blueSpatialParams.enable_blur = isOpen
            rtcKit?.blueMediaPlayer?.setSpatialAudioParams(blueSpatialParams)
        }
    }
    func setPlayerAttenuation(isRed: Bool, isBlue: Bool, attenuation: Double) {
        if isRed {
            rtcKit?.setPlayerAttenuation(attenuation: attenuation,
                                         playerId: rtcKit?.redMediaPlayer?.getMediaPlayerId() ?? 0)
        }
        if isBlue {
            rtcKit?.setPlayerAttenuation(attenuation: attenuation,
                                         playerId: rtcKit?.blueMediaPlayer?.getMediaPlayerId() ?? 0)
        }
    }
}

extension SA3DRtcView {
    
    private func checkEdgeRange(point: CGPoint) -> CGPoint {
        var moveCenter: CGPoint = point
        // 处理边界
        if moveCenter.x <= 50 {
            moveCenter.x = 50
        }
        if moveCenter.y <= frame.origin.y - rtcUserView.height * 0.5 {
            moveCenter.y = frame.origin.y - rtcUserView.height * 0.5
        }
        if moveCenter.x >= frame.size.width - 50 {
            moveCenter.x = frame.size.width - 50
        }

        if moveCenter.y >= frame.height - rtcUserView.height * 0.5 {
            moveCenter.y = frame.height - rtcUserView.height * 0.5
        }
        return moveCenter
    }
    
    @objc private func pan(pan: UIPanGestureRecognizer) {
        let translation = pan.translation(in: self)
        var moveCenter = CGPoint(x: rtcUserView.center.x + translation.x,
                                 y: rtcUserView.center.y + translation.y)
        
        moveCenter = checkEdgeRange(point: moveCenter)
        let angle = getAngle(rtcUserView.center, preP: lastCenterPoint)
        rtcUserView.angle = angle
        lastCenterPoint = rtcUserView.center
        
        rtcUserView.center = CGPoint(x: moveCenter.x, y: moveCenter.y)
        pan.setTranslation(.zero, in: self)
        
        let forward = updateCenterUserPosition()
        let pos = viewCenterPostion(view: rtcUserView)
        DispatchQueue.global().async {
            let currentTime = CFAbsoluteTimeGetCurrent()
            if currentTime - 0.05 < self.lastTime { return }
            if let user = self.micInfos?.first?.member {
                var info = SAPositionInfo()
                info.uid = Int(user.uid ?? "0") ?? 0
                info.forward = forward
                info.x = CGFloat(pos[0])
                info.y = CGFloat(pos[1])
                info.angle = angle
                var streamInfo = SADataStreamInfo()
                streamInfo.message = JSONObject.toJsonString(info)
                guard let streamData = JSONObject.toData(streamInfo) else { return }
                self.rtcKit?.sendStreamMessage(with: streamData)
                self.lastTime = currentTime
            }
        }
    }

    fileprivate func getAngle(_ curP: CGPoint, preP: CGPoint) -> Double {
        let changeX = curP.x - preP.x
        let changeY = curP.y - preP.y
        let radina = atan2(changeY, changeX)
        let angle = 180.0 / Double.pi * radina
        return angle
    }
    
    private func calcuRealPositon(angle: Double) -> (simd_float3, simd_float3)  {
        let fx = cos(angle)
        let fy = sin(angle)
        return ([Float(fx), Float(fy), 0], [Float(fy), Float(-fx), 0])
    }
    
    //  获取视图在笛卡尔坐标系中的位置
    private func viewConvertToPoint(rect: CGRect) -> CGPoint {
        let axisLength = 20.0
        let fullWidth = collectionView.frame.width
        let fullHeight = collectionView.frame.height
        let oPoint = CGPoint(x: fullWidth * 0.5, y: fullHeight * 0.5)
        
        // 翻转Y轴
        let turnY = fullHeight - rect.origin.y
        let vPoint = CGPoint(x: rect.width * 0.5 + rect.origin.x,
                             y: turnY - (rect.height * 0.5))
        // 相对坐标
        let relativePoint = CGPoint(x: vPoint.x - oPoint.x,
                                    y: vPoint.y - oPoint.y)
        // 屏幕相对坐标转化为坐标系坐标
        return CGPoint(x: relativePoint.x / fullWidth * axisLength,
                       y: relativePoint.y / fullHeight * axisLength)
    }
    
    // 将笛卡尔坐标转换成视图中的坐标
    private func pointConvertToView(point: CGPoint) -> CGPoint {
        let axisLength = 20.0
        let fullWidth = collectionView.frame.width
        let fullHeight = collectionView.frame.height
        let oPoint = CGPoint(x: fullWidth * 0.5, y: fullHeight * 0.5)
        // 笛卡尔屏幕坐标
        let vPoint = CGPoint(x: point.x / axisLength * fullWidth,
                             y: point.y / axisLength * fullHeight)
        let x = oPoint.x + vPoint.x
        let y = oPoint.y - vPoint.y
        return CGPoint(x: x, y: y)
    }
    
    private func viewCenterPostion(view: UIView) -> simd_float3 {
        return [Float(viewConvertToPoint(rect: view.frame).x), Float(viewConvertToPoint(rect: view.frame).y), 0]
    }
    private func viewCenterPostion(rect: CGRect) -> simd_float3 {
        return [Float(viewConvertToPoint(rect: rect).x), Float(viewConvertToPoint(rect: rect).y), 0]
    }
    
    private func updateSpatialPos() {
        guard let ary = micInfos else {
            return
        }
        for micInfo in ary {
            self.updateSpatialPos(with: micInfo)
        }
    }
    
    private func updateSpatialPos(with micInfo: SARoomMic) {
        guard let member = micInfo.member else { return }
        if member.uid == VLUserCenter.user.id {
            print("spatial pos: local: \(micInfo.mic_index) \(micInfo.pos ?? [0 ,0 ,0])")
            rtcKit?.updateSpetialPostion(position: micInfo.pos ?? [0, 0, 0],
                                         axisForward: micInfo.forward ?? [1, 0, 0],
                                         axisRight: micInfo.right ?? [0, 1, 0],
                                         axisUp: micInfo.up)
            return
        }
        rtcKit?.updateRemoteSpetialPostion(uid: member.uid,
                                           position: micInfo.pos ?? [0, 0, 0],
                                           forward: micInfo.forward ?? [1, 0, 0])
    }
}

extension SA3DRtcView: UICollectionViewDelegate, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        (micInfos == nil || micInfos?.isEmpty == true) ? 0 : 7
    }

    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        if indexPath.item != 3 {
            return CGSize(width: bounds.size.width / 3.0, height: 150)
        } else {
            return CGSize(width: bounds.size.width, height: bounds.size.height - 300)
        }
    }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        if indexPath.item != 3 {
            let cell = collectionView.dequeueReusableCell(withReuseIdentifier: vIdentifier,
                                                          for: indexPath) as! SA3DUserCollectionViewCell
            cell.clickBlock = {[weak self] tag in
                print("tag:----\(tag)")
                guard let block = self?.clickBlock else { return }
                block(tag)
            }
            switch indexPath.item {
            case 0:
                if let mic_info = micInfos?[2] {
                    cell.tag = 202
                    cell.setArrowInfo(imageName: "sa_downright_arrow", margin: 6)
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeDown
                    cell.refreshUser(with: mic_info)
                }
            case 1:
                if let mic_info = micInfos?[1] {
                    cell.tag = 201
                    cell.setArrowInfo(imageName: "sa_down_arrow", margin: 6)
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeUp
                    cell.refreshUser(with: mic_info)
                }
            case 2:
                if let mic_info = micInfos?[6] {
                    cell.tag = 206
                    let member: SAUser = SAUser()
                    member.name = "spatial_voice_agora_red".spatial_localized()
                    member.portrait = "red"
                    mic_info.member = member
                    cell.setArrowInfo(imageName: "sa_downleft_arrow", margin: 6)
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeDown
                    cell.refreshUser(with: mic_info)
                }
            case 4:
                if let mic_info = micInfos?[3] {
                    cell.tag = 203
                    let member: SAUser = SAUser()
                    member.name = "spatial_voice_agora_blue".spatial_localized()
                    member.portrait = "blue"
                    mic_info.member = member
                    cell.setArrowInfo(imageName: "sa_upright_arrow", margin: -6)
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeUp
                    cell.refreshUser(with: mic_info)
                }
            case 5:
                if let mic_info = micInfos?[4] {
                    cell.tag = 204
                    cell.setArrowInfo(imageName: "sa_up_arrow", margin: -5)
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeDown
                    cell.refreshUser(with: mic_info)
                }
            case 6:
                if let mic_info = micInfos?[5] {
                    cell.tag = 205
                    cell.setArrowInfo(imageName: "sa_upleft_arrow", margin: -4)
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeUp
                    cell.refreshUser(with: mic_info)
                }
            default:
                break
            }
            return cell
        } else {
            let cell = collectionView.dequeueReusableCell(withReuseIdentifier: nIdentifier, for: indexPath)
            return cell
        }
    }
    
    func collectionView(_ collectionView: UICollectionView, willDisplay cell: UICollectionViewCell, forItemAt indexPath: IndexPath) {
        guard let rtcUserView = (cell as? SA3DUserCollectionViewCell)?.rtcUserView.iconView else { return }
        let rect = rtcUserView.convert(rtcUserView.frame, to: collectionView)
        let pos = viewCenterPostion(rect: rect)
        switch indexPath.item {
        case 0:
            if let mic_info = micInfos?[2] {
                mic_info.forward = [1, -1, 0]
                mic_info.right = [-1, -1, 0]
                mic_info.pos = pos
            }
        case 1:
            if let mic_info = micInfos?[1] {
                mic_info.forward = [0, -1, 0]
                mic_info.right = [-1, 0, 0]
                mic_info.pos = pos
            }
        case 2:
            if let mic_info = micInfos?[6] { // red robot
                mic_info.forward = [-1, -1, 0]
                mic_info.right = [-1, 1, 0]
                mic_info.pos = pos
            }
            
        case 4:
            if let mic_info = micInfos?[3] { // blue robot
                mic_info.forward = [1, 1, 0]
                mic_info.right = [1, -1, 0]
                mic_info.pos = pos
            }
        case 5:
            if let mic_info = micInfos?[4] {
                mic_info.forward = [0, 1, 0]
                mic_info.right = [1, 0, 0]
                mic_info.pos = pos
            }
        case 6:
            if let mic_info = micInfos?[5] {
                mic_info.forward = [-1, 1, 0]
                mic_info.right = [1, 1, 0]
                mic_info.pos = pos
            }
        default:
            break
        }
    }

    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        print("index === \(indexPath.item)")
        
    }
}
extension SA3DRtcView: SAMusicPlayerDelegate {
    
    func didReceiveStreamMsgOfUid(uid: UInt, data: Data) {
        guard "\(uid)" != VLUserCenter.user.id else { return }
        DispatchQueue.main.async {
            let result = String(data: data, encoding: .utf8)
            guard let streamInfo = JSONObject.toModel(SADataStreamInfo.self, value: result),
                  let info = JSONObject.toModel(SAPositionInfo.self, value: streamInfo.message) else { return }
            
            var point = self.pointConvertToView(point: CGPoint(x: info.x, y: info.y))
            point = self.checkEdgeRange(point: point)
            let pos = self.viewCenterPostion(rect: CGRect(origin: point, size: self.rtcUserView.size))
            let forward = simd_float3(Float(info.forward[safe: 0] ?? 0), Float(info.forward[safe: 1] ?? 0), Float(info.forward[safe: 2] ?? 0))
            self.rtcKit?.updateRemoteSpetialPostion(uid: "\(uid)",
                                               position: pos,
                                               forward: forward)
            
            UIView.animate(withDuration: 0.25) {
                self.rtcUserView.center = point
            }
            self.rtcUserView.angle = info.angle
        }
    }
    
    func didMPKChangedTo(_ playerKit: AgoraRtcMediaPlayerProtocol, state: AgoraMediaPlayerState, reason: AgoraMediaPlayerReason) {
        if state == .playing && isPlaying == false {
            updateCenterUserPosition()
            setupMeidaPlayerSpatial()
        }
        isPlaying = state == .playing
    }
}

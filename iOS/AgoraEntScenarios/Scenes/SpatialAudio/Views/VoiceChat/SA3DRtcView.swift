//
//  AgoraChatRoom3DRtcView.swift
//  VoiceChat4Swift
//
//  Created by CP on 2022/8/30.
//

import UIKit
import AgoraRtcKit

private enum SATouchState {
    case began
    case moved
    case ended
}

class SA3DRtcView: UIView {
    private var collectionView: UICollectionView!
    private let vIdentifier = "3D"
    private let nIdentifier = "normal"
    private var rtcUserView: SA3DMoveUserView = .init()

    private var lastPoint: CGPoint = .zero
    private var lastPrePoint: CGPoint = .init(x: UIScreen.main.bounds.size.width / 2.0, y: 275~)
    private var lastCenterPoint: CGPoint = .init(x: UIScreen.main.bounds.size.width / 2.0, y: 275~)
    private var lastMovedPoint: CGPoint = .init(x: UIScreen.main.bounds.size.width / 2.0, y: 275~)
    private var touchState: SATouchState = .began
    
    private var redMediaPlayer: AgoraRtcMediaPlayerProtocol?
    private var blueMediaPlayer: AgoraRtcMediaPlayerProtocol?
    private var panGesture: UIPanGestureRecognizer?
    
    public var clickBlock: ((SABaseUserCellType, Int) -> Void)?
    public var activeBlock: ((SABaseUserCellType) -> Void)?
    
    public var rtcKit: SARTCManager?

    public var micInfos: [SARoomMic]? {
        didSet {
            guard let _ = collectionView else {
                return
            }
            collectionView.reloadData()
            
            guard let mic = micInfos?.first else { return }
            rtcUserView.cellType = getCellTypeWithStatus(mic.status)
            rtcUserView.user = mic.member
            panGesture?.isEnabled = mic.member?.uid == VLUserCenter.user.id
        }
    }

    init(rtcKit: SARTCManager?) {
        super.init(frame: .zero)
        self.rtcKit = rtcKit
        SwiftyFitsize.reference(width: 375, iPadFitMultiple: 0.6)
        layoutUI()
        setupSpatialAudio()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func updatePlayerVolume(value: Double) {
        redMediaPlayer?.adjustPlayoutVolume(Int32(value))
        blueMediaPlayer?.adjustPlayoutVolume(Int32(value))
    }
    
    func playMusic(isPlay: Bool) {
        let redMusicPath = "\(SAConfig.CreateCommonRoom)\(SAConfig.baseAlienMic[1])"
        let blueMusicPath = "\(SAConfig.CreateCommonRoom)\(SAConfig.baseAlienMic[0])"
        if isPlay {
            redMediaPlayer?.open(redMusicPath, startPos: 0)
            blueMediaPlayer?.open(blueMusicPath, startPos: 0)
            redMediaPlayer?.mute(true)
            blueMediaPlayer?.mute(true)
        } else {
            redMediaPlayer?.stop()
            blueMediaPlayer?.stop()
        }
        collectionView.reloadData()
    }
    
    private func setupSpatialAudio() {
        rtcKit?.playerDelegate = self
        redMediaPlayer = rtcKit?.initMediaPlayer()
        redMediaPlayer?.adjustPlayoutVolume(Int32(0.45 * 400))
        rtcKit?.setPlayerAttenuation(attenuation: 0.2, playerId: redMediaPlayer?.getMediaPlayerId() ?? 0)
        blueMediaPlayer = rtcKit?.initMediaPlayer()
        blueMediaPlayer?.adjustPlayoutVolume(Int32(0.45 * 400))
        rtcKit?.setPlayerAttenuation(attenuation: 0.2, playerId: blueMediaPlayer?.getMediaPlayerId() ?? 0)
    }
    
    //因为麦位顺序的特殊性 需要对数据进行调整
    private func getRealIndex(with index: Int) -> Int {//4表示中间的用户
        let realIndexs: [Int] = [3, 1, 0, 4, 5, 6, 2]
        return realIndexs[index]
    }
    
    public func updateVolume(with uid: String, vol: Int) {
        /**
         1.根据uid来判断是哪个cell需要更新音量
         2.更新音量
         */
        guard let micInfos = micInfos else {
            return
        }
        for i in micInfos {
            guard let member = i.member else { return }
            guard let cur_uid = member.uid else { return }
            if cur_uid == uid {
                guard let mic_index = member.mic_index else { return }
                let realIndex = getRealIndex(with: mic_index)
                let indexPath = IndexPath(item: realIndex, section: 0)
                if realIndex != 3 {
                    DispatchQueue.main.async {[weak self] in
                        guard let cell: SA3DUserCollectionViewCell = self?.collectionView.cellForItem(at: indexPath) as? SA3DUserCollectionViewCell else { return }
                        cell.refreshVolume(vol: vol)
                    }
                } else {
                    //更新可移动view的数据
                    let micInfo = micInfos[0]
                    rtcUserView.cellType = getCellTypeWithStatus(micInfo.status)
                    rtcUserView.tag = 200
                    rtcUserView.user = micInfo.member
                }
            }
        }
    }

    public func updateVolume(with index: Int, vol: Int) {
        let realIndex: Int = getRealIndex(with: index)
        let indexPath = IndexPath(item: index, section: 0)
        DispatchQueue.main.async {[weak self] in
            if realIndex != 3 {
                guard let cell: SA3DUserCollectionViewCell = self?.collectionView.cellForItem(at: indexPath) as? SA3DUserCollectionViewCell else { return }
                cell.refreshVolume(vol: vol)
            } else {
                //更新可移动view的数据
                guard let micInfos = self?.micInfos else { return }
                let micInfo = micInfos[0]
                self?.rtcUserView.cellType = self?.getCellTypeWithStatus(micInfo.status) ?? .AgoraChatRoomBaseUserCellTypeAdd
                self?.rtcUserView.tag = 200
                self?.rtcUserView.user = micInfo.member
            }
        }
    }

    public func updateUser(_ mic: SARoomMic) {
        
        // 更新micinfos数组
        self.micInfos?[mic.mic_index] = mic
        
        let realIndex: Int = getRealIndex(with: mic.mic_index)
        micInfos?[mic.mic_index] = mic
        let indexPath = IndexPath(item: realIndex, section: 0)
        if realIndex != 3 {
            DispatchQueue.main.async {[weak self] in
                guard let cell: SA3DUserCollectionViewCell = self?.collectionView.cellForItem(at: indexPath) as? SA3DUserCollectionViewCell else { return }
                cell.refreshUser(with: mic)
            }
        } else {
            //更新可移动view的数据
            rtcUserView.cellType = getCellTypeWithStatus(mic.status)
            rtcUserView.user = mic.member
            panGesture?.isEnabled = mic.member?.uid == VLUserCenter.user.id
        }
    }

    public func updateAlienMic(_ index: Int, flag: Bool) {
        let indexPath = IndexPath(item: index, section: 0)
        DispatchQueue.main.async {[weak self] in
            guard let cell: SA3DUserCollectionViewCell = self?.collectionView.cellForItem(at: indexPath) as? SA3DUserCollectionViewCell else { return }
            cell.updateAlienMic(flag: flag)
        }
    }
    
    public func updateAlienMic(with type: SARtcType.ALIEN_TYPE) {
        if type == .red {
            updateAlienMic(2,flag: true)
            updateAlienMic(4, flag: false)
        } else if type == .blue  {
            updateAlienMic(4, flag: true)
            updateAlienMic(4, flag: false)
        } else if type == .blueAndRed {
            updateAlienMic(2, flag: true)
            updateAlienMic(4, flag: true)
        } else if type == .none {
            updateAlienMic(2, flag: false)
            updateAlienMic(4, flag: false)
        }
    }

    @discardableResult
    func updateCenterUserPosition() -> [Double] {
        guard let micInfo = micInfos?.first else { return [] }
        let pos = viewCenterPostion(view: rtcUserView)
        let realPosition = calcuRealPositon(angle: rtcUserView.angle)
        if micInfo.member?.uid == VLUserCenter.user.id {
            rtcKit?.updateSpetialPostion(position: pos,
                                         axisForward: realPosition.0,
                                         axisRight: realPosition.1,
                                         axisUp: [0, 0, 1])
        } else {
            rtcKit?.updateRemoteSpetialPostion(uid: micInfo.member?.uid ?? "0",
                                               position: pos,
                                               forward: realPosition.0)
        }
        print("pos == \(pos)  forward == \(realPosition.0) right == \(realPosition.1) angle == \(rtcUserView.angle)")
        return realPosition.0.map({ $0.doubleValue })
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
            if let micinfo: SARoomMic = self?.micInfos?[0] {
                clickBlock(self?.getCellType(With: micinfo.status) ?? .AgoraChatRoomBaseUserCellTypeAdd, 200)
            } else {
                clickBlock(.AgoraChatRoomBaseUserCellTypeAdd, 200)
            }
        }
        rtcUserView.snp.makeConstraints { make in
            make.center.equalTo(self)
            make.width.height.equalTo(150~)
        }
        
        panGesture = UIPanGestureRecognizer(target: self, action: #selector(pan(pan:)))
        rtcUserView.addGestureRecognizer(panGesture!)
    }
    
    func getCellType(With status: Int) -> SABaseUserCellType {
        if let _ = self.micInfos?[0] {
            switch status {
                case 0:
                    return .AgoraChatRoomBaseUserCellTypeNormalUser
                case 1:
                    return .AgoraChatRoomBaseUserCellTypeMute
                case 2:
                    return .AgoraChatRoomBaseUserCellTypeForbidden
                case 3:
                    return .AgoraChatRoomBaseUserCellTypeLock
                case 4:
                    return .AgoraChatRoomBaseUserCellTypeMuteAndLock
                case -1:
                    return .AgoraChatRoomBaseUserCellTypeAdd
                default:
                    break
            }
        } else {
            return .AgoraChatRoomBaseUserCellTypeAdd
        }
        return .AgoraChatRoomBaseUserCellTypeAdd
    }
}

extension SA3DRtcView {
    
    private func checkEdgeRange(point: CGPoint) -> CGPoint {
        var moveCenter: CGPoint = point
        // 处理边界
        if moveCenter.x <= 50~ {
            moveCenter.x = 50~
        }
        if moveCenter.y <= frame.origin.y - rtcUserView.height * 0.5 {
            moveCenter.y = frame.origin.y - rtcUserView.height * 0.5
        }
        if moveCenter.x >= frame.size.width - 50~ {
            moveCenter.x = frame.size.width - 50~
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
        if pan.state == .changed {
            rtcUserView.angle = angle
            lastCenterPoint = rtcUserView.center
            
            rtcUserView.center = CGPoint(x: moveCenter.x, y: moveCenter.y)
            pan.setTranslation(.zero, in: self)
        } else if pan.state == .ended {
            rtcUserView.angle = angle
            let forward = updateCenterUserPosition()
            
            if let user = micInfos?.first?.member {
                let pos = viewCenterPostion(view: rtcUserView)
                let info = SAPositionInfo()
                info.uid = Int(user.uid ?? "0") ?? 0
                info.position = pos.map({ $0.doubleValue })
                info.forward = forward
                info.x = pos.first?.doubleValue ?? 0
                info.y = pos[1].doubleValue
                info.angle = angle
                guard let streamData = JSONObject.toData(info) else { return }
                let ret = rtcKit?.sendStreamMessage(with: streamData)
                print("ret == \(ret)")
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
    
    private func calcuRealPositon(angle: Double) -> ([NSNumber], [NSNumber])  {
        let angle = angle < 0 ? 90 : angle == 90 ? 270 : angle
        let fx = cos(angle)
        let fy = sin(angle)
        let forward = [NSNumber(value: Double(fx)),
                       NSNumber(value: Double(fy)),
                       NSNumber(0.0)]
        let right = [NSNumber(value: Double(-fy)),
                     NSNumber(value: Double(fx)),
                     NSNumber(0.0)]
        return (forward, right)
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
                       y: relativePoint.y / fullWidth * axisLength)
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
    
    private func viewCenterPostion(view: UIView) -> [NSNumber] {
        let pos = [NSNumber(value: Double(viewConvertToPoint(rect: view.frame).x)),
                   NSNumber(value: Double(viewConvertToPoint(rect: view.frame).y)),
                   NSNumber(0.0)]
        return pos
    }
    private func viewCenterPostion(rect: CGRect) -> [NSNumber] {
        let pos = [NSNumber(value: Double(viewConvertToPoint(rect: rect).x)),
                   NSNumber(value: Double(viewConvertToPoint(rect: rect).y)),
                   NSNumber(0.0)]
        return pos
    }
    
    private func setMediaPlayerPosition(pos: [NSNumber], forward: [NSNumber]?, playerId: Int) {
        rtcKit?.setMediaPlayerPositionInfo(playerId: playerId,
                                           position: pos,
                                           forward: forward)
    }
    
    private func setAirAbsorb(isOpen: Bool, mediaPlayer: AgoraRtcMediaPlayerProtocol?) {
        let spatialParams = AgoraSpatialAudioParams()
        spatialParams.enable_air_absorb = isOpen
        mediaPlayer?.setSpatialAudioParams(spatialParams)
    }
    
    private func setVoiceBlur(isOpen: Bool, mediaPlayer: AgoraRtcMediaPlayerProtocol?) {
        let spatialParams = AgoraSpatialAudioParams()
        spatialParams.enable_blur = isOpen
        mediaPlayer?.setSpatialAudioParams(spatialParams)
    }
    
    private func setPlayerAttenuation(mediaPlayer: AgoraRtcMediaPlayerProtocol?, attenuation: Double) {
        rtcKit?.setPlayerAttenuation(attenuation: attenuation, playerId: mediaPlayer?.getMediaPlayerId() ?? 0)
    }
}

extension SA3DRtcView: UICollectionViewDelegate, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        (micInfos == nil || micInfos?.isEmpty == true) ? 0 : 7
    }

    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        if indexPath.item != 3 {
            return CGSize(width: bounds.size.width / 3.0, height: 150~)
        } else {
            return CGSize(width: bounds.size.width, height: bounds.size.height - 300~)
        }
    }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        if indexPath.item != 3 {
            let cell = collectionView.dequeueReusableCell(withReuseIdentifier: vIdentifier,
                                                          for: indexPath) as! SA3DUserCollectionViewCell
            cell.clickBlock = {[weak self] tag in
                print("tag:----\(tag)")
                guard let block = self?.clickBlock else { return }
                block(cell.cellType, tag)
            }
            switch indexPath.item {
            case 0:
                if let mic_info = micInfos?[2] {
                    cell.tag = 202
                    cell.setArrowInfo(imageName: "sa_downright_arrow", margin: 6)
                    cell.cellType = getCellTypeWithStatus(mic_info.status)
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeDown
                    cell.refreshUser(with: mic_info)
                }
            case 1:
                if let mic_info = micInfos?[1] {
                    cell.tag = 201
                    cell.setArrowInfo(imageName: "sa_down_arrow", margin: 6)
                    cell.cellType = getCellTypeWithStatus(mic_info.status)
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeUp
                    cell.refreshUser(with: mic_info)
                }
            case 2:
                if let mic_info = micInfos?[6] {
                    cell.tag = 206
                    let member: SAUser = SAUser()
                    member.name = "Agora Red"
                    member.portrait = "red"
                    mic_info.member = member
                    cell.setArrowInfo(imageName: "sa_downleft_arrow", margin: 6)
                    cell.cellType = mic_info.status == 5 ? .AgoraChatRoomBaseUserCellTypeAlienActive : .AgoraChatRoomBaseUserCellTypeAlienNonActive
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeDown
                    cell.refreshUser(with: mic_info)
                    
                    setAirAbsorb(isOpen: mic_info.airAbsorb, mediaPlayer: redMediaPlayer)
                    setVoiceBlur(isOpen: mic_info.voiceBlur, mediaPlayer: redMediaPlayer)
                    setPlayerAttenuation(mediaPlayer: redMediaPlayer, attenuation: mic_info.attenuation)
                }
                
            case 4:
                if let mic_info = micInfos?[3] {
                    cell.tag = 203
                    let member: SAUser = SAUser()
                    member.name = "Agora Blue"
                    member.portrait = "blue"
                    mic_info.member = member
                    cell.setArrowInfo(imageName: "sa_upright_arrow", margin: -6)
                    cell.cellType = mic_info.status == 5 ? .AgoraChatRoomBaseUserCellTypeAlienActive : .AgoraChatRoomBaseUserCellTypeAlienNonActive
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeUp
                    cell.refreshUser(with: mic_info)
                    
                    setAirAbsorb(isOpen: mic_info.airAbsorb, mediaPlayer: blueMediaPlayer)
                    setVoiceBlur(isOpen: mic_info.voiceBlur, mediaPlayer: blueMediaPlayer)
                    setPlayerAttenuation(mediaPlayer: blueMediaPlayer, attenuation: mic_info.attenuation)
                }
            case 5:
                if let mic_info = micInfos?[4] {
                    cell.tag = 204
                    cell.setArrowInfo(imageName: "sa_up_arrow", margin: -6)
                    
                    cell.cellType = getCellTypeWithStatus(mic_info.status)
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeDown
                    cell.refreshUser(with: mic_info)
                }
            case 6:
                if let mic_info = micInfos?[5] {
                    cell.tag = 205
                    cell.setArrowInfo(imageName: "sa_upleft_arrow", margin: -6)
                    
                    cell.cellType = getCellTypeWithStatus(mic_info.status)
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
                if mic_info.member?.uid == VLUserCenter.user.id {
                    rtcKit?.updateSpetialPostion(position: pos,
                                                 axisForward: mic_info.forward ?? [],
                                                 axisRight: mic_info.right ?? [],
                                                 axisUp: mic_info.up)
                } else {
                    rtcKit?.updateRemoteSpetialPostion(uid: mic_info.member?.uid,
                                                       position: pos,
                                                       forward: mic_info.forward)
                }
            }
        case 1:
            if let mic_info = micInfos?[1] {
                mic_info.forward = [0, -1, 0]
                mic_info.right = [-1, 0, 0]
                if mic_info.member?.uid == VLUserCenter.user.id {
                    rtcKit?.updateSpetialPostion(position: pos,
                                                 axisForward: mic_info.forward ?? [],
                                                 axisRight: mic_info.right ?? [],
                                                 axisUp: mic_info.up)
                } else {
                    rtcKit?.updateRemoteSpetialPostion(uid: mic_info.member?.uid,
                                                       position: pos,
                                                       forward: mic_info.forward)
                }
            }
        case 2:
            if let mic_info = micInfos?[6] {
                mic_info.forward = [-1, -1, 0]
                mic_info.right = [-1, 1, 0]
                
                setMediaPlayerPosition(pos: pos,
                                       forward: mic_info.forward,
                                       playerId: Int(redMediaPlayer?.getMediaPlayerId() ?? 0))
            }
            
        case 4:
            if let mic_info = micInfos?[3] {
                mic_info.forward = [1, 1, 0]
                mic_info.right = [1, -1, 0]
                
                setMediaPlayerPosition(pos: pos,
                                       forward: mic_info.forward,
                                       playerId: Int(blueMediaPlayer?.getMediaPlayerId() ?? 0))
            }
        case 5:
            if let mic_info = micInfos?[4] {
                mic_info.forward = [0, 1, 0]
                mic_info.right = [1, 0, 0]
                if mic_info.member?.uid == VLUserCenter.user.id {
                    rtcKit?.updateSpetialPostion(position: pos,
                                                 axisForward: mic_info.forward ?? [],
                                                 axisRight: mic_info.right ?? [],
                                                 axisUp: mic_info.up)
                } else {
                    rtcKit?.updateRemoteSpetialPostion(uid: mic_info.member?.uid,
                                                       position: pos,
                                                       forward: mic_info.forward)
                }
            }
        case 6:
            if let mic_info = micInfos?[5] {
                mic_info.forward = [-1, 1, 0]
                mic_info.right = [1, 1, 0]
                if mic_info.member?.uid == VLUserCenter.user.id {
                    rtcKit?.updateSpetialPostion(position: pos,
                                                 axisForward: mic_info.forward ?? [],
                                                 axisRight: mic_info.right ?? [],
                                                 axisUp: mic_info.up)
                } else {
                    rtcKit?.updateRemoteSpetialPostion(uid: mic_info.member?.uid,
                                                       position: pos,
                                                       forward: mic_info.forward)
                }
            }
        default:
            break
        }
    }

    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        print("index === \(indexPath.item)")
        
    }
    
    private func getCellTypeWithStatus(_ status: Int) -> SABaseUserCellType {
        switch status {
            case -2:
                return .AgoraChatRoomBaseUserCellTypeAlienNonActive
            case -1:
                return .AgoraChatRoomBaseUserCellTypeAdd
            case 0:
                return .AgoraChatRoomBaseUserCellTypeNormalUser
            case 1:
                return .AgoraChatRoomBaseUserCellTypeMute
            case 2:
                return .AgoraChatRoomBaseUserCellTypeForbidden
            case 3:
                return .AgoraChatRoomBaseUserCellTypeLock
            case 4:
                return .AgoraChatRoomBaseUserCellTypeMuteAndLock
            case 5:
                return .AgoraChatRoomBaseUserCellTypeAlienActive
            default:
                return .AgoraChatRoomBaseUserCellTypeAdd
        }
    }
}
extension SA3DRtcView: SAMusicPlayerDelegate {
    func didMPKChangedTo(_ playerKit: AgoraRtcMediaPlayerProtocol, state: AgoraMediaPlayerState, error: AgoraMediaPlayerError) {
        if state == .openCompleted || state == .playBackAllLoopsCompleted || state == .playBackCompleted {
            playerKit.play()
        }
    }
    
    func didReceiveStreamMsgOfUid(uid: UInt, data: Data) {
        guard "\(uid)" != VLUserCenter.user.id else { return }
        let result = String(data: data, encoding: .utf8)
        guard let info = JSONObject.toModel(SAPositionInfo.self, value: result) else { return }
        
        let pos = info.position.map({ NSNumber(value: $0) })
        let forward = info.forward.map({ NSNumber(value: $0) })
        rtcKit?.updateRemoteSpetialPostion(uid: "\(uid)",
                                           position: pos,
                                           forward: forward)
        var point = pointConvertToView(point: CGPoint(x: info.x, y: info.y))
        point = checkEdgeRange(point: point)
        UIView.animate(withDuration: 0.25) {
            self.rtcUserView.center = point
        }
        rtcUserView.angle = info.angle
    }
}

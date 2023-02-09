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

    private var _lastPointAngle: Double = 0
    private var lastPoint: CGPoint = .zero
    fileprivate var sendTS: CLongLong = 0
    private var lastPrePoint: CGPoint = .init(x: UIScreen.main.bounds.size.width / 2.0, y: 275~)
    private var lastCenterPoint: CGPoint = .init(x: UIScreen.main.bounds.size.width / 2.0, y: 275~)
    private var lastMovedPoint: CGPoint = .init(x: UIScreen.main.bounds.size.width / 2.0, y: 275~)
    private var touchState: SATouchState = .began
    
    private var redMediaPlayer: AgoraRtcMediaPlayerProtocol?
    private var blueMediaPlayer: AgoraRtcMediaPlayerProtocol?

    public var clickBlock: (() -> Void)?
    public var activeBlock: ((SABaseUserCellType) -> Void)?
    
    public var rtcKit: SARTCManager?

    public var micInfos: [SARoomMic]? {
        didSet {
            guard let _ = collectionView else {
                return
            }
            collectionView.reloadData()

            guard let micInfos = micInfos else { return }
            let micInfo = micInfos[4]
            rtcUserView.cellType = getCellTypeWithStatus(micInfo.status)
            rtcUserView.tag = 204
            rtcUserView.user = micInfo.member
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
        redMediaPlayer?.adjustPlayoutVolume(Int32(value * 400))
        blueMediaPlayer?.adjustPlayoutVolume(Int32(value * 400))
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
        rtcUserView.snp.makeConstraints { make in
            make.center.equalTo(self)
            make.width.height.equalTo(150~)
        }
        
        let pan = UIPanGestureRecognizer(target: self, action: #selector(pan))
        rtcUserView.addGestureRecognizer(pan)
        
        
        rtcKit?.updateSpetialPostion(position: viewCenterPostion(center: CGPoint(x: Screen.width * 0.5,
                                                                                 y: Screen.height * 0.5)),
                                     axisForward: [0, 1, 0],
                                     axisRight: [1, 0, 0],
                                     axisUp: [0, 0, 1])
    }
}

extension SA3DRtcView {
    @objc private func pan(pan: UIPanGestureRecognizer) {
        let translation = pan.translation(in: self)

        var moveCenter = CGPoint(x: rtcUserView.center.x + translation.x, y: rtcUserView.center.y + translation.y)

        // 处理边界
        if moveCenter.x <= 75~ {
            moveCenter.x = 75~
        }

        if moveCenter.y <= 75~ {
            moveCenter.y = 75~
        }

        if moveCenter.x >= bounds.size.width - 75~ {
            moveCenter.x = bounds.size.width - 75~
        }

        if moveCenter.y >= bounds.size.height - 75~ {
            moveCenter.y = bounds.size.height - 75~
        }

        rtcUserView.center = CGPoint(x: moveCenter.x, y: moveCenter.y)
        pan.setTranslation(.zero, in: self)

        let angle = getAngle(rtcUserView.center, preP: lastCenterPoint)
        rtcUserView.angle = angle - _lastPointAngle
        _lastPointAngle = angle
        lastCenterPoint = rtcUserView.center
        
        if pan.state == .ended {
            let pos = viewCenterPostion(view: rtcUserView)
            rtcKit?.updateSpetialPostion(position: pos,
                                         axisForward: [0, 1, 0],
                                         axisRight: [1, 0, 0],
                                         axisUp: [0, 0, 1])
            
            let info = SAPositionInfo()
            // TODO: 待完善uid
            info.uid = 0
            info.position = pos.map({ $0.doubleValue })
            info.forward = [0, 1, 0]
            info.x = pos.first?.doubleValue ?? 0
            info.y = pos[1].doubleValue
            info.angle = angle
            guard let streamData = JSONObject.toData(info) else { return }
            rtcKit?.sendStreamMessage(with: streamData)
        }
    }

    fileprivate func getAngle(_ curP: CGPoint, preP: CGPoint) -> Double {
        let changeX = curP.x - preP.x
        let changeY = curP.y - preP.y
        let radina = atan2(changeY, changeX)
        let angle = 180.0 / Double.pi * radina
        return (angle - 90) / 180.0 * Double.pi
    }

    fileprivate func getCurrentTimeStamp() -> CLongLong {
        // 当前时间戳
        let timestamp = Date().timeIntervalSince1970
        // 毫秒级时间戳
        let timeStamp_now = CLongLong(round(timestamp * 1000))
        return timeStamp_now
    }
    
    private func viewCenterPostion(view: UIView) -> [NSNumber] {
        let rate = frame.width / frame.height * 10
        let pos = [NSNumber(value: Double(view.center.x / rate)),
                   NSNumber(value: Double(view.center.y / rate)),
                   NSNumber(0.0)]
        return pos
    }
    private func viewCenterPostion(center: CGPoint) -> [NSNumber] {
        let rate = frame.width / frame.height * 10
        let pos = [NSNumber(value: Double(center.x) / rate),
                   NSNumber(value: Double(center.y) / rate),
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
        micInfos == nil ? 0 : 7
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
            switch indexPath.item {
            case 0:
                if let mic_info = micInfos?[0] {
                    cell.tag = 200
                    cell.setArrowInfo(imageName: "sa_downright_arrow", margin: 6)
                    cell.user = mic_info.member
                    cell.cellType = getCellTypeWithStatus(mic_info.status)
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeDown
                }
            case 1:
                if let mic_info = micInfos?[1] {
                    cell.tag = 201
                    cell.setArrowInfo(imageName: "sa_down_arrow", margin: 6)
                    cell.user = mic_info.member
                    cell.cellType = getCellTypeWithStatus(mic_info.status)
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeUp
                }
            case 2:
                if let mic_info = micInfos?[6] {
                    let user = SAUser()
                    user.name = "Agora Red"
                    user.portrait = "red"
                    cell.setArrowInfo(imageName: "sa_downleft_arrow", margin: 6)
                    cell.user = user
                    cell.cellType = mic_info.status == 5 ? .AgoraChatRoomBaseUserCellTypeAlienActive : .AgoraChatRoomBaseUserCellTypeAlienNonActive
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeDown
                    
                    setAirAbsorb(isOpen: mic_info.airAbsorb, mediaPlayer: redMediaPlayer)
                    setVoiceBlur(isOpen: mic_info.voiceBlur, mediaPlayer: redMediaPlayer)
                    setPlayerAttenuation(mediaPlayer: redMediaPlayer, attenuation: mic_info.attenuation)
                }
                
            case 4:
                if let mic_info = micInfos?[5] {
                    let user = SAUser()
                    user.name = "Agora Blue"
                    user.portrait = "blue"
                    cell.setArrowInfo(imageName: "sa_upright_arrow", margin: -6)
                    cell.user = user
                    cell.cellType = mic_info.status == 5 ? .AgoraChatRoomBaseUserCellTypeAlienActive : .AgoraChatRoomBaseUserCellTypeAlienNonActive
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeUp
                    
                    setAirAbsorb(isOpen: mic_info.airAbsorb, mediaPlayer: blueMediaPlayer)
                    setVoiceBlur(isOpen: mic_info.voiceBlur, mediaPlayer: blueMediaPlayer)
                    setPlayerAttenuation(mediaPlayer: blueMediaPlayer, attenuation: mic_info.attenuation)
                }
            case 5:
                if let mic_info = micInfos?[2] {
                    cell.tag = 202
                    cell.setArrowInfo(imageName: "sa_up_arrow", margin: -6)
                    cell.user = mic_info.member
                    cell.cellType = getCellTypeWithStatus(mic_info.status)
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeDown
                }
            case 6:
                if let mic_info = micInfos?[3] {
                    cell.tag = 203
                    cell.setArrowInfo(imageName: "sa_upleft_arrow", margin: -6)
                    cell.user = mic_info.member
                    cell.cellType = getCellTypeWithStatus(mic_info.status)
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeUp
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
        let point = rtcUserView.convert(rtcUserView.center, toViewOrWindow: collectionView)
        let pos = viewCenterPostion(center: point)
        switch indexPath.item {
        case 0:
            if let mic_info = micInfos?[0] {
                mic_info.forward = [1, -1, 0]
                mic_info.right = [-1, -1, 0]
                if mic_info.member?.uid == VLUserCenter.user.userNo {
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
                if mic_info.member?.uid == VLUserCenter.user.userNo {
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
            if let mic_info = micInfos?[5] {
                mic_info.forward = [1, 1, 0]
                mic_info.right = [1, -1, 0]
                
                setMediaPlayerPosition(pos: pos,
                                       forward: mic_info.forward,
                                       playerId: Int(blueMediaPlayer?.getMediaPlayerId() ?? 0))
            }
        case 5:
            if let mic_info = micInfos?[2] {
                mic_info.forward = [0, 1, 0]
                mic_info.right = [1, 0, 0]
                if mic_info.member?.uid == VLUserCenter.user.userNo {
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
            if let mic_info = micInfos?[3] {
                mic_info.forward = [-1, 1, 0]
                mic_info.right = [1, 1, 0]
                if mic_info.member?.uid == VLUserCenter.user.userNo {
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
        
        if indexPath.item == 2 {
            redMediaPlayer?.open("https://webdemo.agora.io/audiomixing.mp3", startPos: 0)
        } else if indexPath.item == 4 {
            blueMediaPlayer?.open("https://webdemo.agora.io/dang.mp3", startPos: 0)
        }
    }
    
    private func getCellTypeWithStatus(_ status: Int) -> SABaseUserCellType {
//        switch status {
//            case -2:
//                return .AgoraChatRoomBaseUserCellTypeAlienNonActive
//            case -1:
//                return .AgoraChatRoomBaseUserCellTypeAdd
//            case 0:
//                return .AgoraChatRoomBaseUserCellTypeNormalUser
//            case 1:
//                return .AgoraChatRoomBaseUserCellTypeMute
//            case 2:
//                return .AgoraChatRoomBaseUserCellTypeForbidden
//            case 3:
//                return .AgoraChatRoomBaseUserCellTypeLock
//            case 4:
//                return .AgoraChatRoomBaseUserCellTypeMuteAndLock
//            case 5:
//                return .AgoraChatRoomBaseUserCellTypeAlienActive
//            default:
//                return .AgoraChatRoomBaseUserCellTypeAdd
//        }
        return .AgoraChatRoomBaseUserCellTypeAdd
    }
}
extension SA3DRtcView: SAMusicPlayerDelegate {
    func didMPKChangedTo(_ playerKit: AgoraRtcMediaPlayerProtocol, state: AgoraMediaPlayerState, error: AgoraMediaPlayerError) {
        if state == .openCompleted || state == .playBackAllLoopsCompleted || state == .playBackCompleted {
            playerKit.play()
        }
    }
    
    func didReceiveStreamMsgOfUid(uid: UInt, data: Data) {
        // TODO: 待过滤自己
        let result = String(data: data, encoding: .utf8)
        guard let info = JSONObject.toModel(SAPositionInfo.self, value: result) else { return }
        
        let pos = info.position.map({ NSNumber(value: $0) })
        let forward = info.forward.map({ NSNumber(value: $0) })
        rtcKit?.updateRemoteSpetialPostion(uid: "\(uid)",
                                           position: pos,
                                           forward: forward)
        let rate = frame.width / frame.height * 10
        let x = info.x * rate
        let y = info.y * rate
        UIView.animate(withDuration: 0.25) {
            self.rtcUserView.frame.origin = CGPoint(x: x, y: y)
        }
        rtcUserView.angle = info.angle
    }
}

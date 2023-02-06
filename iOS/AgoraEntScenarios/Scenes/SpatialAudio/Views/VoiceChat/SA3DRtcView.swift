//
//  AgoraChatRoom3DRtcView.swift
//  VoiceChat4Swift
//
//  Created by CP on 2022/8/30.
//

import UIKit

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

    override init(frame: CGRect) {
        super.init(frame: frame)
        SwiftyFitsize.reference(width: 375, iPadFitMultiple: 0.6)
        layoutUI()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
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

        if getCurrentTimeStamp() - sendTS < 300 { return }
        let angle = getAngle(rtcUserView.center, preP: lastCenterPoint)
        print("angle == \(angle)")
        if abs(angle - _lastPointAngle) < 0.2 {
            return
        }
        rtcUserView.angle = angle - _lastPointAngle
        if angle == _lastPointAngle { return }
        _lastPointAngle = angle
        lastCenterPoint = rtcUserView.center
        sendTS = getCurrentTimeStamp()
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
            let pos: [NSNumber] = [NSNumber(value: Double(cell.rtcUserView.center.x)),
                                   NSNumber(value: Double(cell.rtcUserView.center.y)),
                                   0]
            switch indexPath.item {
            case 0:
                if let mic_info = micInfos?[0] {
                    mic_info.forward = [1, -1, 0]
                    mic_info.right = [-1, -1, 0]
                    cell.tag = 200
                    cell.setArrowInfo(imageName: "sa_downright_arrow", margin: 6)
                    cell.user = mic_info.member
                    cell.cellType = getCellTypeWithStatus(mic_info.status)
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeDown
                    rtcKit?.updateSpetialPostion(position: pos,
                                                 axisForward: mic_info.forward ?? [],
                                                 axisRight: mic_info.right ?? [],
                                                 axisUp: mic_info.up)
                }
            case 1:
                if let mic_info = micInfos?[1] {
                    mic_info.forward = [0, -1, 0]
                    mic_info.right = [-1, 0, 0]
                    cell.tag = 201
                    cell.setArrowInfo(imageName: "sa_down_arrow", margin: 6)
                    cell.user = mic_info.member
                    cell.cellType = getCellTypeWithStatus(mic_info.status)
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeUp
                    rtcKit?.updateSpetialPostion(position: pos,
                                                 axisForward: mic_info.forward ?? [],
                                                 axisRight: mic_info.right ?? [],
                                                 axisUp: mic_info.up)
                }
            case 2:
                if let mic_info = micInfos?[6] {
                    mic_info.forward = [-1, -1, 0]
                    mic_info.right = [-1, 1, 0]
                    let user = SAUser()
                    user.name = "Agora Red"
                    user.portrait = "red"
                    cell.setArrowInfo(imageName: "sa_downleft_arrow", margin: 6)
                    cell.user = user
                    cell.cellType = mic_info.status == 5 ? .AgoraChatRoomBaseUserCellTypeAlienActive : .AgoraChatRoomBaseUserCellTypeAlienNonActive
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeDown
                    rtcKit?.updateSpetialPostion(position: pos,
                                                 axisForward: mic_info.forward ?? [],
                                                 axisRight: mic_info.right ?? [],
                                                 axisUp: mic_info.up)
                }
            case 4:
                if let mic_info = micInfos?[5] {
                    let user = SAUser()
                    mic_info.forward = [1, 1, 0]
                    mic_info.right = [1, -1, 0]
                    user.name = "Agora Blue"
                    user.portrait = "blue"
                    cell.setArrowInfo(imageName: "sa_upright_arrow", margin: -6)
                    cell.user = user
                    cell.cellType = mic_info.status == 5 ? .AgoraChatRoomBaseUserCellTypeAlienActive : .AgoraChatRoomBaseUserCellTypeAlienNonActive
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeUp
                    rtcKit?.updateSpetialPostion(position: pos,
                                                 axisForward: mic_info.forward ?? [],
                                                 axisRight: mic_info.right ?? [],
                                                 axisUp: mic_info.up)
                }
            case 5:
                if let mic_info = micInfos?[2] {
                    mic_info.forward = [0, 1, 0]
                    mic_info.right = [1, 0, 0]
                    cell.tag = 202
                    cell.setArrowInfo(imageName: "sa_up_arrow", margin: -6)
                    cell.user = mic_info.member
                    cell.cellType = getCellTypeWithStatus(mic_info.status)
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeDown
                    rtcKit?.updateSpetialPostion(position: pos,
                                                 axisForward: mic_info.forward ?? [],
                                                 axisRight: mic_info.right ?? [],
                                                 axisUp: mic_info.up)
                }
            case 6:
                if let mic_info = micInfos?[3] {
                    mic_info.forward = [-1, 1, 0]
                    mic_info.right = [1, 1, 0]
                    cell.tag = 203
                    cell.setArrowInfo(imageName: "sa_upleft_arrow", margin: -6)
                    cell.user = mic_info.member
                    cell.cellType = getCellTypeWithStatus(mic_info.status)
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeUp
                    rtcKit?.updateSpetialPostion(position: pos,
                                                 axisForward: mic_info.forward ?? [],
                                                 axisRight: mic_info.right ?? [],
                                                 axisUp: mic_info.up)
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

    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        print("index === \(indexPath.item)")
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

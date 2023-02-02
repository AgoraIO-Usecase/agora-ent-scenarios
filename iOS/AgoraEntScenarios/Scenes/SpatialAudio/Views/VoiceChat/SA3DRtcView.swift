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

    public var micInfos: [SARoomMic]? {
        didSet {
            guard let _ = collectionView else {
                return
            }
            collectionView.reloadData()

            guard let micInfos = micInfos else { return }
            guard let micInfo: SARoomMic = micInfos[4] as? SARoomMic else { return }
            rtcUserView.cellType = getCellTypeWithStatus(micInfo.status)
            rtcUserView.tag = 204
            guard let member = micInfo.member else { return }

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
            make.left.top.bottom.right.equalTo(self)
        }

        addSubview(rtcUserView)
        rtcUserView.snp.makeConstraints { make in
            make.center.equalTo(self)
            make.width.height.equalTo(150~)
        }

        isUserInteractionEnabled = true

        let tap = UITapGestureRecognizer(target: self, action: #selector(taptap))
        addGestureRecognizer(tap)

        let pan = UIPanGestureRecognizer(target: self, action: #selector(pan))
        addGestureRecognizer(pan)
    }
}

extension SA3DRtcView {
    @objc private func taptap(tap: UIGestureRecognizer) {
        var location = tap.location(in: self)

        // 处理边界
        if location.x <= 75~ {
            location.x = 75~
        }

        if location.y <= 75~ {
            location.y = 75~
        }

        if location.x >= bounds.size.width - 75~ {
            location.x = bounds.size.width - 75~
        }

        if location.y >= bounds.size.height - 75~ {
            location.y = bounds.size.height - 75~
        }

        let angle = getAngle(location, preP: lastCenterPoint)
        rtcUserView.angle = angle - _lastPointAngle
        UIView.animate(withDuration: 3, delay: 0) { [self] in
            self.rtcUserView.center = CGPoint(x: location.x, y: location.y)
        }

        if angle == _lastPointAngle { return }
        _lastPointAngle = angle
        lastCenterPoint = rtcUserView.center
    }

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
        return (angle + 90) / 180.0 * Double.pi
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
        return 7
    }

    func numberOfSections(in collectionView: UICollectionView) -> Int {
        return 1
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
            let cell: SA3DUserCollectionViewCell = collectionView.dequeueReusableCell(withReuseIdentifier: vIdentifier, for: indexPath) as! SA3DUserCollectionViewCell
            switch indexPath.item {
            case 0:
                if let mic_info = micInfos?[0] {
                    cell.tag = 200
                    cell.user = mic_info.member
                    cell.cellType = getCellTypeWithStatus(mic_info.status)
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeDown
                }
            case 1:
                if let mic_info = micInfos?[1] {
                    cell.tag = 201
                    cell.user = mic_info.member
                    cell.cellType = getCellTypeWithStatus(mic_info.status)
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeUp
                }
            case 2:
                if let mic_info = micInfos?[5] {
                    let user = SAUser()
                    user.name = "Agora Blue"
                    user.portrait = "blue"
                    cell.user = user
                    cell.cellType = getCellTypeWithStatus(mic_info.status)
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeDown
                }
            case 4:
                if let mic_info = micInfos?[6] {
                    let user = SAUser()
                    user.name = "Agora Red"
                    user.portrait = "red"
                    cell.user = user
                    cell.cellType = getCellTypeWithStatus(mic_info.status)
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeUp
                }
            case 5:
                if let mic_info = micInfos?[2] {
                    cell.tag = 202
                    cell.user = mic_info.member
                    cell.cellType = getCellTypeWithStatus(mic_info.status)
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeDown
                }
            case 6:
                if let mic_info = micInfos?[3] {
                    cell.tag = 203
                    cell.user = mic_info.member
                    cell.cellType = getCellTypeWithStatus(mic_info.status)
                    cell.directionType = .AgoraChatRoom3DUserDirectionTypeUp
                }
            default:
                break
            }

            if indexPath.item == 2 || indexPath.item == 4 {
                cell.activeBlock = { [weak self] type in
                    guard let activeBlock = self?.activeBlock else {
                        return
                    }
                    activeBlock(type)
                }
            } else {
                cell.clickBlock = { [weak self] in
                    guard let clickBlock = self?.clickBlock else { return }
                    clickBlock()
                }
            }
            return cell
        } else {
            let cell: UICollectionViewCell = collectionView.dequeueReusableCell(withReuseIdentifier: nIdentifier, for: indexPath)
            return cell
        }
    }

    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {}

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

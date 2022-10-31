//
//  AgoraChatRoom3DUserCollectionViewCell.swift
//  VoiceChat4Swift
//
//  Created by CP on 2022/8/31.
//

import UIKit

public enum AgoraChatRoom3DUserDirectionType {
    case AgoraChatRoom3DUserDirectionTypeUp
    case AgoraChatRoom3DUserDirectionTypeDown
}

class AgoraChatRoom3DUserCollectionViewCell: UICollectionViewCell {
    private var rtcUserView: AgoraChatRoomBaseRtcUserView = .init()

    public var cellType: AgoraChatRoomBaseUserCellType = .AgoraChatRoomBaseUserCellTypeAdd {
        didSet {
            rtcUserView.cellType = cellType
        }
    }

    public var directionType: AgoraChatRoom3DUserDirectionType = .AgoraChatRoom3DUserDirectionTypeDown {
        didSet {
            rtcUserView.snp.updateConstraints { make in
                make.top.equalTo(self.contentView).offset(directionType == .AgoraChatRoom3DUserDirectionTypeUp ? 0~ : 40~)
            }
        }
    }

    public var user: VRUser? {
        didSet {
            rtcUserView.iconImgUrl = user?.portrait ?? ""
            rtcUserView.nameStr = user?.name ?? "\(tag - 200)"
            rtcUserView.volume = user?.volume ?? 0
        }
    }

    public var clickBlock: (() -> Void)?
    public var activeBlock: ((AgoraChatRoomBaseUserCellType) -> Void)?

    override init(frame: CGRect) {
        super.init(frame: frame)
        SwiftyFitsize.reference(width: 375, iPadFitMultiple: 0.6)
        layoutUI()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    fileprivate func layoutUI() {
        contentView.addSubview(rtcUserView)

        rtcUserView.snp.makeConstraints { make in
            make.left.right.bottom.equalTo(self.contentView)
            make.top.equalTo(self.contentView).offset(-20~)
        }

        rtcUserView.clickBlock = { [weak self] in
            guard let clickBlock = self?.clickBlock else { return }
            clickBlock()
        }

//        rtcUserView.activeVBlock = {[weak self] type in
//            guard let activeBlock = self?.activeBlock else {
//                return
//            }
//            activeBlock(type)
//        }
    }
}

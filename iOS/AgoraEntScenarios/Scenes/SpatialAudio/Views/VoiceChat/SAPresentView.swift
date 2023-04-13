//
//  VMPresentView.swift
//  AgoraScene_iOS
//
//  Created by CP on 2022/9/8.
//

import SnapKit
import UIKit
class SAVMPresentView: UIView {
    private var screenSize: CGSize = UIScreen.main.bounds.size
    private var scrollView: UIScrollView = .init()
    private var audioSetView: SAAudioSettingView = .init()
    public var eqView: SAEQSettingView = .init()
    public var roomInfo: SARoomInfo?
    public var isAudience: Bool = false
    public var isTouchAble: Bool = false {
        didSet {
            eqView.isTouchAble = isTouchAble
        }
    }

    var selBlock: ((SARtcType.AINS_STATE) -> Void)?
    var ains_state: SARtcType.AINS_STATE = .mid
    var useRobotBlock: ((Bool) -> Void)?
    var turnAIAECBlock: ((Bool) -> Void)?
    var turnAGCBlock: ((Bool) -> Void)?

    var volBlock: ((Int) -> Void)?

    override func draw(_ rect: CGRect) {
        super.draw(rect)
        backgroundColor = .white
        layoutUI()
    }

    private func layoutUI() {
        let path = UIBezierPath(roundedRect: bounds, byRoundingCorners: [.topLeft, .topRight], cornerRadii: CGSize(width: 20.0, height: 20.0))
        let layer = CAShapeLayer()
        layer.path = path.cgPath
        self.layer.mask = layer

        scrollView.contentSize = CGSize(width: screenSize.width * 2, height: 0)
        scrollView.frame = CGRect(x: 0, y: 0, width: screenSize.width, height: bounds.size.height)
        addSubview(scrollView)
        scrollView.isScrollEnabled = false

        audioSetView.backgroundColor = .white
        audioSetView.frame = CGRect(x: 0, y: 0, width: screenSize.width, height: bounds.size.height)
        audioSetView.roomInfo = roomInfo
        audioSetView.isAudience = isAudience
        audioSetView.ains_state = ains_state
        audioSetView.resBlock = { [weak self] type in
            self?.eqView.settingType = type
            self?.eqView.ains_state = self!.ains_state
            self?.scrollView.setContentOffset(CGPoint(x: (self?.screenSize.width)!, y: 0), animated: true)
        }
        audioSetView.useRobotBlock = { [weak self] flag in
            guard let useRobotBlock = self?.useRobotBlock else { return }

            useRobotBlock(flag)
        }
        audioSetView.volBlock = { [weak self] vol in
            guard let volBlock = self?.volBlock else { return }
            volBlock(vol)
        }
        scrollView.addSubview(audioSetView)

        eqView.backgroundColor = .white
        eqView.frame = CGRect(x: screenSize.width, y: 0, width: screenSize.width, height: bounds.size.height)
        eqView.roomInfo = roomInfo
        eqView.isTouchAble = isTouchAble
        eqView.isAudience = isAudience
        eqView.soundEffect = roomInfo?.room?.sound_effect ?? 1
        eqView.resBlock = { [weak self] type in
            self?.eqView.settingType = type
        }
        eqView.backBlock = { [weak self] in
            self?.scrollView.setContentOffset(CGPoint(x: 0, y: 0), animated: true)
            self?.scrollView.isScrollEnabled = false
        }
        eqView.selBlock = { [weak self] state in
            guard let selBlock = self?.selBlock else {
                return
            }
            self?.ains_state = state
            self?.audioSetView.ains_state = state
            selBlock(state)
        }
        eqView.turnAIAECBlock = {[weak self] flag in
            guard let turnAIAECBlock = self?.turnAIAECBlock else {return}
                    
            turnAIAECBlock(flag);
            self?.roomInfo?.room?.turn_AIAEC = flag;
            self?.audioSetView.tableView.reloadData()
        }
        eqView.turnAGCBlock = {[weak self] flag in
            guard let turnAGCBlock = self?.turnAGCBlock else {return}
                    
            turnAGCBlock(flag);
            self?.roomInfo?.room?.turn_AGC = flag;
            self?.audioSetView.tableView.reloadData()
        }

        scrollView.addSubview(eqView)
    }
}

//
//  VoiceRoomChatBar.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/31.
//

import UIKit
import ZSwiftBaseLib

@objc public enum VoiceRoomChatBarStyle: Int {
    case normal = 0
    case spatialAudio = 1
}

@objc public enum VoiceRoomChatBarEvents: Int {
    case mic = 0
    case handsUp = 1
    case eq = 2
    case gift = 3
}

@objc public enum VoiceRoomChatBarState: Int {
    case unSelected = 1
    case selected = 2
    case disable = 3
}

public class VoiceRoomChatBar: UIView, UICollectionViewDelegate, UICollectionViewDataSource {
    public var events: ((VoiceRoomChatBarEvents) -> Void)?

    public var creator = false

    var handsState: VoiceRoomChatBarState = .unSelected

    var micState = false

    public var raiseKeyboard: (() -> Void)?

    public var datas = ["mic", "handuphard", "eq", "sendgift"]

    @UserDefault("EQShowTips", defaultValue: true) var eqShow

    lazy var chatRaiser: UIButton = .init(type: .custom).frame(CGRect(x: 15, y: 5, width: (110 / 375.0) * ScreenWidth, height: self.frame.height - 10)).backgroundColor(UIColor(red: 0, green: 0, blue: 0, alpha: 0.2)).cornerRadius((self.frame.height - 10) / 2.0).font(.systemFont(ofSize: 12, weight: .regular)).textColor(UIColor(white: 1, alpha: 0.8), .normal).addTargetFor(self, action: #selector(raiseAction), for: .touchUpInside)

    lazy var flowLayout: UICollectionViewFlowLayout = {
        let layout = UICollectionViewFlowLayout()
        layout.itemSize = CGSize(width: self.frame.height - 10, height: self.frame.height - 10)
        layout.minimumInteritemSpacing = 8
        layout.scrollDirection = .horizontal
        return layout
    }()

    public lazy var toolBar: UICollectionView = .init(frame: CGRect(x: self.frame.width - (40 * CGFloat(self.datas.count)) - (CGFloat(self.datas.count) - 1) * 10 - 15, y: 0, width: 40 * CGFloat(self.datas.count) + (CGFloat(self.datas.count) - 1) * 10, height: self.frame.height), collectionViewLayout: self.flowLayout).delegate(self).dataSource(self).backgroundColor(.clear).registerCell(VoiceRoomChatBarCell.self, forCellReuseIdentifier: "VoiceRoomChatBarCell").showsVerticalScrollIndicator(false).showsHorizontalScrollIndicator(false)

    override public init(frame: CGRect) {
        super.init(frame: frame)
    }

    public convenience init(frame: CGRect, style: VoiceRoomChatBarStyle) {
        self.init(frame: frame)
        if style == .normal {
            chatRaiser.isHidden = false
            datas = ["mic", "handuphard", "eq", "sendgift"]
        } else {
            chatRaiser.isHidden = true
            datas = ["mic", "handuphard", "eq"]
        }
        addSubViews([chatRaiser, toolBar])
        chatRaiser.setImage(UIImage("chatraise"), for: .normal)
        chatRaiser.setTitle(" " + "Let's Chat!".localized(), for: .normal)
        chatRaiser.titleEdgeInsets = UIEdgeInsets(top: chatRaiser.titleEdgeInsets.top, left: 10, bottom: chatRaiser.titleEdgeInsets.bottom, right: 10)
        chatRaiser.imageEdgeInsets = UIEdgeInsets(top: 5, left: 10, bottom: 5, right: 80)
        chatRaiser.contentHorizontalAlignment = .left
        if eqShow {
            eqShow = false
            let pop = PopTip().tag(191).backgroundColor(UIColor(0x0CA5FD))
            pop.bubbleColor = UIColor(0x0CA5FD)
            pop.shadowColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.12)
            pop.shadowOpacity = 1
            pop.shadowRadius = 8
            pop.shadowOffset = CGSize(width: 0, height: 0)
            pop.cornerRadius = 12
            pop.shouldConsiderCutoutTapSeparately = true
            DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
                pop.show(customView: UILabel(frame: CGRect(x: 0, y: 0, width: 140, height: 31)).text(LanguageManager.localValue(key: "Try Best Agora Sound")).font(.systemFont(ofSize: 12, weight: .regular)).textAlignment(.center).backgroundColor(UIColor(0x0CA5FD)).textColor(.white), direction: .up, in: self, from: CGRect(x: self.frame.width - (style == .normal ? 110 : 55), y: self.toolBar.visibleCells[2].frame.origin.y, width: self.toolBar.visibleCells[2].frame.width, height: self.toolBar.visibleCells[2].frame.height))
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 6) {
                pop.hide()
            }
        }
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

extension VoiceRoomChatBar {
    @objc func raiseAction() {
        if raiseKeyboard != nil {
            raiseKeyboard!()
        }
    }

    @objc func refresh(event: VoiceRoomChatBarEvents, state: VoiceRoomChatBarState, asCreator: Bool) {
        creator = asCreator
        switch event {
        case .mic:
            refreshMicState(state: state)
            toolBar.reloadItems(at: [IndexPath(row: 0, section: 0)])
        case .handsUp:
            handsState = state
            var idx = 0
            sinkIndex(idx: &idx)
            if !asCreator {
                refreshHandsState(state: state, idx: idx)
            } else {
                switch state {
                case .unSelected: datas[idx] = "handuphard"
                default: datas[idx] = "handuphard"
                }
            }
            toolBar.reloadItems(at: [IndexPath(row: idx, section: 0)])
        default: break
        }
    }

    private func sinkIndex(idx: inout Int) {
        for (index, element) in datas.enumerated() {
            if element == "handuphard" || element == "handup_dot" || element == "handuphard-1" {
                idx = index
                break
            }
        }
    }

    private func refreshMicState(state: VoiceRoomChatBarState) {
        micState = state == .selected ? true : false
        switch state {
        case .unSelected:
            datas[0] = "mic"
        case .selected:
            datas[0] = "unmic"
        case .disable:
            break
        }
    }

    private func refreshHandsState(state: VoiceRoomChatBarState, idx: Int) {
        switch state {
        case .unSelected:
            datas[idx] = "handuphard"
        case .selected:
            datas[idx] = "handup_dot"
        case .disable:
            datas[idx] = "handuphard-1"
        }
    }

    public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        datas.count
    }

    public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "VoiceRoomChatBarCell", for: indexPath) as? VoiceRoomChatBarCell
        cell?.icon.image = UIImage(datas[indexPath.row])
        if indexPath.row == 1, creator, handsState != .selected {
            cell?.redDot.isHidden = false
        } else {
            cell?.redDot.isHidden = true
        }
        return cell ?? VoiceRoomChatBarCell()
    }

    public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        collectionView.deselectItem(at: indexPath, animated: true)
        if events != nil {
            if indexPath.row == 1, handsState != .disable {
                events!(VoiceRoomChatBarEvents(rawValue: indexPath.row)!)
            } else {
                events!(VoiceRoomChatBarEvents(rawValue: indexPath.row)!)
            }
        }
    }
}

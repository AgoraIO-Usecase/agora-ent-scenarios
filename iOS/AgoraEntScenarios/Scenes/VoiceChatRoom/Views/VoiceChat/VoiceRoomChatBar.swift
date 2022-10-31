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

public class VoiceRoomChatBar: UIView,UICollectionViewDelegate,UICollectionViewDataSource {
    
    public var events: ((VoiceRoomChatBarEvents) -> ())?
    
    public var creator = false
    
    var handsState: VoiceRoomChatBarState = .unSelected
    
    var micState = false
    
    public var raiseKeyboard: (() -> ())?
    
    public var datas = ["mic","handuphard","eq","sendgift"]
    
    @UserDefault("EQShowTips", defaultValue: true) var eqShow
    
    lazy var chatRaiser: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: 15, y: 5, width: (110/375.0)*ScreenWidth, height: self.frame.height-10)).backgroundColor(UIColor(red: 0, green: 0, blue: 0, alpha: 0.2)).cornerRadius((self.frame.height-10)/2.0).font(.systemFont(ofSize: 12, weight: .regular)).textColor(UIColor(white: 1, alpha: 0.8), .normal).addTargetFor(self, action: #selector(raiseAction), for: .touchUpInside)
    }()
    
    lazy var flowLayout: UICollectionViewFlowLayout = {
        let layout = UICollectionViewFlowLayout()
        layout.itemSize = CGSize(width: self.frame.height - 10, height: self.frame.height - 10)
        layout.minimumInteritemSpacing = 8
        layout.scrollDirection = .horizontal
        return layout
    }()
    
    public lazy var toolBar: UICollectionView = {
        UICollectionView(frame: CGRect(x: self.frame.width - (40 * CGFloat(self.datas.count)) - (CGFloat(self.datas.count) - 1)*10 - 15, y: 0, width: 40*(CGFloat(self.datas.count))+(CGFloat(self.datas.count) - 1)*10, height: self.frame.height), collectionViewLayout: self.flowLayout).delegate(self).dataSource(self).backgroundColor(.clear).registerCell(VoiceRoomChatBarCell.self, forCellReuseIdentifier: "VoiceRoomChatBarCell").showsVerticalScrollIndicator(false).showsHorizontalScrollIndicator(false)
    }()
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    public convenience init(frame: CGRect,style: VoiceRoomChatBarStyle) {
        self.init(frame: frame)
        if style == .normal {
            self.chatRaiser.isHidden = false
            self.datas = ["mic","handuphard","eq","sendgift"]
        } else {
            self.chatRaiser.isHidden = true
            self.datas = ["mic","handuphard","eq"]
        }
        self.addSubViews([self.chatRaiser,self.toolBar])
        self.chatRaiser.setImage(UIImage("chatraise"), for: .normal)
        self.chatRaiser.setTitle(" "+"Let's Chat!".localized(), for: .normal)
        self.chatRaiser.titleEdgeInsets = UIEdgeInsets(top: self.chatRaiser.titleEdgeInsets.top, left: 10, bottom: self.chatRaiser.titleEdgeInsets.bottom, right: 10)
        self.chatRaiser.imageEdgeInsets = UIEdgeInsets(top: 5, left: 10, bottom: 5, right: 80)
        self.chatRaiser.contentHorizontalAlignment = .left
        if self.eqShow {
            self.eqShow = false
            let pop = PopTip().tag(191).backgroundColor(UIColor(0x0CA5FD))
            pop.bubbleColor = UIColor(0x0CA5FD)
            pop.shadowColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.12)
            pop.shadowOpacity = 1
            pop.shadowRadius = 8
            pop.shadowOffset = CGSize(width: 0, height: 0)
            pop.cornerRadius = 12
            pop.shouldConsiderCutoutTapSeparately = true
            DispatchQueue.main.asyncAfter(deadline: .now()+1) {
                pop.show(customView: UILabel(frame: CGRect(x: 0, y: 0, width: 140, height: 31)).text(LanguageManager.localValue(key: "Try Best Agora Sound")).font(.systemFont(ofSize: 12, weight: .regular)).textAlignment(.center).backgroundColor(UIColor(0x0CA5FD)).textColor(.white), direction: .up, in: self, from: CGRect(x: self.frame.width-(style == .normal ? 110:55), y: self.toolBar.visibleCells[2].frame.origin.y, width: self.toolBar.visibleCells[2].frame.width, height: self.toolBar.visibleCells[2].frame.height))
            }
            DispatchQueue.main.asyncAfter(deadline: .now()+6) {
                pop.hide()
            }
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
}

extension VoiceRoomChatBar {
    
    @objc func raiseAction() {
        if self.raiseKeyboard != nil {
            self.raiseKeyboard!()
        }
    }
    
    @objc func refresh(event: VoiceRoomChatBarEvents,state: VoiceRoomChatBarState,asCreator: Bool) {
        self.creator = asCreator
        switch event {
        case .mic:
            self.refreshMicState(state: state)
            self.toolBar.reloadItems(at: [IndexPath(row: 0, section: 0)])
        case .handsUp:
            self.handsState = state
            var idx = 0
            self.sinkIndex(idx: &idx)
            if !asCreator {
                self.refreshHandsState(state: state,idx: idx)
            } else {
                switch state {
                case .unSelected: self.datas[idx] = "handuphard"
                default: self.datas[idx] = "handuphard"
                }
            }
            self.toolBar.reloadItems(at: [IndexPath(row: idx, section: 0)])
        default: break
        }
    }
    
    private func sinkIndex(idx: inout Int) {
        for (index,element) in self.datas.enumerated() {
            if element == "handuphard" || element == "handup_dot" || element == "handuphard-1" {
                idx = index
                break
            }
        }
    }
    
    private func refreshMicState(state: VoiceRoomChatBarState) {
        self.micState = state == .selected ? true:false
        switch state {
        case .unSelected:
            self.datas[0] = "mic"
        case .selected:
            self.datas[0] = "unmic"
        case .disable:
            break
        }
    }
    
    private func refreshHandsState(state: VoiceRoomChatBarState,idx: Int) {
        switch state {
        case .unSelected:
            self.datas[idx] = "handuphard"
        case .selected:
            self.datas[idx] = "handup_dot"
        case .disable:
            self.datas[idx] = "handuphard-1"
        }
    }
    
    public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        self.datas.count
    }
    
    public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "VoiceRoomChatBarCell", for: indexPath) as? VoiceRoomChatBarCell
        cell?.icon.image = UIImage(self.datas[indexPath.row])
        if indexPath.row == 1,self.creator,self.handsState != .selected {
            cell?.redDot.isHidden = false
        } else {
            cell?.redDot.isHidden = true
        }
        return cell ?? VoiceRoomChatBarCell()
    }
    
    public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        collectionView.deselectItem(at: indexPath, animated: true)
        if self.events != nil {
            if indexPath.row == 1,self.handsState != .disable {
                self.events!(VoiceRoomChatBarEvents(rawValue: indexPath.row)!)
            } else {
                self.events!(VoiceRoomChatBarEvents(rawValue: indexPath.row)!)
            }
        }
    }
}

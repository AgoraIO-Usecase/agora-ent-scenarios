//
//  VRCreateRoomView.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/25.
//

import UIKit
import ZSwiftBaseLib

public class VRCreateRoomView: UIView, HorizontalCardsDelegate, HorizontalCardsDataSource {
    private let datas = [["title": LanguageManager.localValue(key: "Chat Room"), "detail": LanguageManager.localValue(key: "Multi-audio chat scenario where anyone can unmute their mic and speak") + "\n" + LanguageManager.localValue(key: "Co-Watch / Team Chat / Gaming Buddy Chat"), "image": "chat_room"]]
//    ,["title":LanguageManager.localValue(key: "Spatial Audio Mode Room"),"detail":LanguageManager.localValue(key: "Power natural conversations that make people feel like they're 'in the room' together"),"image":"sa_mode"]

    var velocity = CGPoint.zero

    /// 0 normal 1 sp
    var idx = 0

    let cardHeight = (200 / 315.0) * (ScreenWidth - 60)

    var lastOffset: CGPoint = .zero

    var createAction: (() -> Void)?

    lazy var menuBar: VRRoomMenuBar = .init(frame: CGRect(x: 20, y: 0, width: ScreenWidth - 40, height: 42), items: VRRoomMenuBar.entities1, indicatorImage: UIImage("indicator")!, indicatorFrame: CGRect(x: 0, y: 42 - 8, width: 14, height: 8)).backgroundColor(.clear)

    lazy var audioEffectCards: HorizontalCardsView = {
        let cards = HorizontalCardsView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: self.cardHeight))
        cards.dataSource = self
        cards.delegate = self
        cards.cardSpacing = 20
        cards.insets = UIEdgeInsets(top: 10, left: 30, bottom: 10, right: 30)
        return cards
    }()

    lazy var roomInput: VRCreateRoomInputView = .init(frame: CGRect(x: 0, y: self.audioEffectCards.frame.maxY + 15, width: ScreenWidth, height: self.frame.height - self.audioEffectCards.frame.maxY - 30))

    override public init(frame: CGRect) {
        super.init(frame: frame)
        addSubViews([audioEffectCards, roomInput])
        roomInput.randomName.addTarget(self, action: #selector(randomRoomName), for: .touchUpInside)
        roomInput.oldCenter = center
        menuBar.selectClosure = { [weak self] in
            self?.idx = $0.row
            self?.audioEffectCards.collectionView.scrollToItem(at: $0, at: .centeredHorizontally, animated: true)
            self?.refreshBottom(index: $0.row)
            self?.randomRoomName()
        }
        roomInput.action = { [weak self] in
            self?.create()
        }
        randomRoomName()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

public extension VRCreateRoomView {
    private func refreshBottom(index: Int) {
        if index > 0 {
            roomInput.create.setTitle(LanguageManager.localValue(key: "Go Live"), for: .normal)
        } else {
            roomInput.create.setTitle(LanguageManager.localValue(key: "Next"), for: .normal)
        }
    }

    @objc private func randomRoomName() {
        var namePrefix = LanguageManager.localValue(key: "Chat Room")
        if idx == 1 {
            namePrefix = LanguageManager.localValue(key: "Spatial Audio Mode Room")
        }
        roomInput.roomNameField.text = namePrefix + "-" + Date().z.dateString("MMdd") + "-\((1...100).randomElement() ?? 1)"
        roomInput.name = roomInput.roomNameField.text ?? ""
    }

    private func create() {
        if roomInput.privateChoice.isSelected != true {
            if createAction != nil {
                createAction!()
            }
        } else {
            if roomInput.code.count >= 4 {
                if createAction != nil {
                    createAction!()
                }
            } else {
                makeToast("4 Digit Password Required", point: center, title: nil, image: nil, completion: nil)
            }
        }
    }

    func horizontalCardsView(_: HorizontalCardsView, scrollIndex: Int) {
        idx = scrollIndex
//        self.menuBar.refreshSelected(indexPath: IndexPath(row: scrollIndex, section: 0))
        refreshBottom(index: scrollIndex)
        randomRoomName()
    }

    func horizontalCardsView(_: HorizontalCardsView, didSelectItemAtIndex index: Int) {}

    func horizontalCardsViewNumberOfItems(_: HorizontalCardsView) -> Int {
        datas.count
    }

    func horizontalCardsView(_: HorizontalCardsView, viewForIndex index: Int) -> HorizontalCardView {
        let card = HorizontalCardView(frame: CGRect(x: 0, y: 0, width: ScreenWidth - 32, height: cardHeight)).backgroundColor(.clear).cornerRadius(25)
        guard let title = datas[index]["title"], let detail = datas[index]["detail"], let image = UIImage(datas[index]["image"]!) else { return card }
        return VRSoundTypeCard(frame: CGRect(x: 0, y: 0, width: ScreenWidth - 40, height: cardHeight), title: title, note: detail, background: image).cornerRadius(25)
    }

    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        roomInput.endEditing(true)
    }
}

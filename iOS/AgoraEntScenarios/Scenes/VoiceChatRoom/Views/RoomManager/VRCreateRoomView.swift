//
//  VRCreateRoomView.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/25.
//

import UIKit
import ZSwiftBaseLib

public class VRCreateRoomView: UIView, HorizontalCardsDelegate, HorizontalCardsDataSource {
    private let datas = [["title": LanguageManager.localValue(key: "voice_chat_room"), "detail": LanguageManager.localValue(key: "voice_multi_audio_chat_scenario_where_anyone_can_unmute_their_mic_and_speak") + "\n" + LanguageManager.localValue(key: "voice_co_Watch_team_chat_gaming_buddy_chat"), "image": "chat_room"]]
//    ,["title":LanguageManager.localValue(key: "voice_spatial_audio_mode_room"),"detail":LanguageManager.localValue(key: "Power natural conversations that make people feel like they're 'in the room' together"),"image":"sa_mode"]

    var velocity = CGPoint.zero

    /// 0 normal 1 sp
    var idx = 0

    let cardHeight = (200 / 315.0) * (ScreenWidth - 60)

    var lastOffset: CGPoint = .zero

   // var createAction: (() -> Void)?

    var warningView: UIView!
    
    lazy var menuBar: VRRoomMenuBar = .init(frame: CGRect(x: 20, y: 0, width: ScreenWidth - 40, height: 42), items: VRRoomMenuBar.entities1, indicatorImage:UIImage.sceneImage(name: "indicator", bundleName: "VoiceChatRoomResource")!, indicatorFrame: CGRect(x: 0, y: 42 - 8, width: 14, height: 8)).backgroundColor(.clear)

    lazy var audioEffectCards: HorizontalCardsView = {
        let cards = HorizontalCardsView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: self.cardHeight))
        cards.dataSource = self
        cards.delegate = self
        cards.cardSpacing = 20
        cards.insets = UIEdgeInsets(top: 10, left: 30, bottom: 10, right: 30)
        return cards
    }()

    //lazy var roomInput: VRCreateRoomInputView = .init(frame: CGRect(x: 0, y: self.audioEffectCards.frame.maxY + 15, width: ScreenWidth, height: self.frame.height - self.audioEffectCards.frame.maxY - 30))
    lazy var roomInput: VRCreateRoomInputView = .init(frame: .zero)
    var createBtn: UIButton = UIButton()
    override public init(frame: CGRect) {
        super.init(frame: frame)
        //addSubViews([audioEffectCards, roomInput])
        
        addWarningView()
        roomInput.frame = CGRect(x: 0, y: self.warningView.frame.maxY + 10, width: ScreenWidth, height:  200)
        addSubViews([roomInput, createBtn])
        
        createBtn.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            createBtn.leadingAnchor.constraint(equalTo: self.leadingAnchor, constant: 30),
            createBtn.trailingAnchor.constraint(equalTo: self.trailingAnchor, constant: -30),
            createBtn.heightAnchor.constraint(equalToConstant: 48),
            createBtn.topAnchor.constraint(equalTo: self.bottomAnchor, constant: -70)
        ])
        
        createBtn.layer.shadowColor = UIColor(red: 0, green: 0.546, blue: 0.979, alpha: 0.2).cgColor
        createBtn.layer.shadowOpacity = 1
        createBtn.layer.shadowRadius = 8
        createBtn.layer.shadowOffset = CGSize(width: 0, height: 4)
        
        createBtn.cornerRadius(24)
        createBtn.setBackgroundImage(UIImage.sceneImage(name: "createRoom", bundleName: "VoiceChatRoomResource"), for: .normal)
        createBtn.addTarget(self, action: #selector(createAction), for: .touchUpInside)
        
        roomInput.randomName.addTarget(self, action: #selector(randomRoomName), for: .touchUpInside)
        roomInput.oldCenter = center
        menuBar.selectClosure = { [weak self] in
            self?.idx = $0.row
            self?.audioEffectCards.collectionView.scrollToItem(at: $0, at: .centeredHorizontally, animated: true)
            self?.refreshBottom(index: $0.row)
            self?.randomRoomName()
        }
//        roomInput.action = { [weak self] in
//            self?.create()
//        }
        roomInput.privateBlock = {[weak self] flag in
            guard let self = self else {return}
            let height:CGFloat = flag ? 450 : 350
            VRCreateRoomPresentView.shared.update(height)
            self.roomInput.frame = CGRect(x: 0, y: self.bounds.height - height, width: ScreenWidth, height:  height)
        }
        randomRoomName()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc private func createAction() {
        
    }
    
    private func addWarningView() {
        let text = LanguageManager.localValue(key: "voice_create_tips")
        let font = UIFont.systemFont(ofSize: 12)
        let constraintSize = CGSize(width: self.width - 40, height: CGFloat.greatestFiniteMagnitude)
        let attributes = [NSAttributedString.Key.font: font]
        let textRect = text.boundingRect(with: constraintSize,
                                         options: [.usesLineFragmentOrigin, .usesFontLeading],
                                         attributes: attributes,
                                         context: nil)
        let textHeight = ceil(textRect.height)

        self.warningView = UIView(frame: CGRect(x: 10, y: 10, width: self.width - 20, height: textHeight + 10))
        self.warningView.backgroundColor = UIColor(hexString: "#FA396A1A")
        self.warningView.layer.cornerRadius = 5
        self.warningView.layer.masksToBounds = true
        self.addSubview(self.warningView)
        
        let warImgView = UIImageView(frame: CGRect(x: 10, y: 5, width: 14, height: 14))
        warImgView.image = UIImage.sceneImage(name: "zhuyi", bundleName: "VoiceChatRoomResource")
        self.warningView.addSubview(warImgView)

        let contentLabel = UILabel(frame: CGRect(x: 30, y: 5, width: self.warningView.width - 40, height: textHeight))
        contentLabel.numberOfLines = 0
        let attributedText = NSMutableAttributedString(string: text)
        attributedText.addAttributes([.foregroundColor: UIColor.black], range: NSRange(location: 0, length: 77))
        attributedText.addAttributes([.foregroundColor: UIColor.red], range: NSRange(location: 77, length: 41))
        contentLabel.font = UIFont.systemFont(ofSize: 12)
        contentLabel.attributedText = attributedText
        self.warningView.addSubview(contentLabel)
    }
}

public extension VRCreateRoomView {
    private func refreshBottom(index: Int) {
        if index > 0 {
          //  roomInput.createBtn.setTitle(LanguageManager.localValue(key: "voice_go_live"), for: .normal)
        } else {
          //  roomInput.createBtn.setTitle(LanguageManager.localValue(key: "voice_next"), for: .normal)
        }
    }

    @objc private func randomRoomName() {
        var namePrefix = LanguageManager.localValue(key: "voice_chat_room")
        if idx == 1 {
            namePrefix = LanguageManager.localValue(key: "voice_spatial_audio_mode_room")
        }
        roomInput.roomNameField.text = namePrefix + "-" + Date().z.dateString("MMdd") + "-\((1...100).randomElement() ?? 1)"
        roomInput.name = roomInput.roomNameField.text ?? ""
    }

//    private func create() {
//        if roomInput.privateChoice.isSelected != true {
//            if createAction != nil {
//                createAction!()
//            }
//        } else {
//            if roomInput.code.count >= 4 {
//                if createAction != nil {
//                    createAction!()
//                }
//            } else {
//                makeToast("voice_4_digit_password_required".voice_localized(), point: center, title: nil, image: nil, completion: nil)
//            }
//        }
//    }

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
        guard let title = datas[index]["title"], let detail = datas[index]["detail"], let image = UIImage.voice_image(datas[index]["image"]!) else { return card }
        return VRSoundTypeCard(frame: CGRect(x: 0, y: 0, width: ScreenWidth - 40, height: cardHeight), title: title, note: detail, background: image).cornerRadius(25)
    }

    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        roomInput.endEditing(true)
    }
}

//
//  VoiceRoomEmojiList.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/9/5.
//

import UIKit
import ZSwiftBaseLib

public class VoiceRoomEmojiListView: UIView, UICollectionViewDelegate, UICollectionViewDataSource {
    @objc public var deleteClosure: (() -> Void)?

    @objc public var emojiClosure: ((String) -> Void)?

    lazy var flowLayout: UICollectionViewFlowLayout = {
        let layout = UICollectionViewFlowLayout()
        layout.itemSize = CGSize(width: (ScreenWidth - 20 - 60) / 7.0, height: (ScreenWidth - 20 - 60) / 7.0)
        layout.sectionInset = UIEdgeInsets(top: 10, left: 10, bottom: 10, right: 10)
        layout.minimumLineSpacing = 10
        layout.minimumInteritemSpacing = 10
        return layout
    }()

    lazy var emojiList: UICollectionView = .init(frame: CGRect(x: 0, y: 10, width: ScreenWidth, height: self.frame.height - 10), collectionViewLayout: self.flowLayout).registerCell(VoiceRoomEmojiCell.self, forCellReuseIdentifier: "VoiceRoomEmojiCell").dataSource(self).delegate(self).backgroundColor(.white)

    lazy var separaLine: UIView = .init(frame: CGRect(x: 0, y: 10, width: ScreenWidth, height: 1)).backgroundColor(UIColor(0xF8F5FA))

    @objc public lazy var deleteEmoji: UIButton = .init(type: .custom).frame(CGRect(x: self.frame.width - 48, y: self.frame.height - 56, width: 40, height: 40)).addTargetFor(self, action: #selector(deleteAction), for: .touchUpInside).backgroundColor(.white).cornerRadius(16)

    override public init(frame: CGRect) {
        super.init(frame: frame)
        addSubViews([emojiList, deleteEmoji, separaLine])
        deleteEmoji.setImage(UIImage("backspace_clr"), for: .normal)
        deleteEmoji.setImage(UIImage("backspace"), for: .disabled)
        deleteEmoji.isEnabled = false
        deleteEmoji.isUserInteractionEnabled = false
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    @objc func deleteAction() {
        if deleteClosure != nil {
            deleteClosure!()
        }
    }
}

public extension VoiceRoomEmojiListView {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        VoiceRoomEmojiManager.shared.emojis.count
    }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "VoiceRoomEmojiCell", for: indexPath) as? VoiceRoomEmojiCell
        cell?.icon.image = UIImage(VoiceRoomEmojiManager.shared.emojis[indexPath.row])
        return cell ?? VoiceRoomEmojiCell()
    }

    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        collectionView.deselectItem(at: indexPath, animated: true)
        if emojiClosure != nil {
            emojiClosure!(VoiceRoomEmojiManager.shared.emojis[indexPath.row])
        }
    }
}

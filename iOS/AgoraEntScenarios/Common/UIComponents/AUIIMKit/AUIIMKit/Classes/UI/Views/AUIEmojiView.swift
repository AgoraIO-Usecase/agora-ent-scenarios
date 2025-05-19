//
//  AUIEmojisView.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/15.
//

import UIKit
/*!
 *  \~Chinese
 *  表情容器View
 *
 *  \~English
 *  Emoji container Collection
 *
 */
public class AUIEmojiView: UIView, UICollectionViewDelegate, UICollectionViewDataSource {
    @objc public var deleteClosure: (() -> Void)?

    @objc public var emojiClosure: ((String) -> Void)?

    lazy var flowLayout: UICollectionViewFlowLayout = {
        let layout = UICollectionViewFlowLayout()
        layout.itemSize = CGSize(width: (AScreenWidth - 20 - 60) / 7.0, height: (AScreenWidth - 20 - 60) / 7.0)
        layout.sectionInset = UIEdgeInsets(top: 10, left: 10, bottom: 10, right: 10)
        layout.minimumLineSpacing = 10
        layout.minimumInteritemSpacing = 10
        return layout
    }()

    lazy var emojiList: UICollectionView = {
        UICollectionView(frame: CGRect(x: 0, y: 10, width: AScreenWidth, height: self.frame.height - 10), collectionViewLayout: self.flowLayout).registerCell(AUIChatEmojiCell.self, forCellReuseIdentifier: "AUIChatEmojiCell").dataSource(self).delegate(self).backgroundColor(.clear)
    }()

    lazy var separaLine: UIView = {
        UIView(frame: CGRect(x: 0, y: 10, width: AScreenWidth, height: 1)).backgroundColor(.clear)
    }()

    @objc public lazy var deleteEmoji: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: self.frame.width - 48, y: self.frame.height - 56, width: 40, height: 40))
            .addTargetFor(self, action: #selector(deleteAction), for: .touchUpInside)
            .backgroundColor(AUIChatTheme.shared.inputbar.emojiOperationColor)
            .cornerRadius(16)
    }()

    override public init(frame: CGRect) {
        super.init(frame: frame)
        self.addSubViews([self.emojiList, self.deleteEmoji, self.separaLine])
        self.deleteEmoji.setImage(UIImage.aui_Image(named: "backspace_clr"), for: .normal)
        self.deleteEmoji.setImage(UIImage.aui_Image(named: "backspace"), for: .disabled)
        self.deleteEmoji.isEnabled = false
        self.deleteEmoji.isUserInteractionEnabled = false
        self.backgroundColor = AUIChatTheme.shared.inputbar.backgroundColor
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    @objc func deleteAction() {
        if self.deleteClosure != nil {
            deleteClosure!()
        }
    }
}

public extension AUIEmojiView {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        AUIChatEmojiManager.shared.emojis.count
    }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "AUIChatEmojiCell", for: indexPath) as? AUIChatEmojiCell
        cell?.icon.image = AUIChatEmojiManager.shared.emojiMap.isEmpty ? UIImage.aui_Image(named: AUIChatEmojiManager.shared.emojis[indexPath.row]):AUIChatEmojiManager.shared.emojiMap[AUIChatEmojiManager.shared.emojis[indexPath.row]]
        return cell ?? AUIChatEmojiCell()
    }

    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        collectionView.deselectItem(at: indexPath, animated: true)
        if self.emojiClosure != nil {
            self.emojiClosure!(AUIChatEmojiManager.shared.emojis[indexPath.row])
        }
    }
}

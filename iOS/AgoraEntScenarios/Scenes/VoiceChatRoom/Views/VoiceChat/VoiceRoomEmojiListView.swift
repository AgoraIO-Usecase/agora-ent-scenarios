//
//  VoiceRoomEmojiList.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/9/5.
//

import UIKit
import ZSwiftBaseLib

public class VoiceRoomEmojiListView: UIView,UICollectionViewDelegate,UICollectionViewDataSource {
    
    @objc public var deleteClosure: (()->())?
    
    @objc public var emojiClosure: ((String)->())?
    
    lazy var flowLayout: UICollectionViewFlowLayout = {
        let layout = UICollectionViewFlowLayout.init()
        layout.itemSize = CGSize(width: (ScreenWidth-20-60)/7.0, height: (ScreenWidth-20-60)/7.0)
        layout.sectionInset = UIEdgeInsets(top: 10, left: 10, bottom: 10, right: 10)
        layout.minimumLineSpacing = 10
        layout.minimumInteritemSpacing = 10
        return layout
    }()

    lazy var emojiList: UICollectionView = {
        UICollectionView(frame: CGRect(x: 0, y: 10, width: ScreenWidth, height: self.frame.height-10), collectionViewLayout: self.flowLayout).registerCell(VoiceRoomEmojiCell.self, forCellReuseIdentifier: "VoiceRoomEmojiCell").dataSource(self).delegate(self).backgroundColor(.white)
    }()
    
    lazy var separaLine: UIView = {
        UIView(frame: CGRect(x: 0, y: 10, width: ScreenWidth, height: 1)).backgroundColor(UIColor(0xF8F5FA))
    }()
    
    @objc public lazy var deleteEmoji: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: self.frame.width - 48, y: self.frame.height - 56, width: 40, height: 40)).addTargetFor(self, action: #selector(deleteAction), for: .touchUpInside).backgroundColor(.white).cornerRadius(16)
    }()
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        self.addSubViews([self.emojiList,self.deleteEmoji,self.separaLine])
        self.deleteEmoji.setImage(UIImage("backspace_clr"), for: .normal)
        self.deleteEmoji.setImage(UIImage("backspace"), for: .disabled)
        self.deleteEmoji.isEnabled = false
        self.deleteEmoji.isUserInteractionEnabled = false
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc func deleteAction() {
        if self.deleteClosure != nil {
            self.deleteClosure!()
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
        if self.emojiClosure != nil {
            self.emojiClosure!(VoiceRoomEmojiManager.shared.emojis[indexPath.row])
        }
    }
}

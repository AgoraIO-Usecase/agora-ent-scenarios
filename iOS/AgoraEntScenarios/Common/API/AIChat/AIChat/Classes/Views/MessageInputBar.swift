//
//  MessageInputBar.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/3.
//

import UIKit
import ZSwiftBaseLib
import SDWebImage

public class MessageInputBar: UIView {
    
    var sendClosure: ((String) -> Void)?
    
    var becomeFirstResponderClosure: ((Bool,CGFloat) -> Void)?
    
    private var bots = [AIChatBotProfileProtocol]()
    
    private lazy var collectionView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        layout.scrollDirection = .horizontal
        layout.itemSize = CGSize(width: 28, height: 28)
        layout.minimumInteritemSpacing = 12
        
        let collectionView = UICollectionView(frame: .zero, collectionViewLayout: layout).registerCell(ChatBotSelectCell.self, forCellReuseIdentifier: "ChatBotSelectCell").delegate(self).dataSource(self).backgroundColor(.clear)
        collectionView.translatesAutoresizingMaskIntoConstraints = false
        return collectionView
    }()
    
    private lazy var compositeInputView: CompositeInputView = {
        let inputView = CompositeInputView(frame: .zero).backgroundColor(UIColor(white: 1, alpha: 0.8))
        inputView.translatesAutoresizingMaskIntoConstraints = false
        return inputView
    }()
    
    private var collectionViewHeightConstraint: NSLayoutConstraint?
    private var compositeInputViewTopConstraint: NSLayoutConstraint?
    
    public required init(frame: CGRect, showCollectionView: Bool, datas: [AIChatBotProfileProtocol]) {
        super.init(frame: frame)
        self.bots = datas
        self.setupViews(showCollectionView: showCollectionView)
        self.setupConstraints(showCollectionView: showCollectionView)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupViews(showCollectionView: Bool) {
        if showCollectionView {
            self.addSubview(self.collectionView)
        }
        self.addSubview(self.compositeInputView)
        self.compositeInputView.sendClosure = { [weak self] text in
            self?.sendClosure?(text)
        }
//        self.compositeInputView.becomeFirstResponderClosure = { [weak self] isFirstResponder,keyboardHeight in
//            self?.becomeFirstResponderClosure?(isFirstResponder,keyboardHeight)
//        }
    }
    
    private func setupConstraints(showCollectionView: Bool) {
        if showCollectionView {
            self.collectionViewHeightConstraint = self.collectionView.heightAnchor.constraint(equalToConstant: 28)
            self.collectionViewHeightConstraint?.isActive = true
            
            NSLayoutConstraint.activate([
                self.collectionView.topAnchor.constraint(equalTo: topAnchor),
                self.collectionView.leadingAnchor.constraint(equalTo: leadingAnchor,constant: 20),
                self.collectionView.trailingAnchor.constraint(equalTo: trailingAnchor,constant: -20),
            ])
            
            self.compositeInputViewTopConstraint = compositeInputView.topAnchor.constraint(equalTo: self.collectionView.bottomAnchor, constant: 12)
        } else {
            self.compositeInputViewTopConstraint = compositeInputView.topAnchor.constraint(equalTo: topAnchor)
        }
        
        self.compositeInputViewTopConstraint?.isActive = true
        
        NSLayoutConstraint.activate([
            self.compositeInputView.leadingAnchor.constraint(equalTo: leadingAnchor),
            self.compositeInputView.trailingAnchor.constraint(equalTo: trailingAnchor),
            self.compositeInputView.bottomAnchor.constraint(equalTo: bottomAnchor)
        ])
    }

}

extension MessageInputBar: UICollectionViewDataSource,UICollectionViewDelegate {
    public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        self.bots.count
    }
    
    public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "ChatBotSelectCell", for: indexPath) as? ChatBotSelectCell
        if let bot = self.bots[safe: indexPath.row] {
            cell?.refresh(bot: bot)
        }
        return cell ?? ChatBotSelectCell()
    }
}


class ChatBotSelectCell: UICollectionViewCell {
    
    lazy var avatarView: UIImageView = {
        UIImageView(frame: self.contentView.bounds).contentMode(.scaleAspectFill).cornerRadius(14)
    }()
    
    lazy var cover: UIImageView = {
        UIImageView(frame: self.contentView.bounds).contentMode(.scaleAspectFill).cornerRadius(14).backgroundColor(.clear)
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.contentView.addSubview(self.avatarView)
        self.contentView.addSubview(self.cover)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        self.avatarView.frame = self.contentView.bounds
        self.cover.frame = self.contentView.bounds
    }
    
    func refresh(bot: AIChatBotProfileProtocol,enable: Bool = true) {
        self.avatarView.sd_setImage(with: URL(string: bot.botIcon), placeholderImage: UIImage(named: "avatar_placeholder"))
        self.cover.isHidden = !bot.selected
        if bot.selected {
            self.backgroundColor = .clear
        } else {
            self.backgroundColor = enable ? .clear : UIColor(white: 1, alpha: 0.4)
        }
    }
}

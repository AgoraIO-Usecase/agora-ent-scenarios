//
//  JoyGiftListDialog.swift
//  Joy
//
//  Created by wushengtao on 2023/12/6.
//

import UIKit

class JoyGiftListCell: UICollectionViewCell {
    override var isSelected: Bool {
        didSet {
            if isSelected {
                layer.borderWidth = 2
                layer.borderColor = UIColor.joy_btn_bg.cgColor
                backgroundColor = .joy_btn_bg
                nameLabel.textColor = .joy_main_text
            } else {
                layer.borderWidth = 2
                layer.borderColor = UIColor.clear.cgColor
                backgroundColor = .clear
                nameLabel.textColor = .joy_title_text
            }
        }
    }
    var giftInfo: CloudGameGiftInfo? {
        didSet {
            imageView.sd_setImage(with: URL(string: giftInfo?.thumbnail ?? ""),
                                  placeholderImage: UIImage.sceneImage(name: "game_placeholder"))
            nameLabel.text = giftInfo?.name ?? ""
        }
    }
    // 背景图
    private lazy var imageView: UIImageView = {
        let imageView = UIImageView()
        imageView.backgroundColor = .white
        imageView.contentMode = .scaleAspectFit
        return imageView
    }()
    // 房间名称
    private lazy var nameLabel: UILabel = {
        let nameLabel = UILabel()
        nameLabel.font = .joy_M_12
        nameLabel.textAlignment = .center
        return nameLabel
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func createSubviews() {
        layer.cornerRadius = 15
        clipsToBounds = true
        contentView.addSubview(imageView)
        contentView.addSubview(nameLabel)
        nameLabel.snp.makeConstraints { make in
            make.left.right.equalToSuperview()
            make.bottom.equalTo(-8)
            make.height.equalTo(17)
        }
        
        imageView.snp.makeConstraints { make in
            make.left.right.top.equalToSuperview()
            make.bottom.equalTo(nameLabel.snp.top).offset(-4)
        }
    }
}

class JoyGiftListDialog: JoyBaseDialog {
    var onSelectedGift: ((CloudGameGiftInfo)->())?
    var giftList: [CloudGameGiftInfo] = [] {
        didSet {
            listView.reloadData()
        }
    }
    private var selectedGift: CloudGameGiftInfo? {
        didSet {
            button.isEnabled = selectedGift == nil ? false : true
        }
    }
    private lazy var listView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        layout.sectionInset = UIEdgeInsets(top: 0, left: 20, bottom: 0, right: 20)
        layout.minimumInteritemSpacing = 8
        let itemWidth = (self.width - 8.0 * 2.0 - 20 * 2) / 3.0
        layout.itemSize = CGSize(width: floor(itemWidth), height: 120)
        let collectionView = UICollectionView(frame: self.bounds, collectionViewLayout: layout)
        collectionView.backgroundColor = .clear
        collectionView.showsVerticalScrollIndicator = false
        collectionView.register(JoyGiftListCell.self, forCellWithReuseIdentifier: NSStringFromClass(JoyGiftListCell.self))
        collectionView.delegate = self
        collectionView.dataSource = self
        return collectionView
    }()
    
    override func contentSize() -> CGSize {
        return CGSize(width: self.width, height: 401)
    }
    
    override func loadCustomContentView(contentView: UIView) {
        contentView.addSubview(listView)
        listView.snp.makeConstraints { make in
            make.edges.equalTo(contentView)
        }
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        button.frame = CGRect(x: dialogView.width - 96 - 20,
                              y: button.aui_top,
                              width: 96,
                              height: 36)
        button.setjoyVerticalDefaultGradientBackground()
    }
    
    override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        if let superViewHitTestView = super.hitTest(point, with: event), superViewHitTestView != self {
            return superViewHitTestView
        } else {
            hiddenAnimation()
            return self
        }
    }
    
    override func labelTitle() -> String {
        return "dialog_title_giftlist".joyLocalization()
    }
    
    override func buttonTitle() -> String {
        return "dialog_selected_send".joyLocalization()
    }
    
    override func onClickButton() {
        guard let selectedGift = selectedGift else {return}
        onSelectedGift?(selectedGift)
    }
}

extension JoyGiftListDialog: UICollectionViewDelegate, UICollectionViewDataSource {
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell: JoyGiftListCell = collectionView.dequeueReusableCell(withReuseIdentifier: NSStringFromClass(JoyGiftListCell.self), for: indexPath) as! JoyGiftListCell
        let gift = giftList[indexPath.item]
        cell.giftInfo = gift
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        selectedGift = giftList[indexPath.item]
        if let cell = collectionView.cellForItem(at: indexPath) {
            cell.isSelected = true
        }
    }
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return giftList.count
    }
    
    func numberOfSections(in collectionView: UICollectionView) -> Int {
        return 1
    }
}

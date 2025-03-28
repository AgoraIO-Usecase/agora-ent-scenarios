//
//  AUIJukeBoxCell.swift
//  AUIKit
//
//  Created by wushengtao on 2023/4/3.
//

import Foundation
import SDWebImage

public enum AUIJukeSongCellStyle: Int {
    case selectSong      //点歌
    case selectedSong    //已点
}

private let kRangeEdge: CGFloat = 16
private let kRemoveAndPinButtonSize: CGSize = CGSize(width: 24, height: 24)
open class AUIJukeBoxCell: UITableViewCell {
    public var selectSongClosure: (()->())?    //点歌
    public var pinClosure: (()->())?           //置顶
    public var deleteClosure: (()->())?        //删除
    public var nextClosure: (()->())?          //下一首
    
    public var aui_style: AUIJukeSongCellStyle = .selectSong {
        didSet {
            _resetStyle()
            setNeedsLayout()
        }
    }
    
    public var music: AUIJukeBoxItemDataProtocol? {
        didSet {
            self.songNameLabel.text = music?.title ?? ""
            self.descLabel.text = music?.subTitle ?? ""
            self.avatarImageView.theme_image = "JukeBoxCell.avatarPlaceHolder"
            self.avatarImageView.sd_setImage(with: URL(string: music?.avatarUrl ?? ""), placeholderImage: self.avatarImageView.image)
        }
    }
    
    public lazy var rangeImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.theme_image = "JukeBoxCell.playingIcon"
        return imageView
    }()
    
    public lazy var rangeLabel: UILabel = {
        let label = UILabel()
         label.theme_textColor = "JukeBoxCell.titleColor"
         label.theme_font = "CommonFont.middle"
         return label
    }()
    
    public var isSelectedSong: Bool = false {
        didSet {
            selectButton.isSelected = isSelectedSong
            if isSelectedSong {
                selectButton.theme_backgroundColor = AUIColor("JukeBoxCell.selectedButtonBackgroundColor")
                selectButton.layer.theme_borderColor = AUICGColor("JukeBoxCell.selectedButtonBorderColor")
                selectButton.layer.theme_borderWidth = "JukeBoxCell.buttonSelectedBorderWidth"
            } else {
                selectButton.theme_backgroundColor = AUIColor("JukeBoxCell.unselectedButtonBackgroundColor")
                selectButton.layer.theme_borderColor = AUICGColor("JukeBoxCell.normalButtonBorderColor")
                selectButton.layer.theme_borderWidth = "JukeBoxCell.buttonNormalBorderWidth"
            }
        }
    }
    
    public lazy var avatarImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.theme_width = "JukeBoxCell.avatarWidth"
        imageView.theme_height = "JukeBoxCell.avatarHeight"
        imageView.layer.theme_cornerRadius = "JukeBoxCell.avatarCornerRadius"
        imageView.theme_backgroundColor = "JukeBoxCell.avatarBackgroundColor"
        imageView.theme_image = "JukeBoxCell.avatarPlaceHolder"
        imageView.clipsToBounds = true
        return imageView
    }()
    
    public lazy var songNameLabel: UILabel = {
       let label = UILabel()
        label.theme_textColor = "JukeBoxCell.titleColor"
        label.theme_font = "JukeBoxCell.titleFont"
        return label
    }()
    
    public lazy var descLabel: UILabel = {
       let label = UILabel()
        label.theme_textColor = "JukeBoxCell.subTitleColor"
        label.theme_font = "JukeBoxCell.subTitleFont"
        return label
    }()
    
    public lazy var selectButton: UIButton = {
        let button = UIButton(type: .custom)
        button.theme_width = "JukeBoxCell.selectedButtonWidth"
        button.theme_height = "JukeBoxCell.selectedButtonHeight"
        button.layer.theme_cornerRadius = "JukeBoxCell.buttonCornerRadius"
        button.clipsToBounds = true
        button.setTitle(aui_localized("selectSong"), for: .normal)
        button.setTitle(aui_localized("selectedSong"), for: .selected)
        button.titleLabel?.theme_font = "JukeBoxCell.buttonTitleFont"
        button.theme_setTitleColor(AUIColor("JukeBoxCell.normalButtonTitleColor"), forState: .normal)
        button.theme_setTitleColor(AUIColor("JukeBoxCell.selectedButtonTitleColor"), forState: .selected)
        button.addTarget(self, action: #selector(onSelectSongAction(_:)), for: .touchUpInside)
        return button
    }()
    
    public lazy var nextButton: UIButton = {
        let button = UIButton(type: .custom)
        button.aui_size = kRemoveAndPinButtonSize
        button.theme_setImage("JukeBoxCell.nextSongIcon", forState: .normal)
        button.addTarget(self, action: #selector(onNextAction(_:)), for: .touchUpInside)
        return button
    }()
    
    public lazy var removeButton: UIButton = {
        let button = UIButton(type: .custom)
        button.aui_size = kRemoveAndPinButtonSize
        button.theme_setImage("JukeBoxCell.deleteIcon", forState: .normal)
        button.addTarget(self, action: #selector(onDeleteAction(_:)), for: .touchUpInside)
        return button
    }()
    
    public lazy var pinButton: UIButton = {
        let button = UIButton(type: .custom)
        button.aui_size = kRemoveAndPinButtonSize
        button.theme_setImage("JukeBoxCell.pinIcon", forState: .normal)
        button.addTarget(self, action: #selector(onPinAction(_:)), for: .touchUpInside)
        return button
    }()

    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        _loadSubViews()
        _resetStyle()
    }
    
    required public init?(coder: NSCoder) {
        super.init(coder: coder)
        
        _loadSubViews()
        _resetStyle()
    }
    
    private func _loadSubViews() {
        backgroundColor = .clear
        
        contentView.addSubview(rangeImageView)
        contentView.addSubview(rangeLabel)
        contentView.addSubview(avatarImageView)
        contentView.addSubview(songNameLabel)
        contentView.addSubview(descLabel)
        contentView.addSubview(selectButton)
        contentView.addSubview(nextButton)
        contentView.addSubview(removeButton)
        contentView.addSubview(pinButton)
    }
    
    private func _resetStyle() {
        if aui_style == .selectSong {
            rangeLabel.isHidden = true
            rangeImageView.isHidden = true
            selectButton.isHidden = false
            removeButton.isHidden = true
            pinButton.isHidden = true
            nextButton.isHidden = true
        } else {
            rangeLabel.isHidden = false
            rangeImageView.isHidden = false
            selectButton.isHidden = true
        }
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        
        if aui_style == .selectSong {
            avatarImageView.aui_center = CGPoint(x: kRangeEdge + avatarImageView.aui_width / 2, y: aui_height / 2)
            selectButton.aui_center = CGPoint(x: aui_width - kRangeEdge - selectButton.aui_width / 2, y: aui_height / 2)
        } else {
            rangeImageView.frame = CGRect(x: kRangeEdge, y: (aui_height - 12) / 2, width: 12, height: 12)
            rangeLabel.frame = rangeImageView.frame
            avatarImageView.aui_center = CGPoint(x: 40 + avatarImageView.aui_width / 2, y: aui_height / 2)
            removeButton.aui_center = CGPoint(x: aui_width - kRangeEdge - removeButton.aui_width / 2, y: aui_height / 2)
            pinButton.aui_center = CGPoint(x: removeButton.aui_left - kRangeEdge - pinButton.aui_width / 2, y: aui_height / 2)
            nextButton.aui_center = removeButton.aui_center
        }
        songNameLabel.sizeToFit()
        descLabel.sizeToFit()
        let contentHeight = songNameLabel.aui_height + descLabel.aui_height
        songNameLabel.aui_tl = CGPoint(x: avatarImageView.aui_right + 12, y: (aui_height - contentHeight) / 2)
        descLabel.aui_tl = CGPoint(x: songNameLabel.aui_left, y: songNameLabel.aui_bottom)
    }
    
    open override func setSelected(_ selected: Bool, animated: Bool) {
        
    }
    
    open override func setHighlighted(_ highlighted: Bool, animated: Bool) {
        
    }
}


extension AUIJukeBoxCell {
    @objc func onSelectSongAction(_ sender: UIButton) {
        aui_info("onSelectSongAction", tag: "AUIJukeBoxCell")
//        self.isSelectedSong = !sender.isSelected
        self.selectSongClosure?()
    }
    
    @objc func onPinAction(_ sender: UIButton) {
        aui_info("onPinAction", tag: "AUIJukeBoxCell")
        self.pinClosure?()
    }
    
    @objc func onDeleteAction(_ sender: UIButton) {
        aui_info("onDeleteAction", tag: "AUIJukeBoxCell")
        self.deleteClosure?()
    }
    
    @objc func onNextAction(_ sender: UIButton) {
        aui_info("onNextAction", tag: "AUIJukeBoxCell")
        self.nextClosure?()
    }
}

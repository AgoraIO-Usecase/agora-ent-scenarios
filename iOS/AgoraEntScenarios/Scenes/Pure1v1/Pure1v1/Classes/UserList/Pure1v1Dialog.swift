//
//  Pure1v1Dialog.swift
//  Pure1v1
//
//  Created by wushengtao on 2023/7/20.
//

import UIKit

class Pure1v1Dialog: UIView {
    private lazy var iconView = UIImageView(image: UIImage.sceneImage(name: "dialog_icon"))
    private lazy var gradientLayer: CAGradientLayer = {
        let layer = CAGradientLayer()
        layer.colors = [
            UIColor(hexString: "#F6F2FF")!.cgColor,
            UIColor(hexString: "#FFFFFF")!.cgColor,
        ]

        return layer
    }()
    override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubView()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    fileprivate func _loadSubView() {
        backgroundColor = .white
        layer.cornerRadius = 20
        clipsToBounds = true
        
        layer.addSublayer(gradientLayer)
        
        addSubview(iconView)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        gradientLayer.frame = CGRect(x: 0, y: 0, width: aui_width, height: 58)
        iconView.aui_size = CGSize(width: 106, height: 100)
    }
}

class Pure1v1NoDataDialog: Pure1v1Dialog {
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = .black
        label.font = UIFont.systemFont(ofSize: 20)
        label.text = "user_list_waitting".pure1v1Localization()
        return label
    }()
    private lazy var contentLabel: UILabel = {
        let label = UILabel()
        label.textColor = .black
        label.numberOfLines = 0
        label.font = UIFont.systemFont(ofSize: 14)
        label.text = "user_list_nodata_tips".pure1v1Localization()
        return label
    }()
    override func _loadSubView() {
        super._loadSubView()
        addSubview(titleLabel)
        addSubview(contentLabel)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        titleLabel.sizeToFit()
        titleLabel.aui_centerX = self.aui_width / 2
        titleLabel.aui_top = 25
        
        contentLabel.aui_left = 30
        contentLabel.aui_width = self.aui_width - 60
        contentLabel.sizeToFit()
        contentLabel.aui_top = titleLabel.aui_bottom + 40
    }
}

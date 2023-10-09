//
//  ConnectedToastView.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/9/20.
//

import Foundation
import SDWebImage

class ConnectedToastView: UIView {
    var user: ShowTo1v1UserInfo? {
        didSet {
            imageView.sd_setImage(with: URL(string: user?.avatar ?? ""))
            titleLabel.text = user?.userName ?? ""
        }
    }
    
    private lazy var imageView: UIImageView = UIImageView()
    
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.font = UIFont.systemFont(ofSize: 18)
        label.textColor = .black
        return label
    }()
    
    private lazy var subtitleLabel: UILabel = {
        let label = UILabel()
        label.text = "call_connected_toast_tips".showTo1v1Localization()
        label.font = UIFont.systemFont(ofSize: 16)
        label.textColor = UIColor(red: 0.483, green: 0.483, blue: 0.483, alpha: 1)
        return label
    }()
    
    static func show(user: ShowTo1v1UserInfo, canvasView: UIView) {
        let padding = 10.0
        let connectedView = ConnectedToastView(frame: CGRect(x: padding, y: -80, width: canvasView.aui_width - padding * 2, height: 80))
        connectedView.user = user
        canvasView.addSubview(connectedView)
        
        UIView.animate(withDuration: 0.3) {
            connectedView.aui_top = UIDevice.current.aui_SafeDistanceTop
        } completion: { success in
            UIView.animate(withDuration: 0.3, delay: 1) {
                connectedView.aui_top = -80
            } completion: { success in
                connectedView.removeFromSuperview()
            }
        }
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        _loadViews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func _loadViews() {
        layer.backgroundColor = UIColor(red: 1, green: 1, blue: 1, alpha: 0.8).cgColor
        layer.cornerRadius = 24
        clipsToBounds = true
        addSubview(imageView)
        addSubview(titleLabel)
        addSubview(subtitleLabel)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        let wh = aui_height - 20
        imageView.frame = CGRect(x: 10, y: 10, width: wh, height: wh)
        imageView.layer.cornerRadius = wh / 2
        imageView.clipsToBounds = true
        
        let leftPadding = imageView.aui_right + 25
        titleLabel.frame = CGRect(x: leftPadding, y: 20, width: aui_width - leftPadding, height: 18)
        subtitleLabel.frame = CGRect(x: leftPadding, y: titleLabel.aui_bottom + 6, width: titleLabel.aui_width, height: 16)
    }
    
}

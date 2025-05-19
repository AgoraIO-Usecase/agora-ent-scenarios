//
//  SquareViewWithImages.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/10.
//

import UIKit
import SDWebImage
import ZSwiftBaseLib

public class OverlappingAvatarsView: UIImageView {
    
    private let containerView = UIView()
     let avatarView1 = UIImageView()
     let avatarView2 = UIImageView()
    private let maskLayerView = UIView()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupView()
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setupView()
    }
    
    private func setupView() {
        // 配置容器视图
        containerView.frame = bounds
        addSubview(containerView)
        
        // 配置头像视图
        let avatarWidth = self.frame.width*0.72
        avatarView1.contentMode = .scaleAspectFill
        avatarView1.clipsToBounds = true
        avatarView1.layer.cornerRadius = avatarWidth/2
        avatarView1.frame = CGRect(x: 0, y: 0, width: avatarWidth, height: avatarWidth)
        
        avatarView2.contentMode = .scaleAspectFill
        avatarView2.clipsToBounds = true
        avatarView2.layer.cornerRadius = avatarWidth/2
        avatarView2.frame = CGRect(x: self.frame.width-avatarWidth, y: self.frame.height - avatarWidth, width: avatarWidth, height: avatarWidth)
        
        // 配置遮罩视图
        maskLayerView.frame = containerView.bounds
        
        // 添加视图到容器
        containerView.addSubview(maskLayerView)
        containerView.addSubview(avatarView2)
        
        updateMask()
    }
    
    private func updateMask() {
        let maskLayer = CAShapeLayer()
        let path = UIBezierPath(rect: containerView.bounds)
        let circlePath = UIBezierPath(ovalIn: avatarView2.frame.insetBy(dx: -2, dy: -2))
        path.append(circlePath)
        maskLayer.path = path.cgPath
        maskLayer.fillRule = .evenOdd
        
        maskLayerView.layer.addSublayer(avatarView1.layer)
        maskLayerView.layer.mask = maskLayer
    }
    
    // 公开方法来设置头像图片
    func setAvatars(first: UIImage?, second: UIImage?) {
        avatarView1.image = first
        avatarView2.image = second
        setNeedsLayout()
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        containerView.frame = bounds
        let avatarWidth = self.containerView.frame.width*0.72
        avatarView1.frame = CGRect(x: 0, y: 0, width: avatarWidth, height: avatarWidth)
        avatarView2.frame = CGRect(x: self.frame.width-avatarWidth, y: self.frame.height - avatarWidth, width: avatarWidth, height: avatarWidth)
        updateMask()
    }
    
    func refresh(with urls: (String, String)) {
        self.image = nil
        self.cornerRadius(0)
        self.avatarView1.sd_setImage(with: URL(string: urls.0), placeholderImage: UIImage(named: "botavatar", in: .chatAIBundle, with: nil))
        self.avatarView2.sd_setImage(with: URL(string: urls.1), placeholderImage: UIImage(named: "botavatar", in: .chatAIBundle, with: nil))
        let avatarWidth = self.containerView.frame.width*0.72
        self.avatarView1.cornerRadius(avatarWidth/2.0)
        self.avatarView2.cornerRadius(avatarWidth/2.0)
        setNeedsLayout()
    }
}


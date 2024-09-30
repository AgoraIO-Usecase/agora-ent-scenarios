//
//  SquareViewWithImages.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/10.
//

import UIKit
import SDWebImage
import ZSwiftBaseLib

public class SquareViewWithImages: UIImageView {
    
    let topLeftImageView = UIImageView()
    let bottomRightImageView = UIImageView()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.setupView()
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        self.setupView()
    }
    
    private func setupView() {
        // 确保视图是正方形
        let size = min(bounds.width, bounds.height)
        self.bounds = CGRect(x: 0, y: 0, width: size, height: size)
        
        // 设置背景颜色（可选）
        self.backgroundColor = .clear
        
        // 配置和添加顶部左侧的 ImageView
        self.topLeftImageView.backgroundColor = .clear // 用于可视化，可以替换为实际图片
        self.topLeftImageView.translatesAutoresizingMaskIntoConstraints = false
        self.addSubview(self.topLeftImageView)
        
        // 配置和添加底部右侧的 ImageView
        self.bottomRightImageView.backgroundColor = .clear // 用于可视化，可以替换为实际图片
        self.bottomRightImageView.translatesAutoresizingMaskIntoConstraints = false
        self.addSubview(self.bottomRightImageView)
        
        // 设置约束
        NSLayoutConstraint.activate([
            // 顶部左侧 ImageView 约束
            self.topLeftImageView.topAnchor.constraint(equalTo: topAnchor),
            self.topLeftImageView.leftAnchor.constraint(equalTo: leftAnchor),
            self.topLeftImageView.widthAnchor.constraint(equalTo: widthAnchor, multiplier: 0.72),
            self.topLeftImageView.heightAnchor.constraint(equalTo: self.topLeftImageView.widthAnchor),
            
            // 底部右侧 ImageView 约束
            self.bottomRightImageView.bottomAnchor.constraint(equalTo: bottomAnchor),
            self.bottomRightImageView.rightAnchor.constraint(equalTo: rightAnchor),
            self.bottomRightImageView.widthAnchor.constraint(equalTo: widthAnchor, multiplier: 0.72),
            self.bottomRightImageView.heightAnchor.constraint(equalTo: self.bottomRightImageView.widthAnchor)
        ])
        
        // 确保底部右侧的 ImageView 在顶部左侧的 ImageView 之上
        self.bringSubviewToFront(self.bottomRightImageView)
        self.bottomRightImageView.layerProperties(UIColor(white: 1, alpha: 0.5), 2)
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        
        self.topLeftImageView.cornerRadius(self.topLeftImageView.frame.width / 2.0)
        self.bottomRightImageView.cornerRadius(self.topLeftImageView.frame.width / 2.0)
    }
    
    func refresh(with urls: (String, String)) {
        self.image = nil
        self.cornerRadius(0)
        self.topLeftImageView.sd_setImage(with: URL(string: urls.0), placeholderImage: UIImage(named: "botavatar", in: .chatAIBundle, with: nil))
        self.bottomRightImageView.sd_setImage(with: URL(string: urls.1), placeholderImage: UIImage(named: "botavatar", in: .chatAIBundle, with: nil))
    }
}


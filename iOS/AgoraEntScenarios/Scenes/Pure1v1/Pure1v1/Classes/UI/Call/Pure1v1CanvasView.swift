//
//  Pure1v1CanvasView.swift
//  Pure1v1
//
//  Created by wushengtao on 2023/8/4.
//

import Foundation

class Pure1v1CanvasView: UIView {
    lazy var canvasView = UIView()
    
    var tapClosure: (()->())?
    
    lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.font = UIFont.systemFont(ofSize: 12)
        label.textColor = .white
        return label
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        addSubview(canvasView)
        addSubview(titleLabel)
        
        let tapGes = UITapGestureRecognizer {[weak self] _ in
            self?.tapClosure?()
        }
        addGestureRecognizer(tapGes)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        canvasView.frame = bounds
        titleLabel.sizeToFit()
        titleLabel.aui_bl = CGPoint(x: 11, y: aui_height - 10)
        
        //add shadow
        titleLabel.layer.shadowColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.5).cgColor
        titleLabel.layer.shadowOpacity = 1
        titleLabel.layer.shadowRadius = 4
        titleLabel.layer.shadowOffset = CGSize(width: 0, height: 1)
    }
}

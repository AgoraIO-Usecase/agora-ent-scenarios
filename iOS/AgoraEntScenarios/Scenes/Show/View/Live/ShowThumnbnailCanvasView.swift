//
//  ShowThumnbnailCanvasView.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/10/26.
//

import UIKit

class ShowThumnbnailCanvasView: UIView {
    private lazy var canvasView: UIView = {
        let view = UIView()
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()
    private lazy var leaveIconView: UIImageView = {
        let imageView = UIImageView(image: UIImage.sceneImage(name: "icon_user_leave")!)
        imageView.translatesAutoresizingMaskIntoConstraints = false
        return imageView
    }()
    private lazy var leaveTipsLabel = {
        let label = UILabel()
        label.textColor = .white
        label.font = .systemFont(ofSize: 13)
        label.numberOfLines = 0
        label.lineBreakMode = .byWordWrapping
        let paragraphStyle = NSMutableParagraphStyle()
        paragraphStyle.alignment = .center
        paragraphStyle.lineHeightMultiple = 1.3
        label.attributedText = NSMutableAttributedString(string: "user_list_user_leave".show_localized,
                                                        attributes: [.paragraphStyle: paragraphStyle])
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubViews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func _loadSubViews() {
        backgroundColor = UIColor(hexString: "#0038ff")
        addSubview(leaveIconView)
        addSubview(leaveTipsLabel)
        addSubview(canvasView)
        
        leaveIconView.centerXAnchor.constraint(equalTo: centerXAnchor).isActive = true
        leaveIconView.bottomAnchor.constraint(equalTo: centerYAnchor).isActive = true
        
        leaveTipsLabel.centerXAnchor.constraint(equalTo: centerXAnchor).isActive = true
        leaveTipsLabel.topAnchor.constraint(equalTo: leaveIconView.bottomAnchor, constant: 10).isActive = true
        
        canvasView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        canvasView.topAnchor.constraint(equalTo: topAnchor).isActive = true
        canvasView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        canvasView.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
    }
}

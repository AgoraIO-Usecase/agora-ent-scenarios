//
//  PureRttLabelView.swift
//  Pure1v1
//
//  Created by CYG on 2024/6/24.
//

import UIKit
import AgoraCommon

class PureRttLabelView: UIView {
    
    private var cellDidSelectedAction: (() -> ())?
    
    private var separatorLineView: UIView = {
        let view = UIView()
        view.backgroundColor = .black
        view.alpha = 0.1
        return view
    }()
    
    private lazy var valueLabel: UILabel = {
        let label = UILabel()
        label.textColor = .black
        label.font = UIFont.systemFont(ofSize: 16)
        return label
    }()
    
    private lazy var arrowImgView: UIImageView = {
        let imgView = UIImageView()
        imgView.image = UIImage.scene1v1Image(name: "show_arrow_right")
        return imgView
    }()
    
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = .black
        label.font = UIFont.boldSystemFont(ofSize: 16)
        return label
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func createSubviews() {
        addSubview(titleLabel)
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            titleLabel.leftAnchor.constraint(equalTo: self.leftAnchor, constant: 20),
            titleLabel.centerYAnchor.constraint(equalTo: self.centerYAnchor)
        ])
        
        addSubview(separatorLineView)
        separatorLineView.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            separatorLineView.leftAnchor.constraint(equalTo: self.leftAnchor, constant: 20),
            separatorLineView.rightAnchor.constraint(equalTo: self.rightAnchor, constant: -20),
            separatorLineView.bottomAnchor.constraint(equalTo: self.bottomAnchor),
            separatorLineView.heightAnchor.constraint(equalToConstant: 1)
        ])
        
        addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(didSelected)))
        
        addSubview(arrowImgView)
        arrowImgView.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            arrowImgView.rightAnchor.constraint(equalTo: self.rightAnchor, constant: -20),
            arrowImgView.centerYAnchor.constraint(equalTo: self.centerYAnchor)
        ])
        
        addSubview(valueLabel)
        valueLabel.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            valueLabel.rightAnchor.constraint(equalTo: arrowImgView.leftAnchor, constant: -10), // 调整为箭头左侧留有适当空间
            valueLabel.centerYAnchor.constraint(equalTo: self.centerYAnchor)
        ])
    }
    
    @objc private func didSelected() {
        cellDidSelectedAction?()
    }
    
    func setTitle(_ title: String, value: String, cellDidSelectedAction: (() -> ())?) {
        titleLabel.text = title
        valueLabel.text = value
        self.cellDidSelectedAction = cellDidSelectedAction
    }
}


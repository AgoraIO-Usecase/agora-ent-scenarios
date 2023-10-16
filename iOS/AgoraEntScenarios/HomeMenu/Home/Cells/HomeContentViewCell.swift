//
//  HomeContentViewCell.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/10/12.
//

import UIKit

class HomeContentViewCell: UICollectionViewCell {
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.text = ""
        label.textColor = UIColor(hex: "#000000", alpha: 1.0)
        label.font = .systemFont(ofSize: 16.fit, weight: .medium)
        label.numberOfLines = 2
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var descLabel: UILabel = {
        let label = UILabel()
        label.text = ""
        label.textColor = UIColor(hex: "#303553", alpha: 1.0)
        label.font = .systemFont(ofSize: 10.fit)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var backgroundImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage(named: "home_ktv_solo"))
        imageView.contentMode = .scaleAspectFill
        imageView.translatesAutoresizingMaskIntoConstraints = false
        imageView.cornerRadius(16.fit)
        return imageView
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setupData(model: HomeContentModel?) {
        backgroundImageView.image = UIImage(named: model?.imageName ?? "home_ktv_solo")
        titleLabel.text = model?.title
        descLabel.text = model?.desc
    }
    
    private func setupUI() {
        contentView.addSubview(backgroundImageView)
        contentView.addSubview(titleLabel)
        contentView.addSubview(descLabel)
        
        backgroundImageView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor).isActive = true
        backgroundImageView.topAnchor.constraint(equalTo: contentView.topAnchor).isActive = true
        backgroundImageView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor).isActive = true
        backgroundImageView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
        
        titleLabel.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 12.fit).isActive = true
        titleLabel.centerYAnchor.constraint(equalTo: contentView.centerYAnchor, constant: -3).isActive = true
        titleLabel.widthAnchor.constraint(equalToConstant: 77.fit).isActive = true
                
        descLabel.leadingAnchor.constraint(equalTo: titleLabel.leadingAnchor).isActive = true
        descLabel.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 5).isActive = true
    }
}

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
        label.numberOfLines = 0
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var backgroundImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage(named: "home_ktv_solo"))
        imageView.contentMode = .scaleAspectFill
        imageView.translatesAutoresizingMaskIntoConstraints = false
        imageView.cornerRadius(16.fit)
        imageView.layer.borderColor = UIColor(hex: "#CCCCCC", alpha: 0.8)!.cgColor
        imageView.layer.borderWidth = 0.3
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
        let content = "\(model?.title ?? "")\n\(model?.desc ?? "")"
        let paragraphStyle = NSMutableParagraphStyle()
        paragraphStyle.paragraphSpacing = 5
        let attrs = NSMutableAttributedString(string: content, attributes: [.foregroundColor: UIColor(hex: "#000000", alpha: 1.0),
                                                                            .font: UIFont.systemFont(ofSize: 16.fit, weight: .medium),
                                                                            .paragraphStyle: paragraphStyle])
        attrs.addAttributes([.foregroundColor: UIColor(hex: "#303553", alpha: 1.0),
                             .font: UIFont.systemFont(ofSize: 10.fit)], range: NSRange(location: (model?.title?.count ?? 0) + 1,
                                                                                       length: model?.desc?.count ?? 0))
        titleLabel.attributedText = attrs
        titleLabel.accessibilityIdentifier = "home_cell_\(model?.imageName ?? "")"
    }
    
    private func setupUI() {
        contentView.addSubview(backgroundImageView)
        contentView.addSubview(titleLabel)
        
        backgroundImageView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor).isActive = true
        backgroundImageView.topAnchor.constraint(equalTo: contentView.topAnchor).isActive = true
        backgroundImageView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor).isActive = true
        backgroundImageView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
        
        titleLabel.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 12.fit).isActive = true
        titleLabel.centerYAnchor.constraint(equalTo: contentView.centerYAnchor).isActive = true
        titleLabel.widthAnchor.constraint(equalToConstant: 77.fit).isActive = true
    }
}

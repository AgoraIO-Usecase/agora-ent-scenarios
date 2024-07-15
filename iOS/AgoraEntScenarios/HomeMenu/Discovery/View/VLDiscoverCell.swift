//
//  VLDiscoverCell.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/10/20.
//

import UIKit

class VLDiscoverCell: UICollectionViewCell {
    private lazy var bgImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage(named: "discover_cell_bg"))
        imageView.translatesAutoresizingMaskIntoConstraints = false
        imageView.isUserInteractionEnabled = true
        imageView.contentMode = .scaleAspectFill
        imageView.cornerRadius(16.fit)
        imageView.layer.borderWidth = 0.5
        imageView.layer.borderColor = UIColor(hex: "#CCCCCC", alpha: 0.8)!.cgColor
        return imageView
    }()
    private lazy var iconImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage(named: "discover_ktv"))
        imageView.translatesAutoresizingMaskIntoConstraints = false
        return imageView
    }()
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.text = ""
        label.textColor = UIColor(hex: "#303553", alpha: 1.0)
        label.font = .systemFont(ofSize: 16, weight: .medium)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }() 
    private lazy var descLabel: UILabel = {
        let label = UILabel()
        label.text = ""
        label.textColor = UIColor(hex: "#303553", alpha: 1.0)
        label.font = .systemFont(ofSize: 13)
        label.numberOfLines = 0
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var docuButton: UIButton = {
        let button = UIButton()
        button.setTitle(NSLocalizedString("discover_document_title", comment: ""), for: .normal)
        button.setTitleColor(UIColor(hex: "#2E6CF6", alpha: 1.0), for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 13)
        button.setImage(UIImage(named: "discover_document"), for: .normal)
        button.adjustImageTitlePosition(.right, spacing: 2)
        button.translatesAutoresizingMaskIntoConstraints = false
        button.addTarget(self, action: #selector(onClickDocumentButton), for: .touchUpInside)
        button.vm_expandSize(size: 15)
        return button
    }()
    private lazy var musicButton: UIButton = {
        let button = UIButton()
        button.setBackgroundImage(UIImage(named: "discover_music_bg"), for: .normal)
        button.imageView?.contentMode = .scaleAspectFill
        button.translatesAutoresizingMaskIntoConstraints = false
        button.addTarget(self, action: #selector(onClickMusicButton), for: .touchUpInside)
        return button
    }()

    private var bgImageViewRightCons: NSLayoutConstraint?
    private var descRightCons: NSLayoutConstraint?
    private var currentModel: VLDiscoveryItemModel?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setupModel(model: VLDiscoveryItemModel?) {
        currentModel = model
        iconImageView.image = UIImage(named: model?.iconName ?? "")
        titleLabel.text = model?.title
        descLabel.text = model?.desc
    }
    
    func updateLayout(layoutType: VLDiscoveryLayoutType) {
        docuButton.isHidden = layoutType == .half
        musicButton.isHidden = layoutType != .side
        switch layoutType {
        case .full:
            descRightCons?.constant = -114.fit
            bgImageViewRightCons?.constant = 0
            
        case .half:
            descRightCons?.constant = -16.fit
            bgImageViewRightCons?.constant = 0
            
        case .side:
            descRightCons?.constant = -43.fit
            bgImageViewRightCons?.constant = -60.fit
        }
        descRightCons?.isActive = true
        bgImageViewRightCons?.isActive = true
    }
    
    private func setupUI() {
        contentView.backgroundColor = .clear
        
        contentView.addSubview(musicButton)
        musicButton.topAnchor.constraint(equalTo: contentView.topAnchor).isActive = true
        musicButton.trailingAnchor.constraint(equalTo: contentView.trailingAnchor).isActive = true
        musicButton.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
        
        contentView.addSubview(bgImageView)
        bgImageView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor).isActive = true
        bgImageView.topAnchor.constraint(equalTo: contentView.topAnchor).isActive = true
        bgImageViewRightCons = bgImageView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -60)
        bgImageViewRightCons?.isActive = true
        bgImageView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
        
        bgImageView.addSubview(iconImageView)
        iconImageView.leadingAnchor.constraint(equalTo: bgImageView.leadingAnchor, constant: 16.fit).isActive = true
        iconImageView.topAnchor.constraint(equalTo: bgImageView.topAnchor, constant: 16.fit).isActive = true
        iconImageView.widthAnchor.constraint(equalToConstant: 20.fit).isActive = true
        iconImageView.heightAnchor.constraint(equalToConstant: 20.fit).isActive = true
        
        bgImageView.addSubview(titleLabel)
        titleLabel.leadingAnchor.constraint(equalTo: iconImageView.trailingAnchor, constant: 4.fit).isActive = true
        titleLabel.centerYAnchor.constraint(equalTo: iconImageView.centerYAnchor).isActive = true
        
        bgImageView.addSubview(docuButton)
        docuButton.topAnchor.constraint(equalTo: iconImageView.topAnchor).isActive = true
        docuButton.trailingAnchor.constraint(equalTo: bgImageView.trailingAnchor, constant: -16.fit).isActive = true
        
        bgImageView.addSubview(descLabel)
        descLabel.leadingAnchor.constraint(equalTo: iconImageView.leadingAnchor).isActive = true
        descLabel.topAnchor.constraint(equalTo: iconImageView.bottomAnchor, constant: 10.fit).isActive = true
        descLabel.bottomAnchor.constraint(equalTo: bgImageView.bottomAnchor, constant: -16.fit).isActive = true
        descRightCons = descLabel.trailingAnchor.constraint(equalTo: bgImageView.trailingAnchor, constant: -114.fit)
        descRightCons?.isActive = true
    }
    
    @objc
    private func onClickMusicButton() {
        let webViewVC = VLCommonWebViewController()
        webViewVC.urlString = "\(VLURLConfig.kURLPathH5ktv_feedback)?token=\(VLUserCenter.user.token)"
        webViewVC.isShowSystemWebButton = true
        UIViewController.cl_topViewController()?.navigationController?.pushViewController(webViewVC, animated: true)
    }
    @objc
    private func onClickDocumentButton() {
        let webViewVC = VLCommonWebViewController()
        webViewVC.urlString = "\(currentModel?.documentUrl ?? "")?token=\(VLUserCenter.user.token)"
        UIViewController.cl_topViewController()?.navigationController?.pushViewController(webViewVC, animated: true)
    }
}

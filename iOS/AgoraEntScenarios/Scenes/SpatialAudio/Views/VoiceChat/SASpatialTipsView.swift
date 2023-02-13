//
//  SASpatialTipsView.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/2/8.
//

import UIKit

class SASpatialTipsView: UIView {
    private lazy var lineView: UIImageView = {
        let imageView = UIImageView(image: UIImage.sceneImage(name: "tchead"))
        return imageView
    }()
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.text = "How to use 3D Spatial Audio Seat"
        label.textColor = UIColor(hex: "#040925", alpha: 1.0)
        label.font = UIFont.boldSystemFont(ofSize: 16)
        return label
    }()
    private lazy var sort1: UILabel = {
        let label = UILabel()
        label.text = "1"
        label.textColor = UIColor(hex: "#009FFF", alpha: 1.0)
        label.font = UIFont.boldSystemFont(ofSize: 13)
        label.cornerRadius(10)
        label.layer.borderColor = UIColor(hex: "#009FFF", alpha: 1.0)?.cgColor
        label.layer.borderWidth = 1
        label.textAlignment = .center
        return label
    }()
    private lazy var sort2: UILabel = {
        let label = UILabel()
        label.text = "2"
        label.textColor = UIColor(hex: "#009FFF", alpha: 1.0)
        label.font = UIFont.boldSystemFont(ofSize: 13)
        label.cornerRadius(10)
        label.layer.borderColor = UIColor(hex: "#009FFF", alpha: 1.0)?.cgColor
        label.layer.borderWidth = 1
        label.textAlignment = .center
        return label
    }()
    private lazy var step1: UILabel = {
        let label = UILabel()
        label.text = "Put on headsets"
        label.textColor = UIColor(hex: "#3C4267", alpha: 1.0)
        label.font = .systemFont(ofSize: 14)
        return label
    }()
    private lazy var step2: UILabel = {
        let label = UILabel()
        label.text = "Move avatar around to hear different spatial audio effects"
        label.textColor = UIColor(hex: "#3C4267", alpha: 1.0)
        label.font = .systemFont(ofSize: 14)
        label.numberOfLines = 2
        return label
    }()
    private lazy var coverImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage.sceneImage(name: "spatial_tips_coverimage"))
        return imageView
    }()
    private lazy var okButton: UIButton = {
        let button = UIButton()
        button.setTitle("I got it", for: .normal)
        button.setTitleColor(.white, for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 16)
        button.setBackgroundImage(UIImage.sceneImage(name: "spatial_tips_got_button_bg"), for: .normal)
        button.titleEdgeInsets(UIEdgeInsets(top: 0, left: 0, bottom: 7, right: 0))
        button.addTargetFor(self, action: #selector(onClickGotButton), for: .touchUpInside)
        return button
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func show() {
        guard let vc = UIViewController.cl_topViewController() as? SABaseViewController else { return }
        let controller = SAAlertViewController(compent: SAPresentedViewComponent(contentSize: CGSize(width: ScreenWidth,
                                                                                                     height: 480)),
                                               custom: self)
        vc.sa_presentViewController(controller)
    }
    
    private func setupUI() {
        addSubview(lineView)
        lineView.addSubview(titleLabel)
        lineView.translatesAutoresizingMaskIntoConstraints = false
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        
        lineView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        lineView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        lineView.heightAnchor.constraint(equalToConstant: 59).isActive = true
        lineView.topAnchor.constraint(equalTo: topAnchor).isActive = true
        
        titleLabel.centerXAnchor.constraint(equalTo: centerXAnchor).isActive = true
        titleLabel.bottomAnchor.constraint(equalTo: lineView.bottomAnchor, constant: -10).isActive = true
        
        addSubview(sort1)
        addSubview(sort2)
        addSubview(step1)
        addSubview(step2)
        addSubview(coverImageView)
        addSubview(okButton)
        sort1.translatesAutoresizingMaskIntoConstraints = false
        sort2.translatesAutoresizingMaskIntoConstraints = false
        step1.translatesAutoresizingMaskIntoConstraints = false
        step2.translatesAutoresizingMaskIntoConstraints = false
        coverImageView.translatesAutoresizingMaskIntoConstraints = false
        okButton.translatesAutoresizingMaskIntoConstraints = false
        
        sort1.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 15).isActive = true
        sort1.topAnchor.constraint(equalTo: lineView.bottomAnchor, constant: 10).isActive = true
        sort1.widthAnchor.constraint(equalToConstant: 20).isActive = true
        sort1.heightAnchor.constraint(equalToConstant: 20).isActive = true
        step1.leadingAnchor.constraint(equalTo: sort1.trailingAnchor, constant: 10).isActive = true
        step1.centerYAnchor.constraint(equalTo: sort1.centerYAnchor).isActive = true
        
        sort2.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 15).isActive = true
        sort2.topAnchor.constraint(equalTo: sort1.bottomAnchor, constant: 15).isActive = true
        sort2.widthAnchor.constraint(equalToConstant: 20).isActive = true
        sort2.heightAnchor.constraint(equalToConstant: 20).isActive = true
        step2.leadingAnchor.constraint(equalTo: step1.leadingAnchor).isActive = true
        step2.topAnchor.constraint(equalTo: sort2.topAnchor).isActive = true
        step2.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -15).isActive = true
        
        coverImageView.leadingAnchor.constraint(equalTo: sort2.leadingAnchor).isActive = true
        coverImageView.topAnchor.constraint(equalTo: step2.bottomAnchor, constant: 30).isActive = true
        coverImageView.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -15).isActive = true
        
        okButton.leadingAnchor.constraint(equalTo: coverImageView.leadingAnchor).isActive = true
        okButton.topAnchor.constraint(equalTo: coverImageView.bottomAnchor, constant: 20).isActive = true
        okButton.trailingAnchor.constraint(equalTo: coverImageView.trailingAnchor).isActive = true
    }
    
    @objc
    private func onClickGotButton() {
        guard let vc = UIViewController.cl_topViewController() else { return }
        vc.dismiss(animated: true)
    }
}

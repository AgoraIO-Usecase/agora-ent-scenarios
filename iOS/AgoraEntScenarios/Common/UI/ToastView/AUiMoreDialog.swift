//
//  AUiMoreDialog.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/4/18.
//

import UIKit

@objc public class AUiMoreDialog: UIView {
    private lazy var contentView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(hex: "#000000", alpha: 0.85)
        view.layer.cornerRadius = 15
        view.layer.maskedCorners = [.layerMinXMaxYCorner, .layerMaxXMaxYCorner]
        view.frame = CGRect(origin: .zero, size: CGSize(width: self.frame.size.width, height: 152))
        return view
    }()
    
    private lazy var reportButton: UIButton = {
        let button = UIButton()
        button.setImage(UIImage.sceneImage(name: "report_icon", bundleName: "VoiceChatRoomResource"), for: .normal)
        button.setTitle("voice_report".voice_localized(), for: .normal)
        button.titleLabel?.font = UIFont.systemFont(ofSize: 12)
        button.setTitleColor(.gray, for: .normal)
        button.contentHorizontalAlignment = .center;
        button.contentVerticalAlignment = .center
        
        button.addTargetFor(self, action: #selector(onAction(_:)), for: .touchUpInside)
        return button
    }()
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubViews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func _loadSubViews() {
        addSubview(contentView)
        contentView.addSubview(reportButton)
        let buttonSize = CGSize(width: 48, height: 68)
        reportButton.sizeToFit()
        let imgW:CGFloat = reportButton.imageView!.frame.size.width
        let imgH:CGFloat = reportButton.imageView!.frame.size.height
        let lblW:CGFloat = reportButton.titleLabel!.frame.size.width
        let lblH:CGFloat = reportButton.titleLabel!.frame.size.height
        //设置图片和文字的间距，这里可自行调整
        let margin:CGFloat = 4
 
        reportButton.imageEdgeInsets = UIEdgeInsets(top: -lblH - margin, left: 0, bottom: 0, right: -lblW)
        reportButton.titleEdgeInsets = UIEdgeInsets(top: imgH + margin, left: -imgW, bottom: 0, right: 0)
        reportButton.frame = CGRect(x: (contentView.width - buttonSize.width) / 2, y: 66, width: buttonSize.width, height: buttonSize.height)
    }
    
    open override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        super.touchesEnded(touches, with: event)
        if let point = touches.first?.location(in: self), !contentView.frame.contains(point) {
            hidden()
        }
    }
    
    @objc func show() {
        contentView.bottom = 0
        UIView.animate(withDuration: 0.3, delay: 0) {
            self.contentView.top = 0
        }
    }
    
    @objc func hidden() {
        UIView.animate(withDuration: 0.3, delay: 0) {
            self.contentView.bottom = 0
        } completion: { flag in
            self.removeFromSuperview()
        }
    }
    
    @objc private func onAction(_ sender: UIButton) {
        //TODO: mock success
        DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + 0.2) {
            ToastView.show(text: "voice_report_success".voice_localized())
        }
    }
}

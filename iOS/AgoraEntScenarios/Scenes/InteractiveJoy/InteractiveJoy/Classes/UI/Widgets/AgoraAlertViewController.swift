//
//  SafeAlertViewController.swift
//  AgoraEntScenarios
//
//  Created by qinhui on 2024/12/31.
//

import UIKit

class AgoraAlertViewController: UIViewController {
    
    var isBackgroundDismissEnabled: Bool = false
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // 设置模态样式
        modalPresentationStyle = .overFullScreen
        modalTransitionStyle = .crossDissolve
        
        // 添加背景点击手势
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(backgroundTapped))
        view.addGestureRecognizer(tapGesture)
    }
    
    @objc private func backgroundTapped() {
        if isBackgroundDismissEnabled {
            dismiss(animated: true)
        }
    }
}

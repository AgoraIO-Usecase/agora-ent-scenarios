//
//  BaseNavgationController.swift
//  Pods-VoiceRoomBaseUIKit_Example
//
//  Created by 朱继超 on 2022/8/24.
//

import UIKit
import ZSwiftBaseLib
import SVProgressHUD

@objcMembers
class VRBaseViewController: UIViewController {
    lazy var navigation: BaseNavigationView = .init(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: ZNavgationHeight))

    override public func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        view.backgroundColor = .white
        view.addSubview(navigation)
        setupNavigationAttributes()
        navigation.back.addTarget(self, action: #selector(backAction), for: .touchUpInside)
    }

    override var preferredStatusBarStyle: UIStatusBarStyle {
        .default
    }

    public func setupNavigationAttributes() {
        navigation.title.isHidden = !showTitle
        navigation.title.textColor = titleColor
        navigation.back.setImage(UIImage.voice_image(backImageName), for: .normal)
        navigation.backgroundColor = navBackgroundColor
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        SVProgressHUD.dismiss()
    }
}

extension VRBaseViewController {
    var showTitle: Bool { true }

    var titleColor: UIColor { .darkText }

    var backImageName: String { "back" }

    var navBackgroundColor: UIColor { .clear }

    @objc func backAction() {
        navigationController?.popViewController(animated: true)
    }
}

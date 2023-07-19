//
//  BaseNavgationController.swift
//  Pods-VoiceRoomBaseUIKit_Example
//
//  Created by 朱继超 on 2022/8/24.
//

import UIKit
import ZSwiftBaseLib
import SVProgressHUD

@objcMembers public class SABaseViewController: UIViewController {
    lazy var navigation: SABaseNavigationView = .init(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: ZNavgationHeight))

    override public func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationController?.navigationBar.isHidden = true
    }

    override public func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        view.backgroundColor = .white
        view.addSubview(navigation)
        setupNavigationAttributes()
        navigation.back.addTarget(self, action: #selector(backAction), for: .touchUpInside)
    }

    override public var preferredStatusBarStyle: UIStatusBarStyle {
        .darkContent
    }

    public func setupNavigationAttributes() {
        navigation.title.isHidden = !showTitle
        navigation.title.textColor = titleColor
        navigation.back.setImage(UIImage.spatial_image(backImageName), for: .normal)
        navigation.backgroundColor = navBackgroundColor
    }
    
    public override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        SVProgressHUD.dismiss()
    }
}

public extension SABaseViewController {
    var showTitle: Bool { true }

    var titleColor: UIColor { .darkText }

    var backImageName: String { "back" }

    var navBackgroundColor: UIColor { .clear }

    @objc func backAction() {
        navigationController?.popViewController(animated: true)
    }
}

//
//  BaseNavgationController.swift
//  Pods-VoiceRoomBaseUIKit_Example
//
//  Created by 朱继超 on 2022/8/24.
//

import UIKit
import ZSwiftBaseLib

@objcMembers public class VRBaseViewController: UIViewController {
     
    lazy var navigation: BaseNavigationView = {
        BaseNavigationView(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: ZNavgationHeight))
    }()

    public override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.navigationController?.navigationBar.isHidden = true
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        self.view.backgroundColor = .white
        self.view.addSubview(self.navigation)
        self.setupNavigationAttributes()
        self.navigation.back.addTarget(self, action: #selector(backAction), for: .touchUpInside)
    }
    
    public override var preferredStatusBarStyle: UIStatusBarStyle {
        .darkContent
    }
    
    public func setupNavigationAttributes() {
        self.navigation.title.isHidden = !self.showTitle
        self.navigation.title.textColor = self.titleColor
        self.navigation.back.setImage(UIImage(self.backImageName), for: .normal)
        self.navigation.backgroundColor = self.navBackgroundColor
    }
    
}

public extension VRBaseViewController {
    
    var showTitle: Bool { true }
    
    var titleColor: UIColor { .darkText }
    
    var backImageName: String { "back" }
    
    var navBackgroundColor: UIColor { .clear }
    
    @objc func backAction() {
        if self.navigationController?.viewControllers.count ?? 0 > 1 {
            self.navigationController?.popViewController(animated: true)
        } else {
            self.dismiss(animated: true)
        }
    }
}

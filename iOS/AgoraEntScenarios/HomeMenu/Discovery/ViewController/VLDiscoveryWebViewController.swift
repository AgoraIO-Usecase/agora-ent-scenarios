//
//  VLDiscoveyrController.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/10/20.
//

import UIKit

@objc
class VLDiscoveryWebViewController: VLCommonWebViewController {
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setNaviTitleName(NSLocalizedString("app_name", comment: ""))
    }
    
    override func setNaviTitleName(_ titleStr: String) {
        super.setNaviTitleName(NSLocalizedString("app_name", comment: ""))
    }
}

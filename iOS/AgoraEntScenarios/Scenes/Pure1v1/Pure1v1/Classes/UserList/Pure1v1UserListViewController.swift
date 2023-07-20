//
//  Pure1v1UserListViewController.swift
//  AFNetworking
//
//  Created by wushengtao on 2023/7/20.
//

import UIKit
import YYCategories

class Pure1v1UserListViewController: UIViewController {
    var userInfo: Pure1v1UserInfo?
    private lazy var naviBar: Pure1v1NaviBar = Pure1v1NaviBar(frame: CGRect(x: 0, y: UIDevice.current.aui_SafeDistanceTop, width: self.view.aui_width, height: 44))
    private lazy var noDataView: Pure1v1UserNoDataView = Pure1v1UserNoDataView(frame: self.view.bounds)
    private lazy var service: Pure1v1ServiceProtocol = Pure1v1ServiceImp(user: userInfo)
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.addSubview(noDataView)

        view.addSubview(naviBar)
        naviBar.backButton.addTarget(self, action: #selector(_backAction), for: .touchUpInside)
        naviBar.refreshButton.addTarget(self, action: #selector(_refreshAction), for: .touchUpInside)
        naviBar.refreshButton.isHidden = true
        service.joinRoom {[weak self] error in
            self?.naviBar.refreshButton.isHidden = false
        }
    }
}

extension Pure1v1UserListViewController {
    @objc func _backAction() {
        self.navigationController?.popViewController(animated: true)
    }
    
    @objc func _refreshAction() {
        service.getUserList { list in
            
        }
    }
}

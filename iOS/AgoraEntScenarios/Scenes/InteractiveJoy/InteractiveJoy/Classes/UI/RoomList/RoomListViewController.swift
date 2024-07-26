//
//  RoomListViewController.swift
//  Joy
//
//  Created by wushengtao on 2023/7/28.
//

import UIKit
import YYCategories
import AgoraRtcKit
import AgoraRtmKit
import AgoraCommon
import RTMSyncManager

class RoomListViewController: UIViewController {
    private var userInfo: InteractiveJoyUserInfo!
    
    
    deinit {
        JoyLogger.info("deinit-- RoomListViewController")
    }
    
    required init(userInfo: InteractiveJoyUserInfo) {
        super.init(nibName: nil, bundle: nil)
        self.userInfo = userInfo
        JoyLogger.info("init-- RoomListViewController")
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        navigationController?.interactivePopGestureRecognizer?.isEnabled = false
        

    }
}


//
//  Pure1v1CallViewController.swift
//  Pure1v1
//
//  Created by wushengtao on 2023/7/21.
//

import Foundation
import CallAPI

class Pure1v1CallViewController: UIViewController {
    var callApi: CallApiImpl?
    var targetUser: Pure1v1UserInfo? {
        didSet {
            roomInfoView.setRoomInfo(avatar: targetUser?.avatar ?? "",
                                     name: targetUser?.userName ?? "",
                                     id: targetUser?.userId ?? "",
                                     time: Int64(Date().timeIntervalSince1970 * 1000))
        }
    }
    private lazy var roomInfoView: Pure1v1RoomInfoView = Pure1v1RoomInfoView()
    lazy var bigCanvasView: UIView = UIView()
    lazy var smallCanvasView: UIView = UIView()
    private lazy var hangupButton: UIButton = {
        let button = UIButton(type: .custom)
        button.setImage(UIImage.sceneImage(name: "call_reject"), for: .normal)
        button.addTarget(self, action: #selector(_hangupAction), for: .touchUpInside)
        return button
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .black
        
        view.addSubview(bigCanvasView)
        view.addSubview(smallCanvasView)
        
        view.addSubview(roomInfoView)
        view.addSubview(hangupButton)
        
        roomInfoView.frame = CGRect(x: 15, y: UIDevice.current.aui_SafeDistanceTop, width: 202, height: 40)
        bigCanvasView.frame = view.bounds
        smallCanvasView.frame = CGRect(x: view.aui_width - 25 - 109, y: 82 + UIDevice.current.aui_SafeDistanceTop, width: 109, height: 163)
        smallCanvasView.layer.cornerRadius = 20
        smallCanvasView.clipsToBounds = true
        
        hangupButton.aui_size = CGSize(width: 70, height: 70)
        hangupButton.aui_bottom = self.view.aui_height - 20 - UIDevice.current.aui_SafeDistanceBottom
        hangupButton.aui_centerX = self.view.aui_width / 2
    }
    
    @objc private func _hangupAction() {
        callApi?.hangup(roomId: targetUser?.getRoomId() ?? "", completion: { err in
            
        })
    }
}

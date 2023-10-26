//
//  VRCreateViewController.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/10/25.
//

import Foundation

typealias VRCreateRoomBlock = (CGFloat) -> Void
typealias VRCreateRoomVCBlock = (String, String) -> Void

class VRCreateViewController: BaseViewController, VRCreateRoomViewDelegate {
    var createRoomBlock: VRCreateRoomBlock?
    var createRoomVCBlock: VRCreateRoomVCBlock?
    var isRoomPrivate: Bool = false
    var createRoomView: VoiceCreateRoomView?
    
    override func viewDidLoad() {
            super.viewDidLoad()
            view.backgroundColor = .white
            commonUI()
            setUpUI()
            NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow(_:)), name: UIResponder.keyboardWillShowNotification, object: nil)
        }
        
        func commonUI() {
            setBackgroundImage("online_list_BgIcon")
        }
        
        // MARK: - Public Methods
        
        override func configNavigationBar(_ navigationBar: UINavigationBar) {
            super.configNavigationBar(navigationBar)
        }
        
        
        func createBtnAction(_ roomModel: VRRoomEntity) {  //房主创建
            if roomModel.is_private && roomModel.roomPassword.count != 4 {
                return
            }
            createRoomVCBlock?(roomModel.name ?? "", roomModel.roomPassword )
        }
        
        func setUpUI() {
            let createRoomView = VoiceCreateRoomView(frame: CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width, height: 330), withDelegate: self)
            view.addSubview(createRoomView)
            self.createRoomView = createRoomView
        }
        
        @objc func keyboardWillShow(_ notification: Notification) {
            createRoomBlock?(isRoomPrivate ? 560 : 520)
            createRoomView?.frame = CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width, height: isRoomPrivate ? 560 : 520)
        }
        
        func didCreateRoomAction(_ type: VRCreateRoomActionType) {
            if type == .normal {
                isRoomPrivate = false
                createRoomBlock?(330)
                createRoomView?.frame = CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width, height: 330)
            } else if type == .encrypt {
                isRoomPrivate = true
                createRoomBlock?(420)
                createRoomView?.frame = CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width, height: 420)
            }
        }
}

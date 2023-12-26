//
//  VRCreateViewController.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/10/25.
//

import Foundation

typealias VRCreateRoomBlock = (CGFloat) -> Void
typealias VRCreateRoomVCBlock = (String, String) -> Void

class VRCreateViewController: UIViewController, VRCreateRoomViewDelegate {
    var createRoomBlock: VRCreateRoomBlock?
    var createRoomVCBlock: VRCreateRoomVCBlock?
    var isRoomPrivate: Bool = false
    var createRoomView: VoiceCreateRoomView?
    
    override func viewDidLoad() {
            super.viewDidLoad()
            view.backgroundColor = .white
            setUpUI()
            NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow(_:)), name: UIResponder.keyboardWillShowNotification, object: nil)
        }

        func createBtnAction(_ roomModel: VRRoomEntity) {  //房主创建
            if roomModel.is_private && roomModel.roomPassword.count != 4 {
                return
            }
            createRoomVCBlock?(roomModel.name ?? "", roomModel.roomPassword )
        }
        
        func setUpUI() {
            let createRoomView = VoiceCreateRoomView(frame: CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width, height: 343), withDelegate: self)
            view.addSubview(createRoomView)
            self.createRoomView = createRoomView
        }
        
        @objc func keyboardWillShow(_ notification: Notification) {
            createRoomBlock?(isRoomPrivate ? 520 : 480)
            createRoomView?.frame = CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width, height: isRoomPrivate ? 520 : 480)
        }
        
        func didCreateRoomAction(_ type: VRCreateRoomActionType) {
            if type == .normal {
                isRoomPrivate = false
                createRoomBlock?(343)
                createRoomView?.frame = CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width, height: 343)
            } else if type == .encrypt {
                isRoomPrivate = true
                createRoomBlock?(400)
                createRoomView?.frame = CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width, height: 400)
            }
        }
}

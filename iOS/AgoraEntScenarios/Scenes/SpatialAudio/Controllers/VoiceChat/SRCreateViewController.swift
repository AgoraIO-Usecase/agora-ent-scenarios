//
//  VRCreateViewController.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/10/25.
//

import Foundation

typealias SRCreateRoomBlock = (CGFloat) -> Void
typealias SRCreateRoomVCBlock = (String, String) -> Void

class SRCreateViewController: UIViewController, SRCreateRoomViewDelegate {
    var createRoomBlock: SRCreateRoomBlock?
    var createRoomVCBlock: SRCreateRoomVCBlock?
    var isRoomPrivate: Bool = false
    var createRoomView: SRCreateRoomView?
    
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
            let createRoomView = SRCreateRoomView(frame: CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width, height: 330), withDelegate: self)
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

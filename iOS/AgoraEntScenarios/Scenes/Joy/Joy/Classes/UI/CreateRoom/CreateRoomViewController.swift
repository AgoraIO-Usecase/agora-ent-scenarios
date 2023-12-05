//
//  CreateRoomViewController.swift
//  AFNetworking
//
//  Created by wushengtao on 2023/12/5.
//

import Foundation

private let randomRoomName = [
    "show_create_room_name1".joyLocalization(),
    "show_create_room_name2".joyLocalization(),
    "show_create_room_name3".joyLocalization(),
    "show_create_room_name4".joyLocalization(),
    "show_create_room_name5".joyLocalization(),
    "show_create_room_name6".joyLocalization(),
    "show_create_room_name7".joyLocalization(),
    "show_create_room_name8".joyLocalization(),
    "show_create_room_name9".joyLocalization(),
    "show_create_room_name10".joyLocalization(),
]

class CreateRoomViewController: UIViewController {
    private var service: JoyServiceProtocol!
    private lazy var naviBar: JoyNavigationBar = {
        let bar = JoyNavigationBar(frame: CGRect(x: 0, y: UIDevice.current.aui_SafeDistanceTop, width: self.view.width, height: 44))
        bar.navibarType = .light
        return bar
    }()
    
    private lazy var backgroundImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage.sceneImage(name: "room_bg"))
        imageView.frame = view.bounds
        imageView.contentMode = .scaleAspectFill
        return imageView
    }()
    
    private lazy var textField: UITextField = {
        let tf = UITextField(frame: CGRect(x: 12, y: UIDevice.current.aui_SafeDistanceTop + 224,
                                           width: view.width - 24, height: 48))
        tf.font = .joy_M_15
        tf.textColor = .joy_main_text
        tf.tintColor = .joy_placeholder_text
        tf.placeholder = "create_room_title".joyLocalization()
        let leftPaddingView = UIView(frame: CGRect(x: 0, y: 0, width: 12, height: tf.frame.height))
        tf.leftView = leftPaddingView
        tf.leftViewMode = .always

        let button = UIButton(type: .custom)
        button.setImage(UIImage.sceneImage(name: "textfield_refresh"), for: .normal)
        button.contentEdgeInsets = UIEdgeInsets(top: 12, left: 15, bottom: 15, right: 12)
        button.addTarget(self, action: #selector(onRandomAction), for: .touchUpInside)
        tf.rightView = button
        tf.rightViewMode = .always
        
        tf.backgroundColor = .joy_cover
        tf.layer.cornerRadius = 8
        tf.layer.masksToBounds = true
        return tf
    }()
    
    private lazy var createButton: UIButton = {
        let button = UIButton(type: .custom)
        button.frame = CGRect(x: (self.view.aui_width - 230) / 2,
                              y: self.view.aui_height - UIDevice.current.aui_SafeDistanceBottom - 42 - 19,
                              width: 230,
                              height: 42)
        button.backgroundColor = UIColor(hexString: "#345dff")
        button.setCornerRadius(21)
        button.setTitle("user_list_create".joyLocalization(), for: .normal)
        button.setjoyHorizonalDefaultGradientBackground()
        button.titleLabel?.font = UIFont.systemFont(ofSize: 16)
        button.setTitleColor(.white, for: .normal)
        button.addTarget(self, action: #selector(onCreateAction), for: .touchUpInside)
        return button
    }()
    
    required init(service: JoyServiceProtocol? = nil) {
        super.init(nibName: nil, bundle: nil)
        self.service = service
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.addSubview(backgroundImageView)
        view.addSubview(naviBar)
        view.addSubview(textField)
        view.addSubview(createButton)
    }
}

extension CreateRoomViewController {
    @objc private func onRandomAction() {
        let roomNameIdx = Int(arc4random()) % randomRoomName.count
        let roomName = randomRoomName[roomNameIdx]
        textField.text = "\(roomName)\(Int(arc4random()) % 1000000)"
    }
    
    @objc private func onCreateAction() {
        guard let roomName = textField.text else {
            return
        }
        
        service.createRoom(roomName: roomName) {[weak self] info, error in
            guard let self = self else {return}
            if let error = error {
                AUIToast.show(text: error.localizedDescription)
                return
            }
            guard let info = info else { return }
//            self.gotoRoom(roomInfo: info)
        }
    }
}

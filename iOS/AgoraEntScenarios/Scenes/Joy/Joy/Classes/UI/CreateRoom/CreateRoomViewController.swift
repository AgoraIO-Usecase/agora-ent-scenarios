//
//  CreateRoomViewController.swift
//  AFNetworking
//
//  Created by wushengtao on 2023/12/5.
//

import UIKit
import SVProgressHUD
import AgoraRtcKit

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
    "show_create_room_name11".joyLocalization(),
    "show_create_room_name12".joyLocalization(),
    "show_create_room_name13".joyLocalization(),
    "show_create_room_name14".joyLocalization(),
    "show_create_room_name15".joyLocalization(),
    "show_create_room_name16".joyLocalization(),
    "show_create_room_name17".joyLocalization(),
    "show_create_room_name18".joyLocalization(),
    "show_create_room_name19".joyLocalization(),
    "show_create_room_name20".joyLocalization(),
    "show_create_room_name21".joyLocalization(),
]

class CreateRoomViewController: UIViewController {
    private var service: JoyServiceProtocol!
    private var currentUserInfo: JoyUserInfo!
    private lazy var naviBar: JoyNavigationBar = {
        let bar = JoyNavigationBar(frame: CGRect(x: 0, y: UIDevice.current.aui_SafeDistanceTop, width: self.view.width, height: 44))
        bar.navibarType = .light
        return bar
    }()
    
    private lazy var backgroundCanvasView: UIView = UIView(frame: self.view.bounds)
    
    private lazy var bannerView: JoyBannerView = JoyBannerView(frame: CGRect(x: 0, y: naviBar.aui_bottom, width: self.view.aui_width, height: 168))
    
    private lazy var textField: UITextField = {
        let tf = UITextField(frame: CGRect(x: 12, y: UIDevice.current.aui_SafeDistanceTop + 224,
                                           width: view.width - 24, height: 48))
        tf.font = .joy_M_15
        tf.textColor = .joy_main_text
        let attributes: [NSAttributedString.Key: Any] = [
            .foregroundColor: UIColor.joy_placeholder_text
        ]
        tf.attributedPlaceholder = NSAttributedString(string: "create_room_title".joyLocalization(),
                                                      attributes: attributes)
        let leftPaddingView = UIView(frame: CGRect(x: 0, y: 0, width: 12, height: tf.frame.height))
        tf.leftView = leftPaddingView
        tf.leftViewMode = .always

        let button = UIButton(type: .custom)
        button.setImage(UIImage.sceneImage(name: "textfield_refresh"), for: .normal)
        button.contentEdgeInsets = UIEdgeInsets(top: 12, left: 15, bottom: 15, right: 12)
        button.addTarget(self, action: #selector(onRandomAction), for: .touchUpInside)
        tf.rightView = button
        tf.rightViewMode = .always
        
        tf.backgroundColor = UIColor(red: 0.047, green: 0.035, blue: 0.137, alpha: 0.3)
        tf.layer.cornerRadius = 8
        tf.layer.masksToBounds = true
        return tf
    }()
    
    private lazy var tipsLabel: UILabel = {
        let label = UILabel()
        label.textAlignment = .center
        label.textColor = .joy_main_text
        label.font = .joy_R_11
        label.clipsToBounds = true
        
        let textAttr = NSAttributedString(string: "joy_usage_tips".joyLocalization())
        let attach = NSTextAttachment()
        attach.image = UIImage.sceneImage(name: "icon_notice")!
        let imageSize = CGSize(width: 14, height: 14)
        attach.bounds = CGRect(origin: CGPoint(x: 0, y: (label.font.capHeight - imageSize.height).rounded() / 2), size: imageSize)
        let imgAttr = NSAttributedString(attachment: attach)
        let attr = NSMutableAttributedString()
        attr.append(imgAttr)
        attr.append(textAttr)
        label.attributedText = attr
        label.sizeToFit()
        return label
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
        button.setBackgroundImage(UIImage.sceneImage(name: "room_create_button_bg2"), for: .normal)
        button.titleLabel?.font = UIFont.systemFont(ofSize: 16)
        button.setTitleColor(.white, for: .normal)
        button.addTarget(self, action: #selector(onCreateAction), for: .touchUpInside)
        return button
    }()
    
    required init(currentUserInfo: JoyUserInfo, service: JoyServiceProtocol) {
        super.init(nibName: nil, bundle: nil)
        self.service = service
        self.currentUserInfo = currentUserInfo
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .black
        view.addSubview(backgroundCanvasView)
        view.addSubview(naviBar)
        view.addSubview(bannerView)
        view.addSubview(textField)
        view.addSubview(tipsLabel)
        view.addSubview(createButton)
        tipsLabel.aui_bottom = createButton.aui_top - 25
        tipsLabel.aui_centerX = view.width / 2
        
        startPreview()
        
        CloudBarrageAPI.shared.getBannerList {[weak self] err, bannerList in
            self?.bannerView.bannerList = JoyBannerArray(bannerList: bannerList)
        }
    }
    
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        super.touchesEnded(touches, with: event)
        self.view.endEditing(true)
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        if navigationController?.visibleViewController is RoomViewController {
            return
        }
        stopPreview()
    }
    
    private func startPreview() {
        JoyAuthorizedManager.checkCameraAuthorized(parent: self)
        if let engine = CloudBarrageAPI.shared.apiConfig?.engine {
            let broadcasterCanvas = AgoraRtcVideoCanvas()
            broadcasterCanvas.uid = 0
            broadcasterCanvas.view = backgroundCanvasView
            engine.enableVideo()
//            engine.enableAudio()
            engine.startPreview()
            engine.setupLocalVideo(broadcasterCanvas)
        }
    }
    
    private func stopPreview() {
        if let engine = CloudBarrageAPI.shared.apiConfig?.engine {
//            engine.disableVideo()
            engine.stopPreview()
        }
    }
}

extension CreateRoomViewController {
    @objc private func onRandomAction() {
        let roomNameIdx = Int(arc4random()) % randomRoomName.count
        let roomName = randomRoomName[roomNameIdx]
        textField.text = roomName
    }
    
    @objc private func onCreateAction() {
        guard let roomName = textField.text, !roomName.isEmpty else {
            AUIToast.show(text: "create_room_name_empty_tips".joyLocalization())
            return
        }
        SVProgressHUD.show()
        service.createRoom(roomName: roomName) {[weak self] info, error in
            guard let self = self else {return}
            SVProgressHUD.dismiss()
            if let error = error {
                AUIToast.show(text: error.localizedDescription)
                return
            }
            guard let info = info else { return }
            JoyAuthorizedManager.checkAudioAuthorized(parent: self)
            let vc = RoomViewController(roomInfo: info, currentUserInfo: currentUserInfo, service: service)
            self.navigationController?.pushViewController(vc, animated: true)
            
            if let nv = self.navigationController, let index = nv.viewControllers.firstIndex(of: self) {
                // 创建一个新的导航控制器堆栈数组，将vc1移除
                var viewControllers = nv.viewControllers
                viewControllers.remove(at: index)
                nv.viewControllers = viewControllers
            }
        }
    }
}

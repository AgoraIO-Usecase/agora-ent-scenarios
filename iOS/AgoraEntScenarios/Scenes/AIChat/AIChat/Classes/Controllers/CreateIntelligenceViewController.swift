//
//  CreateIntelligenceViewController.swift
//  AIChat
//
//  Created by 朱继超 on 2024/8/29.
//

import UIKit
import ZSwiftBaseLib
import AgoraCommon
import SDWebImage
import AgoraChat

open class CreateIntelligenceViewController: UIViewController {
    
    private let chatBotService: AIChatBotServiceProtocol = AIChatBotImplement()

    lazy var background: UIImageView = {
        UIImageView(frame: self.view.bounds).image(UIImage(named: "roomList", in: .chatAIBundle, with: nil)!).contentMode(.scaleAspectFill)
    }()
        
    lazy var navigation: AIChatNavigation = {
        AIChatNavigation(frame: CGRect(x: 0, y: 0, width: self.view.frame.width, height: NavigationHeight), textAlignment: .center, rightTitle: nil).backgroundColor(.clear)
    }()

    lazy var leftContainer: UIView = {
        UIView {
            UIView(frame: CGRect(x: 0, y: 0, width: 87, height: 40)).backgroundColor(.clear)
            UIButton(type: .custom).frame(CGRect(x: 0, y: 0, width: 77, height: 40)).title("aichat_create_bot_name".toSceneLocalization() as String, .normal).textColor(UIColor(0x303553), .normal).isUserInteractionEnabled(false).font(.systemFont(ofSize: 16,weight: .medium))
            UIView(frame: CGRect(x: 74, y: 10, width: 1, height: 20)).backgroundColor(UIColor(0x979CBB))
        }
    }()
    
    lazy var rightContainer: UIView = {
        UIView {
            UIView(frame: CGRect(x: 0, y: 0, width: 50, height: 48)).backgroundColor(.clear)
            self.limitLabel
        }
    }()
    
    lazy var limitLabel: UILabel = {
        UILabel(frame: CGRect(x: 0, y: 0, width: 40, height: 48)).text("0/32").textColor(UIColor(0x979cbb)).font(.systemFont(ofSize: 16)).backgroundColor(.clear)
    }()

    private var avatarButton: UIImageView!
    private var nameTextField: UITextField!
    private var introTextView: LimitTextView!
    private var descriptionTextView: LimitTextView!
    private var createButton: UIButton!
    private var changeAvatarButton: UIButton!
    
    private var avatarURL = ""
    
    private var createClosure: ((AIChatBotProfileProtocol) -> Void)?
    
    private var contactsCount = 0
    
    required public init(createClosure: @escaping (AIChatBotProfileProtocol) -> Void) {
        super.init(nibName: nil, bundle: nil)
        self.createClosure = createClosure
    }
    
    required public init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        self.view.addSubViews([self.navigation,self.background])
        self.view.bringSubviewToFront(self.navigation)
        self.setupUI()
        self.changeAvatarAction()
        self.navigation.clickClosure = { [weak self] type,_ in
            if type == .back {
                self?.dismiss(animated: true)
            }
        }
    }

    private func setupUI() {
        self.navigation.contentMode = .scaleAspectFill
        self.navigation.image = UIImage(named: "headerbg", in: .chatAIBundle, with: nil)
        self.navigation.contentMode = .scaleAspectFill
        self.navigation.separateLine.isHidden = true
        self.navigation.leftItem.setImage(UIImage(named: "close", in: .chatAIBundle, with: nil), for: .normal)
        let contacts = AgoraChatClient.shared().contactManager?.getContacts() ?? []
        self.contactsCount = contacts.filter { !$0.contains("group") }.count
        self.navigation.subtitle = "\("aichat_create_count".toSceneLocalization() as String)\(self.contactsCount+1)/3"
        self.navigation.title = "aichat_create_title".toSceneLocalization() as String
        // 头像按钮
        self.avatarButton = UIImageView()
        self.avatarButton.layer.cornerRadius = 60
        self.avatarButton.layer.masksToBounds = true
        self.avatarButton.translatesAutoresizingMaskIntoConstraints = false
        self.avatarButton.backgroundColor = .systemGray5
        self.view.addSubview(self.avatarButton)
        
        // 在圆形头像的右下角添加一个按钮
        self.changeAvatarButton = UIButton(type: .custom)
        self.changeAvatarButton.setImage(UIImage(named: "some_icon"), for: .normal)
        self.changeAvatarButton.translatesAutoresizingMaskIntoConstraints = false
        self.changeAvatarButton.backgroundColor = .white
        self.changeAvatarButton.layer.cornerRadius = 12  // 按钮大小 24x24，圆角 12
        self.changeAvatarButton.layer.masksToBounds = true
        self.changeAvatarButton.setImage(UIImage(named: "change_avatar", in: .chatAIBundle, with: nil), for: .normal)
        self.changeAvatarButton.addTarget(self, action: #selector(changeAvatarAction), for: .touchUpInside)
        self.view.addSubview(self.changeAvatarButton)

        // 名称输入框
        self.nameTextField = UITextField()
        self.nameTextField.attributedPlaceholder = NSAttributedString(
            string: "aichat_create_bot_placeholder".toSceneLocalization() as String,
            attributes: [NSAttributedString.Key.foregroundColor: UIColor(0x979CBB)]
        )
        self.nameTextField.clearButtonMode = .whileEditing
        self.nameTextField.font = UIFont.systemFont(ofSize: 16)
        self.nameTextField.translatesAutoresizingMaskIntoConstraints = false
        self.nameTextField.delegate = self
        self.nameTextField.backgroundColor = .white
        self.nameTextField.leftView = self.leftContainer
        self.nameTextField.leftViewMode = .always
        self.nameTextField.rightView = self.rightContainer
        self.nameTextField.rightViewMode = .always
        self.nameTextField.cornerRadius(16)
        self.view.addSubview(self.nameTextField)

        // 设定简介输入框
        self.introTextView = LimitTextView(introduce:
                                            "aichat_set_introduce".toSceneLocalization() as String, placeHolder: "aichat_bot_introduce_placeholder".toSceneLocalization() as String, limitCount: 32)
        self.introTextView.backgroundColor = .white
        self.introTextView.translatesAutoresizingMaskIntoConstraints = false
        self.introTextView.layer.cornerRadius = 16
        self.view.addSubview(self.introTextView)

        // 设定描述输入框
        self.descriptionTextView = LimitTextView(introduce: "aichat_set_description".toSceneLocalization() as String, placeHolder: "aichat_set_description_placeholder".toSceneLocalization() as String, limitCount: 512,needListenKeyboard: true)
        self.descriptionTextView.backgroundColor = .white
        self.descriptionTextView.layer.cornerRadius = 16
        self.descriptionTextView.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(self.descriptionTextView)
        self.descriptionTextView.editStateChanged = { [weak self] state, textView, height in
            if textView.state == .editing {
                self?.moveViewsForKeyboard(height: height)
            } else {
                self?.resetViewsPosition()
            }
        }
        
        
        let shadowImageView = UIImageView(image: UIImage(named: "create_bot_large_shadow", in: .chatAIBundle, with: nil))
        shadowImageView.backgroundColor = .clear
        shadowImageView.contentMode = .scaleAspectFill
        shadowImageView.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(shadowImageView)

        // 创建智能体按钮
        self.createButton = UIButton(type: .system)
        self.createButton.setBackgroundImage(UIImage(named: "create_bot_large", in: .chatAIBundle, with: nil), for: .normal)
        self.createButton.backgroundColor = .clear
        self.createButton.translatesAutoresizingMaskIntoConstraints = false
        self.createButton.contentMode = .scaleAspectFill
        self.createButton.addTarget(self, action: #selector(createBot), for: .touchUpInside)
        self.view.addSubview(self.createButton)
        self.createButton.cornerRadius(23)

        let cos45 = sqrt(2) / 2
        // 添加布局约束
        NSLayoutConstraint.activate([
            // 头像按钮布局
            self.avatarButton.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            self.avatarButton.topAnchor.constraint(equalTo: view.topAnchor, constant: NavigationHeight+28),
            self.avatarButton.widthAnchor.constraint(equalToConstant: 120),
            self.avatarButton.heightAnchor.constraint(equalToConstant: 120),

            // 名称输入框布局
            self.nameTextField.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            self.nameTextField.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            self.nameTextField.topAnchor.constraint(equalTo: avatarButton.bottomAnchor, constant: 40),
            self.nameTextField.heightAnchor.constraint(equalToConstant: 48),

            // 设定简介输入框布局
            self.introTextView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            self.introTextView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            self.introTextView.topAnchor.constraint(equalTo: self.nameTextField.bottomAnchor, constant: 20),
            self.introTextView.heightAnchor.constraint(equalToConstant: 128),

            // 设定描述输入框布局
            self.descriptionTextView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            self.descriptionTextView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            self.descriptionTextView.topAnchor.constraint(equalTo: self.introTextView.bottomAnchor, constant: 20),
            self.descriptionTextView.heightAnchor.constraint(equalToConstant: ScreenHeight < 812 ? 130:216),

            // 创建智能体按钮布局
            self.createButton.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            self.createButton.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            self.createButton.topAnchor.constraint(equalTo: ScreenHeight < 812 ? self.view.bottomAnchor:self.descriptionTextView.bottomAnchor, constant: ScreenHeight < 812 ? -66:40),
            self.createButton.heightAnchor.constraint(equalToConstant: 46),
            
            // 阴影图片布局
            shadowImageView.centerXAnchor.constraint(equalTo: self.createButton.centerXAnchor),
            shadowImageView.topAnchor.constraint(equalTo: self.createButton.topAnchor,constant: 8),
            shadowImageView.widthAnchor.constraint(equalTo: self.createButton.widthAnchor),
            shadowImageView.heightAnchor.constraint(equalToConstant: 56),
            
            // 在头像按钮右下角添加按钮
            self.changeAvatarButton.widthAnchor.constraint(equalToConstant: 24),
            self.changeAvatarButton.heightAnchor.constraint(equalToConstant: 24),
            self.changeAvatarButton.centerXAnchor.constraint(equalTo: self.avatarButton.centerXAnchor, constant: 60 * cos45),
            self.changeAvatarButton.centerYAnchor.constraint(equalTo: self.avatarButton.centerYAnchor, constant: 60 * cos45)
            
        ])
        self.view.bringSubviewToFront(self.navigation)
    }

    private func moveViewsForKeyboard(height: CGFloat) {
        let moveDistance = self.view.frame.height-height-240
        UIView.animate(withDuration: 0.2) {
            self.avatarButton.transform = CGAffineTransform(translationX: 0, y: -moveDistance)
            self.changeAvatarButton.transform = CGAffineTransform(translationX: 0, y: -moveDistance)
            self.nameTextField.transform = CGAffineTransform(translationX: 0, y: -moveDistance)
            self.introTextView.transform = CGAffineTransform(translationX: 0, y: -moveDistance)
            self.descriptionTextView.transform = CGAffineTransform(translationX: 0, y: -moveDistance)
        }
    }

    private func resetViewsPosition() {
        UIView.animate(withDuration: 0.2) {
            self.avatarButton.transform = .identity
            self.changeAvatarButton.transform = .identity
            self.nameTextField.transform = .identity
            self.introTextView.transform = .identity
            self.descriptionTextView.transform = .identity
        }
    }

    open override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        self.view.endEditing(true)
    }
}

extension CreateIntelligenceViewController: UITextFieldDelegate {
    
    public func textField(_ textField: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
        let text = textField.text ?? ""
        let length = text.count + string.count - range.length
        self.limitLabel.text = "\(length)/32"
        return length <= 32
    }

    public func textFieldShouldBeginEditing(_ textField: UITextField) -> Bool {
        true
    }

    public func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return true
    }
    
    @objc private func changeAvatarAction() {
        let random = UInt.random(in: 1...10)
        let string = "https://fullapp.oss-cn-beijing.aliyuncs.com/ent-scenarios/images/aichat/avatar/avatar\(random).png"
        self.avatarURL = string
        guard let url = URL(string: string) else { return }
        self.avatarButton.sd_setImage(with: url,placeholderImage: UIImage(named: "botavatar", in: .chatAIBundle, with: nil), options: .retryFailed, context: nil)
    }
    
    @objc private func createBot() {
        if self.contactsCount >= 3 {
            ToastView.show(text: "aichat_bot_create_limited".toSceneLocalization() as String)
            return
        }
        guard let name = self.nameTextField.text, !name.isEmpty else {
            ToastView.show(text: "请输入智能体名称")
            return
        }
        if name.count > 32 {
            ToastView.show(text: "智能体名称不能超过32个字符")
            return
        }
        if self.introTextView.finalText.isEmpty {
            ToastView.show(text: "请输入智能体简介")
            return
        }
        if self.introTextView.finalText.count > 32 {
            ToastView.show(text: "智能体简介不能超过32个字符")
            return
        }
        if self.descriptionTextView.finalText.isEmpty {
            ToastView.show(text: "请输入智能体描述")
            return
        }
        if self.descriptionTextView.finalText.count > 512 {
            ToastView.show(text: "智能体描述不能超过512个字符")
            return
        }
        let bot = AIChatBotProfile()
        bot.botName = name
        bot.botIcon = self.avatarURL
        bot.prompt = self.descriptionTextView.finalText
        bot.botDescription = self.introTextView.finalText
        bot.type = .custom
        if let avatar = bot.botIcon.components(separatedBy: "/").last?.components(separatedBy: ".").first {
            bot.voiceId = AIChatBotImplement.voiceIds[avatar] ?? "female-chengshu"
        }
        self.createButton.isEnabled = false
        self.chatBotService.createChatBot(bot: bot) { [weak self] error,userId  in
            guard let `self` = self else { return }
            if error == nil {
                bot.botId = userId
                DispatchQueue.main.async {
                    self.chatToBot(bot: bot)
                }
            } else {
                aichatPrint("创建智能体失败 error:\(error?.localizedDescription ?? "")")
                ToastView.show(text: "创建失败:\(error?.localizedDescription ?? "")", postion: .center)
            }
            DispatchQueue.main.async {
                self.createButton.isEnabled = true
            }
        }
    }
    
    private func chatToBot(bot: AIChatBotProfileProtocol) {
        let conversation = AgoraChatClient.shared().chatManager?.getConversation(bot.botId, type: .chat, createIfNotExist: true)
        let timeMessage = AgoraChatMessage(conversationID: bot.botId, body: AgoraChatCustomMessageBody(event: "AIChat_alert_message", customExt: nil), ext: ["something":"\(UInt64(Date().timeIntervalSince1970))"])
        if var ext = conversation?.ext {
            ext.merge(bot.toDictionary()) { _, new in
                new
            }
            conversation?.ext = ext
        } else {
            conversation?.ext = bot.toDictionary()
        }
        conversation?.insert(timeMessage, error: nil)
        let alertMessage = AgoraChatMessage(conversationID: bot.botId, body: AgoraChatCustomMessageBody(event: "AIChat_alert_message", customExt: nil), ext: ["something":"aichat_create_bot_successful".toSceneLocalization() as String])
        self.createClosure?(bot)
        conversation?.insert(alertMessage, error: nil)
        let welcomeMessage = AgoraChatMessage(conversationID: bot.botId, from: bot.botId, to: AppContext.shared.getAIChatUid(), body: AgoraChatTextMessageBody(text: String(format: "aichat_bot_welcome_message".toSceneLocalization() as String, bot.botName)), ext: nil)
        welcomeMessage.direction = .receive
        conversation?.insert(welcomeMessage, error: nil)
        DispatchQueue.main.async {
            self.dismiss(animated: false) {
                let chatVC = AIChatViewController(bot: bot)
                UIViewController.currentController?.navigationController?.pushViewController(chatVC, animated: true)
            }
        }
    }
    
    
}


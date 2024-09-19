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
            UIButton(type: .custom).frame(CGRect(x: 0, y: 0, width: 77, height: 40)).title("名称", .normal).textColor(UIColor(0x303553), .normal).isUserInteractionEnabled(false).font(.systemFont(ofSize: 16))
            UIView(frame: CGRect(x: 74, y: 10, width: 1, height: 20)).backgroundColor(UIColor(0x979CBB))
        }
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
        self.view.addSubview(self.background)
        self.setupUI()
        self.view.addSubview(self.navigation)
        self.changeAvatarAction()
        self.navigation.clickClosure = { [weak self] type,_ in
            if type == .back {
                self?.dismiss(animated: true)
            }
        }
    }

    private func setupUI() {
        self.navigation.contentMode = .scaleAspectFill
        self.navigation.backgroundColor = UIColor(patternImage: UIImage(named: "headerbg", in: .chatAIBundle, with: nil)!)
        self.navigation.leftItem.setImage(UIImage(named: "close", in: .chatAIBundle, with: nil), for: .normal)
        let contacts = AgoraChatClient.shared().contactManager?.getContacts() ?? []
        self.contactsCount = contacts.count
        self.navigation.subtitle = "创建个数\(self.contactsCount)/3"
        self.navigation.title = "创建智能体"
        // 头像按钮
        self.avatarButton = UIImageView()
        self.avatarButton.layer.cornerRadius = 50
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
        self.nameTextField.placeholder = "请输入智能体名称"
        self.nameTextField.clearButtonMode = .whileEditing
        self.nameTextField.font = UIFont.systemFont(ofSize: 16)
        self.nameTextField.translatesAutoresizingMaskIntoConstraints = false
        self.nameTextField.delegate = self
        self.nameTextField.backgroundColor = .white
        self.nameTextField.leftView = self.leftContainer
        self.nameTextField.leftViewMode = .always
        self.nameTextField.cornerRadius(16)
        self.view.addSubview(self.nameTextField)

        // 设定简介输入框
        self.introTextView = LimitTextView(introduce: "设定简介", placeHolder: "对创建的智能体进行简单介绍", limitCount: 32)
        self.introTextView.backgroundColor = .white
        self.introTextView.translatesAutoresizingMaskIntoConstraints = false
        self.introTextView.layer.cornerRadius = 16
        self.view.addSubview(self.introTextView)

        // 设定描述输入框
        self.descriptionTextView = LimitTextView(introduce: "设定描述", placeHolder: "填写您创建智能体设定的详细描述。如：你是一位经验丰富的英语老师，拥有激发学生学习热情的教学方法。你善于运用幽默和实际应用案例，使对话充满趣味。", limitCount: 1024,needListenKeyboard: true)
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

        // 创建智能体按钮
        self.createButton = UIButton(type: .system)
        self.createButton.setBackgroundImage(UIImage(named: "create_bot_large", in: .chatAIBundle, with: nil), for: .normal)
        self.createButton.backgroundColor = .clear
        self.createButton.translatesAutoresizingMaskIntoConstraints = false
        self.createButton.contentMode = .scaleAspectFill
        self.createButton.addTarget(self, action: #selector(createBot), for: .touchUpInside)
        self.view.addSubview(self.createButton)

        let cos45 = sqrt(2) / 2
        // 添加布局约束
        NSLayoutConstraint.activate([
            // 头像按钮布局
            self.avatarButton.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            self.avatarButton.topAnchor.constraint(equalTo: view.topAnchor, constant: NavigationHeight+28),
            self.avatarButton.widthAnchor.constraint(equalToConstant: 100),
            self.avatarButton.heightAnchor.constraint(equalToConstant: 100),

            // 名称输入框布局
            self.nameTextField.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            self.nameTextField.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            self.nameTextField.topAnchor.constraint(equalTo: avatarButton.bottomAnchor, constant: 40),
            self.nameTextField.heightAnchor.constraint(equalToConstant: 40),

            // 设定简介输入框布局
            self.introTextView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            self.introTextView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            self.introTextView.topAnchor.constraint(equalTo: self.nameTextField.bottomAnchor, constant: 20),
            self.introTextView.heightAnchor.constraint(equalToConstant: 128),

            // 设定描述输入框布局
            self.descriptionTextView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            self.descriptionTextView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            self.descriptionTextView.topAnchor.constraint(equalTo: self.introTextView.bottomAnchor, constant: 20),
            self.descriptionTextView.heightAnchor.constraint(equalToConstant: 216),

            // 创建智能体按钮布局
            self.createButton.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            self.createButton.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            self.createButton.topAnchor.constraint(equalTo: self.descriptionTextView.bottomAnchor, constant: 40),
            self.createButton.heightAnchor.constraint(equalToConstant: 68),
            
            // 在头像按钮右下角添加按钮
            self.changeAvatarButton.widthAnchor.constraint(equalToConstant: 24),
            self.changeAvatarButton.heightAnchor.constraint(equalToConstant: 24),
            self.changeAvatarButton.centerXAnchor.constraint(equalTo: self.avatarButton.centerXAnchor, constant: 50 * cos45),
            self.changeAvatarButton.centerYAnchor.constraint(equalTo: self.avatarButton.centerYAnchor, constant: 50 * cos45)
            
        ])
    }

    private func moveViewsForKeyboard(height: CGFloat) {
        let moveDistance = self.view.frame.height-height-216
        UIView.animate(withDuration: 0.3) {
            self.avatarButton.transform = CGAffineTransform(translationX: 0, y: -moveDistance)
            self.changeAvatarButton.transform = CGAffineTransform(translationX: 0, y: -moveDistance)
            self.nameTextField.transform = CGAffineTransform(translationX: 0, y: -moveDistance)
            self.introTextView.transform = CGAffineTransform(translationX: 0, y: -moveDistance)
            self.descriptionTextView.transform = CGAffineTransform(translationX: 0, y: -moveDistance)
        }
    }

    private func resetViewsPosition() {
        UIView.animate(withDuration: 0.3) {
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
        self.avatarButton.sd_setImage(with: url)
    }
    
    @objc private func createBot() {
        if self.contactsCount >= 3 {
            ToastView.show(text: "创建智能体数量已达上限")
            return
        }
        guard let name = self.nameTextField.text, !name.isEmpty else {
            ToastView.show(text: "请输入智能体名称")
            return
        }
        if self.introTextView.finalText.isEmpty {
            ToastView.show(text: "请输入智能体简介")
            return
        }
        if self.descriptionTextView.finalText.isEmpty {
            ToastView.show(text: "请输入智能体描述")
            return
        }
        let bot = AIChatBotProfile()
        bot.botName = name
        bot.botIcon = self.avatarURL
        bot.prompt = self.introTextView.finalText
        bot.botDescription = self.descriptionTextView.finalText
        bot.type = .custom
        if let avatar = bot.botIcon.components(separatedBy: "/").last?.components(separatedBy: ".").first {
            bot.voiceId = AIChatBotImplement.voiceIds[avatar] ?? "female-chengshu"
        }
        
        self.chatBotService.createChatBot(bot: bot) { [weak self] error,userId  in
            if error == nil {
                bot.botId = userId
                DispatchQueue.main.async {
                    self?.chatToBot(bot: bot)
                }
            } else {
                ToastView.show(text: "创建失败", postion: .center)
            }
        }
    }
    
    private func chatToBot(bot: AIChatBotProfileProtocol) {
        let conversation = AgoraChatClient.shared().chatManager?.getConversation(bot.botId, type: .chat, createIfNotExist: true)
        let timeMessage = AgoraChatMessage(conversationID: bot.botId, body: AgoraChatCustomMessageBody(event: "AIChat_alert_message", customExt: nil), ext: ["something":"\(UInt64(Date().timeIntervalSince1970*1000))"])
        if var ext = conversation?.ext {
            ext.merge(bot.toDictionary()) { _, new in
                new
            }
            conversation?.ext = ext
        } else {
            conversation?.ext = bot.toDictionary()
        }
        conversation?.insert(timeMessage, error: nil)
        let alertMessage = AgoraChatMessage(conversationID: bot.botId, body: AgoraChatCustomMessageBody(event: "AIChat_alert_message", customExt: nil), ext: ["something":"智能体创建成功"])
        self.createClosure?(bot)
        conversation?.insert(alertMessage, error: nil)
        let welcomeMessage = AgoraChatMessage(conversationID: bot.botId, from: bot.botId, to: VLUserCenter.user.id, body: AgoraChatTextMessageBody(text: "您好，我是\(bot.botName)，很高兴为您服务。"), ext: nil)
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


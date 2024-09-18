//
//  AIChatViewController.swift
//  AIChat
//
//  Created by 朱继超 on 2024/8/29.
//

import UIKit
import ZSwiftBaseLib
import AgoraChat
import AgoraCommon
import SDWebImage

@objc public enum AIChatType: UInt8 {
    case chat
    case group
}

open class AIChatViewController: UIViewController {
    
    private lazy var background: UIImageView = {
        UIImageView(frame: self.view.bounds).image(UIImage(named: self.chatType == .chat ? "chat_bg":"group_bg", in: .chatAIBundle, with: nil)!)
    }()
    
    public private(set) var bot: AIChatBotProfileProtocol
    
    public private(set) var chatType: AIChatType = .chat
    
    public private(set) lazy var navigation: AIChatNavigation = {
        AIChatNavigation(showLeftItem: true, textAlignment: .left,avatarURL: self.bot.botIcon,rightImages: []).backgroundColor(.clear)
    }()
    
    public private(set) lazy var chatView: AIChatMessagesList = {
        AIChatMessagesList(frame: CGRect(x: 0, y: NavigationHeight, width: self.view.frame.width, height: self.view.frame.height-NavigationHeight), chatType: self.chatType).backgroundColor(.clear)
    }()
    
    private lazy var viewModel: AIChatViewModel = {
        AIChatViewModel(conversationId: self.bot.botId, type: self.chatType)
    }()
    
    required public init(bot: AIChatBotProfileProtocol,type: AIChatType = .chat) {
        self.bot = bot
        self.chatType = type
        super.init(nibName: nil, bundle: nil)
    }
    
    required public init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.navigationController?.interactivePopGestureRecognizer?.isEnabled = false
    }
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = .white
        // Do any additional setup after loading the view.
        self.view.addSubViews([self.background,self.navigation,self.chatView])
        self.navigation.leftItem.setImage(UIImage(systemName: "chevron.backward")?.withTintColor(.white, renderingMode: .alwaysOriginal), for: .normal)
        self.navigation.titleLabel.textColor = .white
        self.navigation.detail.textColor = .white
        self.navigation.avatarURL = self.bot.botIcon
        self.navigation.updateRightItems(images: self.chatType == .chat ? []:[UIImage(named: "more1", in: .chatAIBundle, with: nil)!], original: true)
        if self.chatType == .chat {
            self.navigation.subtitle = self.bot.botDescription
            if let backgroundURL = URL(string: self.bot.botIcon.replacingOccurrences(of: "avatar", with: "bg").replacingOccurrences(of: "png", with: "jpg")) {
                self.background.sd_setImage(with: backgroundURL, placeholderImage: UIImage(named: "chat_bg", in: .chatAIBundle, with: nil))
            }
        }
        self.navigation.title = self.bot.botName
        self.navigation.clickClosure = { [weak self] type,indexPath in
            self?.view.endEditing(true)
            switch type {
            case .back:
                self?.pop()
            case .rightItems:
                if let idx = indexPath {
                    self?.processNavigationRightItemsClick(indexPath: idx)
                }
            default:
                break
            }
        }
        self.viewModel.bindDriver(driver: self.chatView, bot: self.bot)
        self.chatView.voiceChatClosure = { [weak self] in
            self?.voiceChatWithAI()
        }
    }
    
    private func processNavigationRightItemsClick(indexPath: IndexPath) {
        switch indexPath.row {
        case 0:
            self.managerGroup()
        default:
            break
        }
    }
    
    private func managerGroup() {
        let vc = GroupManagerViewController(groupId: self.bot.botId) { [weak self] name in
            self?.navigation.title = name
        } memberChangeClosure: { [weak self] _ in
            self?.viewModel.refreshGroupBots()
        }

        UIViewController.currentController?.navigationController?.pushViewController(vc, animated: true)
    }
  
    @objc func pop() {
        self.viewModel.unbindDriver()
        if self.navigationController != nil {
            UIView.animate(withDuration: 0.2) {
                self.background.alpha = 0
            }
            self.navigationController?.popViewController(animated: true)
        } else {
            self.dismiss(animated: true)
        }
    }
    
    private func voiceChatWithAI() {
        let vc = VoiceChatViewController(bot: self.bot)
        vc.modalPresentationStyle = .fullScreen
        self.present(vc, animated: true)
    }
    
    open override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        DispatchQueue.global().async {
            AgoraChatClient.shared().chatManager?.getConversationWithConvId(self.bot.botId)?.markAllMessages(asRead: nil)
        }
        self.navigationController?.interactivePopGestureRecognizer?.isEnabled = true
    }
    
    deinit {
        aichatPrint("AIChatViewController deinit", context: "AIChatViewController")
    }
}

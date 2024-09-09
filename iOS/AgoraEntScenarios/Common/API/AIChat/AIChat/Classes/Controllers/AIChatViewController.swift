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

@objc public enum AIChatType: UInt8 {
    case chat
    case group
}

open class AIChatViewController: UIViewController {
    
    private lazy var background: UIImageView = {
        UIImageView(frame: self.view.bounds).image(UIImage(named: self.chatType == .chat ? "chat_bg":"group_bg", in: .chatAIBundle, with: nil)!).contentMode(.scaleAspectFill)
    }()
    
    public private(set) var bot: AIChatBotProfileProtocol
    
    public private(set) var chatType: AIChatType = .chat
    
    public private(set) lazy var navigation: AIChatNavigation = {
        AIChatNavigation(showLeftItem: true, textAlignment: .left,avatarURL: self.bot.botIcon).backgroundColor(.clear)
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
    
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        self.view.addSubViews([self.background,self.navigation,self.chatView])
        self.navigation.leftItem.setImage(UIImage(systemName: "chevron.backward")?.withTintColor(.white, renderingMode: .alwaysOriginal), for: .normal)
        self.navigation.titleLabel.textColor = .white
        self.navigation.detail.textColor = .white
        self.navigation.subtitle = self.bot.prompt
        self.navigation.title = self.bot.botName
        self.navigation.clickClosure = { [weak self] type,_ in
            self?.view.endEditing(true)
            if type == .back {
                self?.pop()
            }
        }
        self.viewModel.bindDriver(driver: self.chatView, bot: self.bot)
        self.chatView.voiceChatClosure = { [weak self] in
            self?.voiceChatWithAI()
        }
    }
  
    @objc func pop() {
        if self.navigationController != nil {
            self.navigationController?.popViewController(animated: true)
        } else {
            self.dismiss(animated: true)
        }
    }
    
    private func voiceChatWithAI() {
        let vc = VoiceChatViewController()
        vc.modalPresentationStyle = .fullScreen
        self.present(vc, animated: true)
    }
    
    open override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        AgoraChatClient.shared().chatManager?.getConversationWithConvId(self.bot.botId)?.markAllMessages(asRead: nil)
    }
}

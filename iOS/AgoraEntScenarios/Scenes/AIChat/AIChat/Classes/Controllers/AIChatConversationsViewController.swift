//
//  AIChatConversationsViewController.swift
//  AIChat
//
//  Created by 朱继超 on 2024/8/27.
//

import UIKit
import ZSwiftBaseLib
import AgoraChat
import AgoraCommon

final class AIChatConversationsViewController: UIViewController {
    
    var addRedDotClosure: ((Int32)->Void)?
    
    lazy var background: UIImageView = {
        UIImageView(frame: self.view.bounds).image(UIImage(named: "roomList", in: .chatAIBundle, with: nil)!).contentMode(.scaleAspectFill)
    }()
    
    private var conversations = [AIChatConversationInfo]()
    
    private lazy var toolBar: PageContainerTitleBar = {
        PageContainerTitleBar(frame: CGRect(x: 0, y: NavigationHeight+4, width: self.view.frame.width, height: 44), choices: ["会话"]) { [weak self] _ in
        }.backgroundColor(.clear)
    }()
        
    private lazy var conversationList: AIChatConversationsView = {
        AIChatConversationsView(frame: CGRect(x: 0, y: self.toolBar.frame.maxY+10, width: self.view.frame.width, height: self.view.frame.height-CGFloat(ATabBarHeight)-NavigationHeight-50-10)).backgroundColor(.clear)
    }()
    
    private lazy var viewModel: AIChatConversationViewModel = {
        AIChatConversationViewModel()
    }()
    
    private lazy var createShadow: UIImageView = {
        UIImageView(frame: CGRect(x: self.view.frame.width/2.0-82, y: self.view.frame.height-CGFloat(ATabBarHeight)-46-20, width: 164, height: 46)).contentMode(.scaleAspectFill)
    }()
    
    private lazy var create: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: self.view.frame.width/2.0-82, y: self.view.frame.height-CGFloat(ATabBarHeight)-54-20, width: 164, height: 46)).cornerRadius(23).backgroundColor(.clear).addTargetFor(self, action: #selector(createAction), for: .touchUpInside)
    }()
    
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = .clear
        self.view.addSubview(self.background)
        // Do any additional setup after loading the view.
        
        self.view.addSubViews([self.toolBar,self.conversationList,self.createShadow,self.create])
        self.createShadow.image = UIImage(named: "create_bot_shadow", in: .chatAIBundle, with: nil)
        self.conversationList.chatClosure = { [weak self] bot in
            let conversation = AgoraChatClient.shared().chatManager?.getConversationWithConvId(bot.botId)
            conversation?.markAllMessages(asRead: nil)
            var isGroup = false
            if let group = conversation?.conversationId.contains("group")  {
                isGroup = group
            }
            let chatVC = AIChatViewController(bot: bot, type: isGroup ? .group:.chat)
            chatVC.hidesBottomBarWhenPushed = true
            UIViewController.currentController?.navigationController?.pushViewController(chatVC, animated: true)
        }
        self.create.contentMode = .scaleAspectFill
        self.create.setBackgroundImage(UIImage(named: "create_group", in: .chatAIBundle, with: nil), for: .normal)
        self.viewModel.bind(driver: self.conversationList)
        
    }
    
    @objc private func createAction() {
        let vc = CreateIntelligentGroupViewController()
//        vc.modalPresentationStyle = .fullScreen
//        self.present(vc, animated: true, completion: nil)
        UIViewController.currentController?.navigationController?.pushViewController(vc, animated: true)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.viewModel.loadConversations()
        if let conversations = AgoraChatClient.shared().chatManager?.getAllConversations() {
            var count = Int32(0)
            for conversation in conversations {
                count += conversation.unreadMessagesCount
            }
            self.addRedDotClosure?(count)
        }
    }
}



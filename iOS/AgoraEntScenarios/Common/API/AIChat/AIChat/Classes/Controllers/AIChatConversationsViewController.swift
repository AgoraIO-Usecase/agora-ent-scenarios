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
    
    private var conversations = [AIChatConversationInfo]()
        
    private lazy var conversationList: AIChatConversationsView = {
        AIChatConversationsView(frame: CGRect(x: 0, y: NavigationHeight, width: self.view.frame.width, height: self.view.frame.height-CGFloat(ATabBarHeight)-NavigationHeight)).backgroundColor(.clear)
    }()
    
    private lazy var viewModel: AIChatConversationViewModel = {
        AIChatConversationViewModel()
    }()
    
    private lazy var create: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: self.view.frame.width/2.0-82, y: self.view.frame.height-CGFloat(ATabBarHeight)-70, width: 164, height: 62)).backgroundColor(.clear).addTargetFor(self, action: #selector(createAction), for: .touchUpInside)
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = .white
        // Do any additional setup after loading the view.
        
        self.view.addSubViews([self.conversationList,self.create])
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
    }
}



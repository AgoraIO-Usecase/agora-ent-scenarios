//
//  AIChatConversationViewModel.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/9.
//

import UIKit
import AgoraCommon

class AIChatConversationViewModel: NSObject {
    
    
    public private(set) weak var driver: IAIChatConversationsViewDriver?
    
    public private(set) var conversationService: AIChatConversationServiceProtocol?
    
    func bind(driver: IAIChatConversationsViewDriver) {
        self.conversationService = AIChatConversationImplement()
        self.driver = driver
        self.driver?.addDelegate(self)
        self.loadConversations()
        self.conversationService?.addListener(listener: self)
    }

    func loadConversations() {
        Task {
            let result = await self.conversationService?.fetchAIConversationList()
            if result?.1 == nil,let conversations = result?.0 {
                DispatchQueue.main.async {
                    self.driver?.refresh(conversations: conversations)
                }
            } else {
                await ToastView.show(text: "加载失败")
            }
        }
    }
}

extension AIChatConversationViewModel: AIChatConversationsViewDelegate {
    
    func onDelete(conversation: AIChatConversationInfo) {
        AIChatAlertView().title(title: "确认删除会话？").titleColor(color: UIColor(0x040925)).content(textAlignment: .center).content(content: "此操作不可恢复").contentColor(color: UIColor(0x86909c)).leftButton(title: "取消").leftButton(cornerRadius: 24).leftButton(color: UIColor(0x756e98)).leftButtonBorder(color: .clear).leftButtonBackground(color: UIColor(0xeff4ff)).leftButtonTapClosure {
            
        }.rightButton(title: "删除").rightButtonBackground(color: .clear).rightButtonBorder(color: .clear).rightButtonBorder(width: 0).rightButtonBackgroundImage(image: UIImage(named: "alert_button", in: .chatAIBundle, with: nil)).rightButtonTapClosure { [weak self] in
            self?.delete(conversation: conversation)
        }.show()
        
        
    }
    
    private func delete(conversation: AIChatConversationInfo) {
        Task {
            let error = await self.conversationService?.delete(conversationId: conversation.id)
            if error == nil {
                DispatchQueue.main.async {
                    self.driver?.delete(conversation: conversation)
                }
            } else {
                await ToastView.show(text: "删除失败")
            }
        }
    }
    
}

extension AIChatConversationViewModel: AIChatConversationListener {
    
    func onAIConversationLastMessageChanged(_ conversations: [AIChatConversationInfo]) {
        self.driver?.refresh(conversations: conversations)
    }
    
    func onAIConversationListChanged(_ conversations: [AIChatConversationInfo]) {
        self.driver?.refresh(conversations: conversations)
    }
    
}
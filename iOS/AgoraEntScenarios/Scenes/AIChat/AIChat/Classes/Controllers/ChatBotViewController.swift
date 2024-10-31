//
//  ChatBotViewController.swift
//  AIChat
//
//  Created by 朱继超 on 2024/8/27.
//

import UIKit
import ZSwiftBaseLib
import SVProgressHUD
import AgoraChat
import AgoraCommon

public let ATabBarHeight = (UIApplication.shared.statusBarFrame.height > 20 ? 49+34:49)

public let AStatusBarHeight :CGFloat = UIApplication.shared.statusBarFrame.height

public let ANavigationHeight :CGFloat = UIApplication.shared.statusBarFrame.height + 44

final class ChatBotViewController: UIViewController {
    
    private var mineBots = [AIChatBotProfileProtocol]() {
        didSet {
            AIChatBotImplement.customBot = mineBots
            if self.index == 1 {
                self.empty.isHidden = !mineBots.isEmpty
            } else {
                self.empty.isHidden = true
            }
        }
    }
    
    private var commonBots = [AIChatBotProfileProtocol]() {
        didSet {
            AIChatBotImplement.commonBot = commonBots
        }
    }
    
    @MainActor private var index = 0 {
        didSet {
            if self.index == 0 {
                if !AIChatBotImplement.commonBotIds.isEmpty {
                    self.requestCommonBots()
                } else {
                    self.requestCommonBotIds()
                }
                self.empty.isHidden = true
            } else {
                self.requestMineBots()
            }
        }
    }
    
    lazy var background: UIImageView = {
        UIImageView(frame: self.view.bounds).image(UIImage(named: "roomList", in: .chatAIBundle, with: nil)!).contentMode(.scaleAspectFill)
    }()
    
    private lazy var toolBar: PageContainerTitleBar = {
        PageContainerTitleBar(frame: CGRect(x: 0, y: NavigationHeight+4, width: self.view.frame.width, height: 44), choices: [("aichat_common_bot".toSceneLocalization() as String), ("aichat_mine_bot".toSceneLocalization() as String)]) { [weak self] in
            self?.index = $0
        }.backgroundColor(.clear)
    }()
    
    private lazy var botsList: UITableView = {
        UITableView(frame: CGRect(x: 0, y: self.toolBar.frame.maxY+10, width: self.view.frame.width, height: self.view.frame.height-CGFloat(ATabBarHeight)-NavigationHeight-50-10), style: .plain).delegate(self).dataSource(self).backgroundColor(.clear).separatorStyle(.none).rowHeight(124)
    }()
    
    private lazy var empty: EmptyStateView = {
        EmptyStateView(frame: self.view.bounds, emptyImage: UIImage(named: "empty", in: .chatAIBundle, with: nil)) {
            
        }.backgroundColor(.clear)
    }()
    
    private lazy var createShadow: UIImageView = {
        UIImageView(frame: CGRect(x: self.view.frame.width/2.0-82, y: self.view.frame.height-CGFloat(ATabBarHeight)-46-20, width: 164, height: 46)).contentMode(.scaleAspectFill)
    }()
    
    private lazy var create: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: self.view.frame.width/2.0-82, y: self.view.frame.height-CGFloat(ATabBarHeight)-54-20, width: 164, height: 46)).cornerRadius(23).backgroundColor(.clear).addTargetFor(self, action: #selector(createAction), for: .touchUpInside)
    }()
    
    private let service = AIChatBotImplement()

    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationController?.isNavigationBarHidden = true
        self.view.backgroundColor = .clear
        // Do any additional setup after loading the view.
        self.view.addSubview(self.background)
        self.view.addSubViews([self.empty,self.toolBar,self.botsList,self.createShadow,self.create])
        self.createShadow.image = UIImage(named: "create_bot_shadow", in: .chatAIBundle, with: nil)
        self.create.setBackgroundImage(UIImage(named: "create_bot", in: .chatAIBundle, with: nil), for: .normal)
        self.create.contentMode = .scaleAspectFill
        self.index = 0
        self.empty.isHidden = true
        self.empty.retryButton.setTitle("aichat_empty_alert".toSceneLocalization() as String, for: .normal)
    }
    

    private func chatToBot(bot: AIChatBotProfileProtocol) {
        if AIChatBotImplement.commonBot.contains(where: { $0.botId == bot.botId }) {
            let conversation = AgoraChatClient.shared().chatManager?.getConversation(bot.botId, type: .chat, createIfNotExist: true)
            if var ext = conversation?.ext {
                ext.merge(bot.toDictionary()) { _, new in
                    new
                }
                conversation?.ext = ext
            } else {
                conversation?.ext = bot.toDictionary()
            }
            
            if conversation?.latestMessage == nil {
                var welcomeText = String(format: "aichat_bot_welcome_message".toSceneLocalization() as String, bot.botName)
                if AIChatBotImplement.commonBotIds.contains(bot.botId) {
                    if let id = bot.botId.components(separatedBy: "common-").last {
                        welcomeText = AIChatBotImplement.commonBotWelcomeMessage[id] ?? welcomeText
                    }
                }
                let welcomeMessage = AgoraChatMessage(conversationID: bot.botId, from: bot.botId, to: AppContext.shared.getAIChatUid(), body: AgoraChatTextMessageBody(text: welcomeText), ext: nil)
                welcomeMessage.direction = .receive
                conversation?.insert(welcomeMessage, error: nil)
            }
        }
        let chatVC = AIChatViewController(bot: bot)
        chatVC.hidesBottomBarWhenPushed = true
        UIViewController.currentController?.navigationController?.pushViewController(chatVC, animated: true)
    }

    @objc private func createAction() {
        let contacts = AgoraChatClient.shared().contactManager?.getContacts() ?? []
        let count = contacts.filter { !$0.contains("group") }.count
        if count >= 3 {
            ToastView.show(text: "aichat_bot_create_limited".toSceneLocalization() as String)
            return
        }
        let vc = CreateIntelligenceViewController { bot in
            self.addBot(bot: bot)
        }
        vc.modalPresentationStyle = .fullScreen
        UIViewController.currentController?.navigationController?.present(vc, animated: true)
    }
    
    func addBot(bot: AIChatBotProfileProtocol) {
        if self.mineBots.count <= 0 {
            self.requestMineBots()
        } else {
            self.mineBots.insert(bot, at: 0)
            self.botsList.reloadData()
            self.empty.isHidden = true
        }
    }
    
    private func requestCommonBots() {
        self.botsList.isHidden = false
        if self.commonBots.count > 0 {
            self.botsList.reloadData()
            return
        }
        SVProgressHUD.show(withStatus: "aichat_loading".toSceneLocalization() as String)
        Task {
            let result = await self.service.getCommonBots(botIds: AIChatBotImplement.commonBotIds)
            DispatchQueue.main.async {
                SVProgressHUD.dismiss()
                if let error = result.1 {
                    SVProgressHUD.showError(withStatus: "获取失败：\(error.errorDescription)")
                } else {
                    self.commonBots = result.0 ?? []
                    self.botsList.reloadData()
                }
            }
        }
    }
    
    private func requestCommonBotIds() {
        SVProgressHUD.show(withStatus: "aichat_loading".toSceneLocalization() as String)
        self.service.commonBotIds { [weak self] ids, error in
            SVProgressHUD.dismiss()
            if error == nil {
                self?.requestCommonBots()
            } else {
                ToastView.show(text: "aichat_fetch_common_bot_failed".toSceneLocalization() as String)
            }
        }
    }
    
    private func requestMineBots() {
        if self.mineBots.count > 0 {
            self.botsList.reloadData()
            return
        }
        SVProgressHUD.show(withStatus: "aichat_loading".toSceneLocalization() as String)
        self.mineBots.removeAll()
        Task {
            let result = await self.service.getCustomBotProfile()
            DispatchQueue.main.async {
                SVProgressHUD.dismiss()
                if let error = result.1 {
                    SVProgressHUD.showError(withStatus: "获取失败：\(error.errorDescription)")
                } else {
                    self.mineBots = result.0 ?? []
                    self.botsList.isHidden = self.mineBots.count == 0
                    self.botsList.reloadData()
                }
            }
        }
    }
}

extension ChatBotViewController: UITableViewDelegate,UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        self.index == 0 ? self.commonBots.count : self.mineBots.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: "MineBotCell") as? ChatBotCell
        if cell == nil {
            cell = ChatBotCell(style: .default, reuseIdentifier: "MineBotCell")
        }
        cell?.selectionStyle = .none
        if let bot = (self.index == 0 ? self.commonBots : self.mineBots)[safe: indexPath.row] {
            cell?.refresh(bot: bot)
        }
        return cell ?? ChatBotCell()
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        if let bot = (self.index == 0 ? self.commonBots : self.mineBots)[safe: indexPath.row] {
            self.chatToBot(bot: bot)
        }
    }
    
    public func tableView(_ tableView: UITableView, trailingSwipeActionsConfigurationForRowAt indexPath: IndexPath) -> UISwipeActionsConfiguration? {
        if self.index == 0 {
            return nil
        }
        let action = UIContextualAction(style: .normal, title: nil, handler: { [weak self] (action, view, completion) in
            self?.delete(indexPath: indexPath)
            completion(true)
        })
        action.image = UIImage(named: "delete", in: .chatAIBundle, with: nil)
        action.backgroundColor = UIColor(white: 1, alpha: 0)
        let configuration = UISwipeActionsConfiguration(actions: [action])
        configuration.performsFirstActionWithFullSwipe = false
        return configuration
    }
    
    private func delete(indexPath: IndexPath) {
        AIChatAlertView().title(title: "aichat_delete_bot_alert".toSceneLocalization() as String).titleColor(color: UIColor(0x040925)).content(textAlignment: .center).content(content: "aichat_delete_bot_content".toSceneLocalization() as String).contentColor(color: UIColor(0x86909c)).leftButton(title: "aichat_cancel".toSceneLocalization() as String).leftButton(cornerRadius: 24).leftButton(color: UIColor(0x756e98)).leftButtonBorder(color: .clear).leftButtonBackground(color: UIColor(0xeff4ff)).leftButtonTapClosure {
            
        }.rightButton(title: "aichat_delete".toSceneLocalization() as String).rightButtonBackground(color: .clear).rightButtonBorder(color: .clear).rightButtonBorder(width: 0).rightButtonBackgroundImage(image: UIImage(named: "alert_button", in: .chatAIBundle, with: nil)).rightButtonTapClosure { [weak self] in
            self?.deleteBot(indexPath: indexPath)
        }.show()
        
    }
    
    private func deleteBot(indexPath: IndexPath) {
        SVProgressHUD.show()
        aichatPrint("deleteBot start:\(Date().timeIntervalSince1970*1000)")
        guard let bot = self.mineBots[safe: indexPath.row] else { return }
        if let conversation =
            AgoraChatClient.shared().chatManager?.getConversationWithConvId(bot.botId) {
            AgoraChatClient.shared().chatManager?.deleteServerConversation(conversation.conversationId, conversationType: .chat, isDeleteServerMessages: true,completion: { conversationId, error in
                if error == nil {
                    SVProgressHUD.dismiss()
                    SVProgressHUD.show()
                    AgoraChatClient.shared().chatManager?.delete([conversation], isDeleteMessages: true, completion: { [weak self] error in
                        SVProgressHUD.dismiss()
                        aichatPrint("deleteBot conversation end:\(Date().timeIntervalSince1970*1000)")
                        guard let `self` = self else { return }
                        if error != nil{
                            ToastView.show(text: "删除本地会话失败!")
                            aichatPrint("删除本地端会话失败:\(error?.errorDescription ?? "")")
                        } else {
                            aichatPrint("deleteBot from server start:\(Date().timeIntervalSince1970*1000)")
                            SVProgressHUD.show()
                            self.service.deleteChatBot(botId: bot.botId) { [weak self] error in
                                SVProgressHUD.dismiss()
                                aichatPrint("deleteBot from server end:\(Date().timeIntervalSince1970*1000)")
                                if error == nil {
                                    ToastView.show(text: "删除智能体成功")
                                    self?.mineBots.remove(at: indexPath.row)
                                    self?.botsList.reloadData()
                                } else {
                                    aichatPrint("删除智能体失败:\(error?.localizedDescription ?? "")")
                                }
                            }
                            AgoraChatClient.shared().contactManager?.deleteContact(bot.botId, isDeleteConversation: true)
                        }
                    })
                } else {
                    ToastView.show(text: "删除服务端会话失败!")
                }
            })
        } else {
            self.service.deleteChatBot(botId: bot.botId) { [weak self] error in
                if error == nil {
                    ToastView.show(text: "删除成功")
                    AgoraChatClient.shared().contactManager?.deleteContact(bot.botId, isDeleteConversation: true)
                    DispatchQueue.main.async {
                        self?.mineBots.remove(at: indexPath.row)
                        self?.botsList.reloadData()
                    }
                } else {
                    aichatPrint("删除智能体失败:\(error?.localizedDescription ?? "")")
                }
            }
            
        }
        
    }
}

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
                self.requestCommonBots()
            } else {
                self.requestMineBots()
            }
        }
    }
    
    private lazy var toolBar: PageContainerTitleBar = {
        PageContainerTitleBar(frame: CGRect(x: 0, y: NavigationHeight+4, width: self.view.frame.width, height: 44), choices: ["公共智能体", "我创建的"]) { [weak self] in
            self?.index = $0
        }.backgroundColor(.clear)
    }()
    
    private lazy var botsList: UITableView = {
        UITableView(frame: CGRect(x: 20, y: self.toolBar.frame.maxY, width: self.view.frame.width-40, height: self.view.frame.height-CGFloat(ATabBarHeight)-NavigationHeight-50), style: .plain).delegate(self).dataSource(self).backgroundColor(.clear).separatorStyle(.none).rowHeight(110)
    }()
    
    private lazy var create: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: self.view.frame.width/2.0-82, y: self.view.frame.height-CGFloat(ATabBarHeight)-70, width: 164, height: 62)).backgroundColor(.clear).addTargetFor(self, action: #selector(createAction), for: .touchUpInside)
    }()

    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationController?.isNavigationBarHidden = true
        self.view.backgroundColor = .white
        // Do any additional setup after loading the view.
        self.view.addSubViews([self.toolBar,self.botsList,self.create])
        self.create.setBackgroundImage(UIImage(named: "create_bot", in: .chatAIBundle, with: nil), for: .normal)
        self.create.contentMode = .scaleAspectFill
        self.index = 0
    }
    

    private func chatToBot(bot: AIChatBotProfileProtocol) {
        let chatVC = AIChatViewController(bot: bot)
        chatVC.hidesBottomBarWhenPushed = true
        UIViewController.currentController?.navigationController?.pushViewController(chatVC, animated: true)
    }

    @objc private func createAction() {
        let vc = CreateIntelligenceViewController { bot in
            self.addBot(bot: bot)
        }
        vc.modalPresentationStyle = .fullScreen
        UIViewController.currentController?.navigationController?.present(vc, animated: true)
    }
    
    func addBot(bot: AIChatBotProfileProtocol) {
        self.mineBots.insert(bot, at: 0)
        self.botsList.reloadData()
    }
    
    private func requestCommonBots() {
        if self.commonBots.count > 0 {
            self.botsList.reloadData()
            return
        }
        SVProgressHUD.show(withStatus: "加载中")
        Task {
            let result = await AIChatBotImplement().getCommonBots(botIds: commonBotIds)
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
    
    private func requestMineBots() {
        if self.mineBots.count > 0 {
            self.botsList.reloadData()
            return
        }
        SVProgressHUD.show(withStatus: "加载中")
        self.mineBots.removeAll()
        Task {
            let result = await AIChatBotImplement().getCustomBotProfile()
            DispatchQueue.main.async {
                SVProgressHUD.dismiss()
                if let error = result.1 {
                    SVProgressHUD.showError(withStatus: "获取失败：\(error.errorDescription)")
                } else {
                    self.mineBots = result.0 ?? []
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
        AIChatAlertView().title(title: "确认删除智能体“智能助手”？").titleColor(color: UIColor(0x040925)).content(textAlignment: .center).content(content: "此操作不可恢复").contentColor(color: UIColor(0x86909c)).leftButton(title: "取消").leftButton(cornerRadius: 24).leftButton(color: UIColor(0x756e98)).leftButtonBorder(color: .clear).leftButtonBackground(color: UIColor(0xeff4ff)).leftButtonTapClosure {
            
        }.rightButton(title: "删除").rightButtonBackground(color: .clear).rightButtonBorder(color: .clear).rightButtonBorder(width: 0).rightButtonBackgroundImage(image: UIImage(named: "alert_button", in: .chatAIBundle, with: nil)).rightButtonTapClosure { [weak self] in
            self?.deleteBot(indexPath: indexPath)
        }.show()
        
    }
    
    private func deleteBot(indexPath: IndexPath) {
        guard let bot = self.mineBots[safe: indexPath.row] else { return }
        if let conversation =
            AgoraChatClient.shared().chatManager?.getConversationWithConvId(bot.botId) {
            AgoraChatClient.shared().chatManager?.delete([conversation], isDeleteMessages: true, completion: { [weak self] error in
                guard let `self` = self else { return }
                if error != nil{
                    ToastView.show(text: "删除服务端会话失败!")
                } else {
                    AgoraChatClient.shared().contactManager?.deleteContact(bot.botId, isDeleteConversation: true)
                    ToastView.show(text: "删除成功")
                    DispatchQueue.main.async {
                        self.mineBots.remove(at: indexPath.row)
                        self.botsList.reloadData()
                    }
                }
            })
        } else {
            AgoraChatClient.shared().contactManager?.deleteContact(bot.botId, isDeleteConversation: true)
            self.mineBots.remove(at: indexPath.row)
            self.botsList.reloadData()
        }
        
    }
}

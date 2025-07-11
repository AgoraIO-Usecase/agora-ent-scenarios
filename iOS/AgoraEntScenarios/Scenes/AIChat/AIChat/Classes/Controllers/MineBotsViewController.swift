//
//  MineBotsViewController.swift
//  AIChat
//
//  Created by 朱继超 on 2024/8/27.
//

import UIKit
import SVProgressHUD
import ZSwiftBaseLib
import AgoraChat
import AgoraCommon

final class MineBotsViewController: UIViewController {
        
    private var chatClosure: (AIChatBotProfileProtocol)->()
        
    private var mineBots = [AIChatBotProfileProtocol]() {
        didSet {
            DispatchQueue.main.async {
                if self.mineBots.isEmpty {
                    self.empty.isHidden = false
                    self.mineBotsList.isHidden = true
                } else {
                    self.empty.isHidden = true
                    self.mineBotsList.isHidden = false
                }
            }
        }
    }
    
    private lazy var mineBotsList: UITableView = {
        UITableView(frame: CGRect(x: 0, y: 0, width: self.view.frame.width, height: self.view.frame.height-CGFloat(ATabBarHeight)-NavigationHeight), style: .plain).delegate(self).dataSource(self).backgroundColor(.clear).separatorStyle(.none).rowHeight(110)
    }()
    
    private lazy var empty: EmptyStateView = {
        EmptyStateView(frame: self.view.bounds, emptyImage: UIImage(named: "empty", in: .chatAIBundle, with: nil)) {
            
        }.backgroundColor(.clear)
    }()
    
    private var selectedIds = [String]()
    
    required public init(chatClosure: @escaping (AIChatBotProfileProtocol)->()) {
        self.chatClosure = chatClosure
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.mineBots.append(contentsOf: AIChatBotImplement.customBot)
        
        for selectId in self.selectedIds {
            self.mineBots.removeAll { $0.botId == selectId }
        }
        self.view.backgroundColor = .clear
        self.view.addSubview(self.empty)
        self.view.addSubview(self.mineBotsList)
        // Do any additional setup after loading the view.
        self.empty.retryButton.setTitle("aichat_empty_alert".toSceneLocalization() as String, for: .normal)
    }
    
    func refresh(with selectIds: [String]) {
        self.selectedIds = selectIds
    }
    
    deinit {
        for var bot in AIChatBotImplement.customBot {
            bot.selected = false
        }
    }
}

extension MineBotsViewController: UITableViewDelegate,UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        self.mineBots.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: "MineBotCell") as? GroupBotCell
        if cell == nil {
            cell = GroupBotCell(style: .default, reuseIdentifier: "MineBotCell")
        }
        cell?.selectionStyle = .none
        if let bot = self.mineBots[safe: indexPath.row] {
            cell?.refresh(bot: bot)
        }
        return cell ?? ChatBotCell()
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        if var bot = self.mineBots[safe: indexPath.row] {
            bot.selected = !bot.selected
            tableView.reloadData()
            self.chatClosure(bot)
        }
    }
    
    
}



//
//  AllBotsViewController.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/10.
//

import UIKit
import ZSwiftBaseLib

class AllBotsViewController: UIViewController {

    public private(set) var allBots = AIChatBotImplement.commonBot+AIChatBotImplement.customBot
    
    private var chatClosure: (AIChatBotProfileProtocol)->()
    
    private lazy var commonBotsList: UITableView = {
        UITableView(frame: CGRect(x: 20, y: 0, width: self.view.frame.width-40, height: self.view.frame.height-CGFloat(ATabBarHeight)-NavigationHeight-50), style: .plain).delegate(self).dataSource(self).backgroundColor(.clear).separatorStyle(.none).rowHeight(110)
    }()
    
    required public init(chatClosure: @escaping (AIChatBotProfileProtocol)->()) {
        self.chatClosure = chatClosure
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = .clear
        self.view.addSubview(self.commonBotsList)
        // Do any additional setup after loading the view.
    }

    func refreshBot() {
        self.allBots = AIChatBotImplement.commonBot+AIChatBotImplement.customBot
        self.commonBotsList.reloadData()
    }
    
    deinit {
        for var bot in AIChatBotImplement.customBot {
            bot.selected = false
        }
        for var bot in AIChatBotImplement.commonBot {
            bot.selected = false
        }
    }
}

extension AllBotsViewController: UITableViewDelegate,UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        self.allBots.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: "AllBotCell") as? GroupBotCell
        if cell == nil {
            cell = GroupBotCell(style: .default, reuseIdentifier: "AllBotCell")
        }
        cell?.selectionStyle = .none
        if let bot = self.allBots[safe: indexPath.row] {
            cell?.refresh(bot: bot)
        }
        return cell ?? ChatBotCell()
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        if var bot = self.allBots[safe: indexPath.row] {
            bot.selected = !bot.selected
            tableView.reloadData()
            self.chatClosure(bot)
        }
    }
}

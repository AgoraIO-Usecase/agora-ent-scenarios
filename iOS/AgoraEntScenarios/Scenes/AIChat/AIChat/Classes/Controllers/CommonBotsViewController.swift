//
//  CommonBotsViewController.swift
//  AIChat
//
//  Created by 朱继超 on 2024/8/27.
//

import UIKit
import ZSwiftBaseLib
import SVProgressHUD

final class CommonBotsViewController: UIViewController {
    
    private var chatClosure: (AIChatBotProfileProtocol)->()
    
    private lazy var commonBotsList: UITableView = {
        UITableView(frame: CGRect(x: 0, y: 0, width: self.view.frame.width, height: self.view.frame.height-CGFloat(ATabBarHeight)-NavigationHeight), style: .plain).delegate(self).dataSource(self).backgroundColor(.clear).separatorStyle(.none).rowHeight(110)
    }()
    
    private var commonBots = [AIChatBotProfileProtocol]()
    
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
        self.commonBots.append(contentsOf: AIChatBotImplement.commonBot)
        for selectId in self.selectedIds {
            self.commonBots.removeAll { $0.botId == selectId }
        }
        self.view.backgroundColor = .clear
        self.view.addSubview(self.commonBotsList)
        // Do any additional setup after loading the view.
    }
    
    deinit {
        for var bot in AIChatBotImplement.commonBot {
            bot.selected = false
        }
    }
    
    func refresh(with selectIds: [String]) {
        self.selectedIds = selectIds
    }
}

extension CommonBotsViewController: UITableViewDelegate,UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        self.commonBots.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: "CommonBotCell") as? GroupBotCell
        if cell == nil {
            cell = GroupBotCell(style: .default, reuseIdentifier: "CommonBotsCell")
        }
        cell?.selectionStyle = .none
        if let bot = self.commonBots[safe: indexPath.row] {
            cell?.refresh(bot: bot)
        }
        return cell ?? ChatBotCell()
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        if var bot = self.commonBots[safe: indexPath.row] {
            bot.selected = !bot.selected
            tableView.reloadData()
            self.chatClosure(bot)
        }
    }
}

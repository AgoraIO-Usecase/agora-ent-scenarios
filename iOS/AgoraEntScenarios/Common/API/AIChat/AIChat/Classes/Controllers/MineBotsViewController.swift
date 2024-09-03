//
//  MineBotsViewController.swift
//  AIChat
//
//  Created by 朱继超 on 2024/8/27.
//

import UIKit
import SVProgressHUD
import ZSwiftBaseLib

final class MineBotsViewController: UIViewController {
    
    private var mineBots = [AIChatBotProfileProtocol]()
    
    private var chatClosure: (AIChatBotProfileProtocol)->()
    
    private lazy var mineBotsList: UITableView = {
        UITableView(frame: CGRect(x: 20, y: 0, width: self.view.frame.width-40, height: self.view.frame.height), style: .plain).delegate(self).dataSource(self).backgroundColor(.clear).separatorStyle(.none)
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
        self.view.addSubview(self.mineBotsList)
        // Do any additional setup after loading the view.
//        self.requestMineBots()
    }
    
    private func requestMineBots() {
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
                    self.mineBotsList.reloadData()
                }
            }
        }
    }

}

extension MineBotsViewController: UITableViewDelegate,UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        self.mineBots.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: "MineBotCell") as? ChatBotCell
        if cell == nil {
            cell = ChatBotCell(style: .default, reuseIdentifier: "MineBotCell")
        }
        cell?.selectionStyle = .none
        if let bot = self.mineBots[safe: indexPath.row] {
            cell?.refresh(bot: bot)
        }
        return cell ?? ChatBotCell()
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        if let bot = self.mineBots[safe: indexPath.row] {
            self.chatClosure(bot)
        }
    }
    
    public func tableView(_ tableView: UITableView, trailingSwipeActionsConfigurationForRowAt indexPath: IndexPath) -> UISwipeActionsConfiguration? {
        let action = UIContextualAction(style: .normal, title: nil, handler: { [weak self] (action, view, completion) in
            self?.delete(indexPath: indexPath)
            completion(true)
        })
        action.image = UIImage(named: "delete", in: .chatAIBundle, with: nil)
        action.backgroundColor = .clear
        let configuration = UISwipeActionsConfiguration(actions: [action])
        configuration.performsFirstActionWithFullSwipe = false
        return configuration
    }
    
    private func delete(indexPath: IndexPath) {
        self.mineBotsList.beginUpdates()
        self.mineBotsList.deleteRows(at: [indexPath], with: .automatic)
        self.mineBotsList.endUpdates()
        self.mineBots.remove(at: indexPath.row)
    }

}

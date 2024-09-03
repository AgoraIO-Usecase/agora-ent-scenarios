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
    
    private var commonBots = [AIChatBotProfileProtocol]()
    
    private var chatClosure: (AIChatBotProfileProtocol)->()
    
    private lazy var commonBotsList: UITableView = {
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
        self.view.addSubview(self.commonBotsList)
        // Do any additional setup after loading the view.
//        self.requestCommonBots()
    }
    
    private func requestCommonBots() {
        SVProgressHUD.show(withStatus: "加载中")
        self.commonBots.removeAll()
        Task {
            let result = await AIChatBotImplement().getCommonBots(botIds: ["","","",""])
            DispatchQueue.main.async {
                SVProgressHUD.dismiss()
                if let error = result.1 {
                    SVProgressHUD.showError(withStatus: "获取失败：\(error.errorDescription)")
                } else {
                    self.commonBots = result.0 ?? []
                    self.commonBotsList.reloadData()
                }
            }
        }
    }

}

extension CommonBotsViewController: UITableViewDelegate,UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        self.commonBots.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: "CommonBotCell") as? ChatBotCell
        if cell == nil {
            cell = ChatBotCell(style: .default, reuseIdentifier: "CommonBotsCell")
        }
        cell?.selectionStyle = .none
        if let bot = self.commonBots[safe: indexPath.row] {
            cell?.refresh(bot: bot)
        }
        return cell ?? ChatBotCell()
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        if let bot = self.commonBots[safe: indexPath.row] {
            self.chatClosure(bot)
        }
    }
}

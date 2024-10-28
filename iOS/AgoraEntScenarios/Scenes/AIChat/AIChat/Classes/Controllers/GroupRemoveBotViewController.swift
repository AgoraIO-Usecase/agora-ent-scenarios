//
//  GroupRemoveBotViewController.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/10.
//

import UIKit
import ZSwiftBaseLib

class GroupRemoveBotViewController: UIViewController {
    
    lazy var navigation: AIChatNavigation = {
        AIChatNavigation(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 44),textAlignment: .left,rightTitle: "删除").backgroundColor(.clear)
    }()
    
    public private(set) var allBots = [AIChatBotProfileProtocol]()
    
    
    private lazy var botsList: UITableView = {
        UITableView(frame: CGRect(x: 0, y: self.navigation.frame.maxY, width: self.view.frame.width, height: self.view.frame.height-self.navigation.frame.maxY), style: .plain).delegate(self).dataSource(self).backgroundColor(.clear).separatorStyle(.none).rowHeight(110)
    }()
    
    private var selectClosure: (([AIChatBotProfileProtocol]) -> Void)?
    
    required init(profileIds: [String], selectClosure: (([AIChatBotProfileProtocol]) -> Void)? = nil) {
        self.selectClosure = selectClosure
        for id in profileIds {
            if let bot = AIChatBotImplement.commonBot.first(where: { $0.botId == id }) {
                self.allBots.append(bot)
            }
            if let bot = AIChatBotImplement.customBot.first(where: { $0.botId == id }) {
                self.allBots.append(bot)
            }
        }
        self.allBots.forEach { $0.selected = false }
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = .white
        self.view.cornerRadius(16, [.topLeft,.topRight], .clear, 0)
        let gradient = UIImageView(frame: CGRect(x: 0, y: 0, width: self.view.frame.width, height: NavigationHeight)).contentMode(.scaleAspectFill)
        gradient.image = UIImage(named: "edit_bg", in: .chatAIBundle, with: nil)
        self.view.addSubview(gradient)
        self.navigation.title = "删除群聊伙伴"
        self.navigation.leftItem.isHidden = true
        self.navigation.separateLine.isHidden = true
        self.navigation.rightItem.isEnabled = false
        // Do any additional setup after loading the view.
        self.view.addSubViews([self.navigation,self.botsList])
        self.navigation.clickClosure = { [weak self] type,_ in
            switch type {
            case .back:
                self?.dismiss(animated: true, completion: nil)
            case .rightTitle:
                self?.selectClosure?(self?.allBots.filter({ $0.selected }) ?? [])
                self?.dismiss(animated: true, completion: nil)
            default:
                break
            }
        }
    }
    
    func refreshCount() {
        var count = 0
        let bots = self.allBots
        for bot in bots {
            if bot.selected {
                count += 1
            }
        }
        
        self.navigation.rightItem.isEnabled = count > 0
        var title = "删除"
        if count > 0 {
            title += "(\(count))"
            self.navigation.rightItem.isEnabled = true
        }
        self.navigation.rightItem.setTitle(title, for: .normal)
    }
}

extension GroupRemoveBotViewController: UITableViewDelegate,UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        self.allBots.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: "RemoveBotCell") as? GroupBotCell
        if cell == nil {
            cell = GroupBotCell(style: .default, reuseIdentifier: "RemoveBotCell")
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
            self.botsList.reloadData()
            self.refreshCount()
        }
    }
}

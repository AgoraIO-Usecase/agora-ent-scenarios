//
//  GroupAddBotViewController.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/10.
//

import UIKit
import ZSwiftBaseLib

class GroupAddBotViewController: UIViewController {
    
    lazy var navigation: AIChatNavigation = {
        AIChatNavigation(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 44),textAlignment: .left,rightTitle: "添加")
    }()
    
    lazy var allBot: AllBotsViewController = {
        AllBotsViewController { [weak self] item in
            self?.refreshCount()
        }
    }()
    
    lazy var commonBot: CommonBotsViewController = {
        CommonBotsViewController { [weak self] item in
            self?.refreshCount()
            self?.allBot.refreshBot()
        }
    }()
    
    lazy var customBot: MineBotsViewController = {
        MineBotsViewController { [weak self] item in
            self?.refreshCount()
            self?.allBot.refreshBot()
        }
    }()
    
    lazy var container: PageContainer = {
        PageContainer(frame: CGRect(x: 0, y: self.navigation.frame.maxY, width: self.view.frame.width, height: self.view.frame.height-self.navigation.frame.maxY),viewControllers: [self.allBot,self.commonBot,self.customBot],indicators: ["全部","公共智能体","我创建的"])
    }()
    
    private var selectClosure: (([AIChatBotProfileProtocol]) -> Void)
    
    public required init(userIds: [String],closure: @escaping (([AIChatBotProfileProtocol]) -> Void)) {
        self.selectClosure = closure
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = .white
        self.view.cornerRadius(16, [.topLeft,.topRight], .clear, 0)
        if let image = UIImage(named: "edit_bg", in: .chatAIBundle, with: nil) {
            self.navigation.backgroundColor = UIColor(patternImage: image)
        }
        self.navigation.title = "添加群聊伙伴"
        self.navigation.leftItem.isHidden = true
        self.navigation.separateLine.isHidden = true
        self.navigation.rightItem.isEnabled = false
        // Do any additional setup after loading the view.
        self.view.addSubViews([self.navigation,self.container])
        self.navigation.clickClosure = { [weak self] type,_ in
            switch type {
            case .back:
                self?.dismiss(animated: true, completion: nil)
            case .rightTitle:
                self?.selectClosure(self?.allBot.allBots.filter({ $0.selected }) ?? [])
                self?.dismiss(animated: true, completion: nil)
            default:
                break
            }
        }
    }
    
    func refreshCount() {
        var count = 0
        let bots = self.allBot.allBots
        for bot in bots {
            if bot.selected {
                count += 1
            }
        }
        
        self.navigation.rightItem.isEnabled = count > 0
        var title = "添加"
        if count > 0 {
            title += "(\(count))"
            self.navigation.rightItem.isEnabled = true
        }
        self.navigation.rightItem.setTitle(title, for: .normal)
    }

}

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
        AIChatNavigation(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 44),textAlignment: .left,rightTitle: "aichat_add".toSceneLocalization() as String).backgroundColor(.clear)
    }()
    
    lazy var allBot: AllBotsViewController = {
        AllBotsViewController { [weak self] item in
//            self?.allBot.refreshBot()
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
        PageContainer(frame: CGRect(x: 0, y: self.navigation.frame.maxY, width: self.view.frame.width, height: self.view.frame.height-self.navigation.frame.maxY),viewControllers: [self.allBot,self.commonBot,self.customBot],indicators: [("aichat_all".toSceneLocalization() as String),("aichat_common_bot".toSceneLocalization() as String), ("aichat_mine_bot".toSceneLocalization() as String)])
    }()
    
    private var selectClosure: (([AIChatBotProfileProtocol]) -> Void)
    
    private var selectIds = [String]()
    
    public required init(userIds: [String],closure: @escaping (([AIChatBotProfileProtocol]) -> Void)) {
        self.selectClosure = closure
        self.selectIds = userIds
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
        self.navigation.title = "aichat_add_members".toSceneLocalization() as String
        self.navigation.leftItem.isHidden = true
        self.navigation.separateLine.isHidden = true
        self.navigation.rightItem.isEnabled = false
        // Do any additional setup after loading the view.
        self.view.addSubViews([self.navigation,self.container])
        self.allBot.refresh(with: self.selectIds)
        self.commonBot.refresh(with: self.selectIds)
        self.customBot.refresh(with: self.selectIds)
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
        var title = "aichat_add".toSceneLocalization() as String
        if count > 0 {
            title += "(\(count))"
            self.navigation.rightItem.isEnabled = true
        }
        self.navigation.rightItem.setTitle(title, for: .normal)
    }

}

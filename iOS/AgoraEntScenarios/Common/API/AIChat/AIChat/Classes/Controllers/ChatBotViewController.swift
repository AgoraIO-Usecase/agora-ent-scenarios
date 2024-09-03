//
//  ChatBotViewController.swift
//  AIChat
//
//  Created by 朱继超 on 2024/8/27.
//

import UIKit
import ZSwiftBaseLib

public let ATabBarHeight = (UIApplication.shared.statusBarFrame.height > 20 ? 49+34:49)

public let AStatusBarHeight :CGFloat = UIApplication.shared.statusBarFrame.height

public let ANavigationHeight :CGFloat = UIApplication.shared.statusBarFrame.height + 44

final class ChatBotViewController: UIViewController {
    
    private lazy var commonBotsViewController: CommonBotsViewController = {
        CommonBotsViewController { [weak self] bot in
            self?.chatToBot(bot: bot)
        }
    }()
    
    private lazy var mineBotsViewController: MineBotsViewController = {
        MineBotsViewController { [weak self] bot in
            self?.chatToBot(bot: bot)
        }
    }()
    
    lazy var container: PageContainer = {
        PageContainer(frame: CGRect(x: 0, y: NavigationHeight, width: self.view.frame.width, height: self.view.frame.height-CGFloat(ATabBarHeight)-NavigationHeight), viewControllers: [self.commonBotsViewController, self.mineBotsViewController], indicators: ["公共智能体", "我创建的"])
    }()
    
    private lazy var create: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: self.view.frame.width/2.0-82, y: self.view.frame.height-CGFloat(ATabBarHeight)-70, width: 164, height: 62)).backgroundColor(.clear).addTargetFor(self, action: #selector(createAction), for: .touchUpInside)
    }()

    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = .clear
        // Do any additional setup after loading the view.
        self.view.addSubview(self.container)
        self.view.addSubview(self.create)
        self.view.addSubview(self.create)
        self.create.setBackgroundImage(UIImage(named: "create_bot", in: .chatAIBundle, with: nil), for: .normal)
        self.create.contentMode = .scaleAspectFill
    }
    

    private func chatToBot(bot: AIChatBotProfileProtocol) {
        let chatVC = AIChatViewController(bot: bot)
        chatVC.hidesBottomBarWhenPushed = true
        self.navigationController?.pushViewController(chatVC, animated: true)
    }

    @objc private func createAction() {
        let vc = CreateIntelligenceViewController()
        vc.modalPresentationStyle = .fullScreen
        self.navigationController?.present(vc, animated: true)
    }
}

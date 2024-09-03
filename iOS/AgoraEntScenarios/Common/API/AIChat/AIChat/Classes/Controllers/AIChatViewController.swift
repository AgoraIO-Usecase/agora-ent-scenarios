//
//  AIChatViewController.swift
//  AIChat
//
//  Created by 朱继超 on 2024/8/29.
//

import UIKit

open class AIChatViewController: UIViewController {
    
    public private(set) var bot: AIChatBotProfileProtocol
    
    required public init(bot: AIChatBotProfileProtocol) {
        self.bot = bot
        super.init(nibName: nil, bundle: nil)
    }
    
    required public init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }

}


import UIKit
import ZSwiftBaseLib
import AgoraChat
import AgoraCommon
import SVProgressHUD

public final class AIChatMainViewController: UITabBarController {
        
    lazy var background: UIImageView = {
        UIImageView(frame: self.view.bounds).image(UIImage(named: "roomList", in: .chatAIBundle, with: nil)!).contentMode(.scaleAspectFill)
    }()
        
    lazy var navigation: AIChatNavigation = {
        AIChatNavigation(frame: CGRect(x: 0, y: 0, width: self.view.frame.width, height: NavigationHeight), textAlignment: .center, rightTitle: nil).backgroundColor(.clear)
    }()
    
    private let implement = AIChatImplement(conversationId: "")
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = .white
        self.view.insertSubview(self.background, at: 0)
        self.view.addSubview(self.navigation)
        self.navigation.title = "AI陪聊"
        self.navigation.leftItem.setImage(UIImage(systemName: "chevron.backward")?.withTintColor(.black, renderingMode: .alwaysOriginal), for: .normal)
        self.navigation.clickClosure = { [weak self] type,_ in
            if type == .back {
                self?.pop()
            }
        }
                 
        self.implement.initAIChatSceneRequired { [weak self] error in
            if error == nil {
                self?.setupUI()
            } else {
                self?.pop()
            }
        }
        AgoraEntAuthorizedManager.checkAudioAuthorized(parent: self)
        setupService()
    }
    
    private func setupService() {
        guard let rtcService = AppContext.rtcService() else { return }
            
        guard let audioTextConvertorService = AppContext.audioTextConvertorService() else { return }
        
        audioTextConvertorService.run(appId: AppContext.shared.hyAppId, apiKey: AppContext.shared.hyAPIKey, apiSecret: AppContext.shared.hyAPISecret, convertType: .normal, agoraRtcKit: rtcService.rtcKit)
        
    }
    
    private func setupUI() {

        let bots = ChatBotViewController()
        let nav1 = UINavigationController(rootViewController: bots)
        let botsImage = UIImage(named: "chat_bot", in: .chatAIBundle, with: nil)?.withRenderingMode(.alwaysOriginal)
        let selectedBotsImage = UIImage(named: "chat_bot_highlight", in: .chatAIBundle, with: nil)?.withRenderingMode(.alwaysOriginal)
        nav1.tabBarItem = UITabBarItem(title: "智能体", image: botsImage, selectedImage: selectedBotsImage)
        
        nav1.tabBarItem.setTitleTextAttributes([.foregroundColor:UIColor(0x3C4267)], for: .selected)
        nav1.tabBarItem.setTitleTextAttributes([.foregroundColor:UIColor(0x979CBB)], for: .normal)
        let conversations = AIChatConversationsViewController()
        let nav2 = UINavigationController(rootViewController: conversations)
        let conversation = UIImage(named: "conversation", in: .chatAIBundle, with: nil)?.withRenderingMode(.alwaysOriginal)
        let conversation_focused = UIImage(named: "conversation_highlight", in: .chatAIBundle, with: nil)?.withRenderingMode(.alwaysOriginal)
        nav2.tabBarItem = UITabBarItem(title: "会话", image: conversation, selectedImage: conversation_focused)
        nav2.tabBarItem.setTitleTextAttributes([.foregroundColor:UIColor(0x979CBB)], for: .normal)
        nav2.tabBarItem.setTitleTextAttributes([.foregroundColor:UIColor(0x3C4267)], for: .selected)
        
        self.viewControllers = [nav1,nav2]
        self.selectedIndex = 0
        
        self.tabBar.backgroundColor = .white
        self.tabBar.layer.borderWidth = 0.5
        self.tabBar.layer.borderColor = UIColor(0xcccccc).cgColor
    }
    
    @objc func pop() {
        if self.navigationController != nil {
            self.navigationController?.popViewController(animated: true)
        } else {
            self.dismiss(animated: true)
        }
    }
    
    deinit {
        AppContext.destoryRtcService()
        AppContext.destoryConvertorService()
        AgoraChatClient.shared().logout(false)
        SVProgressHUD.dismiss()
    }
    
}

//
//  ShowRttViewController.swift
//  Pure1v1
//
//  Created by CYG on 2024/6/24.
//

import Foundation
import UIKit
import AgoraCommon

class ShowRttViewController: UIViewController {
    
    var clickDetailButonAction: ((RttLanguageSheetViewController)->())?
    
    // 实时翻译视图
    private let showRttView = ShowRttView()
    public var channelName = ""
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupRttView()
        self.showRttView.setStartRttStatus(open: RttManager.shared.checkSttStatus())
    }
    
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        dismiss(animated: true)
    }
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        modalPresentationStyle = .overFullScreen
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // 设置实时翻译视图
    private func setupRttView() {
        view.addSubview(showRttView)
        showRttView.translatesAutoresizingMaskIntoConstraints = false
        
        NSLayoutConstraint.activate([
            // 固定高度约束
            showRttView.heightAnchor.constraint(equalToConstant: 400),
            
            // 其他约束
            showRttView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            showRttView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            showRttView.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        ])
        
        showRttView.delegate = self
        showRttView.clipsToBounds = false
    }
}

extension ShowRttViewController: ShowRttViewDelegate {
    func onClickRtt(start: Bool) {
        if (start) {
            var rtcToken: String?
            var rtcToken2: String?

            let dispatchGroup = DispatchGroup()

            // 2. 生成 token for uid "1000"
            dispatchGroup.enter()
            NetworkManager.shared.generateTokens(channelName: channelName,
                                                 uid: "1000",
                                                 tokenGeneratorType: .token007,
                                                 tokenTypes: [.rtc]) { [weak self] tokens in
                defer { dispatchGroup.leave() }
                guard let self = self else { return }
                guard let token = tokens[AgoraTokenType.rtc.rawValue] else { return }
                rtcToken = token // 将 token 赋值给 rtcToken
                print("RttApiManager bot1000 token: \(rtcToken)")
            }

            // 3. 生成 token for uid "2000"
            dispatchGroup.enter()
            NetworkManager.shared.generateTokens(channelName: channelName,
                                                 uid: "2000",
                                                 tokenGeneratorType: .token007,
                                                 tokenTypes: [.rtc]) { [weak self] tokens in
                defer { dispatchGroup.leave() }
                guard let self = self else { return }
                guard let token = tokens[AgoraTokenType.rtc.rawValue] else { return }
                rtcToken2 = token // 将 token 赋值给 rtcToken2
                print("RttApiManager bot2000 token: \(rtcToken2)")
            }

            // 4. 通知在主队列上执行
            dispatchGroup.notify(queue: .main) {
                // 在这里访问 rtcToken 和 rtcToken2
                guard let rtcToken = rtcToken, let rtcToken2 = rtcToken2 else {
                    // 如果 rtcToken 或 rtcToken2 为空，处理错误情况
                    return
                }
                
                // 现在可以使用 rtcToken 和 rtcToken2 来调用 RttManager.shared.enableRtt
                RttManager.shared.enableRtt(channelName: self.channelName,
                                            subBotToken: rtcToken,
                                            pubBotToken: rtcToken2) { success in
                    if success {
                        self.showRttView.setStartRttStatus(open: true)
                        self.dismiss(animated: true)
                        AUIToast.show(text: "RTT实时语音功能已开启")
                    } else {
                        AUIToast.show(text: "RTT实时语音功能开启失败")
                        self.showRttView.setStartRttStatus(open: RttManager.shared.checkSttStatus())
                    }
                }
            }
        } else {
            RttManager.shared.disableRtt(force: false) { success in
                if success {
                    self.showRttView.setStartRttStatus(open: false)
                    AUIToast.show(text: "RTT实时语音功能已关闭")
                } else {
                    AUIToast.show(text: "RTT实时语音功能关闭失败")
                    self.showRttView.setStartRttStatus(open: RttManager.shared.checkSttStatus())
                }
            }
        }
    }
    
    func onClickSourceLanguage() {

        let vc = RttLanguageSheetViewController()
        //vc.transitioningDelegate = self?.transDelegate
        vc.title = "对方语言"
        vc.defaultSelectedIndex = RttManager.shared.selectedSourceLanguageIndex
        vc.dataArray = RttManager.shared.languages
        vc.didSelectedIndex = { index in
            RttManager.shared.selectSourceLanguage(at: index)
            self.showRttView.reloadChosenLanguage()
        }
        self.present(vc, animated: true, completion: {
            //vc.showBgView()
        })
        //self.dismiss(animated: true)
    }
    
    func onClickTargetLanguage() {
        let vc = RttLanguageSheetViewController()
        //vc.transitioningDelegate = self?.transDelegate
        vc.title = "翻译语言"
        vc.defaultSelectedIndex = RttManager.shared.selectedTargetLanguageIndex
        vc.dataArray = RttManager.shared.languages
        vc.didSelectedIndex = { index in
            RttManager.shared.selectTargetLanguage(at: index)
            self.showRttView.reloadChosenLanguage()
        }
        self.present(vc, animated: true, completion: {
            //vc.showBgView()
        })
    }
}

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
    
    // 设置实时翻译视图
    private func setupRttView() {
        view.addSubview(showRttView)
        showRttView.translatesAutoresizingMaskIntoConstraints = false
        
        NSLayoutConstraint.activate([
            showRttView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            showRttView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            showRttView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            showRttView.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor)
        ])
        
        showRttView.delegate = self
    }
}

extension ShowRttViewController: ShowRttViewDelegate {
    func onClickRtt(start: Bool) {
        if (start) {
            NetworkManager.shared.generateTokens(channelName: channelName,
                                                 uid: "1000",
                                                 tokenGeneratorType: .token007,
                                                 tokenTypes: [.rtc]) {[weak self] tokens in
                guard let self = self else {return}
                guard let rtcToken = tokens[AgoraTokenType.rtc.rawValue] else {
                    return
                }
                
                print("RttApiManager bot1000 token: \(rtcToken)")
                
                NetworkManager.shared.generateTokens(channelName: channelName,
                                                     uid: "2000",
                                                     tokenGeneratorType: .token007,
                                                     tokenTypes: [.rtc]) {[weak self] tokens in
                    guard let self = self else {return}
                    guard let rtcToken2 = tokens[AgoraTokenType.rtc.rawValue] else {
                        return
                    }
                    
                    print("RttApiManager bot2000 token: \(rtcToken2)")
                    
                    RttManager.shared.enableRtt(channelName: self.channelName, subBotToken: rtcToken, pubBotToken: rtcToken2) { success in
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
            }
        } else {
            RttManager.shared.disableRtt { success in
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
}

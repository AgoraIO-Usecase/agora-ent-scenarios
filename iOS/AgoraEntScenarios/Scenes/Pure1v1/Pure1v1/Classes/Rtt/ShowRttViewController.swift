//
//  ShowRttViewController.swift
//  Pure1v1
//
//  Created by CYG on 2024/6/24.
//

import Foundation
import UIKit
import AgoraCommon
import SVProgressHUD

class ShowRttViewController: UIViewController {
    
    var clickDetailButonAction: ((RttLanguageSheetViewController)->())?
    
    // 实时翻译视图
    private let showRttView = ShowRttView()
    public var channelName = ""
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupRttView()
        self.showRttView.setStartRttStatus(open: RttManager.shared.checkSttStatus())
        self.view.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(onTapClose)))
    }
    
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        modalPresentationStyle = .overFullScreen
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func resetRttStatus() {
        self.showRttView.setStartRttStatus(open: RttManager.shared.checkSttStatus())
        self.showRttView.reloadChosenLanguage()
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
    
    @objc func onTapClose() {
        dismiss(animated: true)
    }
}

extension ShowRttViewController: ShowRttViewDelegate {
    func onClickRtt(start: Bool) {
        SVProgressHUD.setDefaultMaskType(SVProgressHUDMaskType.black)
        SVProgressHUD.show()
        if (start) {
            generateTokens { success in
                if (success) {
                    RttManager.shared.enableRtt(channelName: self.channelName) { success in
                        if success {
                            self.showRttView.setStartRttStatus(open: true)
                            self.dismiss(animated: true)
                            AUIToast.show(text: "rtt_enable_success".pure1v1Localization())
                        } else {
                            AUIToast.show(text: "rtt_enable_failed".pure1v1Localization())
                            self.showRttView.setStartRttStatus(open: RttManager.shared.checkSttStatus())
                        }
                        SVProgressHUD.dismiss()
                    }
                } else {
                    AUIToast.show(text: "rtt_enable_failed".pure1v1Localization())
                    self.showRttView.setStartRttStatus(open: RttManager.shared.checkSttStatus())
                    SVProgressHUD.dismiss()
                }
            }
        } else {
            RttManager.shared.disableRtt(force: false) { success in
                if success {
                    self.showRttView.setStartRttStatus(open: false)
                    AUIToast.show(text: "rtt_disable_success".pure1v1Localization())
                } else {
                    AUIToast.show(text: "rtt_disable_failed".pure1v1Localization())
                    self.showRttView.setStartRttStatus(open: RttManager.shared.checkSttStatus())
                }
                SVProgressHUD.dismiss()
            }
        }
    }
    
    func onClickSourceLanguage() {
        
        if (RttManager.shared.checkSttStatus()) {
            AUIToast.show(text: "rtt_switch_note".pure1v1Localization())
            return
        }

        let vc = RttLanguageSheetViewController()
        vc.title = "rtt_source_language".pure1v1Localization()
        vc.defaultSelectedIndex = RttManager.shared.selectedSourceLanguageIndex
        vc.dataArray = RttManager.shared.languages
        vc.didSelectedIndex = { index in
            RttManager.shared.selectSourceLanguage(at: index)
            self.showRttView.reloadChosenLanguage()
        }
        self.present(vc, animated: true, completion: {
            //vc.showBgView()
        })
    }
    
    func onClickTargetLanguage() {
        
        if (RttManager.shared.checkSttStatus()) {
            AUIToast.show(text: "rtt_switch_note".pure1v1Localization())
            return
        }
        
        let vc = RttLanguageSheetViewController()
        vc.title = "rtt_target_language".pure1v1Localization()
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
    
    func generateTokens(completion: @escaping (Bool) -> ()) {
        if (RttManager.shared.subBotToken != "" && RttManager.shared.pubBotToken != "") {
            completion(true)
            return
        }
        
        var rtcToken: String?
        var rtcToken2: String?

        let dispatchGroup = DispatchGroup()

        // 2. 生成 token for subBotUid
        dispatchGroup.enter()
        NetworkManager.shared.generateTokens(channelName: channelName,
                                             uid: RttManager.shared.subBotUid,
                                             tokenGeneratorType: .token007,
                                             tokenTypes: [.rtc]) { [weak self] tokens in
            defer { dispatchGroup.leave() }
            guard let self = self else { return }
            guard let token = tokens[AgoraTokenType.rtc.rawValue] else { return }
            rtcToken = token // 将 token 赋值给 rtcToken
            print("RttApiManager bot1000 token: \(rtcToken)")
        }

        // 3. 生成 token for pubBotUid
        dispatchGroup.enter()
        NetworkManager.shared.generateTokens(channelName: channelName,
                                             uid: RttManager.shared.pubBotUid,
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
                completion(false)
                return
            }
            
            RttManager.shared.subBotToken = rtcToken
            RttManager.shared.pubBotToken = rtcToken2
            completion(true)
            return
        }
    }
}

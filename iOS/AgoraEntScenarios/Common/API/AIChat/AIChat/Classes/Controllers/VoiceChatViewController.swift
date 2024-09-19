//
//  AIChatVoiceViewController.swift
//  AIChat
//
//  Created by qinhui on 2024/9/3.
//
import UIKit
import AgoraCommon
import AgoraRtcKit

enum VoiceChatKey {
    static let voiceChatContext = "voiceChat"
    static let voiceConvertorContext = "voiceConvertor"
    static let voiceSwitchKey = "voice_switch_key"
}

class VoiceChatViewController: UIViewController {
    private var bot: AIChatBotProfileProtocol
    private var pingTimer: Timer?
    private lazy var agentChannelName = "aiChat_\(VLUserCenter.user.id)_\(bot.botId)"
    private lazy var agentService: AIChatAgentService = {
        let appId = AppContext.shared.appId
        let service = AIChatAgentService(channelName: agentChannelName, appId: appId)
        return service
    }()
    
    private let backgroundView: UIImageView = {
        let imageView = UIImageView()
        imageView.image = UIImage(named: "avatar_image", in: .chatAIBundle, with: nil)?.withRenderingMode(.alwaysOriginal)
        return imageView
    }()
    
    private let toggleSwitchLabel: UILabel = {
        let label = UILabel()
        label.text = "语音打断"
        label.textColor = UIColor.theme.neutralSpecialColor100
        label.font = UIFont.theme.bodyMedium
        return label
    }()
    
    private let toggleSwitch: UISwitch = {
        let s = UISwitch()
        s.onTintColor = .blue
        s.tintColor = UIColor.theme.neutralColor4
        s.addTarget(self, action: #selector(switchAction(_:)), for: .touchUpInside)
        return s
    }()
    
    private let floatingView: UIImageView = {
        let imageView = UIImageView()
        imageView.image = UIImage(named: "floating_button", in: .chatAIBundle, with: nil)?.withRenderingMode(.alwaysOriginal)
        return imageView
    }()
    
    private let avatarImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.image = UIImage(named: "bot_avatar_ico", in: .chatAIBundle, with: nil)?.withRenderingMode(.alwaysOriginal)
        imageView.tintColor = .blue
        imageView.contentMode = .scaleAspectFill
        imageView.layer.borderWidth = 3
        imageView.layer.borderColor = UIColor.theme.neutralColor100.cgColor
        imageView.layer.cornerRadius = 60
        imageView.layer.masksToBounds = true
        return imageView
    }()
    
    private let nicknameLabel: UILabel = {
        let label = UILabel()
        label.text = "昵称"
        label.textColor = UIColor.theme.neutralSpecialColor100
        label.font = UIFont.theme.bodyLarge
        return label
    }()
    
    private let hintLabel: UILabel = {
        let label = UILabel()
        label.text = "试试用语音打断或继续会话"
        label.textColor = UIColor.theme.neutralSpecialColor100
        label.font = UIFont.theme.bodyMedium
        return label
    }()
    
    private lazy var waveformView: AIChatAudioRecorderView = {
        let view = AIChatAudioRecorderView(frame: CGRect(x: 0, y: 0, width: self.view.width, height: self.view.height))
        view.image = nil
        return view
    }()
    
    private let micButton: UIButton = {
        let button = UIButton()
        button.setImage(UIImage(named: "voice_mic_on", in: .chatAIBundle, with: nil)?.withRenderingMode(.alwaysOriginal), for: .normal)
        button.setImage(UIImage(named: "voice_mic_off", in: .chatAIBundle, with: nil)?.withRenderingMode(.alwaysOriginal), for: .selected)
        button.tintColor = .blue
        button.addTarget(self, action: #selector(micButtonAction(_:)), for: .touchUpInside)
        return button
    }()
    
    private let stopButton: UIButton = {
        let button = UIButton()
        button.setImage(UIImage(named: "voice_stop_btn", in: .chatAIBundle, with: nil)?.withRenderingMode(.alwaysOriginal), for: .normal)
        button.setImage(UIImage(named: "voice_stop_btn_dis", in: .chatAIBundle, with: nil)?.withRenderingMode(.alwaysOriginal), for: .selected)
        button.addTarget(self, action: #selector(stopButtonAction(_:)), for: .touchUpInside)
        return button
    }()
    
    private let hangupButton: UIButton = {
        let button = UIButton()
        button.setImage(UIImage(named: "voice_close_btn", in: .chatAIBundle, with: nil)?.withRenderingMode(.alwaysOriginal), for: .normal)
        button.tintColor = .red
        button.addTarget(self, action: #selector(hangupButtonAction), for: .touchUpInside)

        return button
    }()
    
    deinit {
        aichatPrint("deinit VoiceChatViewController", context: "VoiceChatViewController")
    }
    
    init(bot: AIChatBotProfileProtocol) {
        self.bot = bot
        super.init(nibName: nil, bundle: nil)
        aichatPrint("init VoiceChatViewController", context: "VoiceChatViewController")
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .white
        
        aichatPrint("viewDidLoad", context: "VoiceChatViewController")
        let greeting = bot.type == .common ? "aichat_common_greeting".toSceneLocalization() : "aichat_custom_greeting".toSceneLocalization()
        startAgent(greeting: greeting as String)
        setupRtc()
        setupUI()
    }
    
    @objc private func switchAction(_ s: UISwitch) {
        if s.isOn {
            ToastView.show(text: "语音打断已打开")
        } else {
            ToastView.show(text: "语音打断已关闭")
        }
        
        updateHintLabel(state: s.isOn)
//        updateStopBtn(state: s.isOn)
        
        updateVoiceInterruptStatus()
    }
    
    @objc private func micButtonAction(_ button: UIButton) {
        button.isSelected = !button.isSelected
        AppContext.rtcService()?.muteLocalAudioStream(channelName: agentChannelName, isMute: button.isSelected)
    }
    
    @objc private func stopButtonAction(_ button: UIButton) {
//        if button.isSelected {
//            ToastView.show(text: "请开启语音打断后再尝试打断智能体")
//            return
//        }
        AIChatLogger.info("interruptAgent start", context: VoiceChatKey.voiceChatContext)
        agentService.interruptAgent { msg, error in
            AIChatLogger.info("interrupt agent completion: \(error?.localizedDescription ?? "success")", context: VoiceChatKey.voiceChatContext)
        }
    }
    
    @objc private func hangupButtonAction() {
        aichatPrint("hangupButtonAction", context: "VoiceChatViewController")
        destoryPingTimer()
        stopAgent()
    }
    
    private func stopAgent() {
        agentService.stopAgent { [weak self] msg, error in
            guard let self = self else {return}
            AppContext.rtcService()?.leaveChannel(channelName: self.agentChannelName)
            AppContext.rtcService()?.removeDelegate(channelName: agentChannelName, delegate: self)
            self.dismiss(animated: true)
        }
    }
    
    private func pingAgent() {
        AIChatLogger.info("pingAgent start", context: VoiceChatKey.voiceChatContext)
        agentService.pingAgent { msg, error in
            AIChatLogger.info("pingAgent completion: \(error?.localizedDescription ?? "success")", context: VoiceChatKey.voiceChatContext)
        }
    }
    
    private func startAgent(greeting: String? = nil) {
        AIChatLogger.info("startAgent start", context: VoiceChatKey.voiceChatContext)
        agentService.startAgent(prompt: bot.prompt,
                                voiceId: bot.voiceId,
                                greeting: greeting) { [weak self] msg, error in
            AIChatLogger.info("startAgent completion: \(error?.localizedDescription ?? "success")", context: VoiceChatKey.voiceChatContext)
            if error == nil {
                self?.startPingTimer()
            }
        }
    }
    
    private func updateVoiceInterruptStatus() {
        AIChatLogger.info("voiceInterruptAgent start", context: VoiceChatKey.voiceChatContext)
        agentService.voiceInterruptAgent(enable: toggleSwitch.isOn) { msg, error in
            AIChatLogger.info("voiceInterruptAgent completion：\(error?.localizedDescription ?? "success")", context: VoiceChatKey.voiceChatContext)
        }
    }
    
    private func startPingTimer() {
        pingTimer = Timer.scheduledTimer(timeInterval: 5.0, target: self, selector: #selector(pingTimerFired), userInfo: nil, repeats: true)
    }
    
    private func destoryPingTimer() {
        pingTimer?.invalidate()
        pingTimer = nil
    }
    
    @objc func pingTimerFired() {
        pingAgent()
    }
    
    private func setupRtc() {
        AppContext.rtcService()?.joinChannel(channelName: agentChannelName)
        AppContext.rtcService()?.updateRole(channelName: agentChannelName, role: .broadcaster)
        AppContext.rtcService()?.addDelegate(channelName: agentChannelName, delegate: self)
    }
    
    private func setupUI() {
        nicknameLabel.text = bot.botName
        avatarImageView.sd_setImage(with: URL(string: bot.botIcon), placeholderImage: nil)
        view.addSubview(backgroundView)
        view.addSubview(toggleSwitchLabel)
        view.addSubview(toggleSwitch)
        view.addSubview(floatingView)
        
        backgroundView.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            backgroundView.leftAnchor.constraint(equalTo: view.leftAnchor),
            backgroundView.topAnchor.constraint(equalTo: view.topAnchor),
            backgroundView.rightAnchor.constraint(equalTo: view.rightAnchor),
            backgroundView.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        ])
        
        toggleSwitchLabel.translatesAutoresizingMaskIntoConstraints = false
        toggleSwitch.translatesAutoresizingMaskIntoConstraints = false
        floatingView.translatesAutoresizingMaskIntoConstraints = false
        
        NSLayoutConstraint.activate([
            toggleSwitch.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 12),
            toggleSwitch.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -16),
            
            toggleSwitchLabel.centerYAnchor.constraint(equalTo: toggleSwitch.centerYAnchor),
            toggleSwitchLabel.rightAnchor.constraint(equalTo: toggleSwitch.leftAnchor, constant: -12),
            
            floatingView.topAnchor.constraint(equalTo: toggleSwitch.bottomAnchor, constant: 5),
            floatingView.rightAnchor.constraint(equalTo: toggleSwitch.rightAnchor),
            floatingView.heightAnchor.constraint(equalToConstant: 39),
            floatingView.widthAnchor.constraint(equalToConstant: 99)
        ])
        
        view.addSubview(avatarImageView)
        view.addSubview(nicknameLabel)
        
        avatarImageView.translatesAutoresizingMaskIntoConstraints = false
        nicknameLabel.translatesAutoresizingMaskIntoConstraints = false
        
        NSLayoutConstraint.activate([
            avatarImageView.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            avatarImageView.centerYAnchor.constraint(equalTo: view.centerYAnchor, constant: -100),
            avatarImageView.widthAnchor.constraint(equalToConstant: 120),
            avatarImageView.heightAnchor.constraint(equalToConstant: 120),
            
            nicknameLabel.topAnchor.constraint(equalTo: avatarImageView.bottomAnchor, constant: 36),
            nicknameLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor)
        ])
        
        view.addSubview(hintLabel)
        view.addSubview(waveformView)
        view.addSubview(micButton)
        view.addSubview(stopButton)
        view.addSubview(hangupButton)
        
        hintLabel.translatesAutoresizingMaskIntoConstraints = false
        waveformView.translatesAutoresizingMaskIntoConstraints = false
        micButton.translatesAutoresizingMaskIntoConstraints = false
        stopButton.translatesAutoresizingMaskIntoConstraints = false
        hangupButton.translatesAutoresizingMaskIntoConstraints = false
        
        NSLayoutConstraint.activate([
            hintLabel.bottomAnchor.constraint(equalTo: waveformView.topAnchor, constant: -8),
            hintLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            
            waveformView.bottomAnchor.constraint(equalTo: micButton.topAnchor, constant: -39),
            waveformView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            waveformView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            waveformView.heightAnchor.constraint(equalToConstant: 50),
            
            micButton.centerYAnchor.constraint(equalTo: stopButton.centerYAnchor),
            micButton.trailingAnchor.constraint(equalTo: stopButton.leadingAnchor, constant: -54),
            
            stopButton.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -75),
            stopButton.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            
            hangupButton.centerYAnchor.constraint(equalTo: stopButton.centerYAnchor),
            hangupButton.leadingAnchor.constraint(equalTo: stopButton.trailingAnchor, constant: 54)
        ])
        
        let switchState = (UserDefaults.standard.object(forKey: VoiceChatKey.voiceSwitchKey) as? Bool) ?? false
        toggleSwitch.isOn = switchState
//        stopButton.isSelected = !switchState
        
        updateHintLabel(state: switchState)
    }
    
    private func updateHintLabel(state: Bool) {
        hintLabel.isHidden = state
    }
    
//    private func updateStopBtn(state: Bool) {
//        stopButton.isSelected = !state
//    }
}

extension VoiceChatViewController: AgoraRtcEngineDelegate {
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        aichatWarn("didJoinChannel: \(uid) elapsed: \(elapsed)", context: "VoiceChatViewController")
        updateVoiceInterruptStatus()
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason) {
        aichatWarn("didOfflineOfUid: \(uid) reason: \(reason.rawValue)", context: "VoiceChatViewController")
        startAgent()
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
        aichatWarn("didOccurError: \(errorCode.rawValue)", context: "VoiceChatViewController")
    }
    
    public func rtcEngine(_ engine: AgoraRtcEngineKit, reportAudioVolumeIndicationOfSpeakers speakers: [AgoraRtcAudioVolumeInfo], totalVolume: Int) {
        guard speakers.count > 0, totalVolume >= 10 else {return}
        DispatchQueue.main.async {
            self.waveformView.updateIndicatorImage(volume: totalVolume)
        }
    }
}

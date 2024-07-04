//
//  TranscriptSubtitleView.swift
//  AgoraTranscriptSubtitle
//
//  Created by ZYP on 2024/6/17.
//

import UIKit

@objc public class TranscriptSubtitleView: UIView {
    private let messageView = MessageView()
    private let transcriptSubtitleMachine = TranscriptSubtitleMachine()
    private let logTag = "TranscriptSubtitleView"
    
    /// the color of text, when the recognized state is final.
    @objc public var finalTextColor: UIColor = .white { didSet{ updateParam(varName:"finalTextColor") }}
    /// the color of text, when the recognized state is nonfinal.
    @objc public var nonFinalTextColor: UIColor = .gray { didSet{ updateParam(varName:"nonFinalTextColor") }}
    /// the font of text. include TranscriptContent and TranslateContent.
    @objc public var textFont: UIFont = .systemFont(ofSize: 16) { didSet{ updateParam(varName:"textFont") }}
    /// the background color of text area.
    @objc public var textAreaBackgroundColor: UIColor = UIColor.black.withAlphaComponent(0.25) { didSet{ updateParam(varName:"textAreaBackgroundColor") }}
    /// show or hide the transcript content.
    @objc public var showTranscriptContent = true { didSet{ updateParam(varName:"showTranscriptContent") }}
    
    @objc public var debugParam = DebugParam() { didSet { updateDebugParam() } }
     
    // MARK: - Public Method
    
    @objc public convenience init(frame: CGRect, loggers: [ILogger] = [AGFileLogger(), ConsoleLogger()]) {
        Log.setLoggers(loggers: loggers)
        self.init(frame: frame)
    }
    
    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    /// Not Public, Please use `init(frame, loggers)`
    override init(frame: CGRect) {
        super.init(frame: frame)
        Log.debug(text: "version \(String.versionName)", tag: logTag)
        setupUI()
        commonInit()
    }
    
    deinit {
        Log.info(text: "deinit", tag: logTag)
    }
    
    /// push message data
    /// - Parameter data: pb data, recv from stt server.
    /// - Parameter uid: user id
    @objc public func pushMessageData(data: Data, uid: UInt) {
        transcriptSubtitleMachine.pushMessageData(data: data, uid: uid)
    }
    
    /// clear all data, and the view will be empty.
    @objc public func clear() {
        transcriptSubtitleMachine.clear()
        messageView.clear()
    }
    
    // MARK: - Private Method
    private func setupUI() {
        backgroundColor = .clear
        messageView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(messageView)
        messageView.leftAnchor.constraint(equalTo: leftAnchor).isActive = true
        messageView.rightAnchor.constraint(equalTo: rightAnchor).isActive = true
        messageView.topAnchor.constraint(equalTo: topAnchor).isActive = true
        messageView.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
    }
    
    private func commonInit() {
        transcriptSubtitleMachine.delegate = self
    }
    
    private func updateDebugParam() {
        Log.info(text: "debug param was updated: \(debugParam)", tag: logTag)
        transcriptSubtitleMachine.debugParam = debugParam
    }
    
    private func updateParam(varName: String) {
        switch varName {
        case "finalTextColor":
            messageView.finalTextColor = finalTextColor
            Log.info(text: "param was updated, \(varName) = \(finalTextColor)", tag: logTag)
            break
        case "nonFinalTextColor":
            messageView.nonFinalTextColor = nonFinalTextColor
            Log.info(text: "param was updated, \(varName) = \(nonFinalTextColor)", tag: logTag)
            break
        case "textFont":
            messageView.textFont = textFont
            Log.info(text: "param was updated, \(varName) = \(textFont)", tag: logTag)
            break
        case "textAreaBackgroundColor":
            messageView.textAreaBackgroundColor = textAreaBackgroundColor
            Log.info(text: "param was updated, \(varName) = \(textAreaBackgroundColor)", tag: logTag)
            break
        case "showTranscriptContent":
            transcriptSubtitleMachine.showTranscriptContent = showTranscriptContent
            Log.info(text: "param was updated, \(varName) = \(showTranscriptContent)", tag: logTag)
            break
        default:
            Log.errorText(text: "unknow var update \(varName)", tag: logTag)
            fatalError("unknow var update \(varName)")
        }
    }
}

// MARK: - TranscriptSubtitleMachineDelegate
extension TranscriptSubtitleView: TranscriptSubtitleMachineDelegate {
    func transcriptSubtitleMachine(_ machine: TranscriptSubtitleMachine, didAddRenderInfo renderInfo: RenderInfo) {
        messageView.addOrUpdate(renderInfo: renderInfo)
    }
    
    func transcriptSubtitleMachine(_ machine: TranscriptSubtitleMachine, didUpadteRenderInfo renderInfo: RenderInfo) {
        messageView.addOrUpdate(renderInfo: renderInfo)
    }
}

//
//  TranscriptSubtitleView.swift
//  AgoraTranscriptSubtitle
//
//  Created by ZYP on 2024/6/17.
//

import UIKit

@objc public class TranscriptSubtitleView: UIView {
    /**
     决定分割句子的时间间隔 单位：ms，
     如果在两句之间，相差大于该值，则认为需要分割
     **/
    private let messageView = MessageView()
    @objc public var spiltSentenceTimeInterval: UInt = 150
    @objc public var finalTextColor: UIColor = .white { didSet{ messageView.finalTextColor = finalTextColor } }
    @objc public var nonFinalTextColor: UIColor = .gray { didSet{ messageView.nonFinalTextColor = nonFinalTextColor } }
    @objc public var textFont: UIFont = .systemFont(ofSize: 16) { didSet{ messageView.textFont = textFont } }
    @objc public var textAreaBackgroundColor: UIColor = UIColor.black.withAlphaComponent(0.25) { didSet{ messageView.textAreaBackgroundColor = textAreaBackgroundColor } }
    @objc public var debug_dump_input = false { didSet { transcriptSubtitleMachine.debug_dump_input = debug_dump_input } }
    @objc public var debug_dump_deserialize = false { didSet { transcriptSubtitleMachine.debug_dump_deserialize = debug_dump_deserialize } }
    private let transcriptSubtitleMachine = TranscriptSubtitleMachine()
    private let logTag = "TranscriptSubtitleView"
    
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
}

// MARK: - TranscriptSubtitleMachineDelegate
extension TranscriptSubtitleView: TranscriptSubtitleMachineDelegate {
    func transcriptSubtitleMachine(_ machine: TranscriptSubtitleMachine, didAddRenderInfo renderInfo: TranscriptSubtitleMachine.RenderInfo) {
        messageView.addItem(renderInfo: renderInfo)
    }
    
    func transcriptSubtitleMachine(_ machine: TranscriptSubtitleMachine, didUpadteRenderInfo renderInfo: TranscriptSubtitleMachine.RenderInfo) {
        messageView.updateLast(renderInfo: renderInfo)
    }
}

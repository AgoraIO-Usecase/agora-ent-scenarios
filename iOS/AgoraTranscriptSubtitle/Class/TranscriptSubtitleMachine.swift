//
//  TranscriptSubtitleMachine.swift
//  AgoraTranscriptSubtitle
//
//  Created by ZYP on 2024/6/21.
//

import UIKit

class TranscriptSubtitleMachine {
    private let logTag = "TranscriptSubtitleMachine"
    private let deserializer = ProtobufDeserializer()
    private let queue = DispatchQueue(label: "agora.TranscriptSubtitleMachine.queue")
    private var infoCache = InfoCache()
    weak var delegate: TranscriptSubtitleMachineDelegate?
    var debug_dump_input = false
    var debug_dump_deserialize = false
    
    func pushMessageData(data: Data, uid: UInt) {
        queue.async { [weak self] in
            guard let self = self else {
                return
            }
            self._pushMessageData(data: data, uid: uid)
        }
    }
    
    func clear() {
        infoCache.clear()
    }
    
    // MARK: - private
    private func _pushMessageData(data: Data, uid: UInt) {
        if debug_dump_input {
            Log.debug(text: "data:\(data.base64EncodedString())", tag: logTag)
        }
        
        guard let message = deserializer.deserialize(data: data) else {
            Log.errorText(text: "deserialize fail: \(data.base64EncodedString())", tag: logTag)
            return
        }
        
        if debug_dump_deserialize {
            Log.debug(text: "deserialize:\(message.jsonString)", tag: logTag)
        }
        
        _handleMessage(message: message, uid: uid)
    }
    
    private func _handleMessage(message: ProtobufDeserializer.DataStreamMessage, uid: UInt) {
        Log.debug(text: "_handleMessage", tag: logTag)
        guard let type = MessageType(string: message.dataType!) else {
            Log.errorText(text: "unknown message type: \(message.dataType!)", tag: logTag)
            return
        }
        
        if type == .transcribe {
            let words = message.words.map({ TranscriptSubtitleMachine.makeTranscriptWords(sttWord: $0) }).flatMap({ $0 })
            if words.allText == "丁结核核酸检测结果。" {
                print("")
            }
            if let lastOne = infoCache.getLastInfo(uid: uid), !lastOne.transcriptInfo.words.isFinal { /** append */
                lastOne.transcriptInfo.words = words
                lastOne.transcriptInfo.duration += message.durationMs
                lastOne.transcriptInfo.textTs = message.textTs
                let renderInfo = TranscriptSubtitleMachine.convertToRenderInfo(info: lastOne)
                invokeUpdate(self, renderInfo: renderInfo)
            }
            else {
                let transcriptRenderInfo = TranscriptInfo()
                transcriptRenderInfo.startMs = message.textTs
                transcriptRenderInfo.duration = message.durationMs
                transcriptRenderInfo.words = words
                let info = Info(transcriptRenderInfo: transcriptRenderInfo, translateRenderInfo: .init())
                infoCache.addInfo(uid: uid, renderInfo: info)
                let renderInfo = TranscriptSubtitleMachine.convertToRenderInfo(info: info)
                invokeAdd(self, renderInfo: renderInfo)
            }
        }
        else {
            let words = (message.transArray as! [SttTranslation]).map({ TranslateWord(sttTranslation: $0) })
            if words.allText.contains("Ding tuberculosis nucleic acid test results") {
                print("")
            }
            
            if let lastOne = infoCache.getLast(uid: uid, with: message.textTs) { /** append */
                lastOne.translateInfo.words = words
                lastOne.translateInfo.duration += message.durationMs
                let renderInfo = TranscriptSubtitleMachine.convertToRenderInfo(info: lastOne)
                invokeUpdate(self, renderInfo: renderInfo)
            }
            else {
//                Log.warning(text: "get a tarnslate message but no transcript message before it.", tag: logTag)
//                let translateRenderInfo = TranslateInfo()
//                translateRenderInfo.startMs = message.textTs
//                translateRenderInfo.duration = message.durationMs
//                translateRenderInfo.words = words
//                let info = Info(transcriptRenderInfo: .init(), translateRenderInfo: translateRenderInfo)
//                infoCache.addInfo(uid: uid, renderInfo: info)
//                let renderInfo = TranscriptSubtitleMachine.convertToRenderInfo(info: info)
//                invokeAdd(self, renderInfo:renderInfo)
            }
        }
    }
}

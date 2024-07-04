//
//  TranscriptSubtitleMachine.swift
//  AgoraTranscriptSubtitle
//
//  Created by ZYP on 2024/6/21.
//

import UIKit

class TranscriptSubtitleMachine {
    let logTag = "Machine"
    private let deserializer = ProtobufDeserializer()
    private let queue = DispatchQueue(label: "agora.TranscriptSubtitleMachine.queue")
    var infoCache = InfoCache()
    var intermediateInfoCache = InfoCache()
    var showTranscriptContent: Bool = true
    weak var delegate: TranscriptSubtitleMachineDelegate?
    
    var debugParam = DebugParam()
    weak var debug_intermediateDelegate: DebugMachineIntermediateDelegate?
    
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
        intermediateInfoCache.clear()
    }
    
    // MARK: - private
    private func _pushMessageData(data: Data, uid: UInt) {
        if debugParam.dump_input {
            Log.debug(text: "data:\(data.base64EncodedString())", tag: logTag)
        }
        
        guard let message = deserializer.deserialize(data: data) else {
            Log.errorText(text: "deserialize fail: \(data.base64EncodedString())", tag: logTag)
            return
        }
        
        if debugParam.dump_deserialize {
            Log.debug(text: "deserialize:\(message.jsonString)", tag: logTag)
        }
        
        _handleMessage(message: message, uid: uid)
    }
}

//
//  TranscriptSubtitleMachine+Handle.swift
//  AgoraTranscriptSubtitle
//
//  Created by ZYP on 2024/6/24.
//

import Foundation

extension TranscriptSubtitleMachine {
    func _handleMessage(message: ProtobufDeserializer.DataStreamMessage, uid: UInt) {
        Log.debug(text: "_handleMessage", tag: logTag)
        guard let type = MessageType(string: message.dataType!) else {
            Log.errorText(text: "unknown message type: \(message.dataType!)", tag: logTag)
            return
        }
        
        if type == .transcribe {
            if debugParam.dump_deserialize {
                Log.debug(text: "[transcriptBeautyString]:\(message.debug_transcriptBeautyString)", tag: logTag)
            }
            
            _handleTranscriptPreProcess(message: message, uid: uid)
            
            if !debugParam.useFinalTagAsParagraphDistinction {
                _handleTranscriptPostProcess(message: message, uid: uid)
            }
            return
        }
        if type == .translate {
            if !debugParam.showTranslateContent {
                return
            }
            
            _handleTranlatePreProcess(message: message, uid: uid)
            
            if !debugParam.useFinalTagAsParagraphDistinction {
                _handleTranslatePostProcess(message: message, uid: uid)
            }
        }
    }
}

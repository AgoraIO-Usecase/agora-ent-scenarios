//
//  TranscriptSubtitleMachine+Handle+PostProcess.swift
//  AgoraTranscriptSubtitle
//
//  Created by ZYP on 2024/6/27.
//

import Foundation

extension TranscriptSubtitleMachine { /** handle message for Paragraph **/
    
    func _handleTranscriptPostProcess(message: ProtobufDeserializer.DataStreamMessage, uid: UInt) {
        if message.splitToTranscriptWords.allText == "你可以做什么？你是谁？" {
            print("")
        }
        var intermediateInfos = intermediateInfoCache.getAllInfo(uid: uid)
        if let lastOne = intermediateInfos.last, lastOne.transcriptInfo.sentenceEndIndex < 0 { /** append **/
            let infos = infoCache.getAllInfo(uid: uid)
            var willMergeInfos = [Info]()
            
            if infos.last!.transcriptInfo.sentenceEndIndex >= 0 { /** last is T **/
                willMergeInfos.append(infos.last!)
            }
            else { /** last is F **/
                for info in infos.reversed() {
                    if info.transcriptInfo.sentenceEndIndex >= 0 {
                        break
                    }
                    else {
                        willMergeInfos.append(info)
                    }
                }
            }
            
            willMergeInfos = willMergeInfos.reversed()
            
            lastOne.transcriptInfo.words = willMergeInfos.map({ $0.transcriptInfo.words }).flatMap({ $0 })
            lastOne.transcriptInfo.textTs = message.textTs
            lastOne.transcriptInfo.sentenceEndIndex = message.sentenceEndIndex
            let renderInfo = convertToRenderInfo(info: lastOne, useTranscriptText: showTranscriptContent)
            invokeUpdate(self, renderInfo: renderInfo)
        }
        else { /** add **/
            let words = message.words.map({ $0.splitToTranscriptWords }).flatMap({ $0 })
            let transcriptInfo = TranscriptInfo()
            transcriptInfo.startMs = message.textTs
            transcriptInfo.textTs = message.textTs
            transcriptInfo.sentenceEndIndex = message.sentenceEndIndex
            transcriptInfo.duration = message.durationMs
            transcriptInfo.words = words
            let info = Info(transcriptInfo: transcriptInfo, translateInfos: [])
            intermediateInfoCache.addInfo(uid: uid, info: info)
            let renderInfo = convertToRenderInfo(info: info, useTranscriptText: showTranscriptContent)
            invokeUpdate(self, renderInfo: renderInfo)
        }
        
        intermediateInfos = intermediateInfoCache.getAllInfo(uid: uid)
        debug_intermediateDelegate?.debugMachineIntermediate(self, diduUpdate: intermediateInfos)
    }
    
    func _handleTranslatePostProcess(message: ProtobufDeserializer.DataStreamMessage, uid: UInt) {
        let words = (message.transArray as! [SttTranslation]).map({ TranslateWord(sttTranslation: $0) })
        if let lastOne = intermediateInfoCache.getLast(uid: uid, with: message.textTs) { /** append */
            updateTranslateInfo(info: lastOne,
                                words: words,
                                duration: message.durationMs,
                                textTs: message.textTs)
            let renderInfo = convertToRenderInfo(info: lastOne, useTranscriptText: showTranscriptContent)
            invokeUpdate(self, renderInfo: renderInfo)
            return
        }
        
        Log.errorText(text: "can not find a transcript message before a translate message: \(message.jsonString)", tag: logTag)
    }
}

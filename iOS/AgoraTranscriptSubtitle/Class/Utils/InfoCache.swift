//
//  InfoCache.swift
//  AgoraTranscriptSubtitle
//
//  Created by ZYP on 2024/6/24.
//

import Foundation

class InfoCache {
    typealias UidType = UInt
    typealias Info = TranscriptSubtitleMachine.Info
    private var infoDict = [UidType : [Info]]()
    
    func addInfo(uid: UidType, renderInfo: Info) {
        if var infos = infoDict[uid] {
            infos.append(renderInfo)
            infoDict[uid] = infos
        } else {
            infoDict[uid] = [renderInfo]
        }
    }
    
    func getLastInfo(uid: UidType) -> TranscriptSubtitleMachine.Info? {
        return infoDict[uid]?.last
    }
    
    func getLast(uid: UidType, with textTs: Int64) -> TranscriptSubtitleMachine.Info? {
        return infoDict[uid]?.last(where: { textTs >= $0.transcriptInfo.startMs && textTs <= $0.transcriptInfo.textTs })
    }
    
    func clear() {
        infoDict = [:]
    }
}

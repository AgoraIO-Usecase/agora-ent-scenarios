//
//  InfoCache.swift
//  AgoraTranscriptSubtitle
//
//  Created by ZYP on 2024/6/24.
//

import Foundation

class InfoCache {
    private var infoDict = [UidType : [Info]]()
    
    func addInfo(uid: UidType, info: Info) {
        if var infos = infoDict[uid] {
            infos.append(info)
            infoDict[uid] = infos
        } else {
            infoDict[uid] = [info]
        }
    }
    
    func getLastInfo(uid: UidType) -> Info? {
        return infoDict[uid]?.last
    }
    
    func getAllInfo(uid: UidType) -> [Info] {
        return infoDict[uid] ?? []
    }
    
    func getInfo(uid: UidType, startMs: Int64) -> Info?  {
        return infoDict[uid]?.last(where: { startMs == $0.transcriptInfo.startMs})
    }
    
    func getLast(uid: UidType, with textTs: Int64) -> Info? {
        return infoDict[uid]?.last(where: { textTs >= $0.transcriptInfo.startMs && textTs <= $0.transcriptInfo.textTs })
    }
    
    func clear() {
        infoDict = [:]
    }
}

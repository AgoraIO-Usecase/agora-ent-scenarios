//
//  TranscriptSubtitleMachine+Handle+Calculate.swift
//  AgoraTranscriptSubtitle
//
//  Created by ZYP on 2024/6/28.
//

import Foundation

extension TranscriptSubtitleMachine {
    func convertToRenderInfo(info: Info, useTranscriptText: Bool) -> RenderInfo {
        let transcriptString = useTranscriptText ? info.transcriptInfo.words.allText : ""
        var index = 0
        var transcriptRanges = [SegmentRangeInfo]()
        if useTranscriptText {
            for w in info.transcriptInfo.words {
                let range = NSRange(location: index, length: w.text.count)
                let segment = SegmentRangeInfo(range: range, isFinal: w.isFinal)
                transcriptRanges.append(segment)
                index += w.text.count
            }
        }
        
        var translateRenderInfos = [TranslateRenderInfo]()
        for translateInfo in info.translateInfos {
            index = 0
            var ranges = [SegmentRangeInfo]()
            let string = translateInfo.words.map({ $0.text }).joined()
            var lang = ""
            for w in translateInfo.words {
                let range = NSRange(location: index, length: w.text.count)
                let segment = SegmentRangeInfo(range: range, isFinal: w.isFinal)
                ranges.append(segment)
                lang = w.lang
                index += w.text.count
            }
            
            let translateRenderInfo = TranslateRenderInfo(lang: lang, text: string, ranges: ranges)
            translateRenderInfos.append(translateRenderInfo)
        }
        
        return RenderInfo(identifier: info.transcriptInfo.startMs,
            transcriptText: transcriptString,
                                      transcriptRanges: transcriptRanges,
                                      translateRenderInfos: translateRenderInfos)
    }
    
    func updateTranslateInfo(info: Info,
                             words: [TranslateWord],
                             duration: Int32,
                             textTs: Int64) {
        guard let lang = words.first?.lang else {
            Log.errorText(text: "updateTranslateInfo fail, can not find a lange", tag: self.logTag)
            return
        }
        
        if let translateInfo = info.translateInfos.first(where: { $0.words.lang! == lang }) {
            translateInfo.words = words
        }
        else {
            let translateInfo = TranslateInfo()
            translateInfo.startMs = textTs
            translateInfo.words = words
            info.translateInfos.append(translateInfo)
        }
        
    }
}

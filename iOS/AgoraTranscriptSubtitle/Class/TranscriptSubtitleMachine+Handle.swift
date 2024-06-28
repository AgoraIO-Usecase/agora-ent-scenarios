//
//  TranscriptSubtitleMachine+Handle.swift
//  AgoraTranscriptSubtitle
//
//  Created by ZYP on 2024/6/24.
//

import Foundation

extension TranscriptSubtitleMachine {
    
    /// spilit stt words to transcript words
    static func makeTranscriptWords(sttWord: SttWord) -> [TranscriptWord] {
        return sttWord.text!.map({ String($0) }).map({ TranscriptWord.init(text: $0, isFinal: sttWord.isFinal, confidence: sttWord.confidence, startMs: sttWord.startMs, durationMs: sttWord.durationMs) })
    }
    
    static func convertToRenderInfo(info: TranscriptSubtitleMachine.Info) -> MessageView.RenderInfo {
        let transcriptString = info.transcriptInfo.words.map({ $0.text }).joined()
        var index = 0
        var transcriptRanges = [SegmentRangeInfo]()
        for w in info.transcriptInfo.words {
            let range = NSRange(location: index, length: w.text.count)
            let segment = SegmentRangeInfo(range: range, isFinal: w.isFinal)
            transcriptRanges.append(segment)
            index += w.text.count
        }
        
        let translateString = info.translateInfo.words.map({ $0.text }).joined()
        index = 0
        var translateRanges = [SegmentRangeInfo]()
        for w in info.translateInfo.words {
            let range = NSRange(location: index, length: w.text.count)
            let segment = SegmentRangeInfo(range: range, isFinal: w.isFinal)
            translateRanges.append(segment)
            index += w.text.count
        }
        
        return MessageView.RenderInfo(transcriptText: transcriptString,
                                      translatetText: translateString,
                                      transcriptRanges: transcriptRanges,
                                      translateRanges: translateRanges,
                                      identifier: info.transcriptInfo.startMs)
    }
}

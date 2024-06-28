//
//  TranscriptSubtitleMachine+Info.swift
//  AgoraTranscriptSubtitle
//
//  Created by ZYP on 2024/6/21.
//

import Foundation

extension TranscriptSubtitleMachine { /** info **/
    class TranscriptWord {
        let text: String
        let isFinal: Bool
        let confidence: Double
        let startMs: Int32
        let durationMs: Int32
        
        init(sttWord: SttWord) {
            self.text = sttWord.text
            self.isFinal = sttWord.isFinal
            self.confidence = sttWord.confidence
            self.startMs = sttWord.startMs
            self.durationMs = sttWord.durationMs
        }
        
        init(text: String, isFinal: Bool, confidence: Double, startMs: Int32, durationMs: Int32) {
            self.text = text
            self.isFinal = isFinal
            self.confidence = confidence
            self.startMs = startMs
            self.durationMs = durationMs
        }
    }
    
    class TranslateWord {
        let text: String
        let isFinal: Bool
        let lang: String
        
        init(sttTranslation: SttTranslation) {
            self.text = (sttTranslation.textsArray as! [String]).joined()
            self.isFinal = sttTranslation.isFinal
            self.lang = sttTranslation.lang
        }
    }
    
    class Info {
        var transcriptInfo: TranscriptInfo
        var translateInfo: TranslateInfo
        
        init(transcriptRenderInfo: TranscriptInfo, translateRenderInfo: TranslateInfo) {
            self.transcriptInfo = transcriptRenderInfo
            self.translateInfo = translateRenderInfo
        }
        
        var allText: String {
            let isFinalTranscriptTag = transcriptInfo.words.isFinal ? "[F]" : "[NF]"
            let transscriptText = transcriptInfo.words.map({ $0.text }).joined() + isFinalTranscriptTag
            if translateInfo.words.isEmpty {
                return transscriptText
            }
            
            let isFinalTranslateTag = translateInfo.words.isFinal ? "[F]" : "[NF]"
            let translateText = translateInfo.words.map({ $0.text }).joined()
            let langTag = translateInfo.words.first!.lang
            return "\(transscriptText)\n[\(langTag)]\(translateText)\(isFinalTranslateTag)"
        }
    }
    
    class TranscriptInfo {
        var startMs: Int64 = 0
        var textTs: Int64 = 0
        var duration: Int32 = 0
        var words = [TranscriptWord]()
    }
    
    class TranslateInfo {
        var startMs: Int64 = 0
        var duration: Int32 = 0
        var words = [TranslateWord]()
    }
    
    class RenderInfo {
        let transcriptText: String
        let translateText: String
        let transcriptRanges: [SegmentRangeInfo]
        let translateRanges: [SegmentRangeInfo]
        let identifier: Int64
        
        init(transcriptText: String,
             translatetText: String,
             transcriptRanges: [SegmentRangeInfo],
             translateRanges: [SegmentRangeInfo],
             identifier: Int64) {
            self.transcriptText = transcriptText
            self.translateText = translatetText
            self.transcriptRanges = transcriptRanges
            self.translateRanges = translateRanges
            self.identifier = identifier
        }
    }
    
    struct SegmentRangeInfo {
        let range: NSRange
        let isFinal: Bool
    }
}

extension Array where Element == TranscriptSubtitleMachine.TranscriptWord {
    var allText: String {
        map({ $0.text }).joined()
    }
    var isFinal: Bool {
        last?.isFinal ?? false
    }
}

extension Array where Element == TranscriptSubtitleMachine.TranslateWord {
    var allText: String {
        map({ $0.text }).joined()
    }
    var isFinal: Bool {
        last?.isFinal ?? false
    }
}

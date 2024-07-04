//
//  Models.swift
//  AgoraTranscriptSubtitle
//
//  Created by ZYP on 2024/6/17.
//

import Foundation

typealias UidType = UInt
enum MessageType: UInt8 {
    case transcribe = 0
    case translate = 1
    
    init?(string: String) {
        switch string {
        case "transcribe":
            self.init(rawValue: 0)
        case "translate":
            self.init(rawValue: 1)
        default:
            return nil
        }
    }
}

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
    var translateInfos: [TranslateInfo]
    
    init(transcriptInfo: TranscriptInfo, translateInfos: [TranslateInfo]) {
        self.transcriptInfo = transcriptInfo
        self.translateInfos = translateInfos
    }
}

class TranscriptInfo {
    var startMs: Int64 = 0
    var textTs: Int64 = 0
    var duration: Int32 = 0
    var words = [TranscriptWord]()
    var sentenceEndIndex: Int32 = 0
}

class TranslateInfo {
    var startMs: Int64 = 0
    var words = [TranslateWord]()
}

class RenderInfo {
    let identifier: Int64
    let transcriptText: String
    let transcriptRanges: [SegmentRangeInfo]
    let translateRenderInfos: [TranslateRenderInfo]
    
    init(identifier: Int64,
        transcriptText: String,
         transcriptRanges: [SegmentRangeInfo],
         translateRenderInfos: [TranslateRenderInfo]) {
        self.identifier = identifier
        self.transcriptText = transcriptText
        self.transcriptRanges = transcriptRanges
        self.translateRenderInfos = translateRenderInfos
    }
}

struct SegmentRangeInfo {
    let range: NSRange
    let isFinal: Bool
}

struct TranslateRenderInfo {
    let lang: String
    let text: String
    let ranges: [SegmentRangeInfo]
}

extension Array where Element == TranscriptWord {
    var allText: String {
        map({ $0.text }).joined()
    }
    var isFinal: Bool {
        last?.isFinal ?? false
    }
}

extension Array where Element == TranslateWord {
    var allText: String {
        map({ $0.text }).joined()
    }
    var isFinal: Bool {
        last?.isFinal ?? false
    }
    
    var lang: String? {
        first?.lang
    }
}

@objc public class DebugParam: NSObject {
    /// dump message data
    let dump_input: Bool
    /// dump deserialized data
    let dump_deserialize: Bool
    /// use low level isFinal tag for paragraph distinction
    var useFinalTagAsParagraphDistinction: Bool
    /// show TranslateContent in view
    let showTranslateContent: Bool
    
    @objc public init(dump_input: Bool = true,
                      dump_deserialize: Bool = false,
                      useFinalTagAsParagraphDistinction: Bool = false,
                      showTranslateContent: Bool = true) {
        self.dump_input = dump_input
        self.dump_deserialize = dump_deserialize
        self.useFinalTagAsParagraphDistinction = useFinalTagAsParagraphDistinction
        self.showTranslateContent = showTranslateContent
    }
    
    public override var description: String {
        return debugDescription
    }
    
    public override var debugDescription: String {
        return "dump_input: \(dump_input), dump_deserialize: \(dump_deserialize), useFinalTagAsParagraphDistinction: \(useFinalTagAsParagraphDistinction), viewShouldShowTranslateContent: \(showTranslateContent)"
    }
}

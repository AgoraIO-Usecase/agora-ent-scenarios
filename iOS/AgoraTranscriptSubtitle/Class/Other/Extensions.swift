//
//  Extensions.swift
//  AgoraTranscriptSubtitle
//
//  Created by ZYP on 2024/6/17.
//

import Foundation

extension Bundle {
    static var currentBundle: Bundle {
        let bundle = Bundle(for: TranscriptSubtitleView.self)
        return bundle
    }
}

extension String {
    static var versionName: String {
        guard let version = Bundle.currentBundle.infoDictionary?["CFBundleShortVersionString"] as? String else {
            return "unknow version"
        }
        return version
    }
}

extension ProtobufDeserializer.DataStreamMessage {
    var dict: [String : Any] {
        return ["vendor" : vendor,
                "version" : version,
                "seqnum" : seqnum,
                "uid": uid,
                "flag" : flag,
                "time" : time,
                "lang" : lang,
                "starttime" : starttime,
                "offtime" : offtime,
                "wordsArray_Count" : wordsArray_Count,
                "endOfSegment" : endOfSegment,
                "durationMs" : durationMs,
                "dataType": dataType ?? "nil",
                "transArray_Count" : transArray_Count,
                "culture" : culture ?? "nil",
                "textTs" : textTs,
                "sentenceEndIndex" : sentenceEndIndex,
                "wordsArray" : words.map({ $0.dict }),
                "transArray" : trans.map({ $0.dict })
        ]
    }
    
    var debug_transcriptBeautyString: String {
        return """
        {"text" : \(words.first?.text ?? "nil"), "startMs" : \(words.first?.startMs ?? 0), "durationMs" : \(words.first?.durationMs ?? 0), "isFinal" : \(words.first?.isFinal ?? false), "sentenceEndIndex" : \(sentenceEndIndex)} wordsArray_Count:\(wordsArray_Count) textTs:\(textTs)
"""
    }
    
    var words: [SttWord] {
        return wordsArray as! [SttWord]
    }

    var trans: [SttTranslation] {
        return transArray as! [SttTranslation]
    }
    
    var jsonString: String {
        let data = try! JSONSerialization.data(withJSONObject: dict, options: .init(rawValue: 0))
        return String(data: data, encoding: .utf8) ?? "nil"
    }
}

extension SttWord {
    var dict: [String : Any] {
        let dict: [String : Any] = ["text" : text ?? "nil",
                                    "startMs" : startMs,
                                    "durationMs" : durationMs,
                                    "confidence" : confidence,
                                    "isFinal" : isFinal]
        return dict
    }
    var jsonString: String {
        let data = try! JSONSerialization.data(withJSONObject: dict, options: .init(rawValue: 0))
        return String(data: data, encoding: .utf8) ?? "nil"
    }
    
    var splitToTranscriptWords: [TranscriptWord] {
        return text!.map({ String($0) }).map({ TranscriptWord.init(text: $0, isFinal: isFinal, confidence: confidence, startMs: startMs, durationMs: durationMs) })
    }
}

extension SttTranslation {
    var dict: [String: Any] {
        return ["lang" : lang ?? "nil",
                "isFinal" : isFinal,
                "textsArray_Count" : textsArray_Count,
                "textsArray" : textsArray ?? []]
    }
    var jsonString: String {
        let data = try! JSONSerialization.data(withJSONObject: dict, options: .init(rawValue: 0))
        return String(data: data, encoding: .utf8) ?? "nil"
    }
}

extension Array where Element == SttWord {
    var isFinal: Bool {
        return last?.isFinal ?? false
    }
    
    var splitToTranscriptWords: [TranscriptWord] {
        map({ $0.splitToTranscriptWords }).flatMap({ $0 })
    }
}

extension SttText {
    var translateWords: [TranslateWord] {
        (transArray as! [SttTranslation]).map({ TranslateWord(sttTranslation: $0) })
    }
    
    var splitToTranscriptWords: [TranscriptWord] {
        words.splitToTranscriptWords
    }
}

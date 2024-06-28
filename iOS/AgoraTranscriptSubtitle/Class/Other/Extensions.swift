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
                "wordsArray" : words.map({ $0.dict }),
                "transArray" : trans.map({ $0.dict })
        ]
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

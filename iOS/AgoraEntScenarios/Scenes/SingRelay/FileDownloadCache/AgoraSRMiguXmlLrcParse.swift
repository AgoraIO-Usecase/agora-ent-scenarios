//
//  AgoraSRMusicLrcParse.swift
//  AgoraSRKaraokeScore
//
//  Created by zhaoyongqiang on 2021/12/10.
//

import UIKit

private class AgoraSRSong {
    var name: String = ""
    var singer: String = ""
    var type: SRMusicType = .fast
    var sentences: [AgoraSRMiguLrcSentence] = []
}

private enum AgoraSRParserType {
    case general
    case name
    case singer
    case type
    case sentence
    case tone
    case word
    case overlap
}

private class AgoraSRMiguSongLyricXmlParser: NSObject, XMLParserDelegate {
    let parser: XMLParser!
    var song: AgoraSRSong?
    private var parserTypes: [AgoraSRParserType] = []

    init?(lrcFile: String) {
        parser = XMLParser(contentsOf: URL(fileURLWithPath: lrcFile))
        super.init()
        if parser == nil {
            return nil
        } else {
            parser.delegate = self
            let success = parser.parse()
            if !success {
                let error = parser.parserError
                let line = parser.lineNumber
                let col = parser.columnNumber
                debugPrint("xml parsing Error(\(error?.localizedDescription ?? "")) at \(line):\(col)")
            }
            if success, let song = song {
                song.sentences.forEach { sentence in
                    sentence.processBlank()
                }
            }
        }
    }

    func parserDidStartDocument(_: XMLParser) {}

    func parserDidEndDocument(_: XMLParser) {}

    func parser(_: XMLParser, parseErrorOccurred parseError: Error) {
        debugPrint("message == \(parseError.localizedDescription)")
    }

    func parser(_: XMLParser, validationErrorOccurred validationError: Error) {
        debugPrint("message == \(validationError.localizedDescription)")
    }

    func parser(_: XMLParser, didStartElement elementName: String, namespaceURI _: String?, qualifiedName _: String?, attributes attributeDict: [String: String] = [:]) {
        switch elementName {
        case "song":
            song = AgoraSRSong()
        case "general":
            push(.general)
        case "name":
            push(.name)
        case "singer":
            push(.singer)
        case "type":
            push(.type)
        case "sentence":
            push(.sentence)
            song?.sentences.append(AgoraSRMiguLrcSentence(tones: []))
        case "tone":
            push(.tone)
            if let sentence = song?.sentences.last {
                let beginValue = Double(attributeDict["begin"] ?? "0") ?? 0
                let endValue = Double(attributeDict["end"] ?? "0") ?? 0
                let pitchValue = Float(attributeDict["pitch"] ?? "0") ?? 0
                let begin: TimeInterval = beginValue * 1000
                let end: TimeInterval = endValue * 1000
                let pitch = Int(pitchValue)
                let pronounce = attributeDict["pronounce"] ?? ""
                let lang = attributeDict["lang"]
                let tone = AgoraSRMiguLrcTone(begin: begin, end: end, pitch: pitch, pronounce: pronounce, lang: AgoraSRMiguLang.toLang(lang), word: "")
                sentence.tones.append(tone)
            }
        case "word":
            push(.word)
        case "overlap":
            push(.overlap)
            let beginValue = Double(attributeDict["begin"] ?? "0") ?? 0
            let endValue = Double(attributeDict["end"] ?? "0") ?? 0
            let begin: TimeInterval = beginValue * 1000
            let end: TimeInterval = endValue * 1000
            let pitch = 0
            let pronounce = ""
            let lang = attributeDict["lang"]
            let tone = AgoraSRMiguLrcTone(begin: begin, end: end, pitch: pitch, pronounce: pronounce, lang: AgoraSRMiguLang.toLang(lang), word: "")
            song?.sentences.append(AgoraSRMiguLrcSentence(tones: [tone]))
        default:
            break
        }
    }

    func parser(_: XMLParser, foundCharacters string: String) {
        if let last = parserTypes.last {
            switch last {
            case .name:
                song?.name = string
            case .singer:
                song?.singer = string
            case .type:
                song?.type = SRMusicType(rawValue: string) ?? .fast
            case .word, .overlap:
                if let tone = song?.sentences.last?.tones.last {
                    tone.word = tone.word + string
                    if tone.lang == .unknown {
                        do {
                            let regular = try NSRegularExpression(pattern: "[a-zA-Z]", options: .caseInsensitive)
                            let count = regular.numberOfMatches(in: tone.word, options: .anchored, range: NSRange(location: 0, length: tone.word.count))
                            if count > 0 {
                                tone.lang = .en
                            } else {
                                tone.lang = .zh
                            }
                        } catch {
                            tone.lang = .en
                        }
                    }
                }
            default:
                break
            }
        }
    }

    func parser(_: XMLParser, didEndElement elementName: String, namespaceURI _: String?, qualifiedName _: String?) {
        switch elementName {
        case "general":
            pop(equal: .general)
        case "name":
            pop(equal: .name)
        case "singer":
            pop(equal: .singer)
        case "type":
            pop(equal: .type)
        case "sentence":
            pop(equal: .sentence)
        case "tone":
            pop(equal: .tone)
        case "word":
            pop(equal: .word)
        case "overlap":
            pop(equal: .overlap)
        default:
            break
        }
    }

    private func current(type: AgoraSRParserType) -> Bool {
        return parserTypes.last == type
    }

    private func push(_ type: AgoraSRParserType) {
        parserTypes.append(type)
    }

    private func pop() {
        parserTypes.removeLast()
    }

    private func pop(equal: AgoraSRParserType) {
        if current(type: equal) {
            pop()
        }
    }
}

enum SRMusicType: String {
    /// 快歌
    case fast = "1"
    /// 慢歌
    case slow = "2"
}

@objc class AgoraSRMiguSongLyric: NSObject {
    let name: String
    let singer: String
    // 歌曲的类型(1.快歌，2.慢歌)
    let type: SRMusicType
    let sentences: [AgoraSRMiguLrcSentence]

    init?(lrcFile: String) {
        let parser = AgoraSRMiguSongLyricXmlParser(lrcFile: lrcFile)
        if let song = parser?.song {
            name = song.name
            singer = song.singer
            type = song.type
            sentences = song.sentences
        } else {
            return nil
        }
    }
}

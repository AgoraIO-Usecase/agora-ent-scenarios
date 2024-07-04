//
//  TestTranscriptSubtitleMachine.swift
//  AgoraTranscriptSubtitle-Unit-Tests
//
//  Created by ZYP on 2024/6/27.
//

import XCTest
@testable import AgoraTranscriptSubtitle

final class TestTranscriptSubtitleMachine: XCTestCase, TranscriptSubtitleMachineDelegate, DebugMachineIntermediateDelegate {
    
    let transcriptSubtitleMachine = TranscriptSubtitleMachine()
    let exp = XCTestExpectation()
    let exp2 = XCTestExpectation()
    let exp3 = XCTestExpectation()
    
    override func setUpWithError() throws {
        Log.setLoggers(loggers: [AGFileLogger(), ConsoleLogger()])
        transcriptSubtitleMachine.delegate = self
        transcriptSubtitleMachine.debug_intermediateDelegate = self
        transcriptSubtitleMachine.debugParam = .init(dump_input: true,
                                                     dump_deserialize: true,
                                                     useFinalTagAsParagraphDistinction: false,
                                                     showTranslateContent: false)
    }

    override func tearDownWithError() throws {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    func testExample() throws {
        DispatchQueue.global().async {
            let datas = DataStreamFileFetch.fetch(fileName: "dataStreamResults4.txt")
            for (_, data) in datas.enumerated() {
                self.transcriptSubtitleMachine.pushMessageData(data: data, uid: 0)
                Thread.sleep(forTimeInterval: 0.35)
            }
        }
        exp3.expectedFulfillmentCount = 4
        wait(for: [exp], timeout: 10)
    }
    
    func transcriptSubtitleMachine(_ machine: AgoraTranscriptSubtitle.TranscriptSubtitleMachine,
                                   didAddRenderInfo renderInfo: AgoraTranscriptSubtitle.RenderInfo) {
        exp2.fulfill()
    }
    
    func transcriptSubtitleMachine(_ machine: AgoraTranscriptSubtitle.TranscriptSubtitleMachine,
                                   didUpadteRenderInfo renderInfo: AgoraTranscriptSubtitle.RenderInfo) {
        exp3.fulfill()
    }
    var invokeCount = 0
    func debugMachineIntermediate(_ machine: AgoraTranscriptSubtitle.TranscriptSubtitleMachine, diduUpdate infos: [AgoraTranscriptSubtitle.Info]) {
        
        let startMs: Int64 = 1719371677859
        print("invokeCount:\(invokeCount)")
        if invokeCount == 1 {
            XCTAssertEqual(infos.count, 1)
            XCTAssertEqual(infos[0].transcriptInfo.words.allText, "你好吗你好吗")
            XCTAssertEqual(infos[0].transcriptInfo.words.isFinal, false)
            XCTAssertEqual(infos[0].transcriptInfo.sentenceEndIndex, -1)
        }
        
        if invokeCount == 2 {
            XCTAssertEqual(infos.count, 1)
            XCTAssertEqual(infos[0].transcriptInfo.words.allText, "你好吗你好吗你可以")
            XCTAssertEqual(infos[0].transcriptInfo.startMs, startMs)
            XCTAssertEqual(infos[0].transcriptInfo.textTs, 1719371678559)
            XCTAssertEqual(infos[0].transcriptInfo.words.isFinal, false)
            XCTAssertEqual(infos[0].transcriptInfo.sentenceEndIndex, -1)
        }
        
        if invokeCount == 4 {
            XCTAssertEqual(infos.count, 1)
            XCTAssertEqual(infos[0].transcriptInfo.words.allText, "你好吗？你好吗？你可以做什么？")
            XCTAssertEqual(infos[0].transcriptInfo.startMs, startMs)
            XCTAssertEqual(infos[0].transcriptInfo.textTs, 1719371679083)
            XCTAssertEqual(infos[0].transcriptInfo.words.isFinal, true)
            XCTAssertEqual(infos[0].transcriptInfo.sentenceEndIndex, 15)
        }
        
        if invokeCount == 5 {
            XCTAssertEqual(infos.count, 2)
            XCTAssertEqual(infos[0].transcriptInfo.words.allText, "你好吗？你好吗？你可以做什么？")
            XCTAssertEqual(infos[0].transcriptInfo.startMs, startMs)
            XCTAssertEqual(infos[0].transcriptInfo.textTs, 1719371679083)
            XCTAssertEqual(infos[0].transcriptInfo.words.isFinal, true)
            XCTAssertEqual(infos[0].transcriptInfo.sentenceEndIndex, 15)
            
            XCTAssertEqual(infos[1].transcriptInfo.words.allText, "我是")
            XCTAssertEqual(infos[1].transcriptInfo.startMs, 1719371680075)
            XCTAssertEqual(infos[1].transcriptInfo.textTs, 1719371680075)
            XCTAssertEqual(infos[1].transcriptInfo.words.isFinal, false)
            XCTAssertEqual(infos[1].transcriptInfo.sentenceEndIndex, -1)
        }
        
        if invokeCount == 6 {
            XCTAssertEqual(infos.count, 2)
            XCTAssertEqual(infos[0].transcriptInfo.words.allText, "你好吗？你好吗？你可以做什么？")
            XCTAssertEqual(infos[0].transcriptInfo.startMs, 1719371677859)
            XCTAssertEqual(infos[0].transcriptInfo.textTs, 1719371679083)
            XCTAssertEqual(infos[0].transcriptInfo.words.isFinal, true)
            XCTAssertEqual(infos[0].transcriptInfo.sentenceEndIndex, 15)

            XCTAssertEqual(infos[1].transcriptInfo.words.allText, "我是中国人")
            XCTAssertEqual(infos[1].transcriptInfo.startMs, 1719371680075)
            XCTAssertEqual(infos[1].transcriptInfo.textTs, 1719371680378)
            XCTAssertEqual(infos[1].transcriptInfo.words.isFinal, false)
            XCTAssertEqual(infos[1].transcriptInfo.sentenceEndIndex, -1)
        }
        
        if invokeCount == 9 {
            XCTAssertEqual(infos.count, 2)
            XCTAssertEqual(infos[0].transcriptInfo.words.allText, "你好吗？你好吗？你可以做什么？")
            XCTAssertEqual(infos[0].transcriptInfo.startMs, 1719371677859)
            XCTAssertEqual(infos[0].transcriptInfo.textTs, 1719371679083)
            XCTAssertEqual(infos[0].transcriptInfo.words.isFinal, true)
            XCTAssertEqual(infos[0].transcriptInfo.sentenceEndIndex, 15)

            XCTAssertEqual(infos[1].transcriptInfo.words.allText, "我是中国人，你是哪里人？")
            XCTAssertEqual(infos[1].transcriptInfo.startMs, 1719371680075)
            XCTAssertEqual(infos[1].transcriptInfo.textTs, 1719371681894)
            XCTAssertEqual(infos[1].transcriptInfo.words.isFinal, true)
            XCTAssertEqual(infos[1].transcriptInfo.sentenceEndIndex, -1)
        }
        
        if invokeCount == 10 {
            XCTAssertEqual(infos.count, 2)
            XCTAssertEqual(infos[0].transcriptInfo.words.allText, "你好吗？你好吗？你可以做什么？")
            XCTAssertEqual(infos[0].transcriptInfo.startMs, 1719371677859)
            XCTAssertEqual(infos[0].transcriptInfo.textTs, 1719371679083)
            XCTAssertEqual(infos[0].transcriptInfo.words.isFinal, true)
            XCTAssertEqual(infos[0].transcriptInfo.sentenceEndIndex, 15)

            XCTAssertEqual(infos[1].transcriptInfo.words.allText, "我是中国人，你是哪里人？你来自")
            XCTAssertEqual(infos[1].transcriptInfo.startMs, 1719371680075)
            XCTAssertEqual(infos[1].transcriptInfo.textTs, 1719371684124)
            XCTAssertEqual(infos[1].transcriptInfo.words.isFinal, false)
            XCTAssertEqual(infos[1].transcriptInfo.sentenceEndIndex, -1)
        }
        
        if invokeCount == 13 {
            XCTAssertEqual(infos.count, 2)
            XCTAssertEqual(infos[0].transcriptInfo.words.allText, "你好吗？你好吗？你可以做什么？")
            XCTAssertEqual(infos[0].transcriptInfo.startMs, 1719371677859)
            XCTAssertEqual(infos[0].transcriptInfo.textTs, 1719371679083)

            XCTAssertEqual(infos[1].transcriptInfo.words.allText, "我是中国人，你是哪里人？你来自哪里？我来自中国。")
            XCTAssertEqual(infos[1].transcriptInfo.startMs, 1719371680075)
            XCTAssertEqual(infos[1].transcriptInfo.textTs, 1719371685529)
            XCTAssertEqual(infos[1].transcriptInfo.sentenceEndIndex, -1)

            exp.fulfill()
        }
        
        invokeCount += 1
    }
    
}

//
//  TestTranscriptSubtitleMachine2.swift
//  AgoraTranscriptSubtitle-Unit-Tests
//
//  Created by ZYP on 2024/6/28.
//

import XCTest
@testable import AgoraTranscriptSubtitle

final class TestTranscriptSubtitleMachine2: XCTestCase, TranscriptSubtitleMachineDelegate, DebugMachineIntermediateDelegate {
    
    let transcriptSubtitleMachine = TranscriptSubtitleMachine()
    let exp = XCTestExpectation()
    let exp2 = XCTestExpectation()
    let exp3 = XCTestExpectation()
    
    override func setUpWithError() throws {
        Log.setLoggers(loggers: [ATFileLogger(), ConsoleLogger()])
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
            let datas = DataStreamFileFetch.fetch(fileName: "dataStreamResults6.txt")
            for (_, data) in datas.enumerated() {
                self.transcriptSubtitleMachine.pushMessageData(data: data, uid: 0)
                Thread.sleep(forTimeInterval: 0.35)
            }
        }
        exp3.expectedFulfillmentCount = 4
        wait(for: [exp], timeout: 10)
    }
    
    func transcriptSubtitleMachine(_ machine: AgoraTranscriptSubtitle.TranscriptSubtitleMachine,
                                   didAddRenderInfo renderInfo: RenderInfo) {
        exp2.fulfill()
    }
    
    func transcriptSubtitleMachine(_ machine: AgoraTranscriptSubtitle.TranscriptSubtitleMachine,
                                   didUpadteRenderInfo renderInfo: RenderInfo) {
        exp3.fulfill()
    }
    var invokeCount = 0
    func debugMachineIntermediate(_ machine: AgoraTranscriptSubtitle.TranscriptSubtitleMachine, diduUpdate infos: [AgoraTranscriptSubtitle.Info]) {
        
        var startMss: [Int64] = [1719568515609, 1719568519938]
        print("invokeCount:\(invokeCount)")
        if invokeCount == 0 {
            XCTAssertEqual(infos.count, 1)
            XCTAssertEqual(infos[0].transcriptInfo.words.allText, "你好吗你好")
            XCTAssertEqual(infos[0].transcriptInfo.words.isFinal, false)
            XCTAssertEqual(infos[0].transcriptInfo.sentenceEndIndex, -1)
            XCTAssertEqual(infos[0].transcriptInfo.startMs, startMss[0])
            XCTAssertEqual(infos[0].transcriptInfo.textTs, startMss[0])
        }
        
        if invokeCount == 1 {
            XCTAssertEqual(infos.count, 1)
            XCTAssertEqual(infos[0].transcriptInfo.words.allText, "你好吗你好吗")
            XCTAssertEqual(infos[0].transcriptInfo.startMs, startMss[0])
            XCTAssertEqual(infos[0].transcriptInfo.textTs, 1719568516003)
            XCTAssertEqual(infos[0].transcriptInfo.words.isFinal, false)
            XCTAssertEqual(infos[0].transcriptInfo.sentenceEndIndex, -1)
        }
        
        if invokeCount == 2 {
            XCTAssertEqual(infos.count, 1)
            XCTAssertEqual(infos[0].transcriptInfo.words.allText, "你好吗？你好吗？")
            XCTAssertEqual(infos[0].transcriptInfo.startMs, startMss[0])
            XCTAssertEqual(infos[0].transcriptInfo.textTs, 1719568516004)
            XCTAssertEqual(infos[0].transcriptInfo.words.isFinal, true)
            XCTAssertEqual(infos[0].transcriptInfo.sentenceEndIndex, 8)
        }
        
        
        if invokeCount == 3 {
            XCTAssertEqual(infos.count, 2)
            XCTAssertEqual(infos[0].transcriptInfo.words.allText, "你好吗？你好吗？")
            XCTAssertEqual(infos[0].transcriptInfo.startMs, startMss[0])
            XCTAssertEqual(infos[0].transcriptInfo.textTs, 1719568516004)
            XCTAssertEqual(infos[0].transcriptInfo.words.isFinal, true)
            XCTAssertEqual(infos[0].transcriptInfo.sentenceEndIndex, 8)
                
            XCTAssertEqual(infos[1].transcriptInfo.words.allText, "你可以")
            XCTAssertEqual(infos[1].transcriptInfo.startMs, startMss[1])
            XCTAssertEqual(infos[1].transcriptInfo.textTs, startMss[1])
            XCTAssertEqual(infos[1].transcriptInfo.words.isFinal, false)
            XCTAssertEqual(infos[1].transcriptInfo.sentenceEndIndex, -1)
        }
         
        if invokeCount == 4 {
            XCTAssertEqual(infos.count, 2)
            XCTAssertEqual(infos[0].transcriptInfo.words.allText, "你好吗？你好吗？")
            XCTAssertEqual(infos[0].transcriptInfo.startMs, startMss[0])
            XCTAssertEqual(infos[0].transcriptInfo.textTs, 1719568516004)
            XCTAssertEqual(infos[0].transcriptInfo.words.isFinal, true)
            XCTAssertEqual(infos[0].transcriptInfo.sentenceEndIndex, 8)
                
            XCTAssertEqual(infos[1].transcriptInfo.words.allText, "你可以做")
            XCTAssertEqual(infos[1].transcriptInfo.startMs, startMss[1])
            XCTAssertEqual(infos[1].transcriptInfo.textTs, 1719568520544)
            XCTAssertEqual(infos[1].transcriptInfo.words.isFinal, false)
            XCTAssertEqual(infos[1].transcriptInfo.sentenceEndIndex, -1)
        }
        
        if invokeCount == 5 {
            XCTAssertEqual(infos.count, 2)
            XCTAssertEqual(infos[0].transcriptInfo.words.allText, "你好吗？你好吗？")
            XCTAssertEqual(infos[0].transcriptInfo.startMs, startMss[0])
            XCTAssertEqual(infos[0].transcriptInfo.textTs, 1719568516004)
            XCTAssertEqual(infos[0].transcriptInfo.words.isFinal, true)
            XCTAssertEqual(infos[0].transcriptInfo.sentenceEndIndex, 8)
                
            XCTAssertEqual(infos[1].transcriptInfo.words.allText, "你可以做什么")
            XCTAssertEqual(infos[1].transcriptInfo.startMs, startMss[1])
            XCTAssertEqual(infos[1].transcriptInfo.textTs, 1719568520636)
            XCTAssertEqual(infos[1].transcriptInfo.words.isFinal, false)
            XCTAssertEqual(infos[1].transcriptInfo.sentenceEndIndex, -1)
        }
        
        if invokeCount == 6 {
            XCTAssertEqual(infos.count, 2)
            XCTAssertEqual(infos[0].transcriptInfo.words.allText, "你好吗？你好吗？")
            XCTAssertEqual(infos[0].transcriptInfo.startMs, startMss[0])
            XCTAssertEqual(infos[0].transcriptInfo.textTs, 1719568516004)
            XCTAssertEqual(infos[0].transcriptInfo.words.isFinal, true)
            XCTAssertEqual(infos[0].transcriptInfo.sentenceEndIndex, 8)
            
            XCTAssertEqual(infos[1].transcriptInfo.words.allText, "你可以做什么你是谁")
            XCTAssertEqual(infos[1].transcriptInfo.startMs, startMss[1])
            XCTAssertEqual(infos[1].transcriptInfo.textTs, 1719568520838)
            XCTAssertEqual(infos[1].transcriptInfo.words.isFinal, false)
            XCTAssertEqual(infos[1].transcriptInfo.sentenceEndIndex, -1)
        }
        
        if invokeCount == 7 {
            XCTAssertEqual(infos.count, 2)
            XCTAssertEqual(infos[0].transcriptInfo.words.allText, "你好吗？你好吗？")
            XCTAssertEqual(infos[0].transcriptInfo.startMs, startMss[0])
            XCTAssertEqual(infos[0].transcriptInfo.textTs, 1719568516004)
            XCTAssertEqual(infos[0].transcriptInfo.words.isFinal, true)
            XCTAssertEqual(infos[0].transcriptInfo.sentenceEndIndex, 8)
            
            XCTAssertEqual(infos[1].transcriptInfo.words.allText, "你可以做什么？你是谁？")
            XCTAssertEqual(infos[1].transcriptInfo.startMs, startMss[1])
            XCTAssertEqual(infos[1].transcriptInfo.textTs, 1719568521078)
            XCTAssertEqual(infos[1].transcriptInfo.words.isFinal, true)
            XCTAssertEqual(infos[1].transcriptInfo.sentenceEndIndex, 11)
        }
        
        if invokeCount == 7 {
            exp.fulfill()
        }
        
        invokeCount += 1
    }
    
}

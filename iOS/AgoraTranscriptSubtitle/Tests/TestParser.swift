//
//  TestParser.swift
//  AgoraTranscriptSubtitle-Unit-Tests
//
//  Created by ZYP on 2024/6/17.
//

import XCTest
@testable import AgoraTranscriptSubtitle

final class TestParser: XCTestCase {

    override func setUpWithError() throws {
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }

    override func tearDownWithError() throws {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    func testEmpty() throws {
        let deserializer = ProtobufDeserializer()
        let info = deserializer.deserialize(data: Data())
        XCTAssertNil(info)
    }
    
    func testInvalid() throws {
        let data = Data(repeating: 0, count: 100)
        let deserializer = ProtobufDeserializer()
        let info = deserializer.deserialize(data: data)
        XCTAssertNil(info)
    }
    
    func testNormal() throws {
        let datas = DataStreamFileFetch.fetch(fileName: "dataStreamResults.txt")
        let deserializer = ProtobufDeserializer()
        var info = deserializer.deserialize(data: datas[0])!
        

        
        /*{
            "textTs": 1718764743753,
            "uid": 187469,
            "vendor": 0,
            "offtime": 0,
            "durationMs": 0,
            "time": 0,
            "seqnum": 0,
            "wordsArray": [
                {
                    "startMs": 777823957,
                    "text": "再找一下",
                    "durationMs": 680,
                    "confidence": 0,
                    "isFinal": false
                }
            ],
            "transArray_Count": 0,
            "lang": 0,
            "starttime": 0,
            "dataType": "transcribe",
            "culture": "zh-CN",
            "wordsArray_Count": 1,
            "endOfSegment": false,
            "flag": 0,
            "version": 0,
            "transArray": [

            ]
        }*/
        XCTAssertNotNil(info)
        XCTAssertEqual(info.textTs, 1718764743753)
        XCTAssertEqual(info.dataType, "transcribe")
        XCTAssertEqual(info.wordsArray_Count, 1)
        XCTAssertEqual(info.words.first!.text, "再找一下")
        XCTAssertEqual(info.words.first!.isFinal, false)
        XCTAssertEqual(info.words.first!.startMs, 777823957)
        XCTAssertEqual(info.words.first!.durationMs, 680)
        XCTAssertEqual(info.words.first!.confidence, 0)
        
        /**
         {
             "transArray": [
                 {
                     "textsArray_Count": 1,
                     "lang": "en-US",
                     "textsArray": [
                         "Look again"
                     ],
                     "isFinal": false
                 }
             ],
             "endOfSegment": false,
             "wordsArray_Count": 0,
             "durationMs": 0,
             "culture": "",
             "seqnum": 0,
             "transArray_Count": 1,
             "version": 0,
             "vendor": 0,
             "flag": 0,
             "offtime": 0,
             "textTs": 1718764743753,
             "wordsArray": [

             ],
             "time": 0,
             "lang": 0,
             "starttime": 0,
             "dataType": "translate",
             "uid": 187469
         }
         */
        info = deserializer.deserialize(data: datas[1])!
        XCTAssertNotNil(info)
        XCTAssertEqual(info.textTs, 1718764743753)
        XCTAssertEqual(info.dataType, "translate")
        XCTAssertEqual(info.transArray_Count, 1)
        XCTAssertEqual(info.trans.first!.lang, "en-US")
        XCTAssertEqual(info.trans.first!.textsArray_Count, 1)
        XCTAssertEqual(info.trans.first!.isFinal, false)
        XCTAssertEqual(info.trans.first!.textsArray.firstObject as! String, "Look again")
        
        
        /**
         {
             "culture": "zh-CN",
             "endOfSegment": false,
             "durationMs": 0,
             "wordsArray": [
                 {
                     "startMs": 777833967,
                     "text": "你",
                     "confidence": 0,
                     "isFinal": false,
                     "durationMs": 600
                 }
             ],
             "starttime": 0,
             "version": 0,
             "dataType": "transcribe",
             "transArray": [

             ],
             "lang": 0,
             "offtime": 0,
             "time": 0,
             "vendor": 0,
             "flag": 0,
             "seqnum": 0,
             "wordsArray_Count": 1,
             "transArray_Count": 0,
             "uid": 187469,
             "textTs": 1718764756951
         }
         **/
        info = deserializer.deserialize(data: datas[2])!
        XCTAssertNotNil(info)
        XCTAssertEqual(info.textTs, 1718764756951)
        XCTAssertEqual(info.dataType, "transcribe")
        XCTAssertEqual(info.wordsArray_Count, 1)
        XCTAssertEqual(info.words.first!.text, "你")
        XCTAssertEqual(info.words.first!.isFinal, false)
        XCTAssertEqual(info.words.first!.startMs, 777833967)
        XCTAssertEqual(info.words.first!.durationMs, 600)
        XCTAssertEqual(info.words.first!.confidence, 0)
        
        /**
         {
             "seqnum": 0,
             "flag": 0,
             "uid": 187469,
             "transArray_Count": 0,
             "dataType": "transcribe",
             "culture": "zh-CN",
             "vendor": 0,
             "starttime": 0,
             "transArray": [

             ],
             "offtime": 0,
             "endOfSegment": false,
             "textTs": 1718764757052,
             "durationMs": 0,
             "wordsArray_Count": 1,
             "lang": 0,
             "version": 0,
             "wordsArray": [
                 {
                     "durationMs": 840,
                     "confidence": 0,
                     "isFinal": false,
                     "text": "你好吗你好",
                     "startMs": 777833967
                 }
             ],
             "time": 0
         }
         */
        info = deserializer.deserialize(data: datas[3])!
        XCTAssertNotNil(info)
        XCTAssertEqual(info.textTs, 1718764757052)
        XCTAssertEqual(info.dataType, "transcribe")
        XCTAssertEqual(info.wordsArray_Count, 1)
        XCTAssertEqual(info.words.first!.text, "你好吗你好")
        XCTAssertEqual(info.words.first!.isFinal, false)
        XCTAssertEqual(info.words.first!.startMs, 777833967)
        XCTAssertEqual(info.words.first!.durationMs, 840)
        XCTAssertEqual(info.words.first!.confidence, 0)
        
        /**
         {
             "wordsArray": [
                 {
                     "durationMs": 1120,
                     "isFinal": false,
                     "confidence": 0,
                     "text": "你好吗你好吗",
                     "startMs": 777833967
                 }
             ],
             "transArray": [

             ],
             "version": 0,
             "lang": 0,
             "endOfSegment": false,
             "vendor": 0,
             "starttime": 0,
             "wordsArray_Count": 1,
             "dataType": "transcribe",
             "textTs": 1718764757254,
             "seqnum": 0,
             "flag": 0,
             "transArray_Count": 0,
             "time": 0,
             "offtime": 0,
             "durationMs": 0,
             "uid": 187469,
             "culture": "zh-CN"
         }
         **/
        info = deserializer.deserialize(data: datas[4])!
        XCTAssertNotNil(info)
        XCTAssertEqual(info.textTs, 1718764757254)
        XCTAssertEqual(info.dataType, "transcribe")
        XCTAssertEqual(info.wordsArray_Count, 1)
        XCTAssertEqual(info.words.first!.text, "你好吗你好吗")
        XCTAssertEqual(info.words.first!.isFinal, false)
        XCTAssertEqual(info.words.first!.startMs, 777833967)
        XCTAssertEqual(info.words.first!.durationMs, 1120)
        XCTAssertEqual(info.words.first!.confidence, 0)
        
        /**
         {
             "endOfSegment": false,
             "culture": "zh-CN",
             "wordsArray_Count": 1,
             "starttime": 0,
             "dataType": "transcribe",
             "flag": 0,
             "transArray_Count": 0,
             "time": 0,
             "uid": 187469,
             "offtime": 0,
             "lang": 0,
             "durationMs": 0,
             "textTs": 1718764757824,
             "transArray": [

             ],
             "version": 0,
             "vendor": 0,
             "wordsArray": [
                 {
                     "confidence": 0,
                     "startMs": 777833967,
                     "isFinal": true,
                     "text": "你好吗？你好吗？",
                     "durationMs": 1120
                 }
             ],
             "seqnum": 0
         }
         **/
        info = deserializer.deserialize(data: datas[5])!
        XCTAssertNotNil(info)
        XCTAssertEqual(info.textTs, 1718764757824)
        XCTAssertEqual(info.dataType, "transcribe")
        XCTAssertEqual(info.wordsArray_Count, 1)
        XCTAssertEqual(info.words.first!.text, "你好吗？你好吗？")
        XCTAssertEqual(info.words.first!.isFinal, true)
        XCTAssertEqual(info.words.first!.startMs, 777833967)
        XCTAssertEqual(info.words.first!.durationMs, 1120)
        XCTAssertEqual(info.words.first!.confidence, 0)
        
        
        /**
         {
             "endOfSegment": false,
             "time": 0,
             "wordsArray_Count": 0,
             "uid": 187469,
             "version": 0,
             "starttime": 0,
             "lang": 0,
             "culture": "",
             "textTs": 1718764756951,
             "vendor": 0,
             "wordsArray": [

             ],
             "seqnum": 0,
             "durationMs": 0,
             "dataType": "translate",
             "transArray_Count": 1,
             "flag": 0,
             "transArray": [
                 {
                     "isFinal": false,
                     "textsArray": [
                         "You"
                     ],
                     "lang": "en-US",
                     "textsArray_Count": 1
                 }
             ],
             "offtime": 0
         }
         **/
        info = deserializer.deserialize(data: datas[6])!
        XCTAssertNotNil(info)
        XCTAssertEqual(info.textTs, 1718764756951)
        XCTAssertEqual(info.dataType, "translate")
        XCTAssertEqual(info.transArray_Count, 1)
        XCTAssertEqual(info.trans.first!.lang, "en-US")
        XCTAssertEqual(info.trans.first!.textsArray_Count, 1)
        XCTAssertEqual(info.trans.first!.isFinal, false)
        XCTAssertEqual(info.trans.first!.textsArray.firstObject as! String, "You")
        
        /**
         {
             "offtime": 0,
             "endOfSegment": false,
             "time": 0,
             "uid": 187469,
             "transArray_Count": 1,
             "seqnum": 0,
             "durationMs": 0,
             "wordsArray": [

             ],
             "culture": "",
             "transArray": [
                 {
                     "textsArray_Count": 1,
                     "isFinal": true,
                     "textsArray": [
                         "How are you? How are you? "
                     ],
                     "lang": "en-US"
                 }
             ],
             "vendor": 0,
             "wordsArray_Count": 0,
             "flag": 0,
             "starttime": 0,
             "textTs": 1718764757824,
             "dataType": "translate",
             "lang": 0,
             "version": 0
         }
         **/
        info = deserializer.deserialize(data: datas[7])!
        XCTAssertNotNil(info)
        XCTAssertEqual(info.textTs, 1718764757824)
        XCTAssertEqual(info.dataType, "translate")
        XCTAssertEqual(info.transArray_Count, 1)
        XCTAssertEqual(info.trans.first!.lang, "en-US")
        XCTAssertEqual(info.trans.first!.textsArray_Count, 1)
        XCTAssertEqual(info.trans.first!.isFinal, true)
        XCTAssertEqual(info.trans.first!.textsArray.firstObject as! String, "How are you? How are you? ")
        
        /**
         {
             "starttime": 0,
             "culture": "zh-CN",
             "durationMs": 0,
             "dataType": "transcribe",
             "seqnum": 0,
             "flag": 0,
             "wordsArray_Count": 1,
             "uid": 187469,
             "offtime": 0,
             "wordsArray": [
                 {
                     "startMs": 777835957,
                     "confidence": 0,
                     "isFinal": false,
                     "durationMs": 880,
                     "text": "你可以做什么"
                 }
             ],
             "vendor": 0,
             "textTs": 1718764768398,
             "transArray": [

             ],
             "version": 0,
             "lang": 0,
             "transArray_Count": 0,
             "endOfSegment": false,
             "time": 0
         }
         **/
        info = deserializer.deserialize(data: datas[8])!
        XCTAssertNotNil(info)
        XCTAssertEqual(info.textTs, 1718764768398)
        XCTAssertEqual(info.dataType, "transcribe")
        XCTAssertEqual(info.wordsArray_Count, 1)
        XCTAssertEqual(info.words.first!.text, "你可以做什么")
        XCTAssertEqual(info.words.first!.isFinal, false)
        XCTAssertEqual(info.words.first!.startMs, 777835957)
        XCTAssertEqual(info.words.first!.durationMs, 880)
        XCTAssertEqual(info.words.first!.confidence, 0)
        
        /**
         {
             "vendor": 0,
             "lang": 0,
             "seqnum": 0,
             "transArray": [

             ],
             "culture": "zh-CN",
             "time": 0,
             "version": 0,
             "dataType": "transcribe",
             "uid": 187469,
             "textTs": 1718764768949,
             "durationMs": 0,
             "endOfSegment": false,
             "flag": 0,
             "starttime": 0,
             "wordsArray_Count": 1,
             "offtime": 0,
             "transArray_Count": 0,
             "wordsArray": [
                 {
                     "startMs": 777835957,
                     "confidence": 0,
                     "text": "你可以做什么？",
                     "durationMs": 880,
                     "isFinal": true
                 }
             ]
         }
         **/
        info = deserializer.deserialize(data: datas[9])!
        XCTAssertNotNil(info)
        XCTAssertEqual(info.textTs, 1718764768949)
        XCTAssertEqual(info.dataType, "transcribe")
        XCTAssertEqual(info.wordsArray_Count, 1)
        XCTAssertEqual(info.words.first!.text, "你可以做什么？")
        XCTAssertEqual(info.words.first!.isFinal, true)
        XCTAssertEqual(info.words.first!.startMs, 777835957)
        XCTAssertEqual(info.words.first!.durationMs, 880)
        XCTAssertEqual(info.words.first!.confidence, 0)
        
        /**
         {
             "dataType": "translate",
             "culture": "",
             "transArray": [
                 {
                     "lang": "en-US",
                     "isFinal": false,
                     "textsArray_Count": 1,
                     "textsArray": [
                         "What can you do"
                     ]
                 }
             ],
             "textTs": 1718764768398,
             "version": 0,
             "endOfSegment": false,
             "time": 0,
             "durationMs": 0,
             "vendor": 0,
             "transArray_Count": 1,
             "wordsArray": [

             ],
             "seqnum": 0,
             "wordsArray_Count": 0,
             "offtime": 0,
             "flag": 0,
             "lang": 0,
             "uid": 187469,
             "starttime": 0
         }
         **/
        info = deserializer.deserialize(data: datas[10])!
        XCTAssertNotNil(info)
        XCTAssertEqual(info.textTs, 1718764768398)
        XCTAssertEqual(info.dataType, "translate")
        XCTAssertEqual(info.transArray_Count, 1)
        XCTAssertEqual(info.trans.first!.lang, "en-US")
        XCTAssertEqual(info.trans.first!.textsArray_Count, 1)
        XCTAssertEqual(info.trans.first!.isFinal, false)
        XCTAssertEqual(info.trans.first!.textsArray.firstObject as! String, "What can you do")
        
        /**
         {
             "uid": 187469,
             "durationMs": 0,
             "time": 0,
             "starttime": 0,
             "wordsArray_Count": 0,
             "flag": 0,
             "offtime": 0,
             "seqnum": 0,
             "endOfSegment": false,
             "dataType": "translate",
             "lang": 0,
             "version": 0,
             "wordsArray": [

             ],
             "vendor": 0,
             "transArray_Count": 1,
             "transArray": [
                 {
                     "textsArray": [
                         "What can you do? "
                     ],
                     "lang": "en-US",
                     "textsArray_Count": 1,
                     "isFinal": true
                 }
             ],
             "textTs": 1718764768949,
             "culture": ""
         }
         **/
        info = deserializer.deserialize(data: datas[11])!
        XCTAssertNotNil(info)
        XCTAssertEqual(info.textTs, 1718764768949)
        XCTAssertEqual(info.dataType, "translate")
        XCTAssertEqual(info.transArray_Count, 1)
        XCTAssertEqual(info.trans.first!.lang, "en-US")
        XCTAssertEqual(info.trans.first!.textsArray_Count, 1)
        XCTAssertEqual(info.trans.first!.isFinal, true)
        XCTAssertEqual(info.trans.first!.textsArray.firstObject as! String, "What can you do? ")
        
        /**
         {
             "vendor": 0,
             "offtime": 0,
             "wordsArray": [
                 {
                     "durationMs": 560,
                     "isFinal": false,
                     "text": "你是谁",
                     "confidence": 0,
                     "startMs": 777837637
                 }
             ],
             "version": 0,
             "endOfSegment": false,
             "dataType": "transcribe",
             "time": 0,
             "seqnum": 0,
             "culture": "zh-CN",
             "uid": 187469,
             "wordsArray_Count": 1,
             "transArray_Count": 0,
             "transArray": [

             ],
             "flag": 0,
             "lang": 0,
             "textTs": 1718764772826,
             "starttime": 0,
             "durationMs": 0
         }
         **/
        info = deserializer.deserialize(data: datas[12])!
        XCTAssertNotNil(info)
        XCTAssertEqual(info.textTs, 1718764772826)
        XCTAssertEqual(info.dataType, "transcribe")
        XCTAssertEqual(info.wordsArray_Count, 1)
        XCTAssertEqual(info.words.first!.text, "你是谁")
        XCTAssertEqual(info.words.first!.isFinal, false)
        XCTAssertEqual(info.words.first!.startMs, 777837637)
        XCTAssertEqual(info.words.first!.durationMs, 560)
        XCTAssertEqual(info.words.first!.confidence, 0)
    }

}

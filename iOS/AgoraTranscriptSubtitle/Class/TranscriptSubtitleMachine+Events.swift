//
//  TranscriptSubtitleMachine+Events.swift
//  AgoraTranscriptSubtitle
//
//  Created by ZYP on 2024/6/24.
//

import Foundation

protocol TranscriptSubtitleMachineDelegate: NSObjectProtocol {
    func transcriptSubtitleMachine(_ machine: TranscriptSubtitleMachine, didAddRenderInfo renderInfo: TranscriptSubtitleMachine.RenderInfo)
    func transcriptSubtitleMachine(_ machine: TranscriptSubtitleMachine, didUpadteRenderInfo renderInfo: TranscriptSubtitleMachine.RenderInfo)
}

//
//  TranscriptSubtitleMachine+Invoke.swift
//  AgoraTranscriptSubtitle
//
//  Created by ZYP on 2024/6/24.
//

import Foundation

extension TranscriptSubtitleMachine {
    func invokeAdd(_ machine: TranscriptSubtitleMachine, renderInfo: RenderInfo) {
        DispatchQueue.main.async { [weak self] in
            self?.delegate?.transcriptSubtitleMachine(machine, didAddRenderInfo: renderInfo)
        }
    }
    
    func invokeUpdate(_ machine: TranscriptSubtitleMachine, renderInfo: RenderInfo) {
        DispatchQueue.main.async { [weak self] in
            self?.delegate?.transcriptSubtitleMachine(machine, didUpadteRenderInfo: renderInfo)
        }
    }
}
 

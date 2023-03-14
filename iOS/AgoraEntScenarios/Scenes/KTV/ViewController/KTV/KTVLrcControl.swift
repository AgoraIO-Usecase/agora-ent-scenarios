//
//  KTVLrcControl.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/3/14.
//

import Foundation
import AgoraLyricsScore

class KTVLrcControl: NSObject {
    public var lrcView: KaraokeView?
    private var lyricModel: LyricModel?
}


extension KTVLrcControl: KTVLrcViewDelegate {
    func onUpdatePitch(pitch: Float) {
        lrcView?.setPitch(pitch: Double(pitch))
    }
    
    func onUpdateProgress(progress: Int) {
        lrcView?.setProgress(progress: progress)
    }
    
    func onDownloadLrcData(url: String) {
        let musicUrl: URL = URL(fileURLWithPath: url)

        guard let data = try? Data(contentsOf: musicUrl) else {return}
        guard let model: LyricModel = KaraokeView.parseLyricData(data: data) else {return}
        self.lyricModel = model

        lrcView?.setLyricData(data: model)
    }
    
    
}

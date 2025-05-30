//
//  SubtitleView.swift
//  Pure1v1
//
//  Created by CYG on 2024/6/27.
//

import UIKit
import AgoraTranscriptSubtitle

class SubtitleView: UIView {
    let rttView = TranscriptSubtitleView(frame: .zero)
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        backgroundColor = .clear
        rttView.debugParam = DebugParam(dump_input: false, dump_deserialize: true)
        rttView.backgroundColor = .clear
        rttView.finalTextColor = .white
        addSubview(rttView)
        rttView.translatesAutoresizingMaskIntoConstraints = false
        rttView.leftAnchor.constraint(equalTo: leftAnchor, constant: 15).isActive = true
        rttView.rightAnchor.constraint(equalTo: rightAnchor, constant: -15).isActive = true
        rttView.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -5).isActive = true
        rttView.heightAnchor.constraint(equalToConstant: 235).isActive = true
        
        
    }

}

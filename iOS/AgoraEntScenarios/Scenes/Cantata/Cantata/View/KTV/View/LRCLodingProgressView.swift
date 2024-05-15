//
//  LRCLodingProgressView.swift
//
//  Created by CP on 2023/4/20.
//

import Foundation

public class LRCLodingProgressView: UIView {
    
    public func setProgress(_ progress: Int) {
        percentLabel.text = "\(progress)%\("ktv_lrc_loading".toSceneLocalization() as String)"
        progressBar.progress = CGFloat(progress) / 100.0
    }

    private var percentLabel: UILabel = UILabel()
    private var loadingView: UIView = UIView()
    private var progressBar: GradientProgressBar!
    public override init(frame: CGRect) {
        super.init(frame: frame)
        layoutUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func layoutUI() {

        loadingView.frame = CGRect(x: bounds.width / 2.0 - 80, y: (bounds.height) / 2.0 - 30, width: 160, height: 60)
        loadingView.backgroundColor = .black
        loadingView.layer.cornerRadius = 10
        loadingView.layer.masksToBounds = true
        addSubview(loadingView)
        
        percentLabel.frame = CGRect(x: 10, y: 20, width: loadingView.bounds.width - 20, height: 20)
        percentLabel.textColor = .white
        percentLabel.text = "0%"
        percentLabel.font = UIFont.systemFont(ofSize: 13)
        percentLabel.textAlignment = .center
        loadingView.addSubview(percentLabel)
        
        progressBar = GradientProgressBar(frame: CGRect(x: 0, y:loadingView.bounds.height - 10, width: loadingView.bounds.width, height: 10))
        loadingView.addSubview(progressBar)
    }
}

//
//  LRCLodingProgressView.swift
//
//  Created by CP on 2023/4/20.
//

import Foundation

public class ShowDownlodingProgressView: UIView {
    
    public func setProgress(_ title: String, _ progress: Int) {
        let loadingStr = "show_beauty_resource_downloading".show_localized
        percentLabel.text = "\(title)\(loadingStr) \(progress)%"
        progressBar.progress = CGFloat(progress) / 100.0
    }

    private var percentLabel: UILabel = UILabel()
    private var loadingView: UIView = UIView()
    private var progressBar: ShowGradientProgressBar!
    public override init(frame: CGRect) {
        super.init(frame: frame)
        layoutUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func layoutUI() {

        loadingView.frame = bounds
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
        
        progressBar = ShowGradientProgressBar(frame: CGRect(x: 0, y:loadingView.bounds.height - 10, width: loadingView.bounds.width, height: 10))
        loadingView.addSubview(progressBar)
    }
}

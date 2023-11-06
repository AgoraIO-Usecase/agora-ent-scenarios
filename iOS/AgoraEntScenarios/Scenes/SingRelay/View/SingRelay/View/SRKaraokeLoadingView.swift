//
//  AUIKaraokeLoadingView.swift
//  AUIKit
//
//  Created by CP on 2023/4/20.
//

import Foundation

class SRKaraokeLoadingView: UIView {
    
    public func setProgress(_ progress: Int) {
        percentLabel.text = "\(progress)%"
    }
    
    private var titleLabel: UILabel = UILabel()
    private var indicatorView: UIActivityIndicatorView = UIActivityIndicatorView()
    private var percentLabel: UILabel = UILabel()
    private var loadingView: UIView = UIView()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        layoutUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func layoutUI() {
        
        loadingView.frame = CGRect(x: bounds.width / 2.0 - 80, y: (bounds.height) / 2.0 - 30, width: 160, height: 60)
        loadingView.layer.cornerRadius = 30
        loadingView.layer.masksToBounds = true
        addSubview(loadingView)
        
        indicatorView.frame = CGRect(x: 25, y: 8, width: 24, height: 24)
        indicatorView.startAnimating()
        loadingView.addSubview(indicatorView)
        
        titleLabel.frame = CGRect(x: 50, y: 10, width: 87, height: 20)
        titleLabel.textColor = .white
        titleLabel.text = "歌曲准备中..."
        titleLabel.font = UIFont.systemFont(ofSize: 14)
        loadingView.addSubview(titleLabel)
        
        percentLabel.frame = CGRect(x: 10, y: 36, width: loadingView.bounds.width - 20, height: 20)
        percentLabel.textColor = .white
        percentLabel.text = "0%"
        percentLabel.font = UIFont.systemFont(ofSize: 14)
        percentLabel.textAlignment = .center
        loadingView.addSubview(percentLabel)
    }
}

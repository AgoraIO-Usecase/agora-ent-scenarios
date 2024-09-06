//
//  ProgressBar.swift
//  AgoraEntScenarios
//
//  Created by CP on 2024/1/30.
//

import Foundation

class GradientProgressBar: UIView {
    private let gradientLayer = CAGradientLayer()
    private let progressView = UIView()

    @objc public var progress: CGFloat = 0.5 {
        didSet {
            updateProgress()
        }
    }

    override init(frame: CGRect) {
        super.init(frame: frame)
        setupGradientLayer()
        setupProgressView()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setupGradientLayer()
        setupProgressView()
    }

    private func setupGradientLayer() {
        gradientLayer.frame = CGRect(x: 0, y: 0, width: 0, height: bounds.height)
        gradientLayer.colors = [UIColor(red: 153/255, green: 245/255, blue: 255/255, alpha: 1.0).cgColor, UIColor(red: 27/255, green: 111/255, blue: 255/255, alpha: 1.0).cgColor]
        gradientLayer.startPoint = CGPoint(x: 0, y: 0.5)
        gradientLayer.endPoint = CGPoint(x: 1, y: 0.5)
        layer.addSublayer(gradientLayer)
    }

    private func setupProgressView() {
        progressView.backgroundColor = UIColor.clear
        addSubview(progressView)
        updateProgress()
    }

    private func updateProgress() {
//        print("pro:\(progress)--\(bounds.width * progress)")
        progressView.frame = CGRect(x: 0, y: 0, width: bounds.width * progress, height: bounds.height)
        gradientLayer.frame = CGRect(x: 0, y: 0, width: bounds.width * progress, height: bounds.height)
    }
}

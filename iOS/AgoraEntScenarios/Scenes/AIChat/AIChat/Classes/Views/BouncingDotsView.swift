//
//  BouncingDotsView.swift
//  AIChat
//
//  Created by 朱继超 on 2024/9/2.
//

import UIKit

class BouncingDotsView: UIView {
    private let dotSize: CGFloat = 8.0
    private let dotSpacing: CGFloat = 8.0
    private var dots: [UIView] = []

    override init(frame: CGRect) {
        super.init(frame: frame)
        setupDots()
        startAnimating()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setupDots()
    }

    private func setupDots() {
        for i in 0..<3 {
            let dot = UIView()
            dot.frame = CGRect(x: CGFloat(i) * (dotSize + dotSpacing), y: 0, width: dotSize, height: dotSize)
            dot.layer.cornerRadius = dotSize / 2
            dot.backgroundColor = UIColor(0x979cbb)
            dots.append(dot)
            addSubview(dot)
        }
    }

    func startAnimating() {
        for (index, dot) in dots.enumerated() {
            let delay = Double(index) * 0.2
            animateDot(dot, delay: delay)
        }
    }
    
    func stopAnimating() {
        for dot in dots {
            dot.layer.removeAllAnimations()
        }
    }

    private func animateDot(_ dot: UIView, delay: Double) {
        let bounceAnimation = CABasicAnimation(keyPath: "position.y")
        bounceAnimation.fromValue = dot.layer.position.y
        bounceAnimation.toValue = dot.layer.position.y - 30
        bounceAnimation.duration = 0.5
        bounceAnimation.autoreverses = true
        bounceAnimation.repeatCount = .infinity
        bounceAnimation.beginTime = CACurrentMediaTime() + delay
        dot.layer.add(bounceAnimation, forKey: "bounce")

        let colorAnimation = CABasicAnimation(keyPath: "backgroundColor")
        colorAnimation.fromValue = UIColor(0x787ea6)
        colorAnimation.toValue = UIColor(0xb5b9ce)
        colorAnimation.duration = 0.5
        colorAnimation.autoreverses = true
        colorAnimation.repeatCount = .infinity
        colorAnimation.beginTime = CACurrentMediaTime() + delay
        dot.layer.add(colorAnimation, forKey: "colorChange")
    }
}

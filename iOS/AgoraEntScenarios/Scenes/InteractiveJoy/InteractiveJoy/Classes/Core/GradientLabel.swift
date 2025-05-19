//
//  GradientLabel.swift
//  Joy
//
//  Created by wushengtao on 2023/12/1.
//

import UIKit

class GradientLabel: UILabel {
    private var gradientColors: [UIColor] = []
    
    override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
    }
    
    func setGradientBackground(colors: [UIColor]) {
        gradientColors = colors
        setNeedsDisplay()
    }
    
    override func draw(_ rect: CGRect) {
        guard let context = UIGraphicsGetCurrentContext(), gradientColors.count > 1 else {
            super.draw(rect)
            return
        }
        
        let colorSpace = CGColorSpaceCreateDeviceRGB()
        let colorLocations: [CGFloat] = [0.0, 1.0]
        
        if let gradient = CGGradient(colorsSpace: colorSpace, colors: gradientColors.map { $0.cgColor } as CFArray, locations: colorLocations) {
            let startPoint = CGPoint(x: bounds.minX, y: bounds.midY)
            let endPoint = CGPoint(x: bounds.maxX, y: bounds.midY)
            
            context.saveGState()
            context.addRect(rect)
            context.clip()
            
            context.drawLinearGradient(gradient, start: startPoint, end: endPoint, options: [])
            
            context.restoreGState()
        }

        super.draw(rect)
    }
}

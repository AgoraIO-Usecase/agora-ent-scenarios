//
//  AgoraMicVolView.swift
//  VoiceChat4Swift
//
//  Created by CP on 2022/8/29.
//

import UIKit
import SnapKit
class AgoraMicVolView: UIView {

    public enum AgoraMicVolViewState {
        case on, off, forbidden
    }
    
    private var imageView: UIImageView!
    
   // private var animaView: UIImageView!
    
    private var animateView: UIView!
    
    private var progressLayer: CAShapeLayer!
    
    private var micState: AgoraMicVolViewState = .off
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        layoutUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        
        self.progressLayer.frame = bounds
        let path = UIBezierPath.init()
        path.move(to: CGPoint(x: bounds.midX, y: bounds.maxY))
        path.addLine(to: CGPoint(x: bounds.midX, y: bounds.minY))
        self.progressLayer.lineWidth = bounds.width
        self.progressLayer.path = path.cgPath
    }
        
    public func setVolume(_ value: Int) {
        guard micState == .on else {
            return
        }
        let floatValue = min(CGFloat(value), 200.00)
        self.progressLayer.strokeEnd = floatValue / 200.0
    }
    
    public func setState(_ state: AgoraMicVolViewState) {
        guard micState != state else {
            return
        }
        self.micState = state
        switch state {
        case .on:
            self.imageView.image = UIImage("micon")
            self.animateView.isHidden = false
        case .off:
            self.imageView.image = UIImage("micoff")
            self.animateView.isHidden = true
        case .forbidden:
            self.imageView.image = UIImage("micoff")
            self.animateView.isHidden = true
        }
    }

}

private extension AgoraMicVolView {
    func layoutUI() {
        imageView = UIImageView()
        imageView.image = UIImage(named:"micon")
        addSubview(imageView)
        
        animateView = UIView()
        animateView.layer.cornerRadius = 2.22
        animateView.layer.masksToBounds = true
        animateView.backgroundColor = .green
        addSubview(animateView)
        
        progressLayer = CAShapeLayer()
        progressLayer.lineCap = .square
        progressLayer.strokeColor = UIColor.white.cgColor
        progressLayer.strokeStart = 0
        progressLayer.strokeEnd = 0
        animateView.layer.mask = progressLayer
   
        imageView.snp.makeConstraints { make in
            make.left.right.top.bottom.equalToSuperview()
        }
        
        animateView.snp.makeConstraints { make in
            make.centerX.equalToSuperview().offset(1)
            make.width.equalTo(4.44)
            make.height.equalTo(6.87)
            make.top.equalTo(3.73)
        }
    }
}

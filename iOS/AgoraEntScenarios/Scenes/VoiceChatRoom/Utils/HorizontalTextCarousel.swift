//
//  HorizontalTextCarousel.swift
//  AgoraEntScenarios
//
//  Created by 朱继超 on 2023/1/18.
//

import UIKit
import ZSwiftBaseLib

@objc public class HorizontalTextCarousel: UIView {
    
    lazy var voiceIcon: UIImageView = {
        UIImageView(frame: CGRect(x: 8, y: (self.frame.height-10)/2.0, width: 10, height: 10)).image(UIImage.sceneImage(name: "speaker")!)
    }()
    
    lazy var scroll: UIScrollView = {
        let container = UIScrollView(frame: CGRect(x: self.voiceIcon.frame.maxX+5, y: 0, width: self.frame.width-self.voiceIcon.frame.maxX-15, height: self.frame.height))
        container.showsVerticalScrollIndicator = false
        container.showsHorizontalScrollIndicator = false
        container.isUserInteractionEnabled = false
        container.bounces = false
        return container
    }()
    
    lazy var textCarousel: UILabel = {
        UILabel(frame: CGRect(x: 0, y: 0, width: self.frame.width-self.voiceIcon.frame.maxX-15, height: self.frame.height)).font(.systemFont(ofSize: 12, weight: .semibold)).textColor(UIColor(white: 1, alpha: 0.7)).backgroundColor(.clear)
    }()

    public override init(frame: CGRect) {
        super.init(frame: frame)
        self.addSubViews([self.voiceIcon,self.self.scroll])
        self.scroll.addSubview(self.textCarousel)
    }
    
    public func textAnimation(text: NSAttributedString) {
        let width = text.string.z.sizeWithText(font: .systemFont(ofSize: 12, weight: .semibold), size: CGSize(width: 999, height: self.frame.height)).width
        if width > ScreenWidth-60 {
            self.frame = CGRect(x: self.frame.minX, y: self.frame.minY, width: ScreenWidth - 60, height: self.frame.height)
            self.scroll.frame = CGRect(x: self.voiceIcon.frame.maxX+5, y: 0, width: self.frame.width-self.voiceIcon.frame.maxX-15, height: self.frame.height)
            self.setGradient([
                UIColor(red: 1, green: 0.545, blue: 0.125, alpha: 1),
                UIColor(red: 0.672, green: 0, blue: 1, alpha: 1)
            ], [CGPoint(x: 0, y: 0.5),CGPoint(x: 1, y: 0.5)])
            self.textCarousel.frame = CGRect(x: 10, y: 0, width: width+33+20, height: self.frame.height)
            self.scroll.contentSize = CGSize(width: width+33+20, height: self.frame.height)
            self.textCarousel.width = width
            UIView.animate(withDuration: 6, delay: 0, usingSpringWithDamping: 0.8, initialSpringVelocity: 0.3, options: .curveLinear) {
                self.textCarousel.attributedText = text
                self.scroll.contentOffset = CGPoint(x: self.scroll.contentSize.width/3.0, y: self.scroll.contentOffset.y)
            } completion: { finished in
                if finished {
                    self.removeFromSuperview()
                }
            }
        } else {
            self.width = width+33+20
            self.setGradient([
                UIColor(red: 1, green: 0.545, blue: 0.125, alpha: 1),
                UIColor(red: 0.672, green: 0, blue: 1, alpha: 1)
            ], [CGPoint(x: 0, y: 0.5),CGPoint(x: 1, y: 0.5)])
            self.scroll.frame = CGRect(x: 15, y: 0, width: self.frame.width-self.voiceIcon.frame.maxX-15, height: self.frame.height)
            self.textCarousel.frame = CGRect(x: 10, y: 0, width: width+33+20, height: self.frame.height)
            UIView.animate(withDuration: 6, delay: 0, usingSpringWithDamping: 0.8, initialSpringVelocity: 0.3, options: .curveLinear) {
                self.textCarousel.attributedText = text
            } completion: { finished in
                if finished {
                    DispatchQueue.main.async {
                        self.perform(#selector(self.remove), afterDelay: 5)
                    }
                }
            }
        }
    }
    
    @objc private func remove() {
        UIView.animate(withDuration: 0.3) {
            self.alpha = 0
        } completion: { finished in
            if finished {
                self.removeFromSuperview()
            }
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
}

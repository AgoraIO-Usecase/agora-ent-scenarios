import UIKit
import AgoraCommon
class ChorusMicView: UIView {

    private let centralMicSize: CGFloat = 100.0 // 中间大麦位的大小
    private let sideMicSize: CGFloat = 50.0 // 周边麦位的大小
    private let floatingAnimationDuration: TimeInterval = 1.5 // 麦位浮动动画的持续时间

    private var centralMicView: MicView? // 中间大麦位视图
    private var sideMicViews: [MicView] = [] // 周边麦位视图数组
    private let boundaryInset: CGFloat = 20.0 // 边界缩进值

    init(frame: CGRect, topMicCount: Int) {
        super.init(frame: frame)
        addBGView()
        setupMicViews(topMicCount: topMicCount)
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private func addBGView() {
        let bgView = UIImageView(frame: self.bounds)
        
        bgView.image = UIImage.sceneImage(name: "module", bundleName: "DHCResource")
        bgView.contentMode = .center
        self.addSubview(bgView)
    }
    
    private func setupMicViews(topMicCount: Int) {
        let centerPoint = CGPoint(x: bounds.midX, y: bounds.midY)
        
        // 添加中间大麦位视图
        centralMicView = MicView(frame: CGRect(x: (bounds.width - centralMicSize) / 2, y: (bounds.height - centralMicSize) / 2 - 10, width: centralMicSize, height: centralMicSize + 20))
        if let centralMicView = centralMicView {
            addSubview(centralMicView)
            centralMicView.scoreLabel.textColor = .white
            centralMicView.scoreLabel.text = "admin"
            //设置gif图片为 centralMicView.gradeImageView的背景图
            let bundlePath = Bundle.main.path(forResource: "DHCResource", ofType: "bundle") ?? ""
            let bundle = Bundle(path: bundlePath)
            centralMicView.gradeImageView.setGifImage(fromBundle: bundle!, named: "excellent@3x")
            centralMicView.gradeImageView
            let random = nonzeroRandom(in: 5...15)
            centralMicView.addFloatingAnimation(random, random)
        }
        
        let maxRadius = min(bounds.width, bounds.height) / 2 - centralMicSize - sideMicSize - boundaryInset * 2 // 考虑到边界缩进值
        let minRadius = centralMicSize + sideMicSize + boundaryInset * 2 // 考虑到边界缩进值
        let radiusRange = min(minRadius, maxRadius)...max(minRadius, maxRadius)
        for i in 0..<(topMicCount - 1) {
            let angle = CGFloat(i) * 2 * CGFloat.pi / CGFloat(topMicCount) // 计算每个麦位的角度
            
            var isValidPosition = false
            var micView: MicView? = nil
            
            while !isValidPosition {
                let radius = CGFloat.random(in: radiusRange) // 在最小半径和最大半径范围内随机生成麦位的半径
                
                // 计算麦位视图的位置
                let x = centerPoint.x + radius * cos(angle)
                let y = centerPoint.y + radius * sin(angle)
                
                let micFrame = CGRect(x: x - sideMicSize / 2, y: y - sideMicSize / 2 - 10, width: sideMicSize, height: sideMicSize + 20)
                
                if isMicFrameValid(micFrame) {
                    isValidPosition = true
                    micView = MicView(frame: micFrame)
                    micView?.scoreLabel.textColor = .white
                    micView?.scoreLabel.text = "\(i)号麦"
                }
            }
            
            if let micView = micView {
                sideMicViews.append(micView)
                addSubview(micView)
                // 添加浮动效果
                let randomX = nonzeroRandom(in: -20...20)
                let randomY = nonzeroRandom(in: -20...20)
                micView.addFloatingAnimation(randomX, randomY)
            }
        }
    }

    func nonzeroRandom(in range: ClosedRange<CGFloat>) -> CGFloat {
        var randomValue: CGFloat = 0
        repeat {
            randomValue = CGFloat.random(in: range)
        } while randomValue == 0
        return randomValue
    }
    
    private func isMicFrameValid(_ frame: CGRect) -> Bool {
        let boundaryInset: CGFloat = 20.0 // 边界缩进值
        
        for existingMicView in sideMicViews {
            if frame.intersects(existingMicView.frame) {
                return false
            }
        }
        
        if let centralMicView = centralMicView {
            if frame.intersects(centralMicView.frame) {
                return false
            }
        }
        
        if frame.origin.x < boundaryInset || frame.origin.y < boundaryInset || frame.maxX > bounds.width - boundaryInset || frame.maxY > bounds.height - boundaryInset {
            return false
        }
        
        return true
    }
}

class MicView: UIView {
    
    private let micImageView: UIImageView
    public let scoreLabel: UILabel
    public let gradeImageView: UIImageView
    
    var isTaken: Bool = false {
        didSet {
            updateMicImage()
        }
    }
    
    override init(frame: CGRect) {
        micImageView = UIImageView()
        scoreLabel = UILabel()
        gradeImageView = UIImageView()
        
        super.init(frame: frame)
        
        self.frame = frame
        micImageView.contentMode = .scaleAspectFill
        
        micImageView.image = UIImage.sceneImage(name: "ktv_emptySeat_icon")
        
        scoreLabel.font = UIFont.systemFont(ofSize: 12)
        scoreLabel.textAlignment = .center
        
        addSubview(micImageView)
        addSubview(scoreLabel)
        addSubview(gradeImageView)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        micImageView.frame = CGRect(x: 0, y: 0, width: frame.width, height: frame.height - 20)
        scoreLabel.frame = CGRect(x: 0, y: frame.height - 20, width: frame.width, height: 20)
        gradeImageView.frame = CGRect(x: 0, y: frame.height - 50, width: frame.width, height: 20)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func updateMicImage() {
        micImageView.image = isTaken ? UIImage(named: "mic_taken") : UIImage.sceneImage(name: "ktv_emptySeat_icon")
    }
    
    func setScore(_ score: Float) {
        scoreLabel.text = "admin"
    }
}

extension UIView {
    func addFloatingAnimation(_ randomX: CGFloat, _ randomY: CGFloat) {
        let animation = CAKeyframeAnimation(keyPath: "position")
        animation.duration = 5
        animation.repeatCount = .greatestFiniteMagnitude
        animation.autoreverses = true
        
        // 设置计时函数为线性
        animation.timingFunction = CAMediaTimingFunction(name: .linear)
        
        let controlPoint1 = CGPoint(x: center.x + randomX, y: center.y + randomY)
        let controlPoint2 = CGPoint(x: center.x - randomX, y: center.y - randomY)
        let toPoint = center
        
        var intermediatePoints: [CGPoint] = []
        let numIntermediatePoints = 50  // 增加插值点的数量
        for i in 0..<numIntermediatePoints {
            let t = CGFloat(i) / CGFloat(numIntermediatePoints)
            let x = bezierPoint(t, center.x, controlPoint1.x, controlPoint2.x, toPoint.x)
            let y = bezierPoint(t, center.y, controlPoint1.y, controlPoint2.y, toPoint.y)
            let point = CGPoint(x: x, y: y)
            intermediatePoints.append(point)
        }
        
        let path = UIBezierPath()
        path.move(to: center)
        for point in intermediatePoints {
            path.addLine(to: point)
        }
        path.addLine(to: toPoint)
        
        animation.path = path.cgPath
        
        layer.add(animation, forKey: "position")
    }
    
    private func bezierPoint(_ t: CGFloat, _ p0: CGFloat, _ p1: CGFloat, _ p2: CGFloat, _ p3: CGFloat) -> CGFloat {
        let u: CGFloat = 1 - t
        let tt = t * t
        let uu = u * u
        let uuu = uu * u
        let ttt = tt * t
        
        let p = uuu * p0 // (1-t)^3 * P0
            + 3 * uu * t * p1 // 3*(1-t)^2 * t * P1
            + 3 * u * tt * p2 // 3*(1-t) * t^2 * P2
            + ttt * p3 // t^3 * P3
        
        return p
    }
}

extension UIImageView {
    func setGifImage(fromBundle bundle: Bundle, named gifName: String) {
        guard let gifURL = bundle.url(forResource: gifName, withExtension: "gif") else {
            print("无法找到指定的GIF文件")
            return
        }
        
        DispatchQueue.global().async {
            guard let gifSource = CGImageSourceCreateWithURL(gifURL as CFURL, nil) else {
                print("无法创建GIF图像源")
                return
            }
            
            let frameCount = CGImageSourceGetCount(gifSource)
            var images: [UIImage] = []
            var totalDuration: TimeInterval = 0
            
            for i in 0..<frameCount {
                guard let cgImage = CGImageSourceCreateImageAtIndex(gifSource, i, nil) else {
                    continue
                }
                
                let frameDuration = UIImageView.getFrameDuration(from: gifSource, at: i)
                totalDuration += frameDuration
                
                let image = UIImage(cgImage: cgImage)
                images.append(image)
            }
            
            DispatchQueue.main.async {
                self.animationImages = images
                self.animationDuration = totalDuration
                self.startAnimating()
            }
        }
    }
    
    private class func getFrameDuration(from source: CGImageSource, at index: Int) -> TimeInterval {
        let defaultFrameDuration = 0.1 // 默认帧延迟时间
        
        guard let properties = CGImageSourceCopyPropertiesAtIndex(source, index, nil) as? [String: Any] else {
            return defaultFrameDuration // 没有找到属性
        }
        
        guard let gifDict = properties[kCGImagePropertyGIFDictionary as String] as? [String: Any] else {
            return defaultFrameDuration // 没有找到GIF字典
        }
        
        if let unclampedDelayTime = gifDict[kCGImagePropertyGIFUnclampedDelayTime as String] as? NSNumber {
            return unclampedDelayTime.doubleValue
        } else if let delayTime = gifDict[kCGImagePropertyGIFDelayTime as String] as? NSNumber {
            return delayTime.doubleValue
        } else {
            return defaultFrameDuration
        }
    }
}

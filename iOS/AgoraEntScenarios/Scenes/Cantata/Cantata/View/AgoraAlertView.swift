import UIKit

class AgoraAlertView: UIView {
    static var currentAlert: AgoraAlertView?
    static var alertQueue: [AgoraAlertView] = []
    
    var dismissCallback: (() -> Void)?
    
    class func show(with config: (AgoraAlertView) -> Void) {
        let alert = self.init(frame: UIScreen.main.bounds)
        config(alert)
        if currentAlert != nil {
            // 如果当前有弹窗，加入队列
            alertQueue.append(alert)
        } else {
            // 直接显示
            alert.showAlert()
        }
    }
    
    private func showAlert() {
        Self.currentAlert = self
        if let window = UIApplication.shared.windows.first {
            window.addSubview(self)
            
            // 添加显示动画
            self.alpha = 0
            UIView.animate(withDuration: 0.3) {
                self.alpha = 1
            }
        }
    }
    
    func hide() {
        UIView.animate(withDuration: 0.3, animations: {
            self.alpha = 0
        }) { _ in
            self.removeFromSuperview()
            self.dismissCallback?()
            Self.currentAlert = nil
            
            // 显示队列中的下一个弹窗
            if let nextAlert = Self.alertQueue.first {
                Self.alertQueue.removeFirst()
                nextAlert.showAlert()
            }
        }
    }
    
    required override init(frame: CGRect) {
        super.init(frame: frame)
        backgroundColor = UIColor.black.withAlphaComponent(0.4)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
} 

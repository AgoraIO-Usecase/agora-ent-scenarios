//
//  ToastView.swift
//  Scene-Examples
//
//  Created by zhaoyongqiang on 2021/11/10.
//
import UIKit

public enum ToastViewPostion {
    case top, center, bottom
}

public class ToastView: UIView {
    private lazy var tagImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFill
        imageView.layer.masksToBounds = true
        imageView.isHidden = true
        return imageView
    }()

    private lazy var label: UILabel = {
        let label = UILabel()
        label.text = ""
        label.textColor = .white
        label.font = .systemFont(ofSize: 14)
        label.numberOfLines = 0
        label.preferredMaxLayoutWidth = UIScreen.main.bounds.width - 60
        return label
    }()

    var text: String? {
        didSet {
            label.text = text
        }
    }

    var textColor: UIColor? {
        didSet {
            guard let color = textColor else { return }
            label.textColor = color
        }
    }

    var font: UIFont? {
        didSet {
            label.font = font ?? .systemFont(ofSize: 14)
        }
    }

    var tagImage: UIImage? {
        didSet {
            guard tagImage != nil else { return }
            tagImageView.image = tagImage
            tagImageView.isHidden = tagImage == nil
        }
    }

    private static var currentToastView: ToastView?

    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    public static func showWait(text: String, view: UIView? = nil) {
        DispatchQueue.main.async {
            self.currentToastView?.removeFromSuperview()
            let toastView = show(text: text,
                                 tagImage: nil,
                                 textColor: .white,
                                 font: nil,
                                 postion: .center,
                                 view: view)
            self.currentToastView = toastView
            showAnimation(toastView: toastView, isRemove: false)
        }
    }

    public static func hidden(delay: CGFloat = 0.0) {
        if delay <= 0 {
            currentToastView?.removeFromSuperview()
            return
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + delay) {
            UIView.animate(withDuration: 0.15) {
                self.currentToastView?.alpha = 0
            } completion: { _ in
                self.currentToastView?.removeFromSuperview()
            }
        }
    }

    public static func show(text: String, duration: CGFloat = 2.5, view: UIView? = nil) {
        DispatchQueue.main.async {
            let toastView = show(text: text, tagImage: nil,
                                 textColor: .white, font: nil,
                                 postion: .center,
                                 view: view)
            showAnimation(toastView: toastView, duration: duration)
        }
    }

    public static func show(text: String, postion: ToastViewPostion = .center) {
        DispatchQueue.main.async {
            let toastView = show(text: text, tagImage: nil,
                                 textColor: .white, font: nil,
                                 postion: postion,
                                 view: nil)
            showAnimation(toastView: toastView)
        }
    }

    public static func show(text: String,
                     postion: ToastViewPostion = .center,
                     duration: CGFloat = 2.5,
                     view: UIView? = nil)
    {
        DispatchQueue.main.async {
            let toastView = show(text: text, tagImage: nil,
                                 textColor: .white, font: nil,
                                 postion: postion,
                                 view: view)
            showAnimation(toastView: toastView, duration: duration)
        }
    }

    static func show(text: String, tagImage: UIImage? = nil, postion: ToastViewPostion = .center, view: UIView? = nil) {
        DispatchQueue.main.async {
            let toastView = show(text: text, tagImage: tagImage,
                                 textColor: .white, font: nil,
                                 postion: postion,
                                 view: view)

            showAnimation(toastView: toastView)
        }
    }

    @discardableResult
    static func show(text: String,
                     tagImage: UIImage? = nil,
                     textColor: UIColor = .white,
                     font: UIFont? = nil,
                     postion: ToastViewPostion = .center,
                     view: UIView?) -> ToastView
    {
        let toastView = ToastView()
        guard let currentView = view ?? UIViewController.toastKeyWindow else { return toastView }
        toastView.backgroundColor = UIColor.black.withAlphaComponent(0)
        toastView.layer.cornerRadius = 10
        toastView.text = text
        toastView.tagImage = tagImage
        toastView.textColor = textColor
        toastView.font = font
        currentView.addSubview(toastView)
        toastView.translatesAutoresizingMaskIntoConstraints = false
        toastView.centerXAnchor.constraint(equalTo: currentView.centerXAnchor).isActive = true
        switch postion {
        case .top:
            toastView.topAnchor.constraint(equalTo: currentView.safeAreaLayoutGuide.topAnchor, constant: 30).isActive = true
        case .center:
            toastView.centerYAnchor.constraint(equalTo: currentView.centerYAnchor).isActive = true
        case .bottom:
            toastView.bottomAnchor.constraint(equalTo: currentView.safeAreaLayoutGuide.bottomAnchor, constant: -130).isActive = true
        }
        return toastView
    }

    private static func showAnimation(toastView: UIView, isRemove: Bool = true, duration: TimeInterval = 2.5) {
        UIView.animate(withDuration: 0.15) {
            toastView.backgroundColor = UIColor.black.withAlphaComponent(0.6)
        } completion: { _ in
            guard isRemove else { return }
            DispatchQueue.main.asyncAfter(deadline: .now() + duration) {
                UIView.animate(withDuration: 0.15) {
                    toastView.alpha = 0
                } completion: { _ in
                    toastView.removeFromSuperview()
                }
            }
        }
    }

    private func setupUI() {
        backgroundColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.6)
        tagImageView.translatesAutoresizingMaskIntoConstraints = false
        label.translatesAutoresizingMaskIntoConstraints = false
        addSubview(tagImageView)
        addSubview(label)

        tagImageView.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 10).isActive = true
        tagImageView.centerYAnchor.constraint(equalTo: centerYAnchor).isActive = true

        label.leadingAnchor.constraint(equalTo: tagImageView.trailingAnchor, constant: 10).isActive = true
        label.topAnchor.constraint(equalTo: topAnchor, constant: 8).isActive = true
        label.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -20).isActive = true
        label.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -8).isActive = true
    }
}

extension UIViewController {
    static var toastKeyWindow: UIWindow? {
        // Get connected scenes
        if #available(iOS 13.0, *) {
            return UIApplication.shared.connectedScenes
                // Keep only active scenes, onscreen and visible to the user
                .filter { $0.activationState == .foregroundActive }
                // Keep only the first `UIWindowScene`
                .first(where: { $0 is UIWindowScene })
                // Get its associated windows
                .flatMap({ $0 as? UIWindowScene })?.windows
                // Finally, keep only the key window
                .first(where: \.isKeyWindow)
        } else {
            return UIApplication.shared.keyWindow
        }
    }
}

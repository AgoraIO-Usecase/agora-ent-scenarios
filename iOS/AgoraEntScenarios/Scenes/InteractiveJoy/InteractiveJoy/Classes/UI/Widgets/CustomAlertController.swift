//
//  CustomAlertViewController.swift
//  InteractiveJoy
//
//  Created by qinhui on 2024/7/30.
//
import UIKit

class CustomAlertController: UIViewController {
    var selectedTitle: String?
    private let titleText: String?
    private let messageText: String?
    private var actions: [CustomAlertAction] = []
    private var items: [AlertViewItem] = []
    
    private let containerView = UIView()
    private var panGestureRecognizer: UIPanGestureRecognizer!
    
    init(title: String?, message: String?) {
        self.titleText = title
        self.messageText = message
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func addAction(title: String, style: UIAlertAction.Style, handler: ((UIAlertAction) -> Void)?) {
        let action = CustomAlertAction(title: title, style: style, handler: handler)
        actions.append(action)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.backgroundColor = UIColor.black.withAlphaComponent(0.5)
        
        containerView.backgroundColor = UIColor.joy_footer_separator
        containerView.layer.cornerRadius = 10
        self.view.addSubview(containerView)
        
        containerView.snp.makeConstraints { make in
            make.leading.trailing.bottom.equalToSuperview()
            make.height.equalTo(300)
        }
        
        let cornerBackView = UIView()
        cornerBackView.backgroundColor = .white
        containerView.addSubview(cornerBackView)
//        addTopCornersRadius(to: cornerBackView, radius: 19)
        cornerBackView.snp.makeConstraints { make in
            make.top.right.left.equalTo(0)
            make.height.equalTo(19)
        }
        
        let cornerImageView = UIImageView()
        cornerImageView.image = UIImage.sceneImage(name: "alert_corner_ic")
        containerView.addSubview(cornerImageView)
        
        cornerImageView.snp.makeConstraints { make in
            make.height.equalTo(3)
            make.width.equalTo(33)
            make.centerX.equalTo(cornerBackView)
            make.centerY.equalTo(cornerBackView)
        }
        
        panGestureRecognizer = UIPanGestureRecognizer(target: self, action: #selector(handlePanGesture(_:)))
        containerView.addGestureRecognizer(panGestureRecognizer)
        
        let tapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(dismissPopup))
        self.view.addGestureRecognizer(tapGestureRecognizer)
        
        if let titleText = titleText {
            let titleLabel = UILabel()
            titleLabel.text = titleText
            titleLabel.textAlignment = .center
            titleLabel.font = UIFont.boldSystemFont(ofSize: 18)
            titleLabel.backgroundColor = .white
            containerView.addSubview(titleLabel)
            
            titleLabel.snp.makeConstraints { make in
                make.top.equalTo(cornerBackView.snp.bottom)
                make.left.right.equalTo(0)
                make.height.equalTo(60)
            }
        }
        
        var previousItem: AlertViewItem? = nil
        var index = 0
        for action in actions {
            let item = createItem(title: action.title, style: action.style)
            containerView.addSubview(item)
            
            item.snp.makeConstraints { make in
                make.leading.trailing.equalToSuperview()
                make.height.equalTo(48)
                if let previousItem = previousItem { 
                    if index == actions.count - 1 {
                        make.top.equalTo(previousItem.snp.bottom).offset(8)
                    } else {
                        make.top.equalTo(previousItem.snp.bottom).offset(0)
                    }
                } else {
                    make.top.equalToSuperview().offset(60)
                }
            }
            
            previousItem = item
            item.addTarget(self, action: #selector(buttonTapped(_:)), for: .touchUpInside)
            items.append(item)
            index = index + 1
        }
        
        let cancelButton = actions.first { $0.style == .cancel }
        cancelButton?.button?.addTarget(self, action: #selector(dismissPopup), for: .touchUpInside)
        
        if let lastItem = previousItem {
            lastItem.snp.makeConstraints { make in
                make.bottom.equalToSuperview()
            }
            lastItem.showLine = false
        }
    }
    
//    private func addTopCornersRadius(to view: UIView, radius: CGFloat) {
//        let path = UIBezierPath(roundedRect: view.bounds,
//                                byRoundingCorners: [.topLeft, .topRight],
//                                cornerRadii: CGSize(width: radius, height: radius))
//        
//        let maskLayer = CAShapeLayer()
//        maskLayer.path = path.cgPath
//        view.layer.mask = maskLayer
//        
//        let borderLayer = CAShapeLayer()
//        borderLayer.path = path.cgPath
//        borderLayer.fillColor = view.backgroundColor?.cgColor
//        borderLayer.frame = view.bounds
//        view.layer.insertSublayer(borderLayer, at: 0)
//    }
        
    private func createItem(title: String, style: UIAlertAction.Style) -> AlertViewItem {
        let item = AlertViewItem(type: .custom)
        item.title = title
        if let selectedTitle = selectedTitle, title == selectedTitle {
            item.select = true
        }
        return item
    }
    
    @objc private func buttonTapped(_ sender: AlertViewItem) {
        guard let view = sender as? AlertViewItem, let title = view.title else {return}
        guard let action = actions.first(where: { $0.title == title }) else { return }
        action.handler?(UIAlertAction(title: title, style: action.style, handler: action.handler))
        
        clearAllSelectState()
        view.select = true
        dismissPopup()
    }
    
    private func clearAllSelectState() {
        for item in items {
            item.select = false
        }
    }
    
    @objc private func dismissPopup() {
        self.dismiss(animated: true, completion: nil)
    }
    
    @objc private func handlePanGesture(_ recognizer: UIPanGestureRecognizer) {
        let translation = recognizer.translation(in: view)
        
        if recognizer.state == .changed {
            if translation.y > 0 {
                containerView.transform = CGAffineTransform(translationX: 0, y: translation.y)
            }
        } else if recognizer.state == .ended {
            if translation.y > 150 {
                dismiss(animated: true, completion: nil)
            } else {
                UIView.animate(withDuration: 0.3) {
                    self.containerView.transform = .identity
                }
            }
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        containerView.transform = CGAffineTransform(translationX: 0, y: 300)
        UIView.animate(withDuration: 0.3) {
            self.containerView.transform = .identity
        }
    }
    
    override func dismiss(animated flag: Bool, completion: (() -> Void)? = nil) {
        UIView.animate(withDuration: 0.3, animations: {
            self.containerView.transform = CGAffineTransform(translationX: 0, y: 300)
        }) { _ in
            super.dismiss(animated: false, completion: completion)
        }
    }
}

class CustomAlertAction {
    let title: String
    let style: UIAlertAction.Style
    var handler: ((UIAlertAction) -> Void)?
    weak var button: UIButton?
    
    init(title: String, style: UIAlertAction.Style, handler: ((UIAlertAction) -> Void)?) {
        self.title = title
        self.style = style
        self.handler = handler
    }
}

class AlertViewItem: UIButton {
    private lazy var textLabel: UILabel = {
        let label = UILabel()
        label.font = UIFont.joy_R_14
        label.textColor = UIColor.joy_Ellipse6
        return label
    }()
    
    private lazy var line: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor.joy_footer_separator
        return view
    }()
    
    var showLine: Bool = true {
        didSet {
            line.isHidden = !showLine
        }
    }
    
    var select: Bool = false {
        didSet {
            textLabel.textColor = select ? UIColor.joy_zi02 : UIColor.joy_Ellipse6
        }
    }
    
    var title: String? {
        set {
            self.textLabel.text = newValue
        } get {
            return self.textLabel.text
        }
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.backgroundColor = .white
        self.addSubview(textLabel)
        self.addSubview(line)
        
        textLabel.snp.makeConstraints { make in
            make.center.equalTo(self)
        }
        line.snp.makeConstraints { make in
            make.left.right.bottom.equalTo(0)
            make.height.equalTo(1)
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

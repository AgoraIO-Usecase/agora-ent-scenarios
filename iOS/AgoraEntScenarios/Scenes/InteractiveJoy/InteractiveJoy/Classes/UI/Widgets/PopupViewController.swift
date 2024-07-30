//
//  CustomAlertView.swift
//  InteractiveJoy
//
//  Created by qinhui on 2024/7/30.
//
import UIKit

class PopupViewController: UIViewController {
    let containerView = UIView()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.backgroundColor = UIColor.black.withAlphaComponent(0.5)
        
        containerView.backgroundColor = .white
        containerView.layer.cornerRadius = 10
        self.view.addSubview(containerView)
        
        containerView.snp.makeConstraints { make in
            make.leading.trailing.bottom.equalToSuperview()
            make.height.equalTo(300)
        }
        
        let titleLabel = UILabel()
        titleLabel.text = "切换供应商"
        titleLabel.textAlignment = .center
        titleLabel.font = UIFont.boldSystemFont(ofSize: 18)
        containerView.addSubview(titleLabel)
        
        titleLabel.snp.makeConstraints { make in
            make.top.equalToSuperview().offset(16)
            make.centerX.equalToSuperview()
        }
        
        let buttonA = createButton(title: "A")
        let buttonB = createButton(title: "B")
        let buttonC = createButton(title: "C")
        let cancelButton = createButton(title: "取消")
        
        containerView.addSubview(buttonA)
        containerView.addSubview(buttonB)
        containerView.addSubview(buttonC)
        containerView.addSubview(cancelButton)
        
        buttonA.snp.makeConstraints { make in
            make.top.equalTo(titleLabel.snp.bottom).offset(16)
            make.leading.trailing.equalToSuperview().inset(16)
            make.height.equalTo(44)
        }
        
        buttonB.snp.makeConstraints { make in
            make.top.equalTo(buttonA.snp.bottom).offset(8)
            make.leading.trailing.equalToSuperview().inset(16)
            make.height.equalTo(44)
        }
        
        buttonC.snp.makeConstraints { make in
            make.top.equalTo(buttonB.snp.bottom).offset(8)
            make.leading.trailing.equalToSuperview().inset(16)
            make.height.equalTo(44)
        }
        
        cancelButton.snp.makeConstraints { make in
            make.top.equalTo(buttonC.snp.bottom).offset(8)
            make.leading.trailing.equalToSuperview().inset(16)
            make.height.equalTo(44)
            make.bottom.equalToSuperview().offset(-16)
        }
        
        cancelButton.addTarget(self, action: #selector(dismissPopup), for: .touchUpInside)
    }
    
    func createButton(title: String) -> UIButton {
        let button = UIButton(type: .system)
        button.setTitle(title, for: .normal)
        button.setTitleColor(.white, for: .normal)
        button.backgroundColor = .systemBlue
        button.layer.cornerRadius = 5
        button.addTarget(self, action: #selector(buttonTapped(_:)), for: .touchUpInside)
        return button
    }
    
    @objc func buttonTapped(_ sender: UIButton) {
        guard let title = sender.titleLabel?.text else { return }
        print("Selected \(title)")
    }
    
    @objc func dismissPopup() {
        self.dismiss(animated: true, completion: nil)
    }
}

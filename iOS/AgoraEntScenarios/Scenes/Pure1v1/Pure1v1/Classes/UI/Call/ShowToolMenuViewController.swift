//
//  ShowToolMenuViewController.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/29.
//

import UIKit

@objc protocol ShowToolMenuViewControllerDelegate: NSObjectProtocol {
    func onClickCameraButtonSelected(_ menu:ShowToolMenuViewController, _ selected: Bool)
    func onClickMicButtonSelected(_ menu:ShowToolMenuViewController, _ selected: Bool)
    func onClickRealTimeDataButtonSelected(_ menu:ShowToolMenuViewController, _ selected: Bool)
}

class ShowToolMenuViewController: UIViewController {
    private var menuTypes: [ShowToolMenuType]
    var menuTitle: String? {
        didSet {
            menuView?.title = menuTitle
        }
    }
    
    var selectedMap: [ShowToolMenuType: Bool] = [ShowToolMenuType: Bool]() {
        didSet {
            self.menuView?.selectedMap = selectedMap
            updateLayoutForType()
        }
    }
    weak var delegate: ShowToolMenuViewControllerDelegate?
    
    private var menuView: ShowToolMenuView?
    
    required init(menuTypes: [ShowToolMenuType]) {
        self.menuTypes = menuTypes
        super.init(nibName: nil, bundle: nil)
        modalPresentationStyle = .overCurrentContext
//        modalTransitionStyle = .crossDissolve
        
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        setUpUI()
    }
    
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        dismiss(animated: true)
    }
    
    private func setUpUI(){
        menuView = ShowToolMenuView(menuTypes: menuTypes)
        view.addSubview(menuView!)
        menuView?.title = menuTitle
        menuView?.selectedMap = selectedMap
        updateLayoutForType()
        menuView?.onTapItemClosure = {[weak self] modelType, isSelected in
            guard let self = self else { return }
            switch modelType {
            case .real_time_data:
                self.delegate?.onClickRealTimeDataButtonSelected(self, isSelected)
                break
            case .camera:
                self.delegate?.onClickCameraButtonSelected(self, isSelected)
            case .mic:
                self.selectedMap[modelType] = isSelected
                self.delegate?.onClickMicButtonSelected(self, isSelected)
            }
        }
    }
    
    private func updateLayoutForType() {
        var height = 150
        menuView?.snp.remakeConstraints({ make in
            make.left.bottom.right.equalToSuperview()
            make.height.equalTo(height)
        })
    }

}

//
//  ShowToolMenuViewController.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/29.
//

import UIKit

protocol ShowToolMenuViewControllerDelegate: NSObjectProtocol {
    func onClickCameraButtonSelected(_ selected: Bool)
    func onClickHDButtonSelected(_ selected: Bool)
    func onClickEndPkButtonSelected(_ selected: Bool)
    func onClickMicButtonSelected(_ selected: Bool)
    func onClickMuteMicButtonSelected(_ selected: Bool)
    func onClickRealTimeDataButtonSelected(_ selected: Bool)
    func onClickSwitchCameraButtonSelected(_ selected: Bool)
    func onClickSettingButtonSelected(_ selected: Bool)

}

class ShowToolMenuViewController: UIViewController {
    
    var menuTitle: String? {
        didSet {
            menuView.title = menuTitle
        }
    }
    var type: ShowMenuType = .idle_audience {
        didSet {
            menuView.type = type
        }
    }
    var delegate: ShowToolMenuViewControllerDelegate?
    
    private var menuView = ShowToolMenuView(type: .idle_audience)
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        modalPresentationStyle = .overCurrentContext
        modalTransitionStyle = .crossDissolve
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
        view.addSubview(menuView)
        updateLayoutForType(type)
        menuView.onTapItemClosure = {[weak self] modelType, isSelected in
            switch modelType {
            case .camera:
                self?.delegate?.onClickCameraButtonSelected(isSelected)
                break
            case .HD:
                self?.delegate?.onClickHDButtonSelected(isSelected)
                break
            case .end_pk:
                self?.dismiss(animated: true)
                self?.delegate?.onClickEndPkButtonSelected(isSelected)
                break
            case .mic:
                self?.delegate?.onClickMicButtonSelected(isSelected)
                break
            case .mute_mic:
                self?.delegate?.onClickMuteMicButtonSelected(isSelected)
                break
            case .real_time_data:
                self?.delegate?.onClickRealTimeDataButtonSelected(isSelected)
                break
            case .switch_camera:
                self?.delegate?.onClickSwitchCameraButtonSelected(isSelected)
                break
            case .setting:
                self?.delegate?.onClickSettingButtonSelected(isSelected)
                break
            }
        }
    }
    
    private func updateLayoutForType(_ menuType: ShowMenuType) {
        var height = 210
        if type == .idle_audience {
            height = 150
        }
        menuView.type = type
        menuView.snp.remakeConstraints({ make in
            make.left.bottom.right.equalToSuperview()
            make.height.equalTo(height)
        })
    }

}

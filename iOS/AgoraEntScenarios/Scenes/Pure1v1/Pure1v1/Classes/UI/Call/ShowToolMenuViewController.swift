//
//  ShowToolMenuViewController.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/29.
//

import UIKit

protocol ShowToolMenuViewControllerDelegate: NSObjectProtocol {
    func onClickRealTimeDataButtonSelected(_ menu:ShowToolMenuViewController, _ selected: Bool)
}

class ShowToolMenuViewController: UIViewController {
    
    var menuTitle: String? {
        didSet {
            menuView?.title = menuTitle
        }
    }
    var type: ShowMenuType = .idle_audience {
        didSet {
            //TODO(pengpeng):
            if type == oldValue { return }
            updateLayoutForType(type)
        }
    }
    var selectedMap: [ShowToolMenuType: Bool] = [ShowToolMenuType: Bool]() {
        didSet {
            self.menuView?.selectedMap = selectedMap
            updateLayoutForType(type)
        }
    }
    weak var delegate: ShowToolMenuViewControllerDelegate?
    
    private var menuView: ShowToolMenuView?
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
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
        menuView = ShowToolMenuView(type: type)
        view.addSubview(menuView!)
        menuView?.title = menuTitle
        menuView?.selectedMap = selectedMap
        updateLayoutForType(type)
        menuView?.onTapItemClosure = {[weak self] modelType, isSelected in
            guard let self = self else { return }
            switch modelType {
            case .real_time_data:
                self.delegate?.onClickRealTimeDataButtonSelected(self, isSelected)
                break
            }
        }
    }
    
    private func updateLayoutForType(_ menuType: ShowMenuType) {
        var height = 210
        if type == .idle_audience {
            height = 150
        }
        menuView?.type = type
        menuView?.snp.remakeConstraints({ make in
            make.left.bottom.right.equalToSuperview()
            make.height.equalTo(height)
        })
    }

}

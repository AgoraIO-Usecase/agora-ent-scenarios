//
//  ShowReceiveLiveFinishAlertVC.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/16.
//

import UIKit

class ShowReceiveLiveFinishAlertVC: UIViewController {

    private var dismissAlert: (()->())?
    fileprivate lazy var finishAlertView: ShowReceiveFinishView = {
        let view = ShowReceiveFinishView()
        view.delegate = self
        return view
    }()

    fileprivate var headUrl: String? {
        didSet{
            finishAlertView.headImg = headUrl
        }
    }
    
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
    
    private func setUpUI(){
        view.backgroundColor = .show_cover
        
        view.addSubview(finishAlertView)
        finishAlertView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
    }
    
    func dismissAlert(_ dismiss: @escaping (()->())) {
        dismissAlert = dismiss
    }

}

private let alertViewTag = 1000001
extension ShowReceiveLiveFinishAlertVC {
    class func show(topVC: UIViewController,
                    ownerUrl: String,
                    ownerName: String,
                    dismiss: @escaping (()->())){
        for childVC in topVC.children {
            if childVC is ShowReceiveLiveFinishAlertVC {
                return
            }
        }
        
        let vc = ShowReceiveLiveFinishAlertVC()
        vc.finishAlertView.headImg = ownerUrl
        vc.finishAlertView.headName = ownerName
        vc.dismissAlert = dismiss
        topVC.view.addSubview(vc.view)
        vc.view.frame = topVC.view.bounds
        topVC.addChild(vc)
    }
}

extension ShowReceiveLiveFinishAlertVC: ShowReceiveFinishViewDelegate {
    func onClickBackButton() {
        dismiss(animated: true) { [weak self] in
            self?.dismissAlert?()
        }
    }
}

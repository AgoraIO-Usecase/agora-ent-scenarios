//
//  ShowReceiveLiveFinishAlertVC.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/16.
//

import UIKit

class ShowReceiveLiveFinishAlertVC: UIViewController {

    private var dismissAlert: (()->())?
    private lazy var finishAlertView: ShowReceiveFinishView = {
        let view = ShowReceiveFinishView()
        view.headImg = VLUserCenter.user.headUrl
        view.delegate = self
        return view
    }()

    
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

extension ShowReceiveLiveFinishAlertVC {
    class func present(dismiss: @escaping (()->())){
        let vc = ShowReceiveLiveFinishAlertVC()
        vc.dismissAlert = dismiss
        let topVC = UIViewController.cl_topViewController()
        topVC?.present(vc, animated: true)
    }
}

extension ShowReceiveLiveFinishAlertVC: ShowReceiveFinishViewDelegate {
    func onClickBackButton() {
        dismiss(animated: true) { [weak self] in
            self?.dismissAlert?()
        }
    }
}

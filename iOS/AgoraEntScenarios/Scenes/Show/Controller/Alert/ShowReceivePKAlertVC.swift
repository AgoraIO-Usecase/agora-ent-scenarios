//
//  ShowReceivePKAlertVC.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/16.
//

import UIKit

typealias ShowReceivePKAlertVCDismiss = (_ result: ShowReceivePKAlertVC.Result)->()

class ShowReceivePKAlertVC: UIViewController {
    
    var name: String?
    
    var style: ShowReceivePKView.Style?
    
    private var dismissWithResult: ShowReceivePKAlertVCDismiss?
    
    private lazy var pkAlertView: ShowReceivePKView = {
        let view = ShowReceivePKView(style: style)
        view.delegate = self
        return view
    }()
    
    private var countDown = 15 // 倒计时
    private var timer: Timer?
    
    deinit {
        timer?.invalidate()
        timer = nil
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
        createTimer()
    }
    
    private func setUpUI(){
        view.backgroundColor = .show_cover
        pkAlertView.name = name
        pkAlertView.countDown = countDown
        view.addSubview(pkAlertView)
        pkAlertView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
    }
    
    private func createTimer(){
        timer = Timer(timeInterval: 1, repeats: true, block: {[weak self] timer in
            guard let wSelf = self else {return}
            if wSelf.countDown == 0 {
                wSelf.dismiss(animated: true)
                wSelf.dismissWithResult?(.timeOut)
            }
            wSelf.pkAlertView.countDown = wSelf.countDown
            wSelf.countDown -= 1
        })
        RunLoop.current.add(timer!, forMode: .default)
        timer?.fire()
    }
    
    func dismissWithResult(_ dismissWithResult: @escaping ShowReceivePKAlertVCDismiss) {
        self.dismissWithResult = dismissWithResult
    }

}

extension ShowReceivePKAlertVC: ShowReceivePKViewDelegate {
    
    func onClickRefuseButton() {
        dismiss(animated: true)
        dismissWithResult?(.refuse)
    }
    
    func onClickAcceptButton() {
        dismiss(animated: true)
        dismissWithResult?(.accept)
    }

}

extension ShowReceivePKAlertVC {
    class func present(name:String?,style: ShowReceivePKView.Style = .pk, dismiss: @escaping ShowReceivePKAlertVCDismiss){
        let vc = ShowReceivePKAlertVC()
        vc.name = name
        vc.style = style
        vc.dismissWithResult = dismiss
        let topVC = UIViewController.cl_topViewController()
        topVC?.present(vc, animated: true)
    }
}

extension ShowReceivePKAlertVC {
    enum Result {
        case refuse     // 拒绝
        case accept     // 接受
        case timeOut    // 超时
    }
}

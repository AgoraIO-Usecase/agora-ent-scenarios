//
//  ShowReceivePKAlertVC.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/16.
//

import UIKit


class ShowReceivePKAlertVC: UIViewController {
    
    var name: String?
    
    private var dismissWithResult: ((_ result: ShowReceivePKAlertVC.Result)->())?
    
    private lazy var pkAlertView: ShowReceivePKView = {
        let view = ShowReceivePKView()
        view.delegate = self
        return view
    }()
    
    private var countDown = 15 // 倒计时
    private var timer: Timer!
    
    deinit {
        timer.invalidate()
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
        RunLoop.current.add(timer, forMode: .default)
        timer.fire()
    }
    
    func dismissWithResult(_ dismissWithResult: @escaping ((_ result: ShowReceivePKAlertVC.Result)->())) {
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
    enum Result {
        case refuse     // 拒绝
        case accept     // 接受
        case timeOut    // 超时
    }
}

//
//  PlayWebGameController.swift
//  InteractiveJoy
//
//  Created by qinhui on 2024/7/30.
//

import UIKit
import WebKit

class PlayWebGameViewController: UIViewController {
    var url: String?
    private lazy var naviBar: JoyNavigationBar = {
        let bar = JoyNavigationBar(frame: CGRect(x: 0, y: UIDevice.current.aui_SafeDistanceTop, width: self.view.width, height: 44))
        bar.backgroundColor = .white
        return bar
    }()
    
    private lazy var webView: WKWebView = {
        let webView = WKWebView()
        return webView
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .white
        view.addSubview(webView)
        view.addSubview(naviBar)
        
        webView.snp.makeConstraints { make in
            make.top.equalTo(naviBar.snp.bottom)
            make.left.right.bottom.equalTo(0)
        }
        
        if let urlString = url, let requestUrl = URL(string: urlString) {
            let request = URLRequest(url: requestUrl)
            webView.load(request)
        }
    }
}

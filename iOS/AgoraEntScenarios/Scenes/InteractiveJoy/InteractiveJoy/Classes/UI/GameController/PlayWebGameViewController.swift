//
//  PlayWebGameController.swift
//  InteractiveJoy
//
//  Created by qinhui on 2024/7/30.
//

import UIKit
import WebKit

class PlayWebGameViewController: UIViewController, WKNavigationDelegate, WKUIDelegate {
    var url: String?
    private lazy var naviBar: JoyNavigationBar = {
        let bar = JoyNavigationBar(frame: CGRect(x: 0, y: UIDevice.current.aui_SafeDistanceTop, width: self.view.width, height: 44))
        bar.backgroundColor = .white
        return bar
    }()
    
    private lazy var webView: WKWebView = {
        let webView = WKWebView()
        webView.uiDelegate = self
        webView.navigationDelegate = self
        webView.translatesAutoresizingMaskIntoConstraints = false
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
    
    // WKNavigationDelegate 方法，用于拦截链接
    func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
        let url = navigationAction.request.url?.absoluteString ?? ""
        print("shouldOverrideUrlLoading: \(url)")
        
        if url.starts(with: "game://") {
            // 处理特定的 URL
            if url == "game://close" {
                self.navigationController?.popViewController(animated: true)
            }
            decisionHandler(.cancel)
            return
        }
        
        if url.starts(with: "http") || url.starts(with: "https") || url.starts(with: "ftp") || url.starts(with: "file:///android_asset") {
            if url.starts(with: "file:///android_asset") {
                // 处理特定的 URL
                // 例如，打开另一个视图控制器
                decisionHandler(.cancel)
                return
            }
            decisionHandler(.allow)
            return
        }
        
        decisionHandler(.cancel)
    }
}

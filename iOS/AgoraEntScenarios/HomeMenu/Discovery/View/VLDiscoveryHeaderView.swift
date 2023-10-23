//
//  VLDiscoveryHeaderView.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/10/20.
//

import UIKit
import WebKit

class VLDiscoveryHeaderView: UICollectionReusableView {
    private lazy var webView: WKWebView = {
        let configuration = WKWebViewConfiguration()
        configuration.allowsInlineMediaPlayback = true
        configuration.mediaTypesRequiringUserActionForPlayback = .all
        let webView = WKWebView(frame: .zero, configuration: configuration)
        webView.scrollView.backgroundColor = .clear
        webView.backgroundColor = .clear
        webView.navigationDelegate = self
        webView.uiDelegate = self
        webView.isOpaque = false
        webView.translatesAutoresizingMaskIntoConstraints = false
        return webView
    }()
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        addSubview(webView)
        webView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        webView.topAnchor.constraint(equalTo: topAnchor).isActive = true
        webView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        webView.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
        injectMethod(method: "JSBridge")
        loadHtml(urlString: "\(VLURLConfig.kURLPathH5Discover)?token=\(VLUserCenter.user.token)")
    }
    
    func injectMethod(method: String) {
        webView.configuration.userContentController.add(self, name: method)
    }
    
    func loadHtml(urlString: String) {
        guard let url = URL(string: urlString) else { return }
        let request = URLRequest(url: url, cachePolicy: .reloadIgnoringLocalAndRemoteCacheData, timeoutInterval: 10)
        webView.load(request)
    }
}

extension VLDiscoveryHeaderView: WKScriptMessageHandler, WKNavigationDelegate, WKUIDelegate {
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        let body = message.body as? [String: Any]
        let method = body?["nativeMethod"] as? String
        let paramsString = body?["params"] as? String
        guard let jsonData = paramsString?.data(using: .utf8) else { return }
        let dict = try? JSONSerialization.jsonObject(with: jsonData, options: .mutableContainers) as? [String: Any]
    
        if method == "jumpToWebview" {
            let redirectUrl = (dict?["redirectUrl"] as? String) ?? ""
            let webViewVC = VLCommonWebViewController()
            webViewVC.urlString = redirectUrl
            UIViewController.cl_topViewController()?.navigationController?.pushViewController(webViewVC, animated: true)
        }
    }
}

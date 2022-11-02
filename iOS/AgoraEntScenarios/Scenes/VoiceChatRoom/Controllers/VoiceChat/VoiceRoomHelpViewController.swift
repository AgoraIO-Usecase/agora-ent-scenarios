//
//  VoiceRoomHelpViewController.swift
//  AgoraScene_iOS
//
//  Created by CP on 2022/10/19.
//

import UIKit
import WebKit
import ZSwiftBaseLib

class VoiceRoomHelpViewController: VRBaseViewController, WKNavigationDelegate {
    lazy var webView: WKWebView = {
        let preferences = WKPreferences()
        preferences.javaScriptEnabled = true

        let configuration = WKWebViewConfiguration()
        configuration.preferences = preferences
        configuration.userContentController = WKUserContentController()

        var webView = WKWebView(frame: CGRect(x: 0, y: ZNavgationHeight, width: ScreenWidth, height: ScreenHeight), configuration: configuration)
        webView.scrollView.bounces = true
        webView.scrollView.alwaysBounceVertical = true
        webView.navigationDelegate = self
        return webView
    }()

    override func viewDidLoad() {
        super.viewDidLoad()
        navigation.title.text = "Agora"
        let urlStr = "https://www.agora.io"
        webView.load(URLRequest(url: URL(string: urlStr)!))
        view.addSubview(webView)
    }

    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        navigation.isHidden = false
    }
}

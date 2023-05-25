//
//  UIViewController+Show.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/22.
//

import Foundation

extension UIViewController {
    
    func showAlert(title: String? = nil, message: String, confirmTitle: String? = "show_alert_confirm_btn_title".show_localized, cancelTitle: String? = "show_alert_cancel_btn_title".show_localized, confirm: (()->Void)? = nil ) {
        let alertController = UIAlertController(title: title, message: message, preferredStyle: .alert)
        if confirmTitle != nil {
            let action = UIAlertAction(title: confirmTitle, style: .default) { _ in
                confirm?()
            }
            alertController.addAction(action)
        }
        if cancelTitle != nil {
            let cancel = UIAlertAction(title: cancelTitle, style: .cancel, handler: nil)
            alertController.addAction(cancel)
        }
        present(alertController, animated: true, completion: nil)
    }
    
    func showCustomAlert(title: String? = nil,
                         message: String? = nil,
                         confirmTitle: String? = LanguageManager.localValue(key: "Confirm"),
                         cancelTitle: String? = LanguageManager.localValue(key: "Cancel"),
                         confirm: (()->Void)? = nil,
                         cancel: (()->Void)? = nil) {
        
        AUiAlertView()
            .title(title: title)
            .content(content: message)
            .content(textAlignment: .center)
            .contentColor(color: UIColor(hexString: "#6C7192"))
            .leftButton(title: cancelTitle)
            .leftButtonBorder(color: .clear)
            .leftButton(color: UIColor(hexString: "#3C4267"))
            .leftButtonBackground(color: UIColor(hexString: "#EFF4FF"))
            .leftButton(cornerRadius: 24)
            .rightButton(title: confirmTitle)
            .rightButton(color: UIColor(hexString: "#FFFFFF"))
            .rightButtonBackground(color: UIColor(hexString: "#219BFF"))
            .rightButton(cornerRadius: 24)
            .leftButtonTapClosure {
                cancel?()
            }.rightButtonTapClosure {
                confirm?()
            }.show()
    }
}

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
    
}

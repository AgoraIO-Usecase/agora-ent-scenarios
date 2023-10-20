//
//  VLMineAccountViewController.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/10/16.
//

import UIKit

@objc
class VLMineAccountViewController: BaseViewController {
    private lazy var tableView: UITableView = {
        let tableView = UITableView(frame: .zero, style: .grouped)
        tableView.translatesAutoresizingMaskIntoConstraints = false
        tableView.delegate = self
        tableView.dataSource = self
        tableView.register(VLMineAccountCell.self, forCellReuseIdentifier: "cell")
        tableView.translatesAutoresizingMaskIntoConstraints = false
        tableView.backgroundColor = .clear
        tableView.estimatedSectionFooterHeight = 0
        tableView.estimatedSectionHeaderHeight = 0
        tableView.separatorStyle = .singleLine
        tableView.separatorColor = UIColor(hex: "#F2F2F6", alpha: 1.0)
        tableView.showsVerticalScrollIndicator = false

        return tableView
    }()
    private lazy var logoutButton: UIButton = {
        let button = UIButton()
        button.setTitle(NSLocalizedString("app_logout", comment: ""), for: .normal)
        button.backgroundColor = UIColor(hex: "#08062F", alpha: 0.05)
        button.setTitleColor(UIColor(hex: "#303553", alpha: 1.0), for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 15)
        button.translatesAutoresizingMaskIntoConstraints = false
        button.cornerRadius(12)
        button.addTarget(self, action: #selector(onClickLogoutButton), for: .touchUpInside)
        return button
    }()
    
    private var dataArray = VLMineAccountModel.createData()
    
    override func viewDidLoad() {
        super.viewDidLoad()

        setupUI()
    }
    
    private func setupUI() {
        setNaviTitleName(NSLocalizedString("app_my_account", comment: ""))
        setBackBtn()
                
        view.addSubview(tableView)
        tableView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20).isActive = true
        tableView.topAnchor.constraint(equalTo: view.topAnchor, constant: Screen.kNavHeight).isActive = true
        tableView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20).isActive = true
        tableView.bottomAnchor.constraint(equalTo: view.bottomAnchor, constant: -20).isActive = true
    }
    
    @objc
    private func onClickLogoutButton() {
        let alertVC = UIAlertController(title: NSLocalizedString("app_logout", comment: ""), message: NSLocalizedString("app_logout_desc", comment: ""), preferredStyle: .alert)
        let cancel = UIAlertAction(title: NSLocalizedString("cancel", comment: ""), style: .cancel)
        let sure = UIAlertAction(title: NSLocalizedString("app_logout", comment: ""), style: .default) { _ in
            self.logOut()
        }
        alertVC.addAction(sure)
        alertVC.addAction(cancel)
        present(alertVC, animated: true)
    }
    private func modifyAlertActionTextFont(text: String, font: UIFont, color: UIColor) -> NSAttributedString {
        let attributedString = NSAttributedString(string: NSLocalizedString(text, comment: ""), attributes: [
            NSAttributedString.Key.font: font,
            NSAttributedString.Key.foregroundColor: color
        ])
        return attributedString
    }
    
    private func logOut() {
        VLUserCenter.shared().logout()
        UIApplication.shared.delegate?.window??.configRootViewController()
    }
    private func signOut() {
        let params = ["userNo": VLUserCenter.user.userNo.isEmpty ? "" : VLUserCenter.user.userNo]
        VLAPIRequest.getURL(VLURLConfig.kURLPathDestroyUser, parameter: params, showHUD: true) { response in
            if response.code == 0 {
                self.logOut()
            }
        } failure: { _, _ in }
    }
}

extension VLMineAccountViewController: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        dataArray.count
    }
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! VLMineAccountCell
        cell.setupModel(model: dataArray[indexPath.row])
        if dataArray.count == 1 {
            cell.contentView.cornerRadius(16)
        } else {
            if indexPath.row == 0 {
                cell.contentView.cornerRadius(16)
                cell.contentView.layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
            } else if indexPath.row == dataArray.count - 1 {
                cell.contentView.cornerRadius(16)
                cell.contentView.layer.maskedCorners = [.layerMinXMaxYCorner, .layerMaxXMaxYCorner]
            }
        }
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let model = dataArray[indexPath.row]
        switch model.type {
        case .sign_out:
            let alertVC = UIAlertController(title: NSLocalizedString("app_logoff_account", comment: ""),
                                            message: NSLocalizedString("app_logooff_account_desc", comment: ""), preferredStyle: .alert)
            let cancel = UIAlertAction(title: NSLocalizedString("app_not_logoff", comment: ""), style: .default)
            let sure = UIAlertAction(title: NSLocalizedString("app_logoff", comment: ""), style: .destructive) { _ in
                self.signOut()
            }
            alertVC.addAction(sure)
            alertVC.addAction(cancel)
            present(alertVC, animated: true)
        }
    }
    
    func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        let footerView = UIView()
        footerView.addSubview(logoutButton)
        logoutButton.leadingAnchor.constraint(equalTo: footerView.leadingAnchor).isActive = true
        logoutButton.bottomAnchor.constraint(equalTo: footerView.bottomAnchor).isActive = true
        logoutButton.trailingAnchor.constraint(equalTo: footerView.trailingAnchor).isActive = true
        logoutButton.heightAnchor.constraint(equalToConstant: 48).isActive = true
        return footerView
    }
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        20
    }
    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        (tableView.frame.height - Screen.kNavHeight - 20 - 48) < 48 ? 48 : (tableView.frame.height - Screen.kNavHeight - 20 - 48)
    }
}


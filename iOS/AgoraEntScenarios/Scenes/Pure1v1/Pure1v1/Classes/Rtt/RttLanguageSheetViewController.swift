//
//  RttLanguageSheetViewController.swift
//  Pure1v1
//
//  Created by CYG on 2024/6/26.
//

import Foundation
import AgoraCommon

private let TableHeaderHeight: CGFloat = 58
private let TableFooterHeight: CGFloat = 100
private let TableRowHeight: CGFloat = 47

private let RttSheetCellID = "RttSheetCellID"

class RttLanguageSheetViewController: UIViewController {
    
    var defaultSelectedIndex: Int = 0
    var didSelectedIndex: ((_ index: Int)->())?
    var dataArray = [String]()

    private lazy var bgView: UIView = {
        let bgView = UIView()
        bgView.backgroundColor = .show_cover
        bgView.alpha = 0
        return bgView
    }()
    
    private lazy var headerView: RttSettingHeaderView = {
        let headerView = RttSettingHeaderView(frame: CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width, height: TableHeaderHeight))
        return headerView
    }()
    
    private lazy var footerView: RttSettingActionSheetFooterView = {
        let footerView = RttSettingActionSheetFooterView(frame: CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width, height: TableFooterHeight))
        footerView.button.addTarget(self, action: #selector(didClickCancelButton), for: .touchUpInside)
        return footerView
    }()

    private lazy var tableView: UITableView = {
        let tableView = UITableView()
        tableView.backgroundColor = .white
        tableView.delegate = self
        tableView.dataSource = self
        tableView.rowHeight = TableRowHeight
        tableView.tableFooterView = footerView
        tableView.tableHeaderView = headerView
        tableView.isScrollEnabled = true
        tableView.register(RttLanguageCell.self, forCellReuseIdentifier: RttSheetCellID)
        //tableView.clipsToBounds = false
        tableView.cornerRadius(20)
        return tableView
    }()
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        modalPresentationStyle = .overFullScreen
//        modalTransitionStyle = .crossDissolve
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        setUpUI()
    }
    
    private func setUpUI() {
        
        headerView.title = title
        
        view.addSubview(tableView)
        
        tableView.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
                tableView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
                tableView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
                tableView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
                tableView.bottomAnchor.constraint(equalTo: view.bottomAnchor)
            ])
        
        tableView.selectRow(at: IndexPath(row: defaultSelectedIndex, section: 0), animated: false, scrollPosition: .none)
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        //tableView.setRoundingCorners([.topLeft, .topRight], radius: 20)
    }
    
    @objc private func didClickCancelButton(){
        dismiss()
    }
}

extension RttLanguageSheetViewController {
    
    func showBgView(){
        UIView.animate(withDuration: 0.2) {
            self.bgView.alpha = 1
        }
    }
    
    private func dismiss() {
        UIView.animate(withDuration: 0.2) {
            self.bgView.alpha = 0
        } completion: { finish in
            self.dismiss(animated: true)
        }

    }
}

extension RttLanguageSheetViewController: UITableViewDelegate, UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return dataArray.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        let key = dataArray[indexPath.row]
        let cell = tableView.dequeueReusableCell(withIdentifier: RttSheetCellID, for: indexPath) as! RttLanguageCell
        cell.selectionStyle = .none
        cell.text = RttManager.shared.displayName(for: key)
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        didSelectedIndex?(indexPath.row)
        dismiss()
    }
}

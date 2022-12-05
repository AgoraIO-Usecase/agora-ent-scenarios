//
//  ShowSettingActionSheetVC.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/15.
//

import UIKit

private let TableHeaderHeight: CGFloat = 58
private let TableFooterHeight: CGFloat = 100
private let TableRowHeight: CGFloat = 47

private let ShowActionSheetCellID = "ShowActionSheetCellID"

class ShowSettingActionSheetVC: UIViewController {
    
    var defaultSelectedIndex: Int = 0
    var didSelectedIndex: ((_ index: Int)->())?
    var dataArray = [String]()

    private lazy var bgView: UIView = {
        let bgView = UIView()
        bgView.backgroundColor = .show_cover
        bgView.alpha = 0
        return bgView
    }()
    
    private lazy var headerView: ShowSettingHeaderView = {
        let headerView = ShowSettingHeaderView(frame: CGRect(x: 0, y: 0, width: Screen.width, height: TableHeaderHeight))
        return headerView
    }()
    
    private lazy var footerView: ShowSettingActionSheetFooterView = {
        let footerView = ShowSettingActionSheetFooterView(frame: CGRect(x: 0, y: 0, width: Screen.width, height: TableFooterHeight))
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
        tableView.isScrollEnabled = false
        tableView.register(ShowSettingActionSheetCell.self, forCellReuseIdentifier: ShowActionSheetCellID)
        return tableView
    }()
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        modalPresentationStyle = .overCurrentContext
//        modalTransitionStyle = .crossDissolve
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        setUpUI()
    }
    
    private func setUpUI(){
        
        view.addSubview(bgView)
        bgView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
        
        headerView.title = title
        
        view.addSubview(tableView)
        tableView.snp.makeConstraints { make in
            make.left.bottom.right.equalToSuperview()
            make.height.equalTo(TableRowHeight * CGFloat(dataArray.count) + TableFooterHeight + TableHeaderHeight)
        }
        
        tableView.selectRow(at: IndexPath(row: defaultSelectedIndex, section: 0), animated: false, scrollPosition: .none)
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        tableView.setRoundingCorners([.topLeft, .topRight], radius: 20)
    }
    
    @objc private func didClickCancelButton(){
        dismiss()
    }
    
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
       dismiss()
    }
}

extension ShowSettingActionSheetVC {
    
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

extension ShowSettingActionSheetVC: UITableViewDelegate, UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return dataArray.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        let text = dataArray[indexPath.row]
        let cell = tableView.dequeueReusableCell(withIdentifier: ShowActionSheetCellID, for: indexPath) as! ShowSettingActionSheetCell
        cell.selectionStyle = .none
        cell.text = text
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        didSelectedIndex?(indexPath.row)
        dismiss()
    }
}


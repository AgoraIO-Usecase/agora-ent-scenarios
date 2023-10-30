//
//  DropMenu.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/7/3.
//

import UIKit

protocol DropdownMenuDelegate: AnyObject {
    func didSelectItemAtIndex(index: Int)
}

class DropdownMenu: UIView, UITableViewDataSource, UITableViewDelegate {
    
    weak var delegate: DropdownMenuDelegate?
    
    private let tableView = UITableView()
    private var items: [String] = []
    private var selectIndex: Int = 0
    init(frame: CGRect, items: [String], selectIndex: Int) {
        super.init(frame: frame)
        
        self.items = items
        self.selectIndex = selectIndex
        tableView.dataSource = self
        tableView.delegate = self
        
        configureTableView()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func configureTableView() {
        addSubview(tableView)
        tableView.frame = bounds
        tableView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        tableView.tableFooterView = UIView()
        tableView.reloadData()
    }
    
    // MARK: - UITableViewDataSource
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return items.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = UITableViewCell(style: .default, reuseIdentifier: nil)
        cell.textLabel?.text = items[indexPath.row]
        cell.textLabel?.textAlignment = .center
        cell.textLabel?.textColor = indexPath.row == selectIndex ? .red : .black
        return cell
    }
    
    // MARK: - UITableViewDelegate
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        delegate?.didSelectItemAtIndex(index: indexPath.row)
        tableView.deselectRow(at: indexPath, animated: true)
        selectIndex = indexPath.row
        tableView.reloadData()
    }
}

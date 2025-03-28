//
//  AUITableView.swift
//  AUIKit
//
//  Created by wushengtao on 2023/4/1.
//

import Foundation

open class AUITableView: UITableView {
    public var aui_items: [AUITableViewItemProtocol] = [] {
        didSet {
            reloadData()
        }
    }
    
    public override init(frame: CGRect, style: UITableView.Style) {
        super.init(frame: frame, style: style)
        self.delegate = self
        self.dataSource = self
        self.theme_backgroundColor = "TableView.backgroundColor"
    }
    
    required public init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

extension AUITableView: UITableViewDataSource, UITableViewDelegate {
    public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        let count = aui_items.count
        return count
    }
    
    public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell: AUITableViewCell? = tableView.dequeueReusableCell(withIdentifier: "aui_cell") as? AUITableViewCell
        if cell == nil {
            cell = AUITableViewCell(style: .subtitle, reuseIdentifier: "aui_cell")
        }
        let item = aui_items[indexPath.row]
        cell?.item = item
        cell?.setNeedsLayout()
        return cell!
    }
    
    public func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        let item = aui_items[indexPath.row]
        item.onCellSelected?(indexPath)
    }
    
    public func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        let item = aui_items[indexPath.row]
        return AUITableViewCell.tableViewCellDefaultHeight(style: item.aui_style)
    }
}

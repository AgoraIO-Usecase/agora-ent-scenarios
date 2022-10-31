//
//  VMUserListView.swift
//  AgoraScene_iOS
//
//  Created by CP on 2022/9/7.
//

import UIKit

class VMUserListView: UIView {
    private var tableView: UITableView = .init()
    private var titleLabel: UILabel = .init()
    private var conBtn: UIButton = .init()
    private var audBtn: UIButton = .init()
    private var imgView: UIImageView = .init()
    private var conLabel: UILabel = .init()
    private var searchView: UIView = .init()
    private var serachImgView: UIImageView = .init()
    private var tf: UITextField = .init()
    override init(frame: CGRect) {
        super.init(frame: frame)
        backgroundColor = .white
        layoutUI()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private func layoutUI() {}
}

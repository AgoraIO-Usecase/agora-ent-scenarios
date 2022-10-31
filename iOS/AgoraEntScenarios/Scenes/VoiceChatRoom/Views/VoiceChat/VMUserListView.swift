//
//  VMUserListView.swift
//  AgoraScene_iOS
//
//  Created by CP on 2022/9/7.
//

import UIKit

class VMUserListView: UIView {

    private var tableView: UITableView = UITableView()
    private var titleLabel: UILabel = UILabel()
    private var conBtn: UIButton = UIButton()
    private var audBtn: UIButton = UIButton()
    private var imgView: UIImageView = UIImageView()
    private var conLabel: UILabel = UILabel()
    private var searchView: UIView = UIView()
    private var serachImgView: UIImageView = UIImageView()
    private var tf: UITextField = UITextField()
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.backgroundColor = .white
        layoutUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func layoutUI() {
        
    }
    

}

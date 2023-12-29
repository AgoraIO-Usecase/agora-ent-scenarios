//
//  ShowBeautyVenderVIew.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/11/6.
//

import UIKit

class ShowBeautyVenderView: UIView {
    var onSelectedBeautyVenderClosure: ((BeautyFactoryType) -> Void)?
    private lazy var tableView: UITableView = {
        let tableView = UITableView(frame: .zero, style: .plain)
        tableView.translatesAutoresizingMaskIntoConstraints = false
        tableView.delegate = self
        tableView.dataSource = self
        tableView.separatorStyle = .none
        tableView.register(ShowBeautyVenderViewCell.self, forCellReuseIdentifier: "beautyVenderCell")
        tableView.backgroundColor = UIColor(hex: "#18191B", alpha: 0.4)
        tableView.translatesAutoresizingMaskIntoConstraints = false
        tableView.layer.cornerRadius = 8
        tableView.layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        tableView.layer.masksToBounds = true
        return tableView
    }()
    private lazy var dataArray: [BeautyFactoryType] = BeautyFactoryType.allCases
    
    private var preCell: UITableViewCell?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {        
        addSubview(tableView)
        tableView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        tableView.topAnchor.constraint(equalTo: topAnchor).isActive = true
        tableView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        tableView.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
    }
}


extension ShowBeautyVenderView: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        dataArray.count
    }
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "beautyVenderCell", for: indexPath) as! ShowBeautyVenderViewCell
        let titles = dataArray.map({ $0.title })
        cell.setupTitle(title: titles[indexPath.row])
        if preCell == nil && indexPath.row == 1 {
            cell.isSelected = true
            preCell = cell
        }
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        preCell?.isSelected = false
        let cell = tableView.cellForRow(at: indexPath)
        cell?.isSelected = true
        preCell = cell
        let type = dataArray[indexPath.row]
        onSelectedBeautyVenderClosure?(type)
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        40
    }
}

class ShowBeautyVenderViewCell: UITableViewCell {
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.text = ""
        label.textColor = UIColor(hex: "#FFFFFF", alpha: 1.0)
        label.font = .systemFont(ofSize: 14)
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    private lazy var iconImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage.sceneImage(name: "show_beauty_vender_selected"))
        imageView.translatesAutoresizingMaskIntoConstraints = false
        imageView.isHidden = true
        return imageView
    }()
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override var isSelected: Bool {
        didSet {
            titleLabel.textColor = isSelected ? UIColor(hex: "#FFFFFF", alpha: 1.0) : UIColor(hex: "#FFFFFF", alpha: 0.6)
            iconImageView.isHidden = !isSelected
        }
    }
    
    func setupTitle(title: String) {
        titleLabel.text = title
    }
    
    private func setupUI() {
        selectionStyle = .none
        backgroundColor = .clear
        contentView.backgroundColor = .clear
        
        contentView.addSubview(titleLabel)
        contentView.addSubview(iconImageView)
        
        titleLabel.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 12).isActive = true
        titleLabel.centerYAnchor.constraint(equalTo: contentView.centerYAnchor).isActive = true
        
        iconImageView.centerYAnchor.constraint(equalTo: contentView.centerYAnchor).isActive = true
        iconImageView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -14).isActive = true
    }
}

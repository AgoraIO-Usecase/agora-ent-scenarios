//
//  RttLanguageCell.swift
//  Pure1v1
//
//  Created by CYG on 2024/6/26.
//

import UIKit
import Foundation

class RttLanguageCell: UITableViewCell {
    
    var text: String = "" {
        didSet{
            titleLabel.text = text
        }
    }

    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = .show_Ellipse6
        label.font = UIFont.boldSystemFont(ofSize: 16)
        return label
    }()
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        titleLabel.textColor = selected ? .show_zi03 : .show_Ellipse6
    }
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func createSubviews(){
        contentView.addSubview(titleLabel)
        titleLabel.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.centerY.equalToSuperview()
        }
    }
}

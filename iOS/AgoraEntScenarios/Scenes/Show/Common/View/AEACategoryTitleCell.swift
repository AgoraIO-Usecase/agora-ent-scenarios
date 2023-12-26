//
//  AEACategoryTitleCell.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/7/18.
//

import Foundation
import UIKit

class AEACategoryTitleCell: UICollectionViewCell {
    var title: String? {
        didSet {
            titleLabel.text = title
            titleLabel.textColor = isSelected ? titleSelectedColor : titleColor
            titleLabel.font = isSelected ? titleSelectedFont : titleFont
        }
    }
    
    var titleFont: UIFont?
    var titleSelectedFont: UIFont?
    var titleColor: UIColor?
    var titleSelectedColor: UIColor?
    var indicator: UIView?
    
    private var titleLabel: UILabel!
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        titleLabel = UILabel()
        titleLabel.textColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.5)
        titleLabel.font = UIFont.systemFont(ofSize: 16, weight: .medium)
        titleLabel.textAlignment = .center
        contentView.addSubview(titleLabel)
        
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        
        NSLayoutConstraint.activate([
            titleLabel.leftAnchor.constraint(equalTo: contentView.leftAnchor, constant: 5),
            titleLabel.rightAnchor.constraint(equalTo: contentView.rightAnchor, constant: -5),
            titleLabel.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 5),
            titleLabel.bottomAnchor.constraint(equalTo: contentView.bottomAnchor, constant: -5)
        ])
    }
    
    override var isSelected: Bool {
        didSet {
            titleLabel.textColor = isSelected ? titleSelectedColor : titleColor
            titleLabel.font = isSelected ? titleSelectedFont : titleFont
        }
    }
}

//
//  LoadingButton.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/8/9.
//

import UIKit

class LoadingButton: UIButton {
    var isLoading: Bool = false {
        didSet {
            if isLoading {
                activivityView.startAnimating()
            } else {
                activivityView.stopAnimating()
            }
        }
    }
    private lazy var activivityView: UIActivityIndicatorView = {
        let view = UIActivityIndicatorView(style: .white)
        view.aui_size = CGSize(width: 20, height: 20)
        return view
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubView()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func _loadSubView() {
        addSubview(activivityView)
        
        activivityView.snp.makeConstraints { make in
            make.height.equalTo(titleLabel!.snp.height)
            make.width.equalTo(titleLabel!.snp.height)
            make.centerY.equalTo(titleLabel!)
            make.right.equalTo(titleLabel!.snp.left).offset(-4)
        }
    }
}

//
//  AEACategoryView.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/7/18.
//

import Foundation
import UIKit

class AEACategoryItem: NSObject {
    var normalImage: UIImage?
    var selectedImage: UIImage?
    
    class func item(withNormalImage normalImage: UIImage?, selectedImage: UIImage?) -> AEACategoryItem {
        let item = AEACategoryItem()
        item.selectedImage = selectedImage
        item.normalImage = normalImage
        return item
    }
}

protocol AEACategoryViewDelegate: AnyObject {
    func categoryView(_ categoryView: AEACategoryView, didSelectItemat index: Int)
}

class AEACategoryViewLayout: NSObject {
    var itemSize: CGSize = .zero
    var minSpacing: CGFloat = 0
    var contentInsets: UIEdgeInsets = .zero
    
    class func defaultLayout() -> AEACategoryViewLayout {
        let layout = AEACategoryViewLayout()
        let itemWidth: CGFloat = 40
        let insets = UIEdgeInsets(top: 5, left: 25, bottom: 5, right: 25)
        let spacing = (UIScreen.main.bounds.size.width - insets.left - insets.right - itemWidth * 5) / 4
        layout.itemSize = CGSize(width: itemWidth, height: itemWidth)
        layout.minSpacing = spacing
        layout.contentInsets = insets
        return layout
    }
}

class AEACategoryView: UIView, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    private let kTitleCellID = "AEACategoryTitleCell"
    private var selectedIndex: Int = 0
    
    weak var delegate: AEACategoryViewDelegate?
    var defaultSelectedIndex: Int = 0 {
        didSet {
            selectedIndex = defaultSelectedIndex
            
            if collectionView.numberOfItems(inSection: 0) > defaultSelectedIndex {
                let indexPath = IndexPath(item: defaultSelectedIndex, section: 0)
                collectionView.selectItem(at: indexPath, animated: false, scrollPosition: .top)
            }
        }
    }
    
    var showBottomLine: Bool = false {
        didSet {
            lineView.isHidden = !showBottomLine
        }
    }
    
    var titles: [String] = [] {
        didSet {
            dataArray = titles
            collectionView.reloadData()
        }
    }
    
    var items: [AEACategoryItem] = [] {
        didSet {
            dataArray = items
            collectionView.reloadData()
        }
    }
    
    var titleFont: UIFont?
    var titleSelectedFont: UIFont?
    var titleColor: UIColor?
    var titleSelectedColor: UIColor?
    var indicator: UIView? {
        didSet {
            if let indicator = indicator {
                addSubview(indicator)
            }
        }
    }
    
    private var collectionView: UICollectionView!
    private var lineView: UIView!
    private var dataArray: [Any] = []
    private var categoryLayout: AEACategoryViewLayout!
    
    override func layoutSubviews() {
        super.layoutSubviews()
        scrollIndicatorToSelectedIndexAnimated(false)
    }
    
    convenience init(defaultLayout: AEACategoryViewLayout) {
        self.init(frame: .zero)
        createSubviews(with: defaultLayout)
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func createSubviews(with categoryLayout: AEACategoryViewLayout) {
        self.categoryLayout = categoryLayout
        backgroundColor = .white
        
        let layout = UICollectionViewFlowLayout()
        layout.scrollDirection = .horizontal
        layout.itemSize = categoryLayout.itemSize
        layout.minimumLineSpacing = categoryLayout.minSpacing
        layout.sectionInset = categoryLayout.contentInsets
        
        collectionView = UICollectionView(frame: .zero, collectionViewLayout: layout)
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.backgroundColor = .white
        collectionView.showsHorizontalScrollIndicator = false
        collectionView.clipsToBounds = true
        collectionView.register(AEACategoryTitleCell.self, forCellWithReuseIdentifier: kTitleCellID)
        
        lineView = UIView()
        lineView.backgroundColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.1)
        
        addSubview(collectionView)
        addSubview(lineView)
        
        collectionView.translatesAutoresizingMaskIntoConstraints = false
        lineView.translatesAutoresizingMaskIntoConstraints = false
        
        NSLayoutConstraint.activate([
            collectionView.topAnchor.constraint(equalTo: topAnchor),
            collectionView.leftAnchor.constraint(equalTo: leftAnchor, constant: 0),
            collectionView.rightAnchor.constraint(equalTo: rightAnchor, constant: 0),
            collectionView.bottomAnchor.constraint(equalTo: bottomAnchor),
            collectionView.heightAnchor.constraint(equalToConstant: categoryLayout.itemSize.height + 14),
            
            lineView.bottomAnchor.constraint(equalTo: bottomAnchor),
            lineView.leftAnchor.constraint(equalTo: collectionView.leftAnchor),
            lineView.rightAnchor.constraint(equalTo: collectionView.rightAnchor),
            lineView.heightAnchor.constraint(equalToConstant: 1)
        ])
    }
    
    func setIndicator(_ indicator: UIView?) {
        self.indicator = indicator
    }
    
    func scrollIndicatorToSelectedIndexAnimated(_ animated: Bool) {
        guard let cell = collectionView.cellForItem(at: IndexPath(item: selectedIndex, section: 0)) else {
            let defaultSelectedCellCenterX = (categoryLayout.contentInsets.left + CGFloat(selectedIndex + 1) * categoryLayout.itemSize.width + categoryLayout.minSpacing * CGFloat(selectedIndex)) * 0.5
            indicator?.center = CGPoint(x: defaultSelectedCellCenterX, y: bounds.size.height - (indicator?.bounds.size.height ?? 0))
            return
        }
        
        let changeCenter: () -> Void = {
            self.indicator?.center = CGPoint(x: cell.center.x, y: self.bounds.size.height - (self.indicator?.bounds.size.height ?? 0))
        }
        
        if animated {
            UIView.animate(withDuration: 0.2, animations: {
                changeCenter()
            })
        } else {
            changeCenter()
        }
    }
    
    // MARK: - UICollectionViewDataSource & UICollectionViewDelegateFlowLayout
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return dataArray.count
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let data = dataArray[indexPath.item]
        
        let titleCell = collectionView.dequeueReusableCell(withReuseIdentifier: kTitleCellID, for: indexPath) as! AEACategoryTitleCell
        titleCell.titleFont = titleFont
        titleCell.titleSelectedFont = titleSelectedFont
        titleCell.titleColor = titleColor
        titleCell.titleSelectedColor = titleSelectedColor
        
        if let title = data as? String {
            titleCell.title = title
        }
        
        return titleCell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        delegate?.categoryView(self, didSelectItemat: indexPath.item)
        
        collectionView.scrollToItem(at: indexPath, at: .centeredHorizontally, animated: true)
        selectedIndex = indexPath.item
        scrollIndicatorToSelectedIndexAnimated(true)
    }
}

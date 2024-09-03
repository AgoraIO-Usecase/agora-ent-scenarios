//
//  PageContainerTitleBar.swift
//  AIChat
//
//  Created by 朱继超 on 2024/8/27.
//

import UIKit
import ZSwiftBaseLib

@objcMembers open class PageContainerTitleBar: UIView {
    
    var datas: [ChoiceItem] = []
    
    var chooseClosure: ((Int)->())?
        
    lazy var indicator: UIView = {
        UIView(frame: CGRect(x: 16+(self.frame.width-32)/2.0-8, y: self.frame.height-4, width: 18, height: 2)).cornerRadius(1).backgroundColor(UIColor(0x009FFF))
    }()
    
    lazy var layout: UICollectionViewFlowLayout = {
        let flow = UICollectionViewFlowLayout()
        flow.scrollDirection = .horizontal
        flow.itemSize = CGSize(width: 85, height: self.frame.height-16)
        flow.minimumInteritemSpacing = 12
        flow.minimumLineSpacing = 0
        return flow
    }()
    
    lazy var choicesBar: UICollectionView = {
        UICollectionView(frame: CGRect(x: 16, y: 8, width: self.frame.width-32, height: self.frame.height-16), collectionViewLayout: self.layout).dataSource(self).delegate(self).registerCell(ChoiceItemCell.self, forCellReuseIdentifier: NSStringFromClass(ChoiceItemCell.self)).showsHorizontalScrollIndicator(false).backgroundColor(.clear)
    }()

    override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    /**
     A convenience initializer for creating a `PageContainerTitleBar` instance with the specified frame, choices, and selected closure.

     - Parameters:
        - frame: The frame rectangle for the view, measured in points.
        - choices: An array of strings representing the choices to be displayed in the title bar.
        - selectedClosure: A closure that will be called when a choice is selected, passing the index of the selected choice as an argument.

     - Returns: A new `PageContainerTitleBar` instance.
     */
    @objc public init(frame: CGRect, choices: [String], selectedClosure: @escaping (Int)->()) {
        self.chooseClosure = selectedClosure
        self.datas = choices.map({ ChoiceItem(text: $0,selected: false) })
        super.init(frame: frame)
        self.backgroundColor = .white
        self.datas.first?.selected = true
        self.addSubViews([self.indicator,self.choicesBar])
        self.choicesBar.bounces = false
        self.scrollIndicator(to: 0)
    }
    
    required public init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
}

extension PageContainerTitleBar: UICollectionViewDataSource, UICollectionViewDelegate {
    public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        self.datas.count
    }
    
    // MARK: - UICollectionViewDelegate
    public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        guard let cell = collectionView.dequeueReusableCell(withReuseIdentifier: NSStringFromClass(ChoiceItemCell.self), for: indexPath) as? ChoiceItemCell else {
            return ChoiceItemCell()
        }
        cell.refresh(item: self.datas[indexPath.row])
        return cell
    }
    
    public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        self.scrollIndicator(to: indexPath.row)
        for item in self.datas {
            item.selected = false
        }
        self.datas[safe: indexPath.row]?.selected = true
        collectionView.reloadData()
        self.chooseClosure?(indexPath.row)
    }
    
    @objc public func scrollIndicator(to index: Int) {
        for item in self.datas {
            item.selected = false
        }
        self.datas[safe: index]?.selected = true
        self.choicesBar.reloadData()
        let cellFrame = self.getCellFramesInSuperview(indexPath: IndexPath(row: index, section: 0))
        UIView.animate(withDuration: 0.25) {
            self.indicator.frame = CGRect(x: cellFrame.minX+(cellFrame.width/2-9), y: self.frame.height-2, width: 18, height: 2)
        }
    }


    private func getCellFramesInSuperview(indexPath: IndexPath) -> CGRect {
        
        guard let superview = self.choicesBar.superview else {
            print("CollectionView doesn't have a superview")
            return .zero
        }
        
        // 获取cell在collectionView中的frame
        let cellFrame = self.choicesBar.layoutAttributesForItem(at: indexPath)?.frame ?? .zero
        
        // 将cell的frame转换到superview的坐标系
        let frameInSuperview = self.choicesBar.convert(cellFrame, to: superview)
        
        return frameInSuperview
    }

}


@objcMembers open class ChoiceItemCell: UICollectionViewCell {
    
    lazy var content: UILabel = {
        UILabel(frame: CGRect(x: 0, y: 0, width: self.contentView.frame.width, height: self.contentView.frame.height)).textAlignment(.center).backgroundColor(.clear)
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.contentView.backgroundColor = .clear
        self.backgroundColor = .clear
        self.contentView.addSubview(self.content)
    }
    
    required public init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        self.content.frame = CGRect(x: 0, y: 0, width: self.contentView.frame.width, height: self.contentView.frame.height)
    }
    
    public func refresh(item: ChoiceItem) {
        self.content.text = item.text
        if !item.selected {
            self.content.textColor = UIColor(0x6C7192)
            self.content.font = .systemFont(ofSize: 12, weight: .regular)
        } else {
            self.content.textColor = .black
            self.content.font = .systemFont(ofSize: 16, weight: .semibold)
        }
    }
}


open class ChoiceItem: NSObject {
    var text: String
    var selected: Bool
    
    init(text: String, selected: Bool = false) {
        self.text = text
        self.selected = selected
    }
}

/// Choice layout
@objcMembers open class ChoiceItemLayout: UICollectionViewFlowLayout {
    
    internal var center: CGPoint!
    internal var rows: Int!
    
    
    private var deleteIndexPaths: [IndexPath]?
    private var insertIndexPaths: [IndexPath]?
    
    public override func prepare() {
        let size = self.collectionView?.frame.size ?? .zero
        self.rows = self.collectionView?.numberOfItems(inSection: 0) ?? 0
        self.center = CGPoint(x: size.width / 2, y: size.height / 2)
    }
    
    
    public override func layoutAttributesForItem(at indexPath: IndexPath) -> UICollectionViewLayoutAttributes? {
        //Calculate per item center
        let attributes = UICollectionViewLayoutAttributes(forCellWith: indexPath)
        attributes.size = self.itemSize
        if self.rows == 1 {
            attributes.center = self.center
        }
        return attributes
    }
    
    public override func layoutAttributesForElements(in rect: CGRect) -> [UICollectionViewLayoutAttributes]? {
        
        var attributesArray = [UICollectionViewLayoutAttributes]()
        for index in 0 ..< self.rows {
            let indexPath = IndexPath(item: index, section: 0)
            attributesArray.append(self.layoutAttributesForItem(at:indexPath)!)
        }
        return attributesArray
    }
    
    
    
    public override func prepare(forCollectionViewUpdates updateItems: [UICollectionViewUpdateItem]) {
        self.deleteIndexPaths = [IndexPath]()
        self.insertIndexPaths = [IndexPath]()
        
        for updateItem in updateItems {
            if updateItem.updateAction == UICollectionViewUpdateItem.Action.delete {
                guard let indexPath = updateItem.indexPathBeforeUpdate else { return }
                self.deleteIndexPaths?.append(indexPath)
            } else if updateItem.updateAction == UICollectionViewUpdateItem.Action.insert {
                guard let indexPath = updateItem.indexPathAfterUpdate else { return }
                self.insertIndexPaths?.append(indexPath)
            }
        }
        
    }
    
    public override func finalizeCollectionViewUpdates() {
        super.finalizeCollectionViewUpdates()
        self.deleteIndexPaths = nil
        self.insertIndexPaths = nil
    }
    
    public override func initialLayoutAttributesForAppearingItem(at itemIndexPath: IndexPath) -> UICollectionViewLayoutAttributes? {
        //Appear animation
        var attributes = super.initialLayoutAttributesForAppearingItem(at: itemIndexPath)
        
        if self.insertIndexPaths?.contains(itemIndexPath) ?? false {
            if attributes != nil {
                attributes = self.layoutAttributesForItem(at: itemIndexPath)
                attributes?.alpha = 0.0
                attributes?.center = CGPointMake(self.center.x, self.center.y)
            }
        }
        
        
        return attributes
    }
    
    public override func finalLayoutAttributesForDisappearingItem(at itemIndexPath: IndexPath) -> UICollectionViewLayoutAttributes? {
        // Disappear animation
        var attributes = super.finalLayoutAttributesForDisappearingItem(at: itemIndexPath)
        
        if self.deleteIndexPaths?.contains(itemIndexPath) ?? false {
            if attributes != nil {
                attributes = self.layoutAttributesForItem(at: itemIndexPath)
                
                attributes?.alpha = 0.0
                attributes?.center = CGPointMake(self.center.x, self.center.y)
                attributes?.transform3D = CATransform3DMakeScale(0.1, 0.1, 1.0)
            }
        }
        
        return attributes
    }
    
    

}



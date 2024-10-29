//
//  AUIMoreOperationView.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/6/7.
//

import UIKit
/*!
 *  \~Chinese
 *  更多功能视图事件回调
 *
 *  \~English
 *  View's events delegate.
 *
 */
@objc public protocol AUIMoreOperationViewEventsDelegate: NSObjectProtocol {
    func onItemSelected(entity: AUIMoreOperationCellDataProtocol)
}
/*!
 *  \~Chinese
 *  更多功能视图
 *
 *  \~English
 *  More function container Collection
 *
 */
public final class AUIMoreOperationView: UIView {
    
    private var eventHandlers: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    
    /// Description 添加UI事件监听代理
    /// - Parameter actionHandler: 代理对象
    /// Description Add UI event listening agent
    /// - Parameter actionHandler: proxy object
    public func addActionHandler(actionHandler: AUIMoreOperationViewEventsDelegate) {
        if self.eventHandlers.contains(actionHandler) {
            return
        }
        self.eventHandlers.add(actionHandler)
    }

    
    /// Description 移除UI事件监听代理
    /// - Parameter actionHandler: 代理对象
    /// Description Remove UI event listening agent
    /// - Parameter actionHandler: proxy object
    public func removeEventHandler(actionHandler: AUIMoreOperationViewEventsDelegate) {
        self.eventHandlers.remove(actionHandler)
    }
    
    
    public var datas = [AUIMoreOperationCellDataProtocol]()
    
    private lazy var flowLayout: UICollectionViewFlowLayout = {
        let layout = UICollectionViewFlowLayout()
        layout.itemSize = CGSize(width: 66, height: 92)
        layout.minimumLineSpacing = 8
        layout.minimumInteritemSpacing = 26
        return layout
    }()
    
    private lazy var collection: UICollectionView = {
        UICollectionView(frame: CGRect(x: 24, y: 0, width: self.frame.width-48, height: self.frame.height-CGFloat(ABottomBarHeight)), collectionViewLayout: self.flowLayout).delegate(self).dataSource(self).backgroundColor(.clear).showsVerticalScrollIndicator(false).registerCell(AUIMoreOperationCell.self, forCellReuseIdentifier: "AUIMoreOperationCell")
    }()

    public override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    @objc public convenience init(frame: CGRect,datas: [AUIMoreOperationCellDataProtocol]) {
        self.init(frame: frame)
        self.datas = datas
        self.addSubview(self.collection)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
}

extension AUIMoreOperationView: UICollectionViewDelegate, UICollectionViewDataSource {
    
    public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return self.datas.count
    }
    
    public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "AUIMoreOperationCell", for: indexPath) as? AUIMoreOperationCell
        if let info = self.datas[safe: indexPath.row] {
            cell?.refresh(info: info)
        }
        return cell ?? AUIMoreOperationCell()
    }
    
    public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        collectionView.deselectItem(at: indexPath, animated: true)
        if let info = self.datas[safe: indexPath.row] {
            info.showRedDot = !info.showRedDot
            collectionView.reloadData()
            self.eventHandlers.allObjects.forEach {
                $0.onItemSelected(entity: info)
            }

        }
    }
    
}

//
//  AUITabsPageContainer.swift
//  AUiKit
//
//  Created by 朱继超 on 2023/5/22.
//

import UIKit
import SwiftTheme

@objc public protocol AUITabsPageContainerCellDelegate: NSObjectProtocol {
        
    
    /// Description 获取当前容器视图类名作为视图唯一标识
    /// - Returns: 类名
    func viewIdentity() -> String
    
    /// Description 根据协议类名创建用户容器类
    /// - Returns: 继承UIView的容器
    func create(frame: CGRect,datas: [NSObject]) -> UIView?
    
    /// Description 容器类的布局信息
    /// - Returns: frame
    func rawFrame() -> CGRect
    
    /// Description 容器类的数据源数组
    /// - Returns: 数据源数组
    func rawDatas() -> [NSObject]
}



public class AUITabsPageContainer: UIView {
    
    private var titles = [String]()
    
    private var tabStyle = AUITabsStyle()
    
    private var containers = [AUITabsPageContainerCellDelegate]()
            
    private lazy var tabs: AUITabs = {
        self.tabStyle.indicatorHeight = 4
        self.tabStyle.indicatorWidth = 28
        self.tabStyle.indicatorCornerRadius = 2
        self.tabStyle.indicatorStyle = .line
        self.tabStyle.indicatorColor = UIColor(0x009EFF)
        self.tabStyle.selectedTitleColor = UIColor(0x171a1c)
        self.tabStyle.normalTitleColor = UIColor(0xACB4B9)
        self.tabStyle.titleFont = .systemFont(ofSize: 14, weight: .semibold)
        self.tabStyle.alignment = .left
        let tab = AUITabs(frame: CGRect(x: 0, y: 24, width: self.frame.width, height: 44), segmentStyle: self.tabStyle, titles: self.titles).backgroundColor(.clear)
        tab.theme_selectedTitleColor = AUIColor("SendGift.tabTitleNormalColor")
        tab.theme_normalTitleColor = AUIColor("SendGift.tabTitleNormalColor")
        return tab
    }()
    
    private lazy var layout: UICollectionViewFlowLayout = {
        let flow = UICollectionViewFlowLayout()
        flow.scrollDirection = .horizontal
        flow.itemSize = CGSize(width: self.frame.width, height: self.frame.height - self.tabs.frame.maxY)
        flow.minimumLineSpacing = 0
        flow.minimumInteritemSpacing = 0
        return flow
    }()
    
    private lazy var container: UICollectionView = {
        UICollectionView(frame: CGRect(x: 0, y: self.tabs.frame.maxY, width: AScreenWidth, height: self.frame.height - self.tabs.frame.maxY), collectionViewLayout: self.layout).registerCell(AUITabsPageContainerCell.self, forCellReuseIdentifier: "AUITabsPageContainerCell").backgroundColor(.clear).delegate(self).dataSource(self).showsHorizontalScrollIndicator(false).showsHorizontalScrollIndicator(false)
    }()
    
    lazy var gradient: UIImageView = {
        UIImageView(frame: CGRect(x: 0, y: Int(self.frame.height)-ABottomBarHeight-40, width: Int(self.frame.width), height: ABottomBarHeight+40)).backgroundColor(.clear)
    }()
    
    public convenience init(frame: CGRect,barStyle: AUITabsStyle,containers: [AUITabsPageContainerCellDelegate],titles: [String]) {
        self.init(frame: frame)
        self.tabStyle = barStyle
        self.containers = containers
        self.titles = titles
        self.addSubViews([self.tabs,self.container,self.gradient])
        self.gradient.image = UIImage.aui_Image(named: "mask")
        self.container.bounces = false
        self.tabs.valueChange = {
            self.container.scrollToItem(at: IndexPath(row: $0, section: 0), at: .centeredHorizontally, animated: true)
        }
    }
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
}

extension AUITabsPageContainer: UICollectionViewDataSource,UICollectionViewDelegate {
    public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        self.containers.count
    }
    
    public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        guard let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "AUITabsPageContainerCell", for: indexPath) as? AUITabsPageContainerCell  else { return AUITabsPageContainerCell() }
        guard let container = self.containers[safe: indexPath.row] else { return AUITabsPageContainerCell() }
        
        let identity = container.viewIdentity()
        if cell.identity != identity {
            cell.willRenderContainer(displayView: container.create(frame: container.rawFrame(), datas: container.rawDatas()), identity: identity)
        }
        return cell
    }
    
    public func collectionView(_ collectionView: UICollectionView, didEndDisplaying cell: UICollectionViewCell, forItemAt indexPath: IndexPath) {
        if self.containers.count > 10 {
            for index in 0...self.containers.count {
                if index < indexPath.row - 1 || index > indexPath.row + 1 {
                    guard let cell = collectionView.cellForItem(at: IndexPath(row: index, section: indexPath.section)) as? AUITabsPageContainerCell else { continue }
                    cell.view = nil
                    cell.identity = ""
                }
            }
        }
    }
    
    public func scrollViewDidScroll(_ scrollView: UIScrollView) {
        let index = scrollView.contentOffset.x/AScreenWidth
        self.tabs.setSelectIndex(index: Int(index))
    }
    
}


class AUITabsPageContainerCell: UICollectionViewCell {
    
    
    var identity: String = ""
    
    var view: UIView?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
    }
    
    func willRenderContainer(displayView: UIView?,identity: String) {
        self.identity = identity
        self.view?.removeFromSuperview()
        self.view = nil
        self.view = displayView
        self.renderContainer()
    }
    
    func renderContainer() {
        guard let container = self.view else { return }
        self.contentView.addSubview(container)
        
        self.view?.bottomAnchor.constraint(equalTo: self.contentView.bottomAnchor).isActive = true
        self.view?.leftAnchor.constraint(equalTo: self.contentView.leftAnchor).isActive = true
        self.view?.rightAnchor.constraint(equalTo: self.contentView.rightAnchor).isActive = true
        self.view?.topAnchor.constraint(equalTo: self.contentView.topAnchor).isActive = true
    }
    
    
    override func prepareForReuse() {
        super.prepareForReuse()
        self.view?.removeFromSuperview()
        self.view = nil
    }
    

}

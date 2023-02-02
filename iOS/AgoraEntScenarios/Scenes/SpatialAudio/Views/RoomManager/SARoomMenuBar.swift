//
//  VRRoomMenuBar.swift
//  Pods-VoiceRoomBaseUIKit_Example
//
//  Created by 朱继超 on 2022/8/24.
//

import UIKit
import ZSwiftBaseLib

public final class SARoomMenuBar: UIView {
    var selectClosure: ((IndexPath) -> Void)?

    static let menusMap = [["title": sceneLocalized("All"), "detail": "", "selected": true, "soundType": ""],
                           ["title": sceneLocalized("Standard"), "detail": "", "selected": false, "soundType": ""],
                           ["title": sceneLocalized("Spatial Audio"), "detail": "", "selected": false, "soundType": ""]]

    static let menusMap1 = [["title": sceneLocalized("Chat Room"), "detail": "", "selected": false, "soundType": 0],
                            ["title": sceneLocalized("Spatial Audio Mode Room"), "detail": "", "selected": false, "soundType": 0]]

    private var indicatorImage = UIImage()

    private var indicatorFrame = CGRect.zero

    var dataSource = [SARoomMenuBarEntity]() {
        willSet {
            DispatchQueue.main.async { self.menuList.reloadData() }
        }
    }

    lazy var flowLayout: UICollectionViewFlowLayout = {
        let layout = UICollectionViewFlowLayout()
        layout.scrollDirection = .horizontal
        layout.minimumInteritemSpacing = 30
        layout.minimumLineSpacing = 0
        return layout
    }()

    lazy var menuList: UICollectionView = .init(frame: CGRect(x: 0, y: 0, width: self.frame.width, height: self.frame.height), collectionViewLayout: self.flowLayout).delegate(self).dataSource(self).showsHorizontalScrollIndicator(false).showsVerticalScrollIndicator(false).registerCell(SARoomMenuBarCell.self, forCellReuseIdentifier: "VRRoomMenuBarCell").backgroundColor(.clear)

    lazy var indicator: UIImageView = .init(frame: self.indicatorFrame).contentMode(.scaleAspectFit).image(self.indicatorImage)

    var widthMap: [String: CGFloat]? = [:]

    override public init(frame: CGRect) {
        super.init(frame: frame)
    }

    convenience init(frame: CGRect, items: [SARoomMenuBarEntity], indicatorImage: UIImage, indicatorFrame: CGRect) {
        self.init(frame: frame)
        self.indicatorFrame = indicatorFrame
        dataSource.append(contentsOf: items)
        self.indicatorImage = indicatorImage
        addSubview(menuList)
        menuList.addSubview(indicator)
        refreshSelected(indexPath: IndexPath(row: 0, section: 0))
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

extension SARoomMenuBar: UICollectionViewDelegate, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    static var entities: [SARoomMenuBarEntity] {
        var items = [SARoomMenuBarEntity]()
        do {
            for map in SARoomMenuBar.menusMap {
                let data = try JSONSerialization.data(withJSONObject: map, options: [])
                let item = try JSONDecoder().decode(SARoomMenuBarEntity.self, from: data)
                items.append(item)
            }
        } catch {
            print("\(error.localizedDescription)")
        }
        return items
    }

    static var entities1: [SARoomMenuBarEntity] {
        var items = [SARoomMenuBarEntity]()
        do {
            for map in SARoomMenuBar.menusMap1 {
                let data = try JSONSerialization.data(withJSONObject: map, options: [])
                let item = try JSONDecoder().decode(SARoomMenuBarEntity.self, from: data)
                items.append(item)
            }
        } catch {
            assertionFailure("\(error.localizedDescription)")
        }
        return items
    }

    public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        dataSource.count
    }

    public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "VRRoomMenuBarCell", for: indexPath) as? SARoomMenuBarCell
        cell?.render(dataSource[safe: indexPath.row] ?? SARoomMenuBarEntity())
        if cell?.item?.selected == true, cell != nil {
            indicatorMove(cell!)
        }
        return cell!
    }

    public func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        if let item = dataSource[safe: indexPath.row] {
            let width = (item.title + item.detail).z.sizeWithText(font: item.selected == true ? SARoomMenuBarCell.selectedFont : SARoomMenuBarCell.normalFont, size: CGSize(width: 999, height: 18)).width
            return CGSize(width: width, height: frame.height)
        } else {
            return .zero
        }
    }

    public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        collectionView.deselectItem(at: indexPath, animated: true)
        refreshSelected(indexPath: indexPath)
        if selectClosure != nil {
            selectClosure!(indexPath)
        }
    }

    func refreshSelected(indexPath: IndexPath) {
        dataSource.forEach { $0.selected = false }
        let item = dataSource[safe: indexPath.row] ?? SARoomMenuBarEntity()
        item.selected = !item.selected
        menuList.reloadData()
        menuList.scrollToItem(at: indexPath, at: .right, animated: true)
        if let cell = menuList.dequeueReusableCell(withReuseIdentifier: "VRRoomMenuBarCell", for: indexPath) as? SARoomMenuBarCell {
            indicatorMove(cell)
        }
    }

    func indicatorMove(_ cell: UICollectionViewCell) {
        UIView.animate(withDuration: 0.35) {
            self.indicator.center = CGPoint(x: cell.center.x, y: self.indicator.center.y)
        }
    }
}

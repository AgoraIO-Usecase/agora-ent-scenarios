//
//  HorizontalCardsView.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/25.
//

import UIKit

public protocol HorizontalCardsDataSource {
    
    func horizontalCardsViewNumberOfItems(_: HorizontalCardsView) -> Int
    
    func horizontalCardsView(_: HorizontalCardsView, viewForIndex index: Int) -> HorizontalCardView
}

public protocol HorizontalCardsDelegate {
    func horizontalCardsView(_: HorizontalCardsView, didSelectItemAtIndex index: Int)
    
    func horizontalCardsView(_: HorizontalCardsView, scrollIndex: Int)
}

public class HorizontalCardsView: UIView, UICollectionViewDelegate, UICollectionViewDataSource {

    private let flowLayout = UICollectionViewFlowLayout()
    var collectionView: UICollectionView!
    private let reuseIdentifier = "horizontalCardCell"

    private var indexOfCellBeforeDragging = 0

    public var dataSource: HorizontalCardsDataSource!

    public var delegate: HorizontalCardsDelegate?

    private var viewsCount: Int {
        self.dataSource.horizontalCardsViewNumberOfItems(self)
    }

    private var cardSize: CGSize {
        get { self.flowLayout.itemSize }
        set { self.flowLayout.itemSize = newValue }
    }

    public var cardWidthFactor: CGFloat = 0.8

    public var cardSpacing: CGFloat {
        get { self.flowLayout.minimumLineSpacing }
        set { self.flowLayout.minimumLineSpacing = newValue }
    }

    public var insets: UIEdgeInsets {
        get { self.flowLayout.sectionInset }
        set { self.flowLayout.sectionInset = newValue }
    }

    public required init?(coder: NSCoder) {
        super.init(coder: coder)
        self.initCollectionView()
    }

    override public init(frame: CGRect) {
        super.init(frame: frame)
        self.initCollectionView()
    }

    private func initCollectionView() {
        self.flowLayout.scrollDirection = .horizontal

        self.collectionView = UICollectionView(frame: bounds, collectionViewLayout: flowLayout)
        self.collectionView.dataSource = self
        self.collectionView.delegate = self
        self.collectionView.backgroundColor = .clear
        self.collectionView.showsVerticalScrollIndicator = false
        self.collectionView.showsHorizontalScrollIndicator = false
        self.collectionView.register(HorizontalCardCell.self, forCellWithReuseIdentifier: reuseIdentifier)

        self.addSubview(self.collectionView)

        self.collectionView.translatesAutoresizingMaskIntoConstraints = false
        self.collectionView.leftAnchor.constraint(equalTo: leftAnchor, constant: 0).isActive = true
        self.collectionView.rightAnchor.constraint(equalTo: rightAnchor, constant: 0).isActive = true
        self.collectionView.topAnchor.constraint(equalTo: topAnchor, constant: 0).isActive = true
        self.collectionView.bottomAnchor.constraint(equalTo: bottomAnchor, constant: 0).isActive = true
    }

    override public func layoutSubviews() {
        super.layoutSubviews()
        self.setCardSize()
    }

    public func reloadData() {
        self.setCardSize()
        self.collectionView.reloadData()
    }

    
    private func setCardSize() {
        let singleCardWidth = bounds.width - self.insets.left - self.insets.right
        let multiCardsWidth = bounds.width * self.cardWidthFactor
        let cardWidth = self.viewsCount > 1 ? multiCardsWidth : singleCardWidth
        let cardHeght = self.collectionView.bounds.height - self.insets.top - self.insets.bottom
        self.cardSize = CGSize(width: cardWidth, height: cardHeght)
    }

    public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        self.viewsCount
    }

    public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: reuseIdentifier, for: indexPath) as! HorizontalCardCell
        let view = self.dataSource.horizontalCardsView(self, viewForIndex: indexPath.row)
        cell.embedView(view)
        return cell
    }

    public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        self.delegate?.horizontalCardsView(self, didSelectItemAtIndex: indexPath.row)
    }
}

extension HorizontalCardsView {

    public func scrollViewWillBeginDragging(_ scrollView: UIScrollView) {
        self.indexOfCellBeforeDragging = self.getIndexOfMajorCell()
    }

    public func scrollViewWillEndDragging(_ scrollView: UIScrollView, withVelocity velocity: CGPoint, targetContentOffset: UnsafeMutablePointer<CGPoint>) {
        targetContentOffset.pointee = scrollView.contentOffset
        let cellIndexOffset = velocity.x == 0 ? 0 : (velocity.x > 0 ? 1: -1)
        let indexOfDestinationCell = max(0, min(self.viewsCount - 1, self.indexOfCellBeforeDragging + cellIndexOffset))
        let indexPath = IndexPath(row: indexOfDestinationCell, section: 0)
        self.collectionView.scrollToItem(at: indexPath, at: .centeredHorizontally, animated: true)
        self.delegate?.horizontalCardsView(self, scrollIndex: indexPath.row)
    }

    private func getIndexOfMajorCell() -> Int {
        let itemWidth = self.flowLayout.itemSize.width
        let proportionalOffset = self.flowLayout.collectionView!.contentOffset.x / itemWidth
        let index = Int(round(proportionalOffset))
        let numberOfItems = self.collectionView.numberOfItems(inSection: 0)
        let safeIndex = max(0, min(numberOfItems - 1, index))
        return safeIndex
    }
}


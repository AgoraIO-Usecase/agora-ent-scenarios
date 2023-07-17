//
//  VoiceRoomGiftsView.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/9/8.
//

import UIKit
import ZSwiftBaseLib

public class SAGiftsView: UIView, UICollectionViewDelegate, UICollectionViewDataSource {
    var gifts = [SAGiftEntity]() {
        willSet {
            current = gifts.last
        }
    }

    public var sendClosure: ((SAGiftEntity) -> Void)?

    var lastPoint = CGPoint.zero

    lazy var header: SAAlertContainer = .init(frame: CGRect(x: 0, y: 0, width: ScreenWidth, height: 60)).backgroundColor(.white)

    lazy var flowLayout: UICollectionViewFlowLayout = {
        let layout = UICollectionViewFlowLayout()
        layout.itemSize = CGSize(width: (ScreenWidth - 30) / 4.0, height: (110 / 84.0) * (ScreenWidth - 30) / 4.0)
        layout.minimumLineSpacing = 0
        layout.minimumInteritemSpacing = 0
        layout.scrollDirection = .horizontal
        return layout
    }()

    lazy var giftList: UICollectionView = .init(frame: CGRect(x: 15, y: self.header.frame.maxY, width: ScreenWidth - 30, height: (110 / 84.0) * ((ScreenWidth - 30) / 4.0)), collectionViewLayout: self.flowLayout).registerCell(SASendGiftCell.self, forCellReuseIdentifier: "VoiceRoomSendGiftCell").delegate(self).dataSource(self).showsHorizontalScrollIndicator(false).backgroundColor(.white)

    lazy var pageControl: UIPageControl = {
        let pageControl = UIPageControl(frame: CGRect(x: 0, y: self.giftList.frame.maxY + 20, width: self.frame.width, height: 5))
        pageControl.backgroundColor = UIColor.clear
        pageControl.numberOfPages = 3
        pageControl.currentPage = 0
        // 设置pageControl未选中的点的颜色
        pageControl.pageIndicatorTintColor = UIColor(0xEFF4FF)
        // 设置pageControl选中的点的颜色
        pageControl.currentPageIndicatorTintColor = UIColor(0x6378F4)
        return pageControl
    }()

    lazy var contribution: UILabel = .init(frame: CGRect(x: 20, y: self.giftList.frame.maxY + 50, width: ScreenWidth / 2.0 - 40, height: 20)).font(.systemFont(ofSize: 12, weight: .regular)).textColor(UIColor(0x6C7192)).text("\(SAUserInfo.shared.user?.amount ?? 0)")

    lazy var lineLayer: UIView = .init(frame: CGRect(x: ScreenWidth - 172, y: self.giftList.frame.maxY + 38.5, width: 155, height: 40)).cornerRadius(20).layerProperties(UIColor(0xB4D6FF), 1)

    lazy var chooseQuantity: UIButton = .init(type: .custom).frame(CGRect(x: 0, y: 0, width: 76, height: 40)).font(.systemFont(ofSize: 14, weight: .semibold)).textColor(.black, .normal).title("1", .normal).backgroundColor(.white).addTargetFor(self, action: #selector(chooseCount), for: .touchUpInside)

    lazy var send: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: self.chooseQuantity.frame.maxX, y: 0, width: 79, height: 40)).font(.systemFont(ofSize: 14, weight: .semibold)).setGradient([UIColor(0x219BFF), UIColor(0x345DFF)], [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)]).textColor(.white, .normal).title(sceneLocalized( "Send"), .normal).addTargetFor(self, action: #selector(sendAction), for: .touchUpInside)
    }()

    lazy var title: UILabel = .init(frame: CGRect(x: ScreenWidth / 2.0 - 30, y: 25.5, width: 60, height: 20)).textAlignment(.center).textColor(UIColor(0x040925)).font(.systemFont(ofSize: 16, weight: .semibold)).text(sceneLocalized( "Gifts"))

    lazy var disableView: UIView = .init(frame: CGRect(x: ScreenWidth / 2.0, y: self.lineLayer.frame.minY, width: ScreenWidth / 2.0, height: 40)).backgroundColor(UIColor(white: 1, alpha: 0.7))

    lazy var popview: SAPopView = .init(frame: CGRect(x: self.lineLayer.frame.minX - 20, y: 25, width: 120, height: 186)).contentMode(.scaleAspectFill)

    let pop = SAPopTip()

    var gift_count = "1" {
        willSet {
            DispatchQueue.main.async {
                self.chooseQuantity.setTitle(newValue, for: .normal)
            }
        }
    }

    var current: SAGiftEntity?

    override public init(frame: CGRect) {
        super.init(frame: frame)
    }

    public convenience init(frame: CGRect, gifts: [SAGiftEntity]) {
        self.init(frame: frame)
        self.gifts = gifts
        addSubViews([header, giftList, pageControl, contribution, lineLayer, disableView])
        disableView.isHidden = true
        bringSubviewToFront(disableView)
        chooseQuantity.setImage(UIImage.sceneImage(name:"arrow_down"), for: .normal)
        chooseQuantity.setImage(UIImage.sceneImage(name:"arrow_up"), for: .selected)
        chooseQuantity.imageEdgeInsets = UIEdgeInsets(top: 5, left: 55, bottom: 5, right: 10)
        chooseQuantity.titleEdgeInsets = UIEdgeInsets(top: 5, left: 5, bottom: 5, right: 30)
        lineLayer.addSubViews([chooseQuantity, send])
        giftList.isPagingEnabled = true
        giftList.alwaysBounceHorizontal = true
        header.addSubview(title)
        pop.shadowColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.12)
        pop.shadowOpacity = 1
        pop.shadowRadius = 8
        pop.shadowOffset = CGSize(width: 0, height: 0)
        pop.cornerRadius = 12
        pop.shouldConsiderCutoutTapSeparately = true
        pop.dismissHandler = { [weak self] _ in
            self?.chooseQuantity.isSelected = false
        }
        popview.countClosure = { [weak self] in
            guard let self = self else { return }
            self.pop.hide()
            self.chooseQuantity.isSelected = false
            self.gift_count = $0
            self.contribution.text = "Contribution Total".spatial_localized() + ": " + "\(Int(self.gift_count)! * Int(self.current?.gift_price ?? "1")!)"
        }
        current = self.gifts.first
        contribution.text = "Contribution Total".spatial_localized() + ": " + "1"
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

public extension SAGiftsView {
    @objc internal func chooseCount() {
        chooseQuantity.isSelected = !chooseQuantity.isSelected
        if chooseQuantity.isSelected == true {
            pop.show(customView: popview, direction: .up, in: self, from: CGRect(x: lineLayer.frame.minX - 34, y: lineLayer.frame.minY, width: lineLayer.frame.width, height: lineLayer.frame.height))
        } else {
            pop.hide()
        }
    }

    @objc internal func sendAction() {
        disableView.isHidden = false
        if sendClosure != nil, current != nil {
            current?.gift_count = gift_count
            chooseQuantity.setTitle("1", for: .normal)
            sendClosure!(current!.mutableCopy() as! SAGiftEntity)
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
            self.disableView.isHidden = true
            self.contribution.text = "Contribution Total".spatial_localized() + ": " + "\(self.current?.gift_price ?? "1")"
        }
    }

    func scrollViewDidScroll(_ scrollView: UIScrollView) {
        let index = scrollView.contentOffset.x / giftList.frame.width
        pageControl.currentPage = Int(index)
        if index > 1, scrollView.contentOffset.x - lastPoint.x > 0 {
            UIView.animate(withDuration: 0.3, delay: 0) {
                self.giftList.scrollToItem(at: IndexPath(row: self.gifts.count - 1, section: 0), at: .right, animated: false)
            }
        }
        lastPoint = scrollView.contentOffset
    }

    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        gifts.count
    }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "VoiceRoomSendGiftCell", for: indexPath) as? SASendGiftCell
        cell?.refresh(item: gifts[safe: indexPath.row]!)
        return cell ?? SASendGiftCell()
    }

    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        collectionView.deselectItem(at: indexPath, animated: true)
        gifts.forEach { $0.selected = false }
        let gift = gifts[safe: indexPath.row]
        gift?.selected = true
        if indexPath.row == 8 {
            pageControl.currentPage = 3
            giftList.scrollToItem(at: IndexPath(row: gifts.count - 1, section: 0), at: .right, animated: true)
        }
        current = gift
        if let value = gift?.gift_price {
            if Int(value)! >= 100 {
                gift_count = "1"
                chooseQuantity.setTitle(gift_count, for: .normal)
                chooseQuantity.setTitleColor(.lightGray, for: .normal)
                gift?.gift_count = "1"
                chooseQuantity.isEnabled = false
            } else {
                gift?.gift_count = gift_count
                chooseQuantity.isEnabled = true
                chooseQuantity.setTitleColor(.darkText, for: .normal)
            }
        }
        let total = Int(gift_count)! * Int(gift!.gift_price ?? "1")!
        contribution.text = "Contribution Total".spatial_localized() + ": " + "\(total)"
        giftList.reloadData()
    }
}

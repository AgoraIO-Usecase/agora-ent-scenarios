//
//  VoiceRoomGiftView.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/31.
//

import UIKit
import ZSwiftBaseLib

public class VoiceRoomGiftView: UIView, UITableViewDelegate, UITableViewDataSource {
    public var gifts = [VoiceRoomGiftEntity]()

    private var lastOffsetY = CGFloat(0)

    public lazy var giftList: UITableView = .init(frame: CGRect(x: 5, y: 0, width: self.frame.width - 20, height: self.frame.height), style: .plain).tableFooterView(UIView()).separatorStyle(.none).registerCell(VoiceRoomGiftCell.self, forCellReuseIdentifier: "VoiceRoomGiftCell").showsVerticalScrollIndicator(false).showsHorizontalScrollIndicator(false).delegate(self).dataSource(self).backgroundColor(.clear)

    override public init(frame: CGRect) {
        super.init(frame: frame)
        addSubview(giftList)
        giftList.isScrollEnabled = false
        giftList.isUserInteractionEnabled = false
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

public extension VoiceRoomGiftView {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        gifts.count
    }

    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        44
    }

    func tableView(_ tableView: UITableView, didEndDisplaying cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        cell.contentView.transform = CGAffineTransform(scaleX: 0.75, y: 0.75)
        cell.alpha = 0
    }

    func tableView(_ tableView: UITableView, willDisplay cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        cell.contentView.transform = CGAffineTransform(scaleX: 1, y: 1)
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: "VoiceRoomGiftCell", for: indexPath) as? VoiceRoomGiftCell
        if cell == nil {
            cell = VoiceRoomGiftCell(style: .default, reuseIdentifier: "VoiceRoomGiftCell")
        }
        cell?.refresh(item: gifts[safe: indexPath.row] ?? VoiceRoomGiftEntity())
        return cell ?? VoiceRoomGiftCell()
    }

    internal func cellAnimation() {
        alpha = 1
        giftList.reloadData()
        let indexPath = IndexPath(row: gifts.count - 2, section: 0)
        let cell = giftList.cellForRow(at: indexPath) as? VoiceRoomGiftCell
        cell?.refresh(item: gifts[indexPath.row])
        UIView.animate(withDuration: 0.3) {
            cell?.alpha = 0.35
            cell?.contentView.transform = CGAffineTransform(scaleX: 0.75, y: 0.75)
            self.giftList.scrollToRow(at: IndexPath(row: self.gifts.count - 1, section: 0), at: .top, animated: false)
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
            UIView.animate(withDuration: 0.3, delay: 1, options: .curveEaseInOut) {
                self.alpha = 0
            } completion: { finished in
                if finished {
                    self.gifts.removeAll()
                    self.removeFromSuperview()
                }
            }
        }
    }
}

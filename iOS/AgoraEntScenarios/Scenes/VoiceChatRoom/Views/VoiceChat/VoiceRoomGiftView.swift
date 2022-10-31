//
//  VoiceRoomGiftView.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/31.
//

import UIKit
import ZSwiftBaseLib

public class VoiceRoomGiftView: UIView,UITableViewDelegate,UITableViewDataSource {
    
    public var gifts = [VoiceRoomGiftEntity]() 
    
    private var lastOffsetY = CGFloat(0)
                
    public lazy var giftList: UITableView = {
        UITableView(frame: CGRect(x: 5, y: 0, width: self.frame.width-20, height: self.frame.height), style: .plain).tableFooterView(UIView()).separatorStyle(.none).registerCell(VoiceRoomGiftCell.self, forCellReuseIdentifier: "VoiceRoomGiftCell").showsVerticalScrollIndicator(false).showsHorizontalScrollIndicator(false).delegate(self).dataSource(self).backgroundColor(.clear)
    }()

    public override init(frame: CGRect) {
        super.init(frame: frame)
        self.addSubview(self.giftList)
        self.giftList.isScrollEnabled = false
        self.giftList.isUserInteractionEnabled = false
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
}

extension VoiceRoomGiftView {
    
    public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        self.gifts.count
    }
    
    public func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        44
    }
    
    public func tableView(_ tableView: UITableView, didEndDisplaying cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        cell.contentView.transform = CGAffineTransform(scaleX: 0.75, y: 0.75)
        cell.alpha = 0
    }
    
    public func tableView(_ tableView: UITableView, willDisplay cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        cell.contentView.transform = CGAffineTransform(scaleX: 1, y: 1)
    }
    
    public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: "VoiceRoomGiftCell", for: indexPath) as? VoiceRoomGiftCell
        if cell == nil {
            cell = VoiceRoomGiftCell(style: .default, reuseIdentifier: "VoiceRoomGiftCell")
        }
        cell?.refresh(item: self.gifts[safe:indexPath.row] ?? VoiceRoomGiftEntity())
        return cell ?? VoiceRoomGiftCell()
    }
    
    func cellAnimation() {
        self.alpha = 1
        self.giftList.reloadData()
        let indexPath = IndexPath(row: self.gifts.count-2, section: 0)
        let cell = self.giftList.cellForRow(at: indexPath) as? VoiceRoomGiftCell
        cell?.refresh(item: self.gifts[indexPath.row])
        UIView.animate(withDuration: 0.3) {
            cell?.alpha = 0.35
            cell?.contentView.transform = CGAffineTransform(scaleX: 0.75, y: 0.75)
            self.giftList.scrollToRow(at: IndexPath(row: self.gifts.count-1, section: 0), at: .top, animated: false)
        }
        DispatchQueue.main.asyncAfter(deadline: .now()+3) {
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


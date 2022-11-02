//
//  VoiceRoomChatView.swift
//  VoiceRoomBaseUIKit-VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/8/30.
//

import UIKit
import ZSwiftBaseLib

let chatViewWidth = ScreenWidth * (287 / 375.0)

public class VoiceRoomChatView: UIView, UITableViewDelegate, UITableViewDataSource {
    var likeAction: ((UIButton) -> Void)?

    private var lastOffsetY = CGFloat(0)

    private var cellOffset = CGFloat(0)

    var messages: [VoiceRoomChatEntity]? = [VoiceRoomChatEntity]()

    lazy var chatView: UITableView = .init(frame: CGRect(x: 0, y: 0, width: chatViewWidth, height: self.frame.height), style: .plain).delegate(self).dataSource(self).separatorStyle(.none).tableFooterView(UIView()).backgroundColor(.clear).registerCell(VoiceRoomChatCell.self, forCellReuseIdentifier: "VoiceRoomChatCell").showsVerticalScrollIndicator(false)

    lazy var likeView: UIButton = .init(type: .custom).frame(CGRect(x: self.frame.width - 53, y: self.frame.height - 43, width: 38, height: 38)).addTargetFor(self, action: #selector(toLike(_:)), for: .touchUpInside)

    lazy var emitter: VoiceRoomPraiseEmitterView = .init(frame: CGRect(x: ScreenWidth - 80, y: -20, width: 80, height: self.frame.height - 20))

    lazy var gradientLayer: CAGradientLayer = {
        CAGradientLayer().startPoint(CGPoint(x: 0, y: 0)).endPoint(CGPoint(x: 0, y: 0.1)).colors([UIColor.clear.withAlphaComponent(0).cgColor, UIColor.clear.withAlphaComponent(1).cgColor]).locations([NSNumber(0), NSNumber(1)]).rasterizationScale(UIScreen.main.scale).frame(self.blurView.frame)
    }()

    lazy var blurView: UIView = .init(frame: CGRect(x: 0, y: 0, width: chatViewWidth, height: self.frame.height)).backgroundColor(.clear)

    override public init(frame: CGRect) {
        super.init(frame: frame)
        addSubViews([blurView, likeView, emitter])
        blurView.layer.mask = gradientLayer
        blurView.addSubview(chatView)
        likeView.setImage(UIImage("unlike"), for: .normal)
        chatView.bounces = false
        chatView.allowsSelection = false
    }

    func getItem(dic: [String: String], join: Bool) -> VoiceRoomChatEntity {
        let item = VoiceRoomChatEntity()
        item.userName = dic["userName"]
        item.content = dic["content"]
        item.joined = join
        item.attributeContent = item.attributeContent
        item.width = item.width
        item.height = item.height
        return item
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    } /// 渐变蒙层
//    self.gradientLayer = [CAGradientLayer layer];
//    self.gradientLayer.startPoint = CGPointMake(0, 0); //渐变色起始位置
//    self.gradientLayer.endPoint = CGPointMake(0, 0.1); //渐变色终止位置
//    self.gradientLayer.colors = @[(__bridge id)[UIColor.clearColor colorWithAlphaComponent:0].CGColor, (__bridge id)
//     [UIColor.clearColor colorWithAlphaComponent:1.0].CGColor];
//    self.gradientLayer.locations = @[@(0), @(1.0)]; // 对应colors的alpha值
//    self.gradientLayer.rasterizationScale = UIScreen.mainScreen.scale;
//
//    ///  添加蒙层效果的图层
//    self.tableViewBackgroundView = [[UIView alloc] init];
//    self.tableViewBackgroundView.backgroundColor = UIColor.clearColor;
//    [self addSubview:self.tableViewBackgroundView];
//    self.tableViewBackgroundView.layer.mask = self.gradientLayer;
//
//    self.tableView = [[UITableView alloc] init];
//    self.tableView.backgroundColor = UIColor.clearColor;
//    self.tableView.scrollEnabled = NO;
//    self.tableView.allowsSelection =  NO;
//    [self.tableViewBackgroundView addSubview:self.tableView];
}

public extension VoiceRoomChatView {
    @objc internal func toLike(_ sender: UIButton) {
        emitter.setupEmitter()
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        messages?.count ?? 0
    }

    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        let height = messages?[safe: indexPath.row]?.height ?? 60
        return height
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: "VoiceRoomChatCell") as? VoiceRoomChatCell
        if cell == nil {
            cell = VoiceRoomChatCell(style: .default, reuseIdentifier: "VoiceRoomChatCell")
        }
        cell?.refresh(chat: messages![safe: indexPath.row]!)
        cell?.selectionStyle = .none
        return cell!
    }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
    }

    func tableView(_ tableView: UITableView, didEndDisplaying cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        if tableView.contentOffset.y - lastOffsetY < 0 {
            cellOffset -= cell.frame.height
        } else {
            cellOffset += cell.frame.height
        }
    }

    func scrollViewDidScroll(_ scrollView: UIScrollView) {
        let indexPath = chatView.indexPathForRow(at: scrollView.contentOffset) ?? IndexPath(row: 0, section: 0)
        let cell = chatView.cellForRow(at: indexPath)
        let maxAlphaOffset = cell?.frame.height ?? 40
        let offsetY = scrollView.contentOffset.y
        let alpha = (maxAlphaOffset - (offsetY - cellOffset)) / maxAlphaOffset
        if offsetY - lastOffsetY > 0 {
            UIView.animate(withDuration: 0.3) {
                cell?.alpha = alpha
            }
        } else {
            UIView.animate(withDuration: 0.25) {
                cell?.alpha = 1
            }
        }
        lastOffsetY = offsetY
        if lastOffsetY == 0 {
            cellOffset = 0
        }
    }
}

//
//  AUIRoomChatView.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/15.
//

import UIKit


/// Description 聊天整体区域与对应的Binder类数据交互刷新协议，提供给binder调用view里的刷新数据
@objc public protocol IAUIChatListView: NSObjectProtocol {
    
    /// Description 显示新消息
    /// - Parameter entity: 消息渲染的实体包含缓存的渲染富文本与高度宽度
    func showNewMessage(entity: AUIChatEntity)
}

let chatViewWidth = AScreenWidth * (287 / 375.0)
/*!
 *  \~Chinese
 *  聊天消息列表
 *
 *  \~English
 *  Chat list
 *
 */
public class AUIChatListView: UIView,IAUIChatListView {
    public func showNewMessage(entity: AUIChatEntity) {
        self.messages?.append(entity)
        self.chatView.reloadData()
        self.scrollTableViewToBottom()
    }
    

    private var lastOffsetY = CGFloat(0)

    private var cellOffset = CGFloat(0)

    public var messages: [AUIChatEntity]? = [AUIChatEntity]()

    public lazy var chatView: UITableView = {
        UITableView(frame: CGRect(x: 0, y: 0, width: chatViewWidth, height: self.frame.height), style: .plain).delegate(self).dataSource(self).separatorStyle(.none).tableFooterView(UIView()).backgroundColor(.clear).showsVerticalScrollIndicator(false)
    }()

    private lazy var gradientLayer: CAGradientLayer = {
        CAGradientLayer().startPoint(CGPoint(x: 0, y: 0)).endPoint(CGPoint(x: 0, y: 0.1)).colors([UIColor.clear.withAlphaComponent(0).cgColor, UIColor.clear.withAlphaComponent(1).cgColor]).locations([NSNumber(0), NSNumber(1)]).rasterizationScale(UIScreen.main.scale).frame(self.blurView.frame)
    }()

    private lazy var blurView: UIView = {
        UIView(frame: CGRect(x: 0, y: 0, width: chatViewWidth, height: self.frame.height)).backgroundColor(.clear)
    }()

    override public init(frame: CGRect) {
        super.init(frame: frame)
        self.addSubViews([self.blurView])
        self.blurView.layer.mask = self.gradientLayer
        self.blurView.addSubview(self.chatView)
        self.chatView.bounces = false
        self.chatView.allowsSelection = false
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

extension AUIChatListView:UITableViewDelegate, UITableViewDataSource {
    
    @objc public func scrollTableViewToBottom() {
        if self.messages?.count ?? 0 > 1 {
            self.chatView.scrollToRow(at: IndexPath(row: self.messages!.count-1, section: 0), at: .bottom, animated: true)
        }
    }

    public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        self.messages?.count ?? 0
    }

    public func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        let height = self.messages?[safe: indexPath.row]?.height ?? 60
        return height
    }

    public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: "AUIChatCell") as? AUIChatCell
        if cell == nil {
            cell = AUIChatCell(reuseIdentifier: "AUIChatCell",config: AUIChatCellConfig())
        }
        guard let entity = self.messages?[safe: indexPath.row] else { return AUIChatCell() }
        cell?.refresh(chat: entity)
        cell?.selectionStyle = .none
        return cell!
    }

    public func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
    }

    public func tableView(_ tableView: UITableView, didEndDisplaying cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        if tableView.contentOffset.y - self.lastOffsetY < 0 {
            self.cellOffset -= cell.frame.height
        } else {
            self.cellOffset += cell.frame.height
        }
    }

    public func scrollViewDidScroll(_ scrollView: UIScrollView) {
        let indexPath = self.chatView.indexPathForRow(at: scrollView.contentOffset) ?? IndexPath(row: 0, section: 0)
        let cell = self.chatView.cellForRow(at: indexPath)
        let maxAlphaOffset = cell?.frame.height ?? 40
        let offsetY = scrollView.contentOffset.y
        let alpha = (maxAlphaOffset - (offsetY - self.cellOffset)) / maxAlphaOffset
        if offsetY - lastOffsetY > 0 {
            UIView.animate(withDuration: 0.3) {
                cell?.alpha = alpha
            }
        } else {
            UIView.animate(withDuration: 0.25) {
                cell?.alpha = 1
            }
        }
        self.lastOffsetY = offsetY
        if self.lastOffsetY == 0 {
            self.cellOffset = 0
        }
    }
}

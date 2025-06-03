//
//  SudGameBaseEventHandler.h
//  QuickStart
//
//  Created by kaniel on 2024/1/16.
//  Copyright © 2024 Sud.Tech (https://sud.tech). All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SudMGPWrapper.h"
NS_ASSUME_NONNULL_BEGIN

/// 加载SudMGP SDK加载必须的业务参数
@interface SudGameLoadConfigModel : NSObject
/// 应用ID，联系SUD获取
/// Application ID. Contact SUD to obtain it
@property(nonatomic, strong)NSString *appId;
/// Application Key. Contact SUD to obtain it
/// The game ID.
@property(nonatomic, strong)NSString *appKey;
/// 加载环境，正式环境传入NO,测试环境传入YES.
/// Load environment. Pass NO to the formal environment and YES to the test environment.
@property(nonatomic, assign)BOOL isTestEnv;

/// 游戏ID
/// The game ID.
@property (nonatomic, assign)int64_t gameId;
/// 房间ID
/// The room ID.
@property (nonatomic, strong)NSString * roomId;
/// 当前用户ID
/// The current user ID.
@property (nonatomic, strong)NSString * userId;
/// 语言 支持简体"zh-CN"    繁体"zh-TW"    英语"en-US"   马来"ms-MY"
/// The language (e.g., "zh-CN", "zh-TW", "en-US", "ms-MY").
@property (nonatomic, strong)NSString * language;
/// 加载展示视图
/// The view for displaying the game.
@property (nonatomic, strong)UIView * gameView;
/// 授权秘钥,跨域使用，默认不需要设置
/// Just use for cross app, default is nil
@property(nonatomic, strong)NSString *authorizationSecret;
@end

/// 游戏事件处理基类模块
/// SudGameBaseEventHandler is a base class for handling game events.
@interface SudGameBaseEventHandler : NSObject<SudFSMMGListener>


/// SudFSMMGDecorator game -> app 辅助接收解析SudMGP SDK抛出的游戏回调事件、获取相关游戏状态模块
/// Helper module for receiving and parsing game callback events from SudMGP SDK.
@property (nonatomic, strong, readonly) SudFSMMGDecorator *sudFSMMGDecorator;

/// SudFSTAPPDecorator app -> game 辅助APP操作游戏相关指令模块
/// Helper module for sending game-related commands from the app to the game.
@property (nonatomic, strong, readonly) SudFSTAPPDecorator *sudFSTAPPDecorator;

/// 加载游戏配置
/// The loaded game configuration model.
@property(nonatomic, strong, readonly)SudGameLoadConfigModel *loadConfigModel;

/// 设置加载游戏配置
/// Sets the game configuration model.
/// - Parameter loadConfigModel: 配置model
- (void)setupLoadConfigModel:(SudGameLoadConfigModel *)loadConfigModel;


/// 获取code并返回,子类必须实现像自己应用服务端获取code并返回
/// 接入方客户端 调用 接入方服务端 getCode: 获取 短期令牌code
/// 参考文档 https://docs.sud.tech/en-US/app/Server/ImplementAuthenticationByYourself.html
/// - Parameters:
///   - userId: 当前加载游戏用户ID
///   - result: 返回code回调
///   The  `onGetCode`  method is a method that retrieves the code from the server and returns it. The method takes two parameters: the user ID and the result callback. The user ID is the ID of the user who is loading the game, and the result callback is a function that is called when the code has been retrieved. The method first calls the  `getCode`  method on the server to retrieve the code. If the code is successfully retrieved, the method calls the result callback with the code as the argument. Otherwise, the method calls the result callback with an error.
- (void)onGetCode:(nonnull NSString *)userId result:(void(^)( NSString * _Nonnull code))result;

/// 配置游戏视图，应用根据自身配置游戏
/// 开发者可以根据自己需求配置游戏相关功能展示
/// The  `onGetGameCfg`  method is a method that configures the game view. The method takes no parameters and returns a  `GameCfgModel`  object. The  `GameCfgModel`  object contains the configuration information for the game view, such as the size, position, and visibility of the game view. The developer can use this information to configure the game view according to their own needs.
- (nonnull GameCfgModel *)onGetGameCfg;

/// 获取游戏View信息,默认返回全屏，应用根据自身需要覆写并返回视图信息(注意：此回调返回真实屏幕视图点距离即可，不需要使用计算scale值去算，内部会自行换算)
/// The  `onGetGameViewInfo`  method is a method that returns the game view information. The method takes no parameters and returns a  `GameViewInfoModel`  object. The  `GameViewInfoModel`  object contains the information about the game view, such as the size, position, and visibility of the game view. The application can use this information to display the game view.
- (nonnull GameViewInfoModel *)onGetGameViewInfo;
@end

NS_ASSUME_NONNULL_END

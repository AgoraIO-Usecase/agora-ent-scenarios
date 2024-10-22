//
//  SudGameBaseEventHandler.m
//  QuickStart
//
//  Created by kaniel on 2024/1/16.
//  Copyright © 2024 Sud.Tech (https://sud.tech). All rights reserved.
//

#import "SudGameBaseEventHandler.h"

@implementation SudGameLoadConfigModel
@end

@interface SudGameBaseEventHandler()
@property (nonatomic, strong) SudFSMMGDecorator *sudFSMMGDecorator;
@property (nonatomic, strong) SudFSTAPPDecorator *sudFSTAPPDecorator;
@property(nonatomic, strong)SudGameLoadConfigModel *loadConfigModel;

@end

@implementation SudGameBaseEventHandler

- (void)dealloc {
    NSLog(@"SudGameBaseEventHandler dealloc");
}

- (instancetype)init {
    if (self = [super init]) {
        [self createSudMGPWrapper];
    }
    return self;
}


- (void)createSudMGPWrapper {
    self.sudFSTAPPDecorator = [[SudFSTAPPDecorator alloc] init];
    self.sudFSMMGDecorator = [[SudFSMMGDecorator alloc] init];
    [self.sudFSMMGDecorator setEventListener:self];
}


- (void)setupLoadConfigModel:(SudGameLoadConfigModel *)loadConfigModel {
    self.loadConfigModel = loadConfigModel;
}

- (void)onGetCode:(nonnull NSString *)userId result:(void(^)( NSString * _Nonnull code))result {
    NSAssert(NO, @"The game code must be loaded from the application service!!");
}

- (nonnull GameCfgModel *)onGetGameCfg {
    return [GameCfgModel defaultCfgModel];
}

- (nonnull GameViewInfoModel *)onGetGameViewInfo {
    GameViewInfoModel *m = [[GameViewInfoModel alloc] init];
    CGRect gameViewRect = self.loadConfigModel.gameView.bounds;
    // 默认游戏展示区域
    // Default area of game display
    m.view_size.width = gameViewRect.size.width;
    m.view_size.height = gameViewRect.size.height;
    return m;
}



#pragma mark =======SudFSMMGListener 游戏SDK回调 Game SDK callback=======

#pragma mark 启动游戏开发者针对游戏相关自定义配置 Launch game developer for game related custom configuration

- (void)onGetGameCfg:(nonnull id <ISudFSMStateHandle>)handle dataJson:(nonnull NSString *)dataJson {

    // 默认游戏配置
    // Default game configuration
    GameCfgModel *m = [self onGetGameCfg];
    NSString *configJsonStr = m.toJSON;
    NSLog(@"onGetGameCfg:%@", configJsonStr);
    [handle success:configJsonStr];
}

- (void)onGetGameViewInfo:(nonnull id <ISudFSMStateHandle>)handle dataJson:(nonnull NSString *)dataJson {

    NSAssert(self.loadConfigModel.gameView, @"Must set the gameView");
    // 屏幕缩放比例，游戏内部采用px，需要开发者获取本设备比值 x 屏幕点数来获得真实px值设置相关字段中
    // Screen scaling, px is used inside the game, the developer needs to obtain the device ratio x screen points to get the real px value set in the relevant fields
    CGFloat scale = [[UIScreen mainScreen] nativeScale];
    GameViewInfoModel *m = [self onGetGameViewInfo];
    // 游戏展示区域
    // Game display area
    m.view_size.width = (NSInteger)(m.view_size.width * scale);
    m.view_size.height = (NSInteger)(m.view_size.height * scale);
    // 游戏内容布局安全区域，根据自身业务调整顶部间距
    // Game content layout security area, adjust the top spacing according to their own business
    // 顶部间距
    // top spacing
    m.view_game_rect.top = (NSInteger)(m.view_game_rect.top * scale);
    // 左边
    // To the left
    m.view_game_rect.left = (NSInteger)(m.view_game_rect.left * scale);
    // 右边
    // Right
    m.view_game_rect.right = (NSInteger)(m.view_game_rect.right * scale);
    // 底部安全区域
    // Bottom safe area
    m.view_game_rect.bottom = (NSInteger)(m.view_game_rect.bottom * scale);

    m.ret_code = 0;
    m.ret_msg = @"success";
    NSString *viewInfoJsonStr = m.toJSON;
    NSLog(@"onGetGameViewInfo:%@", viewInfoJsonStr);
    [handle success:viewInfoJsonStr];
}


- (void)onExpireCode:(nonnull id<ISudFSMStateHandle>)handle dataJson:(nonnull NSString *)dataJson {
    
    // 请求业务服务器刷新令牌 Code更新
    // Request the service server to refresh the token Code update
    [self onGetCode:self.loadConfigModel.userId result:^(NSString * _Nonnull code) {
        // 调用游戏接口更新令牌
        // Call game interface update token
        [self.sudFSTAPPDecorator updateCode:code];
    }];
}

/// 游戏开始
- (void)onGameStarted {
    /// 此时表明游戏加载成功
    /// The game is loaded successfully
    NSLog(@"Game load finished");
}

/// 游戏销毁
/// Game destruction
- (void)onGameDestroyed {
    NSLog(@"Game destroyed");
}
@end

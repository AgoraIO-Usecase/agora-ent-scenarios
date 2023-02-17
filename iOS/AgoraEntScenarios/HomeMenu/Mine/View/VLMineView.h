//
//  VLMineView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
@class VLLoginModel;

NS_ASSUME_NONNULL_BEGIN

typedef enum : NSUInteger {
    VLMineViewClickTypeUserProtocol = 0,   // 用户协议
    VLMineViewClickTypePrivacyProtocol,    // 隐私政策
    VLMineViewClickTypeAboutUS,            // 关于我们
    VLMineViewClickTypeLogout,             // 退出登录
    VLMineViewClickTypeDestroyAccount,     // 注销账号
    VLMineViewClickTypeDebug,              // 开发者模式
} VLMineViewClickType;

typedef enum : NSUInteger {
    VLMineViewUserClickTypeNickName = 0, // 点击修改头像
    VLMineViewUserClickTypeAvatar        // 点击头像
} VLMineViewUserClickType;

@protocol VLMineViewDelegate <NSObject>

- (void)mineViewDidCick:(VLMineViewClickType)type;

- (void)mineViewDidCickUser:(VLMineViewUserClickType)type;

@optional

@end

@interface VLMineView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLMineViewDelegate>)delegate;

- (void)refreseUserInfo:(VLLoginModel *)loginModel;
- (void)refreseAvatar:(UIImage *)avatar;
- (void)refreseNickName:(NSString *)nickName;
- (void)refreshTableView;

@end

NS_ASSUME_NONNULL_END

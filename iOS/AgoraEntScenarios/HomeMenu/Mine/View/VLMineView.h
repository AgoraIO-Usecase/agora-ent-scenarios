//
//  VLMineView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
@class VLLoginModel;

NS_ASSUME_NONNULL_BEGIN

typedef enum : NSUInteger {
    VLMineViewClickTypeMyAccount = 0,      // 我的账号
    VLMineViewClickTypeUserProtocol,       // 用户协议
    VLMineViewClickTypePrivacyProtocol,    // 隐私政策
    VLMineViewClickTypePersonInfo,         // 个人信息
    VLMineViewClickTypeThirdInfoShared,    // 第三方信息
    VLMineViewClickTypeAboutUS,            // 关于我们
    VLMineViewClickTypeDebug,              // 开发者模式
    VLMineViewClickTypSubmitFeedback       // 提交反馈
} VLMineViewClickType;

typedef enum : NSUInteger {
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

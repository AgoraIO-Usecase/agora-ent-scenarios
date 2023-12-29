//
//  BaseViewController.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
#import "VLEmptyView.h"

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, VLNavigationBarStatus) {
    
    VLNavigationBarStatusLeft,
    VLNavigationBarStatusRight
};

@interface BaseViewController : UIViewController
@property (nonatomic, strong) VLEmptyView *vlEmptyView;
@property (nonatomic, assign, readwrite) BOOL statusBarHidden;

- (void)hideVLEmptyView;

- (void)leftButtonDidClickAction;

- (void)configNavigationBar:(UINavigationBar *)navigationBar;

- (void)setBackgroundImage:(NSString *)imageName;

- (void)setNaviTitleName:(NSString *)titleStr;

- (void)setBackBtn;

- (void)backBtnClickEvent;

@end

NS_ASSUME_NONNULL_END

//
//  UIWindow+Router.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/17.
//

#import "UIWindow+Router.h"
#import "BaseNavigationController.h"
#import "VLLoginViewController.h"
#import "VLMainTabBarController.h"
#import "VLUserCenter.h"
#import "KTVMacro.h"
@import IQKeyboardManager;

@implementation UIWindow (Router)
- (void)configRootViewController {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken,^{
//        [self configurationQMUITemplate];
        [self configureKeyboard];
    });
    
    UIViewController* rootVC = nil;
    if ([VLUserCenter center].isLogin) {
        rootVC = [[VLMainTabBarController alloc] init];
    } else {
        rootVC = [[VLLoginViewController alloc] init];
    }
    BaseNavigationController *navi = [[BaseNavigationController alloc] initWithRootViewController:rootVC];
    self.rootViewController = navi;
}


//- (void)configurationQMUITemplate {
//    QMUIThemeManagerCenter.defaultThemeManager.themeGenerator = ^__kindof NSObject * _Nonnull(NSString * _Nonnull identifier) {
//        if ([identifier isEqualToString:QDThemeIdentifierDefault]) return QMUIConfigurationTemplate.new;
//        return nil;
//    };
//
//    QMUIThemeManagerCenter.defaultThemeManager.currentThemeIdentifier = QDThemeIdentifierDefault;
//    [QDThemeManager.currentTheme applyConfigurationTemplate];
//    [QDCommonUI renderGlobalAppearances];
//
//    if (@available(iOS 13.0, *)) {
//        
//        QMUIThemeManagerCenter.defaultThemeManager.identifierForTrait = ^__kindof NSObject<NSCopying> * _Nonnull(UITraitCollection * _Nonnull trait) {
//            
//            return QMUIThemeManagerCenter.defaultThemeManager.currentThemeIdentifier;
//        };
//        QMUIThemeManagerCenter.defaultThemeManager.respondsSystemStyleAutomatically = false;
//    }
//
//}

- (void)configureKeyboard {
//    IQKeyboardManager *manager = [IQKeyboardManager sharedManager];
//    manager.enable = YES;
//    manager.shouldResignOnTouchOutside =YES;
//    manager.enableAutoToolbar = NO;
}
@end

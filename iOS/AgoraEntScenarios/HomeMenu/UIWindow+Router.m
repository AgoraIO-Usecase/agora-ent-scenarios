//
//  UIWindow+Router.m
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/17.
//

#import "UIWindow+Router.h"
#import "BaseNavigationController.h"
#import "VLLoginViewController.h"
#import "VLMainRootViewController.h"
#import "VLUserCenter.h"
#import "QDThemeManager.h"
#import "QDCommonUI.h"
#import "QDUIHelper.h"
#import "QMUIConfigurationTemplate.h"

@import QMUIKit;
//@import IQKeyboardManager;

@implementation UIWindow (Router)
- (void)configRootViewController {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken,^{
        [self configurationQMUITemplate];
        [self configureKeyboard];
    });
    if ([VLUserCenter center].isLogin) {
        [self vj_configureTabBarController];
    } else {
        VLLoginViewController *lvc = [[VLLoginViewController alloc] init];
        BaseNavigationController *navi = [[BaseNavigationController alloc] initWithRootViewController:lvc];
        self.rootViewController = navi;
    }
}

- (void)vj_configureTabBarController {
    VLMainRootViewController *rootViewController = [[VLMainRootViewController alloc] init];
    [self setRootViewController:rootViewController];
}



- (void)configurationQMUITemplate {
    QMUIThemeManagerCenter.defaultThemeManager.themeGenerator = ^__kindof NSObject * _Nonnull(NSString * _Nonnull identifier) {
        if ([identifier isEqualToString:QDThemeIdentifierDefault]) return QMUIConfigurationTemplate.new;
        return nil;
    };

    QMUIThemeManagerCenter.defaultThemeManager.currentThemeIdentifier = QDThemeIdentifierDefault;
    [QDThemeManager.currentTheme applyConfigurationTemplate];
    [QDCommonUI renderGlobalAppearances];

    if (@available(iOS 13.0, *)) {
        
        QMUIThemeManagerCenter.defaultThemeManager.identifierForTrait = ^__kindof NSObject<NSCopying> * _Nonnull(UITraitCollection * _Nonnull trait) {
            
            return QMUIThemeManagerCenter.defaultThemeManager.currentThemeIdentifier;
        };
        QMUIThemeManagerCenter.defaultThemeManager.respondsSystemStyleAutomatically = false;
    }

}

- (void)configureKeyboard {
//    IQKeyboardManager *manager = [IQKeyboardManager sharedManager];
//    manager.enable = YES;
//    manager.shouldResignOnTouchOutside =YES;
//    manager.enableAutoToolbar = NO;
}
@end

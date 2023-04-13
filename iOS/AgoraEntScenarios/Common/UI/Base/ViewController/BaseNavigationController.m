//
//  BaseNavigationController.m
//  VoiceOnLine
//

#import "BaseNavigationController.h"
#import "BaseViewController.h"

@interface BaseNavigationController ()

@end

@implementation BaseNavigationController

#pragma mark - Life Cycle Methods
- (void)viewDidLoad {
    [super viewDidLoad];
    self.navigationBar.hidden = YES;
}
#pragma mark - Intial Methods

#pragma mark - Events
- (void)leftButtonDidClickAction {
}

#pragma mark - Public Methods
- (BOOL)shouldAutorotate{
    return self.topViewController.shouldAutorotate;
}
- (UIViewController *)popViewControllerAnimated:(BOOL)animated  {
    UIViewController *viewController = [super popViewControllerAnimated:animated];
    return viewController;
}
- (void)pushViewController:(UIViewController *)viewController animated:(BOOL)animated {
    
    // 当前导航栏, 只有第一个viewController push的时候设置隐藏
    if (self.viewControllers.count == 1) {
        viewController.hidesBottomBarWhenPushed = YES;
    } else {
        viewController.hidesBottomBarWhenPushed = NO;
    }
    
    if (![viewController isKindOfClass:[BaseViewController class]]) {
        [super pushViewController:viewController animated:animated];
        return;
    }

//    if (self.viewControllers.count != 0) {
//        viewController.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"back"]
//                                                                                           style:UIBarButtonItemStyleDone
//                                                                                          target:viewController
//                                                                                          action:@selector(leftButtonDidClickAction)];
//    }
    [super pushViewController:viewController animated:animated];
}

//fix https://github.com/ChenYilong/CYLTabBarController/issues/483
- (void)setViewControllers:(NSArray<UIViewController *> *)viewControllers animated:(BOOL)animated {
    if (self.viewControllers.count > 1) {
        UIViewController *viewController = [self.viewControllers lastObject];
        viewController.hidesBottomBarWhenPushed = YES;
    }
    [super setViewControllers:viewControllers animated:animated];
}

@end

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
    
    if (![viewController isKindOfClass:[BaseViewController class]]) {
        [super pushViewController:viewController animated:animated];
        return;
    }

    if (self.viewControllers.count != 0) {
        viewController.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"back"] style:UIBarButtonItemStyleDone target:viewController action:@selector(leftButtonDidClickAction)];
    }
    [super pushViewController:viewController animated:animated];
}

@end

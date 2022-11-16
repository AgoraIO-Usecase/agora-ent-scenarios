//
//  AEAListContainerView.m
//  AgoraEditAvatar
//
//  Created by FanPengpeng on 2022/9/21.
//

#import "AEAListContainerView.h"

@interface AEAListContainerView ()

@property (strong, nonatomic) NSMutableDictionary *childVCDic;
@property (weak, nonatomic) UIViewController *currentVC;

@end

@implementation AEAListContainerView

- (NSArray<UIViewController *> * __nullable)allLoadedViewControllers {
    return self.childVCDic.allValues;
}

- (UIViewController *)viewControllerAtIndex:(NSInteger) index {
    UIViewController *vc = [self.childVCDic objectForKey:@(index)];
    return vc;
}

- (void)setSelectedIndex:(NSInteger)index {
    if (self.childVCDic == nil) {
        self.childVCDic = [NSMutableDictionary dictionary];
    }
    
    UIViewController *vc = [self viewControllerAtIndex:index];
    if (!vc) {
        if ([self.dataSource respondsToSelector:@selector(listContainerView:viewControllerForIndex:)]) {
            vc = [self.dataSource listContainerView:self viewControllerForIndex:index];
        }
        NSAssert(vc, @"listContainerView:viewControllerForIndex:方法中 index = %d 的控制器返回了 nil",index);
        [self.childVCDic setObject:vc forKey:@(index)];
    }
    
    // 如果vc是当前控制器 不做操作直接返回
    if (_currentVC == vc) {
        return;
    }
    [self addChildVC:vc];
    if (_currentVC != nil) {
        [self removeChildVC:_currentVC];
    }
    _currentVC = vc;
}

- (void)addChildVC:(UIViewController *)vc {
    if ([self.dataSource isKindOfClass:[UIViewController class]]) {
        UIViewController *containerVC = (UIViewController *)self.dataSource;
        [containerVC addChildViewController:vc];
        [self addSubview:vc.view];
        vc.view.frame = self.bounds;
        [containerVC didMoveToParentViewController:vc];
    }else{
        [self addSubview:vc.view];
        vc.view.frame = self.bounds;
    }
}

- (void)removeChildVC:(UIViewController *)vc {
    [vc willMoveToParentViewController:nil];
    [vc.view removeFromSuperview];
    [vc removeFromParentViewController];
}


@end

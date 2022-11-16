//
//  AEAListContainerView.h
//  AgoraEditAvatar
//
//  Created by FanPengpeng on 2022/9/21.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class AEAListContainerView;

@protocol AEAListContainerViewDataSource <NSObject>

- (UIViewController *)listContainerView:(AEAListContainerView *)listContainerView viewControllerForIndex:(NSInteger) index;

@end

@interface AEAListContainerView : UIView

@property (weak, nonatomic, readonly) UIViewController *currentVC;

@property (weak, nonatomic) id<AEAListContainerViewDataSource> dataSource;

- (NSArray<UIViewController *> * __nullable)allLoadedViewControllers;

- (UIViewController *)viewControllerAtIndex:(NSInteger)index;


-(void)setSelectedIndex:(NSInteger)index;

@end

NS_ASSUME_NONNULL_END

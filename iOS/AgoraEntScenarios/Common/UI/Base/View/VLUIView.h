//
//  VLUIView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
@import QMUIKit;

NS_ASSUME_NONNULL_BEGIN

@interface VLUIView : UIView

-(UIViewController *)vj_viewController;
-(QMUITableView *)vj_parentTableView;

@end

NS_ASSUME_NONNULL_END

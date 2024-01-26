//
//  VLEarSettingView.h
//  AgoraEntScenarios
//
//  Created by CP on 2023/6/5.
//

#import <UIKit/UIKit.h>
#import "VLBaseView.h"
NS_ASSUME_NONNULL_BEGIN
@class VLEarSettingView;
@protocol VLEarSettingViewViewDelegate <NSObject>
- (void)onVLKTVEarSettingViewValueChanged:(double)Value;
- (void)onVLKTVEarSettingViewSwitchChanged:(BOOL)flag;
@end
@interface VLEarSettingView : VLBaseView
- (instancetype)initWithFrame:(CGRect)frame isEarOn:(BOOL)isEarOn vol:(CGFloat)vol withDelegate:(id<VLEarSettingViewViewDelegate>)delegate;
@property (nonatomic, weak) id <VLEarSettingViewViewDelegate> delegate;
@end

NS_ASSUME_NONNULL_END

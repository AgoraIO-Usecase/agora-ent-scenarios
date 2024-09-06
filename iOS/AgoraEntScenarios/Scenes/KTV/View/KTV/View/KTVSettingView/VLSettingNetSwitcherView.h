//
//  VLSRSwitcherView.h
//  VoiceOnLine
//

#import "VLKTVItemBaseView.h"
@class VLSettingNetSwitcherView;

NS_ASSUME_NONNULL_BEGIN

@protocol VLSettingNetSwitcherViewDelegate <NSObject>

- (void)switcherNetView:(VLSettingNetSwitcherView *)switcherView on:(BOOL)on;

@end

@interface VLSettingNetSwitcherView : VLKTVItemBaseView

@property (nonatomic, copy) NSString *subText;

@property (nonatomic, weak) id <VLSettingNetSwitcherViewDelegate> delegate;

@property (nonatomic, assign) BOOL on;

@end

NS_ASSUME_NONNULL_END

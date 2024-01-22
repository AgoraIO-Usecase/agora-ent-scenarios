//
//  VLKTVSwitcherView.h
//  VoiceOnLine
//

#import "VLKTVItemBaseView.h"
@class VLKTVSwitcherView;

NS_ASSUME_NONNULL_BEGIN

@protocol VLKTVSwitcherViewDelegate <NSObject>

- (void)switcherView:(VLKTVSwitcherView *)switcherView on:(BOOL)on;

@end

@interface VLKTVSwitcherView : VLKTVItemBaseView

@property (nonatomic, copy) NSString *subText;

@property (nonatomic, weak) id <VLKTVSwitcherViewDelegate> delegate;

@property (nonatomic, assign) BOOL on;

@property (nonatomic, assign) BOOL modeOn;//IMMode

@property (nonatomic, strong) UISwitch *swich;

@property (nonatomic, strong) UILabel *subLabel;

@end

NS_ASSUME_NONNULL_END

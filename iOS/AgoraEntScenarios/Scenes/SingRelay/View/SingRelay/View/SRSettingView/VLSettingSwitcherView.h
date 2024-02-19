//
//  VLSRSwitcherView.h
//  VoiceOnLine
//

#import "VLKTVItemBaseView.h"
@class VLSettingSwitcherView;

NS_ASSUME_NONNULL_BEGIN

@protocol VLSettingSwitcherViewDelegate <NSObject>

- (void)switcherSetView:(VLSettingSwitcherView *)switcherView on:(BOOL)on;

@end

@interface VLSettingSwitcherView : VLKTVItemBaseView

@property (nonatomic, copy) NSString *subText;

@property (nonatomic, weak) id <VLSettingSwitcherViewDelegate> delegate;

@property (nonatomic, assign) BOOL on;

@end

NS_ASSUME_NONNULL_END

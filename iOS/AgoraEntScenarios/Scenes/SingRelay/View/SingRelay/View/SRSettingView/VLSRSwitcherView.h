//
//  VLSRSwitcherView.h
//  VoiceOnLine
//

#import "VLSRItemBaseView.h"
@class VLSRSwitcherView;

NS_ASSUME_NONNULL_BEGIN

@protocol VLSRSwitcherViewDelegate <NSObject>

- (void)switcherView:(VLSRSwitcherView *)switcherView on:(BOOL)on;

@end

@interface VLSRSwitcherView : VLSRItemBaseView

@property (nonatomic, copy) NSString *subText;

@property (nonatomic, weak) id <VLSRSwitcherViewDelegate> delegate;

@property (nonatomic, assign) BOOL on;

@end

NS_ASSUME_NONNULL_END

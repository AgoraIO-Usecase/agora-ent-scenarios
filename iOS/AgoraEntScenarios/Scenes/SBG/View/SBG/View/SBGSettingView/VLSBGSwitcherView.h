//
//  VLSBGSwitcherView.h
//  VoiceOnLine
//

#import "VLSBGItemBaseView.h"
@class VLSBGSwitcherView;

NS_ASSUME_NONNULL_BEGIN

@protocol VLSBGSwitcherViewDelegate <NSObject>

- (void)switcherView:(VLSBGSwitcherView *)switcherView on:(BOOL)on;

@end

@interface VLSBGSwitcherView : VLSBGItemBaseView

@property (nonatomic, copy) NSString *subText;

@property (nonatomic, weak) id <VLSBGSwitcherViewDelegate> delegate;

@property (nonatomic, assign) BOOL on;

@end

NS_ASSUME_NONNULL_END

//
//  VLPopOnLineTypeView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class VLSBGPopOnLineTypeView;
@protocol VLSBGPopOnLineTypeViewDelegate <NSObject>

- (void)onVLPopOnLineTypeView:(VLSBGPopOnLineTypeView*)view backBtnTapped:(id)sender;

@end

@interface VLSBGPopOnLineTypeView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGPopOnLineTypeViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END

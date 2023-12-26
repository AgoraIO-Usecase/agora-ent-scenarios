//
//  VLPopOnLineTypeView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class VLSRPopOnLineTypeView;
@protocol VLSRPopOnLineTypeViewDelegate <NSObject>

- (void)onVLPopOnLineTypeView:(VLSRPopOnLineTypeView*)view backBtnTapped:(id)sender;

@end

@interface VLSRPopOnLineTypeView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSRPopOnLineTypeViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END

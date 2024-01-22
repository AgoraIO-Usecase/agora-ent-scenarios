//
//  VLPopOnLineTypeView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class VLPopOnLineTypeView;
@protocol VLPopOnLineTypeViewDelegate <NSObject>

- (void)onVLPopOnLineTypeView:(VLPopOnLineTypeView*)view backBtnTapped:(id)sender;

@end

@interface VLPopOnLineTypeView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLPopOnLineTypeViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END

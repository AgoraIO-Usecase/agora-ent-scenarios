//
//  VLPopOnLineTypeView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol VLPopOnLineTypeViewDelegate <NSObject>

@optional
- (void)backBtnAction;

@end

@interface VLPopOnLineTypeView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLPopOnLineTypeViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END

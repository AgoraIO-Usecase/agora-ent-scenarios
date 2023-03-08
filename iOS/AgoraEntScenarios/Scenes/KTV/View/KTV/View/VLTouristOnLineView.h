//
//  VLTouristOnLineView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@protocol VLTouristOnLineViewDelegate <NSObject>

@optional
- (void)requestOnlineAction;

@end

@interface VLTouristOnLineView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLTouristOnLineViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END

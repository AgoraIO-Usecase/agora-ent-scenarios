//
//  VLNoBodyOnLineView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol VLNoBodyOnLineViewDelegate <NSObject>

@optional


@end

@interface VLNoBodyOnLineView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLNoBodyOnLineViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END

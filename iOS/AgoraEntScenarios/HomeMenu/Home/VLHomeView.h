//
//  VLHomeView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

@class VLHomeItemView;
@protocol VLHomeViewDelegate <NSObject>

- (void)itemClickAction:(int)tagValue;

@optional

@end

@interface VLHomeView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLHomeViewDelegate>)delegate;

@end



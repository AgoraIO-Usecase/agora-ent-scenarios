//
//  VLDiscoveryView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol VLDiscoveryViewDelegate <NSObject>

@optional

@end

@interface VLDiscoveryView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLDiscoveryViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END

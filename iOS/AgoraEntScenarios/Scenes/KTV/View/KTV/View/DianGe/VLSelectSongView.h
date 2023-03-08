//
//  VLSelectSongView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class VLRoomListModel;
@protocol VLSelectSongViewDelegate <NSObject>

@optional


@end

@interface VLSelectSongView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSelectSongViewDelegate>)delegate withRoomNo:(NSString *)roomNo ifChorus:(BOOL)ifChorus;


@end

NS_ASSUME_NONNULL_END

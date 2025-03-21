//
//  VLChoosedSongView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class VLSongList;

typedef enum : NSUInteger {
    VLChoosedSongViewActionTypeDelegate,
    VLChoosedSongViewActionTypeTop,
} VLChoosedSongViewActionType;

@protocol VLSongListDelegate <NSObject>

@optional

@end

@interface VLSongList : UIView


- (instancetype)initWithFrame:(CGRect)frame
                 withDelegate:(id<VLSongListDelegate>)delegate;

- (void)setSelSongsArray:(NSArray * _Nonnull)selSongsArray isOwner:(BOOL)isOwner;
@end

NS_ASSUME_NONNULL_END

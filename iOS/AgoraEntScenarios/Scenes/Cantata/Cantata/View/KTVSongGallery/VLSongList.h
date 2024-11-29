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

@interface VLSongList : UIView

- (void)setSelSongsArray:(NSArray * _Nonnull)selSongsArray isOwner:(BOOL)isOwner;
@end

NS_ASSUME_NONNULL_END

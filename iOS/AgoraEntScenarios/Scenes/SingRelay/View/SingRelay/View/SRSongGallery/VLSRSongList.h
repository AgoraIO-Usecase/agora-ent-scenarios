//
//  VLChoosedSongView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class VLSRSongList;

typedef enum : NSUInteger {
    VLChoosedSongViewActionTypeDelegate,
    VLChoosedSongViewActionTypeTop,
} VLChoosedSongViewActionType;

@interface VLSRSongList : UIView

@property (nonatomic, strong) NSArray *selSongsArray;

@end

NS_ASSUME_NONNULL_END

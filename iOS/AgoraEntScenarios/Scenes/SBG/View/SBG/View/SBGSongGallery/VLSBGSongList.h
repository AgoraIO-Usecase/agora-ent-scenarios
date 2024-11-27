//
//  VLChoosedSongView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
#import "VLSBGRoomSelSongModel.h"
#import "SBGServiceProtocol.h"

NS_ASSUME_NONNULL_BEGIN
@class VLSBGSongList;

typedef enum : NSUInteger {
    VLSBGChoosedSongViewActionTypeDelegate,
    VLSBGChoosedSongViewActionTypeTop,
} VLSBGChoosedSongViewActionType;

@interface VLSBGSongList : UIView

@property (nonatomic, strong) NSArray *selSongsArray;

@end

NS_ASSUME_NONNULL_END

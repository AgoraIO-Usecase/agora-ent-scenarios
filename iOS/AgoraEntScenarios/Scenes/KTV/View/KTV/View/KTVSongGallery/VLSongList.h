//
//  VLChoosedSongView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
#import "VLRoomSelSongModel.h"
#import "KTVServiceProtocol.h"

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

@property (nonatomic, readonly) NSArray *selSongsArray;

- (instancetype)initWithFrame:(CGRect)frame
                 withDelegate:(id<VLSongListDelegate>)delegate;

- (void)setSelSongsUIWithArray:(NSArray *)selSongsArray;

@end

NS_ASSUME_NONNULL_END

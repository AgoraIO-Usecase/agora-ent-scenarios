//
//  VLChoosedSongView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
#import "VLRoomSelSongModel.h"
#import "KTVServiceProtocol.h"

NS_ASSUME_NONNULL_BEGIN
@class VLChoosedSongView;

typedef enum : NSUInteger {
    VLChoosedSongViewActionTypeDelegate,
    VLChoosedSongViewActionTypeTop,
} VLChoosedSongViewActionType;

@protocol VLChoosedSongViewDelegate <NSObject>

@optional

@end

@interface VLChoosedSongView : UIView

@property (nonatomic, readonly) NSArray *selSongsArray;

- (instancetype)initWithFrame:(CGRect)frame
                 withDelegate:(id<VLChoosedSongViewDelegate>)delegate;

- (void)setSelSongsUIWithArray:(NSArray *)selSongsArray;

@end

NS_ASSUME_NONNULL_END

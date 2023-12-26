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

@protocol VLSRSongListDelegate <NSObject>

@optional

@end

@interface VLSRSongList : UIView

@property (nonatomic, strong) NSArray *selSongsArray;

- (instancetype)initWithFrame:(CGRect)frame
                 withDelegate:(id<VLSRSongListDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END

//
//  VLSelectSongView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class VLRoomListModel;
@protocol VLSelectedSongListDelegate <NSObject>

@optional


@end

@interface VLSelectedSongList : UIView
@property (nonatomic, strong) NSArray *selSongsArray;

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSelectedSongListDelegate>)delegate withRoomNo:(NSString *)roomNo ifChorus:(BOOL)ifChorus;

@end

NS_ASSUME_NONNULL_END

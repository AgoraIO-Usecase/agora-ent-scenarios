//
//  VLSelectSongView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class VLSBGRoomListModel;
@protocol VLSBGSelectedSongListDelegate <NSObject>

@optional


@end

@interface VLSBGSelectedSongList : UIView
@property (nonatomic, strong) NSArray *selSongsArray;

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGSelectedSongListDelegate>)delegate withRoomNo:(NSString *)roomNo ifChorus:(BOOL)ifChorus;

@end

NS_ASSUME_NONNULL_END

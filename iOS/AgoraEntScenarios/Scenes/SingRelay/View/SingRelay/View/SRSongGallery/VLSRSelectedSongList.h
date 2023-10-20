//
//  VLSelectSongView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class VLSRRoomListModel;
@protocol VLSRSelectedSongListDelegate <NSObject>

@optional


@end

@interface VLSRSelectedSongList : UIView
@property (nonatomic, strong) NSArray *selSongsArray;

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSRSelectedSongListDelegate>)delegate withRoomNo:(NSString *)roomNo ifChorus:(BOOL)ifChorus;

@end

NS_ASSUME_NONNULL_END

//
//  VLPopChooseSongView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class VLPopSongList;
@protocol VLPopSongListDelegate <NSObject>

@optional
- (void)chooseSongView:(VLPopSongList*)view tabbarDidClick:(NSUInteger)tabIndex;

@end

@interface VLPopSongList : UIView

- (instancetype)initWithFrame:(CGRect)frame
                 withDelegate:(id<VLPopSongListDelegate>)delegate
                   withRoomNo:(NSString *)roomNo
                     ifChorus:(BOOL)ifChorus;

@property (nonatomic, strong) NSArray *selSongsArray;

@end

NS_ASSUME_NONNULL_END

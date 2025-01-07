//
//  VLPopChooseSongView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class DHCPopSongList;
@protocol DHCPopSongListDelegate <NSObject>

@optional
- (void)chooseSongView:(DHCPopSongList*)view tabbarDidClick:(NSUInteger)tabIndex;

@end

@interface DHCPopSongList : UIView

- (instancetype)initWithFrame:(CGRect)frame
                 withDelegate:(id<DHCPopSongListDelegate>)delegate
                   withRoomNo:(NSString *)roomNo
                     ifChorus:(BOOL)ifChorus;

- (void)setSelSongsArray:(NSArray *)selSongsArray isOwner:(BOOL)isOwner;
@end

NS_ASSUME_NONNULL_END

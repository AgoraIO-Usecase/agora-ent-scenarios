//
//  VLPopChooseSongView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class VLSRPopSongList;
@protocol VLSRPopSongListDelegate <NSObject>

@optional
- (void)chooseSongView:(VLSRPopSongList*)view tabbarDidClick:(NSUInteger)tabIndex;

@end

@interface VLSRPopSongList : UIView

- (instancetype)initWithFrame:(CGRect)frame
                 withDelegate:(id<VLSRPopSongListDelegate>)delegate
                   withRoomNo:(NSString *)roomNo
                     ifChorus:(BOOL)ifChorus;

@property (nonatomic, strong) NSArray *selSongsArray;

@end

NS_ASSUME_NONNULL_END

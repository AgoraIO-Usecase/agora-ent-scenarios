//
//  VLPopChooseSongView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
#import "SBGServiceProtocol.h"

NS_ASSUME_NONNULL_BEGIN
@class VLSBGPopSongList;
@protocol VLSBGPopSongListDelegate <NSObject>

@optional
- (void)chooseSongView:(VLSBGPopSongList*)view tabbarDidClick:(NSUInteger)tabIndex;

@end

@interface VLSBGPopSongList : UIView

- (instancetype)initWithFrame:(CGRect)frame
                 withDelegate:(id<VLSBGPopSongListDelegate>)delegate
                   withRoomNo:(NSString *)roomNo
                     ifChorus:(BOOL)ifChorus;

@property (nonatomic, strong) NSArray *selSongsArray;

@end

NS_ASSUME_NONNULL_END

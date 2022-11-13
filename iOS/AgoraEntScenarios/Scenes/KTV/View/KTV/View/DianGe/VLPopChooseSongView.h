//
//  VLPopChooseSongView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
#import "KTVServiceProtocol.h"

NS_ASSUME_NONNULL_BEGIN
@class VLRoomListModel;
@protocol VLPopChooseSongViewDelegate <NSObject>


@end

@interface VLPopChooseSongView : UIView

- (instancetype)initWithFrame:(CGRect)frame
                 withDelegate:(id<VLPopChooseSongViewDelegate>)delegate
                   withRoomNo:(NSString *)roomNo
                     ifChorus:(BOOL)ifChorus;

@property (nonatomic, strong) NSArray *selSongsArray;

@end

NS_ASSUME_NONNULL_END

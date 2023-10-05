//
//  VLSearchSongResultView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol VLSearchSongResultViewDelegate <NSObject>

@optional


@end

@interface VLSearchSongResultView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSearchSongResultViewDelegate>)delegate withRoomNo:(nonnull NSString *)roomNo ifChorus:(BOOL)ifChorus;

- (void)loadSearchDataWithKeyWord:(NSString *)keyWord ifRefresh:(BOOL)ifRefresh;

@end

NS_ASSUME_NONNULL_END

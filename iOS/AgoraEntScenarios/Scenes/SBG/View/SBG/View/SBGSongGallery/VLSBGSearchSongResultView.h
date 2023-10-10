//
//  VLSearchSongResultView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
#import "SBGServiceProtocol.h"

NS_ASSUME_NONNULL_BEGIN

@protocol VLSBGSearchSongResultViewDelegate <NSObject>

@optional


@end

@interface VLSBGSearchSongResultView : UIView

@property (nonatomic, strong) NSArray *selSongsArray;

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGSearchSongResultViewDelegate>)delegate withRoomNo:(nonnull NSString *)roomNo ifChorus:(BOOL)ifChorus;

- (void)loadSearchDataWithKeyWord:(NSString *)keyWord ifRefresh:(BOOL)ifRefresh;

@end

NS_ASSUME_NONNULL_END

//
//  VLSelectSongTableItemView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
#import "JXCategoryListContainerView.h"
#import "KTVServiceProtocol.h"

NS_ASSUME_NONNULL_BEGIN
@class VLRoomListModel;
@interface VLSelectSongTableItemView : UIView <JXCategoryListContentViewDelegate>

- (instancetype)initWithFrame:(CGRect)frame
                    withRooNo:(NSString *)roomNo
                     ifChorus:(BOOL)ifChorus;

- (void)loadDatasWithIndex:(NSInteger)pageType ifRefresh:(BOOL)ifRefresh;


@end

NS_ASSUME_NONNULL_END

//
//  VLSelectSongTableItemView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
#import "JXCategoryListContainerView.h"

NS_ASSUME_NONNULL_BEGIN
@class VLRoomListModel;
@interface VLSelectSongTableItemView : UIView <JXCategoryListContentViewDelegate>
@property (nonatomic, strong) NSArray *selSongsArray;

- (instancetype)initWithFrame:(CGRect)frame
                    withRooNo:(NSString *)roomNo
                     ifChorus:(BOOL)ifChorus;

- (void)loadDatasWithIndex:(NSInteger)pageType ifRefresh:(BOOL)ifRefresh;
//更新别人点的歌曲状态
- (void)setSelSongArrayWith:(NSArray *)array;//更新别人点的歌曲状态
@end

NS_ASSUME_NONNULL_END

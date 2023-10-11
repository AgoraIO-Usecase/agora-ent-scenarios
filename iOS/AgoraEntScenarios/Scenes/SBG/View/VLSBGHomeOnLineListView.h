//
//  VLHomeOnLineListView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class VLSBGRoomListModel;
@protocol VLSBGHomeOnLineListViewDelegate <NSObject>

@optional
- (void)createBtnAction;

- (void)listItemClickAction:(VLSBGRoomListModel *)listModel;

@end

@interface VLSBGHomeOnLineListView : UIView

@property (nonatomic, strong) NSArray *modelsArray;

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGHomeOnLineListViewDelegate>)delegate;

@property (nonatomic, strong) UICollectionView *listCollectionView;

- (void)getRoomListIfRefresh:(BOOL)ifRefresh;

@end

NS_ASSUME_NONNULL_END

//
//  VLHomeOnLineListView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class VLSRRoomListModel;
@protocol VLSRHomeOnLineListViewDelegate <NSObject>

@optional
- (void)createBtnAction;

- (void)listItemClickAction:(VLSRRoomListModel *)listModel;

@end

@interface VLSRHomeOnLineListView : UIView

@property (nonatomic, strong) NSArray *modelsArray;

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSRHomeOnLineListViewDelegate>)delegate;

@property (nonatomic, strong) UICollectionView *listCollectionView;

- (void)getRoomListIfRefresh:(BOOL)ifRefresh;

@end

NS_ASSUME_NONNULL_END

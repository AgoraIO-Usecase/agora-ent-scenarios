//
//  VLHomeOnLineListView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class VLRoomListModel;
@protocol VLHomeOnLineListViewDelegate <NSObject>

@optional
- (void)createBtnAction;

- (void)listItemClickAction:(VLRoomListModel *)listModel;

@end

@interface VLHomeOnLineListView : UIView

@property (nonatomic, strong) NSArray *modelsArray;

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLHomeOnLineListViewDelegate>)delegate;

@property (nonatomic, strong) UICollectionView *listCollectionView;

//- (void)getRoomListIfRefresh:(BOOL)ifRefresh;
-(void)loadData;

@end

NS_ASSUME_NONNULL_END

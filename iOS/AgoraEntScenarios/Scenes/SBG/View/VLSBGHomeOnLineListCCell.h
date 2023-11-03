//
//  VLHomeOnListCCell.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

@class VLSBGRoomListModel;


@interface VLSBGHomeOnLineListCCell : UICollectionViewCell

@property (nonatomic, strong) VLSBGRoomListModel *listModel;

@property (nonatomic, strong) UIImageView *bgImgView;

@property (nonatomic, copy) void (^joinBtnClickBlock)(VLSBGRoomListModel *model);
@end



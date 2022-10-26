//
//  VLHomeOnListCCell.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

@class VLRoomListModel;


@interface VLHomeOnLineListCCell : UICollectionViewCell

@property (nonatomic, strong) VLRoomListModel *listModel;

@property (nonatomic, copy) void (^joinBtnClickBlock)(VLRoomListModel *model);
@end



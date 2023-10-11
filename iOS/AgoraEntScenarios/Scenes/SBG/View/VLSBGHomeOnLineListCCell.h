//
//  VLHomeOnListCCell.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

@class VLSBGRoomListModel;


@interface VLSBGHomeOnLineListCCell : UICollectionViewCell

@property (nonatomic, strong) VLSBGRoomListModel *listModel;

-(void)handleClick;

@property (nonatomic, copy) void (^joinBtnClickBlock)(VLSBGRoomListModel *model);
@end



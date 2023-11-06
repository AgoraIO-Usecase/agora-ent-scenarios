//
//  VLHomeOnListCCell.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

@class VLSRRoomListModel;


@interface VLSRHomeOnLineListCCell : UICollectionViewCell

@property (nonatomic, strong) VLSRRoomListModel *listModel;

-(void)handleClick;

@property (nonatomic, copy) void (^joinBtnClickBlock)(VLSRRoomListModel *model);
@end



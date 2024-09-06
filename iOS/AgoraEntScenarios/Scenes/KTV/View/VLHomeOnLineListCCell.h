//
//  VLHomeOnListCCell.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

@class SyncRoomInfo;
@interface VLHomeOnLineListCCell : UICollectionViewCell
@property (nonatomic, strong) SyncRoomInfo *listModel;
@property (nonatomic, strong) UIImageView *bgImgView;
@property (nonatomic, copy) void (^joinBtnClickBlock)(SyncRoomInfo *model);
@end



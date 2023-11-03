//
//  VLCreateRoomView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

typedef  NS_ENUM(NSUInteger, SBGCreateRoomActionType) {
    SBGCreateRoomActionTypeNormal = 0,
    SBGCreateRoomActionTypeEncrypt = 1,
    SBGCreateRoomActionTypeShowKeyboard = 2,
};


@class VLSBGAddRoomModel;
@protocol VLSBGCreateRoomViewDelegate <NSObject>

@optional
- (void)createBtnAction:(VLSBGAddRoomModel *)roomModel;
-(void)didCreateRoomAction:(SBGCreateRoomActionType)type;

@end

@interface VLSBGCreateRoomView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGCreateRoomViewDelegate>)delegate;

@end

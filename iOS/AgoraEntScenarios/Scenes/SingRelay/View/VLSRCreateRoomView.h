//
//  VLCreateRoomView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

typedef  NS_ENUM(NSUInteger, SRCreateRoomActionType) {
    SRCreateRoomActionTypeNormal = 0,
    SRCreateRoomActionTypeEncrypt = 1,
    SRCreateRoomActionTypeShowKeyboard = 2,
};

@class VLSRAddRoomModel;
@protocol VLSRCreateRoomViewDelegate <NSObject>

@optional
- (void)createBtnAction:(VLSRAddRoomModel *)roomModel;
-(void)didCreateRoomAction:(SRCreateRoomActionType)type;
@end

@interface VLSRCreateRoomView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSRCreateRoomViewDelegate>)delegate;

@end

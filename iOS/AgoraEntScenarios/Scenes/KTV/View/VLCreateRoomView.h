//
//  VLCreateRoomView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

typedef  NS_ENUM(NSUInteger, CreateRoomActionType) {
    CreateRoomActionTypeNormal = 0,
    CreateRoomActionTypeEncrypt = 1,
    CreateRoomActionTypeShowKeyboard = 2,
};

@class VLAddRoomModel;
@protocol VLCreateRoomViewDelegate <NSObject>

@optional
- (void)createBtnAction:(VLAddRoomModel *)roomModel;
-(void)didCreateRoomAction:(CreateRoomActionType)type;
@end

@interface VLCreateRoomView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLCreateRoomViewDelegate>)delegate;
@end

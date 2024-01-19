//
//  VLCreateRoomView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
@class VLAddRoomModel;
typedef  NS_ENUM(NSUInteger, DHCCreateRoomActionType) {
    DHCCreateRoomActionTypeNormal = 0,
    DHCCreateRoomActionTypeEncrypt = 1,
    DHCCreateRoomActionTypeShowKeyboard = 2,
};
@protocol VLCreateRoomViewDelegate <NSObject>

@optional
- (void)createBtnAction:(VLAddRoomModel *)roomModel;
-(void)didCreateRoomAction:(DHCCreateRoomActionType)type;
@end

@interface VLCreateRoomView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLCreateRoomViewDelegate>)delegate;
@property (nonatomic, strong) UIButton *createBtn;
@end

//
//  VLCreateRoomView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
@class VLAddRoomModel;
@protocol VLCreateRoomViewDelegate <NSObject>

@optional
- (void)createBtnAction:(VLAddRoomModel *)roomModel;

@end

@interface VLCreateRoomView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLCreateRoomViewDelegate>)delegate;

@end

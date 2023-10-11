//
//  VLCreateRoomView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
@class VLSBGAddRoomModel;
@protocol VLSBGCreateRoomViewDelegate <NSObject>

@optional
- (void)createBtnAction:(VLSBGAddRoomModel *)roomModel;

@end

@interface VLSBGCreateRoomView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGCreateRoomViewDelegate>)delegate;

@end

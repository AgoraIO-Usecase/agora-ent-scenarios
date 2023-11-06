//
//  VLCreateRoomView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>
@class VLSRAddRoomModel;
@protocol VLSRCreateRoomViewDelegate <NSObject>

@optional
- (void)createBtnAction:(VLSRAddRoomModel *)roomModel;

@end

@interface VLSRCreateRoomView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSRCreateRoomViewDelegate>)delegate;

@end

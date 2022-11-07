//
//  VLPopMoreSelView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

typedef enum : NSUInteger {
    VLKTVMoreBtnClickTypeBelcanto = 0,      //美声
    VLKTVMoreBtnClickTypeSound,             //音效
    VLKTVMoreBtnClickTypeMV                 //MV
} VLKTVMoreBtnClickType;

NS_ASSUME_NONNULL_BEGIN
@class VLPopMoreSelView;
@protocol VLPopMoreSelViewDelegate <NSObject>

- (void)onVLKTVMoreSelView:(VLPopMoreSelView*)view btnTapped:(id)sender withValue:(VLKTVMoreBtnClickType)typeValue;

@end

@interface VLPopMoreSelView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLPopMoreSelViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END

//
//  VLPopMoreSelView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

typedef enum : NSUInteger {
   // VLSBGMoreBtnClickTypeBelcanto = 0,      //美声
    VLSBGMoreBtnClickTypeSound,             //音效
    VLSBGMoreBtnClickTypeSetting,           //配置
    VLSBGMoreBtnClickTypeMV                 //MV
} VLSBGMoreBtnClickType;

NS_ASSUME_NONNULL_BEGIN
@class VLSBGPopMoreSelView;
@protocol VLSBGPopMoreSelViewDelegate <NSObject>

- (void)onVLSBGMoreSelView:(VLSBGPopMoreSelView*)view btnTapped:(id)sender withValue:(VLSBGMoreBtnClickType)typeValue;

@end

@interface VLSBGPopMoreSelView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSBGPopMoreSelViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END

//
//  VLPopMoreSelView.h
//  VoiceOnLine
//

#import <UIKit/UIKit.h>

typedef enum : NSUInteger {
   // VLKTVMoreBtnClickTypeBelcanto = 0,      //美声
    VLSRMoreBtnClickTypeSound,             //音效
    VLSRMoreBtnClickTypeMV                 //MV
} VLSRMoreBtnClickType;

NS_ASSUME_NONNULL_BEGIN
@class VLSRPopMoreSelView;
@protocol VLSRPopMoreSelViewDelegate <NSObject>

- (void)onVLSRMoreSelView:(VLSRPopMoreSelView*)view btnTapped:(id)sender withValue:(VLSRMoreBtnClickType)typeValue;

@end

@interface VLSRPopMoreSelView : UIView

- (instancetype)initWithFrame:(CGRect)frame withDelegate:(id<VLSRPopMoreSelViewDelegate>)delegate;

@end

NS_ASSUME_NONNULL_END

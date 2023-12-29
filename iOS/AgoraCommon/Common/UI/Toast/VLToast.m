//
//  VLToast.m
//  VoiceOnLine
//

#import "VLToast.h"
@import SVProgressHUD;

@implementation VLToast

+ (void)toast:(NSString *)msg {
    [self toast:msg duration:2.0];
}

+ (void)toast:(NSString *)msg duration:(float)duration {
    [SVProgressHUD showImage:nil status:msg];
    [SVProgressHUD setDefaultStyle:SVProgressHUDStyleDark];
    [SVProgressHUD dismissWithDelay:duration];
}

@end

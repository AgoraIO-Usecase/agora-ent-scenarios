//
//  VLUIView.m
//  VoiceOnLine
//

#import "VLUIView.h"
@import QMUIKit;

@interface VLUIView ()


@end

@implementation VLUIView

#pragma mark - Setter Getter Methods
-(UIViewController *)vj_viewController {
    id responder = self;
    while (responder){
        if ([responder isKindOfClass:[UIViewController class]]){
            return responder;
        }
        responder = [responder nextResponder];
    }
    return nil;
}
-(QMUITableView *)vj_parentTableView {
    id responder = self;
    while (responder){
        if ([responder isKindOfClass:[QMUITableView class]]){
            return responder;
        }
        responder = [responder nextResponder];
    }
    return nil;
}
@end

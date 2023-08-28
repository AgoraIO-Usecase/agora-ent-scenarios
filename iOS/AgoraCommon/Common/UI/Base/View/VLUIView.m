//
//  VLUIView.m
//  VoiceOnLine
//

#import "VLUIView.h"

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
-(UITableView *)vj_parentTableView {
    id responder = self;
    while (responder){
        if ([responder isKindOfClass:[UITableView class]]){
            return responder;
        }
        responder = [responder nextResponder];
    }
    return nil;
}
@end

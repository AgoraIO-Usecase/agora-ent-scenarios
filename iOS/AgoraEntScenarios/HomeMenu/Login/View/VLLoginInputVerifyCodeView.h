//
//  VLLoginInputVerifyCodeView.h
//  VoiceOnLine
//

#import "VLBaseView.h"

NS_ASSUME_NONNULL_BEGIN

@protocol VLLoginInputVerifyCodeViewDelegate <NSObject>

- (void)verifyCodeViewDidClickSendVerifyCode:(UIButton *)sender;

@end

@interface VLLoginInputVerifyCodeView : VLBaseView

@property(nonatomic, weak) id<VLLoginInputVerifyCodeViewDelegate> delegate;

@property(nonatomic, copy, readonly) NSString *verifyCode;

@property(nonatomic, assign) BOOL isVerifyCodeSent;

- (void)startTime:(UIButton *)sender;

@end

NS_ASSUME_NONNULL_END

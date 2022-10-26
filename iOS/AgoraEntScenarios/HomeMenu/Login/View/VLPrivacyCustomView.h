//
//  VLPrivacyCustomView.h
//  VoiceOnLine
//

#import "VLBaseView.h"

NS_ASSUME_NONNULL_BEGIN

typedef enum : NSUInteger {
    VLPrivacyClickTypeAgree = 0,
    VLPrivacyClickTypeDisagree,
    VLPrivacyClickTypePrivacy,
    VLPrivacyClickTypeUserAgreement,
} VLPrivacyClickType;

@protocol VLPrivacyCustomViewDelegate <NSObject>

- (void)privacyCustomViewDidClick:(VLPrivacyClickType)type;

@end

@interface VLPrivacyCustomView : VLBaseView

- (instancetype)initWithPass:(int)pass;

@property (nonatomic, weak) id <VLPrivacyCustomViewDelegate> delegate;
@property (nonatomic, assign) int pass;

@end

NS_ASSUME_NONNULL_END

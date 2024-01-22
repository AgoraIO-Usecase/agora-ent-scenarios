//
//  KTVSkipView.h
//  AgoraEntScenarios
//
//  Created by CP on 2023/1/29.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSUInteger, SkipActionType) {
    SkipActionTypeDown = 0,
    SkipActionTypeCancel = 1,
};
typedef NS_ENUM(NSUInteger, SkipType) {
    SkipTypePrelude = 0,
    SkipTypeEpilogue = 1,
};

typedef void (^OnSkipCallback)(SkipActionType type);

@interface KTVSkipView : UIView

-(instancetype)initWithFrame:(CGRect)frame completion:(OnSkipCallback _Nullable)completion;

-(void)setSkipType:(SkipType)type;

@end

NS_ASSUME_NONNULL_END

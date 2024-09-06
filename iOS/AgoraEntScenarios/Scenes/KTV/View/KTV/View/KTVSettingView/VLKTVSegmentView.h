//
//  VLKTVSegmentView.h
//  VoiceOnLine
//

#import "VLKTVItemBaseView.h"

@class VLKTVSegmentView;

typedef NS_ENUM(NSUInteger, SegmentViewType) {
    SegmentViewTypeScore = 0,
    SegmentViewTypeVQS = 1,
    SegmentViewTypeAns = 2,
};


NS_ASSUME_NONNULL_BEGIN

@protocol VLKTVSegmentViewDelegate <NSObject>

- (void)segmentView:(VLKTVSegmentView *)view DidSelectIndex:(NSInteger)index;

@end

@interface VLKTVSegmentView : VLKTVItemBaseView

@property (nonatomic, copy) NSString *subText;

@property (nonatomic, weak) id <VLKTVSegmentViewDelegate> delegate;

@property (nonatomic, assign) NSInteger selectIndex;

@property (nonatomic, assign) SegmentViewType type;

-(void)setSubText:(NSString *)subText attrText:(NSString *)attrText;

@end

NS_ASSUME_NONNULL_END

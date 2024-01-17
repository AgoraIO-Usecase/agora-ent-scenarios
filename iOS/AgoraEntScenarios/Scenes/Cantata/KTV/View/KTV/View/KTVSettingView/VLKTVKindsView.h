//
//  VLKTVKindsView.h
//  VoiceOnLine
//

#import "VLBaseView.h"
#import "VLBaseModel.h"
@class VLKTVKindsView;
@class VLKTVKindsModel;

@protocol VLKTVKindsViewDelegate <NSObject>

- (void)kindsViewDidClickIndex:(NSInteger)index;

@end

NS_ASSUME_NONNULL_BEGIN

@interface VLKTVKindsView : VLBaseView

@property (nonatomic, weak) id <VLKTVKindsViewDelegate> delegate;

@property (nonatomic, copy) NSArray <VLKTVKindsModel *> *list;

@end

@interface VLKTVKindsModel : VLBaseModel

@property (nonatomic, copy) NSString *title;
@property (nonatomic, copy) NSString *imageName;
@property (nonatomic, assign) BOOL selected;

+ (NSArray <VLKTVKindsModel *> *)kinds;

@end

NS_ASSUME_NONNULL_END

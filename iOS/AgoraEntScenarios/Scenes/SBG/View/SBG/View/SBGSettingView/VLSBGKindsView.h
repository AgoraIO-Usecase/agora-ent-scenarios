//
//  VLSBGKindsView.h
//  VoiceOnLine
//

#import "VLBaseView.h"
#import "VLBaseModel.h"
@class VLSBGKindsView;
@class VLSBGKindsModel;

@protocol VLSBGKindsViewDelegate <NSObject>

- (void)kindsViewDidClickIndex:(NSInteger)index;

@end

NS_ASSUME_NONNULL_BEGIN

@interface VLSBGKindsView : VLBaseView

@property (nonatomic, weak) id <VLSBGKindsViewDelegate> delegate;

@property (nonatomic, copy) NSArray <VLSBGKindsModel *> *list;

@end

@interface VLSBGKindsModel : VLBaseModel

@property (nonatomic, copy) NSString *title;
@property (nonatomic, copy) NSString *imageName;
@property (nonatomic, assign) BOOL selected;

+ (NSArray <VLSBGKindsModel *> *)kinds;

@end

NS_ASSUME_NONNULL_END

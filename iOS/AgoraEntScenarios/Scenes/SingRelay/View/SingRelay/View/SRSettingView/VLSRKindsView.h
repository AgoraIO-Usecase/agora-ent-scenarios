//
//  VLSRKindsView.h
//  VoiceOnLine
//

#import "VLBaseView.h"
#import "VLBaseModel.h"
@class VLSRKindsView;
@class VLSRKindsModel;

@protocol VLSRKindsViewDelegate <NSObject>

- (void)kindsViewDidClickIndex:(NSInteger)index;

@end

NS_ASSUME_NONNULL_BEGIN

@interface VLSRKindsView : VLBaseView

@property (nonatomic, weak) id <VLSRKindsViewDelegate> delegate;

@property (nonatomic, copy) NSArray <VLSRKindsModel *> *list;

@end

@interface VLSRKindsModel : VLBaseModel

@property (nonatomic, copy) NSString *title;
@property (nonatomic, copy) NSString *imageName;
@property (nonatomic, assign) BOOL selected;

+ (NSArray <VLSRKindsModel *> *)kinds;

@end

NS_ASSUME_NONNULL_END

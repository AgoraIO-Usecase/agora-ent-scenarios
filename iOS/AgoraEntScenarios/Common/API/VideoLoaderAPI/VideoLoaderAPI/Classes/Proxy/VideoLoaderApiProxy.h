//
//  VideoLoaderApiProxy.h
//  VideoLoaderAPI
//
//  Created by wushengtao on 2023/7/27.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface VideoLoaderApiProxy : NSObject
@property(nonatomic, weak) id<NSObject> delegate;

- (void)addListener:(id <NSObject>)listener;
- (void)removeListener:(id <NSObject>)listener;
- (void)removeAllListener;
@end

NS_ASSUME_NONNULL_END

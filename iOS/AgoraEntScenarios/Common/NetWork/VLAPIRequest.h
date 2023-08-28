//
//  VLAPIRequest.h
//  VoiceOnLine
//

#import "VLRequestRoute.h"
/// model
#import "VLResponseDataModel.h"
@import UIKit;

typedef void (^completeBlock_success)(VLResponseDataModel * _Nonnull response);
typedef void (^completeImageBlock_success)(UIImage * _Nonnull image);
/** NEI ERROR */
typedef void (^errorBlock_fail)(NSError * _Nullable error, NSURLSessionDataTask * _Nullable task);
/** LOADING PREGRESS */
typedef void (^progressBlock)(CGFloat value);

NS_ASSUME_NONNULL_BEGIN

@interface VLAPIRequest : VLRequestRoute

#pragma mark--网络请求
+ (void)getRequestURL:(NSString *)url parameter:(id)parameter showHUD:(BOOL)show success:(completeBlock_success)success failure:(errorBlock_fail)failure;

+ (void)postRequestURL:(NSString *)url parameter:(id)parameter showHUD:(BOOL)show success:(completeBlock_success)success failure:(errorBlock_fail)failure;

+ (void)requestRoute:(NSString *)route showHUD:(BOOL)show method:(NSString *)method parameter:(id)json requestType:(VLRequestType)type progressBlock:(progressBlock)progressBlock completeBlock:(completeBlock_success)completeBlock errorBlock:(errorBlock_fail)errorBlock;

#pragma mark -- 直接获取图片
+ (void)requestImageRoute:(NSString *)route  method:(NSString *)method parameter:(id)json requestType:(VLRequestType)type progressBlock:(progressBlock)progressBlock completeBlock:(completeImageBlock_success)completeBlock errorBlock:(errorBlock_fail)errorBlock;


#pragma mark--上传文件

+ (__kindof NSURLSessionDataTask *)uploadFileRoute:(NSString *)route
                                            method:(NSString *)method
                                      parameters:(id)json
                                            name:(NSString *)name
                                        filePath:(NSString *)filePath
                                        progressBlock:(progressBlock)progressBlock
                                         completeBlock:(completeBlock_success)completeBlock
                                         errorBlock:(errorBlock_fail)errorBlock;

#pragma mark--上传图片

+ (__kindof NSURLSessionDataTask *)uploadImagesRoute:(NSString *)route
                                              method:(NSString *)method
                                            showHUD:(BOOL)show
                                        parameters:(id)json
                                              name:(NSString *)name
                                            images:(NSArray<UIImage *> *)images
                                         fileNames:(NSArray<NSString *> *)fileNames
                                        imageScale:(CGFloat)imageScale
                                         imageType:(NSString *)imageType
                                          progressBlock:(progressBlock)progressBlock
                                           completeBlock:(completeBlock_success)completeBlock
                                           errorBlock:(errorBlock_fail)errorBlock;


+ (NSURLSessionDataTask *)uploadImageURL:(NSString *)url showHUD:(BOOL)show appendKey:(NSString *)key images:(NSArray<UIImage *> *)images success:(completeBlock_success)success failure:(errorBlock_fail)failure;

#pragma mark--下载文件

+ (__kindof NSURLSessionDownloadTask *)downloadRoute:(NSString *)route
                                              method:(NSString *)method
                                       fileDir:(NSString *)fileDir
                                      progressBlock:(progressBlock)progressBlock
                                       completeBlock:(void(^)(NSString *filePath))completeBlock
                                       errorBlock:(errorBlock_fail)errorBlock;

#pragma mark--其他
+ (void)cancelAllRequest;
+ (void)cancelRequestWithURL:(NSString *)URL;
@end

NS_ASSUME_NONNULL_END

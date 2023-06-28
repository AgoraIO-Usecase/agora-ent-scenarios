//
//  VLAPIRequest.m
//  VoiceOnLine
//

#import "VLAPIRequest.h"
//#import "AppDelegate+Config.h"
#import "UIWindow+Router.h"
#import "VLMacroDefine.h"
#import "VLToast.h"
#import "VLUserCenter.h"
#import "NSString+Helper.h"
#import "MenuUtils.h"
@import AFNetworking;
@import YYModel;
@import SVProgressHUD;
@import YYCategories;

#define NSStringFormat(format,...) [NSString stringWithFormat:format,##__VA_ARGS__]

@implementation VLAPIRequest

#pragma mark--init AFHTTPSessionManager
static NSMutableArray *_allSessionTask;
static AFHTTPSessionManager *_sessionManager;

+ (NSMutableArray *)allSessionTask {
    if (!_allSessionTask) {
        _allSessionTask = [[NSMutableArray alloc] init];
    }
    return _allSessionTask;
}

+ (void)load{
    [super load];
    [[AFNetworkReachabilityManager sharedManager] startMonitoring];
}

+ (void)initialize{
    [super initialize];
    
    _sessionManager = [AFHTTPSessionManager manager];
    
    _sessionManager.requestSerializer = [AFJSONRequestSerializer serializer];
    [_sessionManager.requestSerializer willChangeValueForKey:@"timeoutInterval"];
    _sessionManager.requestSerializer.timeoutInterval = Request_Timeout;
    [_sessionManager.requestSerializer didChangeValueForKey:@"timeoutInterval"];
    
    AFHTTPResponseSerializer *response = [AFHTTPResponseSerializer serializer];
    _sessionManager.responseSerializer = response;
    _sessionManager.responseSerializer.acceptableContentTypes = [NSSet setWithObjects:@"application/json", @"text/html", @"text/json", @"text/plain", @"text/javascript", @"text/xml", @"image/*", @"image/png",@"image/jpeg",nil];
    [_sessionManager.requestSerializer setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    
    /*
     
     public let kAppProjectName = "appProject"
     public let kAppProjectValue = "agora_ent_demo"
     public let kAppOS = "appOs"
     public let kAppOSValue = "iOS"
     public let kAppVersion = "versionName"
     */
    [_sessionManager.requestSerializer setValue:@"agora_ent_demo" forHTTPHeaderField:@"appProject"];
    [_sessionManager.requestSerializer setValue:@"iOS" forHTTPHeaderField:@"appOs"];
    [_sessionManager.requestSerializer setValue:[[UIApplication sharedApplication] appVersion] forHTTPHeaderField:@"versionName"];
}

#pragma mark--网络请求

+ (void)getRequestURL:(NSString *)url parameter:(id)parameter showHUD:(BOOL)show success:(completeBlock_success)success failure:(errorBlock_fail)failure {
    [self requestRoute:@"" showHUD:show method:url parameter:parameter requestType:VLRequestTypeGet progressBlock:nil completeBlock:success errorBlock:failure];
}

+ (void)postRequestURL:(NSString *)url parameter:(id)parameter showHUD:(BOOL)show success:(completeBlock_success)success failure:(errorBlock_fail)failure {
    [self requestRoute:@"" showHUD:show method:url parameter:parameter requestType:VLRequestTypePost progressBlock:nil completeBlock:success errorBlock:failure];
}


+ (void)requestRoute:(NSString *)route showHUD:(BOOL)show method:(NSString *)method parameter:(id)json requestType:(VLRequestType)type progressBlock:(progressBlock)progressBlock completeBlock:(completeBlock_success)completeBlock errorBlock:(errorBlock_fail)errorBlock {
    NSString *url = [self doRoute:route andMethod:method];
    url = [url stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
    if (json == nil) json = @{};
    
    NSURLSessionDataTask *sessionTask;

    [_sessionManager.requestSerializer setValue:[self getToken] forHTTPHeaderField:@"Authorization"];
    if (show) [SVProgressHUD show];
    if (type == VLRequestTypeGet) {
        NSDictionary *paramenter = [self setCommonParamenter:json];
        sessionTask = [_sessionManager GET:url parameters:paramenter headers:@{} progress:^(NSProgress * _Nonnull downloadProgress) {
            [self requestProgress:progressBlock value:downloadProgress];
        } success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
            if (show) [SVProgressHUD dismiss];
            NSDictionary *dic = [self dicWithResponseData:responseObject];
            VLResponseDataModel *model = [VLResponseDataModel yy_modelWithDictionary:dic];
            VLLog(@"\n完成请求:\n%@\nheader:\n%@\n参数:\n%@\n响应原数据:\n%@",task.currentRequest.URL,_sessionManager.requestSerializer.HTTPRequestHeaders,paramenter,dic);
            if (model.code == 401) [self setLoginVC];
            [self requestSuccess:completeBlock object:responseObject method:method task:task];
        } failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
            VLLog(@"\n完成请求:\n%@\nheader:\n%@\n参数:\n%@\n响应数据:%@",task.currentRequest.URL,_sessionManager.requestSerializer.HTTPRequestHeaders,paramenter,error);
            [SVProgressHUD dismiss];
            [self requestError:errorBlock error:error task:task];
            NSHTTPURLResponse *urlResponse = (NSHTTPURLResponse *)task.response;
            if (urlResponse.statusCode == 401) [self setLoginVC];
        }];
        
    } else if (type == VLRequestTypePost) {
        NSDictionary *paramenter = [self setCommonParamenter:json];
        sessionTask = [_sessionManager POST:url parameters:paramenter headers:@{} progress:^(NSProgress * _Nonnull uploadProgress) {
            [self requestProgress:progressBlock value:uploadProgress];
        } success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
            if (show) [SVProgressHUD dismiss];
            NSDictionary *dic = [self dicWithResponseData:responseObject];
            VLResponseDataModel *model = [VLResponseDataModel yy_modelWithDictionary:dic];
            VLLog(@"\n完成请求:\n%@\nheader:\n%@\n参数:\n%@\n响应原数据:\n%@",task.currentRequest.URL,_sessionManager.requestSerializer.HTTPRequestHeaders,paramenter,dic);
            if (model.code == 401) [self setLoginVC];
            [self requestSuccess:completeBlock object:responseObject method:method task:task];
        } failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
            VLLog(@"\n完成请求:\n%@\nheader:\n%@\n参数:\n%@\n响应数据:%@",task.currentRequest.URL,_sessionManager.requestSerializer.HTTPRequestHeaders,paramenter,error);
            [SVProgressHUD dismiss];
            [self requestError:errorBlock error:error task:task];
            NSHTTPURLResponse *urlResponse = (NSHTTPURLResponse *)task.response;
            if (urlResponse.statusCode == 401) [self setLoginVC];
        }];
        [self addSessionTask:sessionTask];
    }
}
+ (void)requestImageRoute:(NSString *)route  method:(NSString *)method parameter:(id)json requestType:(VLRequestType)type progressBlock:(progressBlock)progressBlock completeBlock:(completeImageBlock_success)completeBlock errorBlock:(errorBlock_fail)errorBlock {
    
    NSString *url = [self doRoute:route andMethod:method];
    url = [url stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
    if (json == nil) json = @{};
    
    NSURLSessionDataTask *sessionTask;
    //设置请求头
    [_sessionManager.requestSerializer setValue:[self getToken] forHTTPHeaderField:@"Authorization"];

    if (type == VLRequestTypeGet) {
        // GET
        NSDictionary *paramenter = [self setCommonParamenter:json];

        sessionTask = [_sessionManager GET:url parameters:paramenter headers:@{} progress:^(NSProgress * _Nonnull downloadProgress) {
            [self requestProgress:progressBlock value:downloadProgress];
        } success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
//            NSString *resultStr = [[NSString alloc] initWithData:responseObject encoding:NSUTF8StringEncoding];
//            UIImage *image = [UIImage imageWithData:responseObject];
            [self requestImageSuccess:completeBlock object:responseObject method:method task:task];
        } failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
            NSLog(@"GET REQUEST ERROR\n,url=%@\n,error=%@",task.currentRequest.URL,error);
            [self requestError:errorBlock error:error task:task];
        }];
        
    }

}

+ (NSDictionary *)dicWithResponseData:(NSData *)res {
    NSError *err;
    NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:res
                                                         options:NSJSONReadingMutableContainers error:&err];
    if (err) {
        VLLog(@"responseData transform dictionary error!");
    }
    return dic;
}

#pragma mark--上传文件
+ (__kindof NSURLSessionDataTask *)uploadFileRoute:(NSString *)route
                                            method:(NSString *)method
                                        parameters:(id)json
                                              name:(NSString *)name
                                          filePath:(NSString *)filePath
                                     progressBlock:(progressBlock)progressBlock
                                     completeBlock:(completeBlock_success)completeBlock
                                        errorBlock:(errorBlock_fail)errorBlock {
    
    NSString *url = [self doRoute:route andMethod:method];
    url = [url stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
    if (json == nil) json = @{};
    
    NSURLSessionDataTask *sessionTask = [_sessionManager POST:url parameters:json headers:@{} constructingBodyWithBlock:^(id<AFMultipartFormData>  _Nonnull formData) {
        NSError *error = nil;
        [formData appendPartWithFileURL:[NSURL URLWithString:filePath] name:name error:&error];
        error ? [self requestError:errorBlock error:error task:nil] : nil;
    } progress:^(NSProgress * _Nonnull uploadProgress) {
        //上传进度
        [self requestProgress:progressBlock value:uploadProgress];

    } success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
        [self requestSuccess:completeBlock object:responseObject method:method task:task];

    } failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
        [self requestError:errorBlock error:error task:task];

    }];
    // 添加sessionTask到数组
    [self addSessionTask:sessionTask];
    return sessionTask;
}

#pragma mark--上传图片

+ (NSURLSessionDataTask *)uploadImageURL:(NSString *)url showHUD:(BOOL)show appendKey:(NSString *)key images:(NSArray<UIImage *> *)images success:(completeBlock_success)success failure:(errorBlock_fail)failure {
    return [self uploadImagesRoute:@"" method:url showHUD:show parameters:@{} name:key images:images fileNames:@[] imageScale:0 imageType:@"" progressBlock:nil completeBlock:success errorBlock:failure];
}

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
                                          errorBlock:(errorBlock_fail)errorBlock{
    NSString *url = [self doRoute:route andMethod:method];
    url = [url stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
    if (json == nil) json = @{};
    
    // 设置公共参数
    NSDictionary *paramenter = [self setCommonParamenter:json];
    UIWindow *window = [UIApplication sharedApplication].delegate.window;
    if (show) [SVProgressHUD show];
    NSURLSessionDataTask *sessionTask = [_sessionManager POST:url parameters:paramenter headers:@{} constructingBodyWithBlock:^(id<AFMultipartFormData>  _Nonnull formData) {
        for (int i = 0; i < images.count; i++) {
            // 图片压缩
            NSData *imageData = UIImageJPEGRepresentation(images[i], imageScale ?: 1.f);
            // 自定义图片名
            NSString *fileName;
            if (fileNames && fileNames.count == images.count) {
                fileName = fileNames[i];
            }else{
                NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
                formatter.dateFormat = @"yyyyMMddHHmmss";
                NSString *time = [formatter stringFromDate:[NSDate date]];
                fileName = [NSString stringWithFormat:@"%@%u",time,i];
            }
            // 图片类型
            NSString *type = imageType.length > 0 ? imageType : @"jpg";
            [formData appendPartWithFileData:imageData
                                        name:@"file"
                                    fileName:[NSString stringWithFormat:@"%@.%@",fileName,type]
                                    mimeType:[NSString stringWithFormat:@"image/%@",type]];
        }
    } progress:^(NSProgress * _Nonnull uploadProgress) {
        //上传进度
        [self requestProgress:progressBlock value:uploadProgress];

    } success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
        if (show) [SVProgressHUD dismiss];
        NSString *resultStr = [[NSString alloc] initWithData:responseObject encoding:NSUTF8StringEncoding];
        VLResponseDataModel *model = [VLResponseDataModel yy_modelWithJSON:resultStr];
        VLLog(@"\n完成请求:\n%@\nheader:\n%@\n参数:\n%@\n响应原数据:\n%@\n响应模型:%@",task.currentRequest.URL,_sessionManager.requestSerializer.HTTPRequestHeaders,paramenter,resultStr,[model yy_modelDescription]);
        [self requestSuccess:completeBlock object:responseObject method:method task:task];
    } failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
        if (show) [SVProgressHUD dismiss];
        VLLog(@"\n完成请求:\n%@\nheader:\n%@\n参数:\n%@\n响应数据:%@",task.currentRequest.URL,_sessionManager.requestSerializer.HTTPRequestHeaders,paramenter,error);
        [self requestError:errorBlock error:error task:task];

    }];
    // 添加sessionTask到数组
    [self addSessionTask:sessionTask];
    return sessionTask;
}

#pragma mark--下载文件
+ (__kindof NSURLSessionDownloadTask *)downloadRoute:(NSString *)route
                                              method:(NSString *)method
                                             fileDir:(NSString *)fileDir
                                       progressBlock:(progressBlock)progressBlock
                                       completeBlock:(void(^)(NSString *filePath))completeBlock
                                          errorBlock:(errorBlock_fail)errorBlock{
    
    NSString *url = [self doRoute:route andMethod:method];
    url = [url stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
    
    NSURLRequest *request = [NSURLRequest requestWithURL:[NSURL URLWithString:url]];
    __block NSURLSessionDownloadTask *downloadTask = [_sessionManager downloadTaskWithRequest:request progress:^(NSProgress * _Nonnull downloadProgress) {
        [self requestProgress:progressBlock value:downloadProgress];
    } destination:^NSURL * _Nonnull(NSURL * _Nonnull targetPath, NSURLResponse * _Nonnull response) {
        NSString *downloadDir = [[NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) lastObject] stringByAppendingPathComponent:fileDir ? fileDir : @"Download"];
        NSFileManager *fileManager = [NSFileManager defaultManager];
        [fileManager createDirectoryAtPath:downloadDir withIntermediateDirectories:YES attributes:nil error:nil];
        NSString *filePath = [downloadDir stringByAppendingPathComponent:response.suggestedFilename];
        return [NSURL fileURLWithPath:filePath];
        
    } completionHandler:^(NSURLResponse * _Nonnull response, NSURL * _Nullable filePath, NSError * _Nullable error) {
        
        [[self allSessionTask] removeObject:downloadTask];
        if(errorBlock && error) {errorBlock(error, nil) ; return ;};
        completeBlock ? completeBlock(filePath.absoluteString /** NSURL->NSString*/) : nil;
        
    }];
    [downloadTask resume];
    downloadTask ? [[self allSessionTask] addObject:downloadTask] : nil ;
    
    return downloadTask;
}

#pragma mark--公共
// 配置公共参数
+ (NSDictionary *)setCommonParamenter:(NSDictionary *)parament{
    NSMutableDictionary *dic = [parament mutableCopy];
    return dic;
}

+ (NSString *)setCommonParamenterString:(NSDictionary *)parament{
    
    return [NSString convertToJsonData:parament];
}

#pragma mark - 处理token失效后切换登录界面
+ (void)setLoginVC {
    [VLToast toast:AGLocalizedString(@"Token已失效，请重新登录")];
    // TODO: goto login page
    [[VLUserCenter center] logout];
    [[UIApplication sharedApplication].delegate.window configRootViewController];
}

// 请求进度
+ (void)requestProgress:(progressBlock)progressBlock value:(NSProgress *)value{
    dispatch_async(dispatch_get_main_queue(), ^{
        CGFloat values = value.fractionCompleted;
        if (progressBlock) {
            progressBlock(values);
        }
    });
}

// 请求成功
+ (void)requestSuccess:(completeBlock_success)completeBlock object:(id)object method:(NSString *)method task:(NSURLSessionDataTask *)task{
    NSString *resultStr = [[NSString alloc] initWithData:object encoding:NSUTF8StringEncoding];
    VLResponseDataModel *model = [VLResponseDataModel yy_modelWithJSON:resultStr];
    if (!model) {
        model = [[VLResponseDataModel alloc]init];
        model.data = resultStr;
    }
    
    completeBlock(model);
    task ? [self removeSessionTask:task] : nil;
}
// 请求图片成功
+ (void)requestImageSuccess:(completeImageBlock_success)completeBlock object:(id)object method:(NSString *)method task:(NSURLSessionDataTask *)task{
    UIImage *image = [UIImage imageWithData:object];
    if (!image) {
    }
    completeBlock(image);
    task ? [self removeSessionTask:task] : nil;
}

// 请求失败
+ (void)requestError:(errorBlock_fail)errorBlock error:(NSError *)error task:(NSURLSessionDataTask *)task{
    errorBlock(error, task);
    task ? [self removeSessionTask:task] : nil;
}

// 添加到请求队列
+ (void)addSessionTask:(NSURLSessionDataTask *)task{
    [UIApplication sharedApplication].networkActivityIndicatorVisible = YES;
    // 添加最新的sessionTask到数组
    task ? [[self allSessionTask] addObject:task] : nil ;
}

// 从请求队列中移除
+ (void)removeSessionTask:(NSURLSessionDataTask *)task{
    [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
    task ? [[self allSessionTask] removeObject:task] : nil;
}

#pragma mark--其他
+ (void)cancelAllRequest {
    
    @synchronized(self) {
        [[self allSessionTask] enumerateObjectsUsingBlock:^(NSURLSessionTask  *_Nonnull task, NSUInteger idx, BOOL * _Nonnull stop) {
            [task cancel];
        }];
        [[self allSessionTask] removeAllObjects];
    }
}

+ (void)cancelRequestWithURL:(NSString *)URL {
    if (!URL) { return; }
    @synchronized (self) {
        [[self allSessionTask] enumerateObjectsUsingBlock:^(NSURLSessionTask  *_Nonnull task, NSUInteger idx, BOOL * _Nonnull stop) {
            if ([task.currentRequest.URL.absoluteString containsString:URL]) {
                [task cancel];
                [[self allSessionTask] removeObject:task];
                *stop = YES;
            }
        }];
    }
}

@end

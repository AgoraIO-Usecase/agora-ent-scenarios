//
//  VLBaseHander.h
//  VoiceOnLine
//

#ifndef VLBaseHander_h
#define VLBaseHander_h
typedef void(^ Success)(id _Nullable json);
typedef void(^ Failure)(NSString * _Nonnull errorMSG);
typedef void (^FailureBlock)(id _Nullable obj);
typedef void (^LoginCompletionBlock)(void);
#endif /* VLBaseHander_h */

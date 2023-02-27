//
//  KTVDebugInfo.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/2/17.
//

#import <Foundation/Foundation.h>

OBJC_EXPORT NSString* _Nullable const kTitleKey;
OBJC_EXPORT const NSString* _Nullable const kSelectedParamKey;
OBJC_EXPORT const NSString* _Nullable const kUnselectedParamKey;
NS_ASSUME_NONNULL_BEGIN

@interface KTVDebugInfo : NSObject
+ (NSArray*)debugDataArray;
+ (NSDictionary*)allProfiles;
+ (void)updateProfiles:(NSDictionary*)profiles;
+ (BOOL)getSelectedStatusForKey:(NSString*)key;
+ (void)setSelectedStatus:(BOOL)status forKey:(NSString*)ke;
@end

NS_ASSUME_NONNULL_END

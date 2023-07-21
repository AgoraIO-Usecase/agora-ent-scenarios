//
//  SRDebugInfo.h
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/2/17.
//

#import <Foundation/Foundation.h>

OBJC_EXPORT NSString* _Nullable const rTitleKey;
OBJC_EXPORT const NSString* _Nullable const rSelectedParamKey;
OBJC_EXPORT const NSString* _Nullable const rUnselectedParamKey;
NS_ASSUME_NONNULL_BEGIN

@interface SRDebugInfo : NSObject
+ (NSArray*)debugDataArray;
+ (NSDictionary*)allProfiles;
+ (void)updateProfiles:(NSDictionary*)profiles;
+ (BOOL)getSelectedStatusForKey:(NSString*)key;
+ (void)setSelectedStatus:(BOOL)status forKey:(NSString*)ke;
@end

NS_ASSUME_NONNULL_END

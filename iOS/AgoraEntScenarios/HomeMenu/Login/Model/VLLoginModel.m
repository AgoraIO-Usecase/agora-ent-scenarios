//
//  VLLoginModel.m
//  VoiceOnLine
//

#import "VLLoginModel.h"

@implementation VLLoginModel
@synthesize extraDic = _extraDic;

- (NSMutableDictionary*)extraDic {
    if (_extraDic == nil) {
        _extraDic = [NSMutableDictionary dictionaryWithCapacity:2];
    }
    
    return _extraDic;
}
@end

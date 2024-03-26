//
//  BEEffectResourceHelper.m
//  Effect
//
//  Created by qun on 2021/5/18.
//

#import "BEEffectResourceHelper.h"
#import "BELicenseHelper.h"
#import "BundleUtil.h"
#import "BEDynmicResourceConfig.h"

static NSString *LICENSE_PATH = @"LicenseBag";
static NSString *COMPOSER_PATH = @"ComposeMakeup";
static NSString *FILTER_PATH = @"FilterResource";
static NSString *STICKER_PATH = @"StickerResource";
static NSString *MODEL_PATH = @"ModelResource";
static NSString *VIDEOSR_PATH = @"videovrsr";

static NSString *BUNDLE = @"bundle";

@interface BEEffectResourceHelper () {
    NSString            *_licensePrefix;
    NSString            *_composerPrefix;
    NSString            *_filterPrefix;
    NSString            *_stickerPrefix;
}

@end

@implementation BEEffectResourceHelper

- (NSString *)composerNodePath:(NSString *)nodeName {
    if (!_composerPrefix) {
        NSString* resourceFolderPath = [BEDynmicResourceConfig shareInstance].resourceFolderPath;
        _composerPrefix = [NSString stringWithFormat:@"%@/Resource/%@.%@/ComposeMakeup/",
                           resourceFolderPath,
                           COMPOSER_PATH,
                           BUNDLE];
        if (![[NSFileManager defaultManager] fileExistsAtPath:_composerPrefix]) {
            NSBundle *bundle = [BundleUtil bundleWithBundleName:@"ByteEffectLib" podName:@"bytedEffect"];
            _composerPrefix = [[bundle pathForResource:COMPOSER_PATH ofType:BUNDLE] stringByAppendingString:@"/ComposeMakeup/"];
            
        }
    }
    if ([nodeName containsString:_composerPrefix]) {
        return nodeName;
    }
    return [_composerPrefix stringByAppendingString:nodeName];
}

- (NSString *)filterPath:(NSString *)filterName {
    if (!_filterPrefix) {
        NSString* resourceFolderPath = [BEDynmicResourceConfig shareInstance].resourceFolderPath;
        _composerPrefix = [NSString stringWithFormat:@"%@/Resource/%@.%@/Filter/",
                           resourceFolderPath,
                           FILTER_PATH,
                           BUNDLE];
        if (![[NSFileManager defaultManager] fileExistsAtPath:_composerPrefix]) {
            NSBundle *bundle = [BundleUtil bundleWithBundleName:@"ByteEffectLib" podName:@"bytedEffect"];
            _filterPrefix = [[bundle pathForResource:FILTER_PATH ofType:BUNDLE] stringByAppendingFormat:@"/Filter/"];
        }
    }
    return [_filterPrefix stringByAppendingString:filterName];
}

- (NSString *)stickerPath:(NSString *)stickerName {
    if (!_stickerPrefix) {
        NSString* resourceFolderPath = [BEDynmicResourceConfig shareInstance].resourceFolderPath;
        _stickerPrefix = [NSString stringWithFormat:@"%@/Resource/%@.%@/stickers/",
                           resourceFolderPath,
                           STICKER_PATH,
                           BUNDLE];
        if (![[NSFileManager defaultManager] fileExistsAtPath:_stickerPrefix]) {
            NSBundle *bundle = [BundleUtil bundleWithBundleName:@"ByteEffectLib" podName:@"bytedEffect"];
            _stickerPrefix = [[bundle pathForResource:STICKER_PATH ofType:BUNDLE] stringByAppendingString:@"/stickers/"];
        }
    }
    return [_stickerPrefix stringByAppendingString:stickerName];
}

- (NSString *)composerDirPath {
    if (!_composerPrefix) {
        NSString* resourceFolderPath = [BEDynmicResourceConfig shareInstance].resourceFolderPath;
        _composerPrefix = [NSString stringWithFormat:@"%@/Resource/%@.%@/ComposeMakeup/",
                           resourceFolderPath,
                           COMPOSER_PATH,
                           BUNDLE];
        if (![[NSFileManager defaultManager] fileExistsAtPath:_composerPrefix]) {
            NSBundle *bundle = [BundleUtil bundleWithBundleName:@"ByteEffectLib" podName:@"bytedEffect"];
            _composerPrefix = [[bundle pathForResource:COMPOSER_PATH ofType:BUNDLE] stringByAppendingString:@"/ComposeMakeup/"];
        }
    }
    return [_composerPrefix stringByAppendingString:@"/composer"];
}

- (const char *)modelDirPath {
    NSString* resourceFolderPath = [BEDynmicResourceConfig shareInstance].resourceFolderPath;
    NSString* path = [NSString stringWithFormat:@"%@/Resource/%@.%@",
                      resourceFolderPath,
                      MODEL_PATH,
                      BUNDLE];
    if (![[NSFileManager defaultManager] fileExistsAtPath:path]) {
        NSBundle *bundle = [BundleUtil bundleWithBundleName:@"ByteEffectLib" podName:@"bytedEffect"];
        path = [bundle pathForResource:MODEL_PATH ofType:BUNDLE];
    }
    
    return [path UTF8String];
}

- (NSString *)videoSRModelPath
{
    NSString* resourceFolderPath = [BEDynmicResourceConfig shareInstance].resourceFolderPath;
    NSString* path = [NSString stringWithFormat:@"%@/Resource/%@.%@",
                      resourceFolderPath,
                      VIDEOSR_PATH,
                      BUNDLE];
    if (![[NSFileManager defaultManager] fileExistsAtPath:path]) {
        NSBundle *bundle = [BundleUtil bundleWithBundleName:@"ByteEffectLib" podName:@"bytedEffect"];
        path = [bundle pathForResource:VIDEOSR_PATH ofType:BUNDLE];
    }
    
    return path;
}

@end

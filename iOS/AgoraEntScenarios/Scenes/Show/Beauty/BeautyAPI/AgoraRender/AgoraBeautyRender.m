//
//  AgoraBeautyRender.m
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/11/10.
//

#import "AgoraBeautyRender.h"

@implementation AgoraBeautyRender

- (void)destroy {
}

- (AgoraVideoFormat)getVideoFormatPreference {
    return AgoraVideoFormatCVPixelBGRA;
}

- (nonnull CVPixelBufferRef)onCapture:(nonnull CVPixelBufferRef)pixelBuffer {
    return nil;
}

- (void)setBeautyPreset {
}

- (void)reset {
}

@end

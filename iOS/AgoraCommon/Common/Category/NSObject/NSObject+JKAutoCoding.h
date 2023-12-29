// AutoCoding.h
//
// Version 2.2.1
//
// Created by Nick Lockwood on 19/11/2011.
// Copyright (c) 2011 Charcoal Design
//
// Distributed under the permissive zlib License
// Get the latest version from here:
//
// https://github.com/nicklockwood/AutoCoding
//
// This software is provided 'as-is', without any express or implied
// warranty. In no event will the authors be held liable for any damages
// arising from the use of this software.
//
// Permission is granted to anyone to use this software for any purpose,
// including commercial applications, and to alter it and redistribute it
// freely, subject to the following restrictions:
//
// 1. The origin of this software must not be misrepresented; you must not
// claim that you wrote the original software. If you use this software
// in a product, an acknowledgment in the product documentation would be
// appreciated but is not required.
//
// 2. Altered source versions must be plainly marked as such, and must not be
// misrepresented as being the original software.
//
// 3. This notice may not be removed or altered from any source distribution.
//
#import <Foundation/Foundation.h>
@interface NSObject (JKAutoCoding) <NSSecureCoding>
//coding
+ (NSDictionary *)jk_codableProperties;
- (void)jk_setWithCoder:(NSCoder *)aDecoder;
//property access
- (NSDictionary *)jk_codableProperties;
- (NSDictionary *)jk_dictionaryRepresentation;
//loading / saving
+ (instancetype)jk_objectWithContentsOfFile:(NSString *)path;
- (BOOL)jk_writeToFile:(NSString *)filePath atomically:(BOOL)useAuxiliaryFile;
@end
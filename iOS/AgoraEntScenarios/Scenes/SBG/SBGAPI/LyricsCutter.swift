//
//  LyricsCutter.swift
//  Demo
//
//  Created by ZYP on 2023/4/25.
//

import Foundation
import AgoraLyricsScore

class LyricsCutter {
    /// 每一行歌词的信息
    public struct Line {
        var beginTime: Int
        var duration: Int
        var endTime: Int {
            return beginTime + duration
        }
    }
    
    /// 处理时间副歌片段时间（对齐句子）
    /// - Parameters:
    ///   - startTime: 副歌开始时间
    ///   - endTime: 副歌结束时间
    ///   - lines: 歌词所有行数据
    /// - Returns: (startTime, endTime), nil为无法处理
    public static func handleFixTime(startTime: Int,
                                     endTime: Int,
                                     lines: [Line]) -> (Int, Int)? {
        guard startTime < endTime else {
            return nil
        }
        
        guard startTime != endTime else {
            return nil
        }
        
        guard !lines.isEmpty else {
            return nil
        }
        
        var start = startTime
        var end = endTime
        
        if start < lines.first!.beginTime,
            end < lines.first!.beginTime {
            return nil
        }
        
        if start > lines.last!.endTime,
           end > lines.last!.endTime {
            return nil
        }
        
        /// 跨过第一个
        if start < lines.first!.beginTime, end < lines.first!.endTime {
            start = lines.first!.beginTime
            end = lines.first!.endTime
            return (start, end)
        }
        
        /// 跨过最后一个
        if start > lines.last!.beginTime,
           end > lines.last!.endTime {
            start = lines.last!.beginTime
            end = lines.last!.endTime
            return (start, end)
        }
        
        if start < lines.first!.beginTime {
            start = lines.first!.beginTime
        }
        
        if end > lines.last!.endTime {
            end = lines.last!.endTime
        }
        
        var startIndex = 0
        var startGap = Int.max
        var endIndex = 0
        var endGap = Int.max
        for (offset, line) in lines.enumerated() {
            if abs(line.beginTime - start) < startGap {
                startGap = abs(line.beginTime - start)
                startIndex = offset
            }
            if abs(line.endTime - end) < endGap {
                endGap = abs(line.endTime - end)
                endIndex = offset
            }
        }
        
        let result = (lines[startIndex].beginTime, lines[endIndex].endTime)
        if result.0 < result.1 { /** valid **/
            return result
        }
        return nil
    }
    
    /// 裁剪副歌片段
    /// - Parameters:
    ///   - model: 歌词模型
    ///   - start: 副歌开始时间
    ///   - end: 副歌结束时间
    /// - Returns: 副歌模型
    public static func cut(model: LyricModel, startTime: Int, endTime: Int) -> LyricModel {
        var lines = [LyricLineModel]()
        
        var flag = false
        for line in model.lines {
            if line.beginTime == startTime {
                flag = true
            }
            if line.beginTime + line.duration == endTime {
                lines.append(line)
                break
            }
            if flag {
                lines.append(line)
            }
        }
        
        model.lines = lines
        model.preludeEndPosition = 0
        model.duration = (lines.last?.beginTime ?? 0) + (lines.last?.duration ?? 0)
        return model
    }
}

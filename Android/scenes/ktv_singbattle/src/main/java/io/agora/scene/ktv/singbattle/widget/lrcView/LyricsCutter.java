package io.agora.scene.ktv.singbattle.widget.lrcView;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import io.agora.karaoke_view_ex.internal.model.LyricsLineModel;
import io.agora.karaoke_view_ex.model.LyricModel;
import io.agora.scene.ktv.singbattle.KTVLogger;

/**
 * 歌曲裁剪
 */
public class LyricsCutter {

    static class Line {
        private int beginTime;
        private int duration;

        public Line(long beginTime, long duration) {
            this.beginTime = (int) beginTime;
            this.duration = (int) duration;
        }

        public int getBeginTime() {
            return beginTime;
        }

        public int getDuration() {
            return duration;
        }

        public int getEndTime() {
            return beginTime + duration;
        }
    }

    private static String tag = "LyricsCutter";

    // 处理时间副歌片段时间（对齐句子）
    public static Pair<Integer, Integer> handleFixTime(int startTime, int endTime, List<Line> lines) {
        if (startTime >= endTime || lines.isEmpty()) {
            return null;
        }

        int start = startTime;
        int end = endTime;

        Line firstLine = lines.get(0);
        Line lastLine = lines.get(lines.size() - 1);

        if ((start < firstLine.getBeginTime() && end < firstLine.getBeginTime()) ||
                (start > lastLine.getEndTime() && end > lastLine.getEndTime())) {
            return null;
        }

        // 跨过第一个
        if (start < firstLine.getBeginTime() && end < firstLine.getEndTime()) {
            start = firstLine.getBeginTime();
            end = firstLine.getEndTime();
            return new Pair<>(start, end);
        }
        // 跨过最后一个
        if (start > lastLine.getBeginTime() && end > lastLine.getEndTime()) {
            start = lastLine.getBeginTime();
            end = lastLine.getEndTime();
            return new Pair<>(start, end);
        }

        if (start < firstLine.getBeginTime()) {
            start = firstLine.getBeginTime();
        }
        if (end > lastLine.getEndTime()) {
            end = lastLine.getEndTime();
        }

        int startIndex = 0;
        int startGap = Integer.MAX_VALUE;
        int endIndex = 0;
        int endGap = Integer.MAX_VALUE;

        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            int currentStartGap = Math.abs(line.getBeginTime() - start);
            int currentEndGap = Math.abs(line.getEndTime() - end);

            if (currentStartGap < startGap) {
                startGap = currentStartGap;
                startIndex = i;
            }
            if (currentEndGap < endGap) {
                endGap = currentEndGap;
                endIndex = i;
            }
        }

        Line startLine = lines.get(startIndex);
        Line endLine = lines.get(endIndex);
        if (startLine.getBeginTime() < endLine.getEndTime()) {
            return new Pair<>(startLine.getBeginTime(), endLine.getEndTime());
        }
        return null;
    }

    // 裁剪副歌片段
    public static LyricModel cut(LyricModel model, int startTime, int endTime) {
        KTVLogger.d(tag, "cut, startTime:" + startTime + "endTime:" + endTime);
        List<LyricsLineModel> lines = new ArrayList<>();
        boolean flag = false;

        for (LyricsLineModel line : model.lines) {
            if (line.getStartTime() == startTime) {
                flag = true;
            }
            if (line.getEndTime() == endTime) {
                lines.add(line);
                break;
            }
            if (flag) {
                lines.add(line);
            }
        }

        model.lines = lines;
        if (lines.isEmpty()) {
            model.duration = 0;
        } else {
            LyricsLineModel lastLine = lines.get(lines.size() - 1);
            model.duration = lastLine.getEndTime()/* - lastLine.getStartTime()*/;
        }

        return model;
    }
}

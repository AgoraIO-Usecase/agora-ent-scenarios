/*
 * Copyright © Sud.Tech
 * https://sud.tech
 */

package tech.sud.mgp.SudMGPWrapper.model;

/**
 * 游戏视图
 * 参考文档：https://docs.sud.tech/zh-CN/app/Client/API/ISudFSMMG/onGetGameViewInfo.html
 */
public class GameViewInfoModel {
    // 返回码
    public int ret_code;

    // 返回消息
    public String ret_msg;

    // 游戏View的大小
    public GameViewSizeModel view_size = new GameViewSizeModel();

    // 游戏安全操作区域
    public GameViewRectModel view_game_rect = new GameViewRectModel();

    public static class GameViewSizeModel {
        // 游戏View的宽 （单位像素）
        public int width;

        // 游戏View的高 （单位像素）
        public int height;
    }

    public static class GameViewRectModel {
        // 相对于view_size左边框偏移（单位像素）
        public int left;
        // 相对于view_size上边框偏移（单位像素）
        public int top;
        // 相对于view_size右边框偏移（单位像素）
        public int right;
        // 相对于view_size下边框偏移（单位像素）
        public int bottom;
    }

}

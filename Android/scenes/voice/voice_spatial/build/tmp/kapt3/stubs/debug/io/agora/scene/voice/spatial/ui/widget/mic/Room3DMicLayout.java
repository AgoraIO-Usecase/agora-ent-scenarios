package io.agora.scene.voice.spatial.ui.widget.mic;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u00d6\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010%\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0006\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0007\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\b\f\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u0000 q2\u00020\u00012\u00020\u00022\u00020\u0003:\u0001qB\u000f\b\u0016\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006B\u0019\b\u0016\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\u0010\u0007\u001a\u0004\u0018\u00010\b\u00a2\u0006\u0002\u0010\tB!\b\u0016\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\u0010\u0007\u001a\u0004\u0018\u00010\b\u0012\u0006\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\fB)\b\u0016\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\u0010\u0007\u001a\u0004\u0018\u00010\b\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\r\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\u000eJ8\u00102\u001a\u0002032\u0006\u00104\u001a\u0002052&\u00106\u001a\"\u0012\u0004\u0012\u00020\u000b\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u000209\u0012\u0004\u0012\u00020908\u0012\u0004\u0012\u000203\u0018\u000107H\u0016J\b\u0010:\u001a\u000205H\u0002J\u0018\u0010;\u001a\u0002052\u0006\u0010<\u001a\u00020\u000b2\u0006\u0010=\u001a\u00020\u000bH\u0002J\u0010\u0010>\u001a\u0002092\u0006\u0010?\u001a\u000209H\u0002J\u0010\u0010@\u001a\u00020\u000b2\u0006\u0010<\u001a\u00020\u000bH\u0002J\u0010\u0010A\u001a\u00020\u000b2\u0006\u0010=\u001a\u00020\u000bH\u0002J\u0010\u0010B\u001a\u00020\u000b2\u0006\u0010C\u001a\u00020DH\u0016J\u0018\u0010E\u001a\u00020*2\u0006\u0010F\u001a\u00020,2\u0006\u0010G\u001a\u00020,H\u0002J\u0010\u0010H\u001a\u0002092\u0006\u0010I\u001a\u00020\u000bH\u0002J\u0010\u0010J\u001a\u0002092\u0006\u0010K\u001a\u00020LH\u0002J\u0010\u0010M\u001a\u00020N2\u0006\u0010K\u001a\u00020LH\u0002J\u0018\u0010O\u001a\u0002052\u0006\u0010P\u001a\u00020Q2\u0006\u0010R\u001a\u00020QH\u0002J\u0010\u0010S\u001a\u0002032\u0006\u0010\u0004\u001a\u00020\u0005H\u0002J\b\u0010T\u001a\u000203H\u0002J\b\u0010#\u001a\u00020\u000bH\u0016J\u0012\u0010U\u001a\u0002032\b\u0010V\u001a\u0004\u0018\u00010LH\u0016J.\u0010W\u001a\u0002032\f\u0010X\u001a\b\u0012\u0004\u0012\u00020\u001d0Y2\u0006\u0010Z\u001a\u0002052\u000e\u0010[\u001a\n\u0012\u0004\u0012\u000203\u0018\u00010\\H\u0016J\u0010\u0010]\u001a\u0002052\u0006\u0010^\u001a\u00020_H\u0016J0\u0010&\u001a\u00020\u00002\f\u0010&\u001a\b\u0012\u0004\u0012\u00020\u001d0%2\f\u0010$\u001a\b\u0012\u0004\u0012\u00020\u001d0%2\f\u0010\'\u001a\b\u0012\u0004\u0012\u00020\u001d0(J,\u0010`\u001a\u0002032\u0012\u0010a\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u001d0b2\u000e\u0010[\u001a\n\u0012\u0004\u0012\u000203\u0018\u00010\\H\u0016J\u0010\u0010c\u001a\u0002052\u0006\u0010^\u001a\u00020_H\u0016J\u0018\u0010d\u001a\u0002032\u0006\u0010e\u001a\u00020L2\u0006\u0010f\u001a\u000205H\u0002J\u000e\u0010g\u001a\u0002032\u0006\u0010h\u001a\u00020\u000bJ\u0006\u0010i\u001a\u000203J\u0018\u0010j\u001a\u0002032\u0006\u0010k\u001a\u00020\u000b2\u0006\u0010l\u001a\u00020\u000bH\u0016J\u0010\u0010m\u001a\u0002032\u0006\u0010n\u001a\u00020oH\u0016J\u0018\u0010p\u001a\u0002032\u0006\u0010I\u001a\u00020\u000b2\u0006\u0010l\u001a\u00020\u000bH\u0016R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0015\u001a\u00020\u00168BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0019\u0010\u001a\u001a\u0004\b\u0017\u0010\u0018R\u001a\u0010\u001b\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u001d0\u001cX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u001e\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u001f0\u001cX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010 \u001a\u00020\u00168BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\"\u0010\u001a\u001a\u0004\b!\u0010\u0018R\u000e\u0010#\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0016\u0010$\u001a\n\u0012\u0004\u0012\u00020\u001d\u0018\u00010%X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0016\u0010&\u001a\n\u0012\u0004\u0012\u00020\u001d\u0018\u00010%X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0016\u0010\'\u001a\n\u0012\u0004\u0012\u00020\u001d\u0018\u00010(X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010)\u001a\u00020*X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010+\u001a\u00020,X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010-\u001a\u00020.X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001b\u0010/\u001a\u00020\u00168BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b1\u0010\u001a\u001a\u0004\b0\u0010\u0018\u00a8\u0006r"}, d2 = {"Lio/agora/scene/voice/spatial/ui/widget/mic/Room3DMicLayout;", "Landroidx/constraintlayout/widget/ConstraintLayout;", "Landroid/view/View$OnClickListener;", "Lio/agora/scene/voice/spatial/ui/widget/mic/IRoomMicView;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "attrs", "Landroid/util/AttributeSet;", "(Landroid/content/Context;Landroid/util/AttributeSet;)V", "defStyleAttr", "", "(Landroid/content/Context;Landroid/util/AttributeSet;I)V", "defStyleRes", "(Landroid/content/Context;Landroid/util/AttributeSet;II)V", "binding", "Lio/agora/scene/voice/spatial/databinding/VoiceSpatialViewRoom3dMicLayoutBinding;", "constraintSet", "Landroidx/constraintlayout/widget/ConstraintSet;", "lastX", "lastY", "maxTranslationScope", "Landroid/util/Size;", "getMaxTranslationScope", "()Landroid/util/Size;", "maxTranslationScope$delegate", "Lkotlin/Lazy;", "micInfoMap", "", "Lio/agora/scene/voice/spatial/model/VoiceMicInfoModel;", "micViewMap", "Lio/agora/scene/voice/spatial/ui/widget/mic/IRoomMicBinding;", "micViewSize", "getMicViewSize", "micViewSize$delegate", "myRtcUid", "onBotClickListener", "Lio/agora/voice/common/ui/adapter/listener/OnItemClickListener;", "onItemClickListener", "onSpatialMoveListener", "Lio/agora/scene/voice/spatial/model/OnItemMoveListener;", "preAngle", "", "preMovePoint", "Landroid/graphics/Point;", "preTime", "", "rootSize", "getRootSize", "rootSize$delegate", "activeBot", "", "active", "", "each", "Lkotlin/Function2;", "Lkotlin/Pair;", "Landroid/graphics/PointF;", "canMove", "check3DMicChildView", "x", "y", "convertPoint", "point", "correctMotionEventX", "correctMotionEventY", "findMicByUid", "uid", "", "getAngle", "curP", "preP", "getForward", "index", "getPosition", "view", "Landroid/view/View;", "getRect", "Landroid/graphics/RectF;", "ignoreSmallOffsets", "dx", "", "dy", "init", "initListeners", "onClick", "v", "onInitMic", "micInfoList", "", "isBotActive", "complete", "Lkotlin/Function0;", "onInterceptTouchEvent", "event", "Landroid/view/MotionEvent;", "onSeatUpdated", "newMicMap", "", "onTouchEvent", "setChildView", "childView", "isClickable", "setMyRtcUid", "rtcUid", "setUpInitMicInfoMap", "updateBotVolume", "speakerType", "volume", "updateSpatialPosition", "info", "Lio/agora/scene/voice/spatial/model/SeatPositionInfo;", "updateVolume", "Companion", "voice_spatial_debug"})
public final class Room3DMicLayout extends androidx.constraintlayout.widget.ConstraintLayout implements android.view.View.OnClickListener, io.agora.scene.voice.spatial.ui.widget.mic.IRoomMicView {
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.spatial.ui.widget.mic.Room3DMicLayout.Companion Companion = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TAG = "Room3DMicLayout";
    public static final int OFFSET_ANGLE = 10;
    public static final int TIME_INTERVAL = 100;
    public static final int USER_SIZE = 5;
    private io.agora.scene.voice.spatial.databinding.VoiceSpatialViewRoom3dMicLayoutBinding binding;
    private final androidx.constraintlayout.widget.ConstraintSet constraintSet = null;
    private int lastX = -1;
    private int lastY = -1;
    private final android.graphics.Point preMovePoint = null;
    private final kotlin.Lazy micViewSize$delegate = null;
    private final kotlin.Lazy rootSize$delegate = null;
    private final kotlin.Lazy maxTranslationScope$delegate = null;
    private double preAngle = 0.0;
    private long preTime = 0L;
    private io.agora.voice.common.ui.adapter.listener.OnItemClickListener<io.agora.scene.voice.spatial.model.VoiceMicInfoModel> onItemClickListener;
    private io.agora.voice.common.ui.adapter.listener.OnItemClickListener<io.agora.scene.voice.spatial.model.VoiceMicInfoModel> onBotClickListener;
    private io.agora.scene.voice.spatial.model.OnItemMoveListener<io.agora.scene.voice.spatial.model.VoiceMicInfoModel> onSpatialMoveListener;
    
    /**
     * 麦位数据信息
     */
    private final java.util.Map<java.lang.Integer, io.agora.scene.voice.spatial.model.VoiceMicInfoModel> micInfoMap = null;
    
    /**
     * 麦位view信息
     */
    private final java.util.Map<java.lang.Integer, io.agora.scene.voice.spatial.ui.widget.mic.IRoomMicBinding> micViewMap = null;
    private int myRtcUid = -1;
    
    private final android.util.Size getMicViewSize() {
        return null;
    }
    
    private final android.util.Size getRootSize() {
        return null;
    }
    
    private final android.util.Size getMaxTranslationScope() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final io.agora.scene.voice.spatial.ui.widget.mic.Room3DMicLayout onItemClickListener(@org.jetbrains.annotations.NotNull()
    io.agora.voice.common.ui.adapter.listener.OnItemClickListener<io.agora.scene.voice.spatial.model.VoiceMicInfoModel> onItemClickListener, @org.jetbrains.annotations.NotNull()
    io.agora.voice.common.ui.adapter.listener.OnItemClickListener<io.agora.scene.voice.spatial.model.VoiceMicInfoModel> onBotClickListener, @org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.OnItemMoveListener<io.agora.scene.voice.spatial.model.VoiceMicInfoModel> onSpatialMoveListener) {
        return null;
    }
    
    public Room3DMicLayout(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super(null);
    }
    
    public Room3DMicLayout(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.util.AttributeSet attrs) {
        super(null);
    }
    
    public Room3DMicLayout(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.util.AttributeSet attrs, int defStyleAttr) {
        super(null);
    }
    
    public Room3DMicLayout(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.util.AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(null);
    }
    
    private final void init(android.content.Context context) {
    }
    
    public final void setUpInitMicInfoMap() {
    }
    
    private final void initListeners() {
    }
    
    private final android.graphics.RectF getRect(android.view.View view) {
        return null;
    }
    
    /**
     * 获取视图在笛卡尔坐标系中的位置
     */
    private final android.graphics.PointF getPosition(android.view.View view) {
        return null;
    }
    
    /**
     * 将笛卡尔坐标转换成视图中的坐标
     */
    private final android.graphics.PointF convertPoint(android.graphics.PointF point) {
        return null;
    }
    
    @java.lang.Override()
    public void onClick(@org.jetbrains.annotations.Nullable()
    android.view.View v) {
    }
    
    @java.lang.Override()
    public boolean onInterceptTouchEvent(@org.jetbrains.annotations.NotNull()
    android.view.MotionEvent event) {
        return false;
    }
    
    private final boolean canMove() {
        return false;
    }
    
    @java.lang.Override()
    public boolean onTouchEvent(@org.jetbrains.annotations.NotNull()
    android.view.MotionEvent event) {
        return false;
    }
    
    private final int correctMotionEventX(int x) {
        return 0;
    }
    
    private final int correctMotionEventY(int y) {
        return 0;
    }
    
    private final double getAngle(android.graphics.Point curP, android.graphics.Point preP) {
        return 0.0;
    }
    
    private final boolean ignoreSmallOffsets(float dx, float dy) {
        return false;
    }
    
    /**
     * 是否有是3d 座位移动
     */
    private final boolean check3DMicChildView(int x, int y) {
        return false;
    }
    
    private final void setChildView(android.view.View childView, boolean isClickable) {
    }
    
    @java.lang.Override()
    public void onInitMic(@org.jetbrains.annotations.NotNull()
    java.util.List<io.agora.scene.voice.spatial.model.VoiceMicInfoModel> micInfoList, boolean isBotActive, @org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function0<kotlin.Unit> complete) {
    }
    
    @java.lang.Override()
    public void activeBot(boolean active, @org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function2<? super java.lang.Integer, ? super kotlin.Pair<? extends android.graphics.PointF, ? extends android.graphics.PointF>, kotlin.Unit> each) {
    }
    
    @java.lang.Override()
    public void updateVolume(int index, int volume) {
    }
    
    /**
     * 更新机器人提示音量
     */
    @java.lang.Override()
    public void updateBotVolume(int speakerType, int volume) {
    }
    
    @java.lang.Override()
    public int findMicByUid(@org.jetbrains.annotations.NotNull()
    java.lang.String uid) {
        return 0;
    }
    
    @java.lang.Override()
    public void onSeatUpdated(@org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.Integer, io.agora.scene.voice.spatial.model.VoiceMicInfoModel> newMicMap, @org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function0<kotlin.Unit> complete) {
    }
    
    public final void setMyRtcUid(int rtcUid) {
    }
    
    @java.lang.Override()
    public int myRtcUid() {
        return 0;
    }
    
    @java.lang.Override()
    public void updateSpatialPosition(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.spatial.model.SeatPositionInfo info) {
    }
    
    private final android.graphics.PointF getForward(int index) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lio/agora/scene/voice/spatial/ui/widget/mic/Room3DMicLayout$Companion;", "", "()V", "OFFSET_ANGLE", "", "TAG", "", "TIME_INTERVAL", "USER_SIZE", "voice_spatial_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}
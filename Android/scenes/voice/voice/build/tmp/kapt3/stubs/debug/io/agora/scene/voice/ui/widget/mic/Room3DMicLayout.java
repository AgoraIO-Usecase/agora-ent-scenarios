package io.agora.scene.voice.ui.widget.mic;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u00b8\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010%\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\t\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0007\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\b\u000e\u0018\u0000 e2\u00020\u00012\u00020\u00022\u00020\u0003:\u0001eB\u000f\b\u0016\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006B\u0019\b\u0016\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\u0010\u0007\u001a\u0004\u0018\u00010\b\u00a2\u0006\u0002\u0010\tB!\b\u0016\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\u0010\u0007\u001a\u0004\u0018\u00010\b\u0012\u0006\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\fB)\b\u0016\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\u0010\u0007\u001a\u0004\u0018\u00010\b\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\r\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\u000eJ\u0010\u00104\u001a\u0002052\u0006\u00106\u001a\u00020\u0014H\u0016J\b\u00107\u001a\u00020\u0014H\u0002J\u0018\u00108\u001a\u00020\u00142\u0006\u00109\u001a\u00020\u000b2\u0006\u0010:\u001a\u00020\u000bH\u0002J\u0010\u0010;\u001a\u00020\u000b2\u0006\u00109\u001a\u00020\u000bH\u0002J\u0010\u0010<\u001a\u00020\u000b2\u0006\u0010:\u001a\u00020\u000bH\u0002J\u0010\u0010=\u001a\u00020\u000b2\u0006\u0010>\u001a\u00020?H\u0016J\u0018\u0010@\u001a\u00020,2\u0006\u0010A\u001a\u00020.2\u0006\u0010B\u001a\u00020.H\u0002J\u0010\u0010C\u001a\u00020D2\u0006\u0010E\u001a\u00020FH\u0002J\u0018\u0010G\u001a\u00020\u00142\u0006\u0010H\u001a\u00020I2\u0006\u0010J\u001a\u00020IH\u0002J\u0010\u0010K\u001a\u0002052\u0006\u0010\u0004\u001a\u00020\u0005H\u0002J\b\u0010L\u001a\u000205H\u0002J\b\u0010\'\u001a\u00020\u000bH\u0016J\u0012\u0010M\u001a\u0002052\b\u0010N\u001a\u0004\u0018\u00010FH\u0016J\u001e\u0010O\u001a\u0002052\f\u0010P\u001a\b\u0012\u0004\u0012\u00020!0Q2\u0006\u0010R\u001a\u00020\u0014H\u0016J\u0010\u0010S\u001a\u00020\u00142\u0006\u0010T\u001a\u00020UH\u0016J\"\u0010*\u001a\u00020\u00002\f\u0010*\u001a\b\u0012\u0004\u0012\u00020!0)2\f\u0010(\u001a\b\u0012\u0004\u0012\u00020!0)J\u001c\u0010V\u001a\u0002052\u0012\u0010W\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020!0XH\u0016J\u0010\u0010Y\u001a\u00020\u00142\u0006\u0010T\u001a\u00020UH\u0016J\u0018\u0010Z\u001a\u0002052\u0006\u0010[\u001a\u00020F2\u0006\u0010\\\u001a\u00020\u0014H\u0002J\u000e\u0010]\u001a\u0002052\u0006\u0010^\u001a\u00020\u000bJ\u0006\u0010_\u001a\u000205J\u0018\u0010`\u001a\u0002052\u0006\u0010a\u001a\u00020\u000b2\u0006\u0010b\u001a\u00020\u000bH\u0016J\u0018\u0010c\u001a\u0002052\u0006\u0010d\u001a\u00020\u000b2\u0006\u0010b\u001a\u00020\u000bH\u0016R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0017\u001a\u00020\u00188BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u001b\u0010\u001c\u001a\u0004\b\u0019\u0010\u001aR\u0010\u0010\u001d\u001a\u0004\u0018\u00010\u001eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u001f\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020!0 X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\"\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020#0 X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010$\u001a\u00020\u00188BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b&\u0010\u001c\u001a\u0004\b%\u0010\u001aR\u000e\u0010\'\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0016\u0010(\u001a\n\u0012\u0004\u0012\u00020!\u0018\u00010)X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0016\u0010*\u001a\n\u0012\u0004\u0012\u00020!\u0018\u00010)X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010+\u001a\u00020,X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010-\u001a\u00020.X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010/\u001a\u000200X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001b\u00101\u001a\u00020\u00188BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b3\u0010\u001c\u001a\u0004\b2\u0010\u001a\u00a8\u0006f"}, d2 = {"Lio/agora/scene/voice/ui/widget/mic/Room3DMicLayout;", "Landroidx/constraintlayout/widget/ConstraintLayout;", "Landroid/view/View$OnClickListener;", "Lio/agora/scene/voice/ui/widget/mic/IRoomMicView;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "attrs", "Landroid/util/AttributeSet;", "(Landroid/content/Context;Landroid/util/AttributeSet;)V", "defStyleAttr", "", "(Landroid/content/Context;Landroid/util/AttributeSet;I)V", "defStyleRes", "(Landroid/content/Context;Landroid/util/AttributeSet;II)V", "binding", "Lio/agora/scene/voice/databinding/VoiceViewRoom3dMicLayoutBinding;", "constraintSet", "Landroidx/constraintlayout/widget/ConstraintSet;", "isMove", "", "lastX", "lastY", "maxTranslationScope", "Landroid/util/Size;", "getMaxTranslationScope", "()Landroid/util/Size;", "maxTranslationScope$delegate", "Lkotlin/Lazy;", "micClickAnimator", "Landroid/animation/ValueAnimator;", "micInfoMap", "", "Lio/agora/scene/voice/model/VoiceMicInfoModel;", "micViewMap", "Lio/agora/scene/voice/ui/widget/mic/IRoomMicBinding;", "micViewSize", "getMicViewSize", "micViewSize$delegate", "myRtcUid", "onBotClickListener", "Lio/agora/voice/common/ui/adapter/listener/OnItemClickListener;", "onItemClickListener", "preAngle", "", "preMovePoint", "Landroid/graphics/Point;", "preTime", "", "rootSize", "getRootSize", "rootSize$delegate", "activeBot", "", "active", "canMove", "check3DMicChildView", "x", "y", "correctMotionEventX", "correctMotionEventY", "findMicByUid", "uid", "", "getAngle", "curP", "preP", "getRect", "Landroid/graphics/RectF;", "view", "Landroid/view/View;", "ignoreSmallOffsets", "dx", "", "dy", "init", "initListeners", "onClick", "v", "onInitMic", "micInfoList", "", "isBotActive", "onInterceptTouchEvent", "event", "Landroid/view/MotionEvent;", "onSeatUpdated", "newMicMap", "", "onTouchEvent", "setChildView", "childView", "isClickable", "setMyRtcUid", "rtcUid", "setUpInitMicInfoMap", "updateBotVolume", "speakerType", "volume", "updateVolume", "index", "Companion", "voice_debug"})
public final class Room3DMicLayout extends androidx.constraintlayout.widget.ConstraintLayout implements android.view.View.OnClickListener, io.agora.scene.voice.ui.widget.mic.IRoomMicView {
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.ui.widget.mic.Room3DMicLayout.Companion Companion = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TAG = "Room3DMicLayout";
    public static final int OFFSET_ANGLE = 10;
    public static final int TIME_INTERVAL = 100;
    public static final int USER_SIZE = 5;
    private io.agora.scene.voice.databinding.VoiceViewRoom3dMicLayoutBinding binding;
    private final androidx.constraintlayout.widget.ConstraintSet constraintSet = null;
    private int lastX = 0;
    private int lastY = 0;
    private final android.graphics.Point preMovePoint = null;
    private boolean isMove = false;
    private final kotlin.Lazy micViewSize$delegate = null;
    private final kotlin.Lazy rootSize$delegate = null;
    private final kotlin.Lazy maxTranslationScope$delegate = null;
    private double preAngle = 0.0;
    private long preTime = 0L;
    private android.animation.ValueAnimator micClickAnimator;
    private io.agora.voice.common.ui.adapter.listener.OnItemClickListener<io.agora.scene.voice.model.VoiceMicInfoModel> onItemClickListener;
    private io.agora.voice.common.ui.adapter.listener.OnItemClickListener<io.agora.scene.voice.model.VoiceMicInfoModel> onBotClickListener;
    
    /**
     * 麦位数据信息
     */
    private final java.util.Map<java.lang.Integer, io.agora.scene.voice.model.VoiceMicInfoModel> micInfoMap = null;
    
    /**
     * 麦位view信息
     */
    private final java.util.Map<java.lang.Integer, io.agora.scene.voice.ui.widget.mic.IRoomMicBinding> micViewMap = null;
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
    public final io.agora.scene.voice.ui.widget.mic.Room3DMicLayout onItemClickListener(@org.jetbrains.annotations.NotNull()
    io.agora.voice.common.ui.adapter.listener.OnItemClickListener<io.agora.scene.voice.model.VoiceMicInfoModel> onItemClickListener, @org.jetbrains.annotations.NotNull()
    io.agora.voice.common.ui.adapter.listener.OnItemClickListener<io.agora.scene.voice.model.VoiceMicInfoModel> onBotClickListener) {
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
    java.util.List<io.agora.scene.voice.model.VoiceMicInfoModel> micInfoList, boolean isBotActive) {
    }
    
    @java.lang.Override()
    public void activeBot(boolean active) {
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
    java.util.Map<java.lang.Integer, io.agora.scene.voice.model.VoiceMicInfoModel> newMicMap) {
    }
    
    public final void setMyRtcUid(int rtcUid) {
    }
    
    @java.lang.Override()
    public int myRtcUid() {
        return 0;
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lio/agora/scene/voice/ui/widget/mic/Room3DMicLayout$Companion;", "", "()V", "OFFSET_ANGLE", "", "TAG", "", "TIME_INTERVAL", "USER_SIZE", "voice_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}
package io.agora.scene.voice.imkit.manager;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000l\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\f\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0018\n\u0002\u0010$\n\u0002\b\n\u0018\u0000 X2\u00020\u0001:\u0001XB\u0005\u00a2\u0006\u0002\u0010\u0002J\u001d\u0010\u0018\u001a\u0004\u0018\u0001H\u0019\"\u0004\b\u0000\u0010\u00192\u0006\u0010\u001a\u001a\u00020\u0005H\u0002\u00a2\u0006\u0002\u0010\u001bJ\u000e\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u0005J\u0006\u0010\u001f\u001a\u00020 J\b\u0010!\u001a\u00020 H\u0002J\u0006\u0010\"\u001a\u00020 J\b\u0010#\u001a\u00020 H\u0002J\b\u0010$\u001a\u00020 H\u0002J\u0018\u0010%\u001a\u00020\u001d2\b\u0010&\u001a\u0004\u0018\u00010\u00052\u0006\u0010\'\u001a\u00020\u001dJ\u0006\u0010(\u001a\u00020\u0007J\u0018\u0010)\u001a\u00020\u00072\b\u0010&\u001a\u0004\u0018\u00010\u00052\u0006\u0010\'\u001a\u00020\u0007J\f\u0010*\u001a\b\u0012\u0004\u0012\u00020\n0\tJ\u0012\u0010+\u001a\u0004\u0018\u00010\u00052\b\u0010&\u001a\u0004\u0018\u00010\u0005J&\u0010,\u001a\n\u0012\u0004\u0012\u0002H.\u0018\u00010-\"\n\b\u0000\u0010.*\u0004\u0018\u00010/2\b\u0010&\u001a\u0004\u0018\u00010\u0005H\u0007J2\u00100\u001a\u0010\u0012\u0004\u0012\u0002H1\u0012\u0004\u0012\u0002H2\u0018\u00010\u0004\"\n\b\u0000\u00101*\u0004\u0018\u00010/\"\u0004\b\u0001\u001022\b\u0010&\u001a\u0004\u0018\u00010\u0005H\u0007J\u0010\u00103\u001a\u0004\u0018\u00010\n2\u0006\u0010\u001e\u001a\u00020\u0005J\f\u00104\u001a\b\u0012\u0004\u0012\u00020\n0\tJ\u0010\u00105\u001a\u0004\u0018\u0001062\u0006\u0010\u001e\u001a\u00020\u0005J\u0010\u00107\u001a\u0004\u0018\u0001062\u0006\u00108\u001a\u00020\u0007J\u0014\u00109\u001a\u0010\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u0004J\f\u0010:\u001a\b\u0012\u0004\u0012\u00020\u00120\tJ\u0012\u0010;\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00120\u0004J\u0012\u0010<\u001a\u0004\u0018\u00010\u00052\b\u0010&\u001a\u0004\u0018\u00010\u0005J\u001c\u0010<\u001a\u0004\u0018\u00010\u00052\b\u0010&\u001a\u0004\u0018\u00010\u00052\b\u0010\'\u001a\u0004\u0018\u00010\u0005J\u0010\u0010=\u001a\u0004\u0018\u00010\n2\u0006\u0010\u001e\u001a\u00020\u0005J\f\u0010>\u001a\b\u0012\u0004\u0012\u00020\n0\tJ\u0014\u0010?\u001a\u0004\u0018\u00010\u00052\b\u0010@\u001a\u0004\u0018\u00010\u0001H\u0002J\u001a\u0010A\u001a\u00020 2\b\u0010&\u001a\u0004\u0018\u00010\u00052\u0006\u0010B\u001a\u00020\u001dH\u0007J\u001a\u0010C\u001a\u00020 2\b\u0010&\u001a\u0004\u0018\u00010\u00052\u0006\u0010B\u001a\u00020\u0007H\u0007J\"\u0010D\u001a\u00020 2\b\u0010&\u001a\u0004\u0018\u00010\u00052\u0010\u0010E\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010/\u0018\u00010-J:\u0010F\u001a\u00020 \"\n\b\u0000\u00101*\u0004\u0018\u00010/\"\u0004\b\u0001\u001022\b\u0010&\u001a\u0004\u0018\u00010\u00052\u0014\u0010G\u001a\u0010\u0012\u0004\u0012\u0002H1\u0012\u0004\u0012\u0002H2\u0018\u00010\u0004H\u0002J\u001c\u0010H\u001a\u00020 2\b\u0010&\u001a\u0004\u0018\u00010\u00052\b\u0010B\u001a\u0004\u0018\u00010\u0005H\u0007J\u000e\u0010I\u001a\u00020 2\u0006\u0010\u001e\u001a\u00020\u0005J\u000e\u0010J\u001a\u00020 2\u0006\u0010\u001e\u001a\u00020\u0005J\u000e\u0010K\u001a\u00020 2\u0006\u0010L\u001a\u00020\u0007J\u001a\u0010M\u001a\u00020 2\u0012\u0010N\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050OJ\u000e\u0010P\u001a\u00020 2\u0006\u0010Q\u001a\u00020\nJ\u001a\u0010R\u001a\u00020 2\u0012\u0010N\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050OJ\u000e\u0010S\u001a\u00020 2\u0006\u0010T\u001a\u00020\u0012J\u000e\u0010U\u001a\u00020 2\u0006\u0010V\u001a\u00020\nJ\u000e\u0010W\u001a\u00020 2\u0006\u0010L\u001a\u00020\u0007R\u001a\u0010\u0003\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000b\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\n0\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00120\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0013\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00120\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\n0\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0017\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\n0\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006Y"}, d2 = {"Lio/agora/scene/voice/imkit/manager/ChatroomCacheManager;", "", "()V", "allInfoMap", "", "", "giftAmount", "", "invitationList", "", "Lio/agora/scene/voice/model/VoiceMemberModel;", "invitationMap", "mEditor", "Landroid/content/SharedPreferences$Editor;", "mMicInfoMap", "mSharedPreferences", "Landroid/content/SharedPreferences;", "rankingList", "Lio/agora/scene/voice/model/VoiceRankUserModel;", "rankingMap", "roomMemberList", "roomMemberMap", "submitMicList", "submitMicMap", "base64ToObj", "T", "base64", "(Ljava/lang/String;)Ljava/lang/Object;", "checkInvitationByChatUid", "", "chatUid", "clearAllCache", "", "clearMemberList", "clearMicInfo", "clearRankList", "clearSubmitList", "getBoolean", "key", "defValue", "getGiftAmountCache", "getInt", "getInvitationList", "getKvInfo", "getList", "", "E", "Ljava/io/Serializable;", "getMap", "K", "V", "getMember", "getMemberList", "getMicInfoByChatUid", "Lio/agora/scene/voice/model/VoiceMicInfoModel;", "getMicInfoByIndex", "micIndex", "getMicInfoMap", "getRankList", "getRankMap", "getString", "getSubmitMic", "getSubmitMicList", "obj2Base64", "obj", "putBoolean", "value", "putInt", "putList", "list", "putMap", "map", "putString", "removeMember", "removeSubmitMember", "setGiftAmountCache", "amount", "setKvInfo", "kvMap", "", "setMemberList", "member", "setMicInfo", "setRankList", "rankBean", "setSubmitMicList", "voiceMemberBean", "updateGiftAmountCache", "Companion", "voice_debug"})
public final class ChatroomCacheManager {
    private android.content.SharedPreferences.Editor mEditor;
    private android.content.SharedPreferences mSharedPreferences;
    private final java.util.Map<java.lang.String, java.lang.String> mMicInfoMap = null;
    private final java.util.Map<java.lang.String, java.lang.String> allInfoMap = null;
    private final java.util.List<io.agora.scene.voice.model.VoiceMemberModel> submitMicList = null;
    private final java.util.Map<java.lang.String, io.agora.scene.voice.model.VoiceMemberModel> submitMicMap = null;
    private final java.util.List<io.agora.scene.voice.model.VoiceMemberModel> roomMemberList = null;
    private final java.util.Map<java.lang.String, io.agora.scene.voice.model.VoiceMemberModel> roomMemberMap = null;
    private final java.util.List<io.agora.scene.voice.model.VoiceMemberModel> invitationList = null;
    private final java.util.Map<java.lang.String, io.agora.scene.voice.model.VoiceMemberModel> invitationMap = null;
    private java.util.List<io.agora.scene.voice.model.VoiceRankUserModel> rankingList;
    private final java.util.Map<java.lang.String, io.agora.scene.voice.model.VoiceRankUserModel> rankingMap = null;
    private int giftAmount = 0;
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.imkit.manager.ChatroomCacheManager.Companion Companion = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TAG = "ChatroomCacheManager";
    @org.jetbrains.annotations.NotNull()
    private static final io.agora.scene.voice.imkit.manager.ChatroomCacheManager cacheManager = null;
    
    public ChatroomCacheManager() {
        super();
    }
    
    /**
     * 从服务端获取数据 直接赋值giftAmount
     */
    public final void setGiftAmountCache(int amount) {
    }
    
    /**
     * 更新房间礼物总金额
     */
    public final void updateGiftAmountCache(int amount) {
    }
    
    /**
     * 获取房间礼物总金额
     */
    public final int getGiftAmountCache() {
        return 0;
    }
    
    /**
     * 缓存所有kv属性
     */
    public final void setKvInfo(@org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.String> kvMap) {
    }
    
    /**
     * 根据key从缓存获取属性
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getKvInfo(@org.jetbrains.annotations.Nullable()
    java.lang.String key) {
        return null;
    }
    
    /**
     * 清除所有缓存
     */
    public final void clearAllCache() {
    }
    
    /**
     * 设置Mic信息
     */
    public final void setMicInfo(@org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.String> kvMap) {
    }
    
    /**
     * 清除本地MicInfo信息
     */
    public final void clearMicInfo() {
    }
    
    /**
     * 获取Mic信息
     */
    @org.jetbrains.annotations.Nullable()
    public final java.util.Map<java.lang.String, java.lang.String> getMicInfoMap() {
        return null;
    }
    
    /**
     * 获取指定麦位的Mic信息
     */
    @org.jetbrains.annotations.Nullable()
    public final io.agora.scene.voice.model.VoiceMicInfoModel getMicInfoByIndex(int micIndex) {
        return null;
    }
    
    /**
     * 根据chatUid获取VoiceMicInfoModel
     */
    @org.jetbrains.annotations.Nullable()
    public final io.agora.scene.voice.model.VoiceMicInfoModel getMicInfoByChatUid(@org.jetbrains.annotations.NotNull()
    java.lang.String chatUid) {
        return null;
    }
    
    /**
     * 设置申请上麦列表
     */
    public final void setSubmitMicList(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.model.VoiceMemberModel voiceMemberBean) {
    }
    
    /**
     * 获取申请上麦成员列表
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<io.agora.scene.voice.model.VoiceMemberModel> getSubmitMicList() {
        return null;
    }
    
    /**
     * 获取申请上麦列表中指定成员model
     */
    @org.jetbrains.annotations.Nullable()
    public final io.agora.scene.voice.model.VoiceMemberModel getSubmitMic(@org.jetbrains.annotations.NotNull()
    java.lang.String chatUid) {
        return null;
    }
    
    /**
     * 从申请列表移除指定成员对象
     */
    public final void removeSubmitMember(@org.jetbrains.annotations.NotNull()
    java.lang.String chatUid) {
    }
    
    /**
     * 清除本地申请列表
     */
    private final void clearSubmitList() {
    }
    
    /**
     * 设置成员列表
     */
    public final void setMemberList(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.model.VoiceMemberModel member) {
    }
    
    /**
     * 根据chatUid 获取对应实体类
     */
    @org.jetbrains.annotations.Nullable()
    public final io.agora.scene.voice.model.VoiceMemberModel getMember(@org.jetbrains.annotations.NotNull()
    java.lang.String chatUid) {
        return null;
    }
    
    /**
     * 获取成员列表
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<io.agora.scene.voice.model.VoiceMemberModel> getMemberList() {
        return null;
    }
    
    /**
     * 获取邀请列表（过滤已在麦位的成员）
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<io.agora.scene.voice.model.VoiceMemberModel> getInvitationList() {
        return null;
    }
    
    /**
     * 检查邀请列表成员是否已经在麦位上
     */
    public final boolean checkInvitationByChatUid(@org.jetbrains.annotations.NotNull()
    java.lang.String chatUid) {
        return false;
    }
    
    /**
     * 从成员列表中移除指定成员( 成员退出回调中调用 )
     */
    public final void removeMember(@org.jetbrains.annotations.NotNull()
    java.lang.String chatUid) {
    }
    
    /**
     * 清除成员列表
     */
    private final void clearMemberList() {
    }
    
    /**
     * 设置榜单列表
     */
    public final void setRankList(@org.jetbrains.annotations.NotNull()
    io.agora.scene.voice.model.VoiceRankUserModel rankBean) {
    }
    
    /**
     * 获取榜单列表
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<io.agora.scene.voice.model.VoiceRankUserModel> getRankList() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.String, io.agora.scene.voice.model.VoiceRankUserModel> getRankMap() {
        return null;
    }
    
    /**
     * 清除榜单
     */
    private final void clearRankList() {
    }
    
    /**
     * 存入字符串
     * @param key     字符串的键
     * @param value   字符串的值
     */
    @android.annotation.SuppressLint(value = {"ApplySharedPref"})
    public final void putString(@org.jetbrains.annotations.Nullable()
    java.lang.String key, @org.jetbrains.annotations.Nullable()
    java.lang.String value) {
    }
    
    /**
     * 获取字符串
     * @param key     字符串的键
     * @return 得到的字符串
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getString(@org.jetbrains.annotations.Nullable()
    java.lang.String key) {
        return null;
    }
    
    /**
     * 获取字符串
     * @param key     字符串的键
     * @param defValue   字符串的默认值
     * @return 得到的字符串
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getString(@org.jetbrains.annotations.Nullable()
    java.lang.String key, @org.jetbrains.annotations.Nullable()
    java.lang.String defValue) {
        return null;
    }
    
    /**
     * 保存布尔值
     * @param key     键
     * @param value   值
     */
    @android.annotation.SuppressLint(value = {"ApplySharedPref"})
    public final void putBoolean(@org.jetbrains.annotations.Nullable()
    java.lang.String key, boolean value) {
    }
    
    /**
     * 获取布尔值
     * @param key      键
     * @param defValue 默认值
     * @return 返回保存的值
     */
    public final boolean getBoolean(@org.jetbrains.annotations.Nullable()
    java.lang.String key, boolean defValue) {
        return false;
    }
    
    /**
     * 保存int值
     * @param key     键
     * @param value   值
     */
    @android.annotation.SuppressLint(value = {"ApplySharedPref"})
    public final void putInt(@org.jetbrains.annotations.Nullable()
    java.lang.String key, int value) {
    }
    
    /**
     * 存储List集合
     * @param key 存储的键
     * @param list 存储的集合
     */
    public final void putList(@org.jetbrains.annotations.Nullable()
    java.lang.String key, @org.jetbrains.annotations.Nullable()
    java.util.List<? extends java.io.Serializable> list) {
    }
    
    /**
     * 获取List集合
     * @param key 键
     * @param <E> 指定泛型
     * @return List集合
     *   </E>
     */
    @org.jetbrains.annotations.Nullable()
    @androidx.annotation.Nullable()
    public final <E extends java.io.Serializable>java.util.List<E> getList(@org.jetbrains.annotations.Nullable()
    java.lang.String key) {
        return null;
    }
    
    /**
     * 获取int值
     * @param key      键
     * @param defValue 默认值
     * @return 保存的值
     */
    public final int getInt(@org.jetbrains.annotations.Nullable()
    java.lang.String key, int defValue) {
        return 0;
    }
    
    /**
     * 存储Map集合
     * @param key 键
     * @param map 存储的集合
     * @param <K> 指定Map的键
     * @param <V> 指定Map的值
     *   </V></K>
     */
    private final <K extends java.io.Serializable, V extends java.lang.Object>void putMap(java.lang.String key, java.util.Map<K, V> map) {
    }
    
    /**
     * 获取map集合
     * @param key 键
     * @param <K> 指定Map的键
     * @param <V> 指定Map的值
     * @return 存储的集合
     *   </V></K>
     */
    @org.jetbrains.annotations.Nullable()
    @androidx.annotation.Nullable()
    public final <K extends java.io.Serializable, V extends java.lang.Object>java.util.Map<K, V> getMap(@org.jetbrains.annotations.Nullable()
    java.lang.String key) {
        return null;
    }
    
    /**
     * 对象转字符串
     * @param obj 任意对象
     * @return base64字符串
     */
    private final java.lang.String obj2Base64(java.lang.Object obj) {
        return null;
    }
    
    /**
     * base64转对象
     * @param base64 字符串
     * @param <T> 指定转成的类型
     * @return 指定类型对象 失败返回null
     *   </T>
     */
    private final <T extends java.lang.Object>T base64ToObj(java.lang.String base64) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\b\u00a8\u0006\t"}, d2 = {"Lio/agora/scene/voice/imkit/manager/ChatroomCacheManager$Companion;", "", "()V", "TAG", "", "cacheManager", "Lio/agora/scene/voice/imkit/manager/ChatroomCacheManager;", "getCacheManager", "()Lio/agora/scene/voice/imkit/manager/ChatroomCacheManager;", "voice_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final io.agora.scene.voice.imkit.manager.ChatroomCacheManager getCacheManager() {
            return null;
        }
    }
}
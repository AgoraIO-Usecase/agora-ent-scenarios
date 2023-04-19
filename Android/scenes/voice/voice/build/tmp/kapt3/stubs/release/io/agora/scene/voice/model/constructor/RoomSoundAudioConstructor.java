package io.agora.scene.voice.model.constructor;

import java.lang.System;

/**
 * @author create by zhangwei03
 */
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010$\n\u0002\u0018\u0002\n\u0002\b\"\n\u0002\u0010 \n\u0002\b\u0014\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J \u00107\u001a\u00020\u00042\u0006\u00108\u001a\u00020\u00042\u0006\u00109\u001a\u00020\u00042\u0006\u0010:\u001a\u00020\u0006H\u0002J\u0010\u0010;\u001a\u00020\u00042\u0006\u00109\u001a\u00020\u0004H\u0002J\u0010\u0010<\u001a\u00020\u00042\u0006\u00109\u001a\u00020\u0004H\u0002J\u0010\u0010=\u001a\u00020\u00042\u0006\u00109\u001a\u00020\u0004H\u0002J\u0010\u0010>\u001a\u00020\u00042\u0006\u00109\u001a\u00020\u0004H\u0002J\u0010\u0010?\u001a\u00020\u00042\u0006\u00109\u001a\u00020\u0004H\u0002J\u0010\u0010@\u001a\u00020\u00042\u0006\u00109\u001a\u00020\u0004H\u0002J\u0010\u0010A\u001a\u00020\u00042\u0006\u00109\u001a\u00020\u0004H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\'\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u000b0\n8FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000e\u0010\u000f\u001a\u0004\b\f\u0010\rR\u000e\u0010\u0010\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001c\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001d\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001e\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001f\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010 \u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010!\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\"\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010#\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010$\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010%\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010&\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\'\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010(\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010)\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010*\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010+\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010,\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R-\u0010-\u001a\u0014\u0012\u0004\u0012\u00020\u0006\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0.0\n8FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b0\u0010\u000f\u001a\u0004\b/\u0010\rR-\u00101\u001a\u0014\u0012\u0004\u0012\u00020\u0006\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0.0\n8FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b3\u0010\u000f\u001a\u0004\b2\u0010\rR-\u00104\u001a\u0014\u0012\u0004\u0012\u00020\u0006\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0.0\n8FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b6\u0010\u000f\u001a\u0004\b5\u0010\r\u00a8\u0006B"}, d2 = {"Lio/agora/scene/voice/model/constructor/RoomSoundAudioConstructor;", "", "()V", "AINSIntroduce", "", "AINSIntroduceSoundId", "", "AINSSound", "AINSSoundId", "AINSSoundMap", "", "Lio/agora/scene/voice/model/SoundAudioBean;", "getAINSSoundMap", "()Ljava/util/Map;", "AINSSoundMap$delegate", "Lkotlin/Lazy;", "AINS_AlertSound", "AINS_ApplauseSound", "AINS_AudioFeedback", "AINS_ConstructionSound", "AINS_HomeSound", "AINS_KitchenSound", "AINS_MachineSound", "AINS_MicPopFilterSound", "AINS_MicrophoneFingerRub", "AINS_MicrophoneScreenTap", "AINS_OfficeSound", "AINS_StreetSound", "AINS_TVSound", "AINS_WindSound", "BASE_URL", "CN", "CreateCommonRoom", "CreateCommonRoomSoundId", "CreateSpatialRoom", "CreateSpatialRoomSoundId", "EN", "GamingBuddy", "GamingBuddySoundId", "Karaoke", "KaraokeSoundId", "ProfessionalBroadcaster", "ProfessionalBroadcasterSoundId", "SocialChat", "SocialChatSoundId", "anisIntroduceAudioMap", "", "getAnisIntroduceAudioMap", "anisIntroduceAudioMap$delegate", "createRoomSoundAudioMap", "getCreateRoomSoundAudioMap", "createRoomSoundAudioMap$delegate", "soundSelectionAudioMap", "getSoundSelectionAudioMap", "soundSelectionAudioMap$delegate", "getAINSSoundUrl", "audioPathPrefix", "audioPath", "ainsMode", "getANISIntroduceUrl", "getCreateCommonRoomUrl", "getCreateSpatialRoomUrl", "getGamingBuddyUrl", "getKaraokeUrl", "getProfessionalBroadcasterUrl", "getSocialChatUrl", "voice_release"})
public final class RoomSoundAudioConstructor {
    @org.jetbrains.annotations.NotNull()
    public static final io.agora.scene.voice.model.constructor.RoomSoundAudioConstructor INSTANCE = null;
    private static final java.lang.String BASE_URL = "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/fulldemochat/aisound";
    private static final java.lang.String CN = "CN";
    private static final java.lang.String EN = "EN";
    private static final java.lang.String CreateCommonRoom = "/01CreateRoomCommonChatroom%1$s";
    private static final java.lang.String CreateSpatialRoom = "/02CeateRoomSpaticalChatroom%1$s";
    private static final java.lang.String SocialChat = "/03SoundSelectionSocialChat%1$s";
    private static final java.lang.String Karaoke = "/04SoundSelectionKaraoke%1$s";
    private static final java.lang.String GamingBuddy = "/05SoundSelectionGamingBuddy%1$s";
    private static final java.lang.String ProfessionalBroadcaster = "/06SoundProfessionalBroadcaster%1$s";
    private static final java.lang.String AINSIntroduce = "/07AINSIntroduce%1$s";
    private static final java.lang.String AINSSound = "%1$s%2$s-%3$s-%4$s";
    private static final java.lang.String AINS_TVSound = "/08AINSTVSound";
    private static final java.lang.String AINS_KitchenSound = "/09AINSKitchenSound";
    private static final java.lang.String AINS_StreetSound = "/10AINStreetSound";
    private static final java.lang.String AINS_MachineSound = "/11AINSRobotSound";
    private static final java.lang.String AINS_OfficeSound = "/12AINSOfficeSound";
    private static final java.lang.String AINS_HomeSound = "/13AINSHomeSound";
    private static final java.lang.String AINS_ConstructionSound = "/14AINSConstructionSound";
    private static final java.lang.String AINS_AlertSound = "/15AINSAlertSound";
    private static final java.lang.String AINS_ApplauseSound = "/16AINSApplause";
    private static final java.lang.String AINS_WindSound = "/17AINSWindSound";
    private static final java.lang.String AINS_MicPopFilterSound = "/18AINSMicPopFilter";
    private static final java.lang.String AINS_AudioFeedback = "/19AINSAudioFeedback";
    private static final java.lang.String AINS_MicrophoneFingerRub = "/20ANISMicrophoneFingerRubSound";
    private static final java.lang.String AINS_MicrophoneScreenTap = "/21ANISScreenTapSound";
    private static final int CreateCommonRoomSoundId = 100;
    private static final int CreateSpatialRoomSoundId = 200;
    private static final int SocialChatSoundId = 300;
    private static final int KaraokeSoundId = 400;
    private static final int GamingBuddySoundId = 500;
    private static final int ProfessionalBroadcasterSoundId = 600;
    private static final int AINSIntroduceSoundId = 700;
    private static final int AINSSoundId = 800;
    
    /**
     * 新房间创建欢迎语料
     */
    @org.jetbrains.annotations.NotNull()
    private static final kotlin.Lazy createRoomSoundAudioMap$delegate = null;
    
    /**
     * 最佳音效语料
     */
    @org.jetbrains.annotations.NotNull()
    private static final kotlin.Lazy soundSelectionAudioMap$delegate = null;
    
    /**
     * AI 降噪开关讲解语料
     */
    @org.jetbrains.annotations.NotNull()
    private static final kotlin.Lazy anisIntroduceAudioMap$delegate = null;
    
    /**
     * AI 降噪14种语料
     */
    @org.jetbrains.annotations.NotNull()
    private static final kotlin.Lazy AINSSoundMap$delegate = null;
    
    private RoomSoundAudioConstructor() {
        super();
    }
    
    /**
     * 新房间创建欢迎语料
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.Integer, java.util.List<io.agora.scene.voice.model.SoundAudioBean>> getCreateRoomSoundAudioMap() {
        return null;
    }
    
    /**
     * 最佳音效语料
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.Integer, java.util.List<io.agora.scene.voice.model.SoundAudioBean>> getSoundSelectionAudioMap() {
        return null;
    }
    
    /**
     * AI 降噪开关讲解语料
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.Integer, java.util.List<io.agora.scene.voice.model.SoundAudioBean>> getAnisIntroduceAudioMap() {
        return null;
    }
    
    /**
     * AI 降噪14种语料
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.Integer, io.agora.scene.voice.model.SoundAudioBean> getAINSSoundMap() {
        return null;
    }
    
    private final java.lang.String getCreateCommonRoomUrl(java.lang.String audioPath) {
        return null;
    }
    
    private final java.lang.String getCreateSpatialRoomUrl(java.lang.String audioPath) {
        return null;
    }
    
    private final java.lang.String getSocialChatUrl(java.lang.String audioPath) {
        return null;
    }
    
    private final java.lang.String getKaraokeUrl(java.lang.String audioPath) {
        return null;
    }
    
    private final java.lang.String getGamingBuddyUrl(java.lang.String audioPath) {
        return null;
    }
    
    private final java.lang.String getProfessionalBroadcasterUrl(java.lang.String audioPath) {
        return null;
    }
    
    private final java.lang.String getANISIntroduceUrl(java.lang.String audioPath) {
        return null;
    }
    
    private final java.lang.String getAINSSoundUrl(java.lang.String audioPathPrefix, java.lang.String audioPath, int ainsMode) {
        return null;
    }
}
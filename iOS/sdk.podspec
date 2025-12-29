Pod::Spec.new do |spec|
   spec.name          = "sdk"
   spec.version       = "1.0"
   spec.summary       = "Agora iOS video SDK"
   spec.description   = "iOS library for agora A/V communication, broadcasting and data channel service."
   spec.homepage      = "https://docs.agora.io/en/Agora%20Platform/downloads"
   spec.license       = { "type" => "Copyright", "text" => "Copyright 2018 agora.io. All rights reserved.\n"}
   spec.author        = { "Agora Lab" => "developer@agora.io" }
   spec.platform      = :ios
   spec.source        = { :git => "" }
   spec.vendored_frameworks = "libs/*.xcframework"
   spec.requires_arc  = true
   spec.ios.deployment_target  = '9.0'
   
   s.subspec 'RtcBasic' do |ss|
        ss.vendored_frameworks = "AgoraRtcKit.xcframework", "Agorafdkaac.xcframework", "Agoraffmpeg.xcframework", "AgoraSoundTouch.xcframework", "video_dec.xcframework"
        ss.weak_frameworks = "AgoraRtcKit", "Agorafdkaac", "Agoraffmpeg", "AgoraSoundTouch", "video_dec"
        ss.dependency 'AgoraInfra_iOS', '1.3.4'
    end
    s.subspec 'AINS' do |ss|
        ss.vendored_frameworks = "AgoraAiNoiseSuppressionExtension.xcframework"
        ss.weak_frameworks = "AgoraAiNoiseSuppressionExtension"
    end
    s.subspec 'AINSLL' do |ss|
        ss.vendored_frameworks = "AgoraAiNoiseSuppressionLLExtension.xcframework"
        ss.weak_frameworks = "AgoraAiNoiseSuppressionLLExtension"
    end
    s.subspec 'AudioBeauty' do |ss|
        ss.vendored_frameworks = "AgoraAudioBeautyExtension.xcframework"
        ss.weak_frameworks = "AgoraAudioBeautyExtension"
    end
    s.subspec 'ClearVision' do |ss|
        ss.vendored_frameworks = "AgoraClearVisionExtension.xcframework"
        ss.weak_frameworks = "AgoraClearVisionExtension"
    end
    s.subspec 'ContentInspect' do |ss|
        ss.vendored_frameworks = "AgoraContentInspectExtension.xcframework"
        ss.weak_frameworks = "AgoraContentInspectExtension"
    end
    s.subspec 'SpatialAudio' do |ss|
        ss.vendored_frameworks = "AgoraSpatialAudioExtension.xcframework"
        ss.weak_frameworks = "AgoraSpatialAudioExtension"
    end
    s.subspec 'VirtualBackground' do |ss|
        ss.vendored_frameworks = "AgoraVideoSegmentationExtension.xcframework"
        ss.weak_frameworks = "AgoraVideoSegmentationExtension"
    end
    s.subspec 'AIAEC' do |ss|
        ss.vendored_frameworks = "AgoraAiEchoCancellationExtension.xcframework"
        ss.weak_frameworks = "AgoraAiEchoCancellationExtension"
    end
    s.subspec 'AIAECLL' do |ss|
        ss.vendored_frameworks = "AgoraAiEchoCancellationLLExtension.xcframework"
        ss.weak_frameworks = "AgoraAiEchoCancellationLLExtension"
    end
    s.subspec 'VQA' do |ss|
        ss.vendored_frameworks = "AgoraVideoQualityAnalyzerExtension.xcframework"
        ss.weak_frameworks = "AgoraVideoQualityAnalyzerExtension"
    end
    s.subspec 'FaceDetection' do |ss|
        ss.vendored_frameworks = "AgoraFaceDetectionExtension.xcframework"
        ss.weak_frameworks = "AgoraFaceDetectionExtension"
    end
    s.subspec 'FaceCapture' do |ss|
        ss.vendored_frameworks = "AgoraFaceCaptureExtension.xcframework"
        ss.weak_frameworks = "AgoraFaceCaptureExtension"
    end
    s.subspec 'LipSync' do |ss|
        ss.vendored_frameworks = "AgoraLipSyncExtension.xcframework"
        ss.weak_frameworks = "AgoraLipSyncExtension"
    end
    s.subspec 'VideoCodecEnc' do |ss|
        ss.vendored_frameworks = "AgoraVideoEncoderExtension.xcframework", "video_enc.xcframework"
        ss.weak_frameworks = "AgoraVideoEncoderExtension", "video_enc"
    end
    s.subspec 'VideoAv1CodecEnc' do |ss|
        ss.vendored_frameworks = "AgoraVideoAv1EncoderExtension.xcframework"
        ss.weak_frameworks = "AgoraVideoAv1EncoderExtension"
    end
    s.subspec 'ReplayKit' do |ss|
        ss.vendored_frameworks = "AgoraReplayKitExtension.xcframework"
        ss.weak_frameworks = "AgoraReplayKitExtension"
    end
 end

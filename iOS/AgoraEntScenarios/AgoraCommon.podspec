#
# Be sure to run `pod lib lint VideoLoaderAPI.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see https://guides.cocoapods.org/syntax/podspec.html
#

Pod::Spec.new do |s|
  s.name             = 'AgoraCommon'
  s.version          = '0.0.1'
  s.summary          = 'A short description of AgoraCommon.'

# This description is used to generate tags and improve search results.
#   * Think: What does it do? Why did you write it? What is the focus?
#   * Try to keep it short, snappy and to the point.
#   * Write the description between the DESC delimiters below.
#   * Finally, don't worry about the indent, CocoaPods strips it!

  s.description      = <<-DESC
TODO: Add long description of the pod here.
                       DESC

  s.homepage         = 'https://github.com/AgoraIO-Community/VideoLoaderAPI'
  # s.screenshots     = 'www.example.com/screenshots_1', 'www.example.com/screenshots_2'
  s.license          = { :type => 'MIT', :file => 'LICENSE' }
  s.author           = { 'Agora Lab' => 'developer@agora.io' }
  s.source           = { :git => 'https://github.com/AgoraIO-Community/VideoLoaderAPI.git', :tag => s.version.to_s }
  # s.social_media_url = 'https://twitter.com/<TWITTER_USERNAME>'
    
  s.ios.deployment_target = '10.0'
  
  s.xcconfig = {'ENABLE_BITCODE' => 'NO'}
  
  s.static_framework = true
  s.swift_version = '5.0'

  s.source_files = ['Context/*.swift',
                    'Common/Manager/Helper/UserCenter/*',
                    'Common/UI/VLNetwork/Model/VLBaseModel.*',
                    'Common/UI/Base/View/VLBaseView.*',
                    'Common/UI/Base/View/VLUIView.*',
                     'Common/UI/Base/View/VerifyCodeView.*',
                     'Common/UI/Base/View/VRVerifyCodeTextView.*',
                    'Common/UI/VLEmptyView/VLEmptyView.h',
                    'Common/UI/Base/ViewController/VLBaseViewController.*',
                    'Common/UI/Base/View/VRVerifyCodeNumberView.*',
                    'Common/UI/VLFont/VLDeviceUtils.h',
                    'Common/Manager/Authorized/AgoraEntAuthorizedManager.*',
                    'Common/Extension/String+Extension.swift',
                    'Common/Extension/UIImage+Resize.swift',
                    'HomeMenu/Login/Model/VLLoginModel.*',
                    'ThirdParty/QMUI/*',
                    'Common/Network/AUIError.*',
                    'Common/Utils/AESMacro.h',
                    'Common/Manager/VLDefine/VLMacroDefine.*',
                    'Common/Network/AgoraEntCommonLogger.*',
                    'Common/Network/AUINetworking.*',
                    'Common/Network/VLNetworkModel.swift',
                    'Common/Network/AUINetworkModel.swift',
                    'Common/Network/NetworkManagerModel.swift',
                    'Common/UI/VLHotSpotBtn/*',
                    'Common/Utils/JSONObject.swift',
                    'Common/Utils/Networktool.swift',
                    'Common/Utils/Config.swift',
                    'Common/Utils/SyncUtil.swift',
                    'Common/UI/Toast/VLToast.*',
                    'Common/UI/Toast/VLAlert.*',
                    'Common/UI/Toast/VLKTVAlert.*',
                    'Common/Utils/Throttle.*',
                    'Common/UI/ToastView/ToastView.*',
                    'Common/Extension/UIButton+EntLayout.*',
                    'Common/UI/ToastView/AUiMoreDialog.*',
                    'Common/UI/Base/View/AttributedTextView.*',
                    'Common/UI/Base/View/KTVCreateRoomPresentView.swift',
                    'Common/Manager/NetworkManager/NetworkManager.swift'
                    ]
                    
 s.resources = ['AgoraCommon/*.bundle']

  # s.public_header_files = 'Pod/Classes/**/*.h'
 # s.dependency 'AgoraRtcEngine_Special_iOS'
 s.dependency 'SwiftyBeaver'
 s.dependency 'Bugly'
 s.dependency 'YYModel'
 s.dependency 'KakaJSON'
 s.dependency 'YYCategories'
 s.dependency 'AgoraSyncManager'
 s.dependency 'ZSwiftBaseLib'
 s.dependency 'SVProgressHUD'
end

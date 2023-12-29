Pod::Spec.new do |spec|
  spec.name         = "AgoraSyncManager"
  spec.version      = "1.0.4"
  spec.summary      = "AgoraSyncManager"
  spec.description  = "AgoraSyncManager"

  spec.homepage     = "https://www.agora.io"
  spec.license      = "MIT"
  spec.author       = { "ZYP" => "zhuyuping@agora.io" }
  spec.ios.deployment_target = "10.0"
  spec.source       = { :git => "https://github.com/AgoraIO-Community/SyncManager-iOS.git", :tag => "1.0.3" }
  spec.source_files  = "**/*.swift"
  spec.pod_target_xcconfig = { 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'arm64', 'DEFINES_MODULE' => 'YES' }
  spec.user_target_xcconfig = { 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'arm64', 'DEFINES_MODULE' => 'YES' }
  spec.ios.deployment_target = '11.0'
  spec.swift_versions = "5.0"
  spec.dependency "AgoraRtm_iOS", "1.5.1"
  spec.dependency "AgoraSyncKit"
end

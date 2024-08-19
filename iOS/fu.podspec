Pod::Spec.new do |s|
    s.name             = 'fuLib'
    s.version          = '8.11.1'
    s.summary          = 'A framework of FaceUnity.'
    s.description      = <<-DESC
    a seak and package for sticker object and user do not attention release the sticker. convenient and easy to user FaceUnity function.
                         DESC
    s.homepage         = 'https://github.com/Faceunity/FULiveDemo'
    s.author           = { 'faceunity' => 'dev@faceunity.com' }
    s.source           = { :path => '.' }
    s.source_files = 'FURenderKit/**/*.{h,m}'
    s.public_header_files = 'FURenderKit/**/*.h'
    s.platform = :ios, '9.0'
    s.resources = "FURenderKit/**/*.{bundle,txt}"
    s.ios.vendored_frameworks = 'FURenderKit/*.framework'
    s.frameworks = ["OpenGLES",
        "Accelerate",
        "CoreMedia",
        "AVFoundation"]
    s.libraries = ["stdc++"]

    # Flutter.framework does not contain a i386 slice. Only x86_64 simulators are supported.
    s.pod_target_xcconfig = {'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'arm64' }
    
    s.prepare_command = <<-CMD
      rm -rf FURenderKit
      folder="FURenderKit"
      if [ ! -d "$folder" ]; then
         mkdir -p "$folder"
      fi
      curl -L -o FURenderKit/resource.zip https://fu-sdk.oss-cn-hangzhou.aliyuncs.com/Nama_release/8.11.1/all_feature/ios_release/202407251624580800_7f52ee6/FURenderKit_all_feature.zip
      unzip -o FURenderKit/resource.zip -d FURenderKit
      rm -rf FURenderKit/Resources
      rm -rf FURenderKit/resource.zip
   CMD

  end
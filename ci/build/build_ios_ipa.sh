export PATH=$PATH:/opt/homebrew/bin
export LANG=en_US.UTF-8

# 设置默认环境变量
CURRENT_PATH=$PWD
if [ -z "$WORKSPACE" ]; then
    export WORKSPACE=$(pwd)/ci/iosExport
    export LOCALPACKAGE="true"
    mkdir -p $WORKSPACE
else
    source /Users/admin/jenkins/bin/activate
fi

if [ -z "$build_date" ]; then
    export build_date=$(date +%Y%m%d)
fi

if [ -z "$build_time" ]; then
    export build_time=$(date +%H%M%S)
fi

BUILD_VERSION=$(date +%Y%m%d%H%M%S)

# 获取项目目录
PROJECT_PATH="${CURRENT_PATH}/iOS"
echo "Project path: ${PROJECT_PATH}"

# 检查iOS目录是否存在
if [ ! -d "${PROJECT_PATH}" ]; then
    echo "Error: iOS directory not found: ${PROJECT_PATH}"
    echo "Build failed: iOS project directory does not exist"
    exit 1
fi
# 项目相关参数
if [ -z "$PROJECT_NAME" ]; then
    export PROJECT_NAME="AgoraEntScenarios"
fi

if [ -z "$TARGET_NAME" ]; then
    export TARGET_NAME="AgoraEntScenarios"
fi

if [ -z "$packageName" ]; then
    export packageName="io.agora.AgoraVoice"
fi

if [ -z "$method" ]; then
    export method="development"
fi

# 读取版本号
export release_version=$(grep "MARKETING_VERSION" "${PROJECT_PATH}/AgoraEntScenarios.xcodeproj/project.pbxproj" | head -n 1 | cut -d "=" -f2 | tr -d ' ";')
if [ -z "$release_version" ]; then
    echo "Error: Unable to read version number from project configuration"
    exit 1
fi
echo "Version number read from project configuration: ${release_version}"

# 设置制品名称
export ARTIFACT_NAME="${TARGET_NAME}_for_iOS_v${release_version}_${BUILD_VERSION}"
echo "Artifact name: ${ARTIFACT_NAME}"

# 检查关键环境变量
echo "Checking iOS build environment variables:"
echo "Xcode version: $(xcodebuild -version | head -n 1)"
echo "Swift version: $(swift --version | head -n 1)"
echo "Ruby version: $(ruby --version)"
echo "CocoaPods version: $(pod --version)"

# 证书相关参数
if [ "$method" = "app-store" ]; then
    # App Store发布配置
    PROVISIONING_PROFILE="AgoraVoice_dis"
    CODE_SIGN_IDENTITY="Apple Distribution"
    DEVELOPMENT_TEAM="48TB6ZZL5S"
else
    # 开发环境配置
    PROVISIONING_PROFILE="AgoraVoice_dev"
    CODE_SIGN_IDENTITY="Apple Development"
    DEVELOPMENT_TEAM="48TB6ZZL5S"
fi

echo "Build Configuration:"
echo "PROJECT_NAME: $PROJECT_NAME"
echo "TARGET_NAME: $TARGET_NAME"
echo "packageName: $packageName"
echo "method: $method"
echo "DEVELOPMENT_TEAM: $DEVELOPMENT_TEAM"
echo "PROVISIONING_PROFILE: $PROVISIONING_PROFILE"
echo "CODE_SIGN_IDENTITY: $CODE_SIGN_IDENTITY"

if [ -z "$BUILD_NUMBER" ]; then
    export BUILD_NUMBER="1"
    echo "设置默认 BUILD_NUMBER: $BUILD_NUMBER"
fi

if [ -z "$APP_ID" ]; then
    export APP_ID="your_app_id_here"
    echo "设置默认 APP_ID: $APP_ID"
fi

if [ -z "$manifest_url" ]; then
    export manifest_url="https://example.com/manifest.json"
    echo "设置默认 manifest_url: $manifest_url"
fi

swift_version=$(swift --version)
echo "Swift 版本: $swift_version"

xcode_version=$(xcodebuild -version)
echo "当前 Xcode 版本: $xcode_version"

echo PROJECT_PATH: $PROJECT_PATH
echo TARGET_NAME: $TARGET_NAME
echo pwd: $CURRENT_PATH

cd ${PROJECT_PATH}
pod install

if [ $? -eq 0 ]; then
    echo "success"
else
    echo "failed"
    exit 1
fi

# 项目target名
TARGET_NAME=AgoraEntScenarios

KEYCENTER_PATH=${PROJECT_PATH}"/"${TARGET_NAME}"/KeyCenter.swift"

# 打包环境
CONFIGURATION='Release'

#工程文件路径
APP_PATH="${PROJECT_PATH}/${PROJECT_NAME}.xcworkspace"

#工程配置路径
PBXPROJ_PATH="${PROJECT_PATH}/${PROJECT_NAME}.xcodeproj/project.pbxproj"
echo PBXPROJ_PATH: $PBXPROJ_PATH

# 验证文件存在性
echo "Verifying file and directory existence:"
if [ ! -e "${APP_PATH}" ]; then
    echo "Error: Project file not found: ${APP_PATH}"
    # 搜索workspace文件
    find ${PROJECT_PATH} -name "*.xcworkspace"
    exit 1
fi

if [ ! -f "${PBXPROJ_PATH}" ]; then
    echo "Error: Project configuration file not found: ${PBXPROJ_PATH}"
    # 搜索project.pbxproj文件
    find ${PROJECT_PATH} -name "project.pbxproj" -type f
    exit 1
fi

# 非本地构建时解锁keychain
if [ "$LOCALPACKAGE" != "true" ]; then
    echo "Non-local build, starting keychain unlock..."
    cd ~/Library/Keychains
    cp login.keychain-db login.keychain
    security unlock-keychain -p "123456" ~/Library/Keychains/login.keychain
    if [ $? -eq 0 ]; then
        echo "Keychain unlocked successfully"
    else
        echo "Error: Keychain unlock failed"
        exit 1
    fi
fi

# 主项目工程配置
# 根据method判断是否为development环境
if [ "$method" = "development" ]; then
    # Development环境配置
    sed -i '' "s|CURRENT_PROJECT_VERSION = .*;|CURRENT_PROJECT_VERSION = ${BUILD_VERSION};|g" $PBXPROJ_PATH
    sed -i '' "s|PRODUCT_BUNDLE_IDENTIFIER = .*;|PRODUCT_BUNDLE_IDENTIFIER = \"${packageName}\";|g" $PBXPROJ_PATH
    sed -i '' "s|CODE_SIGN_STYLE = .*;|CODE_SIGN_STYLE = \"Manual\";|g" $PBXPROJ_PATH
    sed -i '' "s|DEVELOPMENT_TEAM = .*;|DEVELOPMENT_TEAM = \"${DEVELOPMENT_TEAM}\";|g" $PBXPROJ_PATH
    sed -i '' "s|PROVISIONING_PROFILE_SPECIFIER = .*;|PROVISIONING_PROFILE_SPECIFIER = \"${PROVISIONING_PROFILE}\";|g" $PBXPROJ_PATH
    sed -i '' "s|CODE_SIGN_IDENTITY = .*;|CODE_SIGN_IDENTITY = \"${CODE_SIGN_IDENTITY}\";|g" $PBXPROJ_PATH
else
    # 其他环境配置保持不变
    sed -i '' "s|CURRENT_PROJECT_VERSION = .*;|CURRENT_PROJECT_VERSION = ${BUILD_VERSION};|g" $PBXPROJ_PATH
    sed -i '' "s|PRODUCT_BUNDLE_IDENTIFIER = .*;|PRODUCT_BUNDLE_IDENTIFIER = \"${packageName}\";|g" $PBXPROJ_PATH
    sed -i '' "s|CODE_SIGN_STYLE = .*;|CODE_SIGN_STYLE = \"Manual\";|g" $PBXPROJ_PATH
    sed -i '' "s|DEVELOPMENT_TEAM = .*;|DEVELOPMENT_TEAM = \"${DEVELOPMENT_TEAM}\";|g" $PBXPROJ_PATH
    sed -i '' "s|PROVISIONING_PROFILE_SPECIFIER = .*;|PROVISIONING_PROFILE_SPECIFIER = \"${PROVISIONING_PROFILE}\";|g" $PBXPROJ_PATH
    sed -i '' "s|CODE_SIGN_IDENTITY = .*;|CODE_SIGN_IDENTITY = \"${CODE_SIGN_IDENTITY}\";|g" $PBXPROJ_PATH
fi

# 读取APPID环境变量
echo AGORA_APP_ID:$APP_ID
echo $AGORA_APP_ID

echo PROJECT_PATH: $PROJECT_PATH
echo TARGET_NAME: $TARGET_NAME
echo KEYCENTER_PATH: $KEYCENTER_PATH
echo APP_PATH: $APP_PATH
echo manifest_url: $manifest_url

#修改Keycenter文件
if [ "$LOCALPACKAGE" = "true" ]; then
    echo "本地包构建，跳过KeyCenter修改"
else
    echo "非本地包构建，开始修改KeyCenter文件..."
    if [ -f "${CURRENT_PATH}/ci/build/modify_ios_keycenter.py" ]; then
        python3 ${CURRENT_PATH}/ci/build/modify_ios_keycenter.py $KEYCENTER_PATH
    else
        echo "Error: modify_ios_keycenter.py not found at ${CURRENT_PATH}/ci/build/modify_ios_keycenter.py"
        exit 1
    fi
fi

# 归档路径
ARCHIVE_PATH="${WORKSPACE}/${TARGET_NAME}_${BUILD_VERSION}.xcarchive"

# 设置导出选项文件路径
if [ "$method" = "development" ]; then
    EXPORT_OPTIONS_PLIST="${CURRENT_PATH}/ci/build/ExportOptions_development.plist"
else
    EXPORT_OPTIONS_PLIST="${CURRENT_PATH}/ci/build/ExportOptions_app-store.plist"
fi

echo "Export Options Plist Path: $EXPORT_OPTIONS_PLIST"

# 验证导出选项文件存在
if [ ! -f "$EXPORT_OPTIONS_PLIST" ]; then
    echo "Error: Export options plist file not found: $EXPORT_OPTIONS_PLIST"
    exit 1
fi

# 创建导出目录
EXPORT_PATH="${WORKSPACE}/output"
# 清理现有导出目录
if [ -d "${EXPORT_PATH}" ]; then
    echo "Cleaning existing export directory: ${EXPORT_PATH}"
    rm -rf "${EXPORT_PATH}"
fi
mkdir -p "${EXPORT_PATH}"

# 构建和归档
echo "Starting build and archive..."
xcodebuild clean -workspace "${APP_PATH}" -scheme "${TARGET_NAME}" -configuration "${CONFIGURATION}" -quiet
xcodebuild CODE_SIGN_STYLE="Manual" \
    -workspace "${APP_PATH}" \
    -scheme "${TARGET_NAME}" \
    clean \
    CODE_SIGNING_REQUIRED=NO \
    CODE_SIGNING_ALLOWED=NO \
    -configuration "${CONFIGURATION}" \
    archive \
    -archivePath "${ARCHIVE_PATH}" \
    -destination 'generic/platform=iOS' \
    DEBUG_INFORMATION_FORMAT=dwarf-with-dsym \
    -quiet || exit

# 导出IPA
echo "Starting IPA export..."
xcodebuild -exportArchive \
    -archivePath "${ARCHIVE_PATH}" \
    -exportPath "${EXPORT_PATH}" \
    -exportOptionsPlist "${EXPORT_OPTIONS_PLIST}" \
    -allowProvisioningUpdates

cd ${WORKSPACE}

# 创建临时目录用于打包文件
PACKAGE_DIR="${WORKSPACE}/package_temp"
mkdir -p "${PACKAGE_DIR}"

# 复制IPA和dSYM到临时目录
if [ -f "${EXPORT_PATH}/${TARGET_NAME}.ipa" ]; then
    cp "${EXPORT_PATH}/${TARGET_NAME}.ipa" "${PACKAGE_DIR}/${ARTIFACT_NAME}.ipa"
else
    echo "Error: IPA file not found!"
    exit 1
fi

if [ -d "${ARCHIVE_PATH}/dSYMs" ] && [ "$(ls -A "${ARCHIVE_PATH}/dSYMs")" ]; then
    cp -r "${ARCHIVE_PATH}/dSYMs" "${PACKAGE_DIR}/"
else
    echo "Warning: dSYMs directory is empty or does not exist!"
    mkdir -p "${PACKAGE_DIR}/dSYMs"
fi

# 打包IPA和dSYM
cd "${PACKAGE_DIR}"
zip -r "${WORKSPACE}/${ARTIFACT_NAME}.zip" ./
cd "${WORKSPACE}"

# 非本地构建时上传文件
if [ "$LOCALPACKAGE" != "true" ]; then
    echo "Uploading artifact to artifact repository..."
    
    # 上传文件到制品库并保存输出
    UPLOAD_RESULT=$(python3 artifactory_utils.py --action=upload_file --file="${ARTIFACT_NAME}.zip" --project)
    
    # 检查上传结果是否为URL
    if [[ "$UPLOAD_RESULT" =~ ^https?:// ]]; then
        echo "====🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉🎉========="
        echo "Artifact uploaded successfully! Download URL:"
        echo "$UPLOAD_RESULT"
        echo "===================================================="
    else
        echo "Warning: Upload result format is abnormal"
        echo "Complete upload result:"
        echo "$UPLOAD_RESULT"
    fi
    
    # 清理本地制品
    rm -f "${ARTIFACT_NAME}.zip"
fi

# 清理文件
rm -rf ${TARGET_NAME}_${BUILD_VERSION}.xcarchive
rm -rf ${PACKAGE_DIR}
rm -rf ${EXPORT_PATH}

echo 'Build completed'

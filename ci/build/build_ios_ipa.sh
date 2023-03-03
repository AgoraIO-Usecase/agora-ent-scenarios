CURRENT_PATH=$PWD

# 获取项目目录
PROJECT_PATH="${CURRENT_PATH}/iOS"

echo PROJECT_PATH: $PROJECT_PATH
echo TARGET_NAME: $TARGET_NAME
echo pwd: $CURRENT_PATH

cd ${PROJECT_PATH} && pod install --repo-update

if [ $? -eq 0 ]; then
    echo "success"
else
    echo "failed"
    exit 1
fi

# 项目target名
TARGET_NAME=AgoraEntScenarios

KEYCENTER_PATH=${PROJECT_PATH}"/"${TARGET_NAME}"/KeyCenter.swift"
cp $KEYCENTER_PATH.bak $KEYCENTER_PATH

# 打包环境
set CONFIGURATION='Release'
result=$(echo ${method} | grep "development")
if [ -z "$result" ]
then
    CONFIGURATION='Debug'
fi

#工程文件路径
APP_PATH="${PROJECT_PATH}/${TARGET_NAME}.xcworkspace"

#工程配置路径
PBXPROJ_PATH="${PROJECT_PATH}/${TARGET_NAME}.xcodeproj/project.pbxproj"
echo PBXPROJ_PATH: $PBXPROJ_PATH

# 主项目工程配置
# Debug
# /usr/libexec/PlistBuddy -c "Set :objects:DD2A43F228FFCEE7004CEDCF:buildSettings:CODE_SIGN_STYLE 'Manual'" $PBXPROJ_PATH
# /usr/libexec/PlistBuddy -c "Set :objects:DD2A43F228FFCEE7004CEDCF:buildSettings:DEVELOPMENT_TEAM 'GM72UGLGZW'" $PBXPROJ_PATH
# /usr/libexec/PlistBuddy -c "Set :objects:DD2A43F228FFCEE7004CEDCF:buildSettings:PROVISIONING_PROFILE_SPECIFIER 'App'" $PBXPROJ_PATH
# Release
# /usr/libexec/PlistBuddy -c "Set :objects:DD2A43F328FFCEE7004CEDCF:buildSettings:CODE_SIGN_STYLE 'Manual'" $PBXPROJ_PATH
# /usr/libexec/PlistBuddy -c "Set :objects:DD2A43F328FFCEE7004CEDCF:buildSettings:DEVELOPMENT_TEAM 'GM72UGLGZW'" $PBXPROJ_PATH
# /usr/libexec/PlistBuddy -c "Set :objects:DD2A43F328FFCEE7004CEDCF:buildSettings:PROVISIONING_PROFILE_SPECIFIER 'App'" $PBXPROJ_PATH

# 读取APPID环境变量
echo AGORA_APP_ID:$APP_ID
echo $AGORA_APP_ID

echo PROJECT_PATH: $PROJECT_PATH
echo TARGET_NAME: $TARGET_NAME
echo KEYCENTER_PATH: $KEYCENTER_PATH
echo APP_PATH: $APP_PATH

#修改Keycenter文件
python3 /tmp/jenkins/agora-ent-scenarios/ci/build/modify_ios_keycenter.py $KEYCENTER_PATH 0

# Xcode clean
xcodebuild clean -workspace "${APP_PATH}" -configuration "${CONFIGURATION}" -scheme "${TARGET_NAME}" -quiet

# 时间戳
CURRENT_TIME=$(date "+%Y-%m-%d %H-%M-%S")

# 归档路径
ARCHIVE_PATH="${PROJECT_PATH}/${TARGET_NAME} ${CURRENT_TIME}/${TARGET_NAME}_${BUILD_NUMBER}.xcarchive"
# 编译环境

# 导出路径
EXPORT_PATH="${PROJECT_PATH}/${TARGET_NAME} ${CURRENT_TIME}"

# plist路径
PLIST_PATH="${PROJECT_PATH}/ExportOptions_${method}.plist"

echo PLIST_PATH: $PLIST_PATH

# archive 这边使用的工作区间 也可以使用project
xcodebuild archive -workspace "${APP_PATH}" -scheme "${TARGET_NAME}" -configuration "${CONFIGURATION}" -archivePath "${ARCHIVE_PATH}" -destination 'generic/platform=iOS' -quiet

# 导出ipa
xcodebuild -exportArchive -archivePath "${ARCHIVE_PATH}" -exportPath "${EXPORT_PATH}" -exportOptionsPlist "${PLIST_PATH}" CODE_SIGNING_REQUIRED=NO CODE_SIGNING_ALLOWED=NO -quiet

# 给ipa包签名
echo "============Sign IPA Begin============"
sh ${WORKSPACE}/sign "${EXPORT_PATH}"

mv *.ipa ${EXPORT_PATH}

# 上传IPA
7za a -tzip ${TARGET_NAME}_${BUILD_NUMBER}.zip -r "${EXPORT_PATH}/${TARGET_NAME}.ipa"
python3 $WORKSPACE/artifactory_utils.py --action=upload_file --file="${PROJECT_PATH}/${TARGET_NAME}_${BUILD_NUMBER}.zip" --project

# 上传符号表
7za a -tzip dsym_${BUILD_NUMBER}.zip -r "${ARCHIVE_PATH}/dSYMs/${TARGET_NAME}.app.dSYM"
python3 $WORKSPACE/artifactory_utils.py --action=upload_file --file="${PROJECT_PATH}/dsym_${BUILD_NUMBER}.zip" --project

# 删除IPA文件夹
rm -rf "${EXPORT_PATH}"
cd ${PROJECT_PATH} && rm -rf "*.zip"

#复原Keycenter文件
python3 /tmp/jenkins/agora-ent-scenarios/ci/build/modify_ios_keycenter.py $KEYCENTER_PATH 1





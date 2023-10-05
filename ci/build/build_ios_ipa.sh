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

# 打包环境
CONFIGURATION='Release'
result=$(echo ${method} | grep "development")
if [[ ! -z "$result" ]]
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
# /usr/libexec/PlistBuddy -c "Set :objects:DD2A43F228FFCEE7004CEDCF:buildSettings:DEVELOPMENT_TEAM 'YS397FG5PA'" $PBXPROJ_PATH
# /usr/libexec/PlistBuddy -c "Set :objects:DD2A43F228FFCEE7004CEDCF:buildSettings:PROVISIONING_PROFILE_SPECIFIER 'App'" $PBXPROJ_PATH
/usr/libexec/PlistBuddy -c "Set :objects:DD2A43F228FFCEE7004CEDCF:buildSettings:CURRENT_PROJECT_VERSION ${BUILD_NUMBER}" $PBXPROJ_PATH
/usr/libexec/PlistBuddy -c "Set :objects:DD2A43F228FFCEE7004CEDCF:buildSettings:PRODUCT_BUNDLE_IDENTIFIER ${packageName}" $PBXPROJ_PATH
# Release
# /usr/libexec/PlistBuddy -c "Set :objects:DD2A43F328FFCEE7004CEDCF:buildSettings:CODE_SIGN_STYLE 'Manual'" $PBXPROJ_PATH
# /usr/libexec/PlistBuddy -c "Set :objects:DD2A43F328FFCEE7004CEDCF:buildSettings:DEVELOPMENT_TEAM 'YS397FG5PA'" $PBXPROJ_PATH
# /usr/libexec/PlistBuddy -c "Set :objects:DD2A43F328FFCEE7004CEDCF:buildSettings:PROVISIONING_PROFILE_SPECIFIER 'App'" $PBXPROJ_PATH
/usr/libexec/PlistBuddy -c "Set :objects:DD2A43F328FFCEE7004CEDCF:buildSettings:CURRENT_PROJECT_VERSION ${BUILD_NUMBER}" $PBXPROJ_PATH
/usr/libexec/PlistBuddy -c "Set :objects:DD2A43F328FFCEE7004CEDCF:buildSettings:PRODUCT_BUNDLE_IDENTIFIER ${packageName}" $PBXPROJ_PATH

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
CURRENT_TIME=$(date "+%Y-%m-%d_%H-%M-%S")

# 归档路径
ARCHIVE_PATH="${WORKSPACE}/${TARGET_NAME}_${BUILD_NUMBER}.xcarchive"  #"${PROJECT_PATH}/${TARGET_NAME}_${CURRENT_TIME}/${TARGET_NAME}_${BUILD_NUMBER}.xcarchive"
# 编译环境

# plist路径
PLIST_PATH="${CURRENT_PATH}/ci/build/ExportOptions_${method}.plist"

echo PLIST_PATH: $PLIST_PATH

# archive 这边使用的工作区间 也可以使用project
xcodebuild CODE_SIGN_STYLE="Manual" -workspace "${APP_PATH}" -scheme "${TARGET_NAME}" clean CODE_SIGNING_REQUIRED=NO CODE_SIGNING_ALLOWED=NO -configuration "${CONFIGURATION}" archive -archivePath "${ARCHIVE_PATH}" -destination 'generic/platform=iOS' -quiet || exit

cd ${WORKSPACE}
# 压缩archive
7za a -tzip "${TARGET_NAME}_${BUILD_NUMBER}.xcarchive.zip" "${ARCHIVE_PATH}"

sh export "${TARGET_NAME}_${BUILD_NUMBER}.xcarchive.zip" --project AES --plist "${PLIST_PATH}"

PAYLOAD_PATH="${TARGET_NAME}_${BUILD_NUMBER}_Payload"
# 上传IPA
mkdir "${PAYLOAD_PATH}"
mv "${TARGET_NAME}_${BUILD_NUMBER}.ipa" "${PAYLOAD_PATH}"
7za a -tzip ${TARGET_NAME}_${BUILD_NUMBER}.zip -r "${PAYLOAD_PATH}"
python3 artifactory_utils.py --action=upload_file --file="${TARGET_NAME}_${BUILD_NUMBER}.zip" --project

# 上传符号表
7za a -tzip dsym_${BUILD_NUMBER}.zip -r "${ARCHIVE_PATH}/dSYMs/${TARGET_NAME}.app.dSYM"
python3 artifactory_utils.py --action=upload_file --file="dsym_${BUILD_NUMBER}.zip" --project

echo "Debug info ***"
ls $WORKSPACE

# 删除文件
rm -rf ${TARGET_NAME}_${BUILD_NUMBER}.xcarchive
rm -rf *.zip
rm -rf ${PAYLOAD_PATH}

ls $WORKSPACE
echo "Debug info *** end"

# 复原Keycenter文件
python3 /tmp/jenkins/agora-ent-scenarios/ci/build/modify_ios_keycenter.py $KEYCENTER_PATH 1

echo 'reset keycenter down'



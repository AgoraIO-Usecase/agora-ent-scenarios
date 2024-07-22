# --------------------------------------------------------------------------------------------------------------------------
# =====================================
# ========== Guidelines ===============
# =====================================
#
# -------------------------------------
# ---- Common Environment Variable ----
# -------------------------------------
# ${Package_Publish} (boolean): Indicates whether it is build package process, e.g. If you want to get one CI SDK package.
# ${Clean_Clone} (boolean): Indicates whether it is clean build. If true, CI will clean ${output} for each build process.
# ${is_tag_fetch} (boolean): If true, git checkout will work as tag fetch mode.
# ${is_official_build} (boolean): Indicates whether it is official build release.
# ${arch} (string): Indicates build arch set in build pipeline.
# ${short_version} (string): CI auto generated short version string.
# ${release_version} (string): CI auto generated version string.
# ${build_date} (string(yyyyMMdd)): Build date generated by CI.
# ${build_timestamp} (string (yyyyMMdd_hhmm)): Build timestamp generated by CI.
# ${platform} (string): Build platform generated by CI.
# ${BUILD_NUMBER} (string): Build number generated by CI.
# ${WORKSPACE} (string): Working dir generated by CI.
#
# -------------------------------------
# ------- Job Custom Parameters -------
# -------------------------------------
# If you added one custom parameter via rehoboam website, e.g. extra_args.
# You could use $extra_args to get its value.
#
# -------------------------------------
# ------------- Input -----------------
# -------------------------------------
# ${source_root}: Source root which checkout the source code.
# ${WORKSPACE}: project owned private workspace.
#
# -------------------------------------
# ------------- Output ----------------
# -------------------------------------
# Generally, we should put the output files into ${WORKSPACE}
# 1. for pull request: Output files should be zipped to test.zip, and then copy to ${WORKSPACE}.
# 2. for pull request (options): Output static xml should be static_${platform}.xml, and then copy to ${WORKSPACE}.
# 3. for others: Output files should be zipped to anything_you_want.zip, and then copy it to {WORKSPACE}.
#
# -------------------------------------
# --------- Avaliable Tools -----------
# -------------------------------------
# Compressing & Decompressing: 7za a, 7za x
#
# -------------------------------------
# ----------- Test Related ------------
# -------------------------------------
# PR build, zip test related to test.zip
# Package build, zip package related to package.zip
#
# -------------------------------------
# ------ Publish to artifactory -------
# -------------------------------------
# [Download] artifacts from artifactory:
# python3 ${WORKSPACE}/artifactory_utils.py --action=download_file --file=ARTIFACTORY_URL
#
# [Upload] artifacts to artifactory:
# python3 ${WORKSPACE}/artifactory_utils.py --action=upload_file --file=FILEPATTERN --project
# Sample Code:
# python3 ${WORKSPACE}/artifactory_utils.py --action=upload_file --file=*.zip --project
#
# [Upload] artifacts folder to artifactory
# python3 ${WORKSPACE}/artifactory_utils.py --action=upload_file --file=FILEPATTERN --project --with_folder
# Sample Code:
# python3 ${WORKSPACE}/artifactory_utils.py --action=upload_file --file=./folder --project --with_folder
#
# ========== Guidelines End=============
# --------------------------------------------------------------------------------------------------------------------------


echo Package_Publish: $Package_Publish
echo is_tag_fetch: $is_tag_fetch
echo arch: $arch
echo source_root: ${source_root}
echo output: /tmp/jenkins/${project}_out
echo build_date: $build_date
echo build_time: $build_time
echo release_version: $release_version
echo short_version: $short_version
echo beauty_sources $beauty_sources
echo sdk_url: $sdk_url
echo pwd: `pwd`

# enter android project direction
cd Android

# config android environment
source ~/.bashrc
export ANDROID_HOME=/usr/lib/android_sdk
ls ~/.gradle || mkdir -p /tmp/.gradle && ln -s /tmp/.gradle ~/.gradle && touch ~/.gradle/ln_$(date "+%y%m%d%H") && ls ~/.gradle
echo ANDROID_HOME: $ANDROID_HOME
java --version

# download native sdk if need
if [[ ! -z ${sdk_url} && "${sdk_url}" != 'none' ]]; then
    zip_name=${sdk_url##*/}
    curl -L -H "X-JFrog-Art-Api:${JFROG_API_KEY}" -O $sdk_url || exit 1
    7za x ./$zip_name -y

    unzip_name=`ls -S -d */ | grep Agora`
    echo unzip_name: $unzip_name

    mkdir -p common/base/agora-sdk

    echo source sdk path: "${unzip_name}rtc/sdk/"
    cp -a ${unzip_name}rtc/sdk/. common/base/agora-sdk/
    ls common/base/agora-sdk/

    # config app global properties
    sed -ie "s#$(sed -n '/USE_LOCAL_SDK/p' gradle.properties)#USE_LOCAL_SDK=true#g" gradle.properties
fi

# config app global properties
sed -ie "s#$(sed -n '/SERVER_HOST/p' gradle.properties)#SERVER_HOST=${SERVER_HOST}#g" gradle.properties
sed -ie "s#$(sed -n '/AGORA_APP_ID/p' gradle.properties)#AGORA_APP_ID=${APP_ID}#g" gradle.properties
sed -ie "s#$(sed -n '/IM_APP_KEY/p' gradle.properties)#IM_APP_KEY=${IM_APP_KEY}#g" gradle.properties
sed -ie "s#$(sed -n '/BEAUTY_RESOURCE/p' gradle.properties)#BEAUTY_RESOURCE=${beauty_sources}#g" gradle.properties
cat gradle.properties

# Compile apk
./gradlew clean || exit 1
./gradlew :app:assembleRelease || exit 1

# Upload apk
rm -rf ${WORKSPACE}/*.apk && cp app/build/outputs/apk/release/*.apk ${WORKSPACE}


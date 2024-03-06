#include <jni.h>

#ifndef _Included_io_agora_premium_model
#define _Included_io_agora_premium_model

#ifdef __cplusplus
extern "C" {
#endif
JNIEXPORT jlong JNICALL Java_io_agora_api_example_utils_MetaEngineHandler_getTextureViewHandler
        (JNIEnv *, jobject, jobject);

JNIEXPORT jlong JNICALL Java_io_agora_api_example_utils_MetaEngineHandler_getContextHandler
        (JNIEnv *, jobject, jobject);

JNIEXPORT void JNICALL Java_io_agora_api_example_utils_MetaEngineHandler_destroyHandles
        (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif

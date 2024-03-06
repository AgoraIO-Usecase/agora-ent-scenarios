#include <jni.h>
#include <android/log.h>
#include <cstring>
#include <vector>

#include "video_premium_model_jni.h"

static jobject m_contextRef = NULL;
static std::vector<jobject> m_viewRefList;

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_io_agora_api_example_utils_MetaEngineHandler_getTextureViewHandler
  (JNIEnv *env, jobject obj, jobject view)
{
    jobject viewRef = env->NewGlobalRef(view);
    m_viewRefList.push_back(viewRef);
    jlong handler = reinterpret_cast<jlong>(viewRef);
    return handler;
}

JNIEXPORT jlong JNICALL Java_io_agora_api_example_utils_MetaEngineHandler_getContextHandler
        (JNIEnv *env, jobject obj, jobject context)
{
    m_contextRef = env->NewGlobalRef(context);
    jlong handler = reinterpret_cast<jlong>(m_contextRef);
    return handler;
}

JNIEXPORT void JNICALL Java_io_agora_api_example_utils_MetaEngineHandler_destroyHandles
        (JNIEnv *env, jobject obj)
{
    if (m_contextRef) {
        env->DeleteGlobalRef(m_contextRef);
        m_contextRef = NULL;
    }

    for (auto iter = m_viewRefList.begin(); iter != m_viewRefList.end(); iter++)
    {
        if (*iter) {
            env->DeleteGlobalRef(*iter);
        }
    }
    m_viewRefList.clear();
}

#ifdef __cplusplus
}
#endif

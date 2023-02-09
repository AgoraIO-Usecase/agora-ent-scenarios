/**
*@file st_mobile_face_attribute.h

* 提供人脸特征检测功能，并可根据人脸特征，识别年龄、性别、肤色、五官、表情等
*
*@attention
*该文件中的API不保证线程安全.多线程调用时,需要确保安全调用.
* 例如在 detect/reset 没有执行完就执行 destroy 可能造成crash;
* 在 detect 执行过程中调用 reset 函数可能会造成crash.

* 一般调用步骤：创建句柄->检测特征->销毁句柄

* st_mobile_face_attribute_create

* st_mobile_face_attribute_detect

* st_mobile_face_attribute_destroy

**/
#ifndef INCLUDE_STMOBILE_ST_MOBILE_FACE_ATTRIBUTE_H_
#define INCLUDE_STMOBILE_ST_MOBILE_FACE_ATTRIBUTE_H_

#include "st_mobile_common.h"


/// @brief 创建人脸属性检测句柄
/// @param[in] model_path 模型文件的绝对路径或相对路径
/// @param[out] handle 人脸属性检测句柄
/// @return 成功返回ST_OK, 错误则返回错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_face_attribute_create(
    const char *model_path,
    st_handle_t *handle
);
/// @brief 创建人脸属性检测句柄
/// @param[in] buffer 模型缓存起始地址
/// @param[in] buffer_size 模型缓存大小
/// @param[out] handle 人脸属性检测句柄
/// @return 成功返回ST_OK, 错误则返回错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_face_attribute_create_from_buffer(
const unsigned char* buffer,
unsigned int buffer_size,
st_handle_t * handle);

/// @brief 销毁已初始化的人脸属性检测句柄
/// @param[in] handle 已初始化的人脸属性检测句柄
ST_SDK_API void
st_mobile_face_attribute_destroy(
    st_handle_t handle
);
#ifndef ST_MOBILE_MAX_ATTR_STRING_LEN
#define ST_MOBILE_MAX_ATTR_STRING_LEN 32
#endif

/// @brief 单个属性
typedef struct st_mobile_attribute_t {
    char category[ST_MOBILE_MAX_ATTR_STRING_LEN];   ///< 属性描述, 例如 "age", "gender" 或 "attractive"
    char label[ST_MOBILE_MAX_ATTR_STRING_LEN];      ///< 属性标签描述, 例如 "male", "female", "21" 等
    float score;                                    ///< 该属性标签的置信度
} st_mobile_attribute_t;

/// @brief 一个人脸的所有属性
typedef struct st_mobile_attributes_t {
    st_mobile_attribute_t *p_attributes;    ///< 属性数组
    int attribute_count;                    ///< 属性个数
} st_mobile_attributes_t;

/// @brief 人脸属性检测
/// @param[in] handle 已初始化的人脸属性检测句柄
/// @param[in] image 用于检测的图像数据,推荐灰度格式
/// @param[in] pixel_format 用于检测的图像数据的像素格式, 支持所有彩色图像格式,推荐ST_PIX_FMT_BGR888,不建议使用ST_PIX_FMT_GRAY8（结果不准确）
/// @param[in] image_width 用于检测的图像的宽度(以像素为单位)
/// @param[in] image_height 用于检测的图像的高度(以像素为单位)
/// @param[in] image_stride 用于检测的图像中每一行的跨度(以像素为单位)
/// @param[in] p_face_array 输入待处理的人脸信息,需要包括关键点信息
/// @param[in] face_count 输入的人脸个数
/// @param[out] p_attributes_array 检测到的人脸属性结果数组,包含face_count个st_mobile_attributes_t
/// @return 成功返回ST_OK, 错误则返回错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_face_attribute_detect(
    st_handle_t handle,
    const unsigned char *image,
    st_pixel_format pixel_format,
    int image_width,
    int image_height,
    int image_stride,
    const st_mobile_106_t *p_face_array,
    int face_count,
    st_mobile_attributes_t** p_attributes_array
);

/// @brief 人脸属性检测
/// @param[in] handle 已初始化的人脸属性检测句柄
/// @param[in] image 用于检测的图像数据,推荐灰度格式
/// @param[in] pixel_format 用于检测的图像数据的像素格式, 支持所有彩色图像格式,推荐ST_PIX_FMT_BGR888,不建议使用ST_PIX_FMT_GRAY8（结果不准确）
/// @param[in] image_width 用于检测的图像的宽度(以像素为单位)
/// @param[in] image_height 用于检测的图像的高度(以像素为单位)
/// @param[in] image_stride 用于检测的图像中每一行的跨度(以像素为单位)
/// @param[in] p_face_array 输入待处理的人脸信息,需要包括关键点信息
/// @param[in] face_count 输入的人脸个数
/// @param[out] p_attributes_array 检测到的人脸属性结果数组,包含face_count个st_mobile_attributes_t
/// @return 成功返回ST_OK, 错误则返回错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_face_attribute_detect2(
    st_handle_t handle,
    const unsigned char *image,
    st_pixel_format pixel_format,
    int image_width,
    int image_height,
    int image_stride,
    const st_mobile_face_t *p_face_array,
    int face_count,
    st_mobile_attributes_t** p_attributes_array
);


/// @brief 属性顺序标号
typedef enum{
    ST_ATTR_AGE = 0,                ///< 年龄 (0-100)
    ST_ATTR_GENDER_MALE = 1,        ///< 性别为男性的置信度(0-1)
    ST_ATTR_ATTRACTIVE = 2,         ///< 魅力0-100
    ST_ATTR_EYEGLASS = 3,           ///< 眼镜置信度(0-1)
    ST_ATTR_SUNGLASS = 4,           ///< 太阳镜置信度(0-1)
    ST_ATTR_SMILE = 5,              ///< 微笑置信度(0-1)
    ST_ATTR_MASK = 6,               ///< 面具置信度(0-1)
    ST_ATTR_EYE_OPEN = 7,           ///< 眼睛睁开置信度(0-1)
    ST_ATTR_MOUTH_OPEN = 8,         ///< 嘴巴张开置信度(0-1)
    ST_ATTR_BEARD = 9,              ///< 有胡子置信度(0-1)
    ST_ATTR_R_A = 10,       		///< 置信度(0-1)
    ST_ATTR_R_B = 11,       		///< 置信度(0-1)
    ST_ATTR_R_C = 12,        		///< 置信度(0-1)
    ST_ATTR_EMOTION_ANGRY = 13,     ///< 愤怒置信度(0-1)
    ST_ATTR_EMOTION_CALM = 14,      ///< 平静置信度(0-1)
    ST_ATTR_EMOTION_CONFUSED = 15,  ///< 困惑置信度(0-1)
    ST_ATTR_EMOTION_DISGUST = 16,   ///< 厌恶置信度(0-1)
    ST_ATTR_EMOTION_HAPPY = 17,     ///< 高兴置信度(0-1)
    ST_ATTR_EMOTION_SAD = 18,       ///< 悲伤置信度(0-1)
    ST_ATTR_EMOTION_SCARED = 19,    ///< 惊恐置信度(0-1)
    ST_ATTR_EMOTION_SURPRISED = 20, ///< 诧异置信度(0-1)
    ST_ATTR_EMOTION_SQUINT = 21,    ///< 斜视置信度(0-1)
    ST_ATTR_EMOTION_SCREAM = 22,    ///< 尖叫置信度(0-1)
    ST_ATTR_LENGTH = 32             ///< 属性特征长度
}st_attribute_index;

/// @brief 人脸属性检测, 输出所有特征的置信度
/// @param[in] handle 已初始化的人脸属性检测句柄
/// @param[in] image 用于检测的图像数据,推荐灰度格式
/// @param[in] pixel_format 用于检测的图像数据的像素格式, 支持所有彩色图像格式,推荐ST_PIX_FMT_BGR888,不建议使用ST_PIX_FMT_GRAY8（结果不准确）
/// @param[in] image_width 用于检测的图像的宽度(以像素为单位)
/// @param[in] image_height 用于检测的图像的高度(以像素为单位)
/// @param[in] image_stride 用于检测的图像中每一行的跨度(以像素为单位)
/// @param[in] p_face_array 输入待处理的人脸信息,需要包括关键点信息
/// @param[in] face_count 输入的人脸个数
/// @param[out] p_attribute_array 检测到的人脸属性结果数组,包含face_count* ST_ATTR_LENGTH 个float 数据
/// @return 成功返回ST_OK, 错误则返回错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_face_attribute_detect_ext(
    st_handle_t handle,
    const unsigned char *image,
    st_pixel_format pixel_format,
    int image_width,
    int image_height,
    int image_stride,
    const st_mobile_106_t *p_face_array,
    int face_count,
    float** p_attribute_array
);

/// @brief 人脸属性检测, 输出所有特征的置信度, 只有输入和前一个接口不同
/// @param[in] handle 已初始化的人脸属性检测句柄
/// @param[in] image 用于检测的图像数据,推荐灰度格式
/// @param[in] pixel_format 用于检测的图像数据的像素格式, 支持所有彩色图像格式,推荐ST_PIX_FMT_BGR888,不建议使用ST_PIX_FMT_GRAY8（结果不准确）
/// @param[in] image_width 用于检测的图像的宽度(以像素为单位)
/// @param[in] image_height 用于检测的图像的高度(以像素为单位)
/// @param[in] image_stride 用于检测的图像中每一行的跨度(以像素为单位)
/// @param[in] p_face_array 输入待处理的人脸信息,需要包括关键点信息
/// @param[in] face_count 输入的人脸个数
/// @param[out] p_attribute_array 检测到的人脸属性结果数组,包含face_count* ST_ATTR_LENGTH 个float 数据
/// @return 成功返回ST_OK, 错误则返回错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_face_attribute_detect_ext2(
st_handle_t handle,
const unsigned char *image,
st_pixel_format pixel_format,
int image_width,
int image_height,
int image_stride,
const st_mobile_face_t *p_face_array,
int face_count,
float** p_attribute_array
);

#endif // INCLUDE_STMOBILE_ST_MOBILE_FACE_ATTRIBUTE_H_

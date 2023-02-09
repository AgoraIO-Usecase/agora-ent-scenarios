/**
*@file st_mobile_animal.h

* 提供动物脸的检测功能，支持预览、视频、图片模式，能够跟踪猫脸和狗脸，获取脸部特征点，并提供跟踪结果的旋转、缩放和镜像变换

*@attention
*该文件中的API不保证线程安全.多线程调用时,需要确保安全调用.
* 例如在 detect/reset 没有执行完就执行 destroy 可能造成crash;
* 在 detect 执行过程中调用 reset 函数可能会造成crash.

* 一般调用步骤：创建句柄->加载模型->跟踪检测->销毁句柄
*
* st_mobile_tracker_animal_face_create

* st_mobile_tracker_animal_face_add_sub_model

* st_mobile_tracker_animal_face_track

* st_mobile_tracker_animal_face_destroy

**/
#ifndef INCLUDE_STMOBILE_ST_MOBILE_ANIMAL_H_
#define INCLUDE_STMOBILE_ST_MOBILE_ANIMAL_H_

#include "st_mobile_common.h"


// @brief 动物类型
typedef enum {
	ST_MOBILE_ANIMAL_CAT_FACE = 0,          ///< 猫脸
	ST_MOBILE_ANIMAL_DOG_FACE= 10,         ///< 狗脸
} st_mobile_animal_type;

/// @brief 动物面部检测结果
typedef struct st_mobile_animal_face_t {
	int id;                 ///<  每个检测到的脸拥有唯一的ID.跟踪丢失以后重新被检测到,会有一个新的ID
	st_rect_t rect;         ///< 代表面部的矩形区域
	float score;            ///< 置信度
	st_pointf_t *p_key_points;  ///< 关键点
	int key_points_count;       ///< 关键点个数
	float ear_score[2];     /// 耳朵关键点   只有狗脸支持
	float yaw;              ///< 水平转角,真实度量的左负右正
	float pitch;            ///< 俯仰角,真实度量的上负下正
	float roll;             ///< 旋转角,真实度量的左负右正
	st_mobile_animal_type animal_type;  ///< 动物类型
} st_mobile_animal_face_t, *p_st_mobile_animal_face_t;

/// @brief 创建实时动物脸关键点跟踪句柄
/// @param[in] model_path 模型文件的绝对路径或相对路径,例如models/cat.model 可以是null, 为NULL时需要调用st_mobile_animal_face_add_sub_model添加模型
/// @param[in] config 配置选项 预览使用ST_MOBILE_DETECT_MODE_VIDEO, 离线视频处理使用ST_MOBILE_TRACKING_SINGLE_THREAD， 图片使用ST_MOBILE_DETECT_MODE_IMAGE
/// @param[out] handle 动物脸跟踪句柄,失败返回NULL
/// @return 成功返回ST_OK, 失败返回其他错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_tracker_animal_face_create(
	const char *model_path,
	unsigned int config,
	st_handle_t *handle
);

/// @brief 创建实时动物脸关键点跟踪句柄
/// @param[in] buffer 模型缓存起始地址,为NULL时需要调用st_mobile_animal_face_add_sub_model添加需要的模型
/// @param[in] buffer_size 模型缓存大小
/// @param[in] config 配置选项 预览使用ST_MOBILE_DETECT_MODE_VIDEO, 离线视频处理使用ST_MOBILE_TRACKING_SINGLE_THREAD， 图片使用ST_MOBILE_DETECT_MODE_IMAGE
/// @param[out] handle 动物脸跟踪句柄,失败返回NULL
/// @return 成功返回ST_OK, 失败返回其他错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_tracker_animal_face_create_from_buffer(
    const unsigned char* buffer,
    unsigned int buffer_size,
    unsigned int config,
    st_handle_t *handle
);

/// @brief 添加子模型. Android建议使用st_mobile_animal_face_add_sub_model_from_buffer
/// @param[in] handle 动物脸跟踪句柄
/// @param[in] model_path 模型文件的路径. 后添加的会覆盖之前添加的同类子模型。加载模型耗时较长, 建议在初始化创建句柄时就加载模型
ST_SDK_API st_result_t
st_mobile_tracker_animal_face_add_sub_model(
    st_handle_t handle,
    const char *model_path
);

/// @brief 添加子模型.
/// @param[in] handle 动物脸跟踪句柄
/// @param[in] buffer 模型缓存起始地址
/// @param[in] buffer_size 模型缓存大小
ST_SDK_API st_result_t
st_mobile_tracker_animal_face_add_sub_model_from_buffer(
    st_handle_t handle,
    const unsigned char* buffer,
    unsigned int buffer_size
);
/// @brief 动物脸检测模型类型
typedef enum {
	ST_MOBILE_ANIMAL_MODEL_CAT_FACE = 0,          ///< 猫脸
	ST_MOBILE_ANIMAL_MODEL_DOG_FACE= 10,         ///< 狗脸
} st_mobile_animal_model_type;

/// @brief 删除子模型.
/// @param[in] handle 动物脸跟踪句柄
/// @param[in] model_type 删除对应的模型,可用来减少内存
/// @return 成功返回ST_OK， 失败返回其他错误码
ST_SDK_API
st_result_t st_mobile_tracker_animal_face_remove_model_by_config(
	st_handle_t handle,
	st_mobile_animal_model_type model_type
	);

/// @brief 动物脸检测参数类型
typedef enum {
	/// 设置检测到的最大猫脸数目N,持续track已检测到的N个猫脸直到猫脸数小于N再继续做detect.默认32
	ST_MOBILE_PARAM_CAT_LIMIT = 1,
	/// 设置tracker每多少帧进行一次猫脸detect.
	ST_MOBILE_PARAM_CAT_DETECT_INTERVAL_LIMIT = 2,
	/// 设置猫脸跟踪的阈值
	ST_MOBILE_PARAM_CAT_THRESHOLD = 3,

	/// 设置检测到的最大狗脸数目N,持续track已检测到的N个狗脸直到狗脸数小于N再继续做detect.默认32
	ST_MOBILE_PARAM_DOG_LIMIT = 101,
	/// 设置tracker每多少帧进行一次狗脸detect.
	ST_MOBILE_PARAM_DOG_DETECT_INTERVAL_LIMIT = 102,
	/// 设置狗脸跟踪的阈值
	ST_MOBILE_PARAM_DOG_THRESHOLD = 103,

	/// 设置预处理图像的最长边，默认为原图大小。 值越大，耗时越长，检测到的数目会多一些
	ST_MOBILE_PARAM_ANIMAL_PREPROCESS_MAX_SIZE = 500
} st_animal_face_param_type;

/// @brief 设置参数
/// @param[in] handle 已初始化的动物脸跟踪句柄
/// @param[in] type 参数关键字,例如ST_MOBILE_CAT_LIMIT
/// @param[in] value 参数取值
/// @return 成功返回ST_OK,错误则返回错误码,错误码定义在st_mobile_common.h 中,如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_tracker_animal_face_setparam(
	st_handle_t handle,
	st_animal_face_param_type type,
	float value
);


/// @brief 重置实时动物脸关键点跟踪,清空track造成的缓存,当切换分辨率、切换前后摄像头、切换视频、两帧图像差别较大时建议调用reset
/// @param [in] handle 已初始化的实时目标动物脸关键点跟踪句柄
/// @return 成功返回ST_OK, 失败返回其他错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_tracker_animal_face_reset(
	st_handle_t handle
);

/// @brief 检测类型定义
#define ST_MOBILE_CAT_DETECT		0x00000001  ///< 猫脸检测
#define ST_MOBILE_DOG_DETECT        0x00000010  ///< 狗脸检测

/// @brief 对连续视频帧进行实时快速动物脸关键点跟踪
/// @param[in] handle 已初始化的实时动物脸跟踪句柄
/// @param[in] image 用于检测的图像数据
/// @param[in] pixel_format 用于检测的图像数据的像素格式. 推荐使用GRAY8、NV12、NV21、YUV420P
/// @param[in] image_width 用于检测的图像的宽度(以像素为单位)
/// @param[in] image_height 用于检测的图像的高度(以像素为单位)
/// @param[in] image_stride 用于检测的图像的跨度(以像素为单位),即每行的字节数；目前仅支持字节对齐的padding,不支持roi
/// @param[in] orientation 视频中动物脸的方向
/// @param[in] detect_config 需要检测的类型
/// @param[out] p_faces_array 检测到的动物脸信息数组,api负责管理内存,会覆盖上一次调用获取到的数据
/// @param[out] p_faces_count 检测到的动物脸数量
/// @return 成功返回ST_OK,失败返回其他错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_tracker_animal_face_track(
	st_handle_t handle,
	const unsigned char *image,
	st_pixel_format pixel_format,
	int image_width,
	int image_height,
	int image_stride,
	st_rotate_type orientation,
	unsigned int detect_config,
	st_mobile_animal_face_t **p_faces_array,
	int *p_faces_count
);

/// @brief 动物脸跟踪(输入为多平面图像)
/// @param[in] handle 已初始化的人体行为句柄
/// @param[in] image 用于检测的图像数据 只支持yuv，nv12,nv21格式
/// @param[in] orientation 图像中人脸的方向
/// @param[in] detect_config 需要检测的人体行为,例如ST_MOBILE_EYE_BLINK | ST_MOBILE_MOUTH_AH | ST_MOBILE_HAND_LOVE | ST_MOBILE_SEG_BACKGROUND
/// @param[out] p_animal_face 检测到的人体行为,由用户分配内存. 会覆盖上一次的检测结果.
/// @return 成功返回ST_OK,失败返回其他错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_tracker_animal_face_track_from_multi_plane_image(
    st_handle_t handle,
    const st_multiplane_image_t* image,
    st_rotate_type orientation,
	unsigned int detect_config,
	st_mobile_animal_face_t **p_faces_array,
	int *p_faces_count
	);

/// @brief 销毁已初始化的动物脸跟踪句柄
/// @param[in] handle 已初始化的动物脸跟踪句柄
ST_SDK_API void
st_mobile_tracker_animal_face_destroy(
	st_handle_t handle
);

/// @brief 镜像animal检测结果.
/// @param[in] image_width 检测animal_face的图像的宽度(以像素为单位)
/// @param[in,out] p_faces_array 需要镜像的animal_face检测结果
ST_SDK_API void
st_mobile_animal_face_mirror(
	int image_width,
	st_mobile_animal_face_t *p_faces_array,
	int p_faces_count
);

/// @brief 旋转animal_face检测结果.
/// @param[in] image_width 检测animal_face的图像的宽度(以像素为单位)
/// @param[in] image_height 检测animal_face的图像的宽度(以像素为单位)
/// @param[in] orientation 顺时针旋转的角度
/// @param[in,out] p_faces_array 需要旋转的animal_face检测结果
ST_SDK_API void
st_mobile_animal_face_rotate(
	int image_width,
	int image_height,
	st_rotate_type orientation,
	st_mobile_animal_face_t *p_faces_array,
	int faces_count
);
/// @brief 放大/缩小animal_face检测结果.
/// @param[in] scale 缩放的尺度
/// @param[in,out] p_faces_array 需要缩放的animal_face检测结果
ST_SDK_API
void st_mobile_animal_face_resize(
	float scale,
	st_mobile_animal_face_t *p_faces_array,
	int faces_count
);

/// @brief 拷贝animal_face检测结果.
/// @param[in] p_animal_face_src 需要拷贝的animal_face检测结果
/// @param[out] p_animal_face_dst 需要拷贝的animal_face检测结果.注意初始化成员,如果指针非空, 默认是已被分配过内存, 不会重新分配内存
ST_SDK_API
void st_mobile_animal_face_copy(
    const st_mobile_animal_face_t * p_animal_face_src,
	int src_cnt,
    st_mobile_animal_face_t * p_animal_face_dst,
	int dst_cnt
);

/// @brief 删除animal_face 结果, 只能删除st_mobile_animal_face_copy的输出结果
/// @param[in] p_animal_face 需要删除的animal_face检测结果
ST_SDK_API
void st_mobile_animal_face_delete(
    st_mobile_animal_face_t * p_animal_face,
	int animal_face_cnt
);

#endif // INCLUDE_STMOBILE_ST_MOBILE_ANIMAL_H_

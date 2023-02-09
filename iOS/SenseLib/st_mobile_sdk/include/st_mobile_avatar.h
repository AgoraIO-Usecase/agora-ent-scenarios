#ifndef INCLUDE_STMOBILE_ST_MOBILE_AVATAR_H_
#define INCLUDE_STMOBILE_ST_MOBILE_AVATAR_H_

#include "st_mobile_common.h"


/// @brief 创建Avatar句柄，非线程安全
/// @param[out] p_handle 待创建的Avatar句柄
/// @param[in] p_model_file_path Avatar Core Model的文件路径
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_avatar_create(
    st_handle_t *p_handle,
    const char* p_model_file_path
);

/// @brief 创建Avatar句柄，非线程安全
/// @param[out] p_handle 待创建的Avatar句柄
/// @param[in] p_buffer 已加载到内存中的Avatar Core Model buffer
/// @param[in] buffer_len p_buffer的字节个数
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_avatar_create_from_buffer(
    st_handle_t *p_handle,
    const char* p_buffer,
    int buffer_len
);

/// @brief 销毁Avatar句柄，非线程安全
/// @param[in] handle 待销毁的Avatar句柄
ST_SDK_API void
st_mobile_avatar_destroy(
	st_handle_t handle
);

/// @brief 获取Avatar需要的检测配置，非线程安全
/// @param[in] handle Avatar句柄
/// @return 当前Avatar需要的检测参数，需要传给检测模块获取检测结果
ST_SDK_API unsigned long long
st_mobile_avatar_get_detect_config(
    st_handle_t handle
);

/// @brief 获取根据传入关键点拟合的Avatar表情系数结果，非线程安全
/// @param[in] handle 已初始化的Avatar句柄
/// @param[in] width  预览图像宽度
/// @param[in] height 预览图像高度
/// @param[in] rotate 预览图像中将人脸转正需要的旋转角度
/// @param[in] p_face 当前帧人脸关键点检测结果。目前SDK只支持对一个人脸获取表情系数
/// @param[out] p_expression_array 待写入的参数数组指针，数组应由调用方预先分配，数组大小应该大于或等于ST_AVATAR_EXPRESSION_NUM，否则将产生越界异常
/// @return 成功返回ST_OK,失败返回其他错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_avatar_get_expression(
    st_handle_t handle,
    int width, int height,
    st_rotate_type rotate,
    const st_mobile_face_t* p_face,
    float* p_expression_array
);


#endif // INCLUDE_STMOBILE_ST_MOBILE_AVATAR_H_

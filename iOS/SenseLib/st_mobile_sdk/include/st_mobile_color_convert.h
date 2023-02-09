/**
 * @file st_mobile_color_convert.h

 * 基于GPU（OpenGL）的YUV/RGB颜色转换接口， 支持rgba图像和nv21、nv12以及yuv图像之间的转换
 *@attention
 * 本头文件中的所有接口都需要在GL Context中调用， API不保证线程安全.多线程调用时,需要确保安全调用

 * 一般调用步骤：创建句柄->准备输入、输出资源->调用转换接口->销毁句柄

 * st_handle_t handle = NULL;

 * st_mobile_color_convert_create(&handle);

 * st_mobile_nv12_buffer_to_rgba_tex(handle, 1280, 720, ST_CLOCKWISE_ROTATE_0, false, p_buffer, tex_id);

 * st_mobile_color_convert_destroy(handle);

 * handle = NULL;

 * */


#ifndef INCLUDE_STMOBILE_ST_MOBILE_COLOR_CONVERT_H_
#define INCLUDE_STMOBILE_ST_MOBILE_COLOR_CONVERT_H_
#include "st_mobile_common.h"

/// @brief 创建颜色转换对应的句柄
/// @param[out] p_handle 句柄指针，存储内部分配的转换句柄
/// @return 成功返回ST_OK, 错误则返回错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
/// @attention 需要在OpenGL Context中调用
ST_SDK_API st_result_t
st_mobile_color_convert_create(st_handle_t* p_handle);

/// @brief 销毁颜色转换对应的句柄，需要在OpenGL Context中调用
/// @param[in] handle 颜色转换句柄
/// @return 成功返回ST_OK, 错误则返回错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
/// @attention 需要在OpenGL Context中调用
ST_SDK_API st_result_t
st_mobile_color_convert_destroy(st_handle_t handle);

/// @brief 设置color convert输入buffer/texture的尺寸，提前调用该接口可以提升后续color convert接口的时间。
/// @param[in] handle 颜色转换句柄
/// @param[in] width 待转换图像的宽度
/// @param[in] height 待转换图像的高度
/// @return 成功返回ST_OK, 错误则返回错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
/// @attention 需要在OpenGL Context中调用
ST_SDK_API st_result_t
st_mobile_color_convert_set_size(st_handle_t handle, int width, int height);

/// @brief 对输入的nv21格式的buffer转换成RGBA格式，并输出到texId对应的OpenGL纹理中，需要在OpenGL Context中调用
/// @param[in] handle 已初始化的颜色格式转换句柄
/// @param[in] width 待转换图像的宽度
/// @param[in] height 待转换图像的高度
/// @param[in] orientation 图像朝向，根据传入图像旋转角度，将图像转正。如果旋转角度为90或270，tex_out的宽高需要与buffer的宽高对调。
/// @param[in] horizontal_mirror 是否需要水平镜像，true - 水平镜像，false - 垂直镜像
/// @param[in] p_buffer NV21格式的图像buffer，需要预先分配空间（字节数：width * height * 3 / 2)
/// @param[out] tex_out RGBA格式输出纹理，需要在调用层预先创建
/// @return 成功返回ST_OK, 错误则返回错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
/// @attention 需要在OpenGL Context中调用
ST_SDK_API st_result_t
st_mobile_nv21_buffer_to_rgba_tex(st_handle_t handle, int width, int height,
                                  st_rotate_type orientation, bool horizontal_mirror,
                                  const unsigned char *p_buffer, int tex_out);

/// @brief 对输入的nv12格式的buffer转换成RGBA格式，并输出到texId对应的OpenGL纹理中，需要在OpenGL Context中调用
/// @param[in] handle 已初始化的颜色格式转换句柄
/// @param[in] width 待转换图像的宽度
/// @param[in] height 待转换图像的高度
/// @param[in] orientation 图像朝向，根据传入图像旋转角度，将图像转正。如果旋转角度为90或270，tex_out的宽高需要与buffer的宽高对调。
/// @param[in] horizontal_mirror 是否需要水平镜像，true - 水平镜像，false - 垂直镜像
/// @param[in] p_buffer NV12格式的图像buffer
/// @param[out] tex_out RGBA格式输出纹理，需要在调用层预先创建
/// @return 成功返回ST_OK, 错误则返回错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
/// @attention 需要在OpenGL Context中调用
ST_SDK_API st_result_t
st_mobile_nv12_buffer_to_rgba_tex(st_handle_t handle, int width, int height,
                                  st_rotate_type orientation, bool horizontal_mirror,
                                  const unsigned char *p_buffer, int tex_out);

/// @brief 对输入的yuv buffer转换成RGBA texture，需要在OpenGL Context中调用
/// @param[in] handle 已初始化的颜色格式转换句柄
/// @param[in] p_image 输入yuv图像数据，目前仅支持NV21、NV12的单平面和双平面图像数据
/// @param[in] orientation 图像朝向，根据传入图像旋转角度，将图像转正。如果旋转角度为90或270，tex_out的宽高需要与buffer的宽高对调。
/// @param[in] horizontal_mirror 是否需要水平镜像，true - 水平镜像，false - 垂直镜像
/// @param[out] dst_tex RGBA格式输出纹理，需要在调用层预先创建，如果orientation是90或270，则纹理宽高跟输入buffer的宽高相反
/// @return 成功返回ST_OK, 错误则返回错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
/// @attention 需要在OpenGL Context中调用
ST_SDK_API st_result_t
st_mobile_convert_yuv_buffer_2_rgba_tex(st_handle_t handle, const st_multiplane_image_t *p_image,
                                        st_rotate_type orientation, bool horizontal_mirror, int dst_tex);

/// @brief 该接口将废弃，请调用st_mobile_rgba_tex_to_nv21_buffer接口
///        对输入的RGBA格式texture转换成nv21格式的buffer，并输出到pYUVBuffer中，需要在OpenGL Context中调用
/// @param[in] handle 已初始化的颜色格式转换句柄
/// @param[in] tex_in 输入纹理
/// @param[in] width 待转换图像的宽度
/// @param[in] height 待转换图像的高度
/// @param[out] p_buffer NV21格式的图像buffer，需要预先分配空间（字节数：width * height * 3 / 2)
/// @return 成功返回ST_OK, 错误则返回错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
/// @attention 需要在OpenGL Context中调用
ST_SDK_API st_result_t
st_mobile_rgba_tex_to_nv21_tex(st_handle_t handle, int tex_in,
                               int width, int height, unsigned char *p_buffer);

/// @brief 对输入的RGBA格式texture转换成nv21格式的buffer，并输出到pYUVBuffer中，需要在OpenGL Context中调用
/// @param[in] handle 已初始化的颜色格式转换句柄
/// @param[in] tex_in 输入纹理
/// @param[in] width 待转换图像的宽度
/// @param[in] height 待转换图像的高度
/// @param[out] p_buffer NV21格式的图像buffer，需要预先分配空间（字节数：width * height * 3 / 2)
/// @return 成功返回ST_OK, 错误则返回错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
/// @attention 需要在OpenGL Context中调用
ST_SDK_API st_result_t
st_mobile_rgba_tex_to_nv21_buffer(st_handle_t handle, int tex_in, int width, int height, unsigned char* p_buffer);

/// @brief 对输入的RGBA格式texture转换成nv12格式的buffer，并输出到pYUVBuffer中，需要在OpenGL Context中调用
/// @param[in] handle 已初始化的颜色格式转换句柄
/// @param[in] tex_in 输入纹理
/// @param[in] width 待转换图像的宽度
/// @param[in] height 待转换图像的高度
/// @param[out] p_buffer NV12格式的图像buffer，需要预先分配空间（字节数：width * height * 3 / 2)
/// @return 成功返回ST_OK, 错误则返回错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
/// @attention 需要在OpenGL Context中调用
ST_SDK_API st_result_t st_mobile_rgba_tex_to_nv12_buffer(st_handle_t handle, int tex_in, int width, int height, unsigned char* p_buffer);

/// @brief 对输入的RGBA格式texture转换成yuv格式的buffer，并输出到p_image中，需要在OpenGL Context中调用
/// @param[in] handle 已初始化的颜色格式转换句柄
/// @param[in] src_tex 输入纹理
/// @param[out] p_image 输出图像buffer，目前只支持NV21与NV12两种格式，单平面或者双平面数据
/// @return 成功返回ST_OK, 错误则返回错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
/// @attention 需要在OpenGL Context中调用
ST_SDK_API st_result_t
st_mobile_convert_rgba_tex_2_yuv_buffer(st_handle_t handle, int src_tex, st_multiplane_image_t *p_image);

/// @brief 对输入的RGBA格式texture转换成GRAY8格式的buffer，并输出到p_image中，需要在OpenGL Context中调用
/// @param[in] handle 已初始化的颜色格式转换句柄
/// @param[in] src_tex 输入纹理
/// @param[out] p_image 输出图像buffer，需要保证plane[0]有效
/// @return 成功返回ST_OK, 错误则返回错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
/// @attention 需要在OpenGL Context中调用
ST_SDK_API st_result_t
st_mobile_convert_rgba_tex_2_gray8_buffer(st_handle_t handle, int src_tex, st_multiplane_image_t *p_image);

#endif //INCLUDE_STMOBILE_ST_MOBILE_COLOR_CONVERT_H_

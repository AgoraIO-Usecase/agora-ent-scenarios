/**
*@file st_mobile_license.h
* 提供授权文件激活功能
*
* 一般调用步骤：验证现有激活码->如果验证失败，生成新的激活码

* if(ST_OK != st_mobile_check_activecode(...)) {

*     st_mobile_generate_activecode(...)

*     Save New Active Code to disk

* }

**/
#ifndef INCLUDE_STMOBILE_ST_MOBILE_LICENSE_H_
#define INCLUDE_STMOBILE_ST_MOBILE_LICENSE_H_
#include "st_mobile_common.h"


/// @brief 根据授权文件生成激活码, 在使用新的license文件时使用. Android建议使用st_mobile_generate_activecode_from_buffer
/// @param[in] license_path license文件路径
/// @param[out] activation_code 返回当前设备的激活码,由用户分配内存, 通常建议分配1024字节，如果使用动态在线鉴权，需要分配10240字节
/// @param[in,out] activation_code_len 输入为active_code的内存大小, 返回当前设备的激活码字节长度（包含激活码字符串结束符'\0'）
/// @return 正常返回ST_OK,否则返回错误类型
ST_SDK_API st_result_t
st_mobile_generate_activecode(
    const char* license_path,
    char* activation_code,
    int* activation_code_len
);

/// @brief 检查激活码, 必须在所有接口之前调用. Android建议使用st_mobile_check_activecode_from_buffer
/// @param[in] license_path license文件路径
/// @param[in] activation_code 当前设备的激活码
/// @param[in] activation_code_len 激活码的长度
/// @return 正常返回ST_OK,否则返回错误类型
ST_SDK_API st_result_t
st_mobile_check_activecode(
    const char* license_path,
    const char* activation_code,
    int activation_code_len
);
/// @brief 检查进程中是否已包含相应的license 如果希望复用在其他动态库或静态库中加载的license, 必须使用这个接口
/// @param[in] product_name 已在其他动态库或静态库中加载的license产品名称
ST_SDK_API bool
st_mobile_check_has_license(
const char* product_name
);

/// @brief 根据授权文件缓存生成激活码, 在使用新的license文件时调用
/// @param[in] license_buf license文件缓存地址
/// @param[in] license_size license文件缓存大小
/// @param[out] activation_code 返回当前设备的激活码, 由用户分配内存; 通常建议分配1024字节，如果使用动态在线鉴权，需要分配10240字节
/// @param[in, out] activation_code_len 输入为activation_code分配的内存大小, 返回生成的设备激活码的字节长度（包含激活码字符串结束符'\0'）
/// @return 正常返回ST_OK, 否则返回错误类型
ST_SDK_API st_result_t
st_mobile_generate_activecode_from_buffer(
    const char* license_buf,
    int license_size,
    char* activation_code,
    int* activation_code_len
);

/// @brief 检查激活码, 必须在所有接口之前调用
/// @param[in] license_buf license文件缓存
/// @param[in] license_size license文件缓存大小
/// @param[in] activation_code 当前设备的激活码
/// @param[in] activation_code_len 激活码的长度
/// @return 正常返回ST_OK, 否则返回错误类型
ST_SDK_API st_result_t
st_mobile_check_activecode_from_buffer(
    const char* license_buf,
    int license_size,
    const char* activation_code,
    int activation_code_len
);

/// @brief 根据授权文件在线生成激活码, 需要使用在线license和联网
/// @param[in] license_path license文件路径
/// @param[out] activation_code 返回当前设备的激活码,由用户分配内存, 通常建议分配1024字节，如果使用动态在线鉴权，需要分配10240字节
/// @param[in,out] activation_code_len 输入为active_code的内存大小, 返回当前设备的激活码字节长度（包含激活码字符串结束符'\0'）
/// @return 正常返回ST_OK,否则返回错误类型
ST_SDK_API st_result_t
st_mobile_generate_activecode_online(
			      const char* license_path,
			      char* activation_code,
			      int* activation_code_len
			      );


/// @brief 根据授权文件buffer在线生成激活码, 需要使用在线license和联网
/// @param[in] license_buf license文件缓存地址
/// @param[in] license_size license文件缓存大小
/// @param[out] activation_code 返回当前设备的激活码, 由用户分配内存; 通常建议分配1024字节，如果使用动态在线鉴权，需要分配10240字节
/// @param[in, out] activation_code_len 输入为activation_code分配的内存大小, 返回生成的设备激活码的字节长度（包含激活码字符串结束符'\0'）
/// @return 正常返回ST_OK, 否则返回错误类型
ST_SDK_API st_result_t
st_mobile_generate_activecode_from_buffer_online(
					  const char* license_buf,
					  int license_size,
					  char* activation_code,
					  int* activation_code_len
					  );

/// @brief 获得获取动态在线授权的激活码中使用期限(UTC) Android 建议使用st_mobile_get_expiredtime_from_activate_code_from_buffer
/// @param[in] license_path 动态license文件dynamic_license.lic 路径
/// @param[in] activate_path 激活码文件 activate_code.lic 的路径
/// @param[out]	end activation_code 过期时间(UTC)
/// @return 正常返回ST_OK 否则返回错误的类型
ST_SDK_API st_result_t
st_mobile_get_expiredtime_from_activate_code(
					const char* license_path,
					const char* activate_path,
					long long* end
					);

/// @brief 获得获取动态在线授权的激活码中使用期限(UTC)
/// @param[in] license_buf dynamic_license 文件缓存地址
/// @param[in] license_size dynamic_license 文件缓存大小
/// @param[in] activation_code 当前设备激活码
/// @param[in] activation_code_len 当前设备激活码长度
/// @param[out]	end activation_code 过期时间(UTC)
/// @return 正常返回ST_OK 否则返回错误的类型
ST_SDK_API st_result_t
st_mobile_get_expiredtime_from_activate_code_from_buffer(
					const char* license_buf,
					int license_size,
					const char* activation_code,
					int activation_code_len,
					long long* end
);

/// @brief 获得license 时间限制 Android 建议使用st_mobile_preview_get_license_expiredtime_from_buffer
/// @param[in] license_path license_online.lic 路径
/// @param[in] activate_path 激活码文件 activate_code.lic 的路径
/// @param[out] start 开始时间 int类型（yyyymmddHHMM）
/// @param[out] end 结束时间 int类型（yyyymmddHHMM）
/// @return 正常返回ST_OK 否则返回错误的类型
ST_SDK_API st_result_t
st_mobile_get_license_expiredtime(
					const char* license_path,
					const char* activate_path,
					long long* start,
					long long* end
					);

/// @brief 获得license 时间限制
/// @param[in] license_path license文件缓存地址
/// @param[in] license_size license文件缓存大小
/// @param[in] activation_code 当前设备激活码
/// @param[in] activation_code_len 当前设备激活码的长度
/// @param[out] start 开始时间 int类型（yyyymmddgHHMM）
/// @param[out] end 结束时间 int类型（yyyymmddHHMM）
/// @return 正常返回ST_OK 否则返回错误的类型
ST_SDK_API st_result_t
st_mobile_get_license_expiredtime_from_buffer(
					const char* license_buf,
					int license_size,
					const char* activation_code,
					int activation_code_len,
					long long* start,
					long long* end
);

#endif // INCLUDE_STMOBILE_ST_MOBILE_LICENSE_H_

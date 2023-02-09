/**
 * @file st_mobile_effect.h
 *
 * @brief
 * 特效接口是多个功能的集合，主要包括美颜，美妆，滤镜，2D/3D贴纸, tryon等功能，其相关接口定义在st_mobile_effect.h文件中
 * 目前该接口支持RGBA纹理格式或Buffer数据输入, 具体细节请参考接口文档或示例代码
 * 一般调用步骤：创建句柄->配置参数->开始渲染->销毁句柄
 *
 * 调用示例：
 * st_result_t ret = ST_OK;
 * st_handle_t handle = NULL;
 * ret = st_mobile_effect_create_handle(0x0, &handle);
 *
 * st_mobile_effect_set_beauty_strength(handle, EFFECT_BEAUTY_RESHAPE_NARROW_FACE, 1.0f);
 * st_mobile_effect_add_package(handle, "../makeup_lip.zip", nullptr);
 *
 * st_effect_texture_t effect_in_tex = { in_tex, width, height, ST_PIX_FMT_RGBA8888 };
 * st_effect_texture_t effect_out_tex = { out_tex, width, height, ST_PIX_FMT_RGBA8888 };
 *
 * st_effect_render_in_param_t in_param = {};
 * in_param.p_human = &human_action;
 * in_param.p_tex = &effect_in_tex;
 *
 * st_effect_render_out_param_t out_param = {};
 * out_param.p_human = &another_human_action;
 * out_param.p_tex = &effect_out_tex;
 *
 * st_mobile_effect_render(handle, &in_param, &out_param);
 *
 * st_mobile_effect_destroy_handle(handle);
 * handle = NULL;
 * */

#ifndef _ST_MOBILE_EFFECT_H_
#define _ST_MOBILE_EFFECT_H_

#include "st_mobile_common.h"
#include "st_mobile_human_action.h"
#include "st_mobile_animal.h"
#include <stdint.h>

#define EFFECT_MAX_NAME_LEN 256

/// @brief Effect handle配置模式
typedef enum {
    EFFECT_CONFIG_NONE              = 0x0,  ///< 默认配置
    EFFECT_CONFIG_IMAGE_MODE        = 0x2,  ///< 图片模式, 添加素材后立即生效
} st_effect_handle_config_t;

/// @brief 内存buffer数据块定义
typedef struct {
    char* data_ptr;     ///< 数据的起始地址
    uint32_t data_len;  ///< 数据的长度
} st_effect_buffer_t;

/// @brief 创建特效句柄
/// @param[in] config 素材生效模式
/// @param[out] handle 特效句柄指针
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_create_handle(uint32_t config, st_handle_t* p_handle);

/// @brief 销毁特效句柄, 需在OpenGL渲染线程中执行
/// @param[in] handle 已初始化的特效句柄
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_destroy_handle(st_handle_t handle);

/// @brief Effect句柄级别的配置项, 影响整个句柄。需在OpenGL渲染线程中执行
typedef enum {
    EFFECT_PARAM_MIN_FRAME_INTERVAL,        ///< 贴纸前后两个序列帧切换的最小时间间隔，单位为毫秒。当两个相机帧处理的间隔小于这个值的时候，
                                            ///< 当前显示的贴纸序列帧会继续显示，直到显示的时间大于该设定值贴纸才会切换到下一阵，相机帧不受影响。
    EFFECT_PARAM_MAX_MEMORY_BUDGET_MB,      ///< 设置贴纸素材资源所占用的最大内存（MB），当估算内存超过这个值时，将不能再加载新的素材包

    EFFECT_PARAM_QUATERNION_SMOOTH_FRAME,   ///< 设置相机姿态平滑参数，表示平滑多少帧, 越大延迟越高，抖动越微弱

    EFFECT_PARAM_USE_INPUT_TIMESTAMP,       ///< 设置贴纸是否使用外部时间戳更新

    EFFECT_PARAM_PREFER_MEMORY_CACHE,       ///< 倾向于空间换时间，传0的话，则尽可能清理内部缓存，保持内存最小。目前主要影响3D共享资源

    EFFECT_PARAM_DISABLE_BEAUTY_OVERLAP,    ///< 传入大于0的值，禁用美颜Overlap逻辑（贴纸中的美颜会覆盖前面通过API或者贴纸生效的美颜效果，贴纸成组覆盖，API单个覆盖），默认启用Overlap

    EFFECT_PARAM_DISABLE_MODULE_REORDER,    ///< 传入大于0的值，禁用对于v3.1之前的素材包重新排序module的渲染顺序，该选项只会影响设置之后添加的素材。重新排序是为了在与美妆、风格素材包叠加时达到最佳效果，默认启用ReOrder

    EFFECT_PARAM_3D_POSE_SOLUTION,          ///< 3DPose计算方案，传入0使用106旧模型方案，传1使用基于282模型优化的Pose方案，默认值为1
} st_effect_param_t;

/// @brief 设置特效的参数
/// @param[in] handle 已初始化的贴纸句柄
/// @param[in] param 参数类型
/// @param[in] val 参数数值，具体范围参考参数类型说明
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_set_param(st_handle_t handle, st_effect_param_t param, float val);

/// @brief 获取特效的参数
/// @param[in] handle 已初始化的贴纸句柄
/// @param[in] param 参数类型
/// @param[out] val 参数数值
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_get_param(st_handle_t handle, st_effect_param_t param, float* val);

/// @brief 纹理信息
typedef struct {
    int id;                 ///< OpenGL纹理id
    int width;              ///< 纹理的宽
    int height;             ///< 纹理的高
    st_pixel_format format; ///< 像素格式, 目前仅支持RGBA
} st_effect_texture_t;

/// @brief 自定义参数
typedef struct {
    st_quaternion_t camera_quat;    ///< 相机姿态的四元数，默认四元数(0, 0, 0, 1)对应的姿态是相机水平向下，头向北。不需要传入该数据时，
                                    ///< st_effect_custom_param_t传入空指针即可，传空时，SDK内部的默认朝向是相机竖直朝向-Z轴，头向上。
    bool front_camera;              ///< 是否为前置相机
    int event;                      ///< 用户定义的事件id
} st_effect_custom_param_t;

/// @brief 自定义参数配置
typedef enum {
    EFFECT_CUSTOM_NONE              = 0X0,  ///< 不需要自定义参数
    EFFECT_CUSTOM_CAMERA_QUATION    = 0x1,  ///< 需要相机姿态信息
    EFFECT_CUSTOM_CAMERA_FACING     = 0x2,  ///< 需要前后相机信息
} st_effect_custom_param_config_t;

/// @brief 获取需要的检测配置选项
/// @param[in] handle 已初始化的特效句柄
/// @param[out] p_detect_config 返回检测配置选项, 每一位分别代表该位对应检测选项, 对应状态详见st_mobile_human_action.h中, 如ST_MOBILE_FACE_DETECT等
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_get_detect_config(st_handle_t handle, uint64_t* p_detect_config);

#define EFFECT_CUSTOM_INPUT_EVENT_SCREEN_TAP            0x10000
#define EFFECT_CUSTOM_INPUT_EVENT_SCREEN_DOUBLE_TAP     0x20000

/// @brief 获取需要的自定义事件选项
/// @param[in] handle 已初始化的特效句柄
/// @param[out] p_custom_event_config 返回自定义事件选项
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_get_custom_event_config(st_handle_t handle, uint64_t* p_custom_event_config);

/// @brief 获取前摄/后摄对应的默认手机姿态四元数，在处理图片、视频或者没有相应的手机姿态的情况下，需要传入默认的camera_quat
/// @param[in] handle 已初始化的特效句柄
/// @param[in] b_front_camera 指定当前获取的默认手机姿态四元数对应前摄像头还是后摄像头
/// @param[out] p_default_quat 返回的默认四元数
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_get_default_camera_quaternion(st_handle_t handle, bool b_front_camera, st_quaternion_t *p_default_quat);

/// @brief 获取目前需要的动物检测类型
/// @param[in] handle 已初始化的特效句柄
/// @param[out] p_detect_config 返回的需要检测的类别，对应状态详见st_mobile_common.h中, 如ST_MOBILE_CAT_DETECT等
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_get_animal_detect_config(st_handle_t handle, uint64_t* p_detect_config);

/// @brief 获取自定义配置
/// @param[in] handle 已初始化的特效句柄
/// @param[out] p_custom_param_config 返回的自定义配置选项, 每一位分别代表该位的检测选项, 对应的状态详见st_effect_custom_param_config_t
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_get_custom_param_config(st_handle_t handle, uint64_t* p_custom_param_config);

/// @brief 渲染输入的图片信息
typedef struct {
    st_image_t image;                           ///< 输入的图片信息
    st_rotate_type rotate;                      ///< 输入图片的旋转信息（相对于输入的texture而言）
    bool b_mirror;                              ///< 输入图片是否水平镜像
} st_effect_in_image_t;

/// @brief 渲染的输入参数
typedef struct {
    st_mobile_human_action_t* p_human;          ///< 人脸检测结果
    st_mobile_animal_face_t* p_animal_face;     ///< 动物脸检测结果
    int animal_face_count;                      ///< 动物脸的数量
    st_rotate_type rotate;                      ///< 人脸朝向
    st_rotate_type front_rotate;                ///< 前景渲染朝向
    bool need_mirror;                           ///< 是否需要镜像
    st_effect_custom_param_t* p_custom_param;   ///< 自定义参数配置
    st_effect_texture_t* p_tex;                 ///< 输入的纹理信息
    st_effect_in_image_t* p_image;              ///< 输入的图片信息
    double time_stamp;                          ///< 输入的时间戳
} st_effect_render_in_param_t;

/// @brief 渲染的输出参数
typedef struct {
    st_effect_texture_t* p_tex;                 ///< 输出的纹理信息
    st_image_t* p_image;                        ///< 输出的图片信息
    st_mobile_human_action_t* p_human;          ///< 经过内部美颜、贴纸影响（反算）后的humanAction结果信息
} st_effect_render_out_param_t;

/// @brief 特效渲染, 必须在OpenGL渲染线程中执行
/// @param[in] handle 已初始化的特效句柄
/// @param[in] p_in_param 输入的渲染信息
/// @param[out] p_out_param 输出的渲染信息
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_render(st_handle_t handle, const st_effect_render_in_param_t* p_in_param, st_effect_render_out_param_t* p_out_param);

/// @brief 美颜类型
typedef enum {
    // 基础美颜 base
    EFFECT_BEAUTY_BASE_WHITTEN                      = 101,  ///< 美白，[0,1.0], 默认值0.30, 0.0不做美白
    EFFECT_BEAUTY_BASE_REDDEN                       = 102,  ///< 红润, [0,1.0], 默认值0.36, 0.0不做红润
    EFFECT_BEAUTY_BASE_FACE_SMOOTH                  = 103,  ///< 磨皮, [0,1.0], 默认值0.74, 0.0不做磨皮
    //

    // 美形 reshape
    EFFECT_BEAUTY_RESHAPE_SHRINK_FACE               = 201,  ///< 瘦脸, [0,1.0], 默认值0.11, 0.0不做瘦脸效果
    EFFECT_BEAUTY_RESHAPE_ENLARGE_EYE               = 202,  ///< 大眼, [0,1.0], 默认值0.13, 0.0不做大眼效果
    EFFECT_BEAUTY_RESHAPE_SHRINK_JAW                = 203,  ///< 小脸, [0,1.0], 默认值0.10, 0.0不做小脸效果
    EFFECT_BEAUTY_RESHAPE_NARROW_FACE               = 204,  ///< 窄脸, [0,1.0], 默认值0.0, 0.0不做窄脸
    EFFECT_BEAUTY_RESHAPE_ROUND_EYE                 = 205,  ///< 圆眼, [0,1.0], 默认值0.0, 0.0不做圆眼

    // 微整形 plastic
    EFFECT_BEAUTY_PLASTIC_THINNER_HEAD              = 301,  ///< 小头, [0, 1.0], 默认值0.0, 0.0不做小头效果
    EFFECT_BEAUTY_PLASTIC_THIN_FACE                 = 302,  ///< 瘦脸型，[0,1.0], 默认值0.0, 0.0不做瘦脸型效果
    EFFECT_BEAUTY_PLASTIC_CHIN_LENGTH               = 303,  ///< 下巴，[-1, 1], 默认值为0.0，[-1, 0]为短下巴，[0, 1]为长下巴
    EFFECT_BEAUTY_PLASTIC_HAIRLINE_HEIGHT           = 304,  ///< 额头，[-1, 1], 默认值为0.0，[-1, 0]为低发际线，[0, 1]为高发际线
    EFFECT_BEAUTY_PLASTIC_APPLE_MUSLE               = 305,  ///< 苹果肌，[0, 1.0]，默认值为0.0，0.0不做苹果肌
    EFFECT_BEAUTY_PLASTIC_NARROW_NOSE               = 306,  ///< 瘦鼻翼，[0, 1.0], 默认值为0.0，0.0不做瘦鼻
    EFFECT_BEAUTY_PLASTIC_NOSE_LENGTH               = 307,  ///< 长鼻，[-1, 1], 默认值为0.0, [-1, 0]为短鼻，[0, 1]为长鼻
    EFFECT_BEAUTY_PLASTIC_PROFILE_RHINOPLASTY       = 308,  ///< 侧脸隆鼻，[0, 1.0]，默认值为0.0，0.0不做侧脸隆鼻效果
    EFFECT_BEAUTY_PLASTIC_MOUTH_SIZE                = 309,  ///< 嘴型，[-1, 1]，默认值为0.0，[-1, 0]为放大嘴巴，[0, 1]为缩小嘴巴
    EFFECT_BEAUTY_PLASTIC_PHILTRUM_LENGTH           = 310,  ///< 缩人中，[-1, 1], 默认值为0.0，[-1, 0]为长人中，[0, 1]为短人中
    EFFECT_BEAUTY_PLASTIC_EYE_DISTANCE              = 311,  ///< 眼距，[-1, 1]，默认值为0.0，[-1, 0]为减小眼距，[0, 1]为增加眼距
    EFFECT_BEAUTY_PLASTIC_EYE_ANGLE                 = 312,  ///< 眼睛角度，[-1, 1]，默认值为0.0，[-1, 0]为左眼逆时针旋转，[0, 1]为左眼顺时针旋转，右眼与左眼相对
    EFFECT_BEAUTY_PLASTIC_OPEN_CANTHUS              = 313,  ///< 开眼角，[0, 1.0]，默认值为0.0， 0.0不做开眼角
    EFFECT_BEAUTY_PLASTIC_BRIGHT_EYE                = 314,  ///< 亮眼，[0, 1.0]，默认值为0.0，0.0不做亮眼
    EFFECT_BEAUTY_PLASTIC_REMOVE_DARK_CIRCLES       = 315,  ///< 祛黑眼圈，[0, 1.0]，默认值为0.0，0.0不做去黑眼圈
    EFFECT_BEAUTY_PLASTIC_REMOVE_NASOLABIAL_FOLDS   = 316,  ///< 祛法令纹，[0, 1.0]，默认值为0.0，0.0不做去法令纹
    EFFECT_BEAUTY_PLASTIC_WHITE_TEETH               = 317,  ///< 白牙，[0, 1.0]，默认值为0.0，0.0不做白牙
    EFFECT_BEAUTY_PLASTIC_SHRINK_CHEEKBONE          = 318,  ///< 瘦颧骨， [0, 1.0], 默认值0.0， 0.0不做瘦颧骨
    EFFECT_BEAUTY_PLASTIC_OPEN_EXTERNAL_CANTHUS     = 319,  ///< 开外眼角比例，[0, 1.0]，默认值为0.0， 0.0不做开外眼角
    EFFECT_BEAUTY_PLASTIC_SHRINK_JAWBONE            = 320,  ///< 瘦下颔，[0, 1.0], 默认值0.0， 0.0不做瘦下颔
    EFFECT_BEAUTY_PLASTIC_SHRINK_ROUND_FACE         = 321,  ///< 圆脸瘦脸，[0, 1.0], 默认值0.0， 0.0不做瘦脸
    EFFECT_BEAUTY_PLASTIC_SHRINK_LONG_FACE          = 322,  ///< 长脸瘦脸，[0, 1.0], 默认值0.0， 0.0不做瘦脸
    EFFECT_BEAUTY_PLASTIC_SHRINK_GODDESS_FACE       = 323,  ///< 女神瘦脸，[0, 1.0], 默认值0.0， 0.0不做瘦脸
    EFFECT_BEAUTY_PLASTIC_SHRINK_NATURAL_FACE       = 324,  ///< 自然瘦脸，[0, 1.0], 默认值0.0， 0.0不做瘦脸
    EFFECT_BEAUTY_PLASTIC_SHRINK_WHOLE_HEAD         = 325,  ///< 整体缩放小头，[0, 1.0], 默认值0.0, 0.0不做整体缩放小头效果

    // 调整 tone
    EFFECT_BEAUTY_TONE_CONTRAST                     = 601,  ///< 对比度, [0,1.0], 默认值0.05, 0.0不做对比度处理
    EFFECT_BEAUTY_TONE_SATURATION                   = 602,  ///< 饱和度, [0,1.0], 默认值0.10, 0.0不做饱和度处理
    EFFECT_BEAUTY_TONE_SHARPEN                      = 603,  ///< 锐化, [0, 1.0], 默认值0.0, 0.0不做锐化
    EFFECT_BEAUTY_TONE_CLEAR                        = 604,  ///< 清晰度, 清晰强度, [0,1.0], 默认值0.0, 0.0不做清晰

    // 美妆 makeup
    EFFECT_BEAUTY_MAKEUP_HAIR_DYE                   = 401,  ///< 染发
    EFFECT_BEAUTY_MAKEUP_LIP                        = 402,  ///< 口红
    EFFECT_BEAUTY_MAKEUP_CHEEK                      = 403,  ///< 腮红
    EFFECT_BEAUTY_MAKEUP_NOSE                       = 404,  ///< 修容
    EFFECT_BEAUTY_MAKEUP_EYE_BROW                   = 405,  ///< 眉毛
    EFFECT_BEAUTY_MAKEUP_EYE_SHADOW                 = 406,  ///< 眼影
    EFFECT_BEAUTY_MAKEUP_EYE_LINE                   = 407,  ///< 眼线
    EFFECT_BEAUTY_MAKEUP_EYE_LASH                   = 408,  ///< 眼睫毛
    EFFECT_BEAUTY_MAKEUP_EYE_BALL                   = 409,  ///< 美瞳
    EFFECT_BEAUTY_MAKEUP_PACKED                     = 410,  ///< 打包的美妆素材，可能包含一到多个单独的美妆模块，与其他单独美妆可以同时存在
    EFFECT_BEAUTY_MAKEUP_EYE_PAINTING                 = 411,  ///< 眼妆

    EFFECT_BEAUTY_FILTER                            = 501,  ///< 滤镜

    // 试妆 tryon                                            ///< 参数通过 st_effect_tryon_info_t 结构体中对应的参数来设置
    EFFECT_BEAUTY_TRYON_HAIR_COLOR                  = 701,  ///< 染发，可设置的参数包括：颜色，强度，明暗度，高光
    EFFECT_BEAUTY_TRYON_LIPSTICK                    = 702,  ///< 口红，可设置的参数包括：颜色，强度，高光(特定材质：水润、闪烁、金属)，质地类型
    EFFECT_BEAUTY_TRYON_LIPLINE                     = 703,  ///< 唇线，可设置的参数包括：颜色，强度，唇线线宽
    EFFECT_BEAUTY_TRYON_BLUSH                       = 704,  ///< 腮红，可设置的参数包括：颜色，强度
    EFFECT_BEAUTY_TRYON_BROW                        = 705,  ///< 眉毛，可设置的参数包括：颜色，强度
    EFFECT_BEAUTY_TRYON_FOUNDATION                  = 706,  ///< 粉底，可设置的参数包括：颜色，强度
    EFFECT_BEAUTY_TRYON_CONTOUR                     = 707,  ///< 修容，可设置的参数包括：强度（整体），区域信息（区域id，颜色，强度）
    EFFECT_BEAUTY_TRYON_EYESHADOW                   = 708,  ///< 眼影，可设置的参数包括：强度（整体），区域信息（区域id，颜色，强度）
    EFFECT_BEAUTY_TRYON_EYELINER                    = 709,  ///< 眼线，可设置的参数包括：强度（整体），区域信息（区域id，颜色，强度）
    EFFECT_BEAUTY_TRYON_EYELASH                     = 710,  ///< 眼睫毛，可设置的参数包括：颜色，强度
    EFFECT_BEAUTY_TRYON_STAMPLINER                  = 711,  ///< 眼印，可设置的参数包括：颜色，强度

    // 3D 微整形
    EFFECT_BEAUTY_3D_MICRO_PLASTIC                  =801,
} st_effect_beauty_type_t;


/// @brief 美颜分组信息
typedef enum {
    EFFECT_BEAUTY_GROUP_BASE,           ///< 基础美颜组
    EFFECT_BEAUTY_GROUP_RESHAPE,        ///< 美型组
    EFFECT_BEAUTY_GROUP_PLASTIC,        ///< 微整形组
    EFFECT_BEAUTY_GROUP_TONE,           ///< 图像微调组
    EFFECT_BEAUTY_GROUP_MAKEUP,         ///< 美妆组
    EFFECT_BEAUTY_GROUP_FILTER,         ///< 滤镜组
} st_effect_beauty_group_t;

/// @brief 美颜信息
typedef struct {
    st_effect_beauty_type_t type;       ///< 美颜类型
    float strength;                     ///< 美颜强度
    char name[EFFECT_MAX_NAME_LEN];     ///< 所属的素材包的名字
    int mode;                           ///< 美颜的模式
} st_effect_beauty_info_t;

/// @brief 3D美颜信息
typedef struct {
    char name[EFFECT_MAX_NAME_LEN];
    int part_id;
    float strength;
    float strength_min;
    float strength_max;
} st_effect_3D_beauty_part_info_t;

/// @brief 口红质地类型
typedef enum {
    EFFECT_LIPSTICK_CREAMY,             ///< 自然
    EFFECT_LIPSTICK_MATTE,              ///< 哑光
    EFFECT_LIPSTICK_LUSTRE,             ///< 水润
    EFFECT_LIPSTICK_FROST,              ///< 闪烁
    EFFECT_LIPSTICK_METAL,              ///< 金属
} st_effect_lipstick_finish_t;

/// @brief 试妆区域信息
typedef struct {
    int region_id;                      ///< 区域id
    float strength;                     ///< 颜色强度，[0, 1.0]
    st_color_t color;                   ///< 颜色, 每个通道的取值范围是[0, 255], 目前用到了 r, g, b 三个通道
} st_effect_tryon_region_info_t;

#define REGION_COUNT 6
/// @brief 试妆信息
typedef struct {
    st_color_t color;                                           ///< 颜色, 每个通道的取值范围是[0, 255], 目前用到了 r, g, b 三个通道
    float strength;                                             ///< 颜色强度, [0, 1.0]
    float line_width_ratio;                                     ///< 唇线线宽比例， [0, 1.0]
    float midtone;                                              ///< 明暗度, [0, 1.0], 0.5表示不改变颜色的明暗度
    float highlight;                                            ///< 高光, [0, 1.0], 0.0表示不加高光
    st_effect_lipstick_finish_t lip_finish_type;                ///< 口红质地类型

    int region_count;                                           ///< 当前效果的区域数量，目前只有修容、眼影、眼线支持多区域，其他试妆效果该变量均为0
    st_effect_tryon_region_info_t region_info[REGION_COUNT];    ///< 区域信息，最多支持REGION_COUNT个区域
} st_effect_tryon_info_t;


/// @brief 获取覆盖生效的美颜的数量, 需要在st_mobile_effect_render接口后调用，因为overlap信息是在render之后更新的
/// @param[in] handle 已初始化的特效句柄
/// @param[out] p_info_num 变化的美颜的数量
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_get_overlapped_beauty_count(st_handle_t handle, int* p_info_num);

/// @brief 获取覆盖生效的美颜的信息, 需要在st_mobile_effect_render接口后调用，因为overlap信息是在render之后更新的
/// @param[in] handle 已初始化的特效句柄
/// @param[out] p_infos 美颜信息的起始地址
/// @param[in] info_num 起始地址可以容纳美颜信息的数量
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_get_overlapped_beauty(st_handle_t handle, st_effect_beauty_info_t* p_infos, int info_num);

/// @brief 设置美颜的强度
/// @param[in] handle 已初始化的特效句柄
/// @param[in] param 美颜类型
/// @param[in] val 强度
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_set_beauty_strength(st_handle_t handle, st_effect_beauty_type_t param, float val);

/// @brief 获取美颜的强度
/// @param[in] handle 已初始化的特效句柄
/// @param[in] param 美颜类型
/// @param[out] val 强度
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_get_beauty_strength(st_handle_t handle, st_effect_beauty_type_t param, float* val);

/// @brief 设置美颜的模式, 目前仅对磨皮和美白有效，支持的有效模式为[0, 1, 2]三个值
/// @param[in] handle 已初始化的特效句柄
/// @param[in] param 美颜类型
/// @param[in] mode 模式，支持的有效模式为[0, 1, 2]三个值
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_set_beauty_mode(st_handle_t handle, st_effect_beauty_type_t param, int mode);

/// @brief 获取美颜的模式, 目前仅对磨皮和美白有效
/// @param[in] handle 已初始化的特效句柄
/// @param[in] param 美颜类型
/// @param[out] mode 模式
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_get_beauty_mode(st_handle_t handle, st_effect_beauty_type_t param, int* mode);

/// @brief Effect 美颜配置项，影响美颜功能，需要在OpenGL渲染线程中执行
typedef enum {
    EFFECT_BEAUTY_PARAM_ENABLE_WHITEN_SKIN_MASK,         ///< 是否为美白开启皮肤分割, 默认为不启用. 0 表示不启用， 非0表示启用
} st_effect_beauty_param_t;

/// @brief 设置美颜相关配置项
/// @param[in] handle 已初始化的特效句柄
/// @param[in] param 配置项类型
/// @param[in] val 配置项参数值，具体范围参考配置项说明
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_set_beauty_param(st_handle_t handle, st_effect_beauty_param_t param, float val);

/// @brief 获取美颜相关配置项
/// @param[in] handle 已初始化的特效句柄
/// @param[in] param 配置项类型
/// @param[out] val 配置项值
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_get_beauty_param(st_handle_t handle, st_effect_beauty_param_t param, float* val);

/// @brief 设置试妆相关参数
/// @param[in] handle 已初始化的特效句柄
/// @param[in] tryon_type 试妆类型
/// @param[in] p_info 需要设置的试妆信息
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_set_tryon_param(st_handle_t handle, st_effect_beauty_type_t tryon_type, const st_effect_tryon_info_t* p_info);

/// @brief 获取试妆相关参数
/// @param[in] handle 已初始化的特效句柄
/// @param[in] tryon_type 试妆类型
/// @param[out] p_info 需要返回的试妆信息
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_get_tryon_param(st_handle_t handle, st_effect_beauty_type_t tryon_type, st_effect_tryon_info_t* p_info);

/// @brief 加载美颜素材，可以通过将path参数置为nullptr，清空之前类型设置的对应素材（如美颜、美妆、滤镜素材）
///        目前对美颜支持设置美白、红润两种自定义的素材
/// @param[in] handle 已初始化的特效句柄
/// @param[in] param 美颜类型
/// @param[in] path 待添加的素材文件路径
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_set_beauty(st_handle_t handle, st_effect_beauty_type_t param, const char* path);

/// @brief 加载美颜素材
/// @param[in] handle 已初始化的特效句柄
/// @param[in] param 美颜类型
/// @param[in] buffer 待添加的素材缓存信息
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_set_beauty_from_buffer(st_handle_t handle, st_effect_beauty_type_t param, const st_effect_buffer_t* buffer);

/// @brief 添加素材包
/// @param[in] handle 已初始化的特效句柄
/// @param[in] p_package_path 待添加的素材包文件路径
/// @param[out] p_package_id 素材包ID
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_add_package(st_handle_t handle, const char* p_package_path, int* p_package_id);

/// @brief 添加缓存中的素材包
/// @param[in] handle 已初始化的特效句柄
/// @param[in] p_package_buffer 待添加的素材包缓存信息
/// @param[out] p_package_id 素材包ID
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_add_package_from_buffer(st_handle_t handle, const st_effect_buffer_t* p_package_buffer, int* p_package_id);

/// @brief 更换缓存中的素材包 (删除已有的素材包)
/// @param[in] handle 已初始化的特效句柄
/// @param[in] p_package_path 待更换的素材包文件路径
/// @param[out] package_id 素材包ID
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_change_package(st_handle_t handle, const char* p_package_path, int* p_package_id);

/// @brief 更换缓存中的素材包 (删除已有的素材包)
/// @param[in] handle 已初始化的特效句柄
/// @param[in] p_package_buffer 待更换的素材包缓存信息
/// @param[out] p_package_id 素材包ID
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_change_package_from_buffer(st_handle_t handle, const st_effect_buffer_t* p_package_buffer, int* p_package_id);

/// @brief 删除指定素材包
/// @param[in] handle 已初始化的特效句柄
/// @param[in] package_id 待删除的素材包ID
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_remove_package(st_handle_t handle, int package_id);

/// @brief 清空所有素材包
/// @param[in] handle 已初始化的特效句柄
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_clear_packages(st_handle_t handle);

/// @brief 重新播放制定素材包中的素材
/// @param[in] handle 已初始化的特效句柄
/// @param[in] package_id 素材包ID
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_replay_package(st_handle_t handle, int package_id);

/// @brief 素材包的播放状态类型
typedef enum {
    EFFECT_PACKAGE_BEGIN,           ///< 素材包开始(素材包中的任一素材开始播放认为素材包开始)
    EFFECT_PACKAGE_END,             ///< 素材包结束(素材包中开始播放后被切换或取消,或者已经开始的素材都完成播放循环认为是素材包结束播放)
    EFFECT_PACKAGE_TERMINATED,      ///< 素材包被终止(素材包中已经开始播放的素材都因为对应检测结果消失导致停止播放认为是素材包被终止)
} st_effect_package_state_t;

/// @brief 素材包中Module（子特效）的播放状态类型
typedef enum {
    EFFECT_MODULE_LOADED,               ///< 已加载
    EFFECT_MODULE_PAUSED_FIRST_FRAME,   ///< 暂停到第一帧
    EFFECT_MODULE_PLAYING,              ///< 正在播放
    EFFECT_MODULE_PAUSED,               ///< 暂停
    EFFECT_MODULE_PAUSED_LAST_FRAME,    ///< 暂停到最后一帧
    EFFECT_MODULE_INVISIBLE,            ///< 不可见
    EFFECT_MODULE_RESUMED,              ///< 唤醒, 下一帧开始执行
    EFFECT_MODULE_UNLOADED,             ///< 已被销毁（卸载）
} st_effect_module_state_t;

/// @brief 素材包信息
typedef struct {
    int package_id;                     ///< 素材包的ID
    char name[EFFECT_MAX_NAME_LEN];     ///< 素材包的名字
    st_effect_package_state_t state;    ///< 素材包当前的状态
    int module_count;                   ///< 素材包拥有的特效数量
    int displayed_frames;               ///< 当前播放的帧数
    void* reserved;                     ///< 额外的数据
} st_effect_package_info_t;

/// @brief 特效类型
typedef enum {
    EFFECT_MODULE_STICKER_2D = 0,       ///< 2D贴纸模块
    EFFECT_MODULE_SOUND = 1,            ///< 音乐模块
    EFFECT_MODULE_BEAUTIFY = 2,         ///< 美颜模块
    EFFECT_MODULE_FILTER = 3,           ///< 滤镜模块
    EFFECT_MODULE_DEFORMAITON = 4,      ///< 脸部变形模块
    EFFECT_MODULE_MAKEUP = 5,           ///< 美妆模块
    EFFECT_MODULE_BACKGROUND_EDGE = 6,  ///< 背景描边模块
    EFFECT_MODULE_STICKER_3D = 7,       ///< 3D贴纸模块
    EFFECT_MODULE_PARTICLE = 8,         ///< 粒子模块
    EFFECT_MODULE_AVATAR = 9,           ///< Avatar模块
    EFFECT_MODULE_FACE_EXCHANGE = 10,   ///< 多人换脸模块
    EFFECT_MODULE_FACE_MATTING = 11,    ///< 扣脸
    EFFECT_MODULE_SKYBOX = 12,          ///< 天空盒模块
    EFFECT_MODULE_FACE_STRETCH = 14,    ///< 人脸拖拽模块
    EFFECT_MODULE_DOUBLEGANGER = 15,    ///< 影分身
    EFFECT_MODULE_MASK_FILL = 16,       ///< 头发分割
    EFFECT_MODULE_HEAD_ANIMATION = 17,  ///< 大头模块
    EFFECT_MODULE_3D_FACE_MATTE = 18,   ///< 3D人脸抠图
    EFFECT_MODULE_GAN = 19,             ///< Gan模块
    EFFECT_MODULE_SEGMENT = 20,         ///< Segment模块
    EFFECT_MODULE_3D_MICRO_PLASTIC =22,     ///< 3D微整形模块
    EFFECT_MODULE_FACEMESH_EFFECTS = 23,    ///< 3Dfacemesh特效模块
    EFFECT_MODULE_3D_CAT_STICKER = 24,      ///< 3D猫脸贴纸
    EFFECT_MODULE_3D_DOG_STICKER = 25,      ///< 3D狗脸贴纸
    EFFECT_MODULE_3D_SHOES_STICKER = 26,    ///< 3D鞋子贴纸
    EFFECT_MODULE_3D_WRIST = 27, ///< 3D手饰贴纸
} st_effect_module_type_t;

/// @brief Module Info中的保留数据类型，根据特定case，保留数据的类型会不同
typedef enum
{
    EFFECT_RESERVED_UNKNOWN = 0,        ///< 未知（无意义）保留数据
    EFFECT_RESERVED_SOUND_DATA,         ///< 声音数据的buffer，类型是st_effect_buffer_t
    EFFECT_RESERVED_LOOP_NUM,           ///< module的loop次数，类型是int
    EFFECT_RESERVED_IMAGE,              ///< 图像数据的buffer，类型是st_image_t
} st_effect_reserved_t;

/// @brief Module Info结构体，可以通过st_mobile_effect_get_modules_in_package，st_mobile_effect_set_module_state_change_callback等API获取
///        通过st_mobile_effect_set_module_info API设置module数据
typedef struct {
    st_effect_module_type_t type;       ///< 贴纸的类型
    int module_id;                      ///< 贴纸的ID
    int package_id;                     ///< 贴纸所属素材包的ID
    char name[EFFECT_MAX_NAME_LEN];     ///< 贴纸的名字
    float strength;                     ///< 贴纸的强度
    int instance_id;                    ///< 贴纸对应的position id, 即st_mobile_human_action_t结果中不同类型结果中的id
    st_effect_module_state_t state;     ///< 贴纸的播放状态
    int current_frame;                  ///< 当前播放的帧数
    uint64_t position_type;             ///< 贴纸对应的position种类, 见st_mobile_human_action_t中的动作类型
    st_effect_reserved_t rsv_type;      ///< 额外数据（reserved）的类型，在特定case下需要强转为特定类型，参考st_effect_reserved_type定义
    void* reserved;                     ///< 额外的数据，在声音EFFECT_MODULE_LOADED回调中指向st_effect_buffer_t类型的声音文件buffer地址(该地址只在回调函数调用期间有效)，在声音的EFFECT_MODULE_PLAYING回调中对应的是声音循环次数（强转为int）
} st_effect_module_info_t;

/// @brief 获取素材信息
/// @param[in] handle 已初始化的特效句柄
/// @param[in] package_id 素材包ID
/// @param[out] p_package_info 素材信息
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_get_package_info(st_handle_t handle, int package_id, st_effect_package_info_t* p_package_info);

/// @brief 获取素材的贴纸信息
/// @param[in] handle 已初始化的特效句柄
/// @param[in] package_id 素材包ID
/// @param[in] module_num 贴纸信息地址能容纳的贴纸的数量
/// @param[out] p_modules 贴纸信息的起始地址
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_get_modules_in_package(st_handle_t handle, int package_id, st_effect_module_info_t* p_modules, int module_num);

/// @brief 获取贴纸信息
/// @param[in] handle 已初始化的特效句柄
/// @param[in] module_id 贴纸ID
/// @param[out] p_module_info 贴纸信息的起始地址
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_get_module_info(st_handle_t handle, int module_id, st_effect_module_info_t* p_module_info);

/// @brief 设置贴纸信息
/// @param[in] handle 已初始化的特效句柄
/// @param[in] module_id 贴纸ID
/// @param[in] p_module_info 贴纸信息
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_set_module_info(st_handle_t handle, const st_effect_module_info_t* p_module_info);

/// @brief 设置贴纸素材包内部美颜组合的强度，强度范围[0.0, 1.0]
/// @param[in] handle 已初始化的特效句柄
/// @param[in] group 美颜组合类型，目前只支持设置美妆、滤镜组合的强度
/// @param[in] strength 强度值
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_set_package_beauty_group_strength(st_handle_t handle, int pkg_id, st_effect_beauty_group_t group, float strength);

/// @brief 贴纸播放状态发生改变引起的回调函数
/// @param[in] handle 已初始化的特效句柄
/// @param[in] p_module_info 贴纸信息
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
typedef st_result_t(*st_effect_module_state_change_callback)(st_handle_t handle, const st_effect_module_info_t* p_module_info);

/// @brief 设置贴纸播放状态改变的回调函数指针
/// @param[in] handle 已初始化的特效句柄
/// @param[in] callback 回调函数指针
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_set_module_state_change_callback(st_handle_t handle, st_effect_module_state_change_callback callback);

/// @brief 素材包播放状态发生改变的引起的回调函数
/// @param[in] handle 已初始化的特效句柄
/// @param[in] p_package_info 素材包信息
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
typedef st_result_t(*st_effect_package_state_change_callback)(st_handle_t handle, const st_effect_package_info_t* p_package_info);

/// @brief 设置素材包播放状态改变的回调函数指针
/// @param[in] handle 已初始化的特效句柄
/// @param[in] callback 回调函数指针
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_set_packaged_state_change_callback(st_handle_t handle, st_effect_package_state_change_callback callback);

/// @brief 释放内部缓存的资源，目前主要是GL相关的渲染资源，需要在GL context中调用
/// @param[in] handle 已初始化的特效句柄
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_release_cached_resource(st_handle_t handle);

/// @brief 重置render接口输出buffer时，PC平台内部的双缓冲。一般在PC平台上层有一段时间没有用render接口时，防止闪一帧前面画面时调用。
/// @param[in] handle 已初始化的特效句柄
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_effect_reset_output_buffer_cache(st_handle_t handle);


/// @brief 在调用st_mobile_effect_set_beauty函数加载了3D微整形素材包之后调用。获取到素材包中所有的blendshape的数量
/// @param[in] handle 已初始化的特效句柄
/// @param[out] parts_count 输出的数组长度
ST_SDK_API st_result_t
st_moobile_effect_get_3d_beauty_parts_count(st_handle_t handle, int* parts_count);

/// @brief 在调用st_mobile_effect_set_beauty函数加载了3D微整形素材包之后调用。获取到素材包中所有的blendshape名称、index和当前强度[0, 1]
/// @param[in] handle 已初始化的特效句柄
/// @param[out] parts 输出的blend shape信息数组。由外部分配内存。
/// @param[in] parts_count 输入的数组长度,应小于等于获取的数量
ST_SDK_API st_result_t
st_mobile_effect_get_3d_beauty_parts(st_handle_t handle, st_effect_3D_beauty_part_info_t* parts, int parts_count);

/// @brief 用于输入human action的face mesh list信息。
/// @param[in] handle 已初始化的特效句柄
/// @param[in] parts 从human action中获取的face_mesh_list信息指针
ST_SDK_API st_result_t
st_mobile_effect_set_face_mesh_list(st_handle_t handle, st_mobile_face_mesh_list_t* face_mesh_list);

/// @brief 在调用st_mobile_effect_set_beauty函数加载了3D微整形素材包之后调用。在获取blendshape数组之后，可以依据起信息修改权重[0, 1]，设置给渲染引擎产生效果。
/// @param[in] handle 已初始化的特效句柄
/// @param[in] parts 输入的blend shape信息数组。
/// @param[in] partSize 输入的数组长度
ST_SDK_API st_result_t
st_mobile_effect_set_3d_beauty_parts_strength(st_handle_t handle, st_effect_3D_beauty_part_info_t* parts, int partSize);


#endif // _ST_MOBILE_EFFECT_H_

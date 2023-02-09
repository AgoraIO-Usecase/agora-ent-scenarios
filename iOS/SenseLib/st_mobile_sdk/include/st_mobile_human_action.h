/**
*@file st_mobile_human_action.h

* 提供人体相关的特征的检测，包括头部、面部、耳朵、嘴巴、眼睛等人体部位及肢体动作的检测与识别
* 同时提供检测结果的旋转、缩放、拷贝、镜像等功能
* 支持预览、视频、图片处理 三种模式
*
*@attention
*该文件中的API不保证线程安全.多线程调用时,需要确保安全调用.例如在 detect/reset 没有执行完就执行 process 可能造成crash;在 detect 执行过程中调用 reset 函数可能会造成crash.

* 一般调用步骤：创建句柄->加载模型->获取相关数据(不是必须）->检测特征->销毁句柄

* st_mobile_human_action_create

* st_mobile_human_action_add_sub_model

* st_mobile_human_action_get_face_mesh_list

* st_mobile_human_action_detect

* st_mobile_human_action_destroy

**/
#ifndef INCLUDE_STMOBILE_ST_MOBILE_HUMAN_ACTION_H_
#define INCLUDE_STMOBILE_ST_MOBILE_HUMAN_ACTION_H_

#include "st_mobile_common.h"
#include <stdint.h>
/// 用于detect_config配置选项, 每一位均表示开启/关闭一个检测选项，部分也可用来判断检测结果中的human_action动作类型
#define ST_MOBILE_FACE_DETECT               0x00000001  ///< 人脸检测
#define ST_MOBILE_EYE_BLINK                 0x00000002  ///< 眨眼
#define ST_MOBILE_MOUTH_AH                  0x00000004  ///< 嘴巴大张
#define ST_MOBILE_HEAD_YAW                  0x00000008  ///< 摇头
#define ST_MOBILE_HEAD_PITCH                0x00000010  ///< 点头
#define ST_MOBILE_BROW_JUMP                 0x00000020  ///< 眉毛挑动（该检测耗时较长，请在需要时开启）
#define ST_MOBILE_FACE_LIPS_UPWARD          0x00000040  ///< 嘴角上扬
#define ST_MOBILE_FACE_LIPS_POUTED          0x00000080  ///< 嘟嘴
#define ST_MOBILE_HAND_DETECT               0x00000100  ///  检测手
#define ST_MOBILE_HAND_OK                   0x00000200  ///< OK手势
#define ST_MOBILE_HAND_SCISSOR              0x00000400  ///< 剪刀手
#define ST_MOBILE_HAND_GOOD                 0x00000800  ///< 大拇哥
#define ST_MOBILE_HAND_PALM                 0x00001000  ///< 手掌
#define ST_MOBILE_HAND_PISTOL               0x00002000  ///< 手枪手势
#define ST_MOBILE_HAND_LOVE                 0x00004000  ///< 爱心手势
#define ST_MOBILE_HAND_HOLDUP               0x00008000  ///< 托手手势
#define ST_MOBILE_HAND_CONGRATULATE         0x00020000  ///< 恭贺（抱拳）
#define ST_MOBILE_HAND_FINGER_HEART         0x00040000  ///< 单手比爱心
#define ST_MOBILE_HAND_FINGER_INDEX         0x00100000  ///< 食指指尖
#define ST_MOBILE_HAND_FIST                 0x00200000  ///< 拳头手势
#define ST_MOBILE_HAND_666                  0x00400000  ///< 666
#define ST_MOBILE_HAND_BLESS                0x00800000  ///< 双手合十
#define ST_MOBILE_HAND_ILOVEYOU             0x010000000000  ///< 手势ILoveYou
#define ST_MOBILE_HAND_SSH                  0x400000000000  ///< 手势嘘（依赖于手势检测和106点检测）
#define ST_MOBILE_HAND_THREE                0x10000000000000  ///< 三根手指
#define ST_MOBILE_HAND_FOUR                 0x20000000000000  ///< 四根手指
#define ST_MOBILE_SEG_BACKGROUND            0x00010000  ///< 检测前景背景分割
#define ST_MOBILE_FACE_240_DETECT           0x01000000  ///< 检测人脸240关键点 (deprecated)
#define ST_MOBILE_DETECT_EXTRA_FACE_POINTS  0x01000000  ///< 检测人脸240关键点
#define ST_MOBILE_DETECT_EYEBALL_CENTER     0x02000000  ///< 检测眼球中心点
#define ST_MOBILE_DETECT_EYEBALL_CONTOUR    0x04000000  ///< 检测眼球轮廓点
#define ST_MOBILE_BODY_KEYPOINTS            0x08000000  ///< 检测肢体关键点
#define ST_MOBILE_BODY_CONTOUR              0x10000000  ///< 检测肢体轮廓点
#define ST_MOBILE_SEG_HAIR                  0x20000000    ///< 检测头发分割
#define ST_MOBILE_DETECT_TONGUE             0x40000000  ///< 检测舌头关键点
#define ST_MOBILE_SEG_HEAD                  0x0100000000    /// 检测头部分割
#define ST_MOBILE_SEG_SKIN                  0x0200000000    /// 检测皮肤分割
#define ST_MOBILE_SEG_FACE_OCCLUSION        0x0400000000    /// 检测面部遮挡分割
#define ST_MOBILE_DETECT_FOOT				0x0800000000	/// 脚部关键点检测
#define ST_MOBILE_BODY_ACTION5              0x1000000000    /// 动感超人 暂时不支持
#define ST_MOBILE_SEG_TROUSER_LEG           0x1000000000    /// 检测裤腿分割
#define ST_MOBILE_DETECT_HAND_SKELETON_KEYPOINTS    0x20000000000    /// 检测单手关键点, 最多支持两只手的关键点检测
#define ST_MOBILE_DETECT_HAND_SKELETON_KEYPOINTS_3D 0x40000000000    /// 检测单手3d关键点
#define ST_MOBILE_SEG_MULTI                         0x80000000000  ///< 检测多类分割
#define ST_MOBILE_DETECT_GAZE                       0x100000000000 ///< 检测视线方向
#define ST_MOBILE_DETECT_DYNAMIC_GESTURE            0x200000000000 ///< 检测动态手势
#define ST_MOBILE_DETECT_AVATAR_HELPINFO            0x800000000000 ///< 检测avatar辅助信息
#define ST_MOBILE_DETECT_FACE_S_COLOR            0x1000000000000 ///< 依赖106关键点检测
#define ST_MOBILE_DETECT_HAIR_COLOR                 0x100000000000000  ///< avatar发色检测，依赖106关键点和头发分割，目前只支持单人发色
#define ST_MOBILE_BODY_KEYPOINTS_3D        0x2000000000000  ///< 检测肢体3d关键点
#define ST_MOBILE_DETECT_EAR               0x4000000000000  ///< 检测耳朵关键点
#define ST_MOBILE_DETECT_FOREHEAD          0x8000000000000  ///< 检测额头关键点
#define ST_MOBILE_DETECT_FACE_MESH         0x40000000000000 ///< 检测3dmesh关键点
#define ST_MOBILE_DETECT_MOUTH_PARSE       0x80000000000000 ///< 检测嘴部遮挡
#define ST_MOBILE_DETECT_HEAD              0x200000000000000 ///< 检测head关键点
#define ST_MOBILE_DETECT_HEAD_MESH         0x400000000000000 ///< 检测head mesh关键点
#define ST_MOBILE_DETECT_UPBODY_AVATAR     0x800000000000000 ///< 检测半身avatar
#define ST_MOBILE_SEG_SKY                  0x1000000000000000 ///< 检测天空分割
#define ST_MOBILE_DEPTH_ESTIMATION         0X2000000000000000 ///< 检测深度估计
#define ST_MOBILE_NAIL_DETECT              0x4000000000000000  ///<指甲关键点检测
#define ST_MOBILE_SEG_CLOTH                0x000080000        ///< 衣物检测
#define ST_MOBILE_WRIST_DETECT 0x8000000000000000 ///<手腕关键点检测

/// 以下是常见的检测detect_config
#define ST_MOBILE_FACE_DETECT_FULL        0x000000FF      ///< 检测所有脸部动作
#define ST_MOBILE_HAND_DETECT_FULL        0x30410000F6FF00  ///< 检测所有手势, 如果手势分类和手部骨骼点(2d/3d)的config同时打开时, 对于恭贺（抱拳)/双手合十/手势ILoveYou等组合手势只能检测出一个组合手势．
#define ST_MOBILE_BODY_DETECT_FULL        0x018000000     ///< 检测肢体关键点和肢体轮廓点
///@brief human 3d pose 信息
typedef struct {
	uint64_t position;                              ///<检测的config,ST_MOBILE_WRIST_DETECT或ST_MOBILE_DETECT_FOOT，可用于区分获取的3dPose
	int id;                                         ///<检测id
	st_mobile_transform_t pose;                    ///<输出的pose信息
	bool isvalid;									///< 输出pose信息是否有效
}st_human_pose_t;
/// @brief 动态手势类型
typedef enum st_hand_dynamic_gesture_type_t {
    ST_DYNAMIC_GESTURE_TYPE_INVALID = -1,                      ///< 无效的动态手势
    ST_DYNAMIC_GESTURE_TYPE_HOLD_ON,                           ///< 静止
    ST_DYNAMIC_GESTURE_TYPE_FOREFINGER_CLICK,                  ///< 食指点击
    ST_DYNAMIC_GESTURE_TYPE_FOREFINGER_ROTATION_CLOCKWISE,     ///< 食指顺时针旋转
    ST_DYNAMIC_GESTURE_TYPE_FOREFINGER_ROTATION_ANTICLOCKWISE, ///< 食指逆时针旋转
    ST_DYNAMIC_GESTURE_TYPE_PALM_FAN,                          ///< 手掌扇风（废弃）
    ST_DYNAMIC_GESTURE_TYPE_PALM_MOVING_LEFT_AND_RIGHT,        ///< 手掌左右平移
    ST_DYNAMIC_GESTURE_TYPE_PALM_MOVING_UP_AND_DOWN,           ///< 手掌上下平移
    ST_DYNAMIC_GESTURE_TYPE_MAX_NUM                            ///< 目前支持的动态手势个数
} st_hand_dynamic_gesture_type_t;
/// @brief 动态手势结果
typedef struct st_hand_dynamic_gesture_t {
    int has_dynamic_gesture;                        ///< 是否有动态手势：0表示没有，1表示有
    st_hand_dynamic_gesture_type_t dynamic_gesture; ///< 动态手势类别
    float score;                                    ///< 动态手势得分
} st_hand_dynamic_gesture_t;
/// @brief 手势检测结果
typedef struct st_mobile_hand_t {
    int id;                                         ///< 手的id
    st_rect_t rect;                                 ///< 手部矩形框
    int left_right;                                 ///< 0 unknown ,1 left, 2 right 左手/右手 只在3d骨骼点开启后有结果
    st_pointf_t *p_key_points;                      ///< 手部关键点
    int key_points_count;                           ///< 手部关键点个数
    unsigned long long hand_action;                 ///< 手部动作
    float score;                                    ///< 手部动作置信度
    st_pointf_t *p_skeleton_keypoints;              ///< 手部骨骼点
    int skeleton_keypoints_count;                   ///< 手部骨骼点个数 一般是0/20
    st_point3f_t *p_skeleton_3d_keypoints;          ///< 手部3d骨骼点
    int skeleton_3d_keypoints_count;                ///< 手部3d骨骼点的个数，一般是0/21
    st_hand_dynamic_gesture_t hand_dynamic_gesture; ///< 动态手势
    st_pointf_t *p_dynamic_gesture_keypoints;       ///< 动态手势关键点
    int dynamic_gesture_keypoints_count;            ///< 动态手势关键点的个数
} st_mobile_hand_t, *p_st_mobile_hand_t;

/// @brief 每个指甲检测结果
typedef struct st_mobile_nail_t{
	int id;											///< 指甲id
	int label;										///< 指甲label，取值0-4(大拇指，食指，中指，无名指和小拇指)
	float score;									///< 置信度
	st_rect_t rect;									///< 包络矩形框
	st_pointf_t *p_key_points;						///< 关键点数组
	int points_count; 								///< 关键点数目,一般是16
}st_mobile_nail_t,*p_st_mobile_nail_t;

typedef struct st_mobile_wrist_t{
	int id;											///< 手腕id
	float score;									///< 置信度
	st_rect_t rect;									///< 包络矩形框
	st_pointf_t *p_key_points;						///< 关键点数组
	int points_count; 								///< 关键点数目,一般是8
	st_left_or_right_t label;						///< 左右手分类
	st_mobile_transform_t pose;						///<pose手腕3d位置信息,方向与人正立方向一致，暂不支持resize mirror rotate后处理
	bool isvalid;									///<delay模式 pose是否有效，暂不支持后处理
	bool ismirror;									///<点位是否进行镜像，主要用于调整pose
}st_mobile_wrist_t, *p_st_mobile_wrist_t;


/// @brief 肢体检测结果
typedef struct st_mobile_body_t {
    int id;                         ///< 肢体 id
    st_pointf_t *p_key_points;      ///< 肢体关键点
    float * p_key_points_score;     ///< 肢体关键点的置信度[0,1] 值越大，置信度越高.建议用户使用0.15作为置信度阈值.
    int key_points_count;           ///< 肢体关键点个数 目前为0/4/14
	st_pointf_t *p_contour_points;  ///< 肢体轮廓点
	float * p_contour_points_score; ///< 肢体轮廓点的置信度[0,1] 值越大，置信度越高.建议用户使用0.15作为置信度阈值.
	int contour_points_count;       ///< 肢体轮廓点个数 目前为0/63
	st_point3f_t *p_key_points_3d;	///< 肢体3d点
	float * p_key_points_3d_score;  ///< 肢体3d点置信度
    int key_points_3d_count;		///< 肢体3d点个数(0或17）
	int label;						///< 肢体label 0表示正常 1表示双手抱胸 2表示双手抱头
	int hand_valid[2];              ///< 半身肢体中左右手信息是否有效，仅用于半身avatar模型，用于调试看中间结果
} st_mobile_body_t, *p_st_mobile_body_t;

/// @brief 分割检测结果
typedef struct st_mobile_segment_t {
    st_image_t *p_segment;      ///< 前后背景分割结果图片信息,前景为0,背景为255,边缘部分有模糊(0-255之间),输出图像大小可以调节，mask大小不能超过图像的大小．
    float score;		        ///< 前后背景分割置信度
    ///min_threshold 和max_threshold值都是0.0或分别为0.0和1.0 上层则不需要卡阈值.
    float min_threshold;	    ///< 前后背景最小阈值，与模型相关，取值范围时0.0f-1.0f，当min_threshold和max_threshold同时大于0时，需要外部做后处理，当原始图像尺寸大于320，mask大小是320; 当原始图像尺寸小于320，mask输出大小是原图大小。
    float max_threshold;	    ///< 前后背景最大阈值，同上
    st_pointf_t offset;         ///< 分割结果位于原图的左上角坐标，一般是（0,0），只有嘴唇分割结果不同
    st_pointf_t scale;          ///< 分割结果的缩放比例。p_segment->width(height)*scale为和原图对应的分割结果像素大小。
    st_rotate_type rotate;      ///< 分割结果朝向, 除深度估计和腿部分割结果除外, 其他分割结果默认朝上
    int face_id;                ///< 如果-1表示是整幅图的结果; 如果>=0, 与st_mobile_face_t 中的id对应. 目前嘴部分割和头部特殊模型分割支持id
    unsigned char* p_extra_info_buffer;  ///< 额外输出的中间信息， 仅供sdk中的渲染接口使用， 目前仅天空分割会输出
    int extra_info_length;      ///< 额外输出中间信息buffer长度
} st_mobile_segment_t, *p_st_mobile_segment_t;

/// @brief 人头检测结果信息
typedef struct st_mobile_head_result_t {
    st_rect_t rect;             ///< 目标框，用于表示此目标在当前帧上的位置
    int id;                     ///< 目标ID, 用于表示在目标跟踪中的相同目标在不同帧多次出现
    float score;                ///< 目标置信度，用于表示此目标在当前帧中的置信度 (0 - 10)
    float angle;                ///< 目标roll角，用于表示此目标在当前帧中的旋转信息，原图中目标逆时针旋转angle度后，目标会是正方向 (-180 - 180)
} st_mobile_head_result_t, *p_st_mobile_head_result_t;

/// @brief 人头检测结果
typedef struct st_mobile_head_t {
    st_mobile_head_result_t * p_head_result; ///< 人头检测结果信息
    st_mobile_face_mesh_t * p_head_mesh;     ///< head mesh信息，包括head mesh关键点及个数
} st_mobile_head_t, *p_st_mobile_head_t;

/// @brief 脚部检测结果
typedef struct st_mobile_foot_t {
	int id;						///< 脚的ID
	float score;				///< 脚的置信度
	st_rect_t rect;				///< 矩形框
	st_pointf_t *p_key_points;  ///< 脚部关键点
	int key_points_count;		///< 脚部关键点个数
	st_left_or_right_t label;	///< 脚部类型(左脚还是右脚)
} st_mobile_foot_t, *p_st_mobile_foot_t;

/// @brief 所有human_action 的分割结果
typedef struct st_mobile_human_action_segments_t {
    st_mobile_segment_t *p_figure;           ///< 检测到人像分割信息
    st_mobile_segment_t *p_hair;             ///< 检测到头发分割信息
    st_mobile_segment_t *p_multi;            ///< 检测到多类分割信息
    st_mobile_segment_t *p_sky;				 ///< 检测到天空分割信息
    st_mobile_segment_t *p_skin;             ///< 检测到皮肤分割信息
    st_mobile_segment_t *p_depth;			 ///< 检测到深度估计信息
    st_mobile_segment_t *p_mouth_parse;      ///< 检测到的嘴唇遮挡分割信息
    int mouth_parse_count;                   ///< 检测到的嘴唇数目 与p_faces的id对应
    st_mobile_segment_t *p_head;             ///< 检测到头部分割信息
    int head_count;                          ///< 目前支持多人头，分割face_id与p_faces的id对应
    st_mobile_segment_t *p_face_occlusion;   ///< 检测到人脸遮挡分割信息
    int face_occlusion_count;                ///< 目前只支持1个
	st_mobile_segment_t *p_trouser_leg;	     ///< 检测到裤腿分割信息
    int trouser_leg_count;                   ///< 目前只支持1个
    st_mobile_segment_t* p_cloth;            ///< 检测到的衣物分割信息
    int cloth_count;                         ///< 目前只支持1个
} st_mobile_human_action_segments_t;

/// @brief human_action检测结果
typedef struct st_mobile_human_action_t {
    st_mobile_face_t *p_faces;               ///< 检测到的人脸信息
    int face_count;                          ///< 检测到的人脸数目
    st_mobile_head_t *p_heads;               ///< 检测到的人头信息
    int head_count;                          ///< 检测到的人头数目
    st_mobile_hand_t *p_hands;               ///< 检测到的手的信息
    int hand_count;                          ///< 检测到的手的数目
    st_mobile_body_t *p_bodys;               ///< 检测到的肢体信息
    int body_count;                          ///< 检测到的肢体的数目
    float camera_motion_score;               ///< 摄像头运动状态置信度 无效
    st_mobile_human_action_segments_t * p_segments;  ///< 所有分割结果
	st_mobile_nail_t *p_nail;				 ///< 检测到的指甲的信息
	int nail_count;							 ///< 检测到的指甲个数
	st_mobile_foot_t *p_feet;				 ///< 检测到的脚的信息
	int foot_count;							 ///< 检测到的脚的个数
	st_mobile_wrist_t *p_wrist;				///< 检测到的手腕的信息
	int wrist_count;						 ///< 检测到的手腕的个数
    unsigned char* p_extra_buffer;           ///< 额外检测信息
    int extra_buffer_length;
} st_mobile_human_action_t, *p_st_mobile_human_action_t;
/// 创建人体行为检测句柄的默认配置: 设置检测模式和检测类型
/// 视频检测, 检测人脸、手势和前后背景
#define ST_MOBILE_HUMAN_ACTION_DEFAULT_CONFIG_VIDEO     ST_MOBILE_DETECT_MODE_VIDEO
/// 图片检测, 检测人脸、手势和前后背景
#define ST_MOBILE_HUMAN_ACTION_DEFAULT_CONFIG_IMAGE     ST_MOBILE_DETECT_MODE_IMAGE


/// @brief 创建人体行为检测句柄. Android建议使用st_mobile_human_action_create_from_buffer
/// @param[in] model_path 模型文件的路径,例如models/action.model. 为NULL时需要调用st_mobile_human_action_add_sub_model添加需要的模型
/// @param[in] config 配置选项 预览使用ST_MOBILE_DETECT_MODE_VIDEO, 离线视频处理使用ST_MOBILE_TRACKING_SINGLE_THREAD， 图片使用ST_MOBILE_DETECT_MODE_IMAGE
/// @param[out] handle 人体行为检测句柄,失败返回NULL
/// @return 成功返回ST_OK,失败返回其他错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_human_action_create(
    const char *model_path,
    unsigned int config,
    st_handle_t *handle
);

/// @brief 创建人体行为检测句柄
/// @param[in] buffer 模型缓存起始地址,为NULL时需要调用st_mobile_human_action_add_sub_model添加需要的模型
/// @param[in] buffer_size 模型缓存大小
/// @param[in] config 配置选项 预览使用ST_MOBILE_DETECT_MODE_VIDEO, 离线视频处理使用ST_MOBILE_TRACKING_SINGLE_THREAD， 图片使用ST_MOBILE_DETECT_MODE_IMAGE
/// @param[out] handle 人体行为检测句柄,失败返回NULL
/// @return 成功返回ST_OK,失败返回其他错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_human_action_create_from_buffer(
    const unsigned char* buffer,
    unsigned int buffer_size,
	unsigned int config,
    st_handle_t *handle
);

/// @brief 通过子模型创建人体行为检测句柄, st_mobile_human_action_create和st_mobile_human_action_create_with_sub_models只能调一个
/// @param[in] model_path_arr 模型文件路径指针数组. 根据加载的子模型确定支持检测的类型. 如果包含相同的子模型, 后面的会覆盖前面的.
/// @param[in] model_count 模型文件数目
/// @param[in] config 配置选项 预览使用ST_MOBILE_DETECT_MODE_VIDEO, 离线视频处理使用ST_MOBILE_TRACKING_SINGLE_THREAD， 图片使用ST_MOBILE_DETECT_MODE_IMAGE
/// @param[out] handle 人体行为检测句柄,失败返回NULL
/// @return 成功返回ST_OK,失败返回其他错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_human_action_create_with_sub_models(
    const char **model_path_arr,
    int model_count,
    unsigned int detect_mode,
    st_handle_t *handle
);

/// @brief 添加子模型. Android建议使用st_mobile_human_action_add_sub_model_from_buffer
/// @param[in] handle 人体行为检测句柄
/// @param[in] model_path 模型文件的路径. 后添加的会覆盖之前添加的同类子模型。加载模型耗时较长, 建议在初始化创建句柄时就加载模型
ST_SDK_API st_result_t
st_mobile_human_action_add_sub_model(
    st_handle_t handle,
    const char *model_path
);
/// @brief 添加子模型.
/// @param[in] handle 人体行为检测句柄
/// @param[in] buffer 模型缓存起始地址
/// @param[in] buffer_size 模型缓存大小
ST_SDK_API st_result_t
st_mobile_human_action_add_sub_model_from_buffer(
    st_handle_t handle,
    const unsigned char* buffer,
    unsigned int buffer_size
);

/// @brief 人脸形状
typedef enum st_face_shape_t {
    ST_FACE_SHAPE_UNKNOWN,      /// 未知类型
    ST_FACE_SHAPE_NATURAL,      /// 自然
    ST_FACE_SHAPE_ROUND,        /// 圆脸
    ST_FACE_SHAPE_SQUARE,       /// 方脸
    ST_FACE_SHAPE_LONG,         /// 长脸
    ST_FACE_SHAPE_RECTANGLE     /// 长形脸
} st_face_shape_t;

/// @brief 获取人脸形状
/// @param[in] handle 已初始化的human_action句柄
/// @param[in] p_face 检测到的人脸信息
/// @param[out] p_face_shape 输出人脸形状结果
ST_SDK_API st_result_t
st_mobile_human_action_get_face_shape(
    st_handle_t handle,
    const st_mobile_face_t *p_face,
    st_face_shape_t *p_face_shape
);

/// @brief human action 所用的模型类别，仅用于根据模型类别删除模型
typedef enum {
	ST_MOBILE_MODEL_TYPE_FACE_106 = 0,                      ///< 人脸106点
	ST_MOBILE_MODEL_TYPE_FACE_EXTRA= 1,                     ///< 人脸240点
	ST_MOBILE_MODEL_TYPE_FACE_EYEBALL= 2,                   ///< 眼球中心点
	ST_MOBILE_MODEL_TYPE_FACE_TONGUE= 3,                    ///< 舌头关键点
	ST_MOBILE_MODEL_TYPE_FACE_GAZE = 4,                     ///< 视线方向
	ST_MOBILE_MODEL_TYPE_FACE_AVATAR_HELPER = 5,            ///< 人脸avatar辅助信息开关
	ST_MOBILE_MODEL_TYPE_FACE_EAR = 6,                      ///< 耳朵关键点开关
	ST_MOBILE_MODEL_TYPE_FACE_MESH = 7,                     ///< 3dmesh关键点开关
	ST_MOBILE_MODEL_TYPE_AVARAE_HELPER = 8,                 ///< avatar help 模型
	ST_MOBILE_MODEL_TYPE_FACE_POSE3D = 9,                   ///< face pose 3d pose 模型

	ST_MOBILE_MODEL_TYPE_HAND_DETECT =100,                  ///< 手势
	ST_MOBILE_MODEL_TYPE_HAND_SKELETON_KEYPOINTS_2D3D = 101,///< 手势关节点
	ST_MOBILE_MODEL_TYPE_HAND_DYNAMIC_GESTURE = 103,        ///< 动态手势
//	ST_MOBILE_MODEL_TYPE_HAND_FARDISTANCE = 104             ///< 远距离手势

	ST_MOBILE_MODEL_TYPE_BODY_2D =200,                      ///< 肢体2d模型,半身肢体2d模型，肢体轮廓点模型
	ST_MOBILE_MODEL_TYPE_BODY_3D = 202,                     ///< 肢体3d点
    ST_MOBILE_MODEL_TYPE_UPBODY_AVATAR = 203,               ///< 半身肢体avatar

	ST_MOBILE_MODEL_TYPE_HEAD_DETECT = 300,                 ///< 头部检测
	ST_MOBILE_MODEL_TYPE_HEAD_MESH = 301,                   ///< 头部mesh

	ST_MOBILE_MODEL_TYPE_SEGMENT_FIGURE = 400,              ///< 背景分割
	ST_MOBILE_MODEL_TYPE_SEGMENT_HAIR = 401,                ///< 头发分割
	ST_MOBILE_MODEL_TYPE_SEGMENT_MULTI = 402,               ///< 多类分割
	ST_MOBILE_MODEL_TYPE_SEGMENT_HEAD= 403,                 ///< 头部分割
	ST_MOBILE_MODEL_TYPE_SEGMENT_SKIN = 404,                ///< 皮肤分割
	ST_MOBILE_MODEL_TYPE_SEGMENT_MOUTH_PARSE = 405,         ///< 嘴部遮挡信息分割
	ST_MOBILE_MODEL_TYPE_SEGMENT_FACE_OCCLUSION = 406,      ///< 面部遮挡信息分割
	ST_MOBILE_MODEL_TYPE_SEGMENT_SKY = 407,				    ///< 天空分割

	ST_MOBILE_MODEL_TYPE_DEPTH_ESTIMATION = 408,		    ///< 深度估计信息
    ST_MOBILE_MODEL_TYPE_SEGMENT_TROUSER_LEG = 409,         ///< 裤腿分割
    ST_MOBILE_MODEL_TYPE_SEGMENT_CLOTH = 500,               ///< 衣服分割

	ST_MOBILE_MODEL_TYPE_NAIL = 501,                        ///< 指甲检测

	ST_MOBILE_MODEL_TYPE_FOOT = 600,					    ///< 脚部关键点检测

	ST_MOBILE_MODEL_TYPE_WRIST = 609,					    ///< 手腕关键点检测
} st_mobile_model_type;


/// @brief 删除子模型.
/// @param[in] handle 人体行为检测句柄
/// @param[in] model_type 删除对应的模型,可用来减少内存
/// @return 成功返回ST_OK， 失败返回其他错误码
ST_SDK_API
st_result_t st_mobile_human_action_remove_model_by_config(
	st_handle_t handle,
	st_mobile_model_type model_type
	);

/// @brief 释放人体行为检测句柄
/// @param[in] handle 已初始化的人体行为句柄
ST_SDK_API
void st_mobile_human_action_destroy(
    st_handle_t handle
);

/// @brief 人体行为检测
/// @param[in] handle 已初始化的人体行为句柄
/// @param[in] image 用于检测的图像数据
/// @param[in] pixel_format 用于检测的图像数据的像素格式. 检测人脸建议使用NV12、NV21、YUV420P(转灰度图较快),检测手势和前后背景建议使用BGR、BGRA、RGB、RGBA
/// @param[in] image_width 用于检测的图像的宽度(以像素为单位)
/// @param[in] image_height 用于检测的图像的高度(以像素为单位)
/// @param[in] image_stride 用于检测的图像的跨度(以像素为单位),即每行的字节数；目前仅支持字节对齐的padding,不支持roi
/// @param[in] orientation 图像中人脸的方向
/// @param[in] detect_config 需要检测的人体行为,例如ST_MOBILE_EYE_BLINK | ST_MOBILE_MOUTH_AH | ST_MOBILE_HAND_LOVE | ST_MOBILE_SEG_BACKGROUND
/// @param[out] p_human_action 检测到的人体行为,由用户分配内存. 会覆盖上一次的检测结果.
/// @return 成功返回ST_OK,失败返回其他错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_human_action_detect(
    st_handle_t handle,
    const unsigned char *image,
    st_pixel_format pixel_format,
    int image_width,
    int image_height,
    int image_stride,
    st_rotate_type orientation,
    unsigned long long detect_config,
    st_mobile_human_action_t *p_human_action
);

/// @brief 人体行为检测(输入为多平面图像)
/// @param[in] handle 已初始化的人体行为句柄
/// @param[in] image 用于检测的图像数据 只支持yuv，nv12,nv21格式
/// @param[in] orientation 图像中人脸的方向
/// @param[in] detect_config 需要检测的人体行为,例如ST_MOBILE_EYE_BLINK | ST_MOBILE_MOUTH_AH | ST_MOBILE_HAND_LOVE | ST_MOBILE_SEG_BACKGROUND
/// @param[out] p_human_action 检测到的人体行为,由用户分配内存. 会覆盖上一次的检测结果.
/// @return 成功返回ST_OK,失败返回其他错误码,错误码定义在st_mobile_common.h中,如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_human_action_detect_from_multi_plane_image(
    st_handle_t handle,
    const st_multiplane_image_t* image,
    st_rotate_type orientation,
    unsigned long long detect_config,
    st_mobile_human_action_t *p_human_action
);

/// @brief 重置, 清除所有缓存信息. 视频模式下会在handle中缓存一些状态，当切换分辨率、切换前后摄像头、切换视频、两帧图像差别较大时建议调用reset
/// @param[in] handle　已初始化的human_action句柄
ST_SDK_API st_result_t
st_mobile_human_action_reset(
    st_handle_t handle
);

/// @brief 获取分割前景图像(人像/天空分割需要)
/// @param[in] handle　已初始化的human_action句柄
/// @param[in] 输入分割config，目前只支持config=ST_MOBILE_SEG_BACKGROUND或config=ST_MOBILE_SEG_SKY，不能并联使用．
/// @param[out] 输出分割前景图像，只返回ST_PIX_FMT_BGR888格式
/// @return 成功返回ST_OK, 失败返回其他错误码
ST_SDK_API st_result_t
st_mobile_human_action_get_segment_foreground (
    st_handle_t handle,
    unsigned long long config,
    st_image_t* figure_matting_foreground
);

/// @brief 使用GPU对分割结果做后处理，并将处理后的结果输出到纹理上，需要在OpenGL context中调用
/// @param[in] handle　已初始化的human_action句柄
/// @param[in] p_segment CPU中的分割输出结果（st_mobile_human_action_t）结构体中
/// @param[in] p_src_tex 用于检测的预览/图片纹理
/// @param[in] segment_type 分割数据的类型，通过detect config区分，目前只支持天空分割（ST_MOBILE_SEG_SKY）
/// @param[out] p_dst_tex GPU处理后的分割结果，需要在上层预先创建texture，纹理比例需要与原图一致，分辨率可不同
/// @return 成功返回ST_OK, 失败返回其他错误码
ST_SDK_API st_result_t
st_mobile_human_action_gpu_segment_refine(
    st_handle_t handle,
    const st_mobile_segment_t *p_segment,
    const st_mobile_texture_t *p_src_tex,
    unsigned long long segment_type,
    st_mobile_texture_t *p_dst_tex
);

/// @brief 释放humanAction handle内部的GL资源，需要在OpenGL context中调用。调用该接口之后，如果没有再调用GL相关的接口，handle的销毁不需要在GL线程中调用。否则，
///        如果handle销毁没有在GL线程中调用，GL相关资源可能会泄露。
/// @param[in] handle　已初始化的human_action句柄
/// @return 成功返回ST_OK, 失败返回其他错误码
ST_SDK_API st_result_t
st_mobile_human_action_release_gl_resource (
    st_handle_t handle
);

/// @brief human_action参数类型
typedef enum {
	// 人脸参数
    /// 设置检测到的最大人脸数目N(默认值32, 最大值32),持续track已检测到的N个人脸直到人脸数小于N再继续做detect.值越大,检测到的人脸数目越多,但相应耗时越长. 如果当前人脸数目达到上限，检测线程将休息
    ST_HUMAN_ACTION_PARAM_FACELIMIT = 0,
    /// 设置tracker每多少帧进行一次detect(默认值有人脸时24,无人脸时24/3=8). 值越大,cpu占用率越低, 但检测出新人脸的时间越长.
    ST_HUMAN_ACTION_PARAM_FACE_DETECT_INTERVAL = 1,
    /// 设置106点平滑的阈值[0.0,1.0](默认值0.5), 值越大, 点越稳定,但相应点会有滞后.
    ST_HUMAN_ACTION_PARAM_SMOOTH_THRESHOLD = 2,
    /// 设置head_pose去抖动的阈值[0.0,1.0](默认值0.5),值越大, pose信息的值越稳定,但相应值会有滞后.
    ST_HUMAN_ACTION_PARAM_HEADPOSE_THRESHOLD = 3,
    /// 设置脸部隔帧检测（对上一帧结果做拷贝），目的是减少耗时。默认每帧检测一次. 最多每10帧检测一次. 开启隔帧检测后, 只能对拷贝出来的检测结果做后处理.
    ST_HUMAN_ACTION_PARAM_FACE_PROCESS_INTERVAL = 5,
    ///设置人脸106点检测的阈值[0.0,1.0]
    ST_HUMAN_ACTION_PARAM_FACE_THRESHOLD = 6,

    /// 设置mesh渲染模式, mesh分为人脸，眼睛，嘴巴，后脑勺，耳朵，脖子，眉毛七个部位，2106模型只包含人脸，眼睛，嘴巴三个部位，3060/2396模型只包含人脸，眼睛，嘴巴，后脑勺，耳朵，脖子六个部位
    /// 参数值类型为st_mobile_mesh_part，默认只渲染人脸st_mobile_mesh_part::ST_MOBILE_MESH_PART_FACE，
    /// 可以设置多个部位，例如：渲染人脸和嘴巴，st_mobile_mesh_part::ST_MOBILE_MESH_PART_FACE + st_mobile_mesh_part::ST_MOBILE_MESH_PART_MOUTH
    ST_HUMAN_ACTION_PARAM_MESH_MODE = 20,
    /// 设置mesh额头点扩展scale范围起始值（小于终止值，默认是2）
    ST_HUMAN_ACTION_PARAM_MESH_START_SCALE = 21,
    /// 设置mesh额头点扩展scale范围终止值（大于起始值，默认是3）
    ST_HUMAN_ACTION_PARAM_MESH_END_SCALE = 22,
    /// 设置mesh结果输出坐标系,(0: 屏幕坐标系， 1：三维坐标系，默认是屏幕坐标系）
    ST_HUMAN_ACTION_PARAM_MESH_OUTPUT_FORMAT = 23,
    /// 获取mesh模型支持的关键点的数量（2106/3060/2396）
    ST_HUMAN_ACTION_PARAM_MESH_MODEL_VERTEX_NUM = 24,
    /// 设置face mesh是否需要内缩282关键点坐标(只对face mesh有效，360度mesh不需要，0：不需要内缩，1：需要内缩)
    ST_HUMAN_ACTION_PARAM_FACE_MESH_NARROW_LANDMARK = 25,
    ///// 设置face mesh是否需要计算边界点(0：不需要计算边界点，1：需要计算边界点）
    //ST_HUMAN_ACTION_PARAM_FACE_MESH_CONTOUR = 26,

	// 手部参数
    /// 设置检测到的最大手数目N(默认值2, 最大值32),持续track已检测到的N个hand直到人脸数小于N再继续做detect.值越大,检测到的hand数目越多,但相应耗时越长. 如果当前手数目达到上限，检测线程将休息
    ST_HUMAN_ACTION_PARAM_HAND_LIMIT = 101,
    /// 设置手势检测每多少帧进行一次 detect (默认有手时30帧detect一次, 无手时10(30/3)帧detect一次). 值越大,cpu占用率越低, 但检测出新人脸的时间越长.
    ST_HUMAN_ACTION_PARAM_HAND_DETECT_INTERVAL = 102,
    /// 设置手势隔帧检测（对上一帧结果做拷贝），目的是减少耗时。默认每帧检测一次. 最多每10帧检测一次. 开启隔帧检测后, 只能对拷贝出来的检测结果做后处理.
    ST_HUMAN_ACTION_PARAM_HAND_PROCESS_INTERVAL = 103,
    /// 设置手检测的阈值[0.0,1.0]
	ST_HUMAN_ACTION_PARAM_HAND_THRESHOLD = 104,

	/// 设置手骨架检测的阈值[0.0,1.0]
	ST_HUMAN_ACTION_PARAM_HAND_SKELETON_THRESHOLD = 110,

	//  肢体参数
    /// 设置检测到的最大肢体数目N(默认值1),持续track已检测到的N个肢体直到肢体数小于N再继续做detect.值越大,检测到的body数目越多,但相应耗时越长. 如果当前肢体数目达到上限，检测线程将休息
    ST_HUMAN_ACTION_PARAM_BODY_LIMIT = 200,
    /// 设置肢体关键点检测每多少帧进行一次 detect (默认有肢体时30帧detect一次, 无body时10(30/3)帧detect一次). 值越大,cpu占用率越低, 但检测出新body的时间越长.
    ST_HUMAN_ACTION_PARAM_BODY_DETECT_INTERVAL = 201,
    /// 设置肢体隔帧检测（对上一帧结果做拷贝），目的是减少耗时。默认每帧检测一次. 最多每10帧检测一次. 开启隔帧检测后, 只能对拷贝出来的检测结果做后处理.
    ST_HUMAN_ACTION_PARAM_BODY_PROCESS_INTERVAL = 202,
    /// 设置身体检测的阈值[0.0，1.0]
	ST_HUMAN_ACTION_PARAM_BODY_THRESHOLD = 203,
    /// 已废弃 设置是否根据肢体信息检测摄像头运动状态 (0: 不检测; 1: 检测. 默认检测肢体轮廓点时检测摄像头运动状态)
    //ST_HUMAN_ACTION_PARAM_DETECT_CAMERA_MOTION_WITH_BODY = 203,
    /// 输入真实身高，单位为米，3D骨架乘以身高（整体缩放），得到真实的物理尺度，仅用于儿童肢体检测
	ST_HUMAN_ACTION_PARAM_BODY_STATURE = 210,

	// 人头分割参数
	/// 设置头部分割检测结果灰度图的方向是否需要旋转（0: 不旋转, 保持竖直; 1: 旋转, 方向和输入图片一致. 默认不旋转)
	ST_HUMAN_ACTION_PARAM_HEAD_SEGMENT_RESULT_ROTATE = 300,
	/// 设置人头分割边界区域上限阈值.开启温度系数时设置无效
	ST_HUMAN_ACTION_PARAM_HEAD_SEGMENT_MAX_THRESHOLD = 301,
	/// 设置人头分割边界区域下限阈值 开启温度系数时设置无效
	ST_HUMAN_ACTION_PARAM_HEAD_SEGMENT_MIN_THRESHOLD = 302,
	/// 头部分割后处理长边的长度[10,长边长度](默认长边240,短边=长边/原始图像长边*原始图像短边).值越大,头部分割后处理耗时越长,边缘部分效果越好.
	ST_HUMAN_ACTION_PARAM_HEAD_SEGMENT_MAX_SIZE = 303,
    /// 设置最大人头分割个数，默认支持一个人头分割，face_id为人脸id; 若支持多个人头分割，则face id为-1.
    ST_HUMAN_ACTION_PARAM_HEAD_SEGMENT_MAX_COUNT = 304,
	/// 设置头部分割检测结果边缘模糊程度 取值范围0-1 视频版默认0.5, 图片版默认是1
	ST_HUMAN_ACTION_PARAM_HEAD_SEGMENT_RESULT_BLUR = 305,
	/// 设置头部分割检测结果边缘模糊程度需保证开启温度系数，大于0.5为开启，小于0.5为关闭，关闭状态下使用卡阈值模式得到边界，默认状态为开启
	ST_HUMAN_ACTION_PARAM_HEAD_SEGMENT_USE_TEMPERATURE=306,

	// 背景分割/人像分割参数
    /// 输出的background结果中长边的长度[10,长边长度](默认长边为模型内部处理的长边，若设置会做resize处理输出).值越大,背景分割的耗时越长,边缘部分效果越好.值为0还原为默认值.
    ST_HUMAN_ACTION_PARAM_BACKGROUND_MAX_SIZE = 400,
    /// 背景分割羽化程度[0,1](默认值0.35),0 完全不羽化,1羽化程度最高,在strenth较小时,羽化程度基本不变.值越大,前景与背景之间的过度边缘部分越宽.
    /// 备注：只对1.5.0 人像分割模型有效
    ST_HUMAN_ACTION_PARAM_BACKGROUND_BLUR_STRENGTH = 401,//
    /// 设置前后背景检测结果灰度图的方向是否需要旋转（0: 不旋转, 保持竖直; 1: 旋转, 方向和输入图片一致. 默认不旋转)
    ST_HUMAN_ACTION_PARAM_BACKGROUND_RESULT_ROTATE = 402,
    /// 设置背景分割边界区域上限阈值. 开启温度系数时设置无效
    ST_HUMAN_ACTION_PARAM_SEGMENT_MAX_THRESHOLD = 403,
    /// 设置背景分割边界区域下限阈值 开启温度系数时设置无效
    ST_HUMAN_ACTION_PARAM_SEGMENT_MIN_THRESHOLD = 404,
	/// 设置背景分割检测间隔
	ST_HUMAN_ACTION_PARAM_BACKGROUND_PROCESS_INTERVAL = 405,
//	ST_HUMAN_ACTION_PARAM_SEGMENT_KERNAL_TYPE = 406, 已废弃
    /// 设置背景分割检测结果边缘模糊程度 取值范围0-1, 视频版默认是0.5 图片版默认是1
    ST_HUMAN_ACTION_PARAM_BACKGROUND_SEGMENT_RESULT_BLUR = 407,
	/// 设置背景分割检测结果边缘模糊程度需保证开启温度系数，大于0.5为开启，小于0.5为关闭，关闭状态下使用卡阈值模式得到边界，默认状态为开启
	ST_HUMAN_ACTION_PARAM_BACKGROUND_SEGMENT_USE_TEMPERATURE = 408,

// 头发分割参数
    /// 头发结果中长边的长度[10,长边长度](默认长边240,短边=长边/原始图像长边*原始图像短边).值越大,头发分割的耗时越长,边缘部分效果越好.
    ST_HUMAN_ACTION_PARAM_HAIR_MAX_SIZE = 410,
    /// 头发分割羽化程度[0,1](默认值0.35),0 完全不羽化,1羽化程度最高,在strenth较小时,羽化程度基本不变.值越大,过度边缘部分越宽.
    ST_HUMAN_ACTION_PARAM_HAIR_BLUR_STRENGTH = 411,  // 无效,可删除
    /// 设置头发灰度图的方向是否需要旋转（0: 不旋转, 保持竖直; 1: 旋转, 方向和输入图片一致. 默认0不旋转)
    ST_HUMAN_ACTION_PARAM_HAIR_RESULT_ROTATE = 412,
    /// 设置头发分割隔帧检测（对上一帧结果做拷贝），目的是减少耗时。默认每帧检测一次. 最多每10帧检测一次. 开启隔帧检测后, 只能对拷贝出来的检测结果做后处理.
    ST_HUMAN_ACTION_PARAM_HAIR_PROCESS_INTERVAL = 413,  // 建议删除
    /// 设置头发分割边界区域上限阈值.开启温度系数时设置无效
    ST_HUMAN_ACTION_PARAM_HAIR_SEGMENT_MAX_THRESHOLD = 414,
    /// 设置头发分割边界区域下限阈值 开启温度系数时设置无效
    ST_HUMAN_ACTION_PARAM_HAIR_SEGMENT_MIN_THRESHOLD = 415,
	/// 设置头发分割检测结果边缘模糊程度 取值范围0-1 视频版默认是0.5，图片版默认是1
	ST_HUMAN_ACTION_PARAM_HAIR_SEGMENT_RESULT_BLUR = 416,
	/// 设置头发分割检测结果边缘模糊程度需保证开启温度系数，大于0.5为开启，小于0.5为关闭，关闭状态下使用卡阈值模式得到边界，默认状态为开启
	ST_HUMAN_ACTION_PARAM_HAIR_SEGMENT_USE_TEMPERATURE = 417,

// 多类分割参数
    /// 输出的multisegment结果中长边的长度.
    ST_HUMAN_ACTION_PARAM_MULTI_SEGMENT_MAX_SIZE = 420,
    /// 设置多类分割检测结果灰度图的方向是否需要旋转（0: 不旋转, 保持竖直; 1: 旋转, 方向和输入图片一致. 默认不旋转)
    ST_HUMAN_ACTION_PARAM_MULTI_SEGMENT_RESULT_ROTATE = 421,



// 皮肤分割参数
    /// 输出的皮肤分割结果中长边的长度.
    ST_HUMAN_ACTION_PARAM_SKIN_SEGMENT_MAX_SIZE = 430,
    /// 设置皮肤分割边界区域上限阈值.开启温度系数时设置无效
    ST_HUMAN_ACTION_PARAM_SKIN_SEGMENT_MAX_THRESHOLD = 431,
    /// 设置皮肤分割边界区域下限阈值，开启温度系数时设置无效
    ST_HUMAN_ACTION_PARAM_SKIN_SEGMENT_MIN_THRESHOLD = 432,
    /// 设置皮肤分割检测结果灰度图的方向是否需要旋转（0: 不旋转, 保持竖直; 1: 旋转, 方向和输入图片一致. 默认不旋转)
    ST_HUMAN_ACTION_PARAM_SKIN_SEGMENT_RESULT_ROTATE = 433,
	/// 设置皮肤分割检测结果边缘模糊程度 取值范围0-1 默认0.5
	ST_HUMAN_ACTION_PARAM_SKIN_SEGMENT_RESULT_BLUR = 434,
	/// 设置皮肤分割检测结果边缘模糊程度需保证开启温度系数，大于0.5为开启，小于0.5为关闭，关闭状态下使用卡阈值模式得到边界，默认状态为开启
	ST_HUMAN_ACTION_PARAM_SKIN_SEGMENT_USE_TEMPERATURE = 435,

// 嘴唇分割
    /// 设置嘴唇分割检测结果灰度图的方向是否需要旋转（0: 不旋转, 保持竖直; 1: 旋转, 方向和输入图片一致. 默认不旋转)
    ST_HUMAN_ACTION_PARAM_MOUTH_PARSE_RESULT_ROTATE = 450,
	// 不支持设置输出图像长边长度, 不支持调节阈值(默认是0和1)

// 面部遮挡分割参数
	/// 设置面部遮挡检测结果灰度图的方向是否需要旋转（0: 不旋转, 保持竖直; 1: 旋转, 方向和输入图片一致. 默认不旋转)
	ST_HUMAN_ACTION_PARAM_FACE_OCCLUSION_SEGMENT_RESULT_ROTATE = 460,
	/// 设置面部遮挡分割边界区域上限阈值，开启温度系数时设置无效
	ST_HUMAN_ACTION_PARAM_FACE_OCCLUSION_SEGMENT_MAX_THRESHOLD = 461,
	/// 设置面部遮挡分割边界区域下限阈值，开启温度系数时设置无效
	ST_HUMAN_ACTION_PARAM_FACE_OCCLUSION_SEGMENT_MIN_THRESHOLD = 462,
	/// 面部遮挡分割后处理长边的长度[10,长边长度](默认长边240,短边=长边/原始图像长边*原始图像短边).值越大,面部遮挡分割后处理耗时越长,边缘部分效果越好.
	ST_HUMAN_ACTION_PARAM_FACE_OCCLUSION_SEGMENT_MAX_SIZE = 463,
	// 不支持设置输出图像长边长度

	/// 设置面部遮挡分割检测结果边缘模糊程度 默认参数0.5
	ST_HUMAN_ACTION_PARAM_FACE_OCCLUSION_SEGMENT_RESULT_BLUR = 464,
	/// 设置面部遮挡分割检测结果边缘模糊程度需保证开启温度系数，大于0.5为开启，小于0.5为关闭，关闭状态下使用卡阈值模式得到边界，默认状态为开启
	ST_HUMAN_ACTION_PARAM_FACE_OCCLUSION_SEGMENT_USE_TEMPERATURE = 467,

	// 通用参数
	/// 设置预处理图像大小
	ST_HUMAN_ACTION_PARAM_PREPROCESS_MAX_SIZE = 500, //
	/// 摄像头x方向上的视场角，单位为度，3d点会需要
    ST_HUMAN_ACTION_PARAM_CAM_FOVX = 211,

    //  天空分割参数
	//设置目标特征图像 目前已废弃
	ST_HUMAN_ACTION_PARAM_SKY_TARGET_IMAGE= 509,
	/// 输出的sky结果中长边的长度
	ST_HUMAN_ACTION_PARAM_SKY_MAX_SIZE = 510,
	/// 天空分割检测结果灰度图的方向是否需要旋转
	ST_HUMAN_ACTION_PARAM_SKY_RESULT_ROTATE = 511,
	/// 设置天空分割边界区域上限阈值，开启温度系数时设置无效
	ST_HUMAN_ACTION_PARAM_SKY_SEGMENT_MAX_THRESHOLD = 512,
	/// 设置天空分割边界区域下限阈值，开启温度系数时设置无效
	ST_HUMAN_ACTION_PARAM_SKY_SEGMENT_MIN_THRESHOLD = 513,
	/// 设置天空分割检测结果边缘模糊程度 取值范围0-1，视频版默认参数0.5，图片版默认参数1
	ST_HUMAN_ACTION_PARAM_SKY_SEGMENT_RESULT_BLUR = 508,
	/// 设置天空分割检测结果边缘模糊程度需保证开启温度系数，大于0.5为开启，小于0.5为关闭，关闭状态下使用卡阈值模式得到边界，默认状态为开启
	ST_HUMAN_ACTION_PARAM_SKY_SEGMENT_USE_TEMPERATURE = 507,
	/// 使用CPU进行refine操作,默认是使用（＞0.5），当输入参数小于等于0.5时不使用
	ST_HUMAN_ACTION_PARAM_SKY_SEGMENT_REFINE_CPU_PROCESS = 514,

    //  深度估计参数
	/// 输出的深度估计结果中长边的长度
	ST_HUMAN_ACTION_PARAM_DEPTH_ESTIMATION_MAX_SIZE = 515,

    // 指甲检测
	/// 设置检测到的最大目标数目N,有效范围为[1, 32], 返回的值可能比输入的值要小. 默认值为10.
	ST_HUMAN_ACTION_PARAM_NAIL_LIMIT = 602,
	/// 设置指甲检测每多少帧进行一次 detect (默认指甲时30帧detect一次, 无指甲时10(30/3)帧detect一次). 值越大,cpu占用率越低, 但检测出新对象的时间越长.
	ST_HUMAN_ACTION_PARAM_NAIL_DETECT_INTERVAL = 603,
	/// 设置指甲检测的阈值[0.0,1.0]， 默认是
	ST_HUMAN_ACTION_PARAM_NAIL_THRESHOLD = 604,
	/// 指甲平滑参数，取值范围[0,-) 默认参数是0.1
	ST_HUMAN_ACTION_PARAM_NAIL_SMOOTH = 605,

    //	脚部参数
	/// 设置检测到的最大脚的个数
	ST_HUMAN_ACTION_PARAM_FOOT_MAX_LIMIT = 700,
	/// 设置脚部检测每多少帧进行一次 detect (默认有脚时30帧detect一次, 无脚时10(30/3)帧detect一次). 值越大,cpu占用率越低, 但检测出新对象的时间越长.
	ST_HUMAN_ACTION_PARAM_FOOT_DETECT_INTERVAL = 701,
	/// 设置检测阈值[0.0,1.0]
	ST_HUMAN_ACTION_PARAM_FOOT_THRESHOLD = 702,
	/// 设置裤腿分割检测结果边缘模糊程度，取值范围0-1，默认参数0.5
	ST_HUMAN_ACTION_PARAM_TROUSER_LEG_SEGMENT_BLUR = 750,
	/// 设置分割检测结果边缘模糊程度需保证开启温度系数，大于0.5为开启，小于0.5为关闭，关闭状态下使用卡阈值模式得到边界，默认状态为开启
	ST_HUMAN_ACTION_PARAM_TROUSER_LEG_SEGMENT_USE_TEMPERATURE = 751,


    // 衣物分割
    // 设置衣物分割检测结果灰度图的方向是否需要旋转（0: 不旋转, 保持竖直; 1: 旋转, 方向和输入图片一致. 默认不旋转)
    ST_HUMAN_ACTION_PARAM_CLOTH_SEGMENT_RESULT_ROTATE = 800,
    // 设置衣物分割长边的长度
    ST_HUMAN_ACTION_PARAM_CLOTH_SEGMENT_MAX_SIZE = 801,
    // 设置衣物分割检测结果边缘模糊程度 取值范围0-1，视频版默认参数0.5，图片版默认参数1
    ST_HUMAN_ACTION_PARAM_CLOTH_SEGMENT_RESULT_BLUR = 802,
    // 设置衣物分割检测结果边缘模糊程度需保证开启温度系数，大于0.5为开启，小于0.5为关闭，关闭状态下使用卡阈值模式得到边界，默认状态为开启
    ST_HUMAN_ACTION_PARAM_CLOTH_SEGMENT_USE_TEMPERATURE = 803,
    // 设置衣物分割边界区域上限阈值，开启温度系数时设置无效
    ST_HUMAN_ACTION_PARAM_CLOTH_SEGMENT_MAX_THRESHOLD = 804,
    // 设置衣物分割边界区域下限阈值，开启温度系数时设置无效
    ST_HUMAN_ACTION_PARAM_CLOTH_SEGMENT_MIN_THRESHOLD = 805,

	// 手腕检测
	/// 设置检测到的最大目标数目N,有效范围为[1, 32], 返回的值可能比输入的值要小. 默认值为10.
	ST_HUMAN_ACTION_PARAM_WRIST_LIMIT = 900,
	/// 设置手腕检测每多少帧进行一次 detect (默认30帧detect一次, 无手腕10(30/3)帧detect一次). 值越大,cpu占用率越低, 但检测出新对象的时间越长.
	ST_HUMAN_ACTION_PARAM_WRIST_DETECT_INTERVAL = 901,
	///设置手腕检测的roi参数，默认全0，设置检测roi区域，需enable roi需调用st_mobile_human_action_set_roi，默认全0，不开启
	ST_HUMAN_ACTION_PARAM_WRIST_ROI=906,
	///开启手腕检测roi设置，默认不打开
	ST_HUMAN_ACTION_PARAM_WRIST_ENABLE_ROI=907,
	///打开roi设置之后，resize的ratio比例，(0,1]
	ST_HUMAN_ACTION_PARAM_WRIST_ROI_RATIO=909,
	///手表佩戴位置相比手腕宽度的比例 ,默认值是1.0
	ST_HUMAN_ACTION_PARAM_WRIST_WRIST_RATIO=911,
	///是否使用get3dpose接口，默认不使用，使用时，human action detect不计算pose
	ST_HUMAN_ACTION_PARAM_WRIST_USE_GETPOSE=926,
} st_human_action_param_type;

/// @brief 设置human_action参数
/// @param[in] handle 已初始化的human_action句柄
/// @param[in] type human_action参数关键字,例如ST_HUMAN_ACTION_PARAM_BACKGROUND_MAX_SIZE
/// @param[in] value 参数取值
/// @return 成功返回ST_OK,错误则返回错误码,错误码定义在st_mobile_common.h 中,如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_human_action_setparam(
    st_handle_t handle,
	st_human_action_param_type type,
    float value
);

/// @brief 获取human_action参数
/// @param[in] handle 已初始化的human_action句柄
/// @param[in] type human_action参数关键字,和setparam对应
/// @param[out] value 参数取值
/// @return 成功返回ST_OK,错误则返回错误码,错误码定义在st_mobile_common.h 中,如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_human_action_getparam(
    st_handle_t handle,
    st_human_action_param_type type,
    float* value
);
//  以下为辅助函数

/// @brief 运行时检查是否有能力检测一些功能
/// @param[in] handle 已初始化的人体行为句柄
/// @param[in] detect_config 需要检测的人体行为,例如ST_MOBILE_EYE_BLINK | ST_MOBILE_MOUTH_AH | ST_MOBILE_HAND_LOVE | ST_MOBILE_SEG_BACKGROUND
/// @param[out] actual_detect_config 输入的检测config中实际可以检测出的检测项
/// @return 成功返回ST_OK,失败返回其他错误码,错误码常见license没有授权，对应模型没有加载等
ST_SDK_API
st_result_t st_mobile_human_action_can_detect(
	st_handle_t handle,
	unsigned long long input_detect_config,
	unsigned long long *actual_detect_config
);

/// @brief 镜像human_action检测结果. 隔帧检测时, 需要将检测结果拷贝出来再镜像
/// @param[in] image_width 检测human_action的图像的宽度(以像素为单位)
/// @param[in,out] p_human_action 需要镜像的human_action检测结果
ST_SDK_API void
st_mobile_human_action_mirror(
    int image_width,
    st_mobile_human_action_t *p_human_action
);


/// @brief 旋转human_action检测结果.
/// @param[in] image_width 检测human_action的图像的宽度(以像素为单位)
/// @param[in] image_height 检测human_action的图像的宽度(以像素为单位)
/// @param[in] orientation 顺时针旋转的角度
/// @param[in] b_rotate_image 是否旋转图片
/// @param[in,out] p_human_action 需要旋转的human_action检测结果
ST_SDK_API void
st_mobile_human_action_rotate(
	int image_width,
	int image_height,
	st_rotate_type orientation,
	bool b_rotate_image,
	st_mobile_human_action_t* p_human_action
);

/// @brief 放大/缩小human_action检测结果.背景图像不缩放
/// @param[in] scale 缩放的尺度
/// @param[in,out] p_human_action 需要缩放的human_action检测结果
ST_SDK_API
void st_mobile_human_action_resize(
	float scale,
	st_mobile_human_action_t* p_human_action
);

/// @brief 拷贝human_action检测结果.
/// @param[in] p_human_action_src 需要拷贝的human_action检测结果
/// @param[out] p_human_action_dst 需要拷贝的human_action检测结果.注意初始化成员,如果指针非空, 默认是已被分配过内存, 不会重新分配内存
ST_SDK_API
void st_mobile_human_action_copy(
    const st_mobile_human_action_t * p_human_action_src,
    st_mobile_human_action_t * p_human_action_dst
);

/// @brief 删除human_action 结果, 只能删除st_mobile_human_action_copy的输出结果
/// @param[in] p_human_action 需要删除的human_action检测结果
ST_SDK_API
void st_mobile_human_action_delete(
    st_mobile_human_action_t * p_human_action
);
/// @brief 表情动作类型定义
typedef enum{
	// 脸部动作
	ST_MOBILE_EXPRESSION_EYE_BLINK = 1,  ///< 眨眼
	ST_MOBILE_EXPRESSION_MOUTH_AH = 2, ///< 嘴巴大张
	ST_MOBILE_EXPRESSION_HEAD_YAW = 3, ///< 摇头
	ST_MOBILE_EXPRESSION_HEAD_PITCH = 4,  ///< 点头
	ST_MOBILE_EXPRESSION_BROW_JUMP = 5,  ///< 挑眉
	// 手
	ST_MOBILE_EXPRESSION_HAND_OK = 9,  ///< OK手势
	ST_MOBILE_EXPRESSION_HAND_SCISSOR = 10,  ///< 剪刀手
	ST_MOBILE_EXPRESSION_HAND_GOOD = 11,  ///< 大拇哥
	ST_MOBILE_EXPRESSION_HAND_PALM = 12,  ///< 手掌
	ST_MOBILE_EXPRESSION_HAND_PISTOL = 13,  ///< 手枪手势
	ST_MOBILE_EXPRESSION_HAND_LOVE = 14,  ///< 爱心手势
	ST_MOBILE_EXPRESSION_HAND_HOLDUP = 15,  ///< 托手手势
	ST_MOBILE_EXPRESSION_HAND_CONGRATULATE = 17,  ///< 恭贺（抱拳）
	ST_MOBILE_EXPRESSION_HAND_FINGER_HEART = 18,  ///< 单手比爱心
	ST_MOBILE_EXPRESSION_HAND_FINGER_INDEX = 20,  ///< 食指指尖
	ST_MOBILE_EXPRESSION_HAND_FIST = 21,  ///< 拳头
	ST_MOBILE_EXPRESSION_HAND_666 = 22,  ///< 666
	ST_MOBILE_EXPRESSION_HAND_BLESS = 23,  ///< 双手合十
	ST_MOBILE_EXPRESSION_HAND_ILOVEYOU = 24,  ///< 手势ILoveYou
	ST_MOBILE_EXPRESSION_HAND_SSH = 25,       ///< 嘘
    ST_MOBILE_EXPRESSION_HAND_THREE = 26,     ///< 手势3
    ST_MOBILE_EXPRESSION_HAND_FOUR = 27,      ///< 手势4
	// 头状态
	ST_MOBILE_EXPRESSION_HEAD_NORMAL = 65, ///< 头正向
	ST_MOBILE_EXPRESSION_SIDE_FACE_LEFT = 66, ///< 头向左侧偏
	ST_MOBILE_EXPRESSION_SIDE_FACE_RIGHT = 67, ///< 头向右侧偏
	ST_MOBILE_EXPRESSION_TILTED_FACE_LEFT = 68, ///< 头向左侧倾斜
	ST_MOBILE_EXPRESSION_TILTED_FACE_RIGHT = 69, ///< 头向右侧倾斜
	ST_MOBILE_EXPRESSION_HEAD_RISE = 70,         ///< 抬头
	ST_MOBILE_EXPRESSION_HEAD_LOWER = 71,        ///< 低头
	// 眼状态
	ST_MOBILE_EXPRESSION_TWO_EYE_CLOSE = 85, ///< 两眼都闭
	ST_MOBILE_EXPRESSION_TWO_EYE_OPEN = 86, ///< 两眼都睁
	ST_MOBILE_EXPRESSION_LEFTEYE_OPEN_RIGHTEYE_CLOSE = 87, ///< 左眼睁右眼闭
	ST_MOBILE_EXPRESSION_LEFTEYE_CLOSE_RIGHTEYE_OPEN = 88, ///< 左眼闭右眼睁
	// 嘴状态
	ST_MOBILE_EXPRESSION_MOUTH_OPEN = 105, ///< 张嘴
	ST_MOBILE_EXPRESSION_MOUTH_CLOSE = 106, ///< 闭嘴
	ST_MOBILE_EXPRESSION_FACE_LIPS_UPWARD = 107, ///< 嘴角上扬
	ST_MOBILE_EXPRESSION_FACE_LIPS_POUTED = 108, ///< 嘟嘴
	ST_MOBILE_EXPRESSION_FACE_LIPS_CURL_LEFT = 109,   ///< 左撇嘴
	ST_MOBILE_EXPRESSION_FACE_LIPS_CURL_RIGHT = 110,   ///< 右撇嘴

	ST_MOBILE_EXPRESSION_COUNT = 128,

	// 以下只能用于set_expression接口
	ST_MOBILE_EXPRESSION_FACE_ALL = 257,   ///< 所有脸部动作
	ST_MOBILE_EXPRESSION_HAND_ALL = 258   ///< 所有手部动作
}ST_MOBILE_EXPRESSION;

/// @brief 根据human_action结果获取expression动作信息. 在st_mobile_human_action_detect之后调用
/// @param[in] human_action 输入human_action_detect结果
/// @param[in] orientation 人脸方向
/// @param[in] b_mirror 是否需要镜像expression结果
/// @param[out] expressions_result 用户分配内存，返回检测动作结果数组,动作有效true，无效false
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_get_expression(
st_mobile_human_action_t* human_action,
st_rotate_type orientation, bool b_mirror,
bool expressions_result[ST_MOBILE_EXPRESSION_COUNT]
);

/// @brief 根据human_action结果获取3dPose. 在st_mobile_human_action_detect及后处理之后调用
/// @param[in] handle 输入human_action_detect检测的handle
/// @param[in] human_action 后处理（镜像，旋转，缩放等）之后的human action检测结果信息
/// @param[in] detect_config计算检测的3DPose，一目前仅支持ST_MOBILE_DETECT_FOOT和ST_MOBILE_WRIST_DETECT
/// @param[in] image_width需要处理的图像的width信息，若进行图像（或human action)变换（镜像，旋转，缩放等），需要处理后的结果
/// @param[in] image_height需要处理的图像的height信息，若进行图像（或human action)的变换（镜像，旋转，缩放等），需要处理后的结果
/// @param[in] fov 相机的视场角，单位是度
/// @param[in] rotate 图像旋转角度
/// @param[in/out] info_3d 3dPose信息，需预先分配内存,若detect_config为ST_MOBILE_DETECT_FOOT|ST_MOBILE_WRIST_DETECT，至少分配wrist_count+foot_count
/// @return 成功返回ST_OK, 失败返回其他错误码, 错误码定义在st_mobile_common.h中, 如ST_E_FAIL等
ST_SDK_API st_result_t
st_mobile_human_action_get_3dPose(
st_handle_t handle,
st_mobile_human_action_t* human_action,
unsigned long long detect_config,
int image_width,
int image_height,
float fov,
st_rotate_type rotate,
st_human_pose_t *info_3d
);

/// @brief 设置检测的roi参数，检测时只在roi范围内检测
/// @param[in] handle 输入human_action_detect检测的handle
/// @param[in] detect_config计算检测的3DPose，目前仅支持ST_HUMAN_ACTION_PARAM_WRIST_ROI
/// @param[in] roi信息，设置检测的范围
ST_SDK_API st_result_t
st_mobile_human_action_set_roi(
st_handle_t handle,
st_human_action_param_type type,
st_rect_t roi
);

/// @brief 设置expression动作阈值
/// @param[in] detect_expression 需要设置阈值的检测动作. 目前仅支持face相关的阈值，可以配置为ST_MOBILE_EXPRESSION_FACE_LIPS_POUTE等
/// @param[in] threshold 阈值数值[0,1]，阈值越大，误检越少，漏检越多
ST_SDK_API st_result_t
st_mobile_set_expression_threshold(
ST_MOBILE_EXPRESSION detect_expression,
float threshold
);

/// @param[in] handle 已初始化的human_action句柄
/// @param[in] p_face 输入检测结果中的一个人脸结构指针
/// @param[in] orientation 人脸方向
/// @param[in] width 检测关键点时的图像宽度
/// @param[in] height 检测关键点时的图像高度
/// @param[in] fov 垂直fov参数，相机的内参，单位是角度，比如60。
/// @param[out] p_distance 输出检测距离的结果,单位为米
/// @return  成功返回ST_OK, 失败返回其他错误码
ST_SDK_API st_result_t
st_mobile_human_action_calc_face_distance(
st_handle_t handle,
const st_mobile_face_t *p_face,
st_rotate_type orientation,
int width, int height, float fov,
float  *p_distance
);

/// @brief 设置Human action各个属性阈值
/// @param[in] handle 已初始化的human action句柄
/// @param[in] config 具体设置哪些阈值，为这几种：
///             ST_MOBILE_EYE_BLINK 眨眼动作
///             ST_MOBILE_MOUTH_AH 张嘴动作
///             ST_MOBILE_HEAD_YAW 摇头动作
///             ST_MOBILE_HEAD_PITCH 点头动作
///             ST_MOBILE_BROW_JUMP 挑眉毛动作
///             ST_MOBILE_FACE_LIPS_UPWARD 嘴角上扬动作
///             ST_MOBILE_FACE_LIPS_POUTED 嘟嘴动作
/// @param[in] threshold 设置config指定类型的阈值，置信度为[0,1], 默认阈值为0.5
/// @return  成功返回ST_OK, 失败返回其他错误码
ST_SDK_API st_result_t
st_mobile_set_human_action_threshold(
    st_handle_t handle,
    unsigned long long config,
    float threshold
);

/// @brief 获取已经设置的Human action各个属性阈值
/// @param[in] handle 已初始化的human action句柄
/// @param[in] config 与st_mobile_set_human_action_threshold中config一致
/// @param[in] threshold 取得的阈值
/// @return  成功返回ST_OK, 失败返回其他错误码
ST_SDK_API st_result_t
st_mobile_get_human_action_threshold(
    st_handle_t handle,
    unsigned long long config,
    float* threshold
);


// 以下是mesh相关的使用函数，加载模型后需要调用相关接口获取相关信息

/// @brief mesh类型
typedef enum {
	ST_MOBILE_FACE_MESH = 1,  ///< face mesh 类型
	ST_MOBILE_HEAD_MESH = 2   ///< 360度mesh 类型
} st_mobile_mesh_type;

/// @brief mesh部位
typedef enum {
	ST_MOBILE_MESH_PART_FACE = 1,     ///< 人脸部位
	ST_MOBILE_MESH_PART_EYE = 2,      ///< 眼睛部位
	ST_MOBILE_MESH_PART_MOUTH = 4,    ///< 嘴巴部位
	ST_MOBILE_MESH_PART_SKULL = 8,    ///< 后脑勺部位
	ST_MOBILE_MESH_PART_EAR = 16,     ///< 耳朵部位
	ST_MOBILE_MESH_PART_NECK = 32,    ///< 脖子部位
	ST_MOBILE_MESH_PART_EYEBROW = 64  ///< 眉毛部位
} st_mobile_mesh_part;

/// @brief 三角面片的顶点索引
typedef struct st_face_mesh_index_t {
	int v1;
	int v2;
	int v3;
}st_face_mesh_index_t, *p_st_face_mesh_index_t;

/// @brief 3d mesh 三角面片索引结果
typedef struct st_mobile_face_mesh_list_t {
	st_face_mesh_index_t* p_face_mesh_index;
	int face_mesh_list_count;
}st_mobile_face_mesh_list_t, *p_st_mobile_face_mesh_list_t;

/// @brief 获取mesh三角拓扑面片索引信息，在加载模型后调用一次来获取索引信息，或者在每次设置mesh模式后调用一次来更新索引信息
/// @param[in] handle 已初始化的human_action句柄
/// @param[in] mesh_type mesh类型： face mesh 或者 head mesh
/// @param[out] p_mesh mesh面片索引结果，底层分配内存
/// @param[out] p_mouth_eye_mesh mesh嘴巴和眼睛面片索引结果，底层分配内存
/// @return  成功返回ST_OK, 失败返回其他错误码
ST_SDK_API st_result_t
st_mobile_human_action_get_mesh_list(
	st_handle_t handle,
	st_mobile_mesh_type mesh_type,
	st_mobile_face_mesh_list_t* p_mesh,
	st_mobile_face_mesh_list_t* p_mouth_eye_mesh
);

/// @brief 加载mesh的标准人脸obj模型，用于内部的算法处理与mesh输出
/// @param[in] handle 已初始化的human_action句柄
/// @param[in] p_obj_path obj模型的文件路径
/// @param[in] mesh_type mesh类型： face mesh 或者 head mesh
/// @return  成功返回ST_OK, 失败返回其他错误码
ST_SDK_API st_result_t
st_mobile_human_action_load_standard_mesh_obj(
	st_handle_t handle,
	const char* p_obj_path,
	st_mobile_mesh_type mesh_type
);

/// @brief 加载mesh的标准人脸obj模型，用于内部的算法处理与mesh输出
/// @param[in] handle 已初始化的human_action句柄
/// @param[in] p_buffer 已经加载到内存中的obj模型buffer
/// @param[in] buffer_len buffer的字节数
/// @param[in] mesh_type mesh类型： face mesh 或者 head mesh
/// @return  成功返回ST_OK, 失败返回其他错误码
ST_SDK_API st_result_t
st_mobile_human_action_load_standard_mesh_obj_from_buffer(
	st_handle_t handle,
	const char* p_buffer,
	int buffer_len,
	st_mobile_mesh_type mesh_type
);

/// @brief 获取mesh关键点中的人脸关键点下标，加载模型后调用，或者在重新加载模型后调用
/// @param[in] handle 已初始化的human_action句柄
/// @param[in] mesh_type mesh类型： face mesh 或者 head mesh
/// @param[in] point_count 人脸关键点下标的数量：106或240
/// @param[out] index 人脸关键点的下标，上层分配内存
/// @return  成功返回ST_OK, 失败返回其他错误码
ST_SDK_API st_result_t
st_mobile_human_action_get_face_index_from_mesh(
st_handle_t handle,
st_mobile_mesh_type mesh_type,
int point_count,
int* index
);

///// @brief 计算mesh轮廓点
///// @param[in] p_face_mesh mesh关键点信息，轮廓点计算结果保存在结构体中
///// @param[in] model_type 模型类型，2106/2396/3060，目前只有2396支持轮廓点
///// @param[in] affine_mat 仿射变换矩阵，人脸检测模型内部参数
///// @param[in] contour_mode 轮廓点模式，0: no contour, 1: face contour, 2: head contour
///// @return  成功返回ST_OK, 失败返回其他错误码
//ST_SDK_API st_result_t
//st_mobile_human_action_calc_facemesh_contour(
//st_mobile_face_mesh_t* p_face_mesh,
//int model_type,
//float affine_mat[3][3],
//int contour_mode
//);



/// @brief body四元数结果
typedef struct st_mobile_body_avatar_t {
	st_quaternion_t *p_body_quat_array;     ///< 人体四元数数组
	int body_quat_count;                    /// < 人体四元数组个数(0 或 79)
    bool is_idle;                           /// 是否是idle状态
} st_mobile_body_avatar_t, *p_st_mobile_body_avatar_t;

#endif  // INCLUDE_STMOBILE_ST_MOBILE_HUMAN_ACTION_H_

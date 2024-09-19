package io.agora.hyextension;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import io.agora.base.internal.Logging;
import io.agora.hy.extension.ExtensionManager;
import io.agora.rtc2.RtcEngine;

/**
 * 寰语工具
 *
 * @author dwyue
 * @since 2022-01-23 10:28:51
 */
public class HyUtil {
    // 请在开放平台申请。测试使用，正式使用需要自己申请。

    /**
     * 应用标识
     */
    private String APP_ID = "";

    /**
     * API密钥
     */
    private String API_KEY = "";

    /**
     * API秘密
     */
    private String API_SECRET = "";

    /**
     * 标签
     */
    private final String TAG = "HyUtil";

    /**
     * {@link Parser}
     */
    private final Parser mParser = new Parser();

    /**
     * {@link IListener}。非null。
     */
    private final IListener mListener;

    /**
     * {@link RtcEngine}。非null。
     */
    private final RtcEngine mRtcEngine;

    /**
     * 构造
     *
     * @param rtcEngine {@link RtcEngine}
     */
    public HyUtil(String appId, String apiKey, String apiSecret,IListener listener, RtcEngine rtcEngine) {
        APP_ID = appId;
        API_KEY = apiKey;
        API_SECRET = apiSecret;
        mListener = listener;
        mRtcEngine = rtcEngine;
    }


    /**
     * 获取参数包装集
     *
     * @return 非空
     */
    public ParamWrap[] getParamWraps() {
        return new ParamWrap[]{
                new ParamWrap("中译英", "zh_cn", "mandarin", "ist_ed_open", 1, "wpgs", "cn", "en"),
                new ParamWrap("英译中", "zh_cn", "mandarin", "ist_ed_open", 3, "wpgs", "en", "cn"),
        };
    }

    /**
     * 启动倾听
     *
     * @param {@link ParamWrap}。非null。
     */
    public void start(ParamWrap paramWrap) {
        mParser.clear();

        String val = null;
        try {
            JSONObject rootJo = new JSONObject();
            // 公共对象。必选。
            JSONObject commonJo = new JSONObject();
            {
                // 应用标识。必选。
                commonJo.put("app_id", APP_ID);
                // API密钥。必选。
                commonJo.put("api_key", API_KEY);
                // API秘密。必选。
                commonJo.put("api_secret", API_SECRET);
            }
            rootJo.put("common", commonJo);
            // 语音转写对象。必选。
            JSONObject istJo = new JSONObject();
            {
                // URI。必选。
                istJo.put("uri", "wss://ist-api.xfyun.cn/v2/ist");
                // 请求对象。必选。
                JSONObject reqJo = new JSONObject();
                {
                    // 业务对象。必选。
                    JSONObject businessJo = new JSONObject();
                    {
                        // 语种。必选。
                        businessJo.put("language", paramWrap.mIstLanguage);
                        // 口音。必选。
                        businessJo.put("accent", paramWrap.mIstAccent);
                        // 领域。必选。
                        businessJo.put("domain", paramWrap.mIstDomain);
                        // 语言类型
                        // 值类型：int
                        // 值范围：
                        // 1：中英文模式，中文英文均可识别；
                        // 3：英文模式，只识别出英文
                        // 值默认：1
                        businessJo.put("language_type", paramWrap.mIstLanguageType);
                        // 动态修正
                        businessJo.put("dwa", paramWrap.mIstDwa);
                    }
                    reqJo.put("business", businessJo);
                }
                istJo.put("req", reqJo);
            }
            rootJo.put("ist", istJo);
            // 文本翻译对象。
            JSONObject itsJo = new JSONObject();
            {
                // URI。必选。
                itsJo.put("uri", "https://itrans.xfyun.cn/v2/its");
                // 请求对象。必选。
                JSONObject reqJo = new JSONObject();
                {
                    // 业务对象。必选。
                    JSONObject businessJo = new JSONObject();
                    {
                        // 源语种。必选。
                        businessJo.put("from", paramWrap.mItsFrom);
                        // 目标语种。必选。
                        businessJo.put("to", paramWrap.mItsTo);
                    }
                    reqJo.put("business", businessJo);
                }
                itsJo.put("req", reqJo);
            }
            rootJo.put("its", itsJo);
            val = rootJo.toString();
        } catch (JSONException e) {
            mListener.onLogE(TAG + ".start | json fail", e);
            return;
        }
        int errCode = mRtcEngine.setExtensionProperty(ExtensionManager.EXTENSION_VENDOR_NAME,
                ExtensionManager.EXTENSION_AUDIO_FILTER_NAME, "start_listening", val);
        mListener.onLogI(TAG + ".start | mRtcEngine.setExtensionProperty errCode: " + errCode);
    }

    /**
     * 结束音频获取结果
     */
    public void flush() {
        // 值不能为空，否则收不到。
        int errCode = mRtcEngine.setExtensionProperty(ExtensionManager.EXTENSION_VENDOR_NAME,
                ExtensionManager.EXTENSION_AUDIO_FILTER_NAME, "flush_listening", "{}");
        mListener.onLogI(TAG + ".flush | mRtcEngine.setExtensionProperty errCode: " + errCode);
    }

    /**
     * 停止倾听
     */
    public void stop() {
        int errCode = mRtcEngine.setExtensionProperty(ExtensionManager.EXTENSION_VENDOR_NAME,
                ExtensionManager.EXTENSION_AUDIO_FILTER_NAME, "stop_listening", "{}");
        mListener.onLogI(TAG + ".stop | mRtcEngine.setExtensionProperty errCode: " + errCode);
    }

    /**
     * 事件回调
     *
     * @param key 键
     * @param val 值
     */
    public void onEvent(String key, String val) {
        mParser.onEvent(key, val);
    }

    /**
     * 监听器
     */
    public interface IListener {
        /**
         * logcatI回调
         *
         * @param tip 非null
         */
        void onLogI(String tip);

        /**
         * logcatE回调
         *
         * @param tip 非null
         */
        void onLogE(String tip);

        /**
         * logcatE回调
         *
         * @param tip 非null
         * @param tr  非null
         */
        void onLogE(String tip, Throwable tr);

        /**
         * 语音转写文本回调
         *
         * @param text 非null
         */
        void onIstText(String text, Exception error);


        /**
         * 文本翻译文本回调
         *
         * @param text 非null
         */
        void onItsText(String text);
    }

    /**
     * 参数包装
     */
    public class ParamWrap {
        /**
         * 名称。非空。
         */
        public final String mName;

        /**
         * {@link HyUtil#start(ParamWrap)}
         */
        public final String mIstLanguage;

        /**
         * {@link HyUtil#start(ParamWrap)}
         */
        public final String mIstAccent;

        /**
         * {@link HyUtil#start(ParamWrap)}
         */
        public final String mIstDomain;

        /**
         * {@link HyUtil#start(ParamWrap)}
         */
        public final int mIstLanguageType;

        /**
         * {@link HyUtil#start(ParamWrap)}
         */
        public final String mIstDwa;

        /**
         * {@link HyUtil#start(ParamWrap)}
         */
        public final String mItsFrom;

        /**
         * {@link HyUtil#start(ParamWrap)}
         */
        public final String mItsTo;

        /**
         * 构造
         *
         * @param name            {@link #mName}
         * @param istLanguage     {@link #mIstLanguage}
         * @param istAccent       {@link #mIstAccent}
         * @param istDomain       {@link #mIstDomain}
         * @param istLanguageType {@link #mIstLanguageType}
         * @param istDwa          {@link #mIstDwa}
         * @param itsFrom         {@link #mItsFrom}
         * @param itsTo           {@link #mItsTo}
         */
        public ParamWrap(String name, String istLanguage, String istAccent, String istDomain,
                         int istLanguageType, String istDwa, String itsFrom, String itsTo) {
            mName = name;
            mIstLanguage = istLanguage;
            mIstAccent = istAccent;
            mIstDomain = istDomain;
            mIstLanguageType = istLanguageType;
            mIstDwa = istDwa;
            mItsFrom = itsFrom;
            mItsTo = itsTo;
        }
    }

    /**
     * 解析器
     */
    private class Parser {
        /**
         * 标签
         */
        private static final String TAG = "HyUtil.Parser";

        /**
         * 语音转写文本，key是sn，value是完整的子句转写结果
         */
        private final HashMap<Integer, String> mIstTexts = new HashMap<>();

        /**
         * 文本翻译文本，key是sn，value是完整的子句翻译结果
         */
        private final HashMap<Integer, String> mItsTexts = new HashMap<>();

        /**
         * 当前尚未结束的子句转写结果
         */
        private String mCurIstText = "";

        /**
         * 当前尚未结束的子句翻译结果
         */
        private String mCurItsText = "";

        /**
         * 索引集
         */
        private final LinkedBlockingQueue<Integer> mIdxs = new LinkedBlockingQueue<>(5);

        /**
         * 构造
         */
        public Parser() {
            clear();
        }

        /**
         * 事件回调
         *
         * @param key 键
         * @param val 值
         */
        public void onEvent(String key, String val) {
            switch (key) {
                case "error": {
                    onError(val);
                    break;
                }
                case "ist_result": {
                    onIstResult(val);
                    break;
                }
                case "its_result": {
                    onItsResult(val);
                    break;
                }
                case "end": {
                    onEnd(val);
                    break;
                }
                default: {
                    Log.e(TAG, "onEvent | inv key: " + key);
                    break;
                }
            }
        }

        /**
         * 错误回调
         *
         * @param val 值
         */
        private void onError(String val) {
            mListener.onLogE(TAG + ".onError | " + val);
        }

        /**
         * 语音转写结果回调
         *
         * @param val 值
         */
        private void onIstResult(String val) {
            try {
                parseIstResult(val);
            } catch (Exception e) {
                mListener.onLogE(TAG + ".onIstResult | parseIstResult fail, val: " + val, e);
                mListener.onIstText(null, e);
                stop();
                return;
            }
            final String text = getDisplayText();
            Log.i(TAG, "onIstResult | text: " + text);
            mListener.onIstText(text,null);
        }

        /**
         * 文本翻译结果回调
         *
         * @param val 值
         */
        private void onItsResult(String val) {
            try {
                parseItsResult(val);
            } catch (Exception e) {
                mListener.onLogE(TAG + ".onItsResult | parseItsResult fail, val: " + val, e);
                stop();
                return;
            }
            final String text = getItsDisplayText();
            mListener.onItsText(text);
        }

        /**
         * 结束回调
         *
         * @param val 值
         */
        private void onEnd(String val) {
            mListener.onLogI(TAG + ".onEnd");
        }

        /**
         * 解析语音转写结果
         *
         * @param result 结果。非空。
         * @return 是否结束
         * @throws Exception 失败
         */
        private boolean parseIstResult(String result) throws Exception {
            JSONObject rootJo = new JSONObject(result);
            int code = rootJo.getInt("code");
            if (0 != code) {
                throw new Exception("Parser.parseIstResult | code: " + code);
            }
            // String message = rootJo.getString("message");
            // String sid = rootJo.getString("sid");
            JSONObject dataJo = rootJo.optJSONObject("data");
            if (null == dataJo) {
                Log.w(TAG, "parseIstResult | dataJo: null, result: " + result);
                return false;
            }

            int status = dataJo.getInt("status");
            boolean isEnd = 2 == status;
            JSONObject resultJo = dataJo.optJSONObject("result");
            if (null == resultJo) {
                Log.w(TAG, "parseIstResult | resultJo: null, result: " + result);
                return isEnd;
            }

            // 禁止中途return
            StringBuilder sb = new StringBuilder();

            int sn = resultJo.getInt("sn");
            boolean subEnd = resultJo.optBoolean("sub_end", true);
            JSONArray wsJa = resultJo.optJSONArray("ws");
            if (null == wsJa) {
                Log.w(TAG, "parseIstResult | wsJa: null, result: " + result);
            } else {
                for (int i = 0; wsJa.length() > i; ++i) {
                    JSONObject wsJaItemJo = wsJa.optJSONObject(i);
                    if (null == wsJaItemJo) {
                        Log.w(TAG, "parseIstResult | wsJaItemJo: null i: " + i + ", result: "
                                + result);
                        continue;
                    }

                    JSONArray cwJa = wsJaItemJo.optJSONArray("cw");
                    if (null == cwJa) {
                        Log.w(TAG, "parseIstResult | cwJa: null i: " + i + ", result: "
                                + result);
                        continue;
                    }

                    JSONObject cwJaItem0Jo = cwJa.optJSONObject(0);
                    if (null == cwJaItem0Jo) {
                        Log.w(TAG, "parseIstResult | cwJaItem0Jo: null i: " + i
                                + ", result: " + result);
                        continue;
                    }

                    String w = cwJaItem0Jo.optString("w", null);
                    if (TextUtils.isEmpty(w)) {
                        continue;
                    }
                    sb.append(w);
                }
            }

            mCurIstText = sb.toString();
            // 句空结果没必要加入导致清空，优化显示结果
            if (!mCurIstText.isEmpty()) {
                // 子句结束，添加进mIstTexts，对应的sn放入mIdxs
                if (subEnd || isEnd) {
                    mIstTexts.put(sn, mCurIstText);
                    mIdxs.put(sn);
                    mCurIstText = "";
                }
            }

            return isEnd;
        }
        //
        // /**
        //  * 获取全部文本
        //  *
        //  * @return 非null
        //  */
        // public String getAllText() {
        //     StringBuilder sb = new StringBuilder();
        //     for (String text : mTexts) {
        //         if (null != text) {
        //             sb.append(text);
        //         }
        //     }
        //     return sb.toString();
        // }

        /**
         * 获取显示文本
         *
         * @return 非null
         */
        private String getDisplayText() {
            // 过早的不显示了，更早的清空。
            if (0 == mIdxs.remainingCapacity()) {
                Integer begin = mIdxs.poll();
                mIstTexts.remove(begin);
                mItsTexts.remove(begin);
            }

            StringBuilder sb = new StringBuilder();
            for (Integer mIdx : mIdxs) {
                // text非null
                sb.append(mIstTexts.get(mIdx));
            }
            sb.append(mCurIstText);
            return sb.toString();
        }

        /**
         * 解析文本翻译结果
         *
         * @param result 结果。非空。
         * @throws Exception 失败
         */
        private void parseItsResult(String result) throws Exception {
            JSONObject rootJo = new JSONObject(result);
            int code = rootJo.getInt("code");
            if (0 != code) {
                throw new Exception("Parser.parseItsResult | code: " + code);
            }
            int istSn = rootJo.getInt("ist_sn");
            JSONObject dataJo = rootJo.getJSONObject("data");
            JSONObject resultJo = dataJo.getJSONObject("result");
            JSONObject transResultJo = resultJo.getJSONObject("trans_result");
            final String dst = transResultJo.getString("dst");

            // ist_sn对应Ist子句结束时的sn，由服务器保证
            Integer first = null;
            mCurItsText = "";
            if (mIdxs.contains(istSn)) {
                mItsTexts.put(istSn, dst);
            } else if (null == (first = mIdxs.peek()) || first < istSn) {
                mCurItsText = dst;
            }
            // 若翻译慢，则可能idx已冲掉。
        }

        /**
         * 获取文本翻译显示文本
         *
         * @return 非null
         */
        private String getItsDisplayText() {
            StringBuilder sb = new StringBuilder();
            for (Integer idx : mIdxs) {
                String text = mItsTexts.get(idx);
                if (null == text) {
                    // 可能还没翻译好。
                    Logging.w(TAG, "getItsDisplayText | text: null, idx: " + idx);
                    continue;
                }
                sb.append(text);
            }
            sb.append(mCurItsText);
            return sb.toString();
        }

        /**
         * 清空
         */
        public void clear() {
            mIstTexts.clear();
            mItsTexts.clear();
            mIdxs.clear();
        }
    }
}


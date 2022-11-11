package io.agora.voice.network.tools.bean;

import java.io.Serializable;
import java.util.List;

public class VRMicListBean implements Serializable {

    /**
     * total : 1
     * cursor: null
     * apply_list : [{"mic_index":-1,"member":{"uid":"XrVDDDqVCQiEPTHP0STFSg==","name":"apex3","portrait":"avatar13"},"created_at":1664499627000}]
     */

    private int total;
    private String cursor;
    private List<ApplyListBean> apply_list;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<ApplyListBean> getApply_list() {
        return apply_list;
    }

    public void setApply_list(List<ApplyListBean> apply_list) {
        this.apply_list = apply_list;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public static class ApplyListBean implements Serializable {
        /**
         * mic_index : -1
         * member : {"uid":"XrVDDDqVCQiEPTHP0STFSg==","name":"apex3","portrait":"avatar13"}
         * created_at : 1664499627000
         */

        private int mic_index;
        private VMemberBean member;
        private long created_at;

        public int getMic_index() {
            return mic_index;
        }

        public void setMic_index(int mic_index) {
            this.mic_index = mic_index;
        }

        public VMemberBean getMember() {
            return member;
        }

        public void setMember(VMemberBean member) {
            this.member = member;
        }

        public long getCreated_at() {
            return created_at;
        }

        public void setCreated_at(long created_at) {
            this.created_at = created_at;
        }

//        public static class MemberBean implements Serializable {
//            /**
//             * uid : XrVDDDqVCQiEPTHP0STFSg==
//             * name : apex3
//             * portrait : avatar13
//             */
//
//            private String uid;
//            private String name;
//            private String portrait;
//
//            public String getUid() {
//                return uid;
//            }
//
//            public void setUid(String uid) {
//                this.uid = uid;
//            }
//
//            public String getName() {
//                return name;
//            }
//
//            public void setName(String name) {
//                this.name = name;
//            }
//
//            public String getPortrait() {
//                return portrait;
//            }
//
//            public void setPortrait(String portrait) {
//                this.portrait = portrait;
//            }
//        }
    }
}

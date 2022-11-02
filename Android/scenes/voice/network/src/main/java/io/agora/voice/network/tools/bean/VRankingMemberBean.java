package io.agora.voice.network.tools.bean;

import java.io.Serializable;

/**
 * @author create by zhangwei03
 */
public class VRankingMemberBean implements Serializable {
    /**
     * name : string
     * portrait : string
     * amount : 0
     */

    private String name;
    private String portrait;
    private int amount;

    public String getName() {
        return name;
    }

    public String getPortrait() {
        return portrait;
    }

    public int getAmount() {
        return amount;
    }
}

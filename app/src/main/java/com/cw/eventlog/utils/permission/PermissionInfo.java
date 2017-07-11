package com.cw.eventlog.utils.permission;

/**
 * @author Cw
 * @date 2017/7/10
 */
public class PermissionInfo {
    private String mName;
    private String mShortName;

    PermissionInfo(String name) {
        this.mName = name;
        this.mShortName = name.substring(name.lastIndexOf(".") + 1);
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getShortName() {
        return mShortName;
    }

}

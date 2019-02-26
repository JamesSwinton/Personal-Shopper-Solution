package com.ses.zebra.pssdemo_2019.Utilities;

import com.slacorp.eptt.android.service.CoreListSet;

public class EnumToText {

    // Debugging
    private static final String TAG = "EnumToText";

    // Converts State integer (0-15) into String
    // Docs: com/slacorp/eptt/core/common/SessionState.html
    public static String getSessionStateAsString(int state) {
        switch(state) {
            case 0:
                return "Stopped";
            case 1:
                return "Unregistered";
            case 2:
                return "Registering";
            case 3:
                return "Authenticating";
            case 4:
                return "Idle";
            case 5:
                return "Presence";
            case 6:
                return "Group Presence";
            case 7:
                return "Update";
            case 8:
                return "List Sync";
            case 9:
                return "Config Update";
            case 10:
                return "Provision";
            case 11:
                return "Network Offline";
            case 12:
                return "Network Offline Voice Call";
            case 13:
                return "Network Offline Radio Off";
            case 14:
                return "De-registering";
            case 15:
                return "User Authentication";
            default:
                return "Unknown";
        }
    }

    // Converts list.type into String
    public static String getListTypeAsString(int listType) {
        switch (listType) {
            case CoreListSet.CONTACT_LIST:
                return "Contact";
            case CoreListSet.GROUP_LIST:
                return "Group";
            case CoreListSet.GROUP_PRESENCE_LIST:
                return "Group Presence";
            default:
                return "Unknown";
        }
    }
}

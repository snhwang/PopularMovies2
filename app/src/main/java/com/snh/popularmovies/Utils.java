package com.snh.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by snhwa on 12/11/2017.
 */

public class Utils {
    // From
    // https://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-times-out
    static public boolean isOnline(Context mContext) {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}

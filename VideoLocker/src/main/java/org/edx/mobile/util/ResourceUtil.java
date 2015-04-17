package org.edx.mobile.util;

import android.content.Context;

import com.squareup.phrase.Phrase;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;


public class ResourceUtil {


    public static String getResourceString(String name) {
        Context context = MainApplication.instance().getApplicationContext();
        return getResourceString(name, context);
    }

    /**
     * get the string resources dynamically
     * @param name
     * @param context
     * @return
     */
    public static String getResourceString(String name, Context context) {
        int nameResourceID = context.getResources().getIdentifier(name, "string", context.getApplicationInfo().packageName);
        if (nameResourceID == 0) {
            throw new IllegalArgumentException("No resource string found with name " + name);
        } else {
            return context.getString(nameResourceID);
        }
    }

    public static String getResourceString(int resourceId){
        Context context = MainApplication.instance().getApplicationContext();
        return context.getString(resourceId);
    }

    public static CharSequence getFormattedString(int resourceId, String key, String value){
        if ( value == null )
            value = "";
        return Phrase.from(ResourceUtil.getResourceString(resourceId))
                .put(key, value) .format();
    }

    public static CharSequence getFormattedStringForQuantity(int resourceIdForSingle, int resourceIdForPlural,
                                                       String key, int quantity){
        String template = ResourceUtil.getResourceString(quantity == 1 ? resourceIdForSingle : resourceIdForPlural);
        return Phrase.from(template) .put(key, quantity + "") .format();
    }
}

package com.zenyte.util;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
public class ApiUtils {
    
    public static String removeStart(String string, String remove) {
        if (isNullOrEmpty(string) || isNullOrEmpty(remove)) {
            return string;
        }
        
        if (string.startsWith(remove)) {
            return string.substring(remove.length());
        }
        
        return string;
    }
    
}

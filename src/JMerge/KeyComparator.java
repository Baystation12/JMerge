package JMerge;

import java.util.Comparator;

/**
 * Due to how BYOND maps handle keys, we need sorting order of lowercase-before-uppercase.
 * This comparator is used in TreeMap of keys to achieve this.
 *
 * Created by Atlantis
 * on 26.5.2017.
 */
public class KeyComparator implements Comparator<String> {
    public int compare(String key1, String key2) {
        if(key1.equals(key2)){
            return 0;
        }
        if(key1.length() > key2.length()){
            return 1;
        } else if (key2.length() < key1.length()){
            return -1;
        }
        for(int i = 0 ; i < (Math.min(key1.length(), key2.length())) ; i++) {
            Character c1 = key1.charAt(i);
            Character c2 = key2.charAt(i);

            if(Character.isLowerCase(c1) && Character.isUpperCase(c2)) {
                return -1;
            } else if(Character.isUpperCase(c1) && Character.isLowerCase(c2)) {
                return 1;
            } else {
                int charComparison = c1.compareTo(c2);
                if(charComparison != 0) {
                    return charComparison;
                }
            }
        }
        return 0;
    }
}

package JMerge;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Admin
 * on 27.5.2017.
 */
public class ParamParser {

    Map<Integer, String> params;

    public ParamParser(String[] params) {
        this.params = new HashMap<>();
        for(int i = 0; i < params.length; i++){
            this.params.put(i, params[i]);
        }
    }
}

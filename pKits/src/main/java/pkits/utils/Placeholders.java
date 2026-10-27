package pkits.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Placeholders {

    private Placeholders() {
    }

    public static Map<String, String> of(String... pairs) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }
}

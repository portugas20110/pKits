package pkits.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Text {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private Text() {
    }

    public static String apply(String input, Map<String, String> placeholders) {
        if (input == null) {
            return "";
        }
        String result = input;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static Component parse(String input) {
        return MINI.deserialize(input == null ? "" : input)
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static Component parse(String input, Map<String, String> placeholders) {
        return parse(apply(input, placeholders));
    }

    public static List<Component> parse(List<String> lines, Map<String, String> placeholders) {
        List<Component> result = new ArrayList<>();
        for (String line : lines) {
            result.add(parse(line, placeholders));
        }
        return result;
    }
}

import java.util.*;

public class EmojiConverter {
    public static void main(String[] args) {
        String temp1 = "🐁⬜🔪";
        String[] emojis = extractEmojis(temp1);
        
        // Print the extracted emojis
        for (String emoji : emojis) {
            System.out.print(emoji);
        }
    }

    public static String[] extractEmojis(String input) {
        List<String> emojiList = new ArrayList<>();
        int offset = 0;
        while (offset < input.length()) {
            int codePoint = input.codePointAt(offset);
            int charCount = Character.charCount(codePoint);
            String emoji = input.substring(offset, offset + charCount);
            emojiList.add(emoji);
            offset += charCount;
        }
        return emojiList.toArray(new String[0]);
    }
}
package com.chatapp.util;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for emoji handling in chat messages
 */
public class EmojiUtil {
    private static final Map<String, String> EMOJI_MAP = new HashMap<>();
    private static final Pattern EMOJI_PATTERN = Pattern.compile(":[a-zA-Z_]+:");
    
    static {
        // Initialize emoji mappings (text to unicode)
        EMOJI_MAP.put(":smile:", "😊");
        EMOJI_MAP.put(":laugh:", "😂");
        EMOJI_MAP.put(":sad:", "😢");
        EMOJI_MAP.put(":cry:", "😭");
        EMOJI_MAP.put(":angry:", "😠");
        EMOJI_MAP.put(":confused:", "😕");
        EMOJI_MAP.put(":cool:", "😎");
        EMOJI_MAP.put(":love:", "❤️");
        EMOJI_MAP.put(":thumbsup:", "👍");
        EMOJI_MAP.put(":thumbsdown:", "👎");
        EMOJI_MAP.put(":clap:", "👏");
        EMOJI_MAP.put(":party:", "🎉");
        EMOJI_MAP.put(":gift:", "🎁");
        EMOJI_MAP.put(":fire:", "🔥");
        EMOJI_MAP.put(":star:", "⭐");
        EMOJI_MAP.put(":warning:", "⚠️");
        EMOJI_MAP.put(":check:", "✅");
        EMOJI_MAP.put(":x:", "❌");
        EMOJI_MAP.put(":thinking:", "🤔");
        EMOJI_MAP.put(":wave:", "👋");
    }
    
    /**
     * Convert emoji codes in text to Unicode emojis
     * 
     * @param text Text with emoji codes
     * @return Text with Unicode emojis
     */
    public static String convertToEmojis(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        Matcher matcher = EMOJI_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String emojiCode = matcher.group();
            String emoji = EMOJI_MAP.getOrDefault(emojiCode, emojiCode);
            matcher.appendReplacement(sb, emoji);
        }
        
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * Check if a string contains emoji codes
     * 
     * @param text Text to check
     * @return True if text contains emoji codes
     */
    public static boolean containsEmoji(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        Matcher matcher = EMOJI_PATTERN.matcher(text);
        return matcher.find();
    }
    
    /**
     * Create an emoji selector panel
     * 
     * @param callback Callback function to receive selected emoji
     * @return JPanel containing emoji buttons
     */
    public static JPanel createEmojiPanel(EmojiSelectCallback callback) {
        JPanel emojiPanel = new JPanel(new GridLayout(4, 5, 5, 5));
        emojiPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        for (Map.Entry<String, String> entry : EMOJI_MAP.entrySet()) {
            JButton emojiButton = new JButton(entry.getValue());
            emojiButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            emojiButton.setFocusPainted(false);
            emojiButton.addActionListener(e -> callback.onEmojiSelected(entry.getKey()));
            emojiPanel.add(emojiButton);
        }
        
        return emojiPanel;
    }
    
    /**
     * Interface for emoji selection callbacks
     */
    public interface EmojiSelectCallback {
        void onEmojiSelected(String emojiCode);
    }
} 
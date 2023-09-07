package util;

public class DiscordUtil {
    public static String getDiscordMentionTag(long userId) { return "<@" + userId + ">"; }

    public static String getDiscordMentionTag(String mentioned) { return "<@" + mentioned + ">"; }
}

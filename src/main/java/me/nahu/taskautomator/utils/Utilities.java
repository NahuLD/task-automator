package me.nahu.taskautomator.utils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utilities {
    private static final Pattern PATTERN = Pattern.compile("(\\d+)\\s*(mo|ms|µs|ns|[smhdwy])\\s*", Pattern.CASE_INSENSITIVE);

    @NotNull
    public static Duration parseDuration(@Nullable String input) {
        Duration duration = Duration.ZERO;
        if (input == null || input.isEmpty()) {
            return duration;
        }
        Matcher matcher = PATTERN.matcher(input);
        while (matcher.find()) {
            int number = Integer.parseInt(matcher.group(1));
            String type = matcher.group(2).toLowerCase();
            switch (type) {
                case "ns":
                    duration = duration.plusNanos(number);
                    break;
                case "µs":
                    duration = duration.plusNanos(TimeUnit.MICROSECONDS.toNanos(number));
                    break;
                case "ms":
                    duration = duration.plusMillis(number);
                    break;
                case "s":
                    duration = duration.plusSeconds(number);
                    break;
                case "m":
                    duration = duration.plusMinutes(number);
                    break;
                case "h":
                    duration = duration.plusHours(number);
                    break;
                case "d":
                    duration = duration.plusDays(number);
                    break;
                case "w":
                    // 1 week being 7 days is standard according to ISO-8601.
                    duration = duration.plusDays(number * 7L);
                    break;
                case "mo":
                    // 1 month being 30 days is standard according to ISO-8601.
                    duration = duration.plusDays(number * 30L);
                    break;
                case "y":
                    // 1 year being 365 days is standard according to ISO-8601.
                    duration = duration.plusDays(number * 365L);
                    break;
            }
        }
        return duration;
    }

    public static void sendMessage(@NotNull CommandSender sender, @Nullable BaseComponent... message) {
        if (sender instanceof Player) {
            ((Player) sender).spigot().sendMessage(message);
        } else {
            sender.sendMessage(TextComponent.toLegacyText(message));
        }
    }
}

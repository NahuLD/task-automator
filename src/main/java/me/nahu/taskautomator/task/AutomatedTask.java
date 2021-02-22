package me.nahu.taskautomator.task;

import com.google.common.collect.ImmutableMap;
import me.nahu.taskautomator.TaskAutomatorPlugin;
import me.nahu.taskautomator.utils.Utilities;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AutomatedTask implements ConfigurationSerializable {
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
    private static final Plugin PLUGIN = TaskAutomatorPlugin.getPlugin(TaskAutomatorPlugin.class);

    private final String name;
    private final Duration duration;
    private long lastExecution;
    private final boolean repeated;
    private boolean running = false;
    private List<String> commands;

    private BukkitTask task;

    public AutomatedTask(
        @NotNull String name,
        @NotNull Duration duration,
        long lastExecution,
        boolean repeated,
        boolean running,
        @NotNull List<String> commands
    ) {
        this.name = name;
        this.duration = duration;
        this.lastExecution = lastExecution;
        this.repeated = repeated;
        this.commands = commands;
        if (running) startTask();
    }

    @NotNull
    public String getName() {
        return name;
    }

    public long getLastExecution() {
        return lastExecution;
    }

    public void setLastExecution(long lastExecution) {
        this.lastExecution = lastExecution;
    }

    public boolean isRepeated() {
        return repeated;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @NotNull
    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(@NotNull List<String> commands) {
        this.commands = commands;
    }

    public void startTask() {
        this.running = true;
        this.lastExecution = System.currentTimeMillis();
        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
                if (repeated) {
                    startTask();
                    return;
                }
                running = false;
            }
        }.runTaskLater(
            PLUGIN,
            TimeUnit.MILLISECONDS.toSeconds(nextExecutionInMillis()) * 20L
        );
    }

    public void stopTask() {
        task.cancel();
        running = false;
    }

    private long nextExecutionInMillis() {
        return duration.toMillis() - (System.currentTimeMillis() - this.lastExecution);
    }

    @NotNull
    public String nextExecutionFormatted() {
        if (!running) {
            return "None";
        }
        return DurationFormatUtils.formatDuration(
            nextExecutionInMillis(),
            "mm:ss"
        );
    }

    @NotNull
    public String lastExecutionFormatted() {
        if (lastExecution <= 0) {
            return "None";
        }
        return DATE_FORMATTER.format(
            Instant.ofEpochMilli(lastExecution).atZone(ZoneId.systemDefault()).toLocalDateTime()
        );
    }

    @Override
    public Map<String, Object> serialize() {
        return ImmutableMap.<String, Object>builder()
            .put("name", name)
            .put("duration", String.valueOf(duration.toMillis()).concat("ms"))
            .put("last-execution", lastExecution)
            .put("repeated", repeated)
            .put("running", running)
            .put("commands", commands)
            .build();
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static AutomatedTask deserialize(@NotNull Map<String, Object> args) {
        String name = (String) args.get("name");
        Duration duration = Utilities.parseDuration((String) args.get("duration"));
        long lastExecution = args.containsKey("last-execution") ?
            (long) args.get("last-execution") : System.currentTimeMillis();
        boolean repeated = args.containsKey("repeated") && (boolean) args.get("repeated");
        boolean running = args.containsKey("running") && (boolean) args.get("running");
        List<String> commands = (List<String>) args.get("commands");
        return new AutomatedTask(
            name,
            duration,
            lastExecution,
            repeated,
            running,
            commands
        );
    }
}

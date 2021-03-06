package me.nahu.taskautomator.task;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AutomatedTask implements ConfigurationSerializable {
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
    private static final Plugin PLUGIN = TaskAutomatorPlugin.getPlugin(TaskAutomatorPlugin.class);

    private String name;
    private final Duration duration;
    private long lastExecution;
    private int commandStep;
    private final boolean repeated;
    private boolean running = false;
    private final Map<Integer, List<String>> commands;

    private BukkitTask task;

    public AutomatedTask(
        @NotNull Duration duration,
        boolean repeated,
        @NotNull Map<Integer, List<String>> commands
    ) {
        this.duration = duration;
        this.repeated = repeated;
        this.commands = commands;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public long getLastExecution() {
        return lastExecution;
    }

    public void setLastExecution(long lastExecution) {
        this.lastExecution = lastExecution;
    }

    public int getCommandStep() {
        return commandStep;
    }

    public void setCommandStep(int commandStep) {
        this.commandStep = commandStep;
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
    public Collection<String> getCommands() {
        return commands.get(commandStep);
    }

    public void startTask() {
        if (!running) {
            this.lastExecution = System.currentTimeMillis();
        }

        this.running = true;
        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                getCommands().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
                if (toNextStep()) {
                    return;
                }
                lastExecution = System.currentTimeMillis();
                startTask();
            }
        }.runTaskLater(
            PLUGIN,
            TimeUnit.MILLISECONDS.toSeconds(nextExecutionInMillis()) * 20L
        );
    }

    public void stopTask() {
        this.stopTask(true);
    }

    public void stopTask(boolean stopRunning) {
        if (task != null) {
            task.cancel();
        }
        if (stopRunning) {
            running = false;
        }
    }

    public boolean toNextStep() {
        commandStep++;
        if (commandStep > commands.size()) {
            commandStep = 1;
            if (!repeated) {
                running = false;
                return true;
            }
        }
        return false;
    }

    private long nextExecutionInMillis() {
        return duration.toMillis() - (System.currentTimeMillis() - this.lastExecution);
    }

    @NotNull
    public String nextExecutionFormatted() {
        return nextExecutionFormatted(true);
    }

    @NotNull
    public String nextExecutionFormatted(boolean seconds) {
        if (!running) {
            return "Now";
        }
        return DurationFormatUtils.formatDuration(
            nextExecutionInMillis(),
            seconds ? "dd:HH:mm:ss" : "dd:HH:mm"
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
            .put("last-execution", lastExecution)
            .put("command-step", commandStep)
            .put("running", running)
            .build();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    public static AutomatedTask deserialize(@NotNull Map<String, Object> args) {
        Duration duration = Utilities.parseDuration((String) args.get("duration"));
        boolean repeated = args.containsKey("repeated") && (boolean) args.get("repeated");
        LinkedHashMap<Integer, List<String>> commands = (LinkedHashMap) args.get("commands");
        return new AutomatedTask(
            duration,
            repeated,
            commands
        );
    }
}

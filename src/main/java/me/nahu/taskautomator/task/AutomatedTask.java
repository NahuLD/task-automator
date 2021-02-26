package me.nahu.taskautomator.task;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import me.nahu.taskautomator.TaskAutomatorPlugin;
import me.nahu.taskautomator.utils.Utilities;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemorySection;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
        long lastExecution,
        int commandStep,
        boolean repeated,
        boolean running,
        @NotNull Map<Integer, List<String>> commands
    ) {
        this.duration = duration;
        this.lastExecution = lastExecution;
        this.commandStep = commandStep;
        this.repeated = repeated;
        this.commands = commands;
        if (running) startTask();
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

    public int getCommandStep() {
        return commandStep;
    }

    public boolean isRepeated() {
        return repeated;
    }

    public boolean isRunning() {
        return running;
    }

    @NotNull
    public ImmutableCollection<String> getCommands() {
        return ImmutableList.copyOf(commands.get(commandStep));
    }

    public void startTask() {
        this.running = true;
        this.lastExecution = System.currentTimeMillis();
        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                getCommands().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
                commandStep++;

                if (commandStep > commands.size()) {
                    commandStep = 1;
                    if (!repeated) {
                        running = false;
                        return;
                    }
                }
                startTask();
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
            .put("duration", duration.toMillis() + "ms")
            .put("last-execution", lastExecution)
            .put("command-step", commandStep)
            .put("repeated", repeated)
            .put("running", running)
            .put("commands", commands)
            .build();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    public static AutomatedTask deserialize(@NotNull Map<String, Object> args) {
        Duration duration = Utilities.parseDuration((String) args.get("duration"));
        long lastExecution = args.containsKey("last-execution") ?
            (long) args.get("last-execution") : System.currentTimeMillis();
        int commandStep = args.containsKey("command-step") ? (int) args.get("command-step") : 1;
        boolean repeated = args.containsKey("repeated") && (boolean) args.get("repeated");
        boolean running = args.containsKey("running") && (boolean) args.get("running");

        LinkedHashMap<Integer, List<String>> commands = (LinkedHashMap) args.get("commands");
        return new AutomatedTask(
            duration,
            lastExecution,
            commandStep,
            repeated,
            running,
            commands
        );
    }
}

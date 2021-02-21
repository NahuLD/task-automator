package me.nahu.taskautomator;

import com.google.common.collect.ImmutableSet;
import me.nahu.taskautomator.task.AutomatedTask;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AutomatedTasksManager {
    private final Map<String, AutomatedTask> tasks;

    private final YamlConfiguration configuration;
    private final File configurationFile;

    public AutomatedTasksManager(@NotNull File configurationFile) {
        this.configurationFile = configurationFile;
        this.configuration = YamlConfiguration.loadConfiguration(configurationFile);
        this.tasks = loadTasks();
    }

    @NotNull
    public Optional<AutomatedTask> getTaskByName(@NotNull String name) {
        return Optional.ofNullable(tasks.get(name));
    }

    @NotNull
    public ImmutableSet<AutomatedTask> getTasks() {
        return ImmutableSet.copyOf(tasks.values());
    }

    public void saveTasks() throws IOException {
        getTasks().forEach(task -> configuration.set(task.getName(), task));
        configuration.save(configurationFile);
    }

    @NotNull
    public Map<String, AutomatedTask> loadTasks() {
        return configuration.getKeys(false).stream()
            .map(section -> (AutomatedTask) configuration.get(section))
            .collect(Collectors.toMap(AutomatedTask::getName, Function.identity()));
    }
}

package me.nahu.taskautomator;

import com.google.common.collect.ImmutableSet;
import me.nahu.taskautomator.task.AutomatedTask;
import org.bukkit.configuration.Configuration;
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

    private final File tasksFolder;
    private final YamlConfiguration configuration;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public AutomatedTasksManager(@NotNull File dataFolder) {
        this.tasksFolder = new File(dataFolder, "tasks-data");
        if (!this.tasksFolder.exists()) {
            this.tasksFolder.mkdir();
        }

        File configurationFile = new File(dataFolder, "tasks.yml");
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
        for (AutomatedTask automatedTask : getTasks()) {
            saveTask(automatedTask);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void saveTask(@NotNull AutomatedTask automatedTask) throws IOException {
        File taskFile = new File(tasksFolder, automatedTask.getName().concat(".yml"));
        taskFile.createNewFile();

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(taskFile);
        configuration.set("last-execution", automatedTask.getLastExecution());
        configuration.set("command-step", automatedTask.getCommandStep());
        configuration.set("running", automatedTask.isRunning());

        configuration.save(taskFile);
    }

    @NotNull
    public Map<String, AutomatedTask> loadTasks() {
        return configuration.getKeys(false).stream()
            .map(section -> {
                AutomatedTask task = (AutomatedTask) configuration.get(section);
                task.setName(section);
                return task;
            })
            .peek(automatedTask -> {
                Configuration taskConfiguration = YamlConfiguration.loadConfiguration(
                    new File(tasksFolder, automatedTask.getName().concat(".yml"))
                );

                System.out.println(taskConfiguration.contains("last-execution"));
                long lastExecution = taskConfiguration.getLong("last-execution", System.currentTimeMillis());
                int commandStep = taskConfiguration.getInt("command-step", 1);
                boolean running = taskConfiguration.getBoolean("running", false);

                automatedTask.setLastExecution(lastExecution);
                automatedTask.setCommandStep(commandStep);
                automatedTask.setRunning(running);

                if (running) automatedTask.startTask();
            })
            .collect(Collectors.toMap(AutomatedTask::getName, Function.identity()));
    }
}

package me.nahu.taskautomator.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.nahu.taskautomator.AutomatedTasksManager;
import me.nahu.taskautomator.TaskAutomatorPlugin;
import me.nahu.taskautomator.task.AutomatedTask;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public class TaskAutomatorExtension extends PlaceholderExpansion {
    private static final String PLACEHOLDER_FORMAT = "_next";

    private final TaskAutomatorPlugin plugin;
    private final AutomatedTasksManager tasksManager;

    public TaskAutomatorExtension(@NotNull TaskAutomatorPlugin plugin) {
        this.plugin = plugin;
        this.tasksManager = plugin.getTasksManager();
    }

    @Override
    public @NotNull String getIdentifier() {
        return plugin.getName();
    }

    @Override
    public @NotNull String getAuthor() {
        return Arrays.toString(plugin.getDescription().getAuthors().toArray());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (!params.endsWith(PLACEHOLDER_FORMAT)) {
            return "N/A";
        }
        Optional<AutomatedTask> found = tasksManager.getTaskByName(
            params.replace(PLACEHOLDER_FORMAT, "")
        );
        return found.map(AutomatedTask::nextExecutionFormatted).orElse("N/A");
    }
}

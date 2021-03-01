package me.nahu.taskautomator;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.InvalidCommandArgument;
import me.nahu.taskautomator.command.AutomatedTaskCommand;
import me.nahu.taskautomator.papi.TaskAutomatorExtension;
import me.nahu.taskautomator.task.AutomatedTask;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

public class TaskAutomatorPlugin extends JavaPlugin {
    static {
        ConfigurationSerialization.registerClass(AutomatedTask.class, "AutomatedTask");
    }

    private BukkitCommandManager commandManager;
    private AutomatedTasksManager tasksManager;

    @Override
    public void onEnable() {
        saveResource("tasks.yml", false);
        saveResource("config.yml", false);

        tasksManager = new AutomatedTasksManager(getDataFolder());

        commandManager = new BukkitCommandManager(this);
        commandManager.getCommandCompletions().registerAsyncCompletion(
            "automatedtask",
            context -> tasksManager.getTasks().stream()
                .map(AutomatedTask::getName)
                .collect(Collectors.toSet())
        );
        commandManager.getCommandContexts().registerContext(
            AutomatedTask.class,
            context -> {
                String input = context.popFirstArg();
                if (input == null || input.equals("")) {
                    if (context.isOptional()) {
                        return null;
                    }
                    throw new InvalidCommandArgument("You must give a valid task name.");
                }
                return tasksManager.getTaskByName(input)
                    .orElseThrow(() -> new InvalidCommandArgument("No task found with that name."));
            }
        );

        commandManager.registerCommand(
            new AutomatedTaskCommand(getConfig(), tasksManager)
        );

        new TaskAutomatorExtension(this).register();
    }

    @Override
    public void onDisable() {
        commandManager.unregisterCommands();
        try {
            tasksManager.saveTasks();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AutomatedTasksManager getTasksManager() {
        return tasksManager;
    }
}

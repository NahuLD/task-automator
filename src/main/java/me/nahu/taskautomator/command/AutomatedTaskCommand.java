package me.nahu.taskautomator.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.google.common.collect.Lists;
import de.themoep.minedown.MineDown;
import me.nahu.taskautomator.AutomatedTasksManager;
import me.nahu.taskautomator.task.AutomatedTask;
import net.md_5.bungee.api.chat.BaseComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.nahu.taskautomator.utils.Utilities.sendMessage;

@CommandAlias("automatedtask|task|at")
public class AutomatedTaskCommand extends BaseCommand {
    private final AutomatedTasksManager tasksManager;

    private final String errorTaskRunningMessage;
    private final String errorTaskNotRunningMessage;
    private final String startedTaskMessage;
    private final String stoppedTaskMessage;

    private final List<String> statusMessage;
    private final List<String> listMessage;

    public AutomatedTaskCommand(@NotNull Configuration configuration, @NotNull AutomatedTasksManager tasksManager) {
        this.tasksManager = tasksManager;
        this.errorTaskRunningMessage = configuration.getString("error-task-running", "N/A");
        this.errorTaskNotRunningMessage = configuration.getString("error-task-not-running", "N/A");
        this.startedTaskMessage = configuration.getString("successfully-started-task", "N/A");
        this.stoppedTaskMessage = configuration.getString("successfully-stopped-task", "N/A");
        this.statusMessage = configuration.getStringList("automated-task-status");
        this.listMessage = configuration.getStringList("automated-task-list");
    }

    @Subcommand("start")
    @CommandPermission("automatedtask.start")
    @CommandCompletion("@automatedtask")
    public void start(
        @NotNull CommandSender sender,
        @NotNull AutomatedTask automatedTask
    ) {
        if (automatedTask.isRunning()) {
            sendMessage(sender, MineDown.parse(errorTaskRunningMessage));
            return;
        }
        automatedTask.startTask();
        sendMessage(sender, MineDown.parse(startedTaskMessage, "task_name", automatedTask.getName()));
    }

    @Subcommand("stop")
    @CommandPermission("automatedtask.stop")
    @CommandCompletion("@automatedtask")
    public void stop(
        @NotNull CommandSender sender,
        @NotNull AutomatedTask automatedTask
    ) {
        if (!automatedTask.isRunning()) {
            sendMessage(sender, MineDown.parse(errorTaskNotRunningMessage));
            return;
        }
        automatedTask.stopTask();
        sendMessage(sender, MineDown.parse(stoppedTaskMessage, "task_name", automatedTask.getName()));
    }

    @Subcommand("status")
    @CommandPermission("automatedtask.status")
    @CommandCompletion("@automatedtask")
    public void status(
        @NotNull CommandSender sender,
        @NotNull AutomatedTask automatedTask
    ) {
        statusMessage.stream()
            .map(it -> MineDown.parse(
                it,
                "task_name", automatedTask.getName(),
                "task_repeated", String.valueOf(automatedTask.isRepeated()),
                "task_running", String.valueOf(automatedTask.isRunning()),
                "task_last_execution", automatedTask.lastExecutionFormatted(),
                "task_next_execution", automatedTask.nextExecutionFormatted(),
                "task_command_step", String.valueOf(automatedTask.getCommandStep()),
                "task_commands", StringUtils.join(automatedTask.getCommands(), ", ")
            ))
            .forEach(component -> sendMessage(sender, component));
    }

    @Default
    @Subcommand("list")
    @CommandPermission("automatedtask.list")
    public void list(
        @NotNull CommandSender sender
    ) {
        List<BaseComponent[]> messages = Lists.newArrayList();
        if (listMessage.size() > 1) {
            listMessage.subList(0, listMessage.size() - 1).stream().map(MineDown::parse).forEach(messages::add);
        }
        String portalMessage = listMessage.get(listMessage.size() - 1);
        tasksManager.getTasks().stream().map(task ->
            MineDown.parse(portalMessage,
                "task_name", task.getName()
            )
        ).forEach(messages::add);
        messages.forEach(component -> sendMessage(sender, component));
    }
}

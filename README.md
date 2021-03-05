# TaskAutomator
Automate tasks and the execution time in between steps. The progress is kept even after reboots and restarts.

## Permission nodes
Node                   | Description
 ----------------------|----------------------------------------
automatedtask.*        | Access to all subcommands.
automatedtask.start    | Access to the start command.
automatedtask.stop     | Access to the stop command.
automatedtask.status   | Access to the status command.
automatedtask.nextstep | Access to the next step command.
automatedtask.reload   | Access to the reload command.
automatedtask.list     | Access to the list command.

## Placeholders for PAPI
_Requests MUST be appended with a Player._

Placeholder                                                | Return             | More Info
 ----------------------------------------------------------|--------------------|---------------------------------------------------------------------
%taskautomator_**(task-name)**_next%   | dd:hh:mm           | Time formatted in days, hours and minutes.

## Configurable Messages Syntax

### Inline Formatting
Description   | Syntax             | More Info
--------------|--------------------|---------------------------------------------------------------------
Color legacy  |` &6Text           `| [Formatting codes](https://minecraft.gamepedia.com/Formatting_codes)
Color         |` &gold&Text       `| [Color names](https://minecraft.gamepedia.com/Formatting_codes)
Rainbow       |` &rainbow&Text    `| Inline Rainbow
Rainbow Phase |` &rainbow:20&Text `| Inline Rainbow with a phase
Bold          |` **Text**         `|
Italic        |` ##Text##         `|
Underlined    |` __Text__         `|
Strikethrough |` ~~Text~~         `|
Obfuscated    |` ??Text??         `|

### Events ###
You can define click and hover events with the commonly used MarkDown link syntax
as well as specify formatting, font and colors that way.

#### Simple Syntax
Description                    | Syntax
 -------------------------------|---------------------------------------------------------
General syntax                 |` [Text](text-color text-formatting... link hover text) `
Simple Link                    |` [Text](https://example.com)                           `
Simple Command                 |` [Text](/command to run)                               `
Link + Hover                   |` [Text](https://example.com Hover Text)                `
Text formatting                |` [Text](blue underline)                                `
Rainbow                        |` [Text](rainbow)                                       `
Phased Rainbow                 |` [Text](rainbow:20)                                       `

#### Advanced Syntax
Description        | Syntax                                 | More Info
 -------------------|----------------------------------------|----
General syntax     |` [Text](action=value)                 `| [ClickEvent.Action](https://ci.md-5.net/job/BungeeCord/ws/chat/target/apidocs/net/md_5/bungee/api/chat/ClickEvent.Action.html), [HoverEvent.Action](https://ci.md-5.net/job/BungeeCord/ws/chat/target/apidocs/net/md_5/bungee/api/chat/HoverEvent.Action.html)
Link               |` [Text](open_url=https://example.com) `|
Color              |` [Text](color=red)                    `| [Color names](https://minecraft.gamepedia.com/Formatting_codes)
Formatting         |` [Text](format=underline,bold)        `|
Font               |` [Text](font=custom_font)             `| Set a custom font from a resource pack
Run Command        |` [Text](run_command=/command string)  `| Run command on click
Suggest Command    |` [Text](suggest_command=/command)     `| Suggest a command on click
Simple Hover       |` [Text](hover=Hover Text)             `| Show hover text
Hover Text         |` [Text](show_text=Hover Text)         `| Show hover text
Hover Entity Info  |` [Text](show_entity=uuid:pig Name)    `| Show entity information.
Hover Item Info    |` [Text](show_item=stone*2 nbt...)     `| Show item information, additional information needs to be provided as a string of the nbt in json
Insertion          |` [Text](insert=insert into input)     `| Insert into input on shift click, can be combined with other events

All advanced settings can be chained/included in a event definition.
You can't however add multiple different colors or click and hover actions!

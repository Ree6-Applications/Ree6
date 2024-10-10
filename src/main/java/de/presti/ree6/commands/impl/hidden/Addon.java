package de.presti.ree6.commands.impl.hidden;

import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import io.sentry.Sentry;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;

import java.nio.file.Path;

/**
 * A command to either reload all Addons or list all of them.
 */
@Command(name = "addon", description = "command.description.addon", category = Category.HIDDEN)
public class Addon implements ICommand {

    // TODO:: add messages to language file.

    /**
     * @inheritDoc
     */
    @Override
    public void onPerform(CommandEvent commandEvent) {
        if (!commandEvent.isBotOwner()) {
            commandEvent.reply(commandEvent.getResource("message.default.insufficientPermission", "BE DEVELOPER"), 5);
            return;
        }

        if (!commandEvent.isSlashCommand()) {
            commandEvent.reply(commandEvent.getResource("command.perform.onlySlashSupported"));
            return;
        }

        String subcommand = commandEvent.getSubcommand();

        switch (subcommand) {
            case "reload" -> {
                commandEvent.reply(commandEvent.getResource("message.addon.reloadAll"));
                //Main.getInstance().getPluginManager().reload();
                commandEvent.reply(commandEvent.getResource("message.addon.reloadedAll"));
            }

            case "list" -> {
                StringBuilder stringBuilder = new StringBuilder("```");
                for (PluginWrapper addon : Main.getInstance().getPluginManager().getPlugins()) {
                    stringBuilder.append(addon.getPluginId()).append("v").append(addon.getDescriptor().getVersion()).append(" ").append("by").append(" ").append(addon.getDescriptor().getProvider()).append("\n");
                }
                stringBuilder.append("```");
                commandEvent.reply(commandEvent.getResource("message.addon.list") + " " + (stringBuilder.length() == 6 ? "None" : stringBuilder));
            }

            case "start" -> {
                String addonName = commandEvent.getOption("addon").getAsString();

                try {
                    String id = Main.getInstance().getPluginManager().loadPlugin(Path.of("plugins", addonName));
                    PluginState state = Main.getInstance().getPluginManager().startPlugin(id);

                    commandEvent.reply("Loading attempt for addon with the name " + addonName + " and id " + id + " resulted in: " + state);
                } catch (Exception exception) {
                    commandEvent.reply("Couldn't load the addon called " + addonName, 5);
                    Sentry.captureException(exception);
                }
            }

            case "stop" -> {
                String addonName = commandEvent.getOption("addon").getAsString();
                try {
                    if (Main.getInstance().getPluginManager().unloadPlugin(addonName)) {
                        commandEvent.reply("Unloaded addon with the name " + addonName + "!");
                    } else {
                        commandEvent.reply("Couldn't unload the addon called " + addonName, 5);
                    }
                } catch (Exception exception) {
                    commandEvent.reply("Couldn't unload the addon called " + addonName, 5);
                    Sentry.captureException(exception);
                }
            }

            default -> commandEvent.reply(commandEvent.getResource("message.default.invalidQuery"), 5);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("addon", "command.description.addon").addSubcommands(
                new SubcommandData("reload", "Reload the Addon-System."),
                new SubcommandData("list", "List every loaded Addon."),
                new SubcommandData("start", "Load/start a Addon.")
                        .addOption(OptionType.STRING, "addon", "The Addon to load/start.", true),
                new SubcommandData("stop", "Unload/stop a Addon.")
                        .addOption(OptionType.STRING, "addon", "The Addon to unload/stop.", true)
        );
    }

    /**
     * @inheritDoc
     */
    @Override
    public String[] getAlias() {
        return new String[0];
    }
}
package de.presti.ree6.commands.impl.hidden;

import de.presti.ree6.addons.AddonLoader;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.data.Data;
import io.sentry.Sentry;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

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
        if (!commandEvent.getMember().getUser().getId().equalsIgnoreCase(Data.getBotOwner())) {
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
                Main.getInstance().getAddonManager().reload();
                commandEvent.reply(commandEvent.getResource("message.addon.reloadedAll"));
            }

            case "list" -> {
                StringBuilder stringBuilder = new StringBuilder("```");
                for (de.presti.ree6.addons.Addon addon : Main.getInstance().getAddonManager().addons) {
                    stringBuilder.append(addon.getName()).append("v").append(addon.getVersion())
                            .append(" ").append("for").append(" ").append("by").append(" ").append(addon.getAuthor()).append("\n");
                }
                stringBuilder.append("```");
                commandEvent.reply(commandEvent.getResource("message.addon.list") + " " + (stringBuilder.length() == 6 ? "None" : stringBuilder));
            }

            case "start" -> {
                String addonName = commandEvent.getOption("addon").getAsString();

                try {
                    de.presti.ree6.addons.Addon addon = AddonLoader.loadAddon(addonName);
                    if (addon == null) {
                        commandEvent.reply("Couldn't find a addon called " + addonName, 5);
                        return;
                    }
                    Main.getInstance().getAddonManager().loadAddon(addon);
                    Main.getInstance().getAddonManager().startAddon(addon);
                } catch (Exception exception) {
                    commandEvent.reply("Couldn't load the addon called " + addonName, 5);
                    Sentry.captureException(exception);
                }
            }

            case "stop" -> {
                de.presti.ree6.addons.Addon addon = Main.getInstance().getAddonManager().addons.stream()
                        .filter(a -> a.getName().equalsIgnoreCase(commandEvent.getOption("addon").getAsString())).findFirst().orElse(null);

                if (addon != null) {
                    Main.getInstance().getAddonManager().stopAddon(addon);
                    Main.getInstance().getAddonManager().addons.remove(addon);
                    commandEvent.reply("Stopped addon with the name " + addon.getName() + "!");
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
package de.presti.ree6.commands.impl.fun;

import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.commands.Category;
import de.presti.ree6.commands.CommandEvent;
import de.presti.ree6.commands.interfaces.Command;
import de.presti.ree6.commands.interfaces.ICommand;
import de.presti.ree6.main.Main;
import de.presti.ree6.utils.data.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * Sends a gif of a monkey with a quote.
 */
@Command(name = "monke", description = "command.description.monke", category = Category.FUN)
public class Monkey implements ICommand {

	/**
	 * @inheritDoc
	 */
	@Override
	public void onPerform(CommandEvent commandEvent) {
		final EmbedBuilder em = new EmbedBuilder();

		em.setTitle(commandEvent.getResource("command.label.monkey"));
		em.setColor(BotWorker.randomEmbedColor());
		em.setImage("https://c.tenor.com/Y89PE1f7exQAAAAd/reject-modernity-return-to-monke.gif");
		em.setFooter("Requested by " + commandEvent.getMember().getUser().getAsTag() + " - " + Data.ADVERTISEMENT, commandEvent.getMember().getUser().getAvatarUrl());

		Main.getInstance().getCommandManager().sendMessage(em, commandEvent.getChannel(), commandEvent.getInteractionHook());
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public CommandData getCommandData() {
		return null;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String[] getAlias() {
		return new String[]{"monkey", "monkegif"};
	}
}

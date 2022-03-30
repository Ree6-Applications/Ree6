package de.presti.ree6.events;

import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.presti.ree6.addons.impl.ChatProtector;
import de.presti.ree6.bot.BotWorker;
import de.presti.ree6.bot.version.BotState;
import de.presti.ree6.bot.util.Webhook;
import de.presti.ree6.main.Main;
import de.presti.ree6.sql.entities.UserLevel;
import de.presti.ree6.utils.others.AutoRoleHandler;
import de.presti.ree6.utils.others.RandomUtils;
import de.presti.ree6.utils.others.TimeUtil;
import de.presti.ree6.utils.data.ArrayUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.internal.interactions.component.SelectMenuImpl;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OtherEvents extends ListenerAdapter {

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        BotWorker.setState(BotState.STARTED);
        Main.getInstance().getLogger().info("Boot up finished!");

        Main.getInstance().getCommandManager().addSlashCommand();

        BotWorker.setActivity(event.getJDA(),"%shard_guilds% Guilds, on Shard %shard% with %shards% Shards.", Activity.ActivityType.WATCHING);
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        Main.getInstance().getSqlConnector().getSqlWorker().createSettings(event.getGuild().getId());
    }

    @Override
    public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
        Main.getInstance().getSqlConnector().getSqlWorker().deleteAllData(event.getGuild().getId());
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {

        AutoRoleHandler.handleMemberJoin(event.getGuild(), event.getMember());

        if (!Main.getInstance().getSqlConnector().getSqlWorker().isWelcomeSetup(event.getGuild().getId())) return;

        WebhookMessageBuilder wmb = new WebhookMessageBuilder();

        wmb.setAvatarUrl(event.getJDA().getSelfUser().getAvatarUrl());
        wmb.setUsername("Welcome!");
        wmb.setContent((Main.getInstance().getSqlConnector().getSqlWorker().getMessage(event.getGuild().getId())).replace("%user_name%", event.getMember().getUser().getName()).replace("%user_mention%", event.getMember().getUser().getAsMention()).replace("%guild_name%", event.getGuild().getName()));

        String[] info = Main.getInstance().getSqlConnector().getSqlWorker().getWelcomeWebhook(event.getGuild().getId());

        Webhook.sendWebhook(null, wmb.build(), Long.parseLong(info[0]), info[1], false);
    }

    @Override
    public void onGuildVoiceJoin(@Nonnull GuildVoiceJoinEvent event) {
        if (!ArrayUtil.voiceJoined.containsKey(event.getMember().getUser())) {
            ArrayUtil.voiceJoined.put(event.getMember().getUser(), System.currentTimeMillis());
        }
        super.onGuildVoiceJoin(event);
    }

    @Override
    public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {
        if (ArrayUtil.voiceJoined.containsKey(event.getMember().getUser())) {
            int min = TimeUtil.getTimeinMin(TimeUtil.getTimeinSec(ArrayUtil.voiceJoined.get(event.getMember().getUser())));

            int addxp = 0;

            for (int i = 1; i <= min; i++) {
                addxp += RandomUtils.random.nextInt(5, 11);
            }

            UserLevel userLevel = Main.getInstance().getSqlConnector().getSqlWorker().getVoiceLevelData(event.getGuild().getId(), event.getMember().getId());
            userLevel.setUser(event.getMember().getUser());
            userLevel.addExperience(addxp);

            Main.getInstance().getSqlConnector().getSqlWorker().addVoiceLevelData(event.getGuild().getId(), userLevel);

            AutoRoleHandler.handleVoiceLevelReward(event.getGuild(), event.getMember());

        }
        super.onGuildVoiceLeave(event);
    }

    @Override
    public void onGuildVoiceGuildDeafen(@NotNull GuildVoiceGuildDeafenEvent event) {
        if (event.getMember() != event.getGuild().getSelfMember()) return;

        if (!event.isGuildDeafened()) {
            event.getMember().deafen(true).queue();
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        super.onMessageReceived(event);

        if (event.isFromGuild() && event.isFromType(ChannelType.TEXT) && event.getMember() != null) {
            if (ChatProtector.isChatProtectorSetup(event.getGuild().getId()) &&
                    ChatProtector.checkMessage(event.getGuild().getId(), event.getMessage().getContentRaw())) {
                Main.getInstance().getCommandManager().deleteMessage(event.getMessage(), null);
                event.getChannel().sendMessage("You can't write that!").queue();
                return;
            }

            if (event.getAuthor().isBot()) return;

            if (!ArrayUtil.messageIDwithMessage.containsKey(event.getMessageId())) {
                ArrayUtil.messageIDwithMessage.put(event.getMessageId(), event.getMessage());
            }

            if (!ArrayUtil.messageIDwithUser.containsKey(event.getMessageId())) {
                ArrayUtil.messageIDwithUser.put(event.getMessageId(), event.getAuthor());
            }


            if (!Main.getInstance().getCommandManager().perform(event.getMember(), event.getGuild(), event.getMessage().getContentRaw(), event.getMessage(), event.getTextChannel(), null)) {

                if (!event.getMessage().getMentionedUsers().isEmpty() && event.getMessage().getMentionedUsers().contains(event.getJDA().getSelfUser())) {
                    event.getChannel().sendMessage("Usage " + Main.getInstance().getSqlConnector().getSqlWorker().getSetting(event.getGuild().getId(), "chatprefix").getStringValue() + "help").queue();
                }

                if (!ArrayUtil.timeout.contains(event.getMember())) {

                    UserLevel userLevel = Main.getInstance().getSqlConnector().getSqlWorker().getChatLevelData(event.getGuild().getId(), event.getMember().getId());
                    userLevel.setUser(event.getMember().getUser());

                    if (userLevel.addExperience(RandomUtils.random.nextInt(15, 26))) {
                        Main.getInstance().getCommandManager().sendMessage("You just leveled up to Chat Level " + userLevel.getLevel() + " " + event.getMember().getAsMention() + " !", event.getChannel());
                    }

                    Main.getInstance().getSqlConnector().getSqlWorker().addChatLevelData(event.getGuild().getId(), userLevel);

                    ArrayUtil.timeout.add(event.getMember());

                    new Thread(() -> {
                        try {
                            Thread.sleep(30000);
                        } catch (InterruptedException ignored) {
                            Main.getInstance().getLogger().error("[OtherEvents] User cool-down Thread interrupted!");
                            Thread.currentThread().interrupt();
                        }

                        ArrayUtil.timeout.remove(event.getMember());

                    }).start();
                }

                AutoRoleHandler.handleChatLevelReward(event.getGuild(), event.getMember());
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // Only accept commands from guilds
        if (!event.isFromGuild() && event.getMember() != null) return;

        event.deferReply(true).queue();

        Main.getInstance().getCommandManager().perform(Objects.requireNonNull(event.getMember()), event.getGuild(), null, null, event.getTextChannel(), event);
    }

    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        super.onSelectMenuInteraction(event);

        if (event.getInteraction().getComponent().getId() == null || event.getGuild() == null) return;

        if (event.getMessage().getEmbeds().isEmpty() || event.getMessage().getEmbeds().get(0) == null || event.getInteraction().getSelectedOptions().isEmpty())
            return;

        switch (event.getInteraction().getComponent().getId()) {
            case "setupActionMenu" -> {

                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                List<SelectOption> optionList = new ArrayList<>();

                switch (event.getInteraction().getSelectedOptions().get(0).getValue()) {
                    case "log" -> {
                        optionList.add(SelectOption.of("Setup", "logSetup"));

                        if (Main.getInstance().getSqlConnector().getSqlWorker().isLogSetup(event.getGuild().getId()))
                            optionList.add(SelectOption.of("Delete", "logDelete"));

                        optionList.add(SelectOption.of("Back to Menu", "backToSetupMenu"));

                        embedBuilder.setDescription("You can set up our own Audit-Logging which provides all the Information over and Webhook into the Channel of your desire! " + "But ours is not the same as the default Auditions, ours gives your the ability to set what you want to be logged and what not! " + "We also allow you to log Voice Events!");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRows(ActionRow.of(new SelectMenuImpl("setupLogMenu", "Select your Action", 1, 1, false, optionList))).queue();
                    }

                    case "welcome" -> {
                        optionList.add(SelectOption.of("Setup", "welcomeSetup"));

                        if (Main.getInstance().getSqlConnector().getSqlWorker().isWelcomeSetup(event.getGuild().getId()))
                            optionList.add(SelectOption.of("Delete", "welcomeDelete"));

                        optionList.add(SelectOption.of("Back to Menu", "backToSetupMenu"));

                        embedBuilder.setDescription("You can set up our own Welcome-Messages! " + "You can choice the Welcome-Channel by your own and even configure the Message!");
                        
                        event.editMessageEmbeds(embedBuilder.build()).setActionRows(ActionRow.of(new SelectMenuImpl("setupWelcomeMenu", "Select your Action", 1, 1, false, optionList))).queue();
                    }

                    case "news" -> {
                        optionList.add(SelectOption.of("Setup", "newsSetup"));

                        if (Main.getInstance().getSqlConnector().getSqlWorker().isNewsSetup(event.getGuild().getId()))
                            optionList.add(SelectOption.of("Delete", "newsDelete"));

                        optionList.add(SelectOption.of("Back to Menu", "backToSetupMenu"));

                        embedBuilder.setDescription("You can set up our own Ree6-News! " + "By setting up Ree6-News on a specific channel your will get a Message in the given Channel, when ever Ree6 gets an update!");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRows(ActionRow.of(new SelectMenuImpl("setupNewsMenu", "Select your Action", 1, 1, false, optionList))).queue();
                    }

                    case "mute" -> {
                        embedBuilder.setDescription("You can set up our own Mute-System! " + "You can select the Role that Ree6 should give an Mute User!");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRows(ActionRow.of(Button.link("https://cp.ree6.de", "Webinterface"))).queue();
                    }

                    case "autorole" -> {
                        embedBuilder.setDescription("You can set up our own Autorole-System! " + "You can select Roles that Users should get upon joining the Server!");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRows(ActionRow.of(Button.link("https://cp.ree6.de", "Webinterface"))).queue();
                    }

                    default -> {
                        embedBuilder.setDescription("You somehow selected a Invalid Option? Are you a Wizard?");
                        event.editMessageEmbeds(embedBuilder.build()).queue();
                    }
                }
            }

            case "setupLogMenu" -> {

                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                List<SelectOption> optionList = new ArrayList<>();

                switch (event.getInteraction().getSelectedOptions().get(0).getValue()) {

                    case "backToSetupMenu" -> {
                        optionList.add(SelectOption.of("Audit-Logging", "log"));
                        optionList.add(SelectOption.of("Welcome-channel", "welcome"));
                        optionList.add(SelectOption.of("News-channel", "news"));
                        optionList.add(SelectOption.of("Mute role", "mute"));
                        optionList.add(SelectOption.of("Autorole", "autorole"));

                        embedBuilder.setDescription("Which configuration do you want to check out?");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRows(ActionRow.of(new SelectMenuImpl("setupActionMenu", "Select a configuration Step!", 1, 1, false, optionList))).queue();
                    }

                    case "logSetup" -> {
                        for (TextChannel channel : event.getGuild().getTextChannels()) {
                            optionList.add(SelectOption.of(channel.getName(), channel.getId()));
                        }

                        embedBuilder.setDescription("Which Channel do you want to use as Logging-Channel?");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRows(ActionRow.of(new SelectMenuImpl("setupLogChannel", "Select a Channel!", 1, 1, false, optionList))).queue();
                    }

                    default -> {
                        if (event.getMessage().getEmbeds().isEmpty() || event.getMessage().getEmbeds().get(0) == null)
                            return;

                        embedBuilder.setDescription("You somehow selected a Invalid Option? Are you a Wizard?");
                        event.editMessageEmbeds(embedBuilder.build()).queue();
                    }
                }
            }

            case "setupLogChannel" -> {

                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                TextChannel textChannel = event.getGuild().getTextChannelById(event.getInteraction().getSelectedOptions().get(0).getValue());

                if (textChannel != null) {
                    textChannel.createWebhook("Ree6-Logs").queue(webhook -> {
                        Main.getInstance().getSqlConnector().getSqlWorker().setLogWebhook(event.getGuild().getId(), webhook.getId(), webhook.getToken());
                        embedBuilder.setDescription("Successfully changed the Logging Channel, nice work!");
                        embedBuilder.setColor(Color.GREEN);
                        event.editMessageEmbeds(embedBuilder.build()).setActionRows(new ArrayList<>()).queue();
                    });
                } else {
                    embedBuilder.setDescription("The given Channel doesn't exists, how did you select it? Are you a Wizard?");
                    event.editMessageEmbeds(embedBuilder.build()).queue();
                }

            }

            case "setupWelcomeMenu" -> {

                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                List<SelectOption> optionList = new ArrayList<>();

                switch (event.getInteraction().getSelectedOptions().get(0).getValue()) {

                    case "backToSetupMenu" -> {
                        optionList.add(SelectOption.of("Audit-Logging", "log"));
                        optionList.add(SelectOption.of("Welcome-channel", "welcome"));
                        optionList.add(SelectOption.of("News-channel", "news"));
                        optionList.add(SelectOption.of("Mute role", "mute"));
                        optionList.add(SelectOption.of("Autorole", "autorole"));

                        embedBuilder.setDescription("Which configuration do you want to check out?");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRows(ActionRow.of(new SelectMenuImpl("setupActionMenu", "Select a configuration Step!", 1, 1, false, optionList))).queue();
                    }

                    case "welcomeSetup" -> {
                        for (TextChannel channel : event.getGuild().getTextChannels()) {
                            optionList.add(SelectOption.of(channel.getName(), channel.getId()));
                        }

                        embedBuilder.setDescription("Which Channel do you want to use as Welcome-Channel?");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRows(ActionRow.of(new SelectMenuImpl("setupWelcomeChannel", "Select a Channel!", 1, 1, false, optionList))).queue();
                    }

                    default -> {
                        if (event.getMessage().getEmbeds().isEmpty() || event.getMessage().getEmbeds().get(0) == null)
                            return;

                        embedBuilder.setDescription("You somehow selected a Invalid Option? Are you a Wizard?");
                        event.editMessageEmbeds(embedBuilder.build()).queue();
                    }
                }
            }

            case "setupWelcomeChannel" -> {

                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                TextChannel textChannel = event.getGuild().getTextChannelById(event.getInteraction().getSelectedOptions().get(0).getValue());

                if (textChannel != null) {
                    textChannel.createWebhook("Ree6-Welcome").queue(webhook -> {
                        Main.getInstance().getSqlConnector().getSqlWorker().setWelcomeWebhook(event.getGuild().getId(), webhook.getId(), webhook.getToken());
                        embedBuilder.setDescription("Successfully changed the Welcome-Channel, nice work!");
                        embedBuilder.setColor(Color.GREEN);
                        event.editMessageEmbeds(embedBuilder.build()).setActionRows(new ArrayList<>()).queue();
                    });
                } else {
                    embedBuilder.setDescription("The given Channel doesn't exists, how did you select it? Are you a Wizard?");
                    event.editMessageEmbeds(embedBuilder.build()).queue();
                }

            }

            case "setupNewsMenu" -> {

                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                List<SelectOption> optionList = new ArrayList<>();

                switch (event.getInteraction().getSelectedOptions().get(0).getValue()) {

                    case "backToSetupMenu" -> {
                        optionList.add(SelectOption.of("Audit-Logging", "log"));
                        optionList.add(SelectOption.of("Welcome-channel", "welcome"));
                        optionList.add(SelectOption.of("News-channel", "news"));
                        optionList.add(SelectOption.of("Mute role", "mute"));
                        optionList.add(SelectOption.of("Autorole", "autorole"));

                        embedBuilder.setDescription("Which configuration do you want to check out?");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRows(ActionRow.of(new SelectMenuImpl("setupActionMenu", "Select a configuration Step!", 1, 1, false, optionList))).queue();
                    }

                    case "newsSetup" -> {
                        for (TextChannel channel : event.getGuild().getTextChannels()) {
                            optionList.add(SelectOption.of(channel.getName(), channel.getId()));
                        }

                        embedBuilder.setDescription("Which Channel do you want to use as News-Channel?");

                        event.editMessageEmbeds(embedBuilder.build()).setActionRows(ActionRow.of(new SelectMenuImpl("setupNewsChannel", "Select a Channel!", 1, 1, false, optionList))).queue();
                    }

                    default -> {
                        if (event.getMessage().getEmbeds().isEmpty() || event.getMessage().getEmbeds().get(0) == null)
                            return;

                        embedBuilder.setDescription("You somehow selected a Invalid Option? Are you a Wizard?");
                        event.editMessageEmbeds(embedBuilder.build()).queue();
                    }
                }
            }

            case "setupNewsChannel" -> {
                if (checkPerms(event.getMember(), event.getChannel())) {
                    return;
                }

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                TextChannel textChannel = event.getGuild().getTextChannelById(event.getInteraction().getSelectedOptions().get(0).getValue());

                if (textChannel != null) {
                    textChannel.createWebhook("Ree6-News").queue(webhook -> {
                        Main.getInstance().getSqlConnector().getSqlWorker().setNewsWebhook(event.getGuild().getId(), webhook.getId(), webhook.getToken());
                        embedBuilder.setDescription("Successfully changed the News-Channel, nice work!");
                        embedBuilder.setColor(Color.GREEN);
                        event.editMessageEmbeds(embedBuilder.build()).setActionRows(new ArrayList<>()).queue();
                    });
                } else {
                    embedBuilder.setDescription("The given Channel doesn't exists, how did you select it? Are you a Wizard?");
                    event.editMessageEmbeds(embedBuilder.build()).queue();
                }
            }

            default -> {
                if (event.getMessage().getEmbeds().isEmpty() || event.getMessage().getEmbeds().get(0) == null) return;

                EmbedBuilder embedBuilder = new EmbedBuilder(event.getMessage().getEmbeds().get(0));

                embedBuilder.setDescription("You somehow selected a Invalid Option? Are you a Wizard?");
                event.editMessageEmbeds(embedBuilder.build()).queue();
            }
        }
    }

    private boolean checkPerms(Member member, MessageChannel channel) {
        if (member == null || !member.hasPermission(Permission.ADMINISTRATOR)) {
            channel.sendMessage("You do not have enough Permissions").queue();
            return true;
        }

        return false;
    }
}

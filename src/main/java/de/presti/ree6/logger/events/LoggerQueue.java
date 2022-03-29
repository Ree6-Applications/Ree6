package de.presti.ree6.logger.events;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.presti.ree6.bot.Webhook;
import de.presti.ree6.main.Data;
import net.dv8tion.jda.api.Permission;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;

/**
 * Internal LoggingQueue, created to merge LoggingMessages to prevent
 * Rate-Limits by Cloudflare.
 */
public class LoggerQueue {

    // TODO rework, use a base Log class and add extensions to it by implementing it into new classes, this would make it easier to maintain and add more.

    // A List of every Log-Message.
    final ArrayList<LoggerMessage> logs = new ArrayList<>();

    /**
     * Add a Logging Message into the List.
     *
     * @param loggerMessage the logging message.
     */
    public void add(LoggerMessage loggerMessage) {
        if (!logs.contains(loggerMessage)) {
            logs.add(loggerMessage);

            // Creating a new Webhook Message with an Embed.
            WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder().setAvatarUrl(loggerMessage.getGuild().getJDA().getSelfUser().getAvatarUrl()).setUsername("Ree6Logs");
            WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder().setColor(Color.BLACK.getRGB())
                    .setFooter(new WebhookEmbed.EmbedFooter(loggerMessage.getGuild().getName() + " - " + Data.ADVERTISEMENT,
                            (loggerMessage.getGuild().getIconUrl() != null ? loggerMessage.getGuild().getIconUrl() : null)))
                    .setTimestamp(Instant.now());

            // For later to check if it has been modified or not.
            boolean modified = false;

            // Stop if the Guild is null.
            if (loggerMessage.getGuild() == null) return;

            // Check if it's a VoiceChannel Join log.
            if (loggerMessage.getType() == LoggerMessage.LogTyp.VC_JOIN) {
                if (logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                        loggerMessages.getVoiceData() != null && loggerMessages.getVoiceData().getMember() == loggerMessage.getVoiceData().getMember() &&
                        !loggerMessages.isCanceled()).anyMatch(loggerMessages -> loggerMessages.getType() == LoggerMessage.LogTyp.VC_LEAVE)) {

                    // Cancel every Log-Message which indicates that the person left.
                    logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                            !loggerMessages.isCanceled() &&
                            loggerMessages.getVoiceData() != null &&
                            loggerMessages.getVoiceData().getMember() == loggerMessage.getVoiceData().getMember() &&
                            loggerMessages.getType() == LoggerMessage.LogTyp.VC_LEAVE).forEach(loggerMessages -> loggerMessages.setCanceled(true));

                    // Set the new Webhook Message.
                    webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(loggerMessage.getVoiceData().getMember().getUser().getAsTag(),
                            loggerMessage.getVoiceData().getMember().getUser().getAvatarUrl(), null));
                    webhookEmbedBuilder.setDescription(loggerMessage.getVoiceData().getMember().getAsMention() + " **rejoined the Voice-channel** " +
                            loggerMessage.getVoiceData().getCurrentVoiceChannel().getAsMention());

                    modified = true;
                }
            }
            // Check if it's a VoiceChannel Move log.
            else if (loggerMessage.getType() == LoggerMessage.LogTyp.VC_MOVE) {
                if (logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                        loggerMessages.getVoiceData() != null && loggerMessages.getVoiceData().getMember() == loggerMessage.getVoiceData().getMember() &&
                        !loggerMessages.isCanceled()).anyMatch(loggerMessages -> loggerMessages.getType() == LoggerMessage.LogTyp.VC_MOVE)) {

                    // Cancel every Log-Message which indicates that the person moved.
                    logs.stream().filter(loggerMessages -> loggerMessages.getId() == loggerMessage.getId() &&
                            loggerMessages != loggerMessage &&
                            loggerMessages.getVoiceData() != null &&
                            loggerMessages.getVoiceData().getMember() == loggerMessage.getVoiceData().getMember() && !loggerMessages.isCanceled() &&
                            loggerMessages.getType() == LoggerMessage.LogTyp.VC_MOVE).forEach(loggerMessages -> loggerMessages.setCanceled(true));

                    // Set the new Webhook Message.
                    webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(loggerMessage.getVoiceData().getMember().getUser().getAsTag(),
                            loggerMessage.getVoiceData().getMember().getUser().getAvatarUrl(), null));
                    webhookEmbedBuilder.setDescription(loggerMessage.getVoiceData().getMember().getUser().getAsMention() + " **moved through many Voice-channels and is now in** "
                            + loggerMessage.getVoiceData().getCurrentVoiceChannel().getAsMention());

                    modified = true;
                }
            }
            //Check if it's a VoiceChannel Leave log.
            else if (loggerMessage.getType() == LoggerMessage.LogTyp.VC_LEAVE) {
                if (logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                        loggerMessages.getVoiceData() != null && loggerMessages.getVoiceData().getMember() == loggerMessage.getVoiceData().getMember() &&
                        !loggerMessages.isCanceled()).anyMatch(loggerMessages -> loggerMessages.getType() == LoggerMessage.LogTyp.VC_JOIN)) {

                    // Cancel every Log-Message which indicates that the person joined.
                    logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                            loggerMessages.getVoiceData() != null &&
                            loggerMessages.getVoiceData().getMember() == loggerMessage.getVoiceData().getMember() &&
                            !loggerMessages.isCanceled() && loggerMessages.getType() == LoggerMessage.LogTyp.VC_JOIN).forEach(loggerMessages -> loggerMessages.setCanceled(true));

                    // Set the new Webhook Message.
                    webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(loggerMessage.getVoiceData().getMember().getUser().getAsTag(),
                            loggerMessage.getVoiceData().getMember().getUser().getAvatarUrl(), null));
                    webhookEmbedBuilder.setDescription(loggerMessage.getVoiceData().getMember().getUser().getAsMention() + " **joined and left the Voice-channel** " +
                            loggerMessage.getVoiceData().getPreviousVoiceChannel().getAsMention());

                    modified = true;
                }
            }
            // Check if it's a Nickname Change Log.
            else if (loggerMessage.getType() == LoggerMessage.LogTyp.NICKNAME_CHANGE) {
                if (logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                        loggerMessages.getMemberData() != null && loggerMessages.getMemberData().getMember() == loggerMessage.getMemberData().getMember() &&
                        !loggerMessages.isCanceled()).anyMatch(loggerMessages -> loggerMessages.getType() == LoggerMessage.LogTyp.NICKNAME_CHANGE)) {

                    // Get the latest previous UserData.
                    LoggerMemberData memberData = logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage &&
                            loggerMessages.getId() == loggerMessage.getId() &&
                            loggerMessages.getMemberData().getMember() == loggerMessage.getMemberData().getMember() &&
                            !loggerMessages.isCanceled() && loggerMessages.getType() == LoggerMessage.LogTyp.NICKNAME_CHANGE).findFirst().orElseThrow().getMemberData();

                    // Cancel every Log-Message which indicates that the person changed their name.
                    logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                            loggerMessages.getMemberData().getMember() == loggerMessage.getMemberData().getMember() &&
                            !loggerMessages.isCanceled() && loggerMessages.getType() == LoggerMessage.LogTyp.NICKNAME_CHANGE).forEach(loggerMessages -> loggerMessages.setCanceled(true));

                    // Change the current previous Nickname to the old one.
                    loggerMessage.getMemberData().setPreviousName(memberData.getPreviousName());

                    // Set the new Webhook Message.
                    webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(loggerMessage.getVoiceData().getMember().getUser().getAsTag(),
                            loggerMessage.getMemberData().getMember().getUser().getAvatarUrl(), null));
                    webhookEmbedBuilder.setDescription("The Nickname of " + loggerMessage.getMemberData().getMember().getAsMention() + " has been changed.\n**New Nickname:**\n" +
                            loggerMessage.getMemberData().getCurrentName() + "\n**Old Nickname:**\n" +
                            (memberData.getPreviousName() != null ? memberData.getPreviousName() : loggerMessage.getMemberData().getMember().getUser().getName()));

                    modified = true;
                }
            }
            // Check if it's a Member Role Change log.
            else if (loggerMessage.getType() == LoggerMessage.LogTyp.MEMBERROLE_CHANGE) {
                if (logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getMemberData() != null &&
                        loggerMessages.getMemberData().getMember() == loggerMessage.getMemberData().getMember()
                        && loggerMessages.getId() == loggerMessage.getId() &&
                        !loggerMessages.isCanceled()).anyMatch(loggerMessages -> loggerMessages.getType() == LoggerMessage.LogTyp.MEMBERROLE_CHANGE)) {

                    // Get the latest MemberData.
                    LoggerMemberData memberData = logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage &&
                            loggerMessages.getId() == loggerMessage.getId() &&
                            loggerMessages.getMemberData() != null &&
                            loggerMessages.getMemberData().getMember() == loggerMessage.getMemberData().getMember() &&
                            !loggerMessages.isCanceled() && loggerMessages.getType() == LoggerMessage.LogTyp.MEMBERROLE_CHANGE).findFirst().orElseThrow().getMemberData();

                    // Cancel every other LogEvent of that Typ.
                    logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                            loggerMessages.getMemberData() != null &&
                            loggerMessages.getMemberData().getMember() == loggerMessage.getMemberData().getMember() &&
                            !loggerMessages.isCanceled() && loggerMessages.getType() == LoggerMessage.LogTyp.MEMBERROLE_CHANGE).forEach(loggerMessages -> loggerMessages.setCanceled(true));

                    // Check if the RemoveRoles is null or empty.
                    if ((loggerMessage.getMemberData().getRemovedRoles() == null || loggerMessage.getMemberData().getRemovedRoles().isEmpty()) &&
                            memberData.getRemovedRoles() != null && !memberData.getRemovedRoles().isEmpty()) {

                        // Set the current removed Roles to the one from the latest.
                        loggerMessage.getMemberData().setRemovedRoles(memberData.getRemovedRoles());
                    }

                    // Check if the AddedRoles is null or empty.
                    if ((loggerMessage.getMemberData().getAddedRoles() == null || loggerMessage.getMemberData().getAddedRoles().isEmpty()) &&
                            memberData.getAddedRoles() != null && !memberData.getAddedRoles().isEmpty()) {

                        // Set the current added Roles to the one from the latest.
                        loggerMessage.getMemberData().setAddedRoles(memberData.getAddedRoles());
                    }

                    // Check if the addedRoles and removeRoles are null if so create new List.
                    if (loggerMessage.getMemberData().getAddedRoles() == null)
                        loggerMessage.getMemberData().setAddedRoles(new ArrayList<>());

                    if (loggerMessage.getMemberData().getRemovedRoles() == null)
                        loggerMessage.getMemberData().setRemovedRoles(new ArrayList<>());

                    // Check if the Lists are Empty if not remove duplicated entries.
                    if (!loggerMessage.getMemberData().getRemovedRoles().isEmpty() && !loggerMessage.getMemberData().getAddedRoles().isEmpty()) {
                        loggerMessage.getMemberData().getRemovedRoles().removeIf(role -> role == null || loggerMessage.getMemberData().getAddedRoles().contains(role));
                    }

                    // Merge both lists with the current List.
                    if (memberData != null && memberData.getRemovedRoles() != null && !memberData.getRemovedRoles().isEmpty() && memberData.getRemovedRoles().stream().anyMatch(role -> role != null && loggerMessage.getMemberData().getAddedRoles().contains(role) &&
                            !loggerMessage.getMemberData().getRemovedRoles().contains(role))) {
                        try {
                            memberData.getRemovedRoles().stream().filter(role -> role != null && !loggerMessage.getMemberData().getAddedRoles().contains(role) &&
                                    !loggerMessage.getMemberData().getRemovedRoles().contains(role)).forEach(role -> loggerMessage.getMemberData().getRemovedRoles().add(role));
                        } catch (Exception ignore) {
                        }
                    }

                    if (memberData != null && memberData.getAddedRoles() != null && !memberData.getAddedRoles().isEmpty() && memberData.getAddedRoles().stream().anyMatch(role -> role != null && loggerMessage.getMemberData().getAddedRoles().contains(role) &&
                            !loggerMessage.getMemberData().getRemovedRoles().contains(role))) {
                        try {
                            memberData.getAddedRoles().stream().filter(role -> role != null && !loggerMessage.getMemberData().getAddedRoles().contains(role) &&
                                    !loggerMessage.getMemberData().getRemovedRoles().contains(role)).forEach(role -> loggerMessage.getMemberData().getAddedRoles().add(role));
                        } catch (Exception ignore) {
                        }
                    }

                    // StringBuilder to convert the List into a single String.
                    StringBuilder stringBuilder = new StringBuilder();

                    // Add the Entries into the String.
                    loggerMessage.getMemberData().getAddedRoles().forEach(role -> stringBuilder.append(":white_check_mark:").append(" ").append(role.getName()).append("\n"));
                    loggerMessage.getMemberData().getRemovedRoles().forEach(role -> stringBuilder.append(":no_entry:").append(" ").append(role.getName()).append("\n"));

                    // Set Embed Elements.
                    webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(loggerMessage.getMemberData().getMember().getUser().getAsTag(),
                            loggerMessage.getMemberData().getMember().getUser().getAvatarUrl(), null));
                    webhookEmbedBuilder.setThumbnailUrl(loggerMessage.getMemberData().getMember().getUser().getAvatarUrl());
                    webhookEmbedBuilder.setDescription(":writing_hand: " + loggerMessage.getMemberData().getMember().getAsMention() + " **has been updated.**");

                    if (!loggerMessage.getMemberData().getAddedRoles().isEmpty() || !loggerMessage.getMemberData().getRemovedRoles().isEmpty())
                        webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Roles:**", stringBuilder.toString()));

                    modified = true;
                }
            }
            // Check if it's a Role Update log.
            else if (loggerMessage.getType() == LoggerMessage.LogTyp.ROLEDATA_CHANGE) {
                if (logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                        loggerMessages.getRoleData() != null &&
                        !loggerMessages.isCanceled()).anyMatch(loggerMessages -> loggerMessages.getType() == LoggerMessage.LogTyp.ROLEDATA_CHANGE)) {

                    // Get the latest RoleData.
                    LoggerRoleData roleData = logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                            loggerMessages.getRoleData() != null &&
                            !loggerMessages.isCanceled() && loggerMessages.getType() == LoggerMessage.LogTyp.ROLEDATA_CHANGE).findFirst().orElseThrow().getRoleData();

                    // Cancel every Log-Message which indicates that the person changed their name.
                    logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                            !loggerMessages.isCanceled() && loggerMessages.getType() == LoggerMessage.LogTyp.ROLEDATA_CHANGE).forEach(loggerMessages -> loggerMessages.setCanceled(true));

                    // Start merging the Role Permissions
                    if (roleData != null) {

                        // Check if the latest Role Data has a previous Name.
                        if (roleData.getPreviousName() != null && !roleData.getPreviousName().isEmpty()) {

                            // Set the previous Name of the current Role Data to the latest one.
                            loggerMessage.getRoleData().setPreviousName(roleData.getPreviousName());
                        }

                        // Check if the latest Role Data has a current Name.
                        if ((loggerMessage.getRoleData().getCurrentName() == null || loggerMessage.getRoleData().getCurrentName().isEmpty()) &&
                                roleData.getCurrentName() != null && !roleData.getCurrentName().isEmpty()) {

                            // Set the current Name of the current Role Data to the latest one.
                            loggerMessage.getRoleData().setCurrentName(roleData.getCurrentName());
                        }

                        // Check if the latest Role Data has previous Permissions.
                        if (roleData.getPreviousPermission() != null && !roleData.getPreviousPermission().isEmpty()) {

                            // Set the previous Permissions.
                            loggerMessage.getRoleData().setPreviousPermission(roleData.getPreviousPermission());
                        }

                        // Check if the latest Role Data has current Permissions.
                        if ((loggerMessage.getRoleData().getCurrentPermission() == null || loggerMessage.getRoleData().getCurrentPermission().isEmpty())
                                && roleData.getCurrentPermission() != null && !roleData.getCurrentPermission().isEmpty()) {

                            // Set the current Permissions.
                            loggerMessage.getRoleData().setCurrentPermission(roleData.getCurrentPermission());
                        }

                        // Check if the latest Role Data has a previous Color.
                        if (roleData.getPreviousColor() != null) {

                            // Set the previous Color.
                            loggerMessage.getRoleData().setPreviousColor(roleData.getPreviousColor());
                        }

                        // Check if the latest Role Data has a current Color.
                        if (loggerMessage.getRoleData().getCurrentColor() == null && roleData.getCurrentColor() != null) {

                            // Set the current Color.
                            loggerMessage.getRoleData().setCurrentColor(roleData.getCurrentColor());
                        }

                        // Check if the latest has another value for Hoisted and if the current Value is the default Value or not.
                        if (!loggerMessage.getRoleData().isChangedHoisted() && roleData.isChangedHoisted()) {

                            // Set the current Value to the one from the latest.
                            loggerMessage.getRoleData().setChangedHoisted(roleData.isChangedHoisted());
                        }

                        // Check if the latest has another value for Mentioned and if the current Value is the default Value or not.
                        if (!loggerMessage.getRoleData().isChangedMentioned() && roleData.isChangedMentioned()) {

                            // Set the current Value to the one from the latest.
                            loggerMessage.getRoleData().setChangedMentioned(roleData.isChangedMentioned());
                        }
                    }

                    // Set the author of the WebhookMessage.
                    webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(loggerMessage.getGuild().getName(), loggerMessage.getGuild().getIconUrl(), null));

                    // Check if it isn't a new created or deleted Role.
                    if (!loggerMessage.getRoleData().isCreated() && !loggerMessage.getRoleData().isDeleted()) {

                        // Set update as Description
                        webhookEmbedBuilder.setDescription(":family_mmb: ``" + loggerMessage.getRoleData().getCurrentName() + "`` **has been updated.**");

                        // Check if there is a previous and current Name.
                        if (loggerMessage.getRoleData().getPreviousName() != null && loggerMessage.getRoleData().getCurrentName() != null) {

                            // Add new Fields with Information.
                            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Old name**", loggerMessage.getRoleData().getPreviousName()));
                            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**New name**", loggerMessage.getRoleData().getCurrentName()));
                            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "** **", "** **"));
                        }

                        // Check if the Mentioned has been changed or not.
                        if (loggerMessage.getRoleData().isChangedMentioned()) {

                            // Add new Fields with Information.
                            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Old mentionable**", !loggerMessage.getRoleData().isMentioned() + ""));
                            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**New mentionable**", loggerMessage.getRoleData().isMentioned() + ""));
                            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "** **", "** **"));
                        }

                        // Check if the Hoisted has been changed or not.
                        if (loggerMessage.getRoleData().isChangedHoisted()) {

                            // Add new Fields with Information.
                            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Old hoist**", !loggerMessage.getRoleData().isHoisted() + ""));
                            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**New hoist**", loggerMessage.getRoleData().isHoisted() + ""));
                            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "** **", "** **"));
                        }

                        // Check if a new Color has been added or changed.
                        if (loggerMessage.getRoleData().getPreviousColor() != null || loggerMessage.getRoleData().getCurrentColor() != null) {

                            // Add new Fields with Information.
                            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Old color**", (loggerMessage.getRoleData().getPreviousColor() != null ?
                                    loggerMessage.getRoleData().getPreviousColor() : Color.gray).getRGB() + ""));
                            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**New color**", (loggerMessage.getRoleData().getCurrentColor() != null ?
                                    loggerMessage.getRoleData().getCurrentColor() : Color.gray).getRGB() + ""));
                            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "** **", "** **"));
                        }

                        // Create empty lists in case of them being null
                        if (loggerMessage.getRoleData().getPreviousPermission() == null)
                            loggerMessage.getRoleData().setPreviousPermission(EnumSet.noneOf(Permission.class));

                        if (loggerMessage.getRoleData().getCurrentPermission() == null)
                            loggerMessage.getRoleData().setCurrentPermission(EnumSet.noneOf(Permission.class));

                        // Create StringBuilder for Permission diff.
                        StringBuilder stringBuilder = new StringBuilder(loggerMessage.getRoleData().getCurrentPermission().stream()
                                .anyMatch(permission -> !loggerMessage.getRoleData().getPreviousPermission().contains(permission)) ? ":white_check_mark:" : ":no_entry:").append(" ");

                        // Go through every message in currentPermission and add them to the String.
                        for (Permission permission : loggerMessage.getRoleData().getCurrentPermission().stream()
                                .filter(permission -> !loggerMessage.getRoleData().getPreviousPermission().contains(permission)).toList()) {
                            if (stringBuilder.length() >= 22) {
                                stringBuilder.append("\n:white_check_mark: ").append(permission.getName());
                            } else {
                                stringBuilder.append(permission.getName());
                            }
                        }

                        // Go through every message in previousPermission and add them to the String.
                        for (Permission permission : loggerMessage.getRoleData().getPreviousPermission().stream()
                                .filter(permission -> !loggerMessage.getRoleData().getCurrentPermission().contains(permission)).toList()) {
                            if (stringBuilder.length() >= 11) {
                                stringBuilder.append("\n:no_entry: ").append(permission.getName());
                            } else {
                                stringBuilder.append(permission.getName());
                            }
                        }

                        // Add the String from the StringBuilder as Embed to the Message
                        if (!stringBuilder.toString().isEmpty())
                            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**New permissions**", stringBuilder.toString()));

                    } else {
                        // Check if the Role has been created.
                        if (loggerMessage.getRoleData().isCreated()) {

                            // Set description to new Role created.
                            webhookEmbedBuilder.setDescription(":family_mmb: ``" + loggerMessage.getRoleData().getCurrentName() + "`` **has been created.**");
                        } else {

                            // Set description to Role deleted.
                            webhookEmbedBuilder.setDescription(":family_mmb: ``" + loggerMessage.getRoleData().getCurrentName() + "`` **has been deleted.**");
                        }
                    }

                    // Set the new Webhook Message.
                    modified = true;
                }
            }
            // Check if it's a User leave log.
            else if (loggerMessage.getType() == LoggerMessage.LogTyp.SERVER_LEAVE) {
                if (logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                        loggerMessages.getUserData() != null &&
                        loggerMessages.getUserData().getUser().getIdLong() == loggerMessage.getUserData().getUser().getIdLong() &&
                        !loggerMessages.isCanceled()).anyMatch(loggerMessages -> loggerMessages.getType() == LoggerMessage.LogTyp.SERVER_JOIN || loggerMessages.getType() == LoggerMessage.LogTyp.USER_BAN)) {

                    // Cancel every Log-Message which indicates that the person joined the Server or got banned.
                    logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                            loggerMessages.getUserData() != null &&
                            loggerMessages.getUserData().getUser().getIdLong() == loggerMessage.getUserData().getUser().getIdLong() &&
                            !loggerMessages.isCanceled() && (loggerMessages.getType() == LoggerMessage.LogTyp.SERVER_JOIN ||
                            loggerMessages.getType() == LoggerMessage.LogTyp.USER_BAN)).forEach(loggerMessages -> loggerMessages.setCanceled(true));

                    // Set the new Webhook Message.
                    webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(loggerMessage.getUserData().getUser().getAsTag(),
                            loggerMessage.getUserData().getUser().getAvatarUrl(), null));

                    if (logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                                    loggerMessages.getUserData() != null &&
                                    loggerMessages.getUserData().getUser().getIdLong() == loggerMessage.getUserData().getUser().getIdLong())
                            .anyMatch(loggerMessages -> loggerMessages.getType() == LoggerMessage.LogTyp.USER_BAN)) {
                        webhookEmbedBuilder.setDescription(loggerMessage.getUserData().getUser().getAsMention() + " **has been banned.** ");
                    } else{
                        webhookEmbedBuilder.setDescription(loggerMessage.getUserData().getUser().getAsMention() + " **joined and left this Server.** ");
                    }

                    modified = true;
                }
            }

            // add the created WebhookEmbedBuilder as WebhookEmbed to the WebhookMessage Builder.
            webhookMessageBuilder.addEmbeds(webhookEmbedBuilder.build());

            // If the message has been modified change the WebhookMessage.
            if (modified) {
                loggerMessage.setWebhookMessage(webhookMessageBuilder.build());
            }

            // Create new Thread for Log-Message to send.
            new Thread(() -> {

                // Let it wait for 10 seconds.
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ignored) {
                }

                // If not canceled send it.
                if (!loggerMessage.isCanceled()) {
                    Webhook.sendWebhook(loggerMessage, loggerMessage.getWebhookMessage(), loggerMessage.getId(), loggerMessage.getAuthCode(), true);
                }

                // Remove it from the list.
                logs.remove(loggerMessage);
            }).start();
        }
    }
}

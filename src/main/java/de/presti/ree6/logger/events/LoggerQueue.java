package de.presti.ree6.logger.events;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.presti.ree6.bot.util.WebhookUtil;
import de.presti.ree6.logger.events.implentation.LogMessageMember;
import de.presti.ree6.logger.events.implentation.LogMessageRole;
import de.presti.ree6.logger.events.implentation.LogMessageUser;
import de.presti.ree6.logger.events.implentation.LogMessageVoice;
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

    // A List of every Log-Message.
    final ArrayList<LogMessage> logs = new ArrayList<>();

    /**
     * Add a Logging Message into the List.
     *
     * @param loggerMessage the logging message.
     */
    public void add(LogMessage loggerMessage) {
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
            if (loggerMessage.getType() == LogTyp.VC_JOIN && loggerMessage instanceof LogMessageVoice logMessageVoice) {
                if (logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == logMessageVoice.getId() &&
                        loggerMessages instanceof LogMessageVoice logMessageVoice1 && logMessageVoice1.getMember() == logMessageVoice.getMember() &&
                        !loggerMessages.isCanceled()).anyMatch(loggerMessages -> loggerMessages.getType() == LogTyp.VC_LEAVE)) {

                    // Cancel every Log-Message which indicates that the person left.
                    logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                            !loggerMessages.isCanceled() &&
                            loggerMessages instanceof LogMessageVoice logMessageVoice1 &&
                            logMessageVoice1.getMember() == logMessageVoice.getMember() &&
                            loggerMessages.getType() == LogTyp.VC_LEAVE).forEach(loggerMessages -> loggerMessages.setCanceled(true));

                    // Set the new Webhook Message.
                    webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(logMessageVoice.getMember().getUser().getAsTag(),
                            logMessageVoice.getMember().getUser().getAvatarUrl(), null));
                    webhookEmbedBuilder.setDescription(logMessageVoice.getMember().getAsMention() + " **rejoined the Voice-channel** " +
                            logMessageVoice.getCurrentVoiceChannel().getAsMention());

                    modified = true;
                }
            }
            // Check if it's a VoiceChannel Move log.
            else if (loggerMessage.getType() == LogTyp.VC_MOVE && loggerMessage instanceof LogMessageVoice logMessageVoice) {
                if (logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                        loggerMessages instanceof LogMessageVoice logMessageVoice1 && logMessageVoice1.getMember() == logMessageVoice.getMember() &&
                        !loggerMessages.isCanceled()).anyMatch(loggerMessages -> loggerMessages.getType() == LogTyp.VC_MOVE)) {

                    // Cancel every Log-Message which indicates that the person moved.
                    logs.stream().filter(loggerMessages -> loggerMessages.getId() == loggerMessage.getId() &&
                            loggerMessages != loggerMessage &&
                            loggerMessages instanceof LogMessageVoice logMessageVoice1 &&
                            logMessageVoice1.getMember() == logMessageVoice.getMember() && !loggerMessages.isCanceled() &&
                            loggerMessages.getType() == LogTyp.VC_MOVE).forEach(loggerMessages -> loggerMessages.setCanceled(true));

                    // Set the new Webhook Message.
                    webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(logMessageVoice.getMember().getUser().getAsTag(),
                            logMessageVoice.getMember().getUser().getAvatarUrl(), null));
                    webhookEmbedBuilder.setDescription(logMessageVoice.getMember().getUser().getAsMention() + " **moved through many Voice-channels and is now in** "
                            + logMessageVoice.getCurrentVoiceChannel().getAsMention());

                    modified = true;
                }
            }
            //Check if it's a VoiceChannel Leave log.
            else if (loggerMessage.getType() == LogTyp.VC_LEAVE && loggerMessage instanceof LogMessageVoice logMessageVoice) {
                if (logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                        loggerMessages instanceof LogMessageVoice logMessageVoice1 && logMessageVoice1.getMember() == logMessageVoice.getMember() &&
                        !loggerMessages.isCanceled()).anyMatch(loggerMessages -> loggerMessages.getType() == LogTyp.VC_JOIN)) {

                    // Cancel every Log-Message which indicates that the person joined.
                    logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                            loggerMessages instanceof LogMessageVoice logMessageVoice1 &&
                            logMessageVoice1.getMember() == logMessageVoice.getMember() &&
                            !loggerMessages.isCanceled() && loggerMessages.getType() == LogTyp.VC_JOIN).forEach(loggerMessages -> loggerMessages.setCanceled(true));

                    // Set the new Webhook Message.
                    webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(logMessageVoice.getMember().getUser().getAsTag(),
                            logMessageVoice.getMember().getUser().getAvatarUrl(), null));
                    webhookEmbedBuilder.setDescription(logMessageVoice.getMember().getUser().getAsMention() + " **joined and left the Voice-channel** " +
                            logMessageVoice.getPreviousVoiceChannel().getAsMention());

                    modified = true;
                }
            }
            // Check if it's a Nickname Change Log.
            else if (loggerMessage.getType() == LogTyp.NICKNAME_CHANGE && loggerMessage instanceof LogMessageMember logMessageMember) {
                if (logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                        loggerMessages instanceof LogMessageMember logMessageMember1 && logMessageMember1.getMember() == logMessageMember.getMember() &&
                        !loggerMessages.isCanceled()).anyMatch(loggerMessages -> loggerMessages.getType() == LogTyp.NICKNAME_CHANGE)) {

                    // Get the latest previous UserData.
                    LogMessageMember memberData = (LogMessageMember) logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage &&
                            loggerMessages.getId() == loggerMessage.getId() &&
                            loggerMessages instanceof LogMessageMember logMessageMember1 &&
                            logMessageMember1.getMember() == logMessageMember.getMember() &&
                            !loggerMessages.isCanceled() && loggerMessages.getType() == LogTyp.NICKNAME_CHANGE).findFirst().orElse(null);

                    // Cancel every Log-Message which indicates that the person changed their name.
                    logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                            loggerMessages instanceof LogMessageMember logMessageMember1 &&
                            logMessageMember1.getMember() == logMessageMember.getMember() &&
                            !loggerMessages.isCanceled() && loggerMessages.getType() == LogTyp.NICKNAME_CHANGE).forEach(loggerMessages -> loggerMessages.setCanceled(true));

                    // Change the current previous Nickname to the old one.
                    if (memberData != null && memberData.getPreviousName() != null) logMessageMember.setPreviousName(memberData.getPreviousName());

                    // Set the new Webhook Message.
                    webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(logMessageMember.getMember().getUser().getAsTag(),
                            logMessageMember.getMember().getUser().getAvatarUrl(), null));
                    webhookEmbedBuilder.setDescription("The Nickname of " + logMessageMember.getMember().getAsMention() + " has been changed.\n**New Nickname:**\n" +
                            logMessageMember.getCurrentName() + "\n**Old Nickname:**\n" +
                            (memberData != null && memberData.getPreviousName() != null ? memberData.getPreviousName() : logMessageMember.getMember().getUser().getName()));

                    modified = true;
                }
            }
            // Check if it's a Member Role Change log.
            else if (loggerMessage.getType() == LogTyp.MEMBERROLE_CHANGE && loggerMessage instanceof LogMessageMember logMessageMember) {
                if (logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages instanceof LogMessageMember logMessageMember1 &&
                        logMessageMember1.getMember() == logMessageMember.getMember()
                        && loggerMessages.getId() == loggerMessage.getId() &&
                        !loggerMessages.isCanceled()).anyMatch(loggerMessages -> loggerMessages.getType() == LogTyp.MEMBERROLE_CHANGE)) {

                    // Get the latest MemberData.
                    LogMessageMember memberData = (LogMessageMember) logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage &&
                            loggerMessages.getId() == loggerMessage.getId() &&
                            loggerMessages instanceof LogMessageMember logMessageMember1 &&
                            logMessageMember1.getMember() == logMessageMember.getMember() &&
                            !loggerMessages.isCanceled() && loggerMessages.getType() == LogTyp.MEMBERROLE_CHANGE).findFirst().orElse(null);

                    if (memberData != null) {
                        // Cancel every other LogEvent of that Typ.
                        logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                                loggerMessages instanceof LogMessageMember logMessageMember1 &&
                                logMessageMember1.getMember() == logMessageMember.getMember() &&
                                !loggerMessages.isCanceled() && loggerMessages.getType() == LogTyp.MEMBERROLE_CHANGE).forEach(loggerMessages -> loggerMessages.setCanceled(true));

                        // Check if the RemoveRoles is null or empty.
                        if ((logMessageMember.getRemovedRoles() == null || logMessageMember.getRemovedRoles().isEmpty()) &&
                                memberData.getRemovedRoles() != null && !memberData.getRemovedRoles().isEmpty()) {

                            // Set the current removed Roles to the one from the latest.
                            logMessageMember.setRemovedRoles(memberData.getRemovedRoles());
                        }

                        // Check if the AddedRoles is null or empty.
                        if ((logMessageMember.getAddedRoles() == null || logMessageMember.getAddedRoles().isEmpty()) &&
                                memberData.getAddedRoles() != null && !memberData.getAddedRoles().isEmpty()) {

                            // Set the current added Roles to the one from the latest.
                            logMessageMember.setAddedRoles(memberData.getAddedRoles());
                        }

                        // Check if the addedRoles and removeRoles are null if so create new List.
                        if (logMessageMember.getAddedRoles() == null)
                            logMessageMember.setAddedRoles(new ArrayList<>());

                        if (logMessageMember.getRemovedRoles() == null)
                            logMessageMember.setRemovedRoles(new ArrayList<>());

                        // Check if the Lists are Empty if not remove duplicated entries.
                        if (!logMessageMember.getRemovedRoles().isEmpty() && !logMessageMember.getAddedRoles().isEmpty()) {
                            logMessageMember.getRemovedRoles().removeIf(role -> role == null || logMessageMember.getAddedRoles().contains(role));
                        }

                        // Merge both lists with the current List.
                        if (memberData.getRemovedRoles() != null && !memberData.getRemovedRoles().isEmpty() && memberData.getRemovedRoles().stream().anyMatch(role -> role != null && logMessageMember.getAddedRoles().contains(role) && !logMessageMember.getRemovedRoles().contains(role))) {
                            try {
                                memberData.getRemovedRoles().stream().filter(role -> role != null && !logMessageMember.getAddedRoles().contains(role) &&
                                        !logMessageMember.getRemovedRoles().contains(role)).forEach(role -> logMessageMember.getRemovedRoles().add(role));
                            } catch (Exception ignore) {
                            }
                        }

                        if (memberData.getAddedRoles() != null && !memberData.getAddedRoles().isEmpty() && memberData.getAddedRoles().stream().anyMatch(role -> role != null && !logMessageMember.getAddedRoles().contains(role) && logMessageMember.getRemovedRoles().contains(role))) {
                            try {
                                memberData.getAddedRoles().stream().filter(role -> role != null && !logMessageMember.getAddedRoles().contains(role) &&
                                        !logMessageMember.getRemovedRoles().contains(role)).forEach(role -> logMessageMember.getAddedRoles().add(role));
                            } catch (Exception ignore) {
                            }
                        }
                    }

                    // StringBuilder to convert the List into a single String.
                    StringBuilder stringBuilder = new StringBuilder();

                    // Add the Entries into the String.
                    logMessageMember.getAddedRoles().forEach(role -> stringBuilder.append(":white_check_mark:").append(" ").append(role.getName()).append("\n"));
                    logMessageMember.getRemovedRoles().forEach(role -> stringBuilder.append(":no_entry:").append(" ").append(role.getName()).append("\n"));

                    // Set Embed Elements.
                    webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(logMessageMember.getMember().getUser().getAsTag(),
                            logMessageMember.getMember().getUser().getAvatarUrl(), null));
                    webhookEmbedBuilder.setThumbnailUrl(logMessageMember.getMember().getUser().getAvatarUrl());
                    webhookEmbedBuilder.setDescription(":writing_hand: " + logMessageMember.getMember().getAsMention() + " **has been updated.**");

                    if (!logMessageMember.getAddedRoles().isEmpty() || !logMessageMember.getRemovedRoles().isEmpty())
                        webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Roles:**", stringBuilder.toString()));

                    modified = true;
                }
            }
            // Check if it's a Role Update log.
            else if (loggerMessage.getType() == LogTyp.ROLEDATA_CHANGE && loggerMessage instanceof LogMessageRole logMessageRole) {
                if (logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                        loggerMessages instanceof LogMessageRole logMessageRole1 &&
                        logMessageRole1.getRoleId() == logMessageRole.getId() &&
                        !loggerMessages.isCanceled()).anyMatch(loggerMessages -> loggerMessages.getType() == LogTyp.ROLEDATA_CHANGE)) {

                    // Get the latest RoleData.
                    LogMessageRole roleData = (LogMessageRole) logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                            loggerMessages instanceof LogMessageRole logMessageRole1 &&
                            logMessageRole1.getRoleId() == logMessageRole.getId() &&
                            !loggerMessages.isCanceled() && loggerMessages.getType() == LogTyp.ROLEDATA_CHANGE).findFirst().orElse(null);

                    // Cancel every Log-Message which indicates that the person changed their name.
                    logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                            loggerMessages instanceof LogMessageRole logMessageRole1 &&
                            logMessageRole1.getRoleId() == logMessageRole.getId() &&
                            !loggerMessages.isCanceled() && loggerMessages.getType() == LogTyp.ROLEDATA_CHANGE).forEach(loggerMessages -> loggerMessages.setCanceled(true));

                    // Start merging the Role Permissions
                    if (roleData != null) {

                        // Check if the latest Role Data has a previous Name.
                        if (roleData.getPreviousName() != null && !roleData.getPreviousName().isEmpty()) {

                            // Set the previous Name of the current Role Data to the latest one.
                            logMessageRole.setPreviousName(roleData.getPreviousName());
                        }

                        // Check if the latest Role Data has a current Name.
                        if ((logMessageRole.getCurrentName() == null || logMessageRole.getCurrentName().isEmpty()) &&
                                roleData.getCurrentName() != null && !roleData.getCurrentName().isEmpty()) {

                            // Set the current Name of the current Role Data to the latest one.
                            logMessageRole.setCurrentName(roleData.getCurrentName());
                        }

                        // Check if the latest Role Data has previous Permissions.
                        if (roleData.getPreviousPermission() != null && !roleData.getPreviousPermission().isEmpty()) {

                            // Set the previous Permissions.
                            logMessageRole.setPreviousPermission(roleData.getPreviousPermission());
                        }

                        // Check if the latest Role Data has current Permissions.
                        if ((logMessageRole.getCurrentPermission() == null || logMessageRole.getCurrentPermission().isEmpty())
                                && roleData.getCurrentPermission() != null && !roleData.getCurrentPermission().isEmpty()) {

                            // Set the current Permissions.
                            logMessageRole.setCurrentPermission(roleData.getCurrentPermission());
                        }

                        // Check if the latest Role Data has a previous Color.
                        if (roleData.getPreviousColor() != null) {

                            // Set the previous Color.
                            logMessageRole.setPreviousColor(roleData.getPreviousColor());
                        }

                        // Check if the latest Role Data has a current Color.
                        if (logMessageRole.getCurrentColor() == null && roleData.getCurrentColor() != null) {

                            // Set the current Color.
                            logMessageRole.setCurrentColor(roleData.getCurrentColor());
                        }

                        // Check if the latest has another value for Hoisted and if the current Value is the default Value or not.
                        if (!logMessageRole.isChangedHoisted() && roleData.isChangedHoisted()) {

                            // Set the current Value to the one from the latest.
                            logMessageRole.setChangedHoisted(roleData.isChangedHoisted());
                        }

                        // Check if the latest has another value for Mentioned and if the current Value is the default Value or not.
                        if (!logMessageRole.isChangedMentioned() && roleData.isChangedMentioned()) {

                            // Set the current Value to the one from the latest.
                            logMessageRole.setChangedMentioned(roleData.isChangedMentioned());
                        }
                    }

                    // Set the author of the WebhookMessage.
                    webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(loggerMessage.getGuild().getName(), loggerMessage.getGuild().getIconUrl(), null));

                    // Check if it isn't a new created or deleted Role.
                    if (!logMessageRole.isCreated() && !logMessageRole.isDeleted()) {

                        // Set update as Description
                        webhookEmbedBuilder.setDescription(":family_mmb: ``" + logMessageRole.getCurrentName() + "`` **has been updated.**");

                        // Check if there is a previous and current Name.
                        if (logMessageRole.getPreviousName() != null && logMessageRole.getCurrentName() != null) {

                            // Add new Fields with Information.
                            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Old name**", logMessageRole.getPreviousName()));
                            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**New name**", logMessageRole.getCurrentName()));
                            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "** **", "** **"));
                        }

                        // Check if the Mentioned has been changed or not.
                        if (logMessageRole.isChangedMentioned()) {

                            // Add new Fields with Information.
                            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Old mentionable**", !logMessageRole.isMentioned() + ""));
                            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**New mentionable**", logMessageRole.isMentioned() + ""));
                            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "** **", "** **"));
                        }

                        // Check if the Hoisted has been changed or not.
                        if (logMessageRole.isChangedHoisted()) {

                            // Add new Fields with Information.
                            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Old hoist**", !logMessageRole.isHoisted() + ""));
                            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**New hoist**", logMessageRole.isHoisted() + ""));
                            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "** **", "** **"));
                        }

                        // Check if a new Color has been added or changed.
                        if (logMessageRole.getPreviousColor() != null || logMessageRole.getCurrentColor() != null) {

                            // Add new Fields with Information.
                            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**Old color**", (logMessageRole.getPreviousColor() != null ?
                                    logMessageRole.getPreviousColor() : Color.gray).getRGB() + ""));
                            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "**New color**", (logMessageRole.getCurrentColor() != null ?
                                    logMessageRole.getCurrentColor() : Color.gray).getRGB() + ""));
                            webhookEmbedBuilder.addField(new WebhookEmbed.EmbedField(true, "** **", "** **"));
                        }

                        // Create empty lists in case of them being null
                        if (logMessageRole.getPreviousPermission() == null)
                            logMessageRole.setPreviousPermission(EnumSet.noneOf(Permission.class));

                        if (logMessageRole.getCurrentPermission() == null)
                            logMessageRole.setCurrentPermission(EnumSet.noneOf(Permission.class));

                        // Create StringBuilder for Permission diff.
                        StringBuilder stringBuilder = new StringBuilder(logMessageRole.getCurrentPermission().stream()
                                .anyMatch(permission -> !logMessageRole.getPreviousPermission().contains(permission)) ? ":white_check_mark:" : ":no_entry:").append(" ");

                        // Go through every message in currentPermission and add them to the String.
                        for (Permission permission : logMessageRole.getCurrentPermission().stream()
                                .filter(permission -> !logMessageRole.getPreviousPermission().contains(permission)).toList()) {
                            if (stringBuilder.length() >= 22) {
                                stringBuilder.append("\n:white_check_mark: ").append(permission.getName());
                            } else {
                                stringBuilder.append(permission.getName());
                            }
                        }

                        // Go through every message in previousPermission and add them to the String.
                        for (Permission permission : logMessageRole.getPreviousPermission().stream()
                                .filter(permission -> !logMessageRole.getCurrentPermission().contains(permission)).toList()) {
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
                        if (logMessageRole.isCreated()) {

                            // Set description to new Role created.
                            webhookEmbedBuilder.setDescription(":family_mmb: ``" + logMessageRole.getCurrentName() + "`` **has been created.**");
                        } else {

                            // Set description to Role deleted.
                            webhookEmbedBuilder.setDescription(":family_mmb: ``" + logMessageRole.getCurrentName() + "`` **has been deleted.**");
                        }
                    }

                    // Set the new Webhook Message.
                    modified = true;
                }
            }
            // Check if it's a User leave log.
            else if (loggerMessage.getType() == LogTyp.SERVER_LEAVE && loggerMessage instanceof LogMessageUser logMessageUser) {
                if (logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                        loggerMessages instanceof LogMessageUser logMessageUser1 &&
                        logMessageUser1.getUser().getIdLong() == logMessageUser.getUser().getIdLong() &&
                        !loggerMessages.isCanceled()).anyMatch(loggerMessages -> loggerMessages.getType() == LogTyp.SERVER_JOIN || loggerMessages.getType() == LogTyp.USER_BAN)) {

                    // Cancel every Log-Message which indicates that the person joined the Server or got banned.
                    logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                            loggerMessages instanceof LogMessageUser logMessageUser1 &&
                            logMessageUser1.getUser().getIdLong() == logMessageUser.getUser().getIdLong() &&
                            !loggerMessages.isCanceled() && (loggerMessages.getType() == LogTyp.SERVER_JOIN ||
                            loggerMessages.getType() == LogTyp.USER_BAN)).forEach(loggerMessages -> loggerMessages.setCanceled(true));

                    // Set the new Webhook Message.
                    webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(logMessageUser.getUser().getAsTag(),
                            logMessageUser.getUser().getAvatarUrl(), null));

                    if (logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId() &&
                                    loggerMessages instanceof LogMessageUser logMessageUser1 &&
                                    logMessageUser1.getUser().getIdLong() == logMessageUser.getUser().getIdLong())
                            .anyMatch(loggerMessages -> loggerMessages.getType() == LogTyp.USER_BAN)) {
                        webhookEmbedBuilder.setDescription(logMessageUser.getUser().getAsMention() + " **has been banned.** ");
                    } else {
                        webhookEmbedBuilder.setDescription(logMessageUser.getUser().getAsMention() + " **joined and left this Server.** ");
                    }

                    modified = true;
                }
            } else if (loggerMessage.getType() == LogTyp.SERVER_INVITE && loggerMessage instanceof LogMessageUser logMessageUser) {
                if (logs.stream().filter(loggerMessages -> loggerMessages != loggerMessage && loggerMessages.getId() == loggerMessage.getId()
                        && loggerMessages instanceof LogMessageUser logMessageUser1 &&
                        logMessageUser1.getUser().getIdLong() == logMessageUser.getUser().getIdLong()
                        && !loggerMessages.isCanceled()).anyMatch(loggerMessages -> loggerMessages.getType() == LogTyp.SERVER_LEAVE)) {
                    loggerMessage.setCanceled(true);
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
                    WebhookUtil.sendWebhook(loggerMessage, loggerMessage.getWebhookMessage(), loggerMessage.getId(), loggerMessage.getAuthCode(), true);
                }

                // Remove it from the list.
                logs.remove(loggerMessage);
            }).start();
        }
    }
}

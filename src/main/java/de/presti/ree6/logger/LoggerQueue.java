package de.presti.ree6.logger;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.bot.Webhook;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;

public class LoggerQueue {
    final ArrayList<LoggerMessage> logs = new ArrayList<>();

    public void add(LoggerMessage lm) {
        if (!logs.contains(lm)) {
            logs.add(lm);

            boolean changedMessage = false;

            WebhookMessageBuilder wm = new WebhookMessageBuilder();

            wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
            wm.setUsername("Ree6Logs");

            WebhookEmbedBuilder we = new WebhookEmbedBuilder();
            we.setColor(Color.BLACK.getRGB());
            we.setFooter(new WebhookEmbed.EmbedFooter(lm.getGuild().getName(), (lm.getGuild().getIconUrl() != null ? lm.getGuild().getIconUrl() : null)));
            we.setTimestamp(Instant.now());

            if (lm.getType() == LoggerMessage.LogTyp.VC_JOIN) {
                if (lm.getGuild() != null) {
                    if (getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.VC_LEAVE
                            && loggerMessage != lm).anyMatch(loggerMessage -> !loggerMessage.isCancel())) {
                        getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.VC_LEAVE).forEach(loggerMessage -> loggerMessage.setCancel(true));

                        changedMessage = true;

                        we.setAuthor(new WebhookEmbed.EmbedAuthor(lm.getVoiceData().getMember().getUser().getAsTag(), lm.getVoiceData().getMember().getUser().getAvatarUrl(), null));
                        we.setDescription(lm.getVoiceData().getMember().getAsMention() + " **rejoined the Voicechannel** ``" + lm.getVoiceData().getCurrentVoiceChannel().getName() + "``");
                    }
                }
            } else if (lm.getType() == LoggerMessage.LogTyp.VC_MOVE) {
                if (lm.getGuild() != null) {
                    if (getLogsByGuild(lm.getGuild()).stream().anyMatch(loggerMessage -> loggerMessage.getType() == lm.getType() && loggerMessage != lm && !loggerMessage.isCancel())) {
                        getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == lm.getType()
                                && loggerMessage != lm && !loggerMessage.isCancel()).forEach(loggerMessage -> loggerMessage.setCancel(true));

                        changedMessage = true;

                        we.setAuthor(new WebhookEmbed.EmbedAuthor(lm.getVoiceData().getMember().getUser().getAsTag(), lm.getVoiceData().getMember().getUser().getAvatarUrl(), null));
                        we.setDescription(lm.getVoiceData().getMember().getUser().getAsMention() + " **moved through many Voicechannels and is now in** ``" + lm.getVoiceData().getCurrentVoiceChannel().getName() + "``");
                    }
                }
            } else if (lm.getType() == LoggerMessage.LogTyp.VC_LEAVE) {
                if (lm.getGuild() != null) {
                    if (getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.VC_JOIN
                            && loggerMessage != lm).anyMatch(loggerMessage -> !loggerMessage.isCancel())) {
                        getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.VC_JOIN && !loggerMessage.isCancel()).forEach(loggerMessage -> loggerMessage.setCancel(true));

                        changedMessage = true;

                        we.setAuthor(new WebhookEmbed.EmbedAuthor(lm.getVoiceData().getMember().getUser().getAsTag(), lm.getVoiceData().getMember().getUser().getAvatarUrl(), null));
                        we.setDescription(lm.getVoiceData().getMember().getUser().getAsMention() + " **joined and left the Voicechannel** ``" + lm.getVoiceData().getPreviousVoiceChannel().getName() + "``");
                    }
                }
            } else if (lm.getType() == LoggerMessage.LogTyp.NICKNAME_CHANGE) {
                if (lm.getGuild() != null) {
                    if (getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == lm.getType()
                            && loggerMessage != lm).anyMatch(loggerMessage -> !loggerMessage.isCancel())) {

                        String oldName = getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.NICKNAME_CHANGE
                                && loggerMessage != lm && !loggerMessage.isCancel()).findFirst().get().getMemberData().getPreviousName();

                        getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == lm.getType()
                                && loggerMessage != lm && !loggerMessage.isCancel()).forEach(loggerMessage -> loggerMessage.setCancel(true));

                        lm.getMemberData().setPreviousName(oldName);

                        changedMessage = true;
                        we.setDescription("The Nickname of " + lm.getMemberData().getMember().getAsMention() + " has been changed.\n**New Nickname:**\n" + lm.getMemberData().getCurrentName() + "\n**Old Nickname:**\n" + (oldName != null ? oldName : lm.getMemberData().getMember().getUser().getName()));
                    }
                }
            } else if (lm.getType() == LoggerMessage.LogTyp.ROLEDATA_CHANGE) {
                if (lm.getGuild() != null) {
                    if (getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == lm.getType()
                            && loggerMessage != lm).anyMatch(loggerMessage -> !loggerMessage.isCancel())) {
                        LoggerRoleData currentRoleData = lm.getRoleData();
                        LoggerRoleData oldRoleData = getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.ROLEDATA_CHANGE).filter(loggerMessage -> loggerMessage != lm)
                                .filter(loggerMessage -> !loggerMessage.isCancel()).filter(loggerMessage -> loggerMessage.getRoleData().getRoleId() == currentRoleData.getRoleId()).findFirst().get().getRoleData();

                        getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.ROLEDATA_CHANGE).filter(loggerMessage -> loggerMessage != lm).forEach(loggerMessage -> loggerMessage.setCancel(true));

                        //TODO rework this whole data merge.

                        if (oldRoleData != null && oldRoleData.getPreviousName() != null) {
                            currentRoleData.setPreviousName(oldRoleData.getPreviousName());
                        }

                        if (oldRoleData != null && currentRoleData.getCurrentColor() == null && oldRoleData.getPreviousName() != null) {
                            currentRoleData.setCurrentName(oldRoleData.getCurrentName());
                        }

                        if (oldRoleData != null && oldRoleData.getPreviousPermission() != null) {
                            currentRoleData.setPreviousPermission(oldRoleData.getPreviousPermission());
                        }

                        if (oldRoleData != null && (currentRoleData.getCurrentPermission() == null || currentRoleData.getCurrentPermission().isEmpty()) && oldRoleData.getCurrentPermission() != null) {
                            currentRoleData.setCurrentPermission(oldRoleData.getCurrentPermission());
                        }

                        if (oldRoleData != null && oldRoleData.getPreviousColor() != null) {
                            currentRoleData.setPreviousColor(oldRoleData.getPreviousColor());
                        }

                        if (oldRoleData != null && currentRoleData.getCurrentColor() == null && oldRoleData.getCurrentColor() != null) {
                            currentRoleData.setCurrentColor(oldRoleData.getCurrentColor());
                        }

                        if (oldRoleData != null && !currentRoleData.isChangedHoisted() && oldRoleData.isChangedHoisted()) {
                            currentRoleData.setHoisted(oldRoleData.isHoisted());
                        }
                        if (oldRoleData != null && !currentRoleData.isChangedMentioned() && oldRoleData.isChangedMentioned()) {
                            currentRoleData.setMentioned(oldRoleData.isMentioned());
                        }

                        lm.setRoleData(currentRoleData);
                        we.setAuthor(new WebhookEmbed.EmbedAuthor(lm.getGuild().getName(), lm.getGuild().getIconUrl(), null));

                        if (!currentRoleData.isCreated() && !currentRoleData.isDeleted()) {
                            changedMessage = true;
                            we.setDescription(":family_mmb: ``" + currentRoleData.getPreviousName() + "`` **has been updated.**");

                            if (currentRoleData.getPreviousName() != null && currentRoleData.getCurrentName() != null) {
                                we.addField(new WebhookEmbed.EmbedField(true, "**Old name**", currentRoleData.getPreviousName()));
                                we.addField(new WebhookEmbed.EmbedField(true, "**New name**", currentRoleData.getCurrentName()));
                                we.addField(new WebhookEmbed.EmbedField(true, "**", "**"));
                            }

                            if (currentRoleData.isChangedMentioned()) {
                                we.addField(new WebhookEmbed.EmbedField(true, "**Old mentionable**", !currentRoleData.isMentioned() + ""));
                                we.addField(new WebhookEmbed.EmbedField(true, "**New mentionable**", currentRoleData.isMentioned() + ""));
                                we.addField(new WebhookEmbed.EmbedField(true, "**", "**"));
                            }

                            if (currentRoleData.isChangedHoisted()) {
                                we.addField(new WebhookEmbed.EmbedField(true, "**Old hoist**", !currentRoleData.isHoisted() + ""));
                                we.addField(new WebhookEmbed.EmbedField(true, "**New hoist**", currentRoleData.isHoisted() + ""));
                                we.addField(new WebhookEmbed.EmbedField(true, "**", "**"));
                            }

                            if (currentRoleData.getPreviousColor() != null) {
                                we.addField(new WebhookEmbed.EmbedField(true, "**Old color**", (currentRoleData.getPreviousColor() != null ? currentRoleData.getPreviousColor() : Color.gray).getRGB() + ""));
                                we.addField(new WebhookEmbed.EmbedField(true, "**New color**", (currentRoleData.getCurrentColor() != null ? currentRoleData.getCurrentColor() : Color.gray).getRGB() + ""));
                                we.addField(new WebhookEmbed.EmbedField(true, "**", "**"));
                            }

                            StringBuilder finalString = new StringBuilder();

                            boolean b = false;

                            if (currentRoleData.getCurrentPermission() != null) {
                                for (Permission r : currentRoleData.getCurrentPermission()) {
                                    if (!currentRoleData.getPreviousPermission().contains(r)) {
                                        if (b) {
                                            finalString.append("\n:white_check_mark: ").append(r.getName());
                                        } else {
                                            finalString.append(":white_check_mark: ").append(r.getName());
                                            b = true;
                                        }
                                    }
                                }
                            }

                            if (currentRoleData.getPreviousPermission() != null) {
                                for (Permission r : currentRoleData.getPreviousPermission()) {
                                    if (!currentRoleData.getCurrentPermission().contains(r)) {
                                        if (b) {
                                            finalString.append("\n:no_entry: ").append(r.getName());
                                        } else {
                                            finalString.append(":no_entry: ").append(r.getName());
                                            b = true;
                                        }
                                    }
                                }
                            }

                            if (!finalString.toString().isEmpty()) {
                                we.addField(new WebhookEmbed.EmbedField(true, "**New permissions**", finalString.toString()));
                            }
                        } else {
                            changedMessage = true;
                            if (currentRoleData.isCreated()) {
                                we.setDescription(":family_mmb: ``" + currentRoleData.getPreviousName() + "`` **has been created.**");
                            } else {
                                we.setDescription(":family_mmb: ``" + currentRoleData.getPreviousName() + "`` **has been deleted.**");
                            }
                        }
                    }
                }
            }/* else if(lm.getType() == LoggerMessage.LogTyp.MEMBERROLE_CHANGE) {
                if (lm.getGuild() != null) {
                    if (getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.MEMBERROLE_CHANGE).count() > 1) {
                        ArrayList<Role> oldAddedRoles = getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.MEMBERROLE_CHANGE).filter(loggerMessage -> !loggerMessage.isCancel()).filter(loggerMessage -> loggerMessage.getAddedRoles() != null).toArray().length > 0 ? ((LoggerMessage) (getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.MEMBERROLE_CHANGE).filter(loggerMessage -> !loggerMessage.isCancel()).filter(loggerMessage -> loggerMessage.getAddedRoles() != null).toArray()[0])).getAddedRoles() : null;
                        ArrayList<Role> oldRemoveRoles = getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.MEMBERROLE_CHANGE).filter(loggerMessage -> !loggerMessage.isCancel()).filter(loggerMessage -> loggerMessage.getRemovedRoles() != null).toArray().length > 0 ? ((LoggerMessage) (getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.MEMBERROLE_CHANGE).filter(loggerMessage -> !loggerMessage.isCancel()).filter(loggerMessage -> loggerMessage.getRemovedRoles() != null).toArray()[0])).getRemovedRoles() : null;
                        getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.MEMBERROLE_CHANGE).forEach(loggerMessage -> loggerMessage.setCancel(true));

                        if (lm.getRemovedRoles() == null && oldRemoveRoles != null) {
                            lm.setRemovedRoles(oldRemoveRoles);
                        } else if (oldRemoveRoles == null) {
                            lm.setRemovedRoles(new ArrayList<Role>());
                        }

                        if (lm.getAddedRoles() == null && oldAddedRoles != null) {
                            lm.setAddedRoles(oldAddedRoles);
                        } else if (oldAddedRoles == null) {
                            lm.setAddedRoles(new ArrayList<Role>());
                        }

                        if (lm.getRemovedRoles() != null && lm.getAddedRoles() != null && lm.getRemovedRoles().size() > 0 & lm.getAddedRoles().size() > 0) {
                            lm.getRemovedRoles().removeIf(r -> r != null && lm.getAddedRoles().contains(r));
                        }

                        if (lm.getRemovedRoles() != null && lm.getAddedRoles() != null && oldRemoveRoles != null && oldRemoveRoles.size() > 0) {
                            for (Role r : oldRemoveRoles)
                                if (r != null && (lm.getAddedRoles().size() == 0 || (lm.getAddedRoles().size() > 0 && !lm.getAddedRoles().contains(r)))) {
                                    lm.getRemovedRoles().add(r);
                                }
                        }

                        if (lm.getRemovedRoles() != null && lm.getAddedRoles() != null && oldAddedRoles != null && oldAddedRoles.size() > 0) {
                            for (Role r : oldAddedRoles)
                                if (r != null && (lm.getRemovedRoles().size() == 0 || (lm.getRemovedRoles().size() > 0 && !lm.getRemovedRoles().contains(r)))) {
                                    lm.getAddedRoles().add(r);
                                }
                        }

                        StringBuilder finalString = new StringBuilder();

                        if (lm.getAddedRoles() != null && lm.getAddedRoles().size() > 0) {
                            for (Role r : lm.getAddedRoles()) {
                                finalString.append(":white_check_mark: ").append(r.getName()).append("\n");
                            }
                        }

                        if (lm.getRemovedRoles() != null && lm.getRemovedRoles().size() > 0) {
                            for (Role r : lm.getRemovedRoles()) {
                                finalString.append(":no_entry: ").append(r.getName()).append("\n");
                            }
                        }

                        WebhookMessageBuilder wm = new WebhookMessageBuilder();

                        wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
                        wm.setUsername("Ree6Logs");

                        WebhookEmbedBuilder we = new WebhookEmbedBuilder();
                        we.setColor(Color.BLACK.getRGB());
                        we.setAuthor(new WebhookEmbed.EmbedAuthor(lm.getM().getUser().getAsTag(), lm.getM().getUser().getAvatarUrl(), null));
                        we.setFooter(new WebhookEmbed.EmbedFooter(lm.getGuild().getName(), lm.getGuild().getIconUrl()));
                        we.setTimestamp(Instant.now());

                        we.setDescription(":writing_hand: " + lm.getM().getUser().getAsMention() + " **has been updated.**");
                        we.addField(new WebhookEmbed.EmbedField(true, "**Roles:**", finalString.toString()));

                        wm.addEmbeds(we.build());

                        lm.setWem(wm.build());
                    }
                }
            }*/

            wm.addEmbeds(we.build());

            if (lm.getType() != LoggerMessage.LogTyp.ELSE && changedMessage) lm.setWem(wm.build());

            new Thread(() -> {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ignored) {
                }

                if (!lm.isCancel()) {
                    Webhook.sendWebhook(lm.getWem(), lm.getId(), lm.getAuthCode());
                }

                logs.remove(lm);
            }).start();
        }
    }

    public ArrayList<LoggerMessage> getLogsByGuild(Guild g) {
        ArrayList<LoggerMessage> sheesh = new ArrayList<>();

        for (LoggerMessage lm : logs) {
            if (lm.getGuild() == g) {
                sheesh.add(lm);
            }
        }

        return sheesh;
    }
}

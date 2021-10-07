package de.presti.ree6.logger;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import de.presti.ree6.bot.BotInfo;
import de.presti.ree6.bot.Webhook;
import de.presti.ree6.utils.Logger;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;

public class LoggerQueue {
    final ArrayList<LoggerMessage> logs = new ArrayList<>();

    public void add(LoggerMessage lm) {
        if(!logs.contains(lm)) {
            logs.add(lm);

            boolean temp = false;

            WebhookMessageBuilder wm = new WebhookMessageBuilder();

            wm.setAvatarUrl(BotInfo.botInstance.getSelfUser().getAvatarUrl());
            wm.setUsername("Ree6Logs");

            WebhookEmbedBuilder we = new WebhookEmbedBuilder();
            we.setColor(Color.BLACK.getRGB());
            we.setAuthor(new WebhookEmbed.EmbedAuthor(lm.getM().getUser().getAsTag(), lm.getM().getUser().getAvatarUrl(), null));
            we.setFooter(new WebhookEmbed.EmbedFooter(lm.getGuild().getName(), lm.getGuild().getIconUrl()));
            we.setTimestamp(Instant.now());

            we.setDescription("This is a invalid Body. BodyTyp: " + lm.getType().name());

            if(lm.getType() == LoggerMessage.LogTyp.VC_JOIN) {
                if(lm.getGuild() != null) {
                    if (getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.VC_LEAVE
                            && loggerMessage != lm).anyMatch(loggerMessage -> !loggerMessage.isCancel())) {
                        getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.VC_LEAVE).forEach(loggerMessage -> loggerMessage.setCancel(true));

                        temp = true;
                        we.setDescription(lm.getM().getUser().getAsMention() + " **rejoined the Voicechannel** ``" + lm.getVc().getName() + "``");
                    } else {
                        temp = true;
                    }
                }
            } else if(lm.getType() == LoggerMessage.LogTyp.VC_MOVE) {
                if(lm.getGuild() != null) {
                    if (getLogsByGuild(lm.getGuild()).stream().anyMatch(loggerMessage -> loggerMessage.getType() == lm.getType() && loggerMessage != lm && !loggerMessage.isCancel())) {
                        getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == lm.getType()
                                && loggerMessage != lm && !loggerMessage.isCancel()).forEach(loggerMessage -> loggerMessage.setCancel(true));

                        temp = true;
                        we.setDescription(lm.getM().getUser().getAsMention() + " **moved through many Voicechannels and is now in** ``" + lm.getVc().getName() + "``");
                    }
                }
            } else if(lm.getType() == LoggerMessage.LogTyp.VC_LEAVE) {
                if(lm.getGuild() != null) {
                    if (getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.VC_JOIN
                            && loggerMessage != lm).anyMatch(loggerMessage -> !loggerMessage.isCancel())) {
                        getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.VC_JOIN && !loggerMessage.isCancel()).forEach(loggerMessage -> loggerMessage.setCancel(true));

                        temp = true;
                        we.setDescription(lm.getM().getUser().getAsMention() + " **joined and left the Voicechannel** ``" + lm.getVc().getName() + "``");
                    } else {
                        temp = true;
                    }
                }
            } else if(lm.getType() == LoggerMessage.LogTyp.NICKNAME_CHANGE) {
                if(lm.getGuild() != null) {
                    if (getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == lm.getType()
                            && loggerMessage != lm).anyMatch(loggerMessage -> !loggerMessage.isCancel())) {

                        String oldName = getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.NICKNAME_CHANGE
                                && loggerMessage != lm && !loggerMessage.isCancel()).findFirst().get().getNickname2();

                        getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == lm.getType()
                                && loggerMessage != lm && !loggerMessage.isCancel()).forEach(loggerMessage -> loggerMessage.setCancel(true));

                        // TODO rework this upper part. Update detection part.

                        lm.setNickname2(oldName);

                        temp = true;
                        we.setDescription("The Nickname of " + lm.getM().getAsMention() + " has been changed.\n**New Nickname:**\n" + lm.getM().getNickname() + "\n**Old Nickname:**\n" + (oldName != null ? oldName : lm.getM().getUser().getName()));
                    } else {
                        temp = true;
                    }
                }
            } else if(lm.getType() == LoggerMessage.LogTyp.ROLEDATA_CHANGE) {
                if (lm.getGuild() != null) {
                    if (getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == lm.getType()
                            && loggerMessage != lm).anyMatch(loggerMessage -> !loggerMessage.isCancel())) {
                        LoggerMessage.RoleData currentRoleData = lm.getRoleData();
                        LoggerMessage.RoleData oldRoleData = ((LoggerMessage) (getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.ROLEDATA_CHANGE).filter(loggerMessage -> loggerMessage != lm).filter(loggerMessage -> !loggerMessage.isCancel()).filter(loggerMessage -> loggerMessage.getRoleData().getId().equalsIgnoreCase(currentRoleData.getId())).toArray()[0])).getRoleData();
                        getLogsByGuild(lm.getGuild()).stream().filter(loggerMessage -> loggerMessage.getType() == LoggerMessage.LogTyp.ROLEDATA_CHANGE).filter(loggerMessage -> loggerMessage != lm).forEach(loggerMessage -> loggerMessage.setCancel(true));

                        //TODO rework this part.

                        if (oldRoleData != null && oldRoleData.getOldName() != null) {
                            currentRoleData.setOldName(oldRoleData.getOldName());
                        }

                        if (oldRoleData != null && currentRoleData != null && currentRoleData.getNewName() == null && oldRoleData.getOldName() != null) {
                            currentRoleData.setNewName(oldRoleData.getNewName());
                        }

                        if (oldRoleData != null && currentRoleData != null && oldRoleData.getOldPermissions() != null) {
                            currentRoleData.setOldPermissions(oldRoleData.getOldPermissions());
                        }

                        if (oldRoleData != null && currentRoleData != null && currentRoleData.getNewPermissions() == null && oldRoleData.getNewPermissions() != null) {
                            currentRoleData.setNewPermissions(oldRoleData.getNewPermissions());
                        }

                        if (oldRoleData != null && currentRoleData != null && oldRoleData.getOldColor() != null) {
                            currentRoleData.setOldColor(oldRoleData.getOldColor());
                        }

                        if (oldRoleData != null && currentRoleData != null && currentRoleData.getNewColor() == null && oldRoleData.getNewColor() != null) {
                            currentRoleData.setNewColor(oldRoleData.getNewColor());
                        }

                        if (currentRoleData != null && oldRoleData != null && !currentRoleData.isHoistedChanged() && oldRoleData.isHoistedChanged()) {
                            currentRoleData.setHoisted(oldRoleData.isHoisted());
                        }
                        if (currentRoleData != null && oldRoleData != null && !currentRoleData.isMentionChanged() && oldRoleData.isMentionChanged()) {
                            currentRoleData.setMention(oldRoleData.isMention());
                        }

                        //TODO rework this upper part too.

                        lm.setRoleData(currentRoleData);

                        if (currentRoleData != null && !currentRoleData.isCreated() && !currentRoleData.isDelete()) {
                            temp = true;
                            we.setDescription(":family_mmb: ``" + currentRoleData.getOldName() + "`` **has been updated.**");

                            if (currentRoleData.getOldName() != null && currentRoleData.getNewName() != null) {
                                we.addField(new WebhookEmbed.EmbedField(true, "**Old name**", currentRoleData.getOldName()));
                                we.addField(new WebhookEmbed.EmbedField(true, "**New name**", currentRoleData.getNewName()));
                                we.addField(new WebhookEmbed.EmbedField(true, "**", "**"));
                            }

                            if (currentRoleData.isMentionChanged()) {
                                we.addField(new WebhookEmbed.EmbedField(true, "**Old mentionable**", !currentRoleData.isMention() + ""));
                                we.addField(new WebhookEmbed.EmbedField(true, "**New mentionable**", currentRoleData.isMention() + ""));
                                we.addField(new WebhookEmbed.EmbedField(true, "**", "**"));
                            }

                            if (currentRoleData.isHoistedChanged()) {
                                we.addField(new WebhookEmbed.EmbedField(true, "**Old hoist**", !currentRoleData.isHoisted() + ""));
                                we.addField(new WebhookEmbed.EmbedField(true, "**New hoist**", currentRoleData.isHoisted() + ""));
                                we.addField(new WebhookEmbed.EmbedField(true, "**", "**"));
                            }

                            if (currentRoleData.getOldColor() != null) {
                                we.addField(new WebhookEmbed.EmbedField(true, "**Old color**", (currentRoleData.getOldColor() != null ? currentRoleData.getOldColor() : Color.gray).getRGB() + ""));
                                we.addField(new WebhookEmbed.EmbedField(true, "**New color**", (currentRoleData.getNewColor() != null ? currentRoleData.getNewColor() : Color.gray).getRGB() + ""));
                                we.addField(new WebhookEmbed.EmbedField(true, "**", "**"));
                            }

                            StringBuilder finalString = new StringBuilder();

                            boolean b = false;

                            if (currentRoleData.getNewPermissions() != null) {
                                for (Permission r : currentRoleData.getNewPermissions()) {
                                    if (!currentRoleData.getOldPermissions().contains(r)) {
                                        if (b) {
                                            finalString.append("\n:white_check_mark: ").append(r.getName());
                                        } else {
                                            finalString.append(":white_check_mark: ").append(r.getName());
                                            b = true;
                                        }
                                    }
                                }
                            }

                            if (currentRoleData.getOldPermissions() != null) {
                                for (Permission r : currentRoleData.getOldPermissions()) {
                                    if (!currentRoleData.getNewPermissions().contains(r)) {
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
                                temp = true;
                                we.addField(new WebhookEmbed.EmbedField(true, "**New permissions**", finalString.toString()));
                            }
                        } else {
                            temp = true;
                            if (currentRoleData != null && currentRoleData.isCreated()) {
                                we.setDescription(":family_mmb: ``" + currentRoleData.getOldName() + "`` **has been created.**");
                            } else if (currentRoleData != null){
                                we.setDescription(":family_mmb: ``" + currentRoleData.getOldName() + "`` **has been deleted.**");
                            }
                        }
                    } else {
                        temp = true;
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

            if (lm.getType() != LoggerMessage.LogTyp.ELSE) lm.setWem(wm.build());

            if (!temp && lm.getType() != LoggerMessage.LogTyp.ELSE) {
                Logger.log("LoggerQueue", "Failed to log LogTyp: " + lm.getType().name());
            }

            new Thread(() ->{
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ignored) {}

                if(!lm.isCancel()) {
                    Webhook.sendWebhook(lm.getWem(), lm.getId(), lm.getAuthCode());
                }

                logs.remove(lm);

            }).start();

        }
    }

    public ArrayList<LoggerMessage> getLogsByGuild(Guild g) {
        ArrayList<LoggerMessage> sheesh = new ArrayList<>();

        for(LoggerMessage lm : logs) {
            if(lm.getGuild() == g) {
                sheesh.add(lm);
            }
        }

        return sheesh;
    }
}

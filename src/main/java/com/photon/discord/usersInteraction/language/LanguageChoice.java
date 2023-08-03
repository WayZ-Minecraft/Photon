package com.photon.discord.usersInteraction.language;

import com.photon.discord.Roles;
import com.photon.discord.usersInteraction.data.UsersInfo;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;

public class LanguageChoice {

    /**
     * When a user get a role, add the language to his profile
     * @param event The event of a user getting a role
     * @author Mini
     */
    public static void onMemberRoleAdd(GuildMemberRoleAddEvent event) {
        for (Role role : event.getRoles()){
            // Note : switch case doesn't work with long
            if (role.getIdLong() == Roles.FR.id) UsersInfo.addLanguages(event.getUser().getId(), Languages.FRENCH);
            else if (role.getIdLong() == Roles.EN.id) UsersInfo.addLanguages(event.getUser().getId(), Languages.ENGLISH);
        }
    }

    /**
     * when a user lose a role, remove the language from his profile
     * @param event The event of a user losing a role
     */
    public static void onMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        for (Role role : event.getRoles()){
            // Note : switch case doesn't work with long
            if (role.getIdLong() == Roles.FR.id) UsersInfo.removeLanguages(event.getUser().getId(), Languages.FRENCH);
            else if (role.getIdLong() == Roles.EN.id) UsersInfo.removeLanguages(event.getUser().getId(), Languages.ENGLISH);
        }
    }
}

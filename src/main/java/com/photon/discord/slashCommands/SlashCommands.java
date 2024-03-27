package com.photon.discord.slashCommands;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;

import com.photon.discord.slashCommands.advancedCommands.CustomMute;
import com.photon.discord.slashCommands.advancedCommands.KineticCommands;
import com.photon.discord.slashCommands.advancedCommands.NewsManager;
import com.photon.discord.usersInteraction.xpManager;
import com.photon.informations.PhotonUpdaterManager.UpdateChannel;
import com.photon.informations.PhotonUpdaterManager.UpdateFileType;
import com.photon.network.NetworkDirectories;
import com.photon.network.NetworkEngine;
import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.network.objects.ProfileManager;
import com.photon.network.sql.SqlInteract;
import com.photon.util.ConsoleManager;
import com.photon.util.os.ApplicationUtils;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.FileUpload;

public class SlashCommands {
    public static List<CommandData> commands = new ArrayList<>();
    public static List<CommandData> globalCommand = new ArrayList<>();

    /**
     * Load slash commands, to be registered in discord slash commands interface
     * @author Mini
     */
    public static void load() {

        /*
         * Utility Commands for discord server
         */

        // Add commands here
        globalCommand.add(Commands.slash("hello", "say hello to the bot"));

        // Add commands clear
        commands.add(Commands.slash("clear", "clear a number of message")
            .addOption(OptionType.INTEGER, "number", "number of message to delete", false, false)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE)));

        // Kinetic commands
        {
            commands.add(Commands.slash("convert", "Convert a nebulae files or OBJ, images, ANIM files")
                .addOption(OptionType.ATTACHMENT, "file", "The file to convert", true, false)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));
    
            commands.add(Commands.slash("opacity-map", "Combine a texture with opacity map to get the transparent texture")
                .addOption(OptionType.ATTACHMENT, "texture", "The non-transparent texture", true, false)
                .addOption(OptionType.ATTACHMENT, "opacity_map", "The opacity map", true, false)
                .addOption(OptionType.BOOLEAN, "reversed_colors", "True if you want to use resversed colors (Black instead of white)", false, false)
                .addOption(OptionType.BOOLEAN, "custom_format", "True if you want to get a nebulae-image file format", false, false)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));

            commands.add(Commands.slash("image-quality", "Change the quality of an image to a specific percentage (0-100)")
                .addOption(OptionType.ATTACHMENT, "image", "The image to update", true, false)
                .addOption(OptionType.INTEGER, "percentage", "Percentage of quality (0 - 100) where 0 is more compressed but less 'beautiful'", true, false)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));
        }

        // Add commands time mute
        commands.add(Commands.slash("tempmute", "use to mute a temporaly a specifique player")
            .addOption(OptionType.USER , "user", "the user", true, false)
            .addOption(OptionType.STRING, "duration", "time of mute", true, true)
            .addOption(OptionType.STRING, "reason", "the reason of the mute", true, false)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VOICE_MUTE_OTHERS)));

        // Add commands silence
        commands.add(Commands.slash("silence", "mute all the users in the voice channel")
            .addOption(OptionType.INTEGER, "duration", "time of mute in seconde", false, false)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VOICE_MUTE_OTHERS)));

        /*
         * Network Commands
         */

        // Add commands restart network
        commands.add(Commands.slash("restart-network", "restart the network")
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));

        // Add commands link-account
        commands.add(Commands.slash("link-account", "link your minecraft account to your discord account")
            .addOption(OptionType.STRING, "uuid", "your WayZ unique user identity (in game)", true, false)
            .addOption(OptionType.STRING, "authkey", "your authentication Key", true, false));

        // Add commands to update the network
        commands.add(Commands.slash("post-update", "post an update on the network")
            .addOption(OptionType.ATTACHMENT, "file", "the build file", true, false)
            .addOption(OptionType.STRING, "channel", "the channel (e.g: stable, dev)", false, true)
            .addOption(OptionType.STRING, "filetype", "the file to update (e.g: mod, launcher)", false, true)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));

        /* 
        * Xp Commands
        */

        //add commands Level
        commands.add(Commands.slash("level", "show your level")
            .addOption(OptionType.USER, "user", "the user", false, false));

        //give xp command
        commands.add(Commands.slash("give-xp", "give xp to a player")
            .addOption(OptionType.USER, "user", "the user", true, false)
            .addOption(OptionType.INTEGER, "xp", "the amount of xp", true, false)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));

        //remove xp command
        commands.add(Commands.slash("remove-xp", "remove xp to a player")
            .addOption(OptionType.USER, "user", "the user", true, false)
            .addOption(OptionType.INTEGER, "xp", "the amount of xp", true, false)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));

        /*
         * News
         */

        // Add commands news
        commands.add(Commands.slash("create-news", "create a news")
            .addOption(OptionType.STRING, "title", "the title of the news", true, false)
            .addOption(OptionType.STRING, "content-en", "the content of the news", true, false)
            .addOption(OptionType.STRING, "content-fr", "the content of the news in french", true, false)
            .addOption(OptionType.ATTACHMENT, "image", "the image of the news", true, false)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));
        
        /**
         * Accounts
         */
        commands.add(Commands.slash("account", "Manager accounts")
            .addOption(OptionType.STRING, "type", "can be : uuid, discordid, email or username", true, false)
            .addOption(OptionType.STRING, "getter", "The email/username/... according to the type", true, false)
            .addOption(OptionType.STRING, "action", "Can be get, edit or delete", true, false)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));

        commands.add(Commands.slash("list-accounts", "List all existing accounts")
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));
        
        commands.add(Commands.slash("delete-accounts", "Delete all existing accounts")
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));

        /*
        * Sql commande
        */
        commands.add(Commands.slash("execute-sql", "execute a sql command")
            .addOption(OptionType.STRING, "command", "sql command", true, false)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));
            
    }

    /**
     * Handle slash commands
     * @param event The event that triggered this command
     * @author Mini
     */
    public static void onSlashCommand(SlashCommandInteractionEvent event){
        final String name = event.getName();

        switch (name) {
            case "hello":
                sayHello(event);
                break;
            case "clear":
                clearMessages(event);
                break;
            case "tempmute":
                CustomMute.tempmute(event);
                break;
            case "restart-network":
                restartNetwork(event);
                break;
            case "link-account":
                linkAccount(event);
                break;
            case "level":
                xpManager.levelEmbed(event);
                break;
            case "give-xp":
                xpManager.giveXp(event);
                break;
            case "remove-xp":
                xpManager.removeXp(event);
                break;
            case "create-news":
                NewsManager.createNews(event);
                break;
            case "execute-sql":
                executeSql(event);
                break;
            case "post-update":
                postUpdate(event);
                break;
            case "silence":
                CustomMute.silence(event);
                break;
            case "convert":
                KineticCommands.convertFile(event);
                break;
            case "opacity-map":
                try { KineticCommands.opacityImage(event); } catch (IOException e) { e.printStackTrace(); }
                break;
            case "image-quality":
                try { KineticCommands.imageQuality(event); } catch (IOException e) { e.printStackTrace(); }
                break;
            case "account":
                String type = event.getOption("type").getAsString();
                String getter = event.getOption("getter").getAsString();
                ObjectPlayerAccount profile = null;
                switch (type.toLowerCase()) {
                    case "discordid":
                        profile = ProfileManager.getProfileFromDiscordID(getter);
                        break;
                    case "uuid":
                        profile = ProfileManager.getProfileFromUUID(getter);
                        break;
                    case "email":
                        profile = ProfileManager.getProfileFromEMail(getter);
                        ConsoleManager.debug(getter);
                        break;
                    case "username":
                        profile = ProfileManager.getProfileFromUsername(getter);
                        break;
                }
                managerAccount(event, profile, event.getOption("action").getAsString());
                break;
            case "list-accounts":
                try {
                    List<ObjectPlayerAccount> list = ProfileManager.getAllPorifles();
                    if(list.isEmpty()) event.reply("No accounts found").queue();

                    StringJoiner joiner = new StringJoiner(System.lineSeparator(), "", System.lineSeparator());
                    for(ObjectPlayerAccount account : list) {
                        if(account == null) continue;
                        joiner.add("{").add(account.email).add(account.username).add(account.uuid).add("}");
                    }
                    
                    FileUpload upload = FileUpload.fromData(joiner.toString().getBytes(), "account.txt");
                    event.reply("List of all accounts").addFiles(upload).queue();
                }
                catch(Exception e) {
                    event.reply("Error with the list of accounts").queue();
                }
                break;
            case "delete-accounts":
                for(ObjectPlayerAccount account : ProfileManager.getAllPorifles())
                    ProfileManager.deleteProfile(account);
                
                event.reply("All accounts have been deleted !").queue();
                break;
            default:
                break;
        }
    }

    private static void managerAccount(SlashCommandInteractionEvent event, ObjectPlayerAccount profile, String action) {
        if(profile == null) {
            event.reply("Error, the profile doesn't exist").queue();
            return;
        }
        switch (action.toLowerCase()) {
            case "get":
                FileUpload upload = FileUpload.fromData(ProfileManager.getGson().toJson(profile).getBytes(), profile.username+".json");
                event.reply("The info for the user : " + profile.username+"("+profile.email+")").addFiles(upload).queue();
                break;
            case "edit":
                break;
            case "delete":
                ProfileManager.deleteProfile(profile);
                event.reply("The profile has been deleted !").queue();
                break;
        }
    }

    /**
     * Say hello to the user in the channel (/hello)
     * @param event The event that triggered this command
     * @author Mini
     */
    protected static void sayHello(SlashCommandInteractionEvent event) {
        event.reply("Hello, " + event.getUser().getAsMention() + "!").queue();
    }


    /**
     * Clear a number of messages in the channel (/clear number)
     * @param event The event that triggered this command
     * @author Mini
     */
    protected static void clearMessages(SlashCommandInteractionEvent event) {
        final int number = event.getOption("number").getAsInt();
        event.getChannel().purgeMessages(event.getChannel().getHistory().retrievePast(number).complete());
        event.reply(String.format("Clearing %s messages...", number)).queue();
    }


    /**
     * Restart the network (/restart-network)
     * @param event The event that triggered this command
     * @author Mini
     */
    protected static void restartNetwork(SlashCommandInteractionEvent event) {
        event.reply("Restarting network...").queue();
        ApplicationUtils.restart(NetworkEngine.class, "--restart");
    }


    /**
     * Link a discord account to a minecraft account (/link-account uuid authkey)
     * @param event The event that triggered this command
     * @author Mini
     */
    protected static void linkAccount(SlashCommandInteractionEvent event) {
        final String ingameUUID = event.getOption("uuid").getAsString();
        final String AUTHCODE = String.valueOf(event.getOption("authkey").getAsInt());
        if(!ProfileManager.isAuthCodeValid(ingameUUID, AUTHCODE)) {
            if(!ProfileManager.doesProfileExistByUUID(ingameUUID)) event.reply("Error your User Id is wrong or doesn't exist").queue();
            else event.reply("Error your authentication Key is wrong").queue();
            return;
        }

        ObjectPlayerAccount profile = ProfileManager.getProfileFromUUID(ingameUUID);
        profile.discordID = event.getMember().getId();
        profile.discordAuthCode = AUTHCODE;

        event.reply("Your account has been linked").queue();
    }

    /**
     * To execute a sql command (e.g. /execute-sql "SELECT * FROM users")
     * @param event The event that triggered a SlashCommandInteractionEvent
     */
    protected static void executeSql(SlashCommandInteractionEvent event){
        String command = event.getOption("command").getAsString();
        try {
            String result = SqlInteract.commandSql(command);
            event.reply(result).queue();
        } catch (SQLException e) {
            event.reply("Error with Sql commande :" + e).queue();
        } catch (Exception e) {
            event.reply(e.toString()).queue();
        }

    }


    /**
     * Post an update to the network
     * @param event The event that triggered a SlashCommandInteractionEvent
     */
    protected static void postUpdate(SlashCommandInteractionEvent event){
        Attachment file = event.getOption("file").getAsAttachment();
        
        String fileType;
        if (event.getOption("type") == null) fileType = file.getFileName().split("\\.")[0];
        else fileType = event.getOption("type").getAsString();
        
        String channelType = "stable";
        if (event.getOption("channel") != null) channelType = event.getOption("channel").getAsString();


        HashMap<String, UpdateFileType> fileTypeKeys = new HashMap<>(){{
            put("mod", UpdateFileType.MOD);
            put("launcher", UpdateFileType.LAUNCHER);
            put("api", UpdateFileType.API);
            put("network", UpdateFileType.NETWORK);
        }};

        HashMap<String, UpdateChannel> channelTypeKeys = new HashMap<>(){{
            put("stable", UpdateChannel.STABLE);
            put("dev", UpdateChannel.DEV);
        }};

        HashMap<UpdateChannel, String> channelsPaths = NetworkDirectories.config.filePaths.get(fileTypeKeys.get(fileType));
        Path outputPath = Path.of(channelsPaths.get(channelTypeKeys.get(channelType)));
        
        InputStream inputStream;
        try {
            inputStream = new URL(file.getUrl()).openStream();
            try {
                Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
                event.reply("File updated").queue();
            } catch (NoSuchFileException e) {
                File fileOutput = new File(outputPath.toString()).getParentFile();
                fileOutput.mkdirs();
                Files.copy(inputStream, outputPath);
            } finally { inputStream.close();}
        } catch (Exception e) {
            event.reply("Error with file :" + e).queue();
        }
    }
}

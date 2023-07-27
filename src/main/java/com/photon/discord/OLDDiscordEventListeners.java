package com.photon.discord;

import java.util.List;

import com.photon.informations.PhotonInfosManager;
import com.photon.network.NetworkConnectionServer;
import com.photon.network.NetworkDirectories;
import com.photon.network.objects.ObjectPlayerAccount;
import com.photon.network.objects.ProfileManager;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class OLDDiscordEventListeners extends ListenerAdapter
{
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		final String name = event.getName();
		final String subName = event.getSubcommandName();
		switch(name) {
		case "link-account":
			final String UUID = event.getOption("uuid").getAsString();
			final String AUTHCODE = String.valueOf(event.getOption("authcode").getAsInt());
			if(ProfileManager.isAuthCodeValid(UUID, AUTHCODE)) {
				ObjectPlayerAccount profile = ProfileManager.getProfileFromUUID(UUID);
				profile.discordID = event.getMember().getId();
				profile.discordAuthCode = AUTHCODE;
				final EmbedBuilder embed = new EmbedBuilder();
//				embed.setColor(DiscordEngine.EMBED_THEME_COLOR);
//				embed.setTitle("__Account Linker__");
//				embed.addField("User:", UUID, false);
				event.replyEmbeds(embed.build()).queue();
			}
			break;
		case "clear":
			final MessageHistory history = new MessageHistory(event.getMessageChannel());
			final List<Message> msgs = history.retrievePast(event.getOption("time").getAsInt()).complete();
	        for(Message msg : msgs) event.getMessageChannel().deleteMessageById(msg.getId());
	        event.reply("Cleared "+msgs.size()+" messages !").queue();
			break;
		case "publish":
//	        else if (object instanceof ClientRequestCreateNews) {
//	            final ClientRequestCreateNews request = (ClientRequestCreateNews)object;
//	            final File newsFile = new File(NetworkDirectories.newsDirectory, "news-" + request.objNews.date + ".json");
//	            if(PhotonEngine.networkNewsList.contains(request.objNews)) { return; }
//	            if (!newsFile.exists()) { newsFile.createNewFile(); }
//	            if (newsFile.exists()) {
//	                	BufferedWriter writer = new BufferedWriter(new FileWriter(newsFile));
//	                    writer.write(new Gson().toJson(request.objNews));
//	                    writer.close();
//	            }
//	            PhotonEngine.networkNewsList.add(request.objNews);
//	            ConsoleManager.print(EnumLogType.NETWORK, "A client request news adding: "+request.objNews.authorUUID+"\nDate: "+request.objNews.date + "\nTitle: " + request.objNews.title);
//	        }
			final EmbedBuilder updateImgEmbed = new EmbedBuilder();
			updateImgEmbed.setColor(OLDDiscordEngine.EMBED_THEME_COLOR);
			updateImgEmbed.setImage(NetworkDirectories.config.webUrl+"/project-logo.png");
			final EmbedBuilder updateContentEmbed = new EmbedBuilder();
			updateContentEmbed.setColor(OLDDiscordEngine.EMBED_THEME_COLOR);
			switch(subName) {
			case "news":
				break;
			case "changelog":
				updateContentEmbed.setTitle("**Changelog "+event.getOption("version").getAsString()+" :**");
				updateContentEmbed.addField("", "", false);
				break;
			}
			event.replyEmbeds(updateImgEmbed.build(), updateContentEmbed.build()).setActionRow(Button.link(PhotonInfosManager.getInfos().website_url, "Website"), Button.link(PhotonInfosManager.getInfos().youtube_url, "Youtube")).queue();
			break;
		}
	}
	
	@Override
    public void onGuildReady(GuildReadyEvent event) {
		final CommandData dataClear = Commands.slash("clear", "Clear current text channel").setGuildOnly(true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE)).addOption(OptionType.INTEGER, "time", "Choose time for clearing");
		final CommandData dataLinkaccount = Commands.slash("link-account", "Link your ingame account with your discord account for more features").setGuildOnly(true).addOption(OptionType.STRING, "uuid", "Your ingame uuid").addOption(OptionType.INTEGER, "authcode", "Your discord auth code");
		
		final SubcommandData dataPublishChangelog = new SubcommandData("changelog", "Publish changelog").addOption(OptionType.STRING, "version", "The new version");
		final SubcommandData dataPublishNews = new SubcommandData("news", "Publish news").addOption(OptionType.STRING, "title", "The title").addOption(OptionType.STRING, "content", "Content");
		final CommandData dataPublish = Commands.slash("publish", "Publish a news/changelog").setGuildOnly(true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)).addSubcommands(dataPublishChangelog, dataPublishNews);
		
		event.getGuild().updateCommands().addCommands(dataLinkaccount, dataClear, dataPublish).queue();
    }
	
	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		switch(event.getComponentId()) {
		case "network_restart":
			final EmbedBuilder embed = OLDDiscordEngine.getNetworkPanel();
			embed.setDescription("Restarted");
			event.editMessageEmbeds(embed.build()).queue();
			NetworkConnectionServer.restart();
			event.deferEdit();
			break;
		}
	}
}
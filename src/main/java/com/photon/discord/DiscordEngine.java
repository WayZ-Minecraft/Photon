package com.photon.discord;

import java.awt.Color;
import java.io.InputStream;

import com.photon.informations.PhotonInfosManager;
import com.photon.network.NetworkDirectories;
import com.photon.util.ConsoleManager.EnumLogType;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class DiscordEngine {

	public static JDA jda;

	public static String ID_RANK_FOUNDER = "530338115110567937";
	public static String ID_RANK_MODERATOR = "565218823905476615";
	
	public static String ID_RANK_HIGHT_STAFF = "792033685074608139";
	public static String ID_RANK_STAFF = "609526765324468234";
	public static String ID_RANK_STAFFTEST = "699674919428161597";
	public static String ID_RANK_MUTED = "762235368988213271";
	
	public static Color EMBED_THEME_COLOR = new Color(255, 255, 255);
//	public static Color EMBED_THEME_COLOR = new Color(48, 49, 54);
	
	public static void load() {
        try {
        	final JDABuilder builder = JDABuilder.createDefault(PhotonInfosManager.getInfos().discord_bot_token);
        	builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
        	builder.setActivity(Activity.playing("/"));
        	builder.addEventListeners(new DiscordEventListeners());
			jda = builder.build();
			final InputStream stream = PhotonInfosManager.getGameLogoInputStream();
			if(stream !=null) jda.getSelfUser().getManager().setAvatar(Icon.from(stream)).complete();
			if(PhotonInfosManager.getInfos() !=null) jda.getSelfUser().getManager().setName(PhotonInfosManager.getInfos().project_name).complete();
		} catch (Exception e) {}
	}
	
	public static Role getRole(Member m, String id) { return jda.getRoleById(id); }
	
	public static boolean isMuted(Member m) {
		return hasRole(m, ID_RANK_MUTED);
	}
	
	public static boolean hasRole(Member m, String id) {
		Role role = jda.getRoleById(id);
		return role == null ? false : m.getRoles().contains(role);
	}
	
	public static TextChannel getTextChannelById(String id) { return jda.getTextChannelById(id == null ? "" : id);  }
	
	public static void log(Color color, String title, Object o) {
		final EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(color);
		embed.setTitle(title);
		embed.setDescription(o.toString());
		final TextChannel logChannel = getTextChannelById(NetworkDirectories.config.channelID_LOG);
		if(logChannel !=null) logChannel.sendMessageEmbeds(embed.build()).queue();
	}
	
	public static EmbedBuilder getNetworkPanel() {
		final EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(EnumLogType.NETWORK.color);
		embed.setTitle("Network Panel");
		return embed;
	}
	
	public static void showNetworkPanel() {
		if(jda !=null) {
			final TextChannel panelChannel = getTextChannelById(NetworkDirectories.config.channelID_LOG);
			if(panelChannel !=null) panelChannel.sendMessageEmbeds(getNetworkPanel().build()).setActionRow(Button.primary("network_restart", "Restart")).queue();
		}
	}
	
	public static void sendPermsError(Member m, TextChannel c) {
		if(jda !=null) c.sendMessage("Invalid permissions, " + m.getEffectiveName() + "!").queue();
	}
	
	public static boolean isStaff(Member m) { return canInteract(m, ID_RANK_STAFF) || canInteract(m, ID_RANK_STAFFTEST); }
	
	public static boolean isHightStaff(Member m) { return canInteract(m, ID_RANK_HIGHT_STAFF); }
		
	public static boolean canInteract(Member m, String id) { 
		return m.canInteract(m.getGuild().getRoleById(id)); 
	}
}
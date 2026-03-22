package com.photon.discord.commands;

import com.photon.sql.DiscordProfileTable;
import com.photon.util.NetworkOnly;
import com.photon.util.TranslationManager;
import com.photon.util.TranslationManager.Language;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import niwer.queryon.QueryonException;

/**
 * Command to change the language of the bot responses for a user.
 * Avaliable globally.
 * 
 * @author Niwer
 */
@NetworkOnly
@SuppressWarnings("null") // The compiler in Photon is not good at handling JDA's @Nonnull annotations, so we suppress null warnings in this class
public class LanguageCommand extends AbstractSlashCommand {

    public LanguageCommand() {
        super("lang", "Change the language of the bot responses.");
        this.addOption(OptionType.STRING, "language", "Language to set", true, Language.class);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        final OptionMapping LANGUAGE_ARG = event.getOption("language");
        final var LANGUAGE = Language.fromNameString(LANGUAGE_ARG.getAsString());

        /* Set the new user language */
        try {
            DiscordProfileTable.setLanguage(event.getUser().getId(), LANGUAGE);
            event.reply(TranslationManager.format(event.getUser().getId(), "command.reply.language.success")).queue();
        } catch (QueryonException e) {
            event.reply(TranslationManager.format(event.getUser().getId(), "command.reply.language.failure")).queue();
        }
    }
}
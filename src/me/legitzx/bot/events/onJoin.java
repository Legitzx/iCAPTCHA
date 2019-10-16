package me.legitzx.bot.events;

import me.legitzx.bot.cgen.Captcha;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;
import java.util.HashMap;

public class onJoin extends ListenerAdapter {
    private HashMap<String, String> userCaptcha = new HashMap<>(); // Stores { DiscordID, Captcha Answer }

    /**
     * When a user joins the Guild, the bot will send a CAPTCHA and wait for a valid response.
     * @param event     Default event
     */
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Captcha captcha = new Captcha();

        if(userCaptcha.containsKey(event.getUser().getId())) { // Checking to see if user is already in the database
            userCaptcha.remove(event.getUser().getId());
        }

        event.getUser().openPrivateChannel().queue((channel) -> {
            File captchaFile = captcha.generateCaptcha();

            channel.sendFile(captchaFile).queue();
            channel.sendMessage("**Answer the CAPTCHA to gain permissions**").queue();

            userCaptcha.put(event.getUser().getId(), captchaFile.getName().substring(0, 5));
        });
    }

    /**
     * Listens for correct answers -> if correct, the bot will grant the user something... (Most likely a role)
     * @param event Default event
     */
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        if(userCaptcha.containsKey(event.getAuthor().getId())) { // Checking to see if the user has received a captcha
            if(event.getMessage().getContentRaw().equals(userCaptcha.get(event.getAuthor().getId()))) { // The user answered the captcha correctly
                event.getAuthor().openPrivateChannel().queue((channel -> {
                    channel.sendMessage("Answered Correctly.").queue();
                    userCaptcha.remove(event.getAuthor().getId());
                }));
            }
        }
    }
}

package au.com.addstar.skyblock.misc;

import java.util.WeakHashMap;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ConfirmationPrompt
{
	private static WeakHashMap<CommandSender, ConfirmationPrompt> mActivePrompts = new WeakHashMap<CommandSender, ConfirmationPrompt>();
	
	private CommandSender mPlayer;
	private Callback mCallback;
	private String mPrompt;
	
	public ConfirmationPrompt()
	{
		
	}
	
	public ConfirmationPrompt setPlayer(CommandSender player)
	{
		mPlayer = player;
		return this;
	}
	
	public ConfirmationPrompt setCallback(Callback callback)
	{
		mCallback = callback;
		return this;
	}
	
	public ConfirmationPrompt setText(String promptText)
	{
		mPrompt = promptText;
		return this;
	}
	
	public void launch()
	{
		Validate.notNull(mPrompt);
		Validate.notNull(mPlayer);
		Validate.notNull(mCallback);
		
		mPlayer.sendMessage(mPrompt);
		mPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e/is confirm &fto accept or \n&c/is cancel &fto reject."));
		mActivePrompts.put(mPlayer, this);
	}
	
	public static boolean accept(CommandSender player)
	{
		ConfirmationPrompt prompt = mActivePrompts.remove(player);
		if(prompt == null)
			return false;
		
		prompt.mCallback.onComplete(true, null);
		return true;
	}
	
	public static boolean reject(CommandSender player)
	{
		ConfirmationPrompt prompt = mActivePrompts.remove(player);
		if(prompt == null)
			return false;
		
		prompt.mCallback.onComplete(false, null);
		return true;
	}
}

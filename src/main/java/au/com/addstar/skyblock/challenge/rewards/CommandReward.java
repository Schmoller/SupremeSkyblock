package au.com.addstar.skyblock.challenge.rewards;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandReward extends Reward
{
	private String mName;
	private String mCommand;
	
	public CommandReward(String command, String name)
	{
		mCommand = command;
		mName = name;
	}
	
	@Override
	public void apply( Player player )
	{
		CommandSender sender;
		boolean op = false;
		
		String command;
		switch(mCommand.charAt(0))
		{
		case '~':
			sender = Bukkit.getConsoleSender();
			command = mCommand.substring(1);
			break;
		case '*':
			op = true;
			sender = player;
			command = mCommand.substring(1);
			break;
		default:
			sender = player;
			command = mCommand;
			break;
		}
		
		command = command
			.replace("{player}", player.getName())
			.replace("{uuid}", player.getUniqueId().toString())
			.replace("{disp}", player.getDisplayName());
		
		boolean wasOp = sender.isOp();
		
		if (op)
			sender.setOp(true);
		
		Bukkit.dispatchCommand(sender, command);
		
		if (op)
			sender.setOp(wasOp);
	}

	@Override
	public String getName()
	{
		return mName;
	}

}

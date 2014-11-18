package au.com.addstar.skyblock.misc;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Furnace;
import org.bukkit.block.Jukebox;
import org.bukkit.block.NoteBlock;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;
import org.bukkit.util.BlockVector;

@SuppressWarnings( "deprecation" )
public class StoredBlock
{
	private MaterialData mType;
	
	private Map<String, Object> mExtra;
	
	private BlockVector mLocation;
	
	public StoredBlock()
	{
		mType = Material.AIR.getNewData((byte)0);
	}
	
	public StoredBlock(MaterialData data)
	{
		mType = data;
	}
	
	public StoredBlock(Material material)
	{
		mType = material.getNewData((byte)0);
	}
	
	public StoredBlock(BlockState state)
	{
		mType = state.getData();
		
		mExtra = new HashMap<String, Object>();

		if(state instanceof InventoryHolder)
		{
			ItemStack[] copy = ((InventoryHolder)state).getInventory().getContents().clone();
			for(int i = 0; i < copy.length; ++i)
			{
				if(copy[i] != null)
					copy[i] = copy[i].clone();
			}
			
			mExtra.put("inventory", copy);
		}
		
		if(state instanceof BrewingStand)
			mExtra.put("brewTime", ((BrewingStand) state).getBrewingTime());
		
		if(state instanceof CommandBlock)
		{
			mExtra.put("name", ((CommandBlock) state).getName());
			mExtra.put("cmd", ((CommandBlock) state).getCommand());
		}
		
		if(state instanceof CreatureSpawner)
		{
			mExtra.put("type", ((CreatureSpawner) state).getSpawnedType().name());
			mExtra.put("delay", ((CreatureSpawner) state).getDelay());
		}
		
		if(state instanceof Furnace)
		{
			mExtra.put("cook", ((Furnace) state).getCookTime());
			mExtra.put("burn", ((Furnace) state).getBurnTime());
		}
		
		if(state instanceof Jukebox)
		{
			if(((Jukebox) state).isPlaying())
				mExtra.put("jukebox", ((Jukebox) state).getPlaying().name());
		}
		
		if(state instanceof NoteBlock)
			mExtra.put("note", (short)((NoteBlock) state).getRawNote());
		
		if(state instanceof Sign)
		{
			mExtra.put("line1", ((Sign) state).getLine(0));
			mExtra.put("line2", ((Sign) state).getLine(1));
			mExtra.put("line3", ((Sign) state).getLine(2));
			mExtra.put("line4", ((Sign) state).getLine(3));
		}
		
		if(state instanceof Skull)
		{
			mExtra.put("rot", ((Skull) state).getRotation().name());
			if(((Skull) state).hasOwner())
				mExtra.put("owner", ((Skull) state).getOwner());
			mExtra.put("type", ((Skull) state).getSkullType().name());
		}
		
		if(mExtra.isEmpty())
			mExtra = null;
	}
	
	public void apply(Block block)
	{
		block.setType(mType.getItemType());
		block.setData(mType.getData(), false);
		
		applyExtra(block);
	}
	
	public void applyExtra(Block block)
	{
		BlockState state = block.getState();
		
		if(state instanceof InventoryHolder)
			((InventoryHolder) state).getInventory().setContents((ItemStack[])mExtra.get("inventory"));
		
		if(state instanceof BrewingStand)
			((BrewingStand) state).setBrewingTime((Integer)mExtra.get("brewTime"));
		
		if(state instanceof CommandBlock)
		{
			((CommandBlock) state).setName((String)mExtra.get("name"));
			((CommandBlock) state).setCommand((String)mExtra.get("cmd"));
		}
		
		if(state instanceof CreatureSpawner)
		{
			((CreatureSpawner) state).setSpawnedType(EntityType.valueOf((String)mExtra.get("type")));
			((CreatureSpawner) state).setDelay((Integer)mExtra.get("delay"));
		}
		
		if(state instanceof Furnace)
		{
			((Furnace) state).setCookTime((Short)mExtra.get("cook"));
			((Furnace) state).setBurnTime((Short)mExtra.get("burn"));
		}
		
		if(state instanceof Jukebox)
		{
			if(mExtra.containsKey("jukebox"))
				((Jukebox) state).setPlaying(Material.valueOf((String)mExtra.get("jukebox")));
		}
		
		if(state instanceof NoteBlock)
			((NoteBlock) state).setRawNote((byte)(short)(Short)mExtra.get("note"));
		
		if(state instanceof Sign)
		{
			((Sign) state).setLine(0, (String)mExtra.get("line1"));
			((Sign) state).setLine(1, (String)mExtra.get("line2"));
			((Sign) state).setLine(2, (String)mExtra.get("line3"));
			((Sign) state).setLine(3, (String)mExtra.get("line4"));
		}
		
		if(state instanceof Skull)
		{
			((Skull) state).setRotation(BlockFace.valueOf((String)mExtra.get("rot")));
			if(mExtra.containsKey("owner"))
				((Skull) state).setOwner((String)mExtra.get("owner"));

			((Skull) state).setSkullType(SkullType.valueOf((String)mExtra.get("type")));
		}
		
		state.update(true, false);
	}
	
	public void write(DataOutputStream output, int matId) throws IOException
	{
		output.writeShort(matId);
		
		if(mExtra != null)
		{
			output.writeByte(mExtra.size());
			for(Entry<String, Object> entry : mExtra.entrySet())
			{
				output.writeUTF(entry.getKey());
				if(entry.getValue() instanceof ItemStack[])
				{
					output.writeByte(0);
					ItemStack[] stacks = (ItemStack[])entry.getValue();
					
					output.writeByte(stacks.length);
					
					for(int i = 0; i < stacks.length; ++i)
						DataSerializer.write(output, stacks[i]);
				}
				else
				{
					output.writeByte(1);
					DataSerializer.write(output, entry.getValue());
				}
			}
		}
		else
			output.writeByte(0);
	}
	
	public void read(DataInput in, Map<Integer, MaterialData> materials) throws IOException
	{
		int typeId = in.readShort();
		mType = materials.get(typeId);
		
		int extraSize = in.readByte();
		if (extraSize > 0)
		{
			mExtra = new HashMap<String, Object>();
			for (int i = 0; i < extraSize; ++i)
			{
				String key = in.readUTF();
				Object value;
				switch(in.readByte())
				{
				case 0: // ItemStack[]
				{
					ItemStack[] stacks = new ItemStack[in.readByte()];
					for (int j = 0; j < stacks.length; ++j)
					{
						@SuppressWarnings( "unchecked" )
						Map<String, Object> map = (Map<String, Object>)DataSerializer.read(in);
						if (map != null)
							stacks[j] = ItemStack.deserialize(map);
					}
					value = stacks;
					break;
				}
				default: // Object
					value = DataSerializer.read(in);
					break;
				}
				
				mExtra.put(key, value);
			}
		}
	}
	
	public static BlockFace getDependantFace(MaterialData material)
	{
		if(material.getItemType().hasGravity())
			return BlockFace.DOWN;
		
		if(material instanceof Attachable)
			return ((Attachable)material).getAttachedFace();
		
		switch(material.getItemType())
		{
		case RAILS:
		case POWERED_RAIL:
		case DETECTOR_RAIL:
		case ACTIVATOR_RAIL:
		case SIGN_POST:
		case SAPLING:
		case LONG_GRASS:
		case DEAD_BUSH:
		case YELLOW_FLOWER:
		case RED_ROSE:
		case RED_MUSHROOM:
		case BROWN_MUSHROOM:
		case REDSTONE_WIRE:
		case CROPS:
		case WOODEN_DOOR:
		case STONE_PLATE:
		case WOOD_PLATE:
		case IRON_DOOR_BLOCK:
		case CACTUS:
		case SUGAR_CANE_BLOCK:
		case DIODE_BLOCK_OFF:
		case DIODE_BLOCK_ON:
		case PUMPKIN_STEM:
		case MELON_STEM:
		case WATER_LILY:
		case NETHER_WARTS:
		case FLOWER_POT:
		case CARROT:
		case POTATO:
		case REDSTONE_COMPARATOR_OFF:
		case REDSTONE_COMPARATOR_ON:
		case CARPET:
		case IRON_PLATE:
		case GOLD_PLATE:
		case DRAGON_EGG:
		case DOUBLE_PLANT:
			return BlockFace.DOWN;
			
		case VINE:
			return BlockFace.UP;
			
		default:
			return BlockFace.SELF;
		}
	}
	
	public BlockFace getDependantFace()
	{
		return getDependantFace(mType);
	}
	
	public BlockVector getLocation()
	{
		return mLocation;
	}
	
	public BlockVector getLocationRelative(BlockFace face)
	{
		return new BlockVector(mLocation.getBlockX() + face.getModX(), mLocation.getBlockY() + face.getModY(), mLocation.getBlockZ() + face.getModZ());
	}
	
	public void setLocation(BlockVector location)
	{
		mLocation = location;
	}

	public MaterialData getType()
	{
		return mType;
	}
	
	@Override
	public String toString()
	{
		return mType.toString();
	}
	
	public StoredBlock clone()
	{
		StoredBlock copy = new StoredBlock();
		copy.mType = mType;
		if(mExtra != null)
			copy.mExtra = new HashMap<String, Object>(mExtra);
		return copy;
	}
}

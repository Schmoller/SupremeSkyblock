package au.com.addstar.skyblock.misc;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class DataSerializer
{
	private static final int VAR_NULL = 0;
	private static final int VAR_BYTE = 1;
	private static final int VAR_SHORT = 2;
	private static final int VAR_INT = 3;
	private static final int VAR_LONG = 4;
	private static final int VAR_FLOAT = 5;
	private static final int VAR_DOUBLE = 6;
	private static final int VAR_CHAR = 7;
	private static final int VAR_STRING = 8;
	private static final int VAR_BOOLEAN = 9;
	private static final int VAR_UUID = 10;
	private static final int VAR_MAP = 11;
	private static final int VAR_LIST = 12;
	
	public static void write(DataOutput out, Object value) throws IllegalArgumentException, IOException
	{
		if (value == null)
			out.writeByte(VAR_NULL);
		else if(value instanceof Character)
		{
			out.writeByte(VAR_CHAR);
			out.writeChar((Character)value);
		}
		else if(value instanceof String)
		{
			out.writeByte(VAR_STRING);
			out.writeUTF((String)value);
		}
		else if(value instanceof Boolean)
		{
			out.writeByte(VAR_BOOLEAN);
			out.writeBoolean((Boolean)value);
		}
		else if(value instanceof Number)
		{
			if (value instanceof Double)
			{
				out.writeByte(VAR_DOUBLE);
				out.writeDouble((Double)value);
			}
			else if (value instanceof Float)
			{
				out.writeByte(VAR_FLOAT);
				out.writeFloat((Float)value);
			}
			else if (value instanceof Long)
			{
				out.writeByte(VAR_LONG);
				out.writeLong((Long)value);
			}
			else if (value instanceof Integer)
			{
				out.writeByte(VAR_INT);
				out.writeInt((Integer)value);
			}
			else if (value instanceof Short)
			{
				out.writeByte(VAR_SHORT);
				out.writeShort((Short)value);
			}
			else
			{
				out.writeByte(VAR_BYTE);
				out.writeByte((Byte)value);
			}
		}
		else if(value instanceof UUID)
		{
			out.writeByte(VAR_UUID);
			out.writeLong(((UUID)value).getMostSignificantBits());
			out.writeLong(((UUID)value).getLeastSignificantBits());
		}
		else if(value instanceof Map<?,?>)
		{
			out.writeByte(VAR_MAP);
			Map<?,?> map = (Map<?,?>)value;
			
			out.writeShort(map.size());
			for(Entry<?, ?> entry : map.entrySet())
			{
				write(out, entry.getKey());
				write(out, entry.getValue());
			}
		}
		else if(value instanceof List<?>)
		{
			out.writeByte(VAR_LIST);
			List<?> list = (List<?>)value;
			
			out.writeShort(list.size());
			Iterator<?> it = list.iterator();
			while(it.hasNext())
				write(out, it.next());
		}
		else if(value instanceof ConfigurationSerializable)
		{
			write(out, ((ConfigurationSerializable)value).serialize());
		}
		else
			throw new IllegalArgumentException("Unable to encode type " + value.getClass().getName());
	}
	
	public static void writeObject(final DataOutput out, Object object) throws IOException
	{
		ObjectOutputStream out2;
		boolean close = false;
		
		if (out instanceof DataOutputStream)
			out2 = new ObjectOutputStream((DataOutputStream)out);
		else
		{
			out2 = new ObjectOutputStream(new OutputStream()
			{
				@Override
				public void write( int b ) throws IOException
				{
					out.write(b);
				}
			});
			close = true;
		}
		
		out2.writeObject(object);
		
		if (close)
			out2.close();
	}
	
	public static byte[] toBytes(Object object) throws IllegalArgumentException
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(stream);
		
		try
		{
			write(out, object);
			return stream.toByteArray();
		}
		catch(IOException e)
		{
			return null;
		}
	}
	
	public static Object read(DataInput in) throws IOException
	{
		int type = in.readByte();
		switch(type)
		{
		case VAR_NULL:
			return null;
		case VAR_BYTE:
			return in.readByte();
		case VAR_SHORT:
			return in.readShort();
		case VAR_INT:
			return in.readInt();
		case VAR_LONG:
			return in.readLong();
		case VAR_FLOAT:
			return in.readFloat();
		case VAR_DOUBLE:
			return in.readDouble();
		case VAR_CHAR:
			return in.readChar();
		case VAR_STRING:
			return in.readUTF();
		case VAR_BOOLEAN: // Boolean
			return in.readBoolean();
		case VAR_UUID:
			return new UUID(in.readLong(), in.readLong());
		case VAR_MAP:
		{
			ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builder();
			int count = in.readShort();
			for(int i = 0; i < count; ++i)
				builder.put(read(in), read(in));
			
			return builder.build();
		}
		case VAR_LIST:
		{
			ImmutableList.Builder<Object> builder = ImmutableList.builder();
			int count = in.readShort();
			
			for(int i = 0; i < count; ++i)
				builder.add(read(in));
			
			return builder.build();
		}
		default:
			return null;
		}
	}
	
	public static Object readObject(final DataInput in) throws IOException, ClassNotFoundException
	{
		ObjectInputStream in2;
		boolean close = false;
		if (in instanceof DataInputStream)
			in2 = new ObjectInputStream((DataInputStream)in);
		else
		{
			in2 = new ObjectInputStream(new InputStream()
			{
				@Override
				public int read() throws IOException
				{
					return in.readByte();
				}
			});
			
			close = true;
		}
		
		Object object = in2.readObject();
		if (close)
			in2.close();
		
		return object;
	}
}

package com.example.damjan.programzanavodnjavanje.bluetooth;

import com.example.damjan.programzanavodnjavanje.data.CustomSerialization;
import com.example.damjan.programzanavodnjavanje.data.MyCrc32;

import org.json.JSONObject;

import java.io.InvalidObjectException;


public class Message implements CustomSerialization
{
	Type type;
	Action action;
	Info info;
	byte itemCount;//only set if we are sending something(valves, or time).

	public static final int NETWORK_SIZE = 4;

	public Message()
	{
		this(Type.NONE, Action.NONE, Info.NONE, (byte)0);
	}

	public Message(byte[] arduinoBytes, long crc32) throws InvalidObjectException
	{
		fromArduinoBytes(arduinoBytes, crc32);
	}

	Message(Type type, Action action, Info info, byte itemCount)
	{
		this.type 		= type;
		this.action 	= action;
		this.info 		= info;
		this.itemCount 	= itemCount;
	}

	public Message(Type type, Action action)
	{
		this(type, action, Info.NONE, (byte)0);
	}

	public Message(Type type, Action action, byte itemCount)
	{
		this(type, action, Info.NONE, itemCount);
	}

	public Message(Type type, Info info)
	{
		this(type, Action.NONE, info, (byte) 0);
	}

	@Override
	public JSONObject toJson()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void fromJSON(JSONObject jsonIn)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] toArduinoBytes()
	{
		byte[] bytes = new byte[NETWORK_SIZE];
		bytes[0] 	= type.toArduinoBytes()[0];
		bytes[1]	= action.toArduinoBytes()[0];
		bytes[2]	= info.toArduinoBytes()[0];
		bytes[3]	= itemCount;

		return MyCrc32.calculateCrcAndCombine(bytes);
	}

	@Override
	public void fromArduinoBytes(byte[] bytes, long crc32) throws InvalidObjectException
	{
			/*	TODO: this is dangerous! one could mistakenly say
				action = Action.getAction(bytes[0]) instead of bytes[1]
			*/
		type = Type.getType(bytes[0]);
		action = Action.getAction(bytes[1]);
		info = Info.getInfo(bytes[2]);
		itemCount = bytes[3];
	}

	public enum Type implements CustomSerialization
	{
		NONE	((byte)0x0),
		COMMAND	((byte)0x1),	//Send new valves or time to arduino
		REQUEST	((byte)0x2),	//Ask the arduino for some action
		INFO	((byte)0x3);

		final byte m_value;

		Type(byte b)
		{
			m_value = b;
		}

		public static Type getType(byte type) throws InvalidObjectException
		{
			if		(type == NONE.m_value)
			{
				return NONE;
			}else if(type == COMMAND.m_value)
			{
				return COMMAND;
			}else if(type == REQUEST.m_value)
			{
				return REQUEST;
			}else if(type == INFO.m_value)
			{
				return INFO;
			}else
			{
				throw new InvalidObjectException("Unknown Type value:"+type);
			}
		}

		@Override
		public JSONObject toJson()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void fromJSON(JSONObject jsonIn)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public byte[] toArduinoBytes()
		{
			return new byte[]{m_value};
		}

		@Override
		public void fromArduinoBytes(byte[] bytes, long crc32)
		{
			throw new UnsupportedOperationException();
		}

	}

	public enum Action implements CustomSerialization
	{
		NONE				((byte)0x0),
		VALVE				((byte)0x1),
		TIME				((byte)0x2),
		TEMPERATURE			((byte)0x3),
		TEMPERATURE_FLOAT	((byte)0x4),
		H_BRIDGE_PIN 		((byte)0x5);

		byte m_value;

		Action(byte b)
		{
			m_value = b;
		}

		public static Action getAction(byte action) throws InvalidObjectException
		{
			if		(action == NONE.m_value)
			{
				return NONE;

			}else if(action == VALVE.m_value)
			{
				return VALVE;

			}else if(action == TIME.m_value)
			{
				return TIME;

			}else if(action == TEMPERATURE.m_value)
			{
				return TEMPERATURE;

			}else if(action == TEMPERATURE_FLOAT.m_value)
			{
				return TEMPERATURE_FLOAT;

			}else if(action == H_BRIDGE_PIN.m_value)
			{
				return H_BRIDGE_PIN;

			}else
			{
				throw new InvalidObjectException("Unknown Action value:"+action);
			}
		}

		@Override
		public JSONObject toJson()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void fromJSON(JSONObject jsonIn)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public byte[] toArduinoBytes()
		{
			return new byte[]{m_value};
		}

		@Override
		public void fromArduinoBytes(byte[] type, long crc32)
		{
			throw new UnsupportedOperationException();
		}
	}

	public enum Info implements CustomSerialization
	{

		NONE				((byte)0x0),
		NOMINAL				((byte)0x1),
		ERROR				((byte)0x2),
		READY_TO_SEND		((byte)0x3),
		READY_TO_RECEIVE	((byte)0x4);

		final byte m_value;

		Info(byte b)
		{
			m_value = b;
		}

		public static Info getInfo(byte info) throws InvalidObjectException
		{
			if		(info == NONE.m_value)
			{
				return NONE;
			}else if(info == NOMINAL.m_value)
			{
				return NOMINAL;
			}else if(info == ERROR.m_value)
			{
				return ERROR;
			}else if(info == READY_TO_SEND.m_value)
			{
				return READY_TO_SEND;
			}else if(info == READY_TO_RECEIVE.m_value)
			{
				return READY_TO_RECEIVE;
			}else
			{
				throw new InvalidObjectException("Unknown Action value:"+info);
			}
		}

		@Override
		public JSONObject toJson()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void fromJSON(JSONObject jsonIn)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public byte[] toArduinoBytes()
		{
			return new byte[]{m_value};
		}

		@Override
		public void fromArduinoBytes(byte[] bytes, long crc32)
		{
			throw new UnsupportedOperationException();
		}
	}
}

package com.example.damjan.programzanavodnjavanje.Exceptions;

public class IncorrectBluetoothState extends Exception
{
	public IncorrectBluetoothState()
	{

	}

	public IncorrectBluetoothState(String message)
	{
		super(message);
	}

	public IncorrectBluetoothState(String message, Throwable cause)
	{
		super(message, cause);
	}
}

package com.example.damjan.programzanavodnjavanje.data;

import com.example.damjan.programzanavodnjavanje.data.bluetooth.Message;

public interface NetworkSerializable
{
	Message toMessage();
	void fromMessage(final Message message);
}

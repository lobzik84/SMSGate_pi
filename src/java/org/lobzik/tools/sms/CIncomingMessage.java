package org.lobzik.tools.sms;

import java.util.*;

/**
	This class represents an incoming SMS message, i.e. message read from the GSM device.

	@see	CMessage
	@see	COutgoingMessage
	@see	CService#readMessages(LinkedList, int)
*/
public class CIncomingMessage extends CMessage
{
	public static final int CLASS_ALL = 0;	
	public static final int CLASS_REC_UNREAD = 1;
	public static final int CLASS_REC_READ = 2;
	public static final int CLASS_STO_UNSENT = 3;
	public static final int CLASS_STO_SENT = 4;

	/**
		Default constructor of the class.

		@param	 date	the creation date of the message.
		@param	 originator	the originator's number.
		@param	 text	the actual text of the message.
		@param	 memIndex	the index of the memory location in the GSM device where this message is stored.

		<br><br>Notes:<br>
		<ul>
			<li>Phone numbers are represented in their international format (e.g. +306974... for Greece).</li>
		</ul>
	*/
	public CIncomingMessage(Date date, String originator, String text, int memIndex)
	{
		super(TYPE_INCOMING, date, originator, null, text, memIndex);
	}

	public CIncomingMessage(String pdu, int memIndex)
	{
		super(TYPE_INCOMING, null, null, null, null, memIndex);

		Date date;
		String originator;
		String str1;
		int index, i, j, k, protocol, addr, year, month, day, hour, min, sec;

		str1 = pdu.substring(0, 2);
		i = Integer.parseInt(str1, 16);
		index = (i + 1) * 2;
		index += 2;

		str1 = pdu.substring(index, index + 2);
		i = Integer.parseInt(str1, 16);
		j = index + 4;
		originator = "";
		for (k = 0; k < i; k += 2) originator = originator + pdu.charAt(j + k + 1) + pdu.charAt(j + k);
		originator = "+" + originator;
		if (originator.charAt(originator.length() - 1) == 'F') originator = originator.substring(0, originator.length() - 1);

		// Type of Address
		addr = Integer.parseInt(pdu.substring(j - 2, j), 16);
		if ( (addr & (1 << 6)) != 0 && (addr & (1 << 5)) == 0 && (addr & (1 << 4)) != 0)
		{
			//Alphanumeric, (coded according to GSM TS 03.38 7-bit default alphabet)
			str1 = pduToText(pdu.substring(j, j + k));
			originator = "";
			for (i = 0; i < str1.length(); i++)
			{
				if ( (int) str1.charAt(i) == 27) originator += CGSMAlphabets.hex2ExtChar( (int) str1.charAt(++i), CGSMAlphabets.GSM7BITDEFAULT);
				else originator += CGSMAlphabets.hex2Char( (int) str1.charAt(i), CGSMAlphabets.GSM7BITDEFAULT);
			}
		}
		//else if ( (addr & (1 << 6)) == 0 && (addr & (1 << 5)) == 0 && (addr & (1 << 4)) != 0) originator = "+" + originator;

		index = j + k + 2;
		str1 = "" + pdu.charAt(index) + pdu.charAt(index + 1);
		protocol = Integer.parseInt(str1, 16);
		index += 2;
		year = Integer.parseInt("" + pdu.charAt(index + 1) + pdu.charAt(index)); index += 2;
		month = Integer.parseInt("" + pdu.charAt(index + 1) + pdu.charAt(index)); index += 2;
		day = Integer.parseInt("" + pdu.charAt(index + 1) + pdu.charAt(index)); index += 2;
		hour = Integer.parseInt("" + pdu.charAt(index + 1) + pdu.charAt(index)); index += 2;
		min = Integer.parseInt("" + pdu.charAt(index + 1) + pdu.charAt(index)); index += 2;
		sec = Integer.parseInt("" + pdu.charAt(index + 1) + pdu.charAt(index)); index += 4;
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year + 2000);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, min);
		cal.set(Calendar.SECOND, sec);
		date = cal.getTime();
		str1 = decodeText(pdu.substring(index + 2), protocol & 0x0C);
//		switch (protocol & 0x0C)
//		{
//			case 0:
//				str1 = pduToText(pdu.substring(index + 2));
//				str2 = "";
//				for (i = 0; i < str1.length(); i ++) str2 += CGSMAlphabets.hex2Char((int) str1.charAt(i), CGSMAlphabets.GSM7BITDEFAULT);
//				str1 = str2;
//				break;
//			case 4:
//				index += 2;
//				str1 = "";
//				while (index < pdu.length())
//				{
//					i = Integer.parseInt("" + pdu.charAt(index) + pdu.charAt(index + 1), 16);
//					str1 = str1 + (char) i;
//					index += 2;
//				}
//				break;
//			case 8:
//				index += 2;
//				str1 = "";
//				while (index < pdu.length())
//				{
//					i = Integer.parseInt("" + pdu.charAt(index) + pdu.charAt(index + 1), 16);
//					j = Integer.parseInt("" + pdu.charAt(index + 2) + pdu.charAt(index + 3), 16);
//					str1 = str1 + (char) ((i * 256) + j);
//					index += 4;
//				}
//				break;
//		}
		setOriginator(originator);
		setDate(date);
		setText(str1);
	}

	/**
		Set the phone number of the originator. Applicable to incoming messages.

		@param	recipient	the originator's phone number (international format).
	*/
	public void setOriginator(String originator) { this.originator = originator; }

	/**
		Returns the originator's phone number (international format). 
		Applicable only for incoming messages.

		@return	 the originator's phone number.
	*/
	public String getOriginator() { return originator; }

}

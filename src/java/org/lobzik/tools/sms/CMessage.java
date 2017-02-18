//
//	jSMSEngine API.
//	An open-source API package for sending and receiving SMS via a GSM device.
//	Copyright (C) 2002,2003, Thanasis Delenikas, Athens/GREECE
//		Web Site: http://www.jsmsengine.org
//		EMail: admin@jsmsengine.org
//
//	jSMSEngine is a package which can be used in order to add SMS processing
//		capabilities in an application. jSMSEngine is written in Java. It allows you
//		to communicate with a compatible mobile phone or GSM Modem, and
//		send / receive SMS messages.
//
//	jSMSEngine is distributed under the LGPL license.
//
//	This library is free software; you can redistribute it and/or
//		modify it under the terms of the GNU Lesser General Public
//		License as published by the Free Software Foundation; either
//		version 2.1 of the License, or (at your option) any later version.
//	This library is distributed in the hope that it will be useful,
//		but WITHOUT ANY WARRANTY; without even the implied warranty of
//		MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//		Lesser General Public License for more details.
//	You should have received a copy of the GNU Lesser General Public
//		License along with this library; if not, write to the Free Software
//		Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//	Thank you for using and supporting jSMSEngine.
//	--Thanasis.

package org.lobzik.tools.sms;

import java.util.*;

/**
	This class encapsulates the basic characteristics of an SMS message. A message
	is further subclassed to an "Incoming" message and an "Outgoing" message.
	<br><br>
	This class is <strong>never</strong> used directly. Please use one of its descendants.

	@see	CIncomingMessage
	@see	COutgoingMessage
	@see	CPhoneBook
*/
public class CMessage
{
	public static final int MESSAGE_ENCODING_7BIT = 0;
	public static final int MESSAGE_ENCODING_8BIT = 4;
	public static final int MESSAGE_ENCODING_UNICODE = 8;

	public static final int TYPE_INCOMING = 1;
	public static final int TYPE_OUTGOING = 2;

	private int type;
	protected String id;
	protected int memIndex;
	protected Date date;
	protected String originator;
	protected String recipient;
	protected String text;
	protected int messageEncoding;

	private static final int MAX_PDU_LENGTH = 140; // bytes long

	/**
		Default constructor of the class.

		@param	type	the type (incoming/outgoing) of the message.
		@param	date	the creation date of the message.
		@param	originator	the originator's number. Applicable only for incoming messages.
		@param	recipient	the recipient's number. Applicable only for outgoing messages.
		@param	text	the actual text of the message.
		@param	memIndex		the index of the memory location in the GSM device where
						this message is stored. Applicable only for incoming messages.

		<br><br>Notes:<br>
		<ul>
			<li>Phone numbers are represented in their international format (e.g. +306974... for Greece).</li>
			<li>"Recipient" may be an entry from the phonebook.</li>
		</ul>
	*/
	public CMessage(int type, Date date, String originator, String recipient, String text, int memIndex)
	{
		this.type = type;
		this.date = date;
		this.originator = originator;
		this.recipient = recipient;
		this.memIndex = memIndex;
		this.messageEncoding = MESSAGE_ENCODING_7BIT;
		setText(text);
	}

	/**
		Returns the type of the message. Type is either incoming or outgoing, as denoted
		by the class' static values INCOMING and OUTGOING.

		@return  the type of the message.
	*/
	public int getType() { return type; }

	/**
		Returns the id of the message.

		@return  the id of the message.
	*/
	public String getId() { return id; }

	/**
		Returns the memory index of the GSM device, where the message is stored.
		Applicable only for incoming messages.

		@return  the memory index of the message.
	*/
	public int getMemIndex() { return memIndex; }

	/**
		Returns the date of the message. For incoming messages, this is the sent date.
		For outgoing messages, this is the creation date.

		@return  the date of the message.
	*/
	public Date getDate() { return date; }

	/**
		Returns the actual text of the message (ASCII).

		@return  the text of the message.
	*/
	public String getText() {
		String outputText = text;
		if (text != null) {
			String pdu = encodeText(text);
			outputText = decodeText(pdu, getMessageEncoding());
		}
		return outputText; 
	}

	public String getNativeText()
	{
		return text;
	}
	/**
		Returns the text of the message, in hexadecimal format.

		@return  the text of the message (HEX format).
	*/
	public String getHexText() { 
		return CGSMAlphabets.text2Hex(text, CGSMAlphabets.GSM7BITDEFAULT); 
	}

	/**
		Returns the encoding method of the message. Returns of the constants
		MESSAGE_ENCODING_7BIT, MESSAGE_ENCODING_8BIT, MESSAGE_ENCODING_UNICODE.
		This is meaningful only when working in PDU mode.

		@return  the message encoding.
	*/
	public int getMessageEncoding() { return messageEncoding; }

	/**
		Set the id of the message.

		@param	id	the id of the message.
	*/
	public void setId(String id) { this.id = id; }

	/**
		Set the text of the message.

		@param	text	the text of the message.
	*/
	public void setText(String text) {
		this.text = text;
	}

	/**
		Set the date of the message.

		@param	date	the date of the message.
	*/
	public void setDate(Date date) { this.date = date; }

	/**
		Set the message encoding. Should be one of the constants
		MESSAGE_ENCODING_7BIT, MESSAGE_ENCODING_8BIT, MESSAGE_ENCODING_UNICODE.
		This is meaningful only when working in PDU mode - default is 7bit.

		@param	recipient	the recipient's phone number (international format).
	*/
	public void setMessageEncoding(int messageEncoding) {
		String text = getText(); 
		this.messageEncoding = messageEncoding;
		setText(text);
	}

	public String toString()
	{
		String str;

		str = "** GSM MESSAGE **\n";
		str += "  Type: " + (type == TYPE_INCOMING ? "Incoming." : "Outgoing.") + "\n";
		str += "  Id: " + id + "\n";
		str += "  Memory Index: " + memIndex + "\n";
		str += "  Date: " + date + "\n";
		str += "  Originator: " + originator + "\n";
		str += "  Recipient: " + recipient + "\n";
		str += "  Text: " + text + "\n";
		str += "  Hex Text: " + CGSMAlphabets.text2Hex(text, CGSMAlphabets.GSM7BITDEFAULT) + "\n";
		str += "  Encoding: " + messageEncoding + "\n";
		str += "***\n";
		return str;
	}
	
	public String getPDU(String smscNumber) {
		return getPDU(smscNumber, this.getText());
	}
	
	private String getPDU(String smscNumber, String text) {
		String pdu;
		String str1, str2;

		pdu = "";
		if ((smscNumber != null) && (smscNumber.length() != 0))
		{
			str1 = "91" + toBCDFormat(smscNumber.substring(1));
			str2 = Integer.toHexString(str1.length() / 2);
			if (str2.length() != 2) str2 = "0" + str2;
			pdu = pdu + str2 + str1;
		}
		else if ((smscNumber != null) && (smscNumber.length() == 0)) pdu = pdu + "00";
		pdu = pdu + "11";
		pdu = pdu + "00";
		str1 = recipient;
		str1 = toBCDFormat(str1.substring(1));
		str2 = Integer.toHexString(recipient.length() - 1);
		if (str2.length() != 2) str2 = "0" + str2;
		str1 = "91" + str1;
		pdu = pdu + str2 + str1;
		pdu = pdu + "00";
		switch (getMessageEncoding())
		{
			case MESSAGE_ENCODING_7BIT:
				pdu = pdu + "00";
				break;
			case MESSAGE_ENCODING_8BIT:
				pdu = pdu + "04";
				break;
			case MESSAGE_ENCODING_UNICODE:
				pdu = pdu + "08";
				break;
		}
		pdu = pdu + "AA";
		String encodedText = encodeText(text);
		// Encode the length of the data
		String encodedDataLength = Integer.toHexString(getDataLength(encodedText));
		if (encodedDataLength.length() != 2) 
			encodedDataLength = "0" + encodedDataLength;
		// Finalize PDU
		pdu = pdu + encodedDataLength + encodedText;
		return pdu.toUpperCase();
	}

	private String encodeText(String text) {
		String encodedText = "";
		switch (getMessageEncoding()) {
			case MESSAGE_ENCODING_7BIT:
				encodedText = textToPDU(text);
				break;
			case MESSAGE_ENCODING_8BIT:
				for (int i = 0; i < text.length(); i ++)  {
					char c = text.charAt(i);
					encodedText = encodedText + ((Integer.toHexString((int) c).length() < 2) ? "0" + Integer.toHexString((int) c) : Integer.toHexString((int) c));  
				}
				break;
			case MESSAGE_ENCODING_UNICODE:
				for (int i = 0; i < text.length(); i ++) {
					char c = text.charAt(i);
					int high = (int) (c / 256);
					int low = c % 256;
					encodedText += ((Integer.toHexString(high).length() < 2) ? "0" + Integer.toHexString(high) : Integer.toHexString(high));
					encodedText += ((Integer.toHexString(low).length() < 2) ? "0" + Integer.toHexString(low) : Integer.toHexString(low));
				}
				break;
		}
		// Limit encoded PDU data to MAX_PDU_LENGTH
		if (encodedText.length() > MAX_PDU_LENGTH * 2)
			encodedText = encodedText.substring(0, MAX_PDU_LENGTH * 2);
		return encodedText;
	}
	
	protected String decodeText(String pdu, int encoding) {
		String text = "";
		int index = 0;
		switch (encoding)  {
			case MESSAGE_ENCODING_7BIT: {
				String str1 = pduToText(pdu);
				text = CGSMAlphabets.text2Text(str1, CGSMAlphabets.GSM7BITDEFAULT);
				break;
			}
			case MESSAGE_ENCODING_8BIT: {
				while (index < pdu.length()) {
					int i = Integer.parseInt("" + pdu.charAt(index) + pdu.charAt(index + 1), 16);
					text += (char) i;
					index += 2;
				}
				break;
			}
			case MESSAGE_ENCODING_UNICODE: {
				while (index < pdu.length()) {
					int high = Integer.parseInt("" + pdu.charAt(index) + pdu.charAt(index + 1), 16);
					int low = Integer.parseInt("" + pdu.charAt(index + 2) + pdu.charAt(index + 3), 16);
					text += (char) ((high * 256) + low);
					index += 4;
				}
				break;
			}
		}
		return text;
	}
	
	private int getDataLength(String pdu) {
		int length = 0;
		switch (getMessageEncoding()) {
			case MESSAGE_ENCODING_7BIT: {
				// Return number of septets
				String text = decodeText(pdu, MESSAGE_ENCODING_7BIT);
				String hexText = CGSMAlphabets.text2Hex(text, CGSMAlphabets.GSM7BITDEFAULT);
				length = hexText.length() / 2;
				break;
			}
			case MESSAGE_ENCODING_8BIT: {
				// Return number of octets 
				length = pdu.length() / 2;
				break;
			}
			case MESSAGE_ENCODING_UNICODE: {
				// Return number of octets 
				length = pdu.length() / 2;
				break;
			}
		}
		return length;
	}

	private String textToPDU(String text) {
		String pdu, str1;
		byte[] oldBytes, newBytes;
		BitSet bitSet;
		int i, j, value1, value2;

		str1 = "";		
		text = CGSMAlphabets.text2Hex(text, CGSMAlphabets.GSM7BITDEFAULT);
		for (i = 0; i < (text.length() - 1); i += 2) {
			j = (Integer.parseInt("" + text.charAt(i), 16) * 16) + Integer.parseInt("" + text.charAt(i + 1), 16);
			str1 += (char) j;
		}
		text = str1; 
		oldBytes = text.getBytes();
		bitSet = new BitSet(text.length() * 8);

		value1 = 0;
		for (i = 0; i < text.length(); i ++)
			for (j = 0; j < 7; j ++)
			{
				value1 = (i * 7) + j;
				if ((oldBytes[i] & (1 << j)) != 0) bitSet.set(value1);
			}
		value1 ++;

		if (((value1 / 56) * 56) != value1) value2 = (value1 / 8) + 1;
		else value2 = (value1 / 8);
		if (value2 == 0) value2 = 1;

		newBytes = new byte[value2];
		for (i = 0; i < value2; i ++)
			for (j = 0; j < 8; j ++)
				if ((value1 + 1) > ((i * 8) + j))
					if (bitSet.get(i * 8 + j)) newBytes[i] |= (byte) (1 << j);

		pdu = "";
		for (i = 0; i < value2; i ++)
		{
			str1 = Integer.toHexString((int) newBytes[i]);
			if (str1.length() != 2) str1 = "0" + str1;
			str1 = str1.substring(str1.length() - 2, str1.length());
			pdu += str1;
		}
		return pdu;
	}

	private String toBCDFormat(String s) 	{
		String bcd;
		int i;

		if ((s.length() % 2) != 0) s = s + "F";
		bcd = "";
		for (i = 0; i < s.length(); i += 2) bcd = bcd + s.charAt(i + 1) + s.charAt(i);
		return bcd; 
	}
	
	protected String pduToText(String pdu) {
		String text;
		byte oldBytes[], newBytes[];
		BitSet bitSet;
		int i, j, value1, value2;

		oldBytes = new byte[pdu.length() / 2];
		for (i = 0; i < pdu.length() / 2; i ++)
		{
			oldBytes[i] = (byte) (Integer.parseInt(pdu.substring(i * 2, (i * 2) + 1), 16) * 16);
			oldBytes[i] += (byte) Integer.parseInt(pdu.substring((i * 2) + 1, (i * 2) + 2), 16);
		}

		bitSet = new BitSet(pdu.length() / 2 * 8);
		value1 = 0;
		for (i = 0; i < pdu.length() / 2; i ++)
			for (j = 0; j < 8; j ++)
			{
				value1 = (i * 8) + j;
				if ((oldBytes[i] & (1 << j)) != 0) bitSet.set(value1);
			}
		value1 ++;

		value2 = value1 / 7;
		if (value2 == 0) value2 ++;

		newBytes = new byte[value2];
		for (i = 0; i < value2; i ++)
			for (j = 0; j < 7; j ++)
				if ((value1 + 1) > (i * 7 + j))
					if (bitSet.get(i * 7 + j)) newBytes[i] |= (byte) (1 << j);

		if (newBytes[value2 - 1] == 0) text = new String(newBytes, 0, value2 - 1);
		else text = new String(newBytes);
		return text;
	}
}

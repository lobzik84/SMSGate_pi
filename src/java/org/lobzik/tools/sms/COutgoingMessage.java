package org.lobzik.tools.sms;

import java.util.*;

/**
	This class represents an outgoing SMS message, i.e. message created for dispatch
	from the GSM device.

	@see	CMessage
	@see	CIncomingMessage
	@see	CPhoneBook
	@see	CService#sendMessage(COutgoingMessage)
	@see	CService#sendMessage(LinkedList)
*/
public class COutgoingMessage extends CMessage {
	
	private Date dispatchDate;

	/**
		Default constructor of the class.
	*/
	public COutgoingMessage()
	{
		super(TYPE_OUTGOING, null, null, null, null, -1);
		setDispatchDate(null);
		setDate(new Date());
	}

	/**
		Constructor of the class.

		@param	recipient	the recipients's number.
		@param	text	the actual text of the message.

		<br><br>Notes:<br>
		<ul>
			<li>Phone numbers are represented in their international format (e.g. +306974... for Greece).</li>
			<li>If you use a phonebook, the phone number may be a string starting with the '~' character,
					representing an entry in the phonebook.</li>
			<li>By default, a created message is set to be encoded in 7bit. If you want to change that, be sure
					to operate in PDU mode, and change the encoding with setMessageEncoding() method.</li>
		</ul>
	*/
	public COutgoingMessage(String recipient, String text)
	{
		super(TYPE_OUTGOING, new Date(), null, recipient, text, -1);
		setDispatchDate(null);
		setDate(new Date());
	}

	/**
		Set the phone number of the recipient. Applicable to outgoing messages.

		@param	recipient	the recipient's phone number (international format).
	*/
	public void setRecipient(String recipient) { this.recipient = recipient; }

	/**
		Returns the recipient's phone number (international format). 
		Applicable only for outgoing messages.
		<br>
		<strong>This may be an entry from the phonebook.</strong>

		@return  the type of the message.
	*/
	public String getRecipient() { return recipient; }

	/**
		Sets the dispatch date of the message.

		@param	date	the dispatch date of the message.
	*/
	protected void setDispatchDate(Date date) { this.dispatchDate = date; }

	/**
		Returns the dispatch date of the message.

		@return  the dispatch date of the message.
	*/
	public Date getDispatchDate() { return dispatchDate; }

}

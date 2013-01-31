package edu.utk.mabe.scopelab.scope.admin.service.messenging;

public interface MessageDestination 
{
	void sendMessage(String message) throws MessengingException;
	void setMessageListener(MessageListener listener) throws MessengingException;
	void clearMessageListener() throws MessengingException;
	String receiveMessage() throws MessengingException;
	String receiveMessage(long timeout) throws MessengingException;
	String getName();
	void close() throws MessengingException;
}

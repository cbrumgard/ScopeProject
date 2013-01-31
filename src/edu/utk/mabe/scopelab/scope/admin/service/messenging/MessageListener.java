package edu.utk.mabe.scopelab.scope.admin.service.messenging;



public interface MessageListener 
{
	public void onMessage(String message);
	public void onError(Throwable e);
}

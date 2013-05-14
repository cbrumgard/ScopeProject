package edu.utk.mabe.scopelab.scope.admin.service.websocket;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ScheduledIOOperation implements RunnableFuture<Void> 
{
	/* Instance variables */
	private boolean isDone 		 = false;
	private boolean isCancelled  = false;
	private CountDownLatch latch = new CountDownLatch(1);
	private Runnable runnable    = null;
	private Lock lock			 = new ReentrantLock();
	
	protected void _isDone(boolean isDone)
	{
		this.isDone = isDone;
	}
	
	protected void _isCancelled(boolean isCancelled)
	{
		this.isCancelled = isCancelled;
	}
	
	
	public ScheduledIOOperation(Runnable r) 
	{
		this.runnable = r;
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning)
	{
		if(this.isCancelled)
		{
			return true;
		}
		
		if(this.isDone)
		{
			return false;
		}
		
		if(this.lock.tryLock())
		{
			latch.countDown();
			this.isCancelled = true;
			
			this.lock.unlock();
			
			return true;
		}else
		{
			return false;
		}
	}
	
	@Override
	public Void get() throws InterruptedException, ExecutionException
	{
		latch.await();
		
		if(this.isCancelled)
		{
			throw new ExecutionException("Operation was cancelled", null);
		}
		
		return null;
	}
	
	@Override
	public Void get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException
	{
		if(latch.await(timeout, unit) == false)
		{
			throw new TimeoutException();
		}
		
		if(this.isCancelled)
		{
			throw new ExecutionException("Operation was cancelled", null);
		}
		
		return null;
	}
	
	@Override
	public boolean isCancelled()
	{
		return this.isCancelled;
	}
	
	@Override
	public boolean isDone()
	{
		return this.isDone;
	}
	
	@Override
	public void run()
	{
		try
		{
			this.lock.lock();

			if(this.isCancelled)
			{
				return;
			}

			try
			{
				this.runnable.run();

			}finally
			{
				this.isDone = true;
				this.lock.unlock();
			}
		
		}finally
		{
			this.latch.countDown();
		}
	}
}

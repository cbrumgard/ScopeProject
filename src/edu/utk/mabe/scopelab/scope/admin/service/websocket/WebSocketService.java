package edu.utk.mabe.scopelab.scope.admin.service.websocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannel;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.log.StdErrLog;

public class WebSocketService 
{
	/* Instance variables */
	protected AsynchronousChannelGroup asynchronousChannelGroup = null;
	protected AsynchronousServerSocketChannel serverChannel = null;
	protected List<WebSocketConnection> connections = new LinkedList<>();
	protected BlockingQueue<ScheduledIOOperation> queue = new LinkedBlockingQueue<>();
	
	{
		new Thread(new Runnable()
			{
				@Override
				public void run() 
				{
					try 
					{
						for(;;)
						{
							Runnable r = queue.take();

							r.run();
						}
						
					}catch(InterruptedException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
	}
	
	void addIORequest(AsynchronousChannel channel, Runnable r)
	{
		this.queue.add(new ScheduledIOOperation(r));
	}
	
	void closeWebSocketConnection(WebSocketConnection conn)
	{
		try 
		{
			this.connections.remove(conn);
			conn.close();
			
		}catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public WebSocketService() throws IOException 
	{
		this.asynchronousChannelGroup = 
				AsynchronousChannelGroup.withFixedThreadPool(32, 
						Executors.defaultThreadFactory());
		
		/* Creates the server socket channel */
		this.serverChannel = AsynchronousServerSocketChannel.open(
				this.asynchronousChannelGroup);
		this.serverChannel.bind(null);
		
		final WebSocketService webSocketService = this;
		
		/* Creates accept handler  */
		this.addIORequest(this.serverChannel, new Runnable() 
			{
				@Override
				public void run() 
				{
					final Runnable r = this;
					
					serverChannel.accept(null,
						new CompletionHandler<AsynchronousSocketChannel, Void>() 
						{
							@Override
							public void completed(AsynchronousSocketChannel result,
									Void attachment) 
							{
								/* Reset the completion handler */
								addIORequest(serverChannel, r);
								
								System.out.println("I have a connection");
								
								try 
								{
									result.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
									
								}catch (IOException e) 
								{
									e.printStackTrace();
								}
								
								
								final WebSocketConnection webSocketConnection = 
										new WebSocketConnection(result, webSocketService);
								connections.add(webSocketConnection);
								
								webSocketConnection.start();
							}

							@Override
							public void failed(Throwable exc, Void attachment) 
							{
								System.out.println("Error");
								exc.printStackTrace();
								
								/* Reset the completion handler */
								addIORequest(serverChannel, r);
							}	
						});
				}
			});
		
		
		
		
	}
	
	public InetSocketAddress getInetAddress() throws IOException
	{
		return (InetSocketAddress)(this.serverChannel.getLocalAddress());
	}
}

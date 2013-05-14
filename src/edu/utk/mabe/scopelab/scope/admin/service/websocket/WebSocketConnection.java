package edu.utk.mabe.scopelab.scope.admin.service.websocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.security.MessageDigest;
import java.util.BitSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;



class WebSocketConnection implements AsynchronousByteChannel
{
	protected static class WebSocketHeader
	{
		private boolean fin;
		private byte resv;
		private byte opcode;
		private boolean maskbit;
		private byte[] mask;
		private int headerLength;
		private int payloadLength;
		
		protected WebSocketHeader(boolean fin, byte resv, byte opcode, 
				boolean maskbit, byte[] mask, int headerLength, int payloadLength)
		{
			this.setFin(fin);
			this.resv = resv;
			this.opcode = opcode;
			this.maskbit = maskbit;
			this.mask    = mask;
			this.headerLength = headerLength;
			this.payloadLength = payloadLength;
		}
		
		static WebSocketHeader parseHeader(ByteBuffer buffer) throws IOException
		{
			/* Gets the fin flag */
			boolean fin = (buffer.get(0) & 0b10000000) != 0;
			
			/* Gets the reserved flags */
			byte resv = (byte)(buffer.get(0) & 0b01110000);
			
			if(resv != 0)
			{
				throw new IOException("Invalid resv settings in websocket header");
			}
			
			/* Gets the opcode */
			byte opcode = (byte)(buffer.get(0) & 0b00001111);
			
			System.out.printf("Opcode is %x\n", opcode);
			
			/* Gets the mask bit */
			boolean maskbit = (buffer.get(1) & 0b10000000) != 0;
			
			System.out.printf("Mask bit = %b\n", maskbit);
			
			if(maskbit == false)
			{
				throw new IOException("Mask bit from client not set");
			}
			
			/* Gets the length */
			byte tmpLength = (byte)(buffer.get(1) & 0b01111111);
			
			System.out.printf("Length = %d\n", tmpLength);
			
			
			int maskOffset = 0;
			long length;
			
			switch(tmpLength)
			{
				case 126:
					length = 0L | buffer.getShort(2);
					maskOffset = 6;
					break;
					
				case 127:
					length = buffer.getLong(2);
					
					if(length > Integer.MAX_VALUE)
					{
						throw new IOException("Data packet too big to be processed");
					}
					
					maskOffset = 10;
					break;
					
				/* Length is length */
				default:
					length = tmpLength;
					maskOffset = 2;
					break;
			}
			
		
			byte[] mask = new byte[4];
			
			buffer.position(maskOffset);
			buffer.get(mask);
			
			return new WebSocketHeader(fin, resv, opcode, maskbit, mask, 
					maskOffset+4, (int)length);
		}

		public boolean isFin() 
		{
			return fin;
		}

		public void setFin(boolean fin) 
		{
			this.fin = fin;
		}
		
		public byte getResv() 
		{
			return resv;
		}

		public void setResv(byte resv) 
		{
			this.resv = resv;
		}

		public byte getOpcode() 
		{
			return opcode;
		}

		public void setOpcode(byte opcode) 
		{
			this.opcode = opcode;
		}

		public boolean isMaskbit() 
		{
			return maskbit;
		}

		public void setMaskbit(boolean maskbit) 
		{
			this.maskbit = maskbit;
		}

		public byte[] getMask() 
		{
			return mask;
		}

		public void setMask(byte[] mask) 
		{	
			this.mask = mask;
		}

		public int getHeaderLength() 
		{
			return headerLength;
		}

		public void setHeaderLength(int headerLength) 
		{
			this.headerLength = headerLength;
		}

		public int getPayloadLength() 
		{
			return payloadLength;
		}

		public void setPayloadLength(int payloadLength) 
		{
			this.payloadLength = payloadLength;
		}
	}
	
	/* Instance variables */
	protected AsynchronousSocketChannel socketChannel = null;
	protected ByteBuffer recvBuffer = ByteBuffer.wrap(new byte[4096]);
	protected ByteBuffer sendBuffer = ByteBuffer.wrap(new byte[4096]);
	protected WebSocketService webSocketService = null;
	
	protected WebSocketHeader currRecvHeader = null;
	
	WebSocketConnection(AsynchronousSocketChannel socketChannel, 
			WebSocketService webSocketService) 
	{
		this.socketChannel = socketChannel;
		this.webSocketService = webSocketService;
	}

	
	protected int _processPacket(ByteBuffer headerBuffer, ByteBuffer dataBuffer) 
			throws IOException
	{
		int start = dataBuffer.position();
		
		/* Parse out the header */
		if(this.currRecvHeader == null)
		{
			currRecvHeader = WebSocketHeader.parseHeader(headerBuffer);
			
			dataBuffer.put(headerBuffer);
		}
		
		if(this.currRecvHeader.isMaskbit())
		{
			for(int i=0; i<dataBuffer.position()-start; i++)
			{
				dataBuffer.put(start+i,
					(byte)(dataBuffer.get(start+i) ^ currRecvHeader.getMask()[i % 4]));
			}
		}
		
	
		return dataBuffer.position()-start;
	}
	
	void start()
	{
		
		/*** Handles the handshake ***/
		
		this.socketChannel.read(recvBuffer, 30, TimeUnit.SECONDS, webSocketService, 
			new CompletionHandler<Integer, WebSocketService>()
			{
				@Override
				public void completed(Integer result, final WebSocketService webSocketService) 
				{
					try(BufferedReader reader = new BufferedReader( 
							new StringReader(new String(recvBuffer.array()))))
					{
						
						/*  
						 GET / HTTP/1.1
						 Upgrade: websocket
						 Connection: Upgrade
						 Host: localhost:52410
						 Origin: http://localhost:8080
						 Sec-WebSocket-Key: NwS1ADVa71hH5HgPrqVIkA==
						 Sec-WebSocket-Version: 13
						 Sec-WebSocket-Extensions: x-webkit-deflate-frame

						 */
						
						String line = reader.readLine();
						
						System.out.println(line+"<end>");
						
						/* Checks and gets the service from the first line */
						Pattern getLinePattern = Pattern.compile("GET\\s+([^s]+)\\s+HTTP\\/1.1\\s*");
						Matcher matcher = getLinePattern.matcher(line);
						
						if(matcher.matches() == false)
						{
							throw new Exception("");
						}
						
						System.out.printf("match = %s\n", matcher.group(0));
						String service = matcher.group(1);
						
						if(service == null)
						{
							throw new Exception("No service");
						}
						
						System.out.println("Service = "+service);
						
						
						
						String securityKey = null;
						boolean upgradeLine = false;
						boolean connectionLine = false;
						
						
						while((line = reader.readLine()) != null)
						{
							System.out.println("<start>"+line+"<end>");
							/* End of header detected */
							if(line.equals(""))
							{
								break;
							}
								
							String[] fields = line.split(":\\s+", 2);
							
							if(fields.length != 2)
							{
								throw new Exception("");
							}
							
							System.out.printf("Field[%s] = %s\n", fields[0], fields[1]);
							
							
							
							switch(fields[0])
							{
								case "Upgrade":
									if(fields[1].trim().equals("websocket"))
									{
										upgradeLine = true;
									}
									
									break;
									
								case "Connection":
									connectionLine = true;
									break;
									
								case "Sec-WebSocket-Version":
									if(fields[1].trim().equals("13") == false)
									{
										throw new Exception("Invalid version");
									}
									
									break;
									
								case "Sec-WebSocket-Key":
									securityKey = fields[1].trim();
									break;
							}
						}
						
						/* Checks the Upgrade line */
						if(upgradeLine == false)
						{
							throw new Exception("No upgrade line in header");
						}
						
						/* Checks the Connection line */
						if(connectionLine == false)
						{
							throw new Exception("No connection line in header");
						}
						
						/* Checks that the security key was filled in */
						if(securityKey == null)
						{
							throw new Exception("No security key sent");
						}
						
						final String responseSecurityKey = securityKey +"258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
						
						final MessageDigest md = MessageDigest.getInstance("SHA1");
						
						
						webSocketService.addIORequest(socketChannel, 
							new Runnable()
							{
								public void run()
								{
	
									socketChannel.write(
											ByteBuffer.wrap(
													String.format("HTTP/1.1 101 Switching Protocols\r\n" +
															"Upgrade: websocket\r\n" + 
															"Connection: Upgrade\r\n" +
															"Sec-WebSocket-Accept: %s\r\n" +
															
															"\r\n", 
															Base64.encodeBase64String(
																	md.digest(responseSecurityKey.getBytes()))).getBytes()), 
																	30, TimeUnit.SECONDS,
																	null, 
																	new CompletionHandler<Integer, Void>() 
																	{
	
												@Override
												public void completed(
														Integer result,
														Void attachment) 
												{
													System.out.printf("Wrote %d\n", result);
													
													
													webSocketService.addIORequest(socketChannel, 
														new Runnable()
														{
															@Override
															public void run() 
															{
				
																final ByteBuffer buffer = ByteBuffer.wrap(new byte[4096]);
																
																socketChannel.read(buffer, webSocketService, new CompletionHandler<Integer, WebSocketService>() 
																{

																	@Override
																	public void completed( 
																			Integer result,
																			WebSocketService attachment) 
																	{
																		
																	}

																	@Override
																	public void failed(
																			Throwable exc,
																			WebSocketService attachment) 
																	{
																		
																	}
																});
															}
														
														});
												}
	
												@Override
												public void failed(Throwable exc,
														Void attachment) 
												{
	
													System.out.printf("Error on write 1 with %s\n", exc);
													exc.printStackTrace();
												}
	
										});
								}
							});

						
		
						
					}catch(Throwable e)
					{
						e.printStackTrace();
						
//						try 
//						{
//							socketChannel.close();
//						}catch (IOException e1) 
//						{
//							e1.printStackTrace();
//						}
					}
					
				}

				@Override
				public void failed(Throwable exc, 
						WebSocketService webSocketService) 
				{
					System.out.printf("Error on read 1 with %s\n", exc);
					exc.printStackTrace();
				}
			});
	}
	
	
	public void close() throws IOException
	{
		this.socketChannel.close();
	}

	@Override
	public boolean isOpen() 
	{
		return this.socketChannel.isOpen();
	}

	@Override
	public <A> void read(final ByteBuffer userBuffer, final A attachment,
			final CompletionHandler<Integer, ? super A> handler) 
	{
		
		final ByteBuffer headerBuffer = 
			(currRecvHeader == null) ? ByteBuffer.wrap(new byte[10]) : null;
			
			
		final int start = userBuffer.position();
		
		this.socketChannel.read(headerBuffer, null, 
			new CompletionHandler<Integer, Integer>() 
			{
				@Override
				public void completed(Integer result, Integer payloadLength) 
				{
					try 
					{
						/* Processes the packet of data */
						payloadLength = _processPacket(headerBuffer, userBuffer);
						
						/* Issue another read with user buffer to fill in */
						if(payloadLength > 0 && userBuffer.remaining() > 0)
						{
							socketChannel.read(userBuffer, payloadLength, this);
							
						/* More data to be read but no buffer to read it into */
						}else if(payloadLength > 0)
						{
							handler.completed(userBuffer.position()-start, attachment);
							
						/* Done */
						}else
						{
							handler.completed((int)payloadLength, attachment);
						}
						
					}catch (Throwable e) 
					{
						try 
						{
							close();
						}catch (IOException e1) 
						{
							// Do nothing
						}
						
						/* Call the failed method for the user's handler */
						handler.failed(e, attachment);
					}
				}

				@Override
				public void failed(Throwable exc, Integer payload) 
				{
					handler.failed(exc, attachment);
				}
			});
		
	}

	@Override
	public Future<Integer> read(ByteBuffer dst) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <A> void write(ByteBuffer src, A attachment,
			CompletionHandler<Integer, ? super A> handler) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Future<Integer> write(ByteBuffer src) 
	{
		// TODO Auto-generated method stub
		return null;
	}
}

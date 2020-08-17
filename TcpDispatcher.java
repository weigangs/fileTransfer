package fileTransfer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpDispatcher {
	
	private String dispatchIp = "";
	
	private int dispatchPort = 0;
	
	private int receivePort = 0;
	
	private ServerSocket server = null;
	
	private ExecutorService threadPool = null;
	

	public TcpDispatcher(String dispatchIp, int dispatchPort, int receivePort){
		threadPool = Executors.newFixedThreadPool(20);
		this.dispatchIp = dispatchIp;
		this.dispatchPort = dispatchPort;
		this.receivePort = receivePort;
	}
	
	public void execute(){
		try {
			server = new ServerSocket(receivePort);
			Socket socket = null;
			System.out.println("dispatcher started!");
			while(true){
				socket = server.accept();
				threadPool.execute(new MessageHandler(socket));
				Thread.sleep(100);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	class MessageHandler implements Runnable{
		
		private Socket socket = null;
		
		private Socket dispatchSocket = null;
		
		private DataInputStream rcvIn = null;
		
		private DataOutputStream rtnOut = null;
		
		private DataInputStream dispatchIn = null;
		
		private DataOutputStream dispatchOut = null;
		
		public MessageHandler(Socket socket){
			this.socket = socket;
			try {
				dispatchSocket = new Socket(dispatchIp, dispatchPort);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try{
				rcvIn = getRcvInputStream(socket.getInputStream());
				rtnOut = getRtnOutputStream(socket.getOutputStream());
				dispatchIn = getDispatchInputStream(dispatchSocket.getInputStream());
				dispatchOut = getDispatchOutputStream(dispatchSocket.getOutputStream());
				System.out.println("开始接受信息...");
				getRcvBytes();
				System.out.println("信息转发完成！");
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				try {
					if(rcvIn != null){rcvIn.close();}
					if(rtnOut != null){rtnOut.close();}
					if(dispatchIn != null){dispatchIn.close();}
					if(dispatchOut != null){dispatchOut.close();}
					if(socket != null){socket.close();}
					if(dispatchSocket != null){dispatchSocket.close();}
				}catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
		private DataInputStream getRcvInputStream(InputStream in){
			DataInputStream rcvIn = new DataInputStream(in);
			return rcvIn;
		}
		
		private DataOutputStream getRtnOutputStream(OutputStream out){
			DataOutputStream rtnOut = new DataOutputStream(out);
			return rtnOut;
		}
		
		private DataInputStream getDispatchInputStream(InputStream in){
			DataInputStream rcvIn = new DataInputStream(in);
			return rcvIn;
		}
		
		private DataOutputStream getDispatchOutputStream(OutputStream out){
			DataOutputStream rtnOut = new DataOutputStream(out);
			return rtnOut;
		}
		
		private void getRcvBytes(){
			byte [] buff = new byte[1024];
			int length = 0;
			try{
				while((length = rcvIn.read(buff)) != -1){
					dispatchOut.write(buff, 0, length);
					dispatchOut.flush();
					if(length < 1024){
						getRtnBytes();
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		
		private void getRtnBytes(){
			byte [] buff = new byte[1024];
			int length = 0;
			try{
				while((length = dispatchIn.read(buff)) != -1){
					rtnOut.write(buff, 0, length);
					rtnOut.flush();
					if(length < 1024){
						break;
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}
	
	public static void main(String []args){
		TcpDispatcher dispatcher = new TcpDispatcher("198.198.198.73", 8268, 10004);
		dispatcher.execute();
	}

}

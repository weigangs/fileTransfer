package fileTransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServerPro {
	
	private ExecutorService threadPool = null;
	
	private  int PORT = 10002;
	
	private  String DIR = "C:\\Users\\shiweigang\\Desktop\\";
	
	public ServerPro(int port, String dirPath){
		threadPool = Executors.newFixedThreadPool(15);
		this.PORT = port;
		this.DIR = dirPath;
	}
	
	public void execute(){
		System.out.println("start to accept new request...");
		ServerSocket server = null;
		Socket socket = null;
		long sT = System.currentTimeMillis();
		int count = 0;
		try{
			server = new ServerSocket(PORT);
			server.setSoTimeout(10000);
			System.out.print("receiving...");
			while(true){
				socket = server.accept();
				System.out.println("starting to receive "+(++count)+"-th task");
				System.out.println("task "+count+" has pushed queue！");
				threadPool.execute(new ServerHandler(socket));
				Thread.sleep(1);
				
			}
		}catch(Exception e){
			if(e instanceof SocketTimeoutException){
				System.err.println("waiting threads...");
				threadPool.shutdown();
				try {
					if(threadPool.awaitTermination(180, TimeUnit.SECONDS)){
						if(count >= 1){
							System.out.println("files received successfully!");
						}
						System.err.println("it costed total time is :"+(System.currentTimeMillis()-sT));
						System.exit(0);
					}
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}else{
				System.err.print(e);
			}
		}finally{
			if(server != null){
				try {
					server.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.exit(0);
		}

	}

	

	
	    
	    class ServerHandler implements Runnable{
	    	
	    	private Socket socket;
	    	
	    	private byte startOver = 0;
	    	
	    	private byte flag = 0;
	    	
	    	private int index = 0;
	    	
	    	private long remainFileLength = 0L;
	    	
	    	private int saveLength = 0;
	    	
	    	private int nextLength = 0;
	    	
	    	private FileOutputStream out = null;
	    	
	    	private InputStream in = null;
	    	
	    	public ServerHandler(Socket socket){
	    		this.socket = socket;
	    	}

			@Override
			public void run() {
				// TODO Auto-generated method stub
				int size = 5094;
				byte [] buff = new byte[size];
				byte [] saveBuff = new byte[size];
				int length  = 0;
				try {
					in = socket.getInputStream();
					while((length = in.read(buff)) != -1){
						copyFile(buff, saveBuff, 0, length, DIR);
					}
					if(out != null){
						out.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			public  void copyFile(byte [] buff, byte [] saveBuff, int off, int len, String parentPath) throws IOException{
				if(startOver != 3 || saveLength == 0){
					if(off == len)return;
				}
				index = off;
				
				if(startOver == 0){
					startOver = 1;
					
					flag = buff[index++];
					
					if(flag == 0 || flag == 1){
						nextLength = 4;
						saveLength = 0;
						if(index == len)return;
						
						if(len - index >= 4){
							nextLength = 0;
							System.arraycopy(buff, index, saveBuff, 0, 4);
							index += 4;
							copyFile(buff, saveBuff, index, len, parentPath);
						}else{
							System.arraycopy(buff, index, saveBuff, 0, len-index);
							nextLength = 4 - len + index;
							saveLength = len-index;
						}
					}else{
						nextLength = 8;
						saveLength = 0;
						if(index == len)return;
						
						if(len - index >= 8){
							nextLength = 0;
							saveLength = 8;
							System.arraycopy(buff, index, saveBuff, 0, 8);
							index += 8;
							copyFile(buff, saveBuff, index, len, parentPath);
							
						}else{
							nextLength = 8 - len + index;
							saveLength = len - index;
							System.arraycopy(buff, index, saveBuff, 0, saveLength);
							
						}
					}
				}else if(startOver == 1){
					startOver = 3;
					if(flag == 0 || flag == 1){
						if(nextLength != 0){
							if(len - index >= nextLength){
								System.arraycopy(buff, index, saveBuff, saveLength, nextLength);
								index += nextLength;
								nextLength = 0;
							}else{
								System.arraycopy(buff, index, saveBuff, saveLength, len-index);
								saveLength += len - index;
								nextLength = nextLength - saveLength;
								startOver = 1;
								return;
							}
						}
						int fNlength = bytesToInt(saveBuff, 0);
						saveLength = 0;
						nextLength = fNlength;
						if(len - index >= fNlength){
							saveLength = nextLength;
							nextLength = 0;
							System.arraycopy(buff, index, saveBuff, 0, fNlength);
							index += fNlength;
							copyFile(buff, saveBuff, index, len, parentPath);
						}else{
							nextLength = fNlength - len + index;
							saveLength = len - index;
							System.arraycopy(buff, index, saveBuff, 0, saveLength);
						}
					}else{
						if(nextLength != 0){
							if(len - index >= nextLength){
								System.arraycopy(buff, index, saveBuff, saveLength, nextLength);
								saveLength += nextLength;
								index += nextLength;
								nextLength = 0;
							}else{
								System.arraycopy(buff, index, saveBuff, saveLength, len-index);
								nextLength = nextLength - len + index;
								saveLength += nextLength;
								startOver = 1;
								return;
							}
						}
						remainFileLength = bytesToLong(saveBuff, 0);
						copyFile(buff, saveBuff, index, len, parentPath);
					}
				}else{
					if(flag == 0){
						startOver = 0;
						if(nextLength != 0){
							if(len - index >= nextLength){
								System.arraycopy(buff, index, saveBuff, saveLength, nextLength);
								index += nextLength;
								saveLength += nextLength;
								nextLength = 0;
							}else{
								System.arraycopy(buff, index, saveBuff, saveLength, len-index);
								nextLength = nextLength - len + index;
								saveLength = saveLength + len - index;
								startOver = 3;
								return;
							}
						}
						String fileName = new String(saveBuff, 0, saveLength+nextLength);
						File file = new File(parentPath + fileName);
						if(!file.exists()){
							file.mkdir();
						}
						copyFile(buff, saveBuff, index, len, parentPath);
					}else if(flag == 1){
						startOver = 0;
						if(nextLength != 0){
							if(len - index >= nextLength){
								System.arraycopy(buff, index, saveBuff, saveLength, nextLength);
								index += nextLength;
								saveLength += nextLength;
								nextLength = 0;
							}else{
								System.arraycopy(buff, index, saveBuff, saveLength, len-index);
								nextLength = nextLength - len + index;
								saveLength += len - index;
								startOver = 3;
								return;
							}
						}
						
						String fileName = new String(saveBuff, 0, saveLength+nextLength);
						File file = new File(parentPath + fileName);
						if(!file.exists()){
							file.createNewFile();
							out = new FileOutputStream(file);
						}
						copyFile(buff, saveBuff, index, len, parentPath);
					}else{
						startOver = 0;
						if(len - index >= remainFileLength){
							out.write(buff, index, (int)remainFileLength);
							out.close();
							index += (int)remainFileLength;
							remainFileLength = 0L;
							copyFile(buff, saveBuff, index, len, parentPath);
						}else{
							startOver = 3;
							remainFileLength = remainFileLength - len + index;
							out.write(buff, index, len-index);
						}
					}
				}
			}
			
			 private  int bytesToInt(byte[] buf,int off){  
			        int i=0;  
			        i=i|((buf[off]&255)<<24);  
			        i=i|((buf[off+1]&255)<<16);  
			        i=i|((buf[off+2]&255)<<8);  
			        i=i|(buf[off+3]&255);  
			        return i;  
			    }  
			      
			    private  long bytesToLong(byte[] buf,int off){ 
			        long i=0;  
			        i=i|(((long)buf[off]&255)<<56)  
			        |(((long)buf[off+1]&255)<<48)  
			        |(((long)buf[off+2]&255)<<40)  
			        |(((long)buf[off+3]&255)<<32)  
			        |(((long)buf[off+4]&255)<<24)  
			        |(((long)buf[off+5]&255)<<16)  
			        |(((long)buf[off+6]&255)<<8)  
			        |((long)buf[off+7]&255);  
			        return i;  
			    }  
	    	
	    }
	public static void main(String []args){
		//云桌面接受文件
//		ServerFinal server = new ServerFinal(10002, "C:\\Users\\shiweigang\\Desktop\\");
//		long sT = System.currentTimeMillis();
//		server.execute();
//		System.out.println("costed total time is "+(System.currentTimeMillis() - sT));
		//从云桌面下载文件
		try {
			File file = new File("C:\\Users\\shiweigang\\Desktop\\GSRCUMobileBank.zip");
			if(file.isFile()){
				ServerSocket server = new ServerSocket(10003);
				System.out.println("start receive..");
				Socket socket = server.accept();
				OutputStream out = socket.getOutputStream();
				FileInputStream fi = new FileInputStream(file);
				String fileName = file.getName();
				byte fileNameLength = (byte) fileName.getBytes().length;
				out.write(fileNameLength);
				out.write(fileName.getBytes());
				byte bytes [] = new byte[1024];
				int length = 0;
				while((length = fi.read(bytes)) != -1){
					out.write(bytes, 0, length);
				}
				fi.close();
				out.close();
				socket.close();
				server.close();
				System.out.println("file send already!");
			}else{
				System.err.println("read file exception");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

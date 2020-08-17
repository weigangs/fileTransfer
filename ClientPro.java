package fileTransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ClientPro {
	
	private ExecutorService threadPool;
	
	private  String IP = "";
	
	private int PORT = 0;
	
	private String FILE_PATH = "";//"C:\\Users\\59205\\Desktop\\as.txt";
	
	public ClientPro(String ip, int port, String filePath){
		threadPool = Executors.newFixedThreadPool(30);
		this.IP = ip;
		this.PORT = port;
		this.FILE_PATH = filePath;
	}
	
	public void execute(){
		if(!FILE_PATH.endsWith(File.separator)){
			FILE_PATH += File.separator;
		}
		File file = new File(FILE_PATH);
		String parent = file.getParent();
		long sT = System.currentTimeMillis();
		try{
			if(parent == null){
				File[] files = file.listFiles();
				for(File fileItem : files){
					copyFile(fileItem, parent);
				}
			}else{
				copyFile(file, parent);
			}
			threadPool.shutdown();
			System.err.println("waiting threads...");
			if(threadPool.awaitTermination(180, TimeUnit.SECONDS)){
				System.out.println("file has been sent!");
				System.err.println("costed total time is:"+(System.currentTimeMillis()-sT));
				System.exit(0);
			}
		}catch(Exception e){
			System.err.print(e);
			System.exit(0);
		}
	}
	
	public  void copyFile(File file, String parentPath){
		String fullPathName = file.getAbsolutePath();
		String fileName = fullPathName.replace(parentPath, "");
		if(file.isDirectory()){
			threadPool.execute(new FileTransHandler(file, fileName));
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(File fileItem : file.listFiles()){
				copyFile(fileItem, parentPath);
			}
		}else{
			threadPool.execute(new FileTransHandler(file, fileName));
		}
	}

	
	class FileTransHandler implements Runnable{
		
		private Socket socket;
		
		private OutputStream out;
		
		private  FileInputStream in = null;
		
		private  byte buff [] = new byte[5094];
		
		private File file = null;
		
		private String fileName = null;
		
		public FileTransHandler(File file, String fileName){
			try {
				socket = new Socket(IP, PORT);
				out = socket.getOutputStream();
				this.file = file;
				this.fileName = fileName;
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
				if(file.isDirectory()){
					out.write(new byte[]{(byte)0});//0 directory 1 file 2 data
					int fNLength = fileName.getBytes().length;
					out.write(intToBytes(fNLength));
					out.write(fileName.getBytes());
					out.flush();
					out.close();
					socket.close();
				}else{
					out.write(new byte[]{(byte)1});
					int fNLength = fileName.getBytes().length;
					out.write(intToBytes(fNLength));
					out.write(fileName.getBytes());
					out.flush();
					out.write(new byte[]{(byte)2});
					long fileLength = file.length();
					out.write(longToBytes(fileLength));
					out.flush();
					in = new FileInputStream(file);
					int length = 0;
					while((length = in.read(buff)) != -1){
						out.write(buff, 0, length);
						out.flush();
					}
					in.close();
					out.close();
					socket.close();
				}
			}catch(IOException e){
				try {
					if(out != null)out.close();
					if(in != null)in.close();
					if(socket != null)socket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
			
		}
		
		
		
		

		public  byte [] intToBytes(int num){
			byte [] bytes = new byte[4];
			bytes[0] = (byte)(num >>> 24);
			bytes[1] = (byte)(num >>> 16);
			bytes[2] = (byte)(num >>> 8);
			bytes[3] = (byte)num;
			return bytes;
		}
		
		public  byte [] longToBytes(long num){
			byte [] bytes = new byte[8];
			bytes[0] = (byte)(num >>> 56);
			bytes[1] = (byte)(num >>> 48);
			bytes[2] = (byte)(num >>> 40);
			bytes[3] = (byte)(num >>> 32);
			bytes[4] = (byte)(num >>> 24);
			bytes[5] = (byte)(num >>> 16);
			bytes[6] = (byte)(num >>> 8);
			bytes[7] = (byte)num;
			return bytes;
		}
		
		
	}
	
	//C:\\Users\\59205\\Desktop\\1.2.69
	//D:\\soft\\feiq\\Recv Files\\
	public static void main(String []args){
		//向云桌面发送文件
//		FileTransferFinal fileTrans = new FileTransferFinal("198.198.207.68", 10002, 
//				"E:\\GSRCUMobileBank.zip");
//		fileTrans.execute();
		//下载云桌面文件
		try {
			Socket socket = new Socket("198.198.207.68", 10003);
			byte [] bytes = new byte[10240];
			InputStream in = socket.getInputStream();
			byte length [] = new byte[1];
			in.read(length);
        	byte fileName [] = new byte[length[0]];
        	in.read(fileName);
        	String fileNameString = new String(fileName);
        	File file = new File("C:\\Users\\59205\\Desktop\\"+fileNameString);
			if(!file.exists()){
				file.createNewFile();
			}else{
				file.delete();
				file.createNewFile();
			}
        	FileOutputStream out = new FileOutputStream(file);
			int lengths = 0;
			while((lengths = in.read(bytes)) != -1){
				out.write(bytes,0,lengths);
			}
			out.close();
			socket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
}

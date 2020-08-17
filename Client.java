package fileTransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
	
	public final static String IP = "198.198.207.68";
	public final static int PORT = 10002;
	
	public final static String IP2 = "localhost";
	public final static int PORT2 = 8000;
	
	public final static String TEXT_FILE_PATH = "C:\\Users\\59205\\Desktop\\text.txt";
	
	public final static String FILE_PATH = "C:\\Users\\59205\\Desktop\\FileTransferClient.java";

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Socket socket = new Socket(IP, PORT);
		OutputStream os = socket.getOutputStream();
		byte type = 1;//0 text  1 file
		os.write(type);
		if(0 == type){
			sendText(TEXT_FILE_PATH, os);
		}else if(1 == type){
			sendFile(FILE_PATH, os);
		}
	}
	
	public static void sendText(String filePath, OutputStream out) throws IOException{
		File file = new File(filePath);
		if(file.isFile()){
			FileInputStream fi = new FileInputStream(file);
			byte bytes [] = new byte[1024];
			int length = 0;
			while((length = fi.read(bytes)) != -1){
				out.write(bytes, 0, length);
			}
			fi.close();
			out.close();
			System.out.println("text send already!");
		}else{
			System.err.println("read file exception");
			out.close();
		}
	}
	
	public static void sendFile(String filePath, OutputStream out) throws IOException{
		File file = new File(filePath);
		if(file.isFile()){
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
			System.out.println("file send already!");
		}else{
			System.err.println("read file exception");
			out.close();
		}
	}
	
	public static void sendDir(String filePath, OutputStream out) throws IOException{
		
	}

}

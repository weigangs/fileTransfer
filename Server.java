package fileTransfer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	public final static int PORT = 10002;
	
	//receive directory path
	public final static String DIR = "C:\\Users\\shiweigang\\Desktop\\";

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ServerSocket server = new ServerSocket(PORT);
		while(true){
			System.out.println("waiting for connection....");
			Socket socket = server.accept();
			InputStream is = socket.getInputStream();
			int type = is.read();
			if(type == 0){//文本
				receiveText(is);
			}else if(type == 1){//文件
				receiveFile(is);
			}else{
				System.err.println("error data format!");
				is.close();
			}
		}
	}
	
	public static void receiveText(InputStream is) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuffer sb = new StringBuffer();
		String temp = null;
		while((temp = br.readLine()) != null){
			sb.append(temp);
		}
		br.close();
		System.out.println("=============TEXT AREA===============");
		System.out.println(sb);
		System.out.println("=====================================");
	}
	
	public static void receiveFile(InputStream is) throws IOException{
		int fileNameL = is.read();
		if(fileNameL == -1){
			System.err.println("empty data packet");
			return;
		}
		byte fileNameB [] = new byte[fileNameL];
		is.read(fileNameB);
		String fileName = new String(fileNameB);
		File file = new File(DIR +fileName);
		StringBuffer temp = null;
		if(file.exists()){
			int index = fileName.length();
			if(fileName.contains(".")){
				index = fileName.indexOf(".");
			}
			for(int i = 0; true; i++){
				temp = new StringBuffer(fileName);
				temp.insert(index, "("+i+")");
				file = new File(DIR + temp.toString());
				if(!file.exists()){
					break;
				}
			}
		}
		FileOutputStream fo = new FileOutputStream(file);
		byte bytes [] = new byte[1024];
		int length = 0;
		while((length = is.read(bytes)) != -1){
			fo.write(bytes, 0, length);
		}
		is.close();
		fo.close();
		System.out.println("file received successfully");
	}
}

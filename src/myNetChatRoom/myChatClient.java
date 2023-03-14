package myNetChatRoom;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class myChatClient {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 9900);
        //启动发送消息线程
        new sendMsgThread(socket).start();
        //启动接收消息线程
        new reciveMsgThread(socket).start();
    }

}
//专门用来发送消息
class sendMsgThread extends Thread{
    public Socket socket;

    public sendMsgThread(Socket socket){
        this.socket = socket;
    }
    public Scanner scan = new Scanner(System.in);
    @Override
    public void run(){
        while(true){
            try {
                System.out.println("群聊格式[直接发言]");
                System.out.println("私聊格式[port-聊天内容]");
                System.out.println("请输入要发送的消息内容：  ");
                String msg= scan.next();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"utf-8"));
                bufferedWriter.write(msg+"\n");
                bufferedWriter.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
//专门用来接收消息
class reciveMsgThread extends Thread{
    public Socket socket;

    public reciveMsgThread(Socket socket){
        this.socket = socket;
    }
    @Override
    public void run(){
        while(true){
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf-8"));
                System.out.println(bufferedReader.readLine());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

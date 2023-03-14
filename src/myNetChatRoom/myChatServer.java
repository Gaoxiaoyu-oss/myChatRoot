package myNetChatRoom;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class myChatServer {

    private ServerSocket serverSocket;

    private ArrayList<Socket> clientSockets ;

    public myChatServer(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
        //提示信息：服务器已经启动了
        System.out.println("聊天室服务器---端口号：[" + serverSocket.getLocalPort() + "]---启动成功...");
        clientSockets = new ArrayList<>();
    }

    // 当有新的client进入聊天室时通知所有其他client
    public void bcastMsg(Socket sc) throws IOException {
        for (Socket s : clientSockets) {
            if(s.getPort() == sc.getPort()){
                continue;
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(),"utf-8"));
            bufferedWriter.write("新的用户------["+sc.getPort()+"]"+"  进入了聊天室"+"\n");
            bufferedWriter.flush();
        }
    }

    // 启动服务
    public void startServer() throws IOException {
        while(true){
            //接收client的连接并创建该连接的socket,存入ArrayList<Socket>
            Socket clientSocket = serverSocket.accept();
            clientSockets.add(clientSocket);
            // 通知其他client有新的client进到聊天室
            bcastMsg(clientSocket);
            // 为该刚进来的client创建一个线程进行处理
            new oneClientProcessThread(serverSocket,clientSocket,clientSockets).start();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket sSocket = new ServerSocket(9900);
        myChatServer server = new myChatServer(sSocket);
        server.startServer();
    }


}

class oneClientProcessThread extends Thread{
    private ArrayList<Socket> clientSockets;
    private ServerSocket serverSocket;
    private Socket socket;

    public oneClientProcessThread(ServerSocket serverSocket,Socket socket,ArrayList<Socket> clientSockets){
        this.serverSocket = serverSocket;
        this.socket = socket;
        this.clientSockets = clientSockets;
    }

    @Override
    public void run(){
        while(true){
            try {
                //读取某client发送过来的消息
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf-8"));
                String recivedmsg = reader.readLine();
                //根据收到的消息判断是私聊还是群聊
                if( recivedmsg.contains("-")  ){
                    //私聊

                    //从ArrayList<Socket>中找到消息中指定的端口号,并向该指定的soket发送消息
                    clientSockets.stream().filter( (c) -> c.getPort() == Integer.parseInt(recivedmsg.split("-")[0]) ).forEach(s ->{
                        try {
                            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(),"utf-8"));
                            bufferedWriter.write("来自 "+socket.getPort()+" 的消息: "+recivedmsg+"\n");
                            bufferedWriter.flush();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

                }else{
                    //群聊

                    //向全部的其他client广播消息
                    for (Socket s : clientSockets) {
                        //跳过自己
                        if(s.getPort() == this.socket.getPort()){
                            continue;
                        }
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(),"utf-8"));
                        bufferedWriter.write("来自 "+socket.getPort()+" 的消息: "+recivedmsg+"\n");
                        bufferedWriter.flush();
                    }

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

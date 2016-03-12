package Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;


/**
 * Created by zsc on 2015/3/9.
 */
public class ChatServer {

    ServerFrame serverFrame = new ServerFrame();
//    Frame f = new Frame();
    ServerSocket serverSocket = null;
    Socket socket = null;
    boolean bconnected = false;
    Map<String,String> clientInfo= new HashMap<>();
    java.util.List<ReceiveMsg> clients = new ArrayList<ReceiveMsg>();
    String DELIMITER ="\f";
    String SEPARATOR ="\r";


//    Button login = new Button("启动");
//    Label chat = new Label("记录");
//    java.awt.List chatList = new java.awt.List(20,false);
//    Label online = new Label("在线用户列表");
//    TextField onlienCount = new TextField("在线人数");
//    java.awt.List clientList = new java.awt.List(20,false);

    public static void main(String[] args) {
        new ChatServer().init();
    }

    private void init(){
        serverFrame.init();
    }
    //启动服务端
    class Server implements Runnable {
        boolean start = false;
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
                start = true;
            } catch (BindException e) {
                System.out.println("端口使用中");
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                while (start) {
                    socket = serverSocket.accept();
                    ReceiveMsg c = new ReceiveMsg(socket);
                    clients.add(c);

                    System.out.println("a client connected!");
                    new Thread(c).start();
                    //dis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //接受客户端连接
    class ReceiveMsg implements Runnable{
        private Socket s;
        private DataInputStream disWithClient = null;
        private DataOutputStream dosWithClient = null;
        String name = "";
        String port = "";
        String str = "";
        String peer = "";


        ReceiveMsg(Socket s ){
            this.s=s;
            try {
                disWithClient = new DataInputStream(s.getInputStream());
                dosWithClient = new DataOutputStream(s.getOutputStream());
                bconnected = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //发送信息
        private void send(String str) throws IOException{
            try {
                dosWithClient.writeUTF(str);
            } catch (IOException e) {
                clients.remove(this);
                e.printStackTrace();
            }
        }
        //发送信息和名字与端口号
        private void sendAll(String data,String name_and_port) throws IOException{
            for(int i=0;i<clients.size();i++){
                ReceiveMsg c = clients.get(i);
                c.send(data);
                c.send(name_and_port);
            }
            serverFrame.getOnlienCount().setText("在线人数" + ": " + serverFrame.getClientList().getItemCount());
        }

        private String nameAndPort(java.awt.List clientList, Map<String,String> clientInfo) {
            StringBuilder name_port = new StringBuilder();
            for(int k =0;k<clientList.getItemCount();k++){
                name_port =name_port.append(clientList.getItem(k)).append(SEPARATOR).append(clientInfo.get(clientList.getItem(k))).append(DELIMITER);
            }
            String name_and_port=name_port.deleteCharAt(name_port.length()-1).toString();
            return name_and_port;
        }

        public void run(){
            try {
                while(bconnected) {
                    String data_from_client = disWithClient.readUTF();
//                    StringBuilder name_port = new StringBuilder();
                    java.util.List<String> data_from_client_split = Arrays.asList(data_from_client.split(DELIMITER));
                    name = data_from_client_split.get(0);
                    port = data_from_client_split.get(1);
                    clientInfo.put(name,port);
                    boolean flag = false;
                    System.out.println("DDDDDD" + clientInfo.size());

                    for(int j =0;j<serverFrame.getClientList().getItemCount();j++){
                        if(serverFrame.getClientList().getItem(j).equals(name)) flag = true;
                    }
                    if(!flag) {
                        serverFrame.getClientList().add(name);
                        serverFrame.getChatList().add(name + "已上线");
                    }
//                    for(int k =0;k<clientList.getItemCount();k++){
//                        name_port =name_port.append(clientList.getItem(k)).append(SEPARATOR).append(clientInfo.get(clientList.getItem(k))).append(DELIMITER);
//                    }
//                    String name_and_port=name_port.deleteCharAt(name_port.length()-1).toString();
                    str = data_from_client_split.get(2);
                    peer = data_from_client_split.get(3);
                    System.out.println(str + "aaaaa");
                    sendAll(data_from_client, nameAndPort(serverFrame.getClientList(), clientInfo));
                    System.out.println("no1");
                }
            }
            catch (SocketException e){
                clients.remove(this);
                serverFrame.getClientList().remove(this.name);
                serverFrame.getChatList().add(name + "已下线");
                //StringBuilder name_port = new StringBuilder();
                String without_str = name + DELIMITER + port + DELIMITER + "" + DELIMITER + peer;
//                for(int k =0;k<clientList.getItemCount();k++){
//                    name_port =name_port.append(clientList.getItem(k)).append(SEPARATOR).append(clientInfo.get(clientList.getItem(k))).append(DELIMITER);
//                }
                if(serverFrame.getClientList().getItemCount()==0){
                    try {
                        sendAll(without_str, " ");
                    } catch (IOException e1) {
                        System.out.println("no Client");
                    }
                }
                else{
                    //String name_and_port=nameAndPort(clientList, clientInfo);
                    try {
                        sendAll(without_str, nameAndPort(serverFrame.getClientList(), clientInfo));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                System.out.println("Client closed0");
            }
            catch (EOFException e){
                clients.remove(this);
                serverFrame.getClientList().remove(this.name);
                System.out.println("Client closed1");
            }catch (IOException e){
                System.out.println("Client closed2");
            }finally {
                try {
                    if(disWithClient !=null) disWithClient.close();
                    if(s !=null) s.close();
                    if(dosWithClient !=null) dosWithClient.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }



}

class UserClient{
    private String name = "";
    private String port = "";
    private String str = "";
    private String peer = "";
    private Socket socket = null;
    private DataInputStream disWithClient;
    private DataOutputStream dosWithClient;
    private boolean bconnected = true;
    private Map<String,String> clientInfo= new HashMap<>();
    private java.util.List<ChatServer.ReceiveMsg> clients = new ArrayList<ChatServer.ReceiveMsg>();
    private String DELIMITER ="\f";
    private String SEPARATOR ="\r";
    public UserClient(Socket socket){
        this.socket = socket;
        try {
            disWithClient = new DataInputStream(socket.getInputStream());
            dosWithClient = new DataOutputStream(socket.getOutputStream());
            bconnected = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMsg(String str) throws IOException{
        try {
            dosWithClient.writeUTF(str);
        } catch (IOException e) {
            clients.remove(this);
            e.printStackTrace();
        }
    }
}

class User{
    private Socket socket = null;
}

class ServerFrame{
    Frame f = new Frame();

    private static java.awt.List chatList = new java.awt.List(20,false);
    private static TextField onlineCount = new TextField("在线人数");
    private static java.awt.List clientList = new java.awt.List(20,false);

    private Button login = new Button("启动");
    private Label chat = new Label("记录");
    private Label online = new Label("在线用户列表");

    public java.awt.List getChatList(){
        return chatList;
    }

    public TextField getOnlienCount(){
        return onlineCount;
    }

    public java.awt.List getClientList(){
        return clientList;
    }

    public void init(){
        f.setTitle("服务端");
        //用户
        Box client = Box.createHorizontalBox();
//        client.add(clientname);
//        client.add(Box.createHorizontalGlue());
//        client.add(clienttf);
        client.add(Box.createHorizontalStrut(20));
        client.add(login);
        client.add(Box.createHorizontalGlue());
        //client.add(cancel);

        //左边
        Box left = Box.createVerticalBox();
        left.add(Box.createVerticalStrut(10));
        left.add(client);
        left.add(chat);
        //left.add(Box.createVerticalStrut(5));
        left.add(chatList);
        left.add(Box.createVerticalStrut(5));
//        left.add(content);
//        left.add(bottom);
        //右边
        Box right = Box.createVerticalBox();
        right.add(Box.createVerticalStrut(5));
        right.add(online);
        right.add(Box.createVerticalStrut(0));
        right.add(onlineCount);
        right.add(Box.createVerticalStrut(5));
        right.add(clientList);
        right.add(Box.createVerticalStrut(5));
        //合并
        Box all = Box.createHorizontalBox();
        all.add(left);
        all.add(Box.createHorizontalStrut(10));
        all.add(right);
        f.add(all);
        f.pack();

        f.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                }
        );
        login.addActionListener(new loginListener());
        f.setVisible(true);

    }
    private class loginListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            new Thread(new ChatServer().new Server()).start();
            login.setEnabled(false);
        }
    }
}

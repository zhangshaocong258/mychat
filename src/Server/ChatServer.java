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

    private ServerFrame serverFrame = new ServerFrame();
    private UserClientList userClientList = new UserClientList();

//    Frame f = new Frame();
    Socket socket = null;
    boolean bconnected = false;
//    Map<String,String> clientInfo= new HashMap<>();
//    java.util.List<UserClient> clients = new ArrayList<UserClient>();

//    Button login = new Button("启动");
//    Label chat = new Label("记录");
//    java.awt.List chatList = new java.awt.List(20,false);
//    Label online = new Label("在线用户列表");
//    TextField onlienCount = new TextField("在线人数");
//    java.awt.List clientList = new java.awt.List(20,false);

    public static void main(String[] args) {
        new ChatServer().init();
    }

    private void init() {
        serverFrame.init();
    }

    //启动服务端
    class Server implements Runnable {
        private ServerSocket serverSocket = null;
        private UserClient userClient;
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
                    userClient = new UserClient(socket);
                    ReceiveMsg client = new ReceiveMsg(userClient);
                    userClientList.addClients(userClient);
                    System.out.println("a client connected!");
                    new Thread(client).start();
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

    //接受客户端信息
    class ReceiveMsg implements Runnable {
        private UserClient userClient;
        private UserClientMsg userClientMsg;
        private Map<String, String> clientInfomation = new HashMap<String, String>();
        private String name_port;
        private String name = "";
        private String port = "";
//        private String str = "";
        private String peer = "";

        ReceiveMsg(UserClient userClient) {
            this.userClient = userClient;
            bconnected = true;
        }
        //发送信息
//        private void send(String str) throws IOException{
//            try {
//                dosWithClient.writeUTF(str);
//            } catch (IOException e) {
//                clients.remove(this);
//                e.printStackTrace();
//            }
//        }
        //发送信息和名字与端口号
//        private void sendAll(String data,String name_and_port) throws IOException{
//            for(int i=0;i<clients.size();i++){
//                UserClient c = clients.get(i);
//                c.sendData(data);
//                c.sendData(name_and_port);
//            }
//        }

//        private String nameAndPort(Map<String,String> clientInfo) {
//            StringBuilder name_port = new StringBuilder();
//            for(Map.Entry<String,String> entry : clientInfo.entrySet()){
//                name_port =name_port.append(entry.getKey()).append(SEPARATOR).append(entry.getValue()).append(DELIMITER);
//            }
//            String name_and_port=name_port.deleteCharAt(name_port.length()-1).toString();
//            return name_and_port;
//        }

        public void run() {
            try {
                while (bconnected) {
                    String data_from_client = userClient.receiveData();
//                    StringBuilder name_port = new StringBuilder();
//                    java.util.List<String> data_from_client_split = Arrays.asList(data_from_client.split(DELIMITER));
//                    name = data_from_client_split.get(0);
//                    port = data_from_client_split.get(1);
//                    clientInfo.put(name,port);
                    userClientMsg = new UserClientMsg(data_from_client);
                    clientInfomation = userClientMsg.putClientInfo();//
                    name = userClientMsg.getName();
                    port = userClientMsg.getPort();
                    peer = userClientMsg.getPeer();

                    name_port = userClientMsg.buildNamePort(clientInfomation);
                    boolean flag = false;
                    System.out.println("用户数" + userClientMsg.getClientInfo().size());

                    for (int j = 0; j < serverFrame.getClientList().getItemCount(); j++) {
                        if (serverFrame.getClientList().getItem(j).equals(name)) flag = true;
                    }
                    if (!flag) {
                        serverFrame.getClientList().add(name);
                        serverFrame.getChatList().add(name + "已上线");
                    }
//                    for(int k =0;k<clientList.getItemCount();k++){
//                        name_port =name_port.append(clientList.getItem(k)).append(SEPARATOR).append(clientInfo.get(clientList.getItem(k))).append(DELIMITER);
//                    }
//                    String name_and_port=name_port.deleteCharAt(name_port.length()-1).toString();
//                    for(Map.Entry<String,String> entry : clientInfo.entrySet()){
//                        System.out.println("信息list" + entry.getKey() + "    " + entry.getValue());
//                    }
//                    for(int k =0;k<serverFrame.getClientList().getItemCount();k++){
//                        System.out.println("信息list" + serverFrame.getClientList().getItem(k));
//                    }
                    userClientList.sendMsg(data_from_client, name_port);
                    serverFrame.getOnlienCount().setText("在线人数" + ": " + serverFrame.getClientList().getItemCount());
                    System.out.println("no1");
                }
            } catch (SocketException e) {
                userClientList.removeClients(this.userClient);//List删除下线用户
                serverFrame.getClientList().remove(this.name);//Frame中删除下线用户
                serverFrame.getOnlienCount().setText("在线人数" + ": " + serverFrame.getClientList().getItemCount());//在线人数减一
                clientInfomation = userClientMsg.removeClientInfo(this.name);//Map中删除name_port，向其他用户发送信息
                name_port = userClientMsg.buildNamePort(clientInfomation);//建立name_port,发送
                serverFrame.getChatList().add(name + "已下线");
                String without_str = userClientMsg.buildMsg(name, port, "", peer);
//                for(int k =0;k<clientList.getItemCount();k++){
//                    name_port =name_port.append(clientList.getItem(k)).append(SEPARATOR).append(clientInfo.get(clientList.getItem(k))).append(DELIMITER);
//                }
//                if(serverFrame.getClientList().getItemCount()==0){
//                    try {
//                        sendAll(without_str, " ");
//                    } catch (IOException e1) {
//                        System.out.println("no Client");
//                    }
//                }
                if (serverFrame.getClientList().getItemCount() != 0) {
                    try {
                        userClientList.sendMsg(without_str, name_port);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                System.out.println("Client closed0");
            } catch (EOFException e) {
                userClientList.removeClients(this.userClient);
                serverFrame.getClientList().remove(this.name);
                System.out.println("Client closed1");
            } catch (IOException e) {
                System.out.println("Client closed2");
            } finally {
                try {
                    userClient.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }


}

//启动socket连接，定义发送和接受信息
class UserClient {
    private Socket socket = null;
    private DataInputStream disWithClient;
    private DataOutputStream dosWithClient;

    public UserClient(Socket socket) {
        this.socket = socket;
        try {
            disWithClient = new DataInputStream(socket.getInputStream());
            dosWithClient = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        try {
            if (disWithClient != null) disWithClient.close();
            if (socket != null) socket.close();
            if (dosWithClient != null) dosWithClient.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void sendData(String str) throws IOException {
        dosWithClient.writeUTF(str);
        dosWithClient.flush();
    }

    public String receiveData() throws IOException {
        String msg = disWithClient.readUTF();
        return msg;
    }
}

//用户List的封装
class UserClientList {
    private static java.util.List<UserClient> clients = new ArrayList<UserClient>();

    public void addClients(UserClient userClient) {
        clients.add(userClient);
    }

    public void removeClients(UserClient userClient) {
        clients.remove(userClient);
    }

    public java.util.List<UserClient> getClients() {
        return clients;
    }

    public void sendMsg(String data, String NamePort) throws IOException {
        for (int i = 0; i < clients.size(); i++) {
            UserClient client = clients.get(i);
            try {
                client.sendData(data);
                client.sendData(NamePort);
            } catch (IOException e) {
                removeClients(client);
                e.printStackTrace();
            }
        }
    }
}

//用户信息类
class UserClientMsg {
    private String name = "";
    private String port = "";
    private String str = "";
    private String peer = "";
    private static Map<String, String> clientInfo = new HashMap<String, String>();
    private String DELIMITER = "\f";
    private String SEPARATOR = "\r";

    UserClientMsg(String data_from_client) {
        java.util.List<String> data_from_client_split = Arrays.asList(data_from_client.split(DELIMITER));
        this.name = data_from_client_split.get(0);
        this.port = data_from_client_split.get(1);
        this.str = data_from_client_split.get(2);
        this.peer = data_from_client_split.get(3);
    }

    public String getName() {
        return name;
    }

    public String getPort() {
        return port;
    }

    public String getPeer() {
        return peer;
    }

    public Map<String, String> putClientInfo() {
        clientInfo.put(getName(), getPort());
        return clientInfo;
    }

    public Map<String, String> removeClientInfo(String name) {
        clientInfo.remove(name);
        return clientInfo;
    }

    public Map<String, String> getClientInfo() {
        return clientInfo;
    }

    public String buildNamePort(Map<String, String> clientInfo) {
        StringBuilder name_port = new StringBuilder();
        if (clientInfo.size() > 0) {
            for (Map.Entry<String, String> entry : clientInfo.entrySet()) {
                name_port = name_port.append(entry.getKey()).append(SEPARATOR).append(entry.getValue()).append(DELIMITER);
            }
            String NamePort = name_port.deleteCharAt(name_port.length() - 1).toString();
            return NamePort;
        } else return null;
    }

    public String buildMsg(String name, String port, String str, String peer) {
        String buildMsg = name + DELIMITER + port + DELIMITER + str + DELIMITER + peer;
        return buildMsg;
    }
}

//界面显示
class ServerFrame {
    private Frame f = new Frame();

    private static java.awt.List chatList = new java.awt.List(20, false);
    private static TextField onlineCount = new TextField("在线人数");
    private static java.awt.List clientList = new java.awt.List(20, false);

    private Button login = new Button("启动");
    private Label chat = new Label("记录");
    private Label online = new Label("在线用户列表");

    public java.awt.List getChatList() {
        return chatList;
    }

    public TextField getOnlienCount() {
        return onlineCount;
    }

    public java.awt.List getClientList() {
        return clientList;
    }

    public void init() {
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

package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.List;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;


/**
 * Created by zsc on 2015/3/9.
 */
public class ChatClient {
    ConnectServer connectServer = new ConnectServer();
    ClientMsg clientMsg = new ClientMsg();
    ConnectPeerClient connectPeerClient = new ConnectPeerClient();
    Frame f = new Frame();
    Socket s = null;
    Socket socketWithPeer = null;
    ServerSocket clientServerSocket = null;
//    DataOutputStream dosWithServer = null;
//    DataInputStream disWithServer = null;
    DataOutputStream peerDos = null;
    DataInputStream peerDis = null;
    Map<String, String> clientInfo = new HashMap<>();
    String name = "";
    String port = "";
    String str = "";
    String peer = "群聊";
    String all = "";
    String DELIMITER = "\f";
    String SEPARATOR = "\r";
//    boolean bconnected = false;
    boolean cClient = false;

    Label clientLabel = new Label("用户名");
    TextField clientName = new TextField(10);
    Button login = new Button("登录");

    Label chatLabel = new Label("聊天记录");
    TextArea ta = new TextArea(25, 20);
    TextArea content = new TextArea(2, 20);

    Label onlineLabel = new Label("在线好友列表");
    TextField onlineCount = new TextField("在线人数");
    List clientList = new List(30, false);

    Button ok = new Button("发送");
    Button clear = new Button("清除");

    public static void main(String[] args) {
        new ChatClient().init();
    }

    public void init() {
        f.setTitle("客户端");

        //用户
        Box client = Box.createHorizontalBox();
        client.add(clientLabel);
        //client.add(Box.createHorizontalGlue());
        client.add(clientName);
        client.add(Box.createHorizontalGlue());
        client.add(login);
        client.add(Box.createHorizontalGlue());
        //client.add(cancel);

        //发送清除
        Box bottom = Box.createHorizontalBox();
        bottom.add(ok);
        bottom.add(Box.createHorizontalGlue());
        bottom.add(clear);

        //左边
        Box left = Box.createVerticalBox();
        left.add(Box.createVerticalStrut(10));
        left.add(client);
        left.add(chatLabel);
        left.add(ta);
        left.add(Box.createVerticalStrut(5));
        left.add(content);
        left.add(bottom);
        left.add(Box.createVerticalStrut(5));
        //右边
        Box right = Box.createVerticalBox();
        right.add(Box.createVerticalStrut(10));
        right.add(onlineLabel);
        right.add(Box.createVerticalStrut(5));
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
        clientList.add("群聊");
        f.pack();

        f.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        //disconnect();
                        System.exit(0);
                    }
                }
        );
        ok.addActionListener(new okListener());
        clear.addActionListener(new clearListener());
        login.addActionListener(new clientloginListener());
        clientList.addItemListener(new peerListener());
        f.setVisible(true);
    }

    //客户端服务
    class ClientServer implements Runnable {
        private PeerClient peerClient;
        boolean start = false;

        public void run() {
            try {
                clientServerSocket = new ServerSocket(connectServer.clientSocket.getLocalPort() + 1);
                System.out.println(connectServer.clientSocket.getLocalPort());
                start = true;
            } catch (BindException e) {
                System.out.println("端口使用中");
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                while (start) {
                    Socket socket = clientServerSocket.accept();
                    peerClient = new PeerClient(socket);
                    ReceivePeerMsg receivePeerMsg = new ReceivePeerMsg(peerClient);
                    System.out.println("a client connected!");
                    new Thread(receivePeerMsg).start();
                    //dis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //客户端接收客户端
    class ReceivePeerMsg implements Runnable {
        private PeerClient peerClient;
        public ReceivePeerMsg(PeerClient peerClient) {
            this.peerClient = peerClient;
                connectPeerClient.cClient = true;
        }

        public void run() {
            try {
                while (connectPeerClient.cClient) {
                    String str = clientMsg.receiveData(peerClient.disWithPeer);
//                    String str = peerClient.disWithPeer.readUTF();
                    ta.setText(ta.getText() + str + "\n");
                    System.out.println(str);
                }
            } catch (SocketException e) {
                connectPeerClient.cClient = false;
                System.out.println("Client closed0");
            } catch (EOFException e) {
                System.out.println("Client closed1");
            } catch (IOException e) {
                System.out.println("Client closed2");

            } finally {
                //关闭要封装一下
                try {
                    peerClient.close();
//                    if (peerClient.disWithPeer != null) peerClient.disWithPeer.close();
//                    if (s != null) s.close();

                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        }
    }

    //客户端连接服务端
//    public void connect() {
//        try {
//            s = new Socket("127.0.0.1", 8888);
//            dosWithServer = new DataOutputStream(s.getOutputStream());
//            disWithServer = new DataInputStream(s.getInputStream());
//
//            name = clientName.getText();
//            port = String.valueOf(s.getLocalPort() + 1);
//            peer = name;
//            all = name + DELIMITER + port + DELIMITER + str + DELIMITER + peer;
//            dosWithServer.writeUTF(all);
//
//            System.out.println("connected");
//            bconnected = true;
//        } catch (UnknownHostException e) {
//            System.out.println("sever not start");
//            e.printStackTrace();
//        } catch (IOException e) {
//            System.out.println("sever not start");
//            System.exit(1);
//            e.printStackTrace();
//        }
//    }

//    public void disconnect() {
//        try {
//            dosWithServer.close();
//            s.close();
//            bconnected = false;
//        } catch (IOException e) {
//            System.exit(0);
//        }
//    }

    //客户端连接客户端
//    public void connectpeer(int peerport) {
//        try {
//            socketWithPeer = new Socket("127.0.0.1", peerport);
//            peerDos = new DataOutputStream(socketWithPeer.getOutputStream());
//            cClient = true;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    //客户端断开连接客户端
//    public void disconnectpeer() {
//        connectPeerClient.cClient = false;
//        System.out.println("peer disconnected");
//    }

    //发送信息
    private void SendThread() {
        clientMsg.setStr(clientMsg.getName() + "说：" + content.getText().trim());
        String all = clientMsg.buildMsg(clientMsg.getName(), clientMsg.getPort(),clientMsg.getStr(),clientMsg.getPeer());
        //ta.setText(str);
        content.setText(null);
        try {
            if (!connectPeerClient.cClient) {
                clientMsg.sendData(connectServer.dosWithServer, all);
                connectServer.dosWithServer.flush();
            } else {
                clientMsg.sendData(connectPeerClient.dosWithPeer, clientMsg.getStr());
                System.out.println("发送的信息" + clientMsg.getStr());
                if (!clientMsg.getStr().split("：")[1].equals(""))
                    ta.setText(ta.getText() + clientMsg.getStr() + "\n");
            }
            //dos.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    //接收服务端信息
    private class ReceiveServerMsg implements Runnable {
        String data = null;
        java.util.List<String> data_split = null;
        String name_and_port = null;

        public void run() {
            while (connectServer.bconnected) {
                try {
                    data = clientMsg.receiveData(connectServer.disWithServer);
                    name_and_port = clientMsg.receiveData(connectServer.disWithServer);
                    System.out.println(name_and_port);

                    data_split = Arrays.asList(data.split(DELIMITER));
                    java.util.List<String> listname = Arrays.asList(name_and_port.split(DELIMITER));
                    String rname = data_split.get(3);
                    String rport = data_split.get(1);
                    if (data_split.get(2).equals("")) {
                        clientInfo.clear();
                        clientList.removeAll();
                        clientList.add("群聊");
                        for (int j = 0; j < listname.size(); j++) {
                            clientList.add(listname.get(j).split(SEPARATOR)[0]);
                            clientInfo.put(listname.get(j).split(SEPARATOR)[0], listname.get(j).split(SEPARATOR)[1]);
                        }
                        System.out.println(clientInfo.size());

                        onlineCount.setText("在线人数" + ": " + (clientList.getItemCount() - 1));
                    }

                } catch (SocketException e1) {
                    System.out.println("Server closed");
                    System.exit(0);
                } catch (EOFException e2) {
                    System.exit(0);
                } catch (IOException e) {
                    System.out.println("Server closed");

                }
                String str = data_split.get(2);
                try {
                    //str = b.get(2);
                    System.out.println(str);
                    if (!str.split("：")[1].equals("")) {
                        ta.setText(ta.getText() + str + "\n");
                    }
                } catch (Exception e) {
                    System.out.println("client closed");
                }
            }
        }
    }

    private class okListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            SendThread();
        }
    }

    private class clearListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            content.getText().trim();
            content.setText(null);

        }
    }

    private class clientloginListener implements ActionListener {


        public void actionPerformed(ActionEvent e) {
            login.setEnabled(false);
            clientName.setEnabled(false);
            connectServer.connect();//客户端连接服务端
            clientMsg.setName(clientName.getText());
            clientMsg.setPort(String.valueOf(connectServer.clientSocket.getLocalPort() + 1));
            clientMsg.setPeer(clientMsg.getName());
            String all = clientMsg.buildMsg(clientMsg.getName(), clientMsg.getPort(),"",clientMsg.getPeer());
            try {
                clientMsg.sendData(connectServer.dosWithServer, all);//客户端向服务端发送登录信息
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            new Thread(new ClientServer()).start();//启动客户端作为服务端的服务
            new Thread(new ReceiveServerMsg()).start();//启动接受信息服务
        }
    }

    private class peerListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            System.out.println(clientList.getSelectedItem());
            peer = clientList.getSelectedItem();
            for (Map.Entry<String, String> entry : clientInfo.entrySet()) {
                System.out.println("Map内容  " + entry.getKey() + "    " + entry.getValue());
            }
            if (!peer.equals("群聊")) {
                connectPeerClient.connectpeer(Integer.parseInt(clientInfo.get(peer)));
            } else {
                connectPeerClient.disconnectpeer();
            }
        }
    }
}

class PeerClient{
    private Socket peerSocket = null;//客户端的服务端
//    boolean cClient = false;
    DataInputStream disWithPeer;

    public PeerClient(Socket peerSocket) {
        this.peerSocket = peerSocket;
        try {
            disWithPeer = new DataInputStream(peerSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        try {
            if (disWithPeer != null) disWithPeer.close();
            if (peerSocket != null) peerSocket.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}

class ConnectPeerClient{
    private Socket socketWithPeer = null;//客户端的客户端
    boolean cClient;
    DataOutputStream dosWithPeer;

    //客户端连接客户端
    public void connectpeer(int peerport) {
        try {
            socketWithPeer = new Socket("127.0.0.1", peerport);
            dosWithPeer = new DataOutputStream(socketWithPeer.getOutputStream());
            this.cClient = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnectpeer() {
        this.cClient = false;
        System.out.println("peer disconnected");
    }
}
class ConnectServer{
    Socket clientSocket = null;//Client自己的scoket
    DataOutputStream dosWithServer = null;
    DataInputStream disWithServer = null;
    boolean bconnected = false;

    public void connect() {
        try {
            clientSocket = new Socket("127.0.0.1", 8888);
            dosWithServer = new DataOutputStream(clientSocket.getOutputStream());
            disWithServer = new DataInputStream(clientSocket.getInputStream());
            bconnected = true;
            System.out.println("connected");
        } catch (UnknownHostException e) {
            System.out.println("sever not start");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("sever not start");
            System.exit(1);
            e.printStackTrace();
        }
    }
}

class ClientMsg {
    private String name = "";
    private String port = "";
    private String str = "";
    private String peer = "";
    private static Map<String, String> clientInfo = new HashMap<String, String>();
    private String DELIMITER = "\f";
    private String SEPARATOR = "\r";

//    ClientMsg(String data_from_client) {
//        java.util.List<String> data_from_client_split = Arrays.asList(data_from_client.split(DELIMITER));
//        this.name = data_from_client_split.get(0);
//        this.port = data_from_client_split.get(1);
//        this.str = data_from_client_split.get(2);
//        this.peer = data_from_client_split.get(3);
//    }

    public void setName(String name){
        this.name = name;
    }

    public void setPort(String port){
        this.port = port;
    }

    public void setStr(String str){
        this.str = str;
    }

    public void setPeer(String peer){
        this.peer = peer;
    }

    public String getName() {
        return name;
    }

    public String getPort() {
        return port;
    }

    public String getStr(){
        return str;
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

    public String buildMsg(String name, String port, String str, String peer) {
        String buildMsg = name + DELIMITER + port + DELIMITER + str + DELIMITER + peer;
        return buildMsg;
    }

    public void sendData(DataOutputStream dataOutputStream, String Data) throws IOException{
        dataOutputStream.writeUTF(Data);
    }

    public String receiveData(DataInputStream dataInputStream) throws IOException{
        String msg = dataInputStream.readUTF();
        return msg;
    }
}


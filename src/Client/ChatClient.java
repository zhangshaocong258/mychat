package Client;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
    static boolean listener = false;
    ConnectServer connectServer = new ConnectServer();//客户端连接服务端
    ClientData clientData = new ClientData();//发送用户登录信息，封装了buildMsg、send和receive
    ConnectPeerClient connectPeerClient = new ConnectPeerClient();//客户端作为服务端
    JFrame jFrame = new JFrame();

    JLabel clientLabel = new JLabel("用户名");
    JTextField clientName = new JTextField(10);
    JButton login = new JButton("登录");

    JLabel chatLabel = new JLabel("聊天记录");
    JTextArea chatRecord = new JTextArea(25, 20);
    JTextArea chatBox = new JTextArea(2, 20);

    JLabel onlineLabel = new JLabel("在线好友列表");
    static JTextField onlineCount = new JTextField("在线人数");
    static DefaultListModel listModel = new DefaultListModel();
    JList clientList = new JList(listModel);
    JScrollPane jScrollPane = new JScrollPane(clientList);


    JButton send = new JButton("发送");
    JButton clear = new JButton("清除");

    public static void main(String[] args) {
        new ChatClient().init();
    }

    public void init() {
        jFrame.setTitle("客户端");

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
        bottom.add(send);
        bottom.add(Box.createHorizontalGlue());
        bottom.add(clear);

        //左边
        Box left = Box.createVerticalBox();
        left.add(Box.createVerticalStrut(10));
        left.add(client);
        left.add(chatLabel);
        left.add(chatRecord);
        left.add(Box.createVerticalStrut(5));
        left.add(chatBox);
        left.add(bottom);
        left.add(Box.createVerticalStrut(5));
        //右边
        Box right = Box.createVerticalBox();
        right.add(Box.createVerticalStrut(10));
        right.add(onlineLabel);
        right.add(Box.createVerticalStrut(5));
        right.add(onlineCount);
        right.add(Box.createVerticalStrut(5));
        right.add(jScrollPane);
        right.add(Box.createVerticalStrut(5));
        //合并
        Box all = Box.createHorizontalBox();
        all.add(left);
        all.add(Box.createHorizontalStrut(10));
        all.add(right);
        jFrame.add(all);
        listModel.addElement("群聊");
        jFrame.pack();

        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        send.addActionListener(new sendListener());
        clear.addActionListener(new clearListener());
        login.addActionListener(new clientLoginListener());
        clientList.addListSelectionListener(new p2pListener());
        jFrame.setVisible(true);
    }

    //客户端作为服务端
    class ClientServer implements Runnable {
        private PeerClient peerClient;
        private ServerSocket clientServerSocket = null;
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

    //客户端接收客户端信息
    class ReceivePeerMsg implements Runnable {
        private ReceiveData receiveData;
        private PeerClient peerClient;

        public ReceivePeerMsg(PeerClient peerClient) {
            this.peerClient = peerClient;
            connectPeerClient.setReceiveClientTrue();//接收标志位，有别于发送标志位
            System.out.println("cClient是什么" + connectPeerClient.getReceiveClient());
        }

        public void run() {
            try {
                while (connectPeerClient.getReceiveClient()) {
                    String data = clientData.receiveData(peerClient.disWithPeer);
                    receiveData = new ReceiveData(data);
//                    String str = peerClient.disWithPeer.readUTF();
                    //判断消息是否为空，“：”是否存在，是否是给本人发送信息
                    if ((receiveData.getStr() != "") && !(receiveData.getName().equals(clientData.getName()))) {
                        if (receiveData.getStr().split("：").length != 1)
                            chatRecord.setText(chatRecord.getText() + receiveData.getStr() + "\n");
                    }
                    System.out.println(data);
                }
            } catch (SocketException e) {
                connectPeerClient.setReceiveClientFalse();//接收置位false
                System.out.println("Client closed0");
            } catch (EOFException e) {
                System.out.println("Client closed1");
            } catch (IOException e) {
                System.out.println("Client closed2");

            } finally {
                //关闭要封装一下
                try {
                    peerClient.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        }
    }

    //发送信息
    private void SendThread() {
        clientData.setStr(clientData.getName() + "说：" + chatBox.getText().trim());
        String all = clientData.buildMsg(clientData.getName(), clientData.getPort(), clientData.getStr());
        //ta.setText(str);
        System.out.println("发出去了吗" + clientData.getStr());
        chatBox.setText(null);
        //发送时需要进行各种判断，先判断为空，在判断给服务端还是客户端发送，服务端应该设为默认
        try {
            if (clientData.getStr().length() != clientData.getName().length() + 2) {
                if (!connectPeerClient.getsendCLient()) {
                    clientData.sendData(connectServer.dosWithServer, all);
                    connectServer.dosWithServer.flush();
                } else {
                    clientData.sendData(connectPeerClient.dosWithPeer, all);
                    System.out.println("发送的信息" + clientData.getStr());
                    if (clientData.getStr().length() != clientData.getName().length() + 2)
                        chatRecord.setText(chatRecord.getText() + clientData.getStr() + "\n");
                }
            }
            //dos.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    //接收服务端信息
    private class ReceiveServerMsg implements Runnable {
        ReceiveData receiveData;
        String data = null;
        String name_and_port = null;

        public void run() {
            while (connectServer.connectedWithServer) {
                try {
                    data = clientData.receiveData(connectServer.disWithServer);
                    name_and_port = clientData.receiveData(connectServer.disWithServer);
                    receiveData = new ReceiveData(data, name_and_port);
                    connectPeerClient.setSendClientFalse();//下线后JList全部清空，默认群聊
                } catch (SocketException e1) {
                    System.out.println("Server closed");
                    System.exit(0);
                } catch (EOFException e2) {
                    System.exit(0);
                } catch (IOException e) {
                    System.out.println("Server closed");

                }
                try {
                    if (receiveData.getStr() != "") {
                        if (receiveData.getStr().length() != receiveData.getName().length() + 2) {
                            chatRecord.setText(chatRecord.getText() + receiveData.getStr() + "\n");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("client closed");
                }
            }
        }
    }

    private class sendListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            SendThread();
        }
    }

    private class clearListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            chatBox.getText().trim();
            chatBox.setText(null);

        }
    }

    private class clientLoginListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (clientName.getText().trim().length() > 1) {
                login.setEnabled(false);
                clientName.setEnabled(false);
                connectServer.connect();//客户端连接服务端
                clientData.setName(clientName.getText());
                clientData.setPort(String.valueOf(connectServer.clientSocket.getLocalPort() + 1));
                String all = clientData.buildMsg(clientData.getName(), clientData.getPort(), "");
                try {
                    clientData.sendData(connectServer.dosWithServer, all);//客户端向服务端发送登录信息
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                new Thread(new ClientServer()).start();//启动客户端作为服务端的服务
                new Thread(new ReceiveServerMsg()).start();//启动接受信息服务
            } else {
                clientName.setText(null);
            }
        }
    }

    private class p2pListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            //设置一个listener标志位，下线后JList会有监听，不采取任何动作
            if (!listener) {
                String peer = String.valueOf(clientList.getSelectedValue());
//            for (Map.Entry<String, String> entry : ReceiveData.getClientInfo().entrySet()) {
//                System.out.println("Map内容  " + entry.getKey() + "    " + entry.getValue());
//            }
                System.out.println("peeeeeer" + peer);
                if (peer != null && !peer.equals("群聊")) {
                    connectPeerClient.connectpeer(Integer.parseInt(ReceiveData.getClientInfo().get(peer)));
                } else {
                    connectPeerClient.setSendClientFalse();
                }
            }
        }
    }
}

//客户端的客户端类
class PeerClient {
    private Socket peerSocket = null;//客户端的服务端
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

//客户端的客户端连接客户端的服务端类
class ConnectPeerClient {
    private Socket socketWithPeer = null;//客户端的客户端
    //两个标志位
    private static boolean sendClient;
    private static boolean receiveClient;
    DataOutputStream dosWithPeer;

    //客户端连接客户端
    public void connectpeer(int peerport) {
        try {
            socketWithPeer = new Socket("127.0.0.1", peerport);
            dosWithPeer = new DataOutputStream(socketWithPeer.getOutputStream());
            this.sendClient = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setReceiveClientTrue() {
        this.receiveClient = true;
        System.out.println("peer disconnected");
    }

    public void setReceiveClientFalse() {
        this.receiveClient = false;
        System.out.println("peer disconnected");
    }

    public void setSendClientFalse() {
        this.sendClient = false;
    }

    public boolean getsendCLient() {
        return sendClient;
    }

    public boolean getReceiveClient() {
        return receiveClient;
    }
}

//客户端连接服务端类
class ConnectServer {
    Socket clientSocket = null;//Client自己的scoket
    DataOutputStream dosWithServer = null;
    DataInputStream disWithServer = null;
    static boolean connectedWithServer = false;

    public void connect() {
        try {
            clientSocket = new Socket("127.0.0.1", 8888);
            dosWithServer = new DataOutputStream(clientSocket.getOutputStream());
            disWithServer = new DataInputStream(clientSocket.getInputStream());
            connectedWithServer = true;
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

//接收信息类
class ReceiveData {
    private String name = "";
    private String port = "";
    private String str = "";
    //    private String peer = "";
    private static Map<String, String> clientInfo = new HashMap<String, String>();

    private String DELIMITER = "\f";
    private String SEPARATOR = "\r";

    //客户端之间通信构造器初始化
    ReceiveData(String data_from_client) {
        java.util.List<String> data_from_client_split = Arrays.asList(data_from_client.split(DELIMITER));
        this.name = data_from_client_split.get(0);
        this.port = data_from_client_split.get(1);
        if (data_from_client_split.size() == 3) {
            this.str = data_from_client_split.get(2);
        } else {
            this.str = "";
        }
    }

    //客户端接收服务端信息构造器初始化
    ReceiveData(String data_from_client, String name_and_port) {
        java.util.List<String> data_from_client_split = Arrays.asList(data_from_client.split(DELIMITER));
        java.util.List<String> listname = Arrays.asList(name_and_port.split(DELIMITER));
        this.name = data_from_client_split.get(0);
        this.port = data_from_client_split.get(1);
//        this.str = data_from_client_split.get(2);
//        this.peer = data_from_client_split.get(3);

        if (data_from_client_split.size() == 3) {
            this.str = data_from_client_split.get(2);
        }
        System.out.println("str是什么" + data_from_client.length());
        System.out.println("群发的内容" + str);
        //添加listener标志，最后解除
        if (str.equals("")) {
            ChatClient.listener = true;
            clearClientInfo();
            ChatClient.listModel.removeAllElements();
            ChatClient.listModel.addElement("群聊");
            for (int j = 0; j < listname.size(); j++) {
                ChatClient.listModel.addElement(listname.get(j).split(SEPARATOR)[0]);
                putClientInfo(listname.get(j).split(SEPARATOR)[0], listname.get(j).split(SEPARATOR)[1]);
            }
            System.out.println("列表人数" + clientInfo.size());
            ChatClient.onlineCount.setText("在线人数" + ": " + (ChatClient.listModel.getSize() - 1));
            ChatClient.listener = false;
        }
    }

    public String getName() {
        return name;
    }

    public String getPort() {
        return port;
    }

    public String getStr() {
        return str;
    }

//    public String getPeer() {
//        return peer;
//    }

    public Map<String, String> putClientInfo(String name, String port) {
        clientInfo.put(name, port);
        return clientInfo;
    }

    public void clearClientInfo() {
        clientInfo.clear();
    }

//    public Map<String, String> removeClientInfo(String name) {
//        clientInfo.remove(name);
//        return clientInfo;
//    }

    public static Map<String, String> getClientInfo() {
        return clientInfo;
    }

}

//用户登录信息封装类，包含发送接收信息方法
class ClientData {
    private String name = "";
    private String port = "";
    private String str = "";
    //    private String peer = "";
    private String DELIMITER = "\f";
    private String SEPARATOR = "\r";

    public void setName(String name) {
        this.name = name;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setStr(String str) {
        this.str = str;
    }

//    public void setPeer(String peer){
//        this.peer = peer;
//    }

    public String getName() {
        return name;
    }

    public String getPort() {
        return port;
    }

    public String getStr() {
        return str;
    }

//    public String getPeer() {
//        return peer;
//    }

    public String buildMsg(String name, String port, String str) {
        String buildMsg = name + DELIMITER + port + DELIMITER + str;
        return buildMsg;
    }

    public void sendData(DataOutputStream dataOutputStream, String Data) throws IOException {
        dataOutputStream.writeUTF(Data);
    }

    public String receiveData(DataInputStream dataInputStream) throws IOException {
        String msg = dataInputStream.readUTF();
        return msg;
    }
}

package Client;

import sun.plugin.javascript.JSClassLoader;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;


/**
 * Created by zsc on 2015/3/9.
 */
public class ChatClientFrame {

    private ChatClient chatClient = new ChatClient();

   //不变的组件
    private JFrame jFrame = new JFrame();

    private JLabel clientLabel = new JLabel("用户名");

    private JLabel chatLabel = new JLabel("聊天记录");

    private JLabel onlineLabel = new JLabel("在线好友列表");

    private JButton send = new JButton("发送");
    private JButton clear = new JButton("清除");

    public static void main(String[] args) {
        new ChatClientFrame().init();
    }

    public void init() {
        jFrame.setTitle("客户端");

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));

        JPanel leftPanel = new JPanel();

        JPanel leftTop = new JPanel();
        leftTop.add(Box.createRigidArea(new Dimension(5, 0)));
        leftTop.setLayout(new BoxLayout(leftTop, BoxLayout.X_AXIS));
        leftTop.add(clientLabel);
        leftTop.add(chatClient.getClientName());
        leftTop.add(chatClient.getLogin());

        JPanel leftMiddleTop = new JPanel();
        leftMiddleTop.add(Box.createRigidArea(new Dimension(5, 0)));
        leftMiddleTop.setLayout(new BoxLayout(leftMiddleTop, BoxLayout.X_AXIS));
        chatClient.getChatRecord().setBorder(BorderFactory.createLineBorder(Color.gray, 1));
        leftMiddleTop.add(chatClient.getChatRecord());

        JPanel leftMiddle = new JPanel();
        leftMiddle.add(Box.createRigidArea(new Dimension(5, 0)));
        leftMiddle.setLayout(new BoxLayout(leftMiddle, BoxLayout.X_AXIS));
        leftMiddle.add(chatLabel);
        leftMiddle.add(Box.createGlue());

        JPanel leftMiddleBottom = new JPanel();
        leftMiddleBottom.add(Box.createRigidArea(new Dimension(5, 0)));
        leftMiddleBottom.setLayout(new BoxLayout(leftMiddleBottom, BoxLayout.X_AXIS));
        chatClient.getChatBox().setBorder(BorderFactory.createLineBorder(Color.gray, 1));
        leftMiddleBottom.add(chatClient.getChatBox());


        JPanel leftBottom = new JPanel();
        leftBottom.add(Box.createRigidArea(new Dimension(5, 0)));
        leftBottom.setLayout(new BoxLayout(leftBottom, BoxLayout.X_AXIS));
        leftBottom.add(send);
        leftBottom.add(Box.createGlue());
        leftBottom.add(clear);


        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(leftTop);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(leftMiddle);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftPanel.add(leftMiddleTop);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(leftMiddleBottom);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftPanel.add(leftBottom);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));


        JPanel rightPanel = new JPanel();
        chatClient.getJScrollPane().setPreferredSize(new Dimension(30, 420));

        JPanel rightTop = new JPanel();
        rightTop.setLayout(new BoxLayout(rightTop, BoxLayout.X_AXIS));
        rightTop.add(onlineLabel);
        rightTop.add(Box.createGlue());


        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(Box.createRigidArea(new Dimension(10, 10)));
        rightPanel.add(rightTop);
        rightPanel.add(Box.createRigidArea(new Dimension(10, 5)));
        rightPanel.add(chatClient.getOnlineCount());
        rightPanel.add(Box.createRigidArea(new Dimension(10, 5)));
        rightPanel.add(chatClient.getJScrollPane());
        rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));


        jPanel.add(leftPanel);
        jPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        jPanel.add(rightPanel);

        chatClient.initModel();//初始化添加“群聊”

        jFrame = new JFrame("客户端");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setSize(new Dimension(200, 500));
        jFrame.add(jPanel);
        jFrame.setResizable(true);
        jFrame.setVisible(true);

        send.addActionListener(new sendListener());
        clear.addActionListener(new clearListener());
        chatClient.getLogin().addActionListener(new clientLoginListener());
        chatClient.getClientList().addListSelectionListener(new p2pListener());
        jFrame.setVisible(true);
    }

    //监听全部在外部实现
    private class sendListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            chatClient.SendThread();
        }
    }

    private class clearListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            chatClient.getChatBox().getText().trim();
            chatClient.getChatBox().setText(null);

        }
    }

    private class clientLoginListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
           chatClient.ClientLogin();
        }
    }

    private class p2pListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            chatClient.ConnectPeer();
        }
    }
}

//客户端的客户端类
class PeerClient {
    private Socket peerSocket = null;//客户端的服务端
    private DataInputStream disWithPeer = null;

    public PeerClient(Socket peerSocket) {
        this.peerSocket = peerSocket;
        try {
            disWithPeer = new DataInputStream(peerSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DataInputStream getDisWithPeer() {
        return disWithPeer;
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
    //两个标志位
    private static boolean sendClient = false;
    private static boolean receiveClient = false;
    private DataOutputStream dosWithPeer;

    //客户端连接客户端
    public void connectPeer(int peerPort) {
        try {
            Socket socketWithPeer = new Socket("127.0.0.1", peerPort);
            dosWithPeer = new DataOutputStream(socketWithPeer.getOutputStream());
            sendClient = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setReceiveClientTrue() {
        receiveClient = true;
    }

    public void setReceiveClientFalse() {
        receiveClient = false;
    }

    public void setSendClientFalse() {
        sendClient = false;
    }

    public boolean getSendClient() {
        return sendClient;
    }

    public boolean getReceiveClient() {
        return receiveClient;
    }

    public DataOutputStream getDosWithPeer() {
        return dosWithPeer;
    }
}

//客户端连接服务端类
class ConnectServer {
    Socket clientSocket = null;//Client自己的scoket
    private DataOutputStream dosWithServer = null;
    private DataInputStream disWithServer = null;
    static boolean connectedWithServer = false;

    public DataInputStream getDisWithServer() {
        return disWithServer;
    }

    public DataOutputStream getDosWithServer() {
        return dosWithServer;
    }

    public void connect() {
        try {
            clientSocket = new Socket("127.0.0.1", 8888);
            dosWithServer = new DataOutputStream(clientSocket.getOutputStream());
            disWithServer = new DataInputStream(clientSocket.getInputStream());
            connectedWithServer = true;
            System.out.println("客户端已连接");
        } catch (UnknownHostException e) {
            System.out.println("服务端未启动");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("服务端未启动");
            System.exit(1);
            e.printStackTrace();
        }
    }
}

//接收信息类
class ReceiveData {
    private String name = "";
    private String str = "";
    //    private String peer = "";
    private static Map<String, String> clientInfo = new HashMap<>();
    private java.util.List<String> listNames;

    private String DELIMITER = "\f";

    //客户端之间通信构造器初始化
    ReceiveData(String data_from_client) {
        java.util.List<String> data_from_client_split = Arrays.asList(data_from_client.split(DELIMITER));
        this.name = data_from_client_split.get(0);
        if (data_from_client_split.size() == 3) {
            this.str = data_from_client_split.get(2);
        } else {
            this.str = "";
        }
    }

    //客户端接收服务端信息构造器初始化
    ReceiveData(String data_from_client, String name_and_port) {
        java.util.List<String> data_from_client_split = Arrays.asList(data_from_client.split(DELIMITER));
        listNames = Arrays.asList(name_and_port.split(DELIMITER));
        this.name = data_from_client_split.get(0);
//        this.str = data_from_client_split.get(2);

        if (data_from_client_split.size() == 3) {
            this.str = data_from_client_split.get(2);
        }
        System.out.println("群发的内容" + str + "tail");
    }

    public String getName() {
        return name;
    }

    public String getStr() {
        return str;
    }

    public java.util.List<String> getListNames(){
        return listNames;
    }
    public Map<String, String> putClientInfo(String name, String port) {
        clientInfo.put(name, port);
        return clientInfo;
    }

    public void clearClientInfo() {
        clientInfo.clear();
    }

    public static Map<String, String> getClientInfo() {
        return clientInfo;
    }

}

//用户登录信息封装类，包含发送接收信息方法
class ClientData {
    private String name = "";
    private String port = "";
    private String str = "";

    public void setName(String name) {
        this.name = name;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setStr(String str) {
        this.str = str;
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

    public String buildMsg(String name, String port, String str) {
        String DELIMITER = "\f";
        return name + DELIMITER + port + DELIMITER + str;
    }

    public void sendData(DataOutputStream dataOutputStream, String Data) throws IOException {
        dataOutputStream.writeUTF(Data);
    }

    public String receiveData(DataInputStream dataInputStream) throws IOException {
        return dataInputStream.readUTF();
    }
}

//主客户端
class ChatClient {
    private static boolean listener = false;//Jlist事件设置一个标志位，用来区别是否建立连接
    private ConnectServer connectServer = new ConnectServer();//客户端连接服务端
    private ClientData clientData = new ClientData();//发送用户登录信息，封装了buildMsg、send和receive
    private ConnectPeerClient connectPeerClient = new ConnectPeerClient();//客户端作为服务端

    private JTextField clientName = new JTextField(10);
    private JButton login = new JButton("登录");

    private JTextArea chatRecord = new JTextArea(25, 20);
    private JTextArea chatBox = new JTextArea(2, 20);

    private static JTextField onlineCount = new JTextField("在线人数");
    private static DefaultListModel<String> listModel = new DefaultListModel<>();
    private static JList<String> clientList = new JList<>(listModel);
    private JScrollPane jScrollPane = new JScrollPane(clientList);

    public void initModel(){
        listModel.addElement("群聊");
    }

    public JTextField getClientName() {
        return clientName;
    }

    public JButton getLogin() {
        return login;
    }

    public JTextArea getChatRecord(){
        return chatRecord;
    }

    public JTextArea getChatBox(){
        return chatBox;
    }

    public JTextField getOnlineCount(){
        return onlineCount;
    }

    public JList<String> getClientList(){
        return clientList;
    }

    public JScrollPane getJScrollPane(){
        return jScrollPane;
    }


    //发送信息
    public void SendThread() {
        clientData.setStr(clientData.getName() + "说：" + chatBox.getText().trim());
        String all = clientData.buildMsg(clientData.getName(), clientData.getPort(), clientData.getStr());
        //ta.setText(str);
        chatBox.setText(null);
        //发送时需要进行各种判断，先判断为空，在判断给服务端还是客户端发送，服务端应该设为默认
        try {
            if (clientData.getStr().length() != clientData.getName().length() + 2) {
                if (!connectPeerClient.getSendClient()) {
                    clientData.sendData(connectServer.getDosWithServer(), all);
                } else {
                    clientData.sendData(connectPeerClient.getDosWithPeer(), all);
                    System.out.println("发送的信息" + clientData.getStr());
                    if (clientData.getStr().length() != clientData.getName().length() + 2)
                        chatRecord.setText(chatRecord.getText() + clientData.getStr() + "\n");
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    //登录监听
    public void ClientLogin(){
        if (clientName.getText().trim().length() >= 1) {
            login.setEnabled(false);
            clientName.setEnabled(false);
            connectServer.connect();//客户端连接服务端
            clientData.setName(clientName.getText());
            clientData.setPort(String.valueOf(connectServer.clientSocket.getLocalPort() + 1));
            String all = clientData.buildMsg(clientData.getName(), clientData.getPort(), "");
            try {
                clientData.sendData(connectServer.getDosWithServer(), all);//客户端向服务端发送登录信息
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            new Thread(new ClientServer()).start();//启动客户端作为服务端的服务
            new Thread(new ReceiveServerMsg()).start();//启动接受信息服务
        } else {
            clientName.setText(null);
        }
    }

    //连接peer监听
    public void ConnectPeer(){

        //设置一个listener标志位，下线后JList会有监听，不采取任何动作
        if (!listener) {
            String peer = String.valueOf(clientList.getSelectedValue());
//            for (Map.Entry<String, String> entry : ReceiveData.getClientInfo().entrySet()) {
//                System.out.println("Map内容  " + entry.getKey() + "    " + entry.getValue());
//            }
            System.out.println("选中项" + peer);
            if (peer != null && !peer.equals("群聊")) {
                connectPeerClient.connectPeer(Integer.parseInt(ReceiveData.getClientInfo().get(peer)));
            } else {
                connectPeerClient.setSendClientFalse();
            }
        }
    }

    //客户端作为服务端
    class ClientServer implements Runnable {
        private PeerClient peerClient;
        private ServerSocket clientServerSocket = null;
        boolean start = false;

        public void run() {
            try {
                clientServerSocket = new ServerSocket(connectServer.clientSocket.getLocalPort() + 1);
                System.out.println("自己的端口" + connectServer.clientSocket.getLocalPort());
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
                    System.out.println("客户端已连接");
                    new Thread(receivePeerMsg).start();
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
        }

        public void run() {
            try {
                while (connectPeerClient.getReceiveClient()) {
                    String data = clientData.receiveData(peerClient.getDisWithPeer());
                    receiveData = new ReceiveData(data);
                    //判断消息是否为空，“：”是否存在，是否是给本人发送信息
                    if ((!receiveData.getStr().equals("")) && !(receiveData.getName().equals(clientData.getName()))) {
                        if (receiveData.getStr().split("：").length != 1)
                            chatRecord.setText(chatRecord.getText() + receiveData.getStr() + "\n");
                    }
                    System.out.println(data);
                }
            } catch (SocketException e) {
                connectPeerClient.setReceiveClientFalse();//接收置位false
                System.out.println("客户端关闭1");
            } catch (EOFException e) {
                System.out.println("客户端关闭2");
            } catch (IOException e) {
                System.out.println("客户端关闭3");

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


    //接收服务端信息
    class ReceiveServerMsg implements Runnable {
        ReceiveData receiveData;
        String data = null;
        String name_and_port = null;

        //刷新在线列表
        public void addLists() {
            listener = true;
            receiveData.clearClientInfo();
            listModel.removeAllElements();
            listModel.addElement("群聊");
            for (String listName : receiveData.getListNames()) {
                String SEPARATOR = "\r";
                listModel.addElement(listName.split(SEPARATOR)[0]);
                receiveData.putClientInfo(listName.split(SEPARATOR)[0], listName.split(SEPARATOR)[1]);
            }
            clientList.setModel(listModel);
            System.out.println("列表人数" + ReceiveData.getClientInfo().size());
            onlineCount.setText("在线人数" + ": " + (listModel.getSize() - 1));
            listener = false;
        }

        public void run() {
            while (ConnectServer.connectedWithServer) {
                try {
                    data = clientData.receiveData(connectServer.getDisWithServer());
                    name_and_port = clientData.receiveData(connectServer.getDisWithServer());
                    receiveData = new ReceiveData(data, name_and_port);
                    //Swing多线程
                    if (receiveData.getStr().equals("")){
                        EventQueue.invokeLater(this::addLists);
//                    EventQueue.invokeLater(new Runnable() {
//                        public void run(){
//                            receiveData.addLists();
//                        }
//                    });
                    }
                    connectPeerClient.setSendClientFalse();//下线后JList全部清空，默认群聊
                } catch (SocketException e1) {
                    System.out.println("服务端关闭");
                    System.exit(0);
                } catch (EOFException e2) {
                    System.exit(0);
                } catch (IOException e) {
                    System.out.println("服务端关闭");

                }
                try {
                    if (!receiveData.getStr().equals("")) {
                        if (receiveData.getStr().length() != receiveData.getName().length() + 2) {
                            chatRecord.setText(chatRecord.getText() + receiveData.getStr() + "\n");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("客户端关闭");
                }
            }
        }
    }

}
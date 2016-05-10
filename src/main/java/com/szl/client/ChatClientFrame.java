package com.szl.client;

import com.szl.utils.DayTime;
import com.szl.utils.Disconnect;
import com.szl.utils.Dom4jXML;
import com.szl.utils.PropertiesGBC;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by zsc on 2015/3/9.
 * <p>
 * 创建ChatClient对象、带有监听事件的Swing、Frame初始化、监听类、main函数
 */

public class ChatClientFrame {

    private ChatClient chatClient = new ChatClient();

    private JLabel clientLabel = new JLabel("用户名");

    private JLabel chatLabel = new JLabel("聊天记录");

    private JLabel onlineLabel = new JLabel("在线好友列表");

    private JButton send = new JButton("发送");
    private JButton clear = new JButton("清空");
    private JButton file = new JButton("文件");
    private JButton receive = new JButton("接收");


    public static void main(String[] args) {
        new ChatClientFrame().init();
    }

    public void init() {
        JPanel jPanel = new JPanel();
        jPanel.setBorder(BorderFactory.createTitledBorder("客户端窗口"));
        jPanel.setLayout(new GridBagLayout());

        chatClient.getChatRecord().setEditable(false);
        chatClient.getOnlineCount().setEditable(false);

        chatClient.initModel();//初始化添加“群聊”

        //用户名
        jPanel.add(clientLabel, new PropertiesGBC(0, 0, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(0, 0).setInsets(0, 5, 5, 0));

        //用户名输入框
        jPanel.add(chatClient.getClientName(), new PropertiesGBC(1, 0, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(1, 0).setInsets(0, 0, 5, 0));

        //登录
        jPanel.add(chatClient.getBtnConnect(), new PropertiesGBC(2, 0, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(0, 0).setInsets(0, 0, 5, 5));

//        //退出
//        jPanel.add(chatClient.getExit(), new PropertiesGBC(3, 0, 1, 1).
//                setFill(PropertiesGBC.BOTH).setWeight(0, 0).setInsets(0, 0, 5, 5));

        //聊天记录
        jPanel.add(chatLabel, new PropertiesGBC(0, 1, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(0, 0).setInsets(0, 5, 5, 5));

        //接收
        jPanel.add(receive, new PropertiesGBC(2, 1, 1, 1).
                setAnchor(PropertiesGBC.EAST).setWeight(0, 0).setInsets(0, 5, 5, 5));

        //聊天记录框
        jPanel.add(chatClient.getChatRecordJScrollPane(), new PropertiesGBC(0, 2, 3, 1).
                setFill(PropertiesGBC.BOTH).setWeight(1, 1).setInsets(0, 5, 5, 5));

        //发送框
        jPanel.add(chatClient.getChatBoxJScrollPane(), new PropertiesGBC(0, 3, 3, 1).
                setFill(PropertiesGBC.BOTH).setWeight(1, 0).setInsets(0, 5, 5, 5).setIpad(0, 20));

        //清空
        jPanel.add(clear, new PropertiesGBC(0, 5, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(0, 0).setInsets(0, 5, 5, 5));

        //文件
        jPanel.add(file, new PropertiesGBC(1, 5, 1, 1).
                setAnchor(PropertiesGBC.EAST).setWeight(0, 0).setInsets(0, 5, 5, 5));

        //发送
        jPanel.add(send, new PropertiesGBC(2, 5, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(0, 0).setInsets(0, 5, 5, 5));


        //在线好友列表
        jPanel.add(onlineLabel, new PropertiesGBC(3, 0, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(0, 0).setInsets(0, 5, 5, 5));

        //在线人数
        jPanel.add(chatClient.getOnlineCountJScrollPane(), new PropertiesGBC(3, 1, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(0.1, 0).setInsets(0, 5, 5, 5));

        //JScrollPane
        jPanel.add(chatClient.getJScrollPane(), new PropertiesGBC(3, 2, 1, 4).
                setFill(PropertiesGBC.BOTH).setWeight(0.1, 1).setInsets(0, 5, 5, 5));

        JFrame jFrame = new JFrame("客户端");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setSize(new Dimension(400, 500));
        jFrame.add(jPanel);
        jFrame.setResizable(true);
        jFrame.setVisible(true);

        send.addActionListener(new sendListener());
        clear.addActionListener(new clearListener());
        chatClient.getBtnConnect().addActionListener(new clientLoginListener());
        chatClient.getClientList().addListSelectionListener(new p2pListener());
        //接收和文件监听
        file.addActionListener(new fileListener());
        receive.addActionListener(new receiveListener());
        jFrame.setVisible(true);
    }

    //监听全部在外部实现
    //发送
    private class sendListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            chatClient.SendAllThread();
        }
    }

    //清空
    private class clearListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            chatClient.getChatBox().setText(null);

        }
    }

    //文件
    private class fileListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            chatClient.OpenFileChooser();
        }
    }

    //接收
    private class receiveListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            chatClient.SaveFileChooser();
        }
    }


    //登录退出
    private class clientLoginListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String btnText = chatClient.getBtnConnect().getText();
            if (btnText.equals("登录")) {
                chatClient.ClientConnect();
            } else if (btnText.equals("退出")) {
                chatClient.ClientExit();
            }
        }
    }

    //List选择监听
    private class p2pListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            chatClient.ConnectPeer();
        }
    }
}

/**
 * 外部类
 */

/**
 * 客户端的客户端类，只用来接收信息，用于封装
 * 上层关闭
 */

//out仅在传送文件的流中使用
class PeerClient {
    private Socket peerSocket = null;//客户端的服务端
    private DataInputStream disWithPeer = null;
    private DataOutputStream dosWithPeer = null;

    public PeerClient(Socket peerSocket) {
        this.peerSocket = peerSocket;
        try {
            disWithPeer = new DataInputStream(peerSocket.getInputStream());
            dosWithPeer = new DataOutputStream(peerSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DataInputStream getDisWithPeer() {
        return disWithPeer;
    }

    public DataOutputStream getDosWithPeer() {
        return dosWithPeer;
    }

    public void close() {
        Disconnect.disconnect(null, peerSocket, disWithPeer, dosWithPeer);
    }
}

/**
 * 客户端的客户端连接客户端的服务端类，只发送消息
 */
//in仅在传送文件的流中使用
class ConnectPeerClient {
    //两个标志位
    private static boolean sendClient = false;
    private static boolean receiveClient = false;
    private Socket socketWithPeer = null;
    private DataInputStream disWithPeer;
    private DataOutputStream dosWithPeer;

    //客户端连接客户端
    public void connectPeer(int peerPort) {
        try {
            socketWithPeer = new Socket("127.0.0.1", peerPort);
            disWithPeer = new DataInputStream(socketWithPeer.getInputStream());
            dosWithPeer = new DataOutputStream(socketWithPeer.getOutputStream());
            sendClient = true;
            System.out.println("连接上的端口号" + socketWithPeer.getLocalPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        Disconnect.disconnect(null, socketWithPeer, null, dosWithPeer);
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

    public DataInputStream getDisWithPeer() {
        return disWithPeer;
    }

    public DataOutputStream getDosWithPeer() {
        return dosWithPeer;
    }
}

/**
 * 客户端连接服务端类
 */
class ConnectServer {
    private Socket clientSocket = null;//Client自己的scoket
    private DataOutputStream dosWithServer = null;
    private DataInputStream disWithServer = null;
    static boolean connectedWithServer = false;

    public Socket getClientSocket() {
        return clientSocket;
    }

    public DataInputStream getDisWithServer() {
        return disWithServer;
    }

    public DataOutputStream getDosWithServer() {
        return dosWithServer;
    }

    public void connect() {
        try {
            clientSocket = new Socket("127.0.0.1", 30000);
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

    public void close() {
        Disconnect.disconnect(null, clientSocket, disWithServer, dosWithServer);
        connectedWithServer = false;
    }
}

/**
 * 接收信息类，从服务端和客户端接收，重载
 */
class ReceiveData {
    private String fileName = "";
    private String name = "";
    private String str = "";
    private static Map<String, String> clientInfo = new HashMap<>();
    private List<String> listNames = null;
    private boolean isFile = false;

    private String DELIMITER = "\f";

    //客户端之间通信构造器初始化，客户peer接收的信息不可能为空
    ReceiveData(String data_from_client) {
        java.util.List<String> data_from_client_split = Arrays.asList(data_from_client.split(DELIMITER));
        this.name = data_from_client_split.get(0);
        this.str = data_from_client_split.get(2);
    }

    //客户端接收服务端信息构造器初始化，信息类型为name&port（登录或退出信息格式）或者name&port&str（普通信息格式）
    ReceiveData(String data_from_client, String name_and_port) {
        List<String> data_from_client_split = Arrays.asList(data_from_client.split(DELIMITER));
        listNames = Arrays.asList(name_and_port.split(DELIMITER));
        this.name = data_from_client_split.get(0);
//        this.str = data_from_client_split.get(2);

        if (data_from_client_split.size() == 3) {
            this.str = data_from_client_split.get(2);
        }
    }

    public boolean isFile() {
        return isFile;
    }

    public String getFileName() {
        return fileName;
    }

    public String getName() {
        return name;
    }

    public String getStr() {
        return str;
    }

    //用于刷新在线列表
    public java.util.List<String> getListNames() {
        return listNames;
    }

    public Map<String, String> putClientInfo(String name, String port) {
        clientInfo.put(name, port);
        return clientInfo;
    }

    public void clearClientInfo() {
        clientInfo.clear();
    }

    //map用于进行P2P连接，name对应port
    public static Map<String, String> getClientInfo() {
        return clientInfo;
    }
}

/**
 * 基础信息类,用户登录信息封装类，包含发送接收信息方法,build普通信息，登录信息重载
 */
class ClientData {
    private String name = "";
    private String port = "";
    private String str = "";
    private String fileName = "";
    private static boolean haveFile;

    public void setHaveFile() {
        this.haveFile = true;
    }

    public void setHaveFileFalse() {
        this.haveFile = false;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setStr(String sender, String receiver, String str) {
        this.str = buildStr(sender, receiver, str);
    }

    public boolean getHaveFile(){
        return haveFile;
    }

    public String getFileName() {
        return fileName;
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

    public String buildMsg(String name, String port) {
        String DELIMITER = "\f";
        return name + DELIMITER + port;
    }

    //当receiver为空时，StringValueof返回"null"字符串
    public String buildStr(String sender, String receiver, String str) {
        if (receiver.equals("null") || receiver.equals("群聊")) {
            receiver = "所有人";
        }
        return sender + " 对 " + receiver + " 说：" +
                " -- " + new DayTime().getDateString() + " --" +
                "\n  " + str;
    }

    //替换回车，replace用"\n"，替换"\n"，用"\\n"，键盘回车是"\n"，replaceAll是转义字符，另说
    public String formatStr(String str) {
        return str.replace("\n", "\n  ");
    }

    //将字符串转为Ascii，得到换行符的Ascii码，判断是哪个转义字符
    public String stringToAscii(String value) {
        StringBuilder sbu = new StringBuilder();
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i != chars.length - 1) {
                sbu.append((int) chars[i]).append(",");
            } else {
                sbu.append((int) chars[i]);
            }
        }
        return sbu.toString();
    }

    public void sendData(DataOutputStream dataOutputStream, String Data) throws IOException {
        dataOutputStream.writeUTF(Data);
    }

    public String receiveData(DataInputStream dataInputStream) throws IOException {
        return dataInputStream.readUTF();
    }
}

/**
 * 主客户端，封装监听的Swing，XML的路径，发送信息方法，部分监听方法
 * 内部类
 */
//主客户端
class ChatClient {
    private ClientServer clientServer = new ClientServer();//客户端作为服务端
    private ReceiveServerMsg receiveServerMsg = new ReceiveServerMsg();//接收服务端信息
    //JList事件设置一个标志位listener，用来区别是否建立连接，因为刷新clientslist列表时，始终监听，所以刷新之前设为true，防止连接出错
    private static boolean listener = false;
    private ConnectServer connectServer = new ConnectServer();//客户端连接服务端
    private ClientData clientData = new ClientData();//发送用户登录信息，封装了buildMsg、send和receive
    private ConnectPeerClient connectPeerClient = new ConnectPeerClient();//客户端连接客户端服务端，本身是客户端的客户端
    private ConnectPeerClient connectPeerClientFile = new ConnectPeerClient();
    private PeerClient peerClient;
    private PeerClient peerClientFile;

    private FileTransmit fileTransmit = new FileTransmit();


    private JTextField clientName = new JTextField(10);
    private JButton btnConnect = new JButton("登录");

    private JTextArea chatRecord = new JTextArea();
    private JTextArea chatBox = new JTextArea();
    private JScrollPane chatBoxJScrollPane = new JScrollPane(chatBox);
    private JScrollPane chatRecordJScrollPane = new JScrollPane(chatRecord);


    private static JTextArea onlineCount = new JTextArea("在线人数");
    private JScrollPane onlineCountJScrollPane = new JScrollPane(onlineCount);

    private static DefaultListModel<String> listModel = new DefaultListModel<>();
    private static JList<String> clientList = new JList<>(listModel);
    private JScrollPane jScrollPane = new JScrollPane(clientList);

    public void initModel() {
        listModel.addElement("群聊");
    }

    public JTextField getClientName() {
        return clientName;
    }

    public JButton getBtnConnect() {
        return btnConnect;
    }

    public JTextArea getChatRecord() {
        return chatRecord;
    }

    public JScrollPane getChatRecordJScrollPane() {
        return chatRecordJScrollPane;
    }

    public JTextArea getChatBox() {
        return chatBox;
    }

    public JScrollPane getChatBoxJScrollPane() {
        return chatBoxJScrollPane;
    }


    public JTextArea getOnlineCount() {
        return onlineCount;
    }

    public JScrollPane getOnlineCountJScrollPane() {
        return onlineCountJScrollPane;
    }

    public JList<String> getClientList() {
        return clientList;
    }

    public JScrollPane getJScrollPane() {
        return jScrollPane;
    }

    private ClientDom4j clientDom4j = new ClientDom4j();

    private String dirPath;

    private String filePath;


    //send文件和聊天信息分开
    public void SendAllThread(){
        if (fileTransmit.getSend()) {
            SendThreadFile();
        } else {
            SendThread();
        }
    }

    //发送文件
    public void SendThreadFile() {
        chatBox.setText(null);//清空输入框的文件信息
        //传输文件
        fileTransmit.sendRunnable(connectPeerClientFile.getDosWithPeer(), connectPeerClientFile.getDisWithPeer());
    }

    //发送信息
    public void SendThread() {
        boolean isNull = true;
        if (chatBox.getText().trim().length() > 0) {
            isNull = false;
        }
        //发送时先判断是否为空，再判断给服务端还是客户端发送，服务端应该设为默认，
        //给服务端发送信息时，Record不用append，等接收服务端返回的信息时append；给peer发送信息时，需要append
        try {
            //判断信息是否为空，利用name + "：" + "说"的长度判断
            if (!isNull) {
                clientData.setStr(clientData.getName(),
                        String.valueOf(clientList.getSelectedValue()), clientData.formatStr(chatBox.getText().trim()));
                //转换为Ascii码用来判断
//                System.out.println(clientData.stringToAscii(chatBox.getText().trim()));
                String msg = clientData.buildMsg(clientData.getName(),
                        clientData.getPort(), clientData.getStr());
                chatBox.setText(null);
                //判断给服务器还是peer端发送信息
                if (!connectPeerClient.getSendClient()) {
                    clientData.sendData(connectServer.getDosWithServer(), msg);
                } else {
                    clientData.sendData(connectPeerClient.getDosWithPeer(), msg);
                    chatRecord.append(clientData.getStr() + "\n");

                    clientDom4j.createRecord(chatRecord.getText());
                    clientDom4j.saveXML(Dom4jXML.getRecordDocument(), dirPath, filePath);
                }
            } else {
                chatRecord.append("发送的内容不能为空\n");
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    //登录监听，设置XML路径
    public void ClientConnect() {
        connectServer.connect();//客户端连接服务端
        //登录名不能为空，为空则set null，重新输入
        if ((clientName.getText().trim().length() >= 1) && clientDom4j.queryElement(clientName.getText())) {
//            btnConnect.setEnabled(false);
            btnConnect.setText("退出");
            clientName.setEnabled(false);
            clientData.setName(clientName.getText());
            clientData.setPort(String.valueOf(connectServer.getClientSocket().getLocalPort() + 1));
            String msg = clientData.buildMsg(clientData.getName(), clientData.getPort());
            dirPath = "D:/" + clientData.getName() + "-" + new DayTime().getDateString();
            filePath = dirPath + File.separator + clientData.getName() + "-" + "ChatRecord.xml";
            try {
                clientData.sendData(connectServer.getDosWithServer(), msg);//客户端向服务端发送登录信息
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            new Thread(clientServer).start();//启动客户端作为服务端的服务
            new Thread(receiveServerMsg).start();//启动接受信息服务
        } else {
            if (clientName.getText().trim().length() == 0) {
                chatRecord.append("用户名不能为空\n");
            } else if (!clientDom4j.queryElement(clientName.getText())) {
                chatRecord.append("用户名已存在\n");
            }
            clientName.setText(null);
        }
    }

    //需要关闭流和socket以及进程，设为false
    public void ClientExit() {
        btnConnect.setText("登录");
        clientName.setEnabled(true);
        //客户端断开与服务端的连接
        connectServer.close();
        //客户端客户端断开与客户端服务端的连接
        connectPeerClient.close();
        //客户端作为服务端关闭，结束while循环
        clientServer.close();
        //客户端接收客户端消息关闭，结束while循环
        receiveServerMsg.close();

        //转为lambda表达式,设置listener屏蔽监听
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                listener = true;
                listModel.removeAllElements();
                listModel.addElement("群聊");
                clientList.setModel(listModel);
                listener = false;
                onlineCount.setText("在线人数" + ": " + (listModel.getSize() - 1));
            }
        });
    }

    //连接peer监听
    public void ConnectPeer() {
        //设置一个listener标志位，下线后JList会有监听，不采取任何动作
        if (!listener) {
            String peer = String.valueOf(clientList.getSelectedValue());
//            for (Map.Entry<String, String> entry : ReceiveData.getClientInfo().entrySet()) {
//                System.out.println("Map内容  " + entry.getKey() + "    " + entry.getValue());
//            }
            System.out.println("选中项" + peer);
            if (peer != null && !peer.equals("群聊")) {
                //点击peer之后直接开2条路
                connectPeerClient.connectPeer(Integer.parseInt(ReceiveData.getClientInfo().get(peer)));
                connectPeerClientFile.connectPeer(Integer.parseInt(ReceiveData.getClientInfo().get(peer)));
            } else {
                //断开P2P连接，默认群聊,顺序不要错
                connectPeerClient.setSendClientFalse();
                connectPeerClientFile.setReceiveClientFalse();
            }
        }
    }

    public void OpenFileChooser() {
        //设置文件传送true
        fileTransmit.chooseFile();
        //起一个新的连接
        //打开后发送框显示路径，并连接识别字符发送至接收方，接收方据此判断，打开接收
        chatBox.setText(fileTransmit.getFolderPath());
    }

    public void SaveFileChooser(){
        fileTransmit.saveFile();
        //打开保存对话框，确定路径后再启动线程
//        if (fileTransmit.getReceive()) {
//            fileTransmit.receiveRunnable(peerClient.getDisWithPeer());
//        }
    }

    //客户端作为服务端
    class ClientServer implements Runnable {
        private ServerSocket clientServerSocket = null;
        //        private PeerClient peerClient;
        private ReceivePeerMsg receivePeerMsg;
        private ReceivePeerFile receivePeerFile;
        private boolean start = false;

        public void close() {
            start = false;
        }

        @Override
        public void run() {
            try {
                clientServerSocket = new ServerSocket(connectServer.getClientSocket().getLocalPort() + 1);
                System.out.println("自己的端口" + connectServer.getClientSocket().getLocalPort());
                start = true;
            } catch (BindException e) {
                System.out.println("端口使用中");
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                //每次连接2条路
                while (start) {
                    Socket socket = clientServerSocket.accept();
                    Socket socketFile = clientServerSocket.accept();

                    peerClient = new PeerClient(socket);
                    peerClientFile = new PeerClient(socketFile);
                    receivePeerMsg = new ReceivePeerMsg(peerClient);
                    receivePeerFile = new ReceivePeerFile(peerClientFile);
                    System.out.println("客户端已连接");
                    new Thread(receivePeerMsg).start();
                    new Thread(receivePeerFile).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    peerClientFile.close();
                    peerClient.close();
                    clientServerSocket.close();
                    receivePeerMsg.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //接收文件信息
    class ReceivePeerFile implements Runnable {
        private PeerClient peerClientFile;

        public ReceivePeerFile(PeerClient peerClientFile) {
            this.peerClientFile = peerClientFile;
        }

        @Override
        public void run(){
            fileTransmit.receiveRunnable(peerClientFile.getDisWithPeer(), peerClientFile.getDosWithPeer());
        }
    }


    //客户端接收客户端信息，socket和流封装在PeerClient
    class ReceivePeerMsg implements Runnable {
        private ReceiveData receiveData;
        private PeerClient peerClient;

        public ReceivePeerMsg(PeerClient peerClient) {
            this.peerClient = peerClient;
            connectPeerClient.setReceiveClientTrue();//接收标志位，有别于发送标志位
        }

        public void close() {
//            peerClient.close();
            connectPeerClient.setReceiveClientFalse();
        }

        @Override
        public void run() {
            try {
                while (connectPeerClient.getReceiveClient()) {
                    String data = clientData.receiveData(peerClient.getDisWithPeer());
                    receiveData = new ReceiveData(data);
                    //只需判断是否是给本人发送信息，内容不可能为空，因为发送时以判定内容不能为空，如果是给本人发送的，Record不set信息
                    if (!(receiveData.getName().equals(clientData.getName()))) {
                        chatRecord.append(receiveData.getStr() + "\n");

                        clientDom4j.createRecord(chatRecord.getText());
                        clientDom4j.saveXML(Dom4jXML.getRecordDocument(), dirPath, filePath);
                    }
                }
            } catch (SocketException e) {
                connectPeerClient.setReceiveClientFalse();//接收置位false，停止接收
                System.out.println("客户端关闭1");
            } catch (EOFException e) {
                System.out.println("客户端关闭2");
            } catch (IOException e) {
                System.out.println("客户端关闭3");
            } finally {
                peerClient.close();
            }
        }
    }

    //接收服务端信息,没有finally，因为connectServer另有地方关闭它
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

        public void close() {
            ConnectServer.connectedWithServer = false;
        }

        //while放到try里面，setSendClientFalse()放到if里面
        @Override
        public void run() {
            try {
                while (ConnectServer.connectedWithServer) {
                    data = clientData.receiveData(connectServer.getDisWithServer());
                    name_and_port = clientData.receiveData(connectServer.getDisWithServer());
                    receiveData = new ReceiveData(data, name_and_port);
                    //Swing多线程，判断str长度是否为空，invokeLater，解决窗口没有反应，重新登录或下线才可能恢复正常的bug
                    if (receiveData.getStr().length() == 0) {
                        EventQueue.invokeLater(this::addLists);
//                    EventQueue.invokeLater(new Runnable() {
//                        public void run(){
//                            receiveData.addLists();
//                        }
//                    });
                        connectPeerClient.setSendClientFalse();//下线后JList全部清空，默认群聊
                    }
                    //判断是普通消息还是注册信息，普通消息不可能为空，注册消息为空
                    if (receiveData.getStr().length() != 0) {
                        chatRecord.append(receiveData.getStr() + "\n");
                        clientDom4j.createRecord(chatRecord.getText());
                        clientDom4j.saveXML(Dom4jXML.getRecordDocument(), dirPath, filePath);
                    }
                }
            } catch (SocketException e) {
                System.out.println("服务端关闭1");
                //用来判断是客户端断开还是服务端断开，服务端断开为true，客户端断开为false
                if (ConnectServer.connectedWithServer) {
                    System.exit(0);
                }
            } catch (EOFException e) {
                System.out.println("服务端关闭2");
//                    System.exit(0);
            } catch (IOException e) {
                System.out.println("服务端关闭3");
            }
//                try {
//
//                } catch (Exception e) {
//                    System.out.println("客户端关闭");
//                }
        }
    }
}

class FileTransmit {
    private static String folderPath = "";
    private static String folderName = "";
    private long totalLen = 0L;
    private static boolean isSend = false;
    private boolean isReceive = false;
    private boolean isOver = false;
    private static final int BUF_LEN = 102400;
    private File file;
    private FileInputStream fileInputStream;
    private FileOutputStream fileOutputStream;
    private DataInputStream disWithPeer;
    private DataOutputStream dosWithPeer;

    //set 文件名为发送来的文件名，用于接收并存储
    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public String getFolderName() {
        return folderName;
    }

    public boolean getSend() {
        return isSend;
    }

    public boolean getReceive(){
        return isReceive;
    }


    public void chooseFile() {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int returnVal = jFileChooser.showOpenDialog(null);
        //点击打开后，发送框显示地址，点发送，接收方先收到信息，显示接收按钮，按下后设置接收地址，在接收
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = jFileChooser.getSelectedFile();
            //输出文件大小
            long l = file.length();
            System.out.println(l);
            this.folderPath = file.getAbsolutePath();
            this.folderName = file.getName();
            this.isSend = true;
            System.out.println("发送文件夹:" + folderPath);
            System.out.println("发送文件:" + folderName);

        }
    }

    public void saveFile() {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = jFileChooser.showSaveDialog(null);
        //点击打开后，发送框显示地址，点发送，接收方先收到信息，显示接收按钮，按下后设置接收地址，在接收
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = jFileChooser.getSelectedFile();
            //输出文件大小
            long l = file.length();
            //接收文件名字不变，路径自设
            this.folderPath = file.getAbsolutePath();
            this.isReceive = true;
            System.out.println("接收文件夹:" + folderPath);
            System.out.println("接收文件:" + folderName);

        }
    }

    public void sendRunnable(DataOutputStream dataOutputStream, DataInputStream dataInputStream) {
        Runnable send = new Runnable() {
            @Override
            public void run() {
                sendFile(dataOutputStream, dataInputStream);
            }
        };
        new Thread(send).start();
    }

    public void receiveRunnable(DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        Runnable receive = new Runnable() {
            @Override
            public void run() {
                receiveFile(dataInputStream,dataOutputStream);
//                receiveInt(dataInputStream);
            }
        };
        new Thread(receive).start();
    }

    public void sendFile(DataOutputStream dataOutputStream, DataInputStream dataInputStream) {
        this.isSend = false;
        this.disWithPeer = dataInputStream;
        this.dosWithPeer = dataOutputStream;
        byte[] sendBuffer = new byte[BUF_LEN];
        int length = 0;

        file = new File(folderPath);
        System.out.println("fasong文件:" + folderPath);
        try {
            dosWithPeer.writeUTF(folderName);
            String allow = disWithPeer.readUTF();
            if (allow.equals("Agree")) {
                //发送文件
                fileInputStream = new FileInputStream(file);
                while ((length = fileInputStream.read(sendBuffer, 0 , sendBuffer.length)) > 0) {
                    System.out.println("length " + length);
                    dosWithPeer.write(sendBuffer, 0, length);
                    dosWithPeer.flush();
                }
                System.out.println("发送方结束循环" + length);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("发送文件结束");
        } finally {
            try {
                if (fileInputStream != null)
                    fileInputStream.close();
                if (dosWithPeer != null)
                    dosWithPeer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void receiveFile(DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        System.out.println("接收循环跑了吗");
        this.disWithPeer = dataInputStream;
        this.dosWithPeer = dataOutputStream;
        byte[] receiveBuffer = new byte[BUF_LEN];
        int length = 0;

        try {
            System.out.println("接收循环开始");
            folderName = disWithPeer.readUTF();
            while (folderPath.length() == 0) {
                System.out.print(".");
            }
            System.out.println("接收循环goon..");

            folderPath = folderPath + File.separator + folderName;
            fileOutputStream = new FileOutputStream(new File(folderPath));
            dosWithPeer.writeUTF("Agree");
            while ((length = disWithPeer.read(receiveBuffer, 0, receiveBuffer.length)) > 0) {
                fileOutputStream.write(receiveBuffer, 0, length);
                fileOutputStream.flush();
                System.out.println("接收方length  " + length);
            }
            System.out.println("接收方结束循环");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null)
                    fileOutputStream.close();
                if (disWithPeer != null)
                    disWithPeer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

/**
 * 操作XML进行记录，创建XML文件保存Record
 */
/*class ClientOperateXML extends OperateXML {
    private String clientsListPath;

    public ClientOperateXML() {
        this.clientsListPath = "D:/ClientsList.xml";
    }

    public boolean queryElement(String name) {
        boolean flag = true;
        Document document;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            document = builder.parse(new File(clientsListPath));
            NodeList clientsList = document.getElementsByTagName("clients");
            if (clientsList.getLength() >= 0) {
                for (int i = 0; i < clientsList.getLength(); i++) {
                    NodeList clientsChildList = clientsList.item(i).getChildNodes();
                    for (int j = 0; j < clientsChildList.getLength(); j++) {
                        System.out.println("name" + clientsChildList.item(j).getTextContent());
                        if (clientsChildList.item(j).getNodeName().trim().equals("name")) {
                            if (clientsChildList.item(j).getTextContent().trim().equals(name)) {
                                flag = false;
                            }
                        }
                    }

                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return flag;
    }
}*/

class ClientDom4j extends Dom4jXML {
    private String clientsListPath;

    public ClientDom4j() {
        this.clientsListPath = "D:/ChatServer/ClientsList.xml";
    }

    public boolean queryElement(String name) {
        boolean flag = true;
        try {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(new File(clientsListPath));
            Element rootElement = document.getRootElement();
            //得到clients的List，进行遍历判断
            List clientsList = rootElement.elements("clients");
            //foreach遍历
            for (Object clientsListElement : clientsList) {
                Element nameElement = (Element) clientsListElement;
                if (nameElement.element("name").getText().equals(name)) {
                    flag = false;
                }
            }
//            for (int i = 0; i < clientsList.size(); i++) {
//                Element nameElement = (Element) clientsList.get(i);
//                if (nameElement.element("name").getText().equals(name)) {
//                    flag = false;
//                }
//            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return flag;
    }
}
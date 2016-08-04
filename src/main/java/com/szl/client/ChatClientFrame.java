package com.szl.client;

import com.szl.utils.DayTime;
import com.szl.utils.Disconnect;
import com.szl.utils.Dom4jXML;
import com.szl.utils.PropertiesGBC;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.omg.PortableInterceptor.ACTIVE;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


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


    public static void main(String[] args) {
        new ChatClientFrame().init();
    }

    public void init() {
        JPanel jPanel = chatClient.getjPanel();
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
        jPanel.add(chatClient.getFile(), new PropertiesGBC(1, 5, 1, 1).
                setAnchor(PropertiesGBC.EAST).setWeight(0, 0).setInsets(0, 5, 5, 5));
        chatClient.getFile().setEnabled(false);

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

        JFrame jFrame = chatClient.getjFrame();
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
        chatClient.getFile().addActionListener(new fileListener());
        chatClient.getReceive().addActionListener(new receiveListener());
        chatClient.getCancel().addActionListener((new cancelListener()));
        jFrame.setVisible(true);
    }

    //监听全部在外部实现
    //发送
    private class sendListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            chatClient.send();
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
            chatClient.openFileChooser();
        }
    }

    //接收
    private class receiveListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            chatClient.saveFileChooser();
        }
    }

    private class cancelListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            chatClient.cancelFileTransmit();
        }
    }


    //登录退出
    private class clientLoginListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String btnText = chatClient.getBtnConnect().getText();
            if (btnText.equals("登录")) {
                chatClient.clientLogin();
            } else if (btnText.equals("退出")) {
                chatClient.clientExit();
            }
        }
    }

    //List选择监听
    private class p2pListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            chatClient.connectPeer();
        }
    }
}

/**
 * 外部类
 */

/**
 * 客户端的客户端类，主要配合ReceivePeerMsg使用，用来接收信息;并且用于其他类的封装
 * 由上层关闭
 */

class IOUtil {
    private Socket socket = null;
    private DataInputStream dataInputStream = null;
    private DataOutputStream dataOutputStream = null;

    public DataInputStream getDataInputStream() {
        return dataInputStream;
    }

    public void setDataInputStream(DataInputStream dataInputStream) {
        this.dataInputStream = dataInputStream;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public void setDataOutputStream(DataOutputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}

//out仅在传送文件的流中使用
class PeerClient {
    private IOUtil ioUtil = new IOUtil();

    public PeerClient(Socket peerSocket) {
        ioUtil.setSocket(peerSocket);
        try {
            ioUtil.setDataInputStream(new DataInputStream(peerSocket.getInputStream()));
            ioUtil.setDataOutputStream(new DataOutputStream(peerSocket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DataInputStream getDisWithPeer() {
        return ioUtil.getDataInputStream();
    }

    public DataOutputStream getDosWithPeer() {
        return ioUtil.getDataOutputStream();
    }

    public void close() {
        Disconnect.disconnect(null, ioUtil.getSocket(), ioUtil.getDataInputStream(), ioUtil.getDataOutputStream());
    }
}

/**
 * 客户端的客户端连接客户端的服务端类，只发送消息
 */
//in仅在传送文件的流中使用
class ClientConnectPeerClient {
    //两个标志位
    private static boolean sendToClient = false;
    private static boolean receiveFromClient = false;
    private IOUtil ioUtil = new IOUtil();

    //客户端连接客户端
    public void connectPeerClient(int peerClientPort) {
        try {
            ioUtil.setSocket(new Socket("127.0.0.1", peerClientPort));
            ioUtil.setDataInputStream(new DataInputStream(ioUtil.getSocket().getInputStream()));
            ioUtil.setDataOutputStream(new DataOutputStream(ioUtil.getSocket().getOutputStream()));
            sendToClient = true;
            System.out.println("连接上的端口号" + ioUtil.getSocket().getLocalPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        Disconnect.disconnect(null, ioUtil.getSocket(), ioUtil.getDataInputStream(), ioUtil.getDataOutputStream());
    }

    public void setSendToClient(boolean sendToClient) {
        ClientConnectPeerClient.sendToClient = sendToClient;
    }

    public void setReceiveFromClient(boolean receiveFromClient) {
        ClientConnectPeerClient.receiveFromClient = receiveFromClient;
    }

    public boolean getSendToClient() {
        return sendToClient;
    }

    public boolean getReceiveFromClient() {
        return receiveFromClient;
    }

    public DataInputStream getDisWithPeer() {
        return ioUtil.getDataInputStream();
    }

    public DataOutputStream getDosWithPeer() {
        return ioUtil.getDataOutputStream();
    }
}

/**
 * 客户端连接服务端类
 */
class ClientConnectServer {
    private static boolean connectWithServer = false;
    private IOUtil ioUtil = new IOUtil();

    public void setConnectWithServer(boolean connectWithServer) {
        ClientConnectServer.connectWithServer = connectWithServer;
    }

    public void connectServer() {
        try {
            ioUtil.setSocket(new Socket("127.0.0.1", 30000));
            ioUtil.setDataInputStream(new DataInputStream(ioUtil.getSocket().getInputStream()));
            ioUtil.setDataOutputStream(new DataOutputStream(ioUtil.getSocket().getOutputStream()));
            connectWithServer = true;
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

    public boolean getConnectWithServer() {
        return connectWithServer;
    }

    public Socket getClientSocket() {
        return ioUtil.getSocket();
    }

    public DataInputStream getDisWithServer() {
        return ioUtil.getDataInputStream();
    }

    public DataOutputStream getDosWithServer() {
        return ioUtil.getDataOutputStream();
    }

    public void close() {
        Disconnect.disconnect(null, ioUtil.getSocket(), ioUtil.getDataInputStream(), ioUtil.getDataOutputStream());
        connectWithServer = false;
    }
}

/**
 * 接收信息类，从服务端和客户端接收，重载
 */
class ReceiveData {
    private String name = "";
    private String str = "";
    private String file = "";
    private String fileStr = "";
    private static Map<String, String> clientInfo = new HashMap<>();//name和port
    private List<String> namePortList = null;//List初始化问题？

    private String DELIMITER = "\f";
    private String FILE_DELIMITER = "\f\r\f";

    //客户端之间通信构造器初始化，客户peer接收的信息不可能为空
    ReceiveData(String data_from_client) {
        if (data_from_client.contains(FILE_DELIMITER)) {
            System.out.println("进入文件接收构造");
            List<String> data_from_client_split = Arrays.asList(data_from_client.split(FILE_DELIMITER));
            this.file = data_from_client_split.get(1);
            this.fileStr = data_from_client_split.get(0);
        } else {
            List<String> data_from_client_split = Arrays.asList(data_from_client.split(DELIMITER));
            this.name = data_from_client_split.get(0);
            this.str = data_from_client_split.get(2);
        }
    }

    //客户端接收服务端信息构造器初始化，信息类型为name&port（登录或退出信息格式）或者name&port&str（普通信息格式）
    ReceiveData(String data_from_client, String name_and_port) {
        List<String> data_from_client_split = Arrays.asList(data_from_client.split(DELIMITER));
        namePortList = Arrays.asList(name_and_port.split(DELIMITER));
        this.name = data_from_client_split.get(0);
//        this.str = data_from_client_split.get(2);

        if (data_from_client_split.size() == 3) {
            this.str = data_from_client_split.get(2);
        }
    }

    public String getName() {
        return name;
    }

    public String getStr() {
        return str;
    }

    public String getFile() {
        return file;
    }

    public String getFileStr() {
        return fileStr;
    }

    //用于刷新在线列表
    public List<String> getNamePortList() {
        return namePortList;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setStr(String sender, String receiver, String str) {
        this.str = buildStr(sender, receiver, str);
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

    public String buildFileMsg(String sender, String str) {
        String FILE_DELIMITER = "\f\r\f";
        return sender + " 发来文件 " + str + " 请接收" + FILE_DELIMITER + "file";
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
    private ClientConnectServer clientConnectServer = new ClientConnectServer();//客户端连接服务端
    private ClientData clientData = new ClientData();//发送用户登录信息，封装了buildMsg、send和receive
    private ClientConnectPeerClient clientConnectPeerClient = new ClientConnectPeerClient();//客户端连接客户端服务端，本身是客户端的客户端
    private ClientConnectPeerClient clientConnectPeerClientFile = new ClientConnectPeerClient();
//    private PeerClient peerClient;
//    private PeerClient peerClientFile;

    private FileTransmit fileTransmit = new FileTransmit();

    private static JFrame jFrame = new JFrame("客户端");
    private static JPanel jPanel = new JPanel();

    private JTextField clientName = new JTextField(10);
    private JButton btnConnect = new JButton("登录");

    private static JButton file = new JButton("文件");
    private static JButton receive = new JButton("接收");
    private static JLabel rate = new JLabel("完成:  0%");
    private static JButton cancel = new JButton("取消");

    private static JTextArea chatRecord = new JTextArea();
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

    public static void appendChatMsg(String str) {
        chatRecord.append(str);
    }

    public static JButton getFile() {
        return file;
    }

    public static JButton getReceive() {
        return receive;
    }

    public static JLabel getRate() {
        return rate;
    }

    public static JButton getCancel() {
        return cancel;
    }


    public static JPanel getjPanel() {
        return jPanel;
    }

    public static JFrame getjFrame() {
        return jFrame;
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

    private String dirPath = "";

    private String filePath = "";


    //send文件和聊天信息分开
    public void send() {
        if (fileTransmit.getSend()) {
            sendFile();
        } else {
            sendMsg();
        }
    }

    //发送文件
    public void sendFile() {
//        chatBox.setText(null);//清空输入框的文件信息
        sendMsg();
        fileTransmit.connect(clientConnectPeerClientFile.getDisWithPeer(), clientConnectPeerClientFile.getDosWithPeer());
        //传输文件
        fileTransmit.sendRunnable();
        fileTransmit.setIsSend(false);
        file.setEnabled(false);
        fileTransmit.addRateCancel();
    }

    //发送信息
    public void sendMsg() {
        boolean isNull = true;
        String msg;
        if (chatBox.getText().trim().length() > 0) {
            isNull = false;
        }
        //发送时先判断是否为空，再判断给服务端还是客户端发送，服务端应该设为默认，
        //给服务端发送信息时，Record不用append，等接收服务端返回的信息时append；给peer发送信息时，需要append
        try {
            //判断信息是否为空，利用name + "：" + "说"的长度判断
            if (!isNull) {
                if (fileTransmit.getSend()) {
                    msg = clientData.buildFileMsg(clientData.getName(), chatBox.getText().trim());
                    chatBox.setText(null);
                    clientData.sendData(clientConnectPeerClient.getDosWithPeer(), msg);
                } else {
                    clientData.setStr(clientData.getName(),
                            String.valueOf(clientList.getSelectedValue()), clientData.formatStr(chatBox.getText().trim()));
                    //转换为Ascii码用来判断
//                System.out.println(clientData.stringToAscii(chatBox.getText().trim()));
                    msg = clientData.buildMsg(clientData.getName(),
                            clientData.getPort(), clientData.getStr());
                    chatBox.setText(null);
                    //判断给服务器还是peer端发送信息
                    if (!clientConnectPeerClient.getSendToClient()) {
                        clientData.sendData(clientConnectServer.getDosWithServer(), msg);
                    } else {
                        clientData.sendData(clientConnectPeerClient.getDosWithPeer(), msg);
                        chatRecord.append(clientData.getStr() + "\n");

                        clientDom4j.createRecord(chatRecord.getText());
                        clientDom4j.saveXML(Dom4jXML.getRecordDocument(), dirPath, filePath);
                    }
                }


            } else {
                chatRecord.append("发送的内容不能为空\n");
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    //登录监听，设置XML路径
    public void clientLogin() {
        clientConnectServer.connectServer();//客户端连接服务端
        //登录名不能为空，为空则set null，重新输入
        if ((clientName.getText().trim().length() >= 1) && clientDom4j.queryElement(clientName.getText())) {
//            btnConnect.setEnabled(false);
            btnConnect.setText("退出");
            clientName.setEnabled(false);
            clientData.setName(clientName.getText());
            clientData.setPort(String.valueOf(clientConnectServer.getClientSocket().getLocalPort() + 1));
            String msg = clientData.buildMsg(clientData.getName(), clientData.getPort());
            dirPath = "D:/" + clientData.getName() + "-" + new DayTime().getDateString();
            filePath = dirPath + File.separator + clientData.getName() + "-" + "ChatRecord.xml";
            try {
                clientData.sendData(clientConnectServer.getDosWithServer(), msg);//客户端向服务端发送登录信息
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
    public void clientExit() {
        btnConnect.setText("登录");
        clientName.setEnabled(true);
        //客户端断开与服务端的连接
        clientConnectServer.close();
        //客户端客户端断开与客户端服务端的连接
        clientConnectPeerClient.close();
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
    public void connectPeer() {
        //设置一个listener标志位，下线后JList会有监听，不采取任何动作
        if (!listener) {
            String peer = String.valueOf(clientList.getSelectedValue());
//            for (Map.Entry<String, String> entry : ReceiveData.getClientInfo().entrySet()) {
//                System.out.println("Map内容  " + entry.getKey() + "    " + entry.getValue());
//            }
            System.out.println("选中项" + peer);
            if (peer != null && !peer.equals("群聊")) {
                //点击peer之后直接开2条路
                clientConnectPeerClient.connectPeerClient(Integer.parseInt(ReceiveData.getClientInfo().get(peer)));
                if (!peer.equals(clientData.getName().trim())) {
                    clientConnectPeerClientFile.connectPeerClient(Integer.parseInt(ReceiveData.getClientInfo().get(peer)));
                    file.setEnabled(true);
                } else {
                    clientConnectPeerClientFile.setReceiveFromClient(false);
                    file.setEnabled(false);
                }
            } else {
                //断开P2P连接，默认群聊,顺序不要错
                clientConnectPeerClient.setSendToClient(false);
            }
        }
    }

    public void openFileChooser() {
        //设置文件传送true
        fileTransmit.chooseFile();
        //起一个新的连接
        //打开后发送框显示路径，并连接识别字符发送至接收方，接收方据此判断，打开接收
        chatBox.setText(fileTransmit.getFolderPath());
    }

    public void saveFileChooser() {
        fileTransmit.saveFile();
        //打开保存对话框，确定路径后再启动线程
//        if (fileTransmit.getReceive()) {
//            fileTransmit.receiveRunnable(peerClient.getDisWithPeer());
//        }
    }

    public void cancelFileTransmit() {
        fileTransmit.close();
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                file.setEnabled(false);
                listener = true;
                clientList.setModel(listModel);
                listener = false;
                clientConnectPeerClient.setSendToClient(false);
            }
        });

//        clientConnectPeerClientFile.
//                connectPeerClient(Integer.parseInt(ReceiveData.getClientInfo().
//                        get(String.valueOf(clientList.getSelectedValue()))));
//        file.setEnabled(true);
    }

    //客户端作为服务端
    class ClientServer implements Runnable {
        private ServerSocket clientServerSocket = null;
        private PeerClient peerClient = null;
        private PeerClient peerClientFile = null;
        private ReceivePeerMsg receivePeerMsg = null;
        private ReceivePeerFile receivePeerFile = null;
        private boolean start = false;

        public void close() {
            start = false;
        }

        @Override
        public void run() {
            try {
                clientServerSocket = new ServerSocket(clientConnectServer.getClientSocket().getLocalPort() + 1);
                System.out.println("自己的端口" + clientConnectServer.getClientSocket().getLocalPort());
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
                    Socket fileSocket = clientServerSocket.accept();

                    peerClient = new PeerClient(socket);
                    peerClientFile = new PeerClient(fileSocket);
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
        private PeerClient peerClientFile = null;

        public ReceivePeerFile(PeerClient peerClientFile) {
            this.peerClientFile = peerClientFile;
        }

        @Override
        public void run() {
            fileTransmit.connect(peerClientFile.getDisWithPeer(), peerClientFile.getDosWithPeer());
            fileTransmit.receiveRunnable();
        }
    }


    //客户端接收客户端信息，socket和流封装在PeerClient
    class ReceivePeerMsg implements Runnable {
        private ReceiveData receiveData;
        private PeerClient peerClient;

        public ReceivePeerMsg(PeerClient peerClient) {
            this.peerClient = peerClient;
            clientConnectPeerClient.setReceiveFromClient(true);//接收标志位，有别于发送标志位
        }

        public void close() {
//            peerClient.close();
            clientConnectPeerClient.setReceiveFromClient(false);
        }

        @Override
        public void run() {
            try {
                while (clientConnectPeerClient.getReceiveFromClient()) {
                    String data = clientData.receiveData(peerClient.getDisWithPeer());
                    receiveData = new ReceiveData(data);
                    //只需判断是否是给本人发送信息，内容不可能为空，因为发送时以判定内容不能为空，如果是给本人发送的，Record不set信息
                    if (!(receiveData.getName().equals(clientData.getName()))) {
                        if (receiveData.getFile().equals("file")) {
                            chatRecord.append(receiveData.getFileStr() + "\n");
                            //接收
                            jPanel.add(receive, new PropertiesGBC(2, 1, 1, 1).
                                    setAnchor(PropertiesGBC.EAST).setWeight(0, 0).setInsets(0, 5, 5, 5));
                            jFrame.validate();//刷新
                        } else {
                            chatRecord.append(receiveData.getStr() + "\n");

                            clientDom4j.createRecord(chatRecord.getText());
                            clientDom4j.saveXML(Dom4jXML.getRecordDocument(), dirPath, filePath);
                        }
                    }
                }
            } catch (SocketException e) {
                clientConnectPeerClient.setReceiveFromClient(false);//接收置位false，停止接收
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
        ReceiveData receiveData = null;
        String data = "";
        String namePort = "";

        //刷新在线列表
        public void addLists() {
            listener = true;
            receiveData.clearClientInfo();
            listModel.removeAllElements();
            listModel.addElement("群聊");
            for (String listName : receiveData.getNamePortList()) {
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
            clientConnectServer.setConnectWithServer(false);
        }

        //while放到try里面，setSendToClientFalse()放到if里面
        @Override
        public void run() {
            try {
                while (clientConnectServer.getConnectWithServer()) {
                    data = clientData.receiveData(clientConnectServer.getDisWithServer());
                    namePort = clientData.receiveData(clientConnectServer.getDisWithServer());
                    receiveData = new ReceiveData(data, namePort);
                    //Swing多线程，判断str长度是否为空，invokeLater，解决窗口没有反应，重新登录或下线才可能恢复正常的bug
                    if (receiveData.getStr().length() == 0) {
                        EventQueue.invokeLater(this::addLists);
//                    EventQueue.invokeLater(new Runnable() {
//                        public void run(){
//                            receiveData.addLists();
//                        }
//                    });
                        clientConnectPeerClient.setSendToClient(false);//下线后JList全部清空，默认群聊
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
                if (clientConnectServer.getConnectWithServer()) {
                    System.exit(0);
                }
            } catch (EOFException e) {
                System.out.println("服务端关闭2");
//                    System.exit(0);
            } catch (IOException e) {
                System.out.println("服务端关闭3");
            }
        }
    }
}

/**
 * 发送线程启动，向接收方发送文件名，接收方接收到文件名后，阻塞，等待saveFile生成路径，
 * 生成路径后，唤起接收线程，向发送方返回Agree，然后直接启动接收，发送方接收到Agree后，
 * 执行发送（接收线程的启动先于发送线程）
 */

class FileTransmit {
    private static String folderPath = "";
    private static String folderName = "";
    private String useTime = "";
    private String speed = "";
    private boolean firstTime = true;//用于取消显示正确文件文件名
    private int index;
    private long totalLen = 0L;
    private boolean isSend = false;
    private static final int BUF_LEN = 102400;
    private FileInputStream fileInputStream;
    private FileOutputStream fileOutputStream;
    private DataInputStream disWithPeer;
    private DataOutputStream dosWithPeer;
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    //set 文件名为发送来的文件名，用于接收并存储
    public String getFolderPath() {
        return folderPath;
    }

    public boolean getSend() {
        return isSend;
    }

    public String getUseTime() {
        return useTime;
    }

    public String getSpeed() {
        return speed;
    }

    public void setIsSend(boolean isSend) {
        this.isSend = isSend;
    }

    public void connect(DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        this.disWithPeer = dataInputStream;
        this.dosWithPeer = dataOutputStream;
    }

    public void addRateCancel() {
        //完成百分比
        ChatClient.getjPanel().add(ChatClient.getRate(), new PropertiesGBC(1, 1, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(0, 0).setInsets(0, 5, 5, 5));

        ChatClient.getRate().setText("完成:  0%");

        //取消
        ChatClient.getjPanel().add(ChatClient.getCancel(), new PropertiesGBC(2, 1, 1, 1).
                setAnchor(PropertiesGBC.EAST).setWeight(0, 0).setInsets(0, 5, 5, 5));
        ChatClient.getjFrame().validate();//刷新
    }

    public void removeRateCancel() {
        ChatClient.getjPanel().remove(ChatClient.getRate());
        ChatClient.getjPanel().remove(ChatClient.getCancel());
        ChatClient.getjFrame().validate();
    }

    public void close() {
        try {
            if (fileInputStream != null)
                fileInputStream.close();
            if (fileOutputStream != null)
                fileOutputStream.close();
            if (disWithPeer != null)
                disWithPeer.close();
            if (dosWithPeer != null)
                dosWithPeer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getFolderTotalLen(String path) {
        this.totalLen = 0L;
        File folder = new File(path);
        getFileLen(folder);
    }

    private void getFileLen(File folder) {
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                this.totalLen += file.length();
            } else if (file.isDirectory()) {
                getFileLen(file);
            }
        }
    }

    public String getUseTime(long time) {
        String useTime;
        if (time / 1000D / 60D >= 1) {
            useTime = time / 1000 / 60 + " 分钟";
        } else {
            if (time / 1000 == 0) {
                useTime = "1 秒钟";
            } else {
                useTime = time / 1000 + " 秒钟";
            }
        }
        return useTime;
    }

    public String getSpeed(long time, long totalLen) {
        String speed = (totalLen * 1000D) / (1024D * 1024D * time) + "";
        int indexP = speed.indexOf(".");
        if (indexP != -1) {
            speed = speed.substring(0, indexP + 3);
        }
        return (speed + " MB/S");
    }

    public void chooseFile() {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int returnVal = jFileChooser.showOpenDialog(null);
        //点击打开后，发送框显示地址，点发送，接收方先收到信息，显示接收按钮，按下后设置接收地址，在接收
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jFileChooser.getSelectedFile();
            //输出文件大小
            long l = file.length();
            folderPath = file.getAbsolutePath();
            folderName = file.getName();
            isSend = true;
        }
    }

    public void saveFile() {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = jFileChooser.showSaveDialog(null);
        //点击打开后，发送框显示地址，点发送，接收方先收到信息，显示接收按钮，按下后设置接收地址，在接收
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jFileChooser.getSelectedFile();
            //输出文件大小
            long l = file.length();
            //接收文件名字不变，路径自设
            lock.lock();
            try {
                folderPath = file.getAbsolutePath();
                condition.signal();
            } finally {
                lock.unlock();
            }
        }
    }

    public void sendRunnable() {
        Runnable send = new Runnable() {
            private long haveSendLen = 0L;

            @Override
            public void run() {
                try {
                    File folder = new File(folderPath);
                    index = folderPath.length() - folderName.length();
                    long beginTime = 0L;
                    long endTime;
                    String begin;
                    begin = disWithPeer.readUTF();
                    if (begin.equals("Agree")) {
                        beginTime = System.currentTimeMillis();
                        if (folder.isFile()) {
                            totalLen = folder.length();//文件长度
                            dosWithPeer.writeLong(totalLen);
                            sendFile(folder);
                        } else {
                            getFolderTotalLen(folderPath);//得到totalLen
                            dosWithPeer.writeLong(totalLen);
                            sendFolder(folder);
                        }
                    }
                    endTime = System.currentTimeMillis();
                    dosWithPeer.writeUTF("endTransmit");
                    useTime = getUseTime(endTime - beginTime);
                    speed = getSpeed(endTime - beginTime, totalLen);
                    ChatClient.appendChatMsg("文件【" + folderName + "】发送完毕, 传送用时: "
                            + useTime + ",速度: " + speed
                            + " !\n");
                    ChatClient.getFile().setEnabled(true);
                    removeRateCancel();
                } catch (IOException e) {
                    ChatClient.appendChatMsg("文件 " + folderName + " 取消传送\n");
                    ChatClient.getFile().setEnabled(true);
                    removeRateCancel();
                    e.printStackTrace();
                }

            }

            private void sendFolder(File folder) {
                String selectFolderPath = folder.getAbsolutePath().substring(index);//选择的文件夹名字
                try {
                    dosWithPeer.writeUTF("sendFolder");
                    dosWithPeer.writeUTF(selectFolderPath);//发送子文件夹
                } catch (IOException e) {
                    e.printStackTrace();
                }
                File[] files = folder.listFiles();
                List<File> listFile = new ArrayList<>();
                List<File> listFolder = new ArrayList<>();
                for (File file : files) {
                    if (file.isFile()) {
                        listFile.add(file);
                    } else if (file.isDirectory()) {
                        listFolder.add(file);
                    }
                }
                //转换为foreach
                for (File file : listFile) {
                    sendFile(file);
                }
                for (File file : listFolder) {
                    sendFolder(file);
                }
            }

            private void sendFile(File file) {
                byte[] sendBuffer = new byte[BUF_LEN];
                int length;
                try {
                    dosWithPeer.writeUTF("sendFile");
                    dosWithPeer.writeUTF(file.getName());//发送文件名，用来写
                    //发送文件
                    //每次都要发送length，以判断单个文件开始和结束，否则全部文件会写到一个文件中
                    fileInputStream = new FileInputStream(file);
                    length = fileInputStream.read(sendBuffer, 0, sendBuffer.length);
                    while (length > 0) {
                        dosWithPeer.writeInt(length);
                        dosWithPeer.write(sendBuffer, 0, length);
                        dosWithPeer.flush();
                        haveSendLen += length;
                        setTransferRate(haveSendLen, totalLen);
                        length = fileInputStream.read(sendBuffer, 0, sendBuffer.length);
                    }
                    dosWithPeer.writeInt(length);//-1
                    System.out.println("发送方结束循环" + length);

                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("发送文件结束");
                } finally {
                    try {
                        if (fileInputStream != null)
                            fileInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            private void setTransferRate(long haveRecvLen, long folderLen) {
                long rate = ((haveRecvLen * 100) / folderLen);
                ChatClient.getRate().setText("完成:  " + rate + "%");
                ChatClient.getjFrame().validate();
//                dataPanel.getLblInfo().setText("完成:  " + rate +"%");
            }
        };
        new Thread(send).start();
    }

    public void receiveRunnable() {
        Runnable receive = new Runnable() {
            private String finalFileName = "";
            private long haveSendLen = 0L;

            @Override
            public void run() {
                String finalFolderPath = "";
                String subFolder = "";
                long beginTime = 0L;
                long endTime;
                try {
                    lock.lock();
                    try {
                        condition.await();
                        finalFolderPath = folderPath;//接收时手动窗口选择的顶层路径
                    } finally {
                        lock.unlock();
                    }
                    ChatClient.getjPanel().remove(ChatClient.getReceive());
                    addRateCancel();
                    dosWithPeer.writeUTF("Agree");
                    totalLen = disWithPeer.readLong();
                    beginTime = System.currentTimeMillis();
                    while (true) {
                        String firstRead = disWithPeer.readUTF();
                        if (firstRead.equals("sendFile")) {
                            receiveFile(finalFolderPath);//仅文件
                        } else if (firstRead.equals("sendFolder")) {
                            subFolder = disWithPeer.readUTF();//发送方的selectFolderPath子目录
                            finalFolderPath = folderPath + File.separator + subFolder;//用于创建文件夹，传入receiveFile作为参数
                            //生成子目录
                            File file = new File(finalFolderPath);
                            file.mkdirs();
                            //记录文件名，用于取消
                            if (firstTime) {
                                finalFileName = subFolder;
                                firstTime = false;
                            }
                        } else if (firstRead.equals("endTransmit")) {
                            break;
                        }
//                        if (firstTime) {
//                            finalFileName = firstRead.equals("sendFile")? folderName : subFolder;
//                            firstTime = false;
//                        }
                    }
                    endTime = System.currentTimeMillis();
                    useTime = getUseTime(endTime - beginTime);
                    speed = getSpeed(endTime - beginTime, totalLen);
                    ChatClient.appendChatMsg("文件【" + finalFileName + "】发送完毕, 传送用时: "
                            + useTime + ",速度: " + speed
                            + " !\n");
                    removeRateCancel();
                    firstTime = true;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    System.out.println("同步锁");
                    e.printStackTrace();
                }
            }

            private void receiveFile(String finalFolderPath) {
                byte[] receiveBuffer = new byte[BUF_LEN];
                int length;
                try {
                    folderName = disWithPeer.readUTF();//得到要写的文件的文件名
                    //记录文件名，用于取消
                    if (firstTime) {
                        finalFileName = folderName;
                        firstTime = false;
                    }
                    //传入的finalFilePath已经包含了子文件夹，加上文件名即可
                    String finalFilePath = finalFolderPath + File.separator + folderName;
                    fileOutputStream = new FileOutputStream(new File(finalFilePath));
                    length = disWithPeer.readInt();
                    while (length > 0) {
                        disWithPeer.readFully(receiveBuffer, 0, length);//read到length才返回，若用read，可能不到length就返回
                        fileOutputStream.write(receiveBuffer, 0, length);
                        fileOutputStream.flush();
                        haveSendLen += length;
                        setTransferRate(haveSendLen, totalLen);
                        length = disWithPeer.readInt();
                    }
                    System.out.println("接收方结束循环");
                } catch (IOException e) {
                    System.out.println("文件传输流关闭");
                    ChatClient.appendChatMsg("文件 " + finalFileName + " 取消传送\n");
                    removeRateCancel();
                    ChatClient.getjPanel().add(ChatClient.getReceive(), new PropertiesGBC(2, 1, 1, 1).
                            setAnchor(PropertiesGBC.EAST).setWeight(0, 0).setInsets(0, 5, 5, 5));
                    ChatClient.getjFrame().validate();//刷新
//            e.printStackTrace();
                } finally {
                    try {
                        if (fileOutputStream != null)
                            fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            private void setTransferRate(long haveRecvLen, long folderLen) {
                long rate = ((haveRecvLen * 100) / folderLen);
                ChatClient.getRate().setText("完成:  " + rate + "%");
                ChatClient.getjFrame().validate();
//                dataPanel.getLblInfo().setText("完成:  " + rate +"%");
            }
        };
        new Thread(receive).start();
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
    private String clientsListPath = "";

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
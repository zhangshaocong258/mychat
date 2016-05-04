package com.szl.server;

import com.szl.utils.Dom4jXML;
import com.szl.utils.OperateXML;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

import com.szl.utils.PropertiesGBC;
import org.dom4j.Document;
import org.dom4j.Element;


/**
 * Created by zsc on 2015/3/9.
 * <p>
 * 创建ChatServer对象、带有监听事件的Swing、Frame初始化、监听类、main函数
 */

public class ChatServerFrame {
    private ChatServer chatServer = new ChatServer();

    //固定的
    private JButton login = new JButton("启动");
    private JLabel record = new JLabel("记录");
    private JLabel online = new JLabel("在线用户列表");

    public static void main(String[] args) {
        new ChatServerFrame().init();
    }

    private void init() {
        JPanel jPanel = new JPanel();
        jPanel.setBorder(BorderFactory.createTitledBorder("服务器窗口"));
        jPanel.setLayout(new GridBagLayout());

        chatServer.getClientRecord().setEditable(false);
        chatServer.getOnlineCount().setEditable(false);
        //设置大小，防止出现突然变大错误
        chatServer.getClientRecordJScrollPane().setPreferredSize(new Dimension(2000, 1000));
        /**
         * 总体窗格3*3，JTextArea占2*1，
         */
        //启动
        jPanel.add(login, new PropertiesGBC(0, 0, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(0, 0).setInsets(0, 5, 5, 5));

        //记录
        jPanel.add(record, new PropertiesGBC(0, 1, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(0, 0).setInsets(0, 5, 5, 5));

        //JTextArea
        jPanel.add(chatServer.getClientRecordJScrollPane(), new PropertiesGBC(0, 2, 2, 1).
                setFill(PropertiesGBC.BOTH).setWeight(1, 1).setInsets(0, 5, 5, 5));

        //在线用户列表
        jPanel.add(online, new PropertiesGBC(2, 0, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(0, 0).setInsets(0, 5, 5, 5));

        //在线人数JTextArea
        jPanel.add(chatServer.getOnlineCountJScrollPane(), new PropertiesGBC(2, 1, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(0.5, 0).setInsets(0, 5, 5, 5));

        //JList_JScrollPane
        jPanel.add(chatServer.getClientJScrollPane(), new PropertiesGBC(2, 2, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(0.5, 1).setInsets(0, 5, 5, 5));

        JFrame jFrame = new JFrame("服务器");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setSize(new Dimension(300, 400));
        jFrame.add(jPanel);
        jFrame.setResizable(true);
        jFrame.setVisible(true);

        login.addActionListener(new loginListener());
    }

    private class loginListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            new Thread(chatServer.new Server()).start();
            login.setEnabled(false);
        }
    }
}

/**
 * 启动socket连接，定义发送和接受信息，定义关闭流方法close，封装发送和接受方法
 */
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
    }

    public String receiveData() throws IOException {
        return disWithClient.readUTF();
    }
}

/**
 * 用户List的封装，封装了发送信息包装方法，添加用户和删除用户
 */
class UserClientList {
    private static List<UserClient> clients = new ArrayList<>();

    public void addClients(UserClient userClient) {
        clients.add(userClient);
    }

    public void removeClients(UserClient userClient) {
        clients.remove(userClient);
    }

    public void sendMsg(String data, String NamePort) throws IOException {
        for (UserClient client : clients) {
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

/**
 * 用户信息类，拆分从客户端接收的信息，初始化Map，用于建立name&port发送给客户端，封装buildMsg
 */
class UserClientMsg {
    private String name = "";
    private String port = "";
    private static Map<String, String> clientInfo = new HashMap<>();
    private String DELIMITER = "\f";

    UserClientMsg(String data_from_client) {
        java.util.List<String> data_from_client_split = Arrays.asList(data_from_client.split(DELIMITER));
        this.name = data_from_client_split.get(0);
        this.port = data_from_client_split.get(1);
    }

    public String getName() {
        return name;
    }

    public String getPort() {
        return port;
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
                String SEPARATOR = "\r";
                name_port = name_port.append(entry.getKey()).append(SEPARATOR).append(entry.getValue()).append(DELIMITER);
            }
            return name_port.deleteCharAt(name_port.length() - 1).toString();
        } else return null;
    }

    public String buildMsg(String name, String port) {
        return name + DELIMITER + port;
    }
}

/**
 * 主服务，初始化监听的Swing
 */
class ChatServer {
    private UserClientList userClientList = new UserClientList();//用户List,用于维护当前用户，给他们发送信息
    private ServerDom4j serverDom4j;//启动按钮按下时再初始化

    //上下线记录
    private JTextArea clientRecord = new JTextArea();
    private JScrollPane clientRecordJScrollPane = new JScrollPane(clientRecord);

    //在线人数
    private static JTextArea onlineCount = new JTextArea("在线人数");
    private JScrollPane onlineCountJScrollPane = new JScrollPane(onlineCount);

    private static DefaultListModel<String> clientListModel = new DefaultListModel<>();
    private JList<String> clientList = new JList<>(clientListModel);
    private JScrollPane clientJScrollPane = new JScrollPane(clientList);

    public JTextArea getOnlineCount() {
        return onlineCount;
    }

    public JScrollPane getOnlineCountJScrollPane() {
        return onlineCountJScrollPane;
    }

    public JTextArea getClientRecord() {
        return clientRecord;
    }

    public JScrollPane getClientRecordJScrollPane() {
        return clientRecordJScrollPane;
    }

    public JScrollPane getClientJScrollPane() {
        return clientJScrollPane;
    }

    public void addClientListModelElement(String str) {
        clientListModel.addElement(str);
    }

    public void removeClientListModelElement(String str) {
        clientListModel.removeElement(str);
    }

    public void addClientRecord(String str) {
        clientRecord.append(str);
    }

    //启动服务端
    class Server implements Runnable {
        private ServerSocket serverSocket = null;
        private UserClient userClient;
        private boolean start = false;

        public void run() {
            serverDom4j = new ServerDom4j();
            try {
                serverSocket = new ServerSocket(8887);
                start = true;
            } catch (BindException e) {
                System.out.println("端口使用中...");
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                while (start) {
                    Socket socket = serverSocket.accept();
                    userClient = new UserClient(socket);
                    ReceiveMsg client = new ReceiveMsg(userClient);
                    userClientList.addClients(userClient);
                    System.out.println("一个客户端已连接！");
                    new Thread(client).start();
                }
            } catch (IOException e) {
                System.out.println("服务端错误位置");
                e.printStackTrace();
            } finally {
                try {
                    serverSocket.close();
                    start = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 接受客户端信息，不管是注册信息还是普通信息，注册信息只有name&port，普通信息有name&port&str，
     * 直接连同namePort组成的list字符串发送回去；
     * 客户端下线时，catch Exception，发送的信息为name&port，和客户端登录时发送的信息格式一样，不能包含str
     */
    class ReceiveMsg implements Runnable {
        private boolean isConnected = false;
        private UserClient userClient;
        private UserClientMsg userClientMsg;
        private Map<String, String> clientInfo = new HashMap<>();
        private String dataFromClient = "";
        private String namePort = "";
        private String name = "";
        private String port = "";

        ReceiveMsg(UserClient userClient) {
            this.userClient = userClient;
            isConnected = true;
        }

        //catch由3变1
        public void run() {
            try {
                while (isConnected) {
                    dataFromClient = userClient.receiveData();
                    userClientMsg = new UserClientMsg(dataFromClient);
                    clientInfo = userClientMsg.putClientInfo();//客户端名字端口信息
                    name = userClientMsg.getName();
                    port = userClientMsg.getPort();
                    namePort = userClientMsg.buildNamePort(clientInfo);
                    boolean flag = false;
                    System.out.println("用户数" + userClientMsg.getClientInfo().size());

                    for (int j = 0; j < clientListModel.getSize(); j++) {
                        if (clientListModel.getElementAt(j).equals(name)) flag = true;
                    }
                    if (!flag) {
                        addClientListModelElement(name);
                        addClientRecord(name + "已上线" + "\n");

                        serverDom4j.createElement(name, port);
                        serverDom4j.saveXML(ServerDom4j.getClientDocument(), serverDom4j.getClientsListPath());
                        serverDom4j.createRecord(clientRecord.getText());
                        serverDom4j.saveXML(ServerDom4j.getRecordDocument(), serverDom4j.getServerRecordPath());

                    }
                    userClientList.sendMsg(dataFromClient, namePort);//发送从客户端发来的信息，以及封装的用户名_端口
                    onlineCount.setText("在线人数" + ": " + clientListModel.getSize());
                    System.out.println("接收到的数据" + dataFromClient);
                }
            } catch (IOException e) {
//                e.printStackTrace();
                userClientList.removeClients(this.userClient);//List删除下线用户
                removeClientListModelElement(this.name);//Frame中删除下线用户
                onlineCount.setText("在线人数" + ": " + clientListModel.getSize());//在线人数减一
                clientInfo = userClientMsg.removeClientInfo(this.name);//Map中删除name_port，向其他用户发送信息
                namePort = userClientMsg.buildNamePort(clientInfo);//建立name_port,发送
                addClientRecord(name + "已下线" + "\n");

                serverDom4j.deleteElement(name);
                serverDom4j.saveXML(ServerDom4j.getClientDocument(), serverDom4j.getClientsListPath());
                serverDom4j.createRecord(clientRecord.getText());
                serverDom4j.saveXML(ServerDom4j.getRecordDocument(), serverDom4j.getServerRecordPath());

                //掉线后模仿登录时发送的信息格式
                if (clientListModel.getSize() != 0) {
                    try {
                        String dataFromClientWithoutStr = userClientMsg.buildMsg(userClientMsg.getName(), userClientMsg.getPort());
                        userClientList.sendMsg(dataFromClientWithoutStr, namePort);//发送的str为"",客户端进行判断
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                System.out.println("客户端关闭1");
//            } catch (EOFException e) {
//                userClientList.removeClients(this.userClient);
//                removeClientListModelElement(this.name);
//                System.out.println("客户端关闭2");
//            } catch (IOException e) {
//                System.out.println("客户端关闭3");
            } finally {
                try {
                    this.userClient.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}

/**
 * 操作XML进行记录，创建2个XML文件分别保存Record和ClientsList
 */
//class ServerOperateXML extends OperateXML {
//    private static Document clientDocument;
//
//    private Element clientsListRoot;
//
//    private String clientsListPath;
//    private String serverRecordPath;
//
//    //初始化直接生成空文件
//    public ServerOperateXML() {
//        clientDocument = initDocument();
//        clientsListRoot = clientDocument.createElement("content");
//        clientDocument.appendChild(clientsListRoot);
//
//        this.clientsListPath = "D:/ClientsList.xml";
//        this.serverRecordPath = "D:/ServerRecord.xml";
//
//        saveXML(clientDocument, clientsListPath);
//        saveXML(getRecordDocument(), serverRecordPath);
//
//    }
//
//    public static Document getClientDocument() {
//        return clientDocument;
//    }
//
//    public String getServerRecordPath() {
//        return serverRecordPath;
//    }
//
//    public String getClientsListPath() {
//        return clientsListPath;
//    }
//
//    public void createElement(String clientName, String clientPort) {
//
//        Element client = clientDocument.createElement("clients");
//
//        Element name = clientDocument.createElement("name");
//        name.appendChild(clientDocument.createTextNode(clientName));
//        client.appendChild(name);
//
//        Element port = clientDocument.createElement("port");
//        port.appendChild(clientDocument.createTextNode(clientPort));
//        client.appendChild(port);
//
//        clientsListRoot.appendChild(client);
//    }
//
//    public void deleteElement(String clientName) {
//        NodeList clientsList = clientDocument.getElementsByTagName("clients");
//        //列出每一个clients的NodeList
//        for (int i = 0; i < clientsList.getLength(); i++) {
//            NodeList clientsChildList = clientsList.item(i).getChildNodes();
//            if (clientsChildList.item(0).getTextContent().trim().equals(clientName)) {
//                clientsList.item(i).getParentNode().removeChild(clientsList.item(i));
//            }
//        }
//    }
//}

class ServerDom4j extends Dom4jXML {
    private static Document clientDocument;

    private Element clientsListRoot;

    private String clientsListPath;
    private String serverRecordPath;

    //初始化直接生成空文件
    public ServerDom4j() {
        clientDocument = initDocument();
        clientsListRoot = clientDocument.addElement("content");

        this.clientsListPath = "D:/ClientsList.xml";
        this.serverRecordPath = "D:/ServerRecord.xml";

        saveXML(clientDocument, clientsListPath);
        saveXML(getRecordDocument(), serverRecordPath);
    }

    public static Document getClientDocument() {
        return clientDocument;
    }

    public String getServerRecordPath() {
        return serverRecordPath;
    }

    public String getClientsListPath() {
        return clientsListPath;
    }

    public void createElement(String clientName, String clientPort) {
        Element client = clientsListRoot.addElement("clients");

        Element name = client.addElement("name");
        name.setText(clientName);
        Element port = client.addElement("port");
        port.setText(clientPort);
    }

    public void deleteElement(String clientName) {
        List<Element> clientsList = clientsListRoot.elements("clients");
        //列出每一个clients的List
        for (int i = 0; i < clientsList.size(); i++) {
            Element nameElement = clientsList.get(i).element("name");
            if (nameElement.getText().equals(clientName)) {
                clientsList.get(i).getParent().remove(clientsList.get(i));
            }
        }
    }
}
package Server;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.xml.sax.SAXException;


/**
 * Created by zsc on 2015/3/9.
 */

/**
 * 创建ChatServer对象、带有监听事件的Swing、Frame初始化、监听类、main函数
 */
public class ChatServerFrame {

    private ChatServer chatServer = new ChatServer();

    private JFrame jFrame = new JFrame();

    //固定的
    private JButton login = new JButton("启动");
    private JLabel record = new JLabel("记录");
    private JLabel online = new JLabel("在线用户列表");

    public static void main(String[] args) {
        new ChatServerFrame().init();
    }

    public void init() {
        jFrame.setTitle("服务端");

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));

        JPanel leftPanel = new JPanel();
        chatServer.getJScrollPane().setPreferredSize(new Dimension(150, 300));

        JPanel leftTop = new JPanel();
        leftTop.setLayout(new BoxLayout(leftTop, BoxLayout.X_AXIS));
        leftTop.add(Box.createRigidArea(new Dimension(5, 0)));
        leftTop.add(login);
        leftTop.add(Box.createGlue());

        JPanel leftMiddle = new JPanel();
        leftMiddle.setLayout(new BoxLayout(leftMiddle, BoxLayout.X_AXIS));
        leftMiddle.add(Box.createRigidArea(new Dimension(5, 0)));
        leftMiddle.add(record);
        leftMiddle.add(Box.createGlue());

        JPanel leftBottom = new JPanel();
        leftBottom.setLayout(new BoxLayout(leftBottom, BoxLayout.X_AXIS));
        leftBottom.add(Box.createRigidArea(new Dimension(5, 0)));
        leftBottom.add(chatServer.getJScrollPane());
        leftBottom.add(Box.createGlue());

        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(leftTop);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(leftMiddle);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftPanel.add(leftBottom);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));


        JPanel rightPanel = new JPanel();
        chatServer.getClientJScrollPane().setPreferredSize(new Dimension(100, 300));


        JPanel rightTop = new JPanel();
        rightTop.setLayout(new BoxLayout(rightTop, BoxLayout.X_AXIS));
        rightTop.add(online);
        rightTop.add(Box.createGlue());

        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(rightTop);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        rightPanel.add(chatServer.getOnlineCount());
        rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        rightPanel.add(chatServer.getClientJScrollPane());
        rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        jPanel.add(leftPanel);
        jPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        jPanel.add(rightPanel);


        jFrame = new JFrame("服务器");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setSize(new Dimension(280, 350));
        jFrame.add(jPanel);
        jFrame.setResizable(false);
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
    private static java.util.List<UserClient> clients = new ArrayList<>();

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

    //上下线记录
    private static DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> chatList = new JList<>(listModel);
    private JScrollPane jScrollPane = new JScrollPane(chatList);

    //在线人数
    private static JTextField onlineCount = new JTextField("在线人数");

    private static DefaultListModel<String> clientListModel = new DefaultListModel<>();
    private JList<String> clientList = new JList<>(clientListModel);
    private JScrollPane clientJScrollPane = new JScrollPane(clientList);

    public JTextField getOnlineCount() {
        return onlineCount;
    }

    public JScrollPane getJScrollPane() {
        return jScrollPane;
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

    public void addListModelElement(String str) {
        listModel.addElement(str);
        chatList.setModel(listModel);
    }

    private OperateXML operateXML = new OperateXML();


    //启动服务端
    class Server implements Runnable {
        private ServerSocket serverSocket = null;
        private UserClient userClient;

        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
            } catch (BindException e) {
                System.out.println("端口使用中...");
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                while (true) {
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
                        addListModelElement(name + "已上线");

                        operateXML.createElement(name, port);
                        operateXML.createRecord(name + "已上线");
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
                    userClientList.sendMsg(dataFromClient, namePort);//发送从客户端发来的信息，以及封装的用户名_端口
                    onlineCount.setText("在线人数" + ": " + clientListModel.getSize());
                    System.out.println("接收到的数据" + dataFromClient);
                }
            } catch (SocketException e) {
//                e.printStackTrace();
                userClientList.removeClients(this.userClient);//List删除下线用户
                removeClientListModelElement(this.name);//Frame中删除下线用户
                onlineCount.setText("在线人数" + ": " + clientListModel.getSize());//在线人数减一
                clientInfo = userClientMsg.removeClientInfo(this.name);//Map中删除name_port，向其他用户发送信息
                namePort = userClientMsg.buildNamePort(clientInfo);//建立name_port,发送
                addListModelElement(name + "已下线");

                operateXML.deleteElement(name);
                operateXML.createRecord(name + "已下线");

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
            } catch (EOFException e) {
                userClientList.removeClients(this.userClient);
                removeClientListModelElement(this.name);
                System.out.println("客户端关闭2");
            } catch (IOException e) {
                System.out.println("客户端关闭3");
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
class OperateXML {
    private static Document clientDocument;
    private static Document recordDocument;

    private Element clientsListRoot;
    private Element recordRoot;
    private TransformerFactory transformerFactory = TransformerFactory.newInstance();

    public OperateXML(){
        clientDocument = initDocument();
        recordDocument = initDocument();
        clientsListRoot = clientDocument.createElement("content");
        recordRoot = recordDocument.createElement("content");
        clientDocument.appendChild(clientsListRoot);
        recordDocument.appendChild(recordRoot);

    }

    public Document initDocument() {
        Document document;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        document = builder.newDocument();
        return document;

    }

//    public Element initElement(){
//        Element element;
//        element = document.createElement("content");
//        document.appendChild(element);
//        return element;
//    }

    public void createElement(String clientName,String clientPort){

        Element client = clientDocument.createElement("clients");

        Element name = clientDocument.createElement("name");
        name.appendChild(clientDocument.createTextNode(clientName));
        client.appendChild(name);

        Element port = clientDocument.createElement("port");
        port.appendChild(clientDocument.createTextNode(clientPort));
        client.appendChild(port);

        clientsListRoot.appendChild(client);


        saveXML(clientDocument,"D:/ClientsList.xml");
    }

    public void deleteElement(String clientName){
        NodeList clientsList = clientDocument.getElementsByTagName("clients");
        //列出每一个clients的NodeList
        for(int i = 0;i< clientsList.getLength();i++){
            NodeList clientsChildList = clientsList.item(i).getChildNodes();
            if(clientsChildList.item(0).getTextContent().trim().equals(clientName)){
                clientsList.item(i).getParentNode().removeChild(clientsList.item(i));
            }
        }

        saveXML(clientDocument,"D:/ClientsList.xml");
    }

    public void createRecord(String chatRecord){
        Element record = recordDocument.createElement("record");
        record.appendChild((recordDocument.createTextNode(chatRecord)));
        recordRoot.appendChild(record);

        saveXML(recordDocument, "D:/ServerRecord.xml");
    }

    public void saveXML(Document document,String path) {
        try {
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            PrintWriter printWriter = new PrintWriter(new FileOutputStream(path));
            StreamResult result = new StreamResult(printWriter);
            transformer.transform(source, result);     //关键转换
            System.out.println("生成XML文件成功!");
        } catch (IllegalArgumentException | FileNotFoundException | TransformerException e) {
            System.out.println(e.getMessage());
        }
    }

}
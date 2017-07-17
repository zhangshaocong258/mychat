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
 *
 * ����ChatServer���󡢴��м����¼���Swing��Frame��ʼ���������ࡢmain����
 */

public class ChatServerFrame {
    private ChatServer chatServer = new ChatServer();

    //�̶���
    private JButton login = new JButton("����");
    private JLabel record = new JLabel("��¼");
    private JLabel online = new JLabel("�����û��б�");

    public static void main(String[] args) {
        new ChatServerFrame().init();
    }

    private void init() {
        JPanel jPanel = new JPanel();
        jPanel.setBorder(BorderFactory.createTitledBorder("����������"));
        jPanel.setLayout(new GridBagLayout());

        chatServer.getClientRecord().setEditable(false);
        chatServer.getOnlineCount().setEditable(false);
        //���ô�С����ֹ����ͻȻ������
        chatServer.getClientRecordJScrollPane().setPreferredSize(new Dimension(2000, 1000));
        /**
         * ���崰��3*3��JTextAreaռ2*1��
         */
        //����
        jPanel.add(login, new PropertiesGBC(0, 0, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(0, 0).setInsets(0, 5, 5, 5));

        //��¼
        jPanel.add(record, new PropertiesGBC(0, 1, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(0, 0).setInsets(0, 5, 5, 5));

        //JTextArea
        jPanel.add(chatServer.getClientRecordJScrollPane(), new PropertiesGBC(0, 2, 2, 1).
                setFill(PropertiesGBC.BOTH).setWeight(1, 1).setInsets(0, 5, 5, 5));

        //�����û��б�
        jPanel.add(online, new PropertiesGBC(2, 0, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(0, 0).setInsets(0, 5, 5, 5));

        //��������JTextArea
        jPanel.add(chatServer.getOnlineCountJScrollPane(), new PropertiesGBC(2, 1, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(0.5, 0).setInsets(0, 5, 5, 5));

        //JList_JScrollPane
        jPanel.add(chatServer.getClientJScrollPane(), new PropertiesGBC(2, 2, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(0.5, 1).setInsets(0, 5, 5, 5));

        JFrame jFrame = new JFrame("������");
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
 * ����socket���ӣ����巢�ͺͽ�����Ϣ������ر�������close����װ���ͺͽ��ܷ���
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
 * �û�List�ķ�װ����װ�˷�����Ϣ��װ����������û���ɾ���û�
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
 * �û���Ϣ�࣬��ִӿͻ��˽��յ���Ϣ����ʼ��Map�����ڽ���name&port���͸��ͻ��ˣ���װbuildMsg
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
 * �����񣬳�ʼ��������Swing
 */
class ChatServer {
    private UserClientList userClientList = new UserClientList();//�û�List,����ά����ǰ�û��������Ƿ�����Ϣ
    private ServerDom4j serverDom4j;//������ť����ʱ�ٳ�ʼ��

    //�����߼�¼
    private JTextArea clientRecord = new JTextArea();
    private JScrollPane clientRecordJScrollPane = new JScrollPane(clientRecord);

    //��������
    private static JTextArea onlineCount = new JTextArea("��������");
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

    //���������
    class Server implements Runnable {
        private ServerSocket serverSocket = null;
        private UserClient userClient;
        private boolean start = false;

        public void run() {
            serverDom4j = new ServerDom4j();
            try {
                serverSocket = new ServerSocket(30000);
                start = true;
            } catch (BindException e) {
                System.out.println("�˿�ʹ����...");
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
                    System.out.println("һ���ͻ��������ӣ�");
                    new Thread(client).start();
                }
            } catch (IOException e) {
                System.out.println("����˴���λ��");
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
     * ���ܿͻ�����Ϣ��������ע����Ϣ������ͨ��Ϣ��ע����Ϣֻ��name&port����ͨ��Ϣ��name&port&str��
     * ֱ����ͬnamePort��ɵ�list�ַ������ͻ�ȥ��
     * �ͻ�������ʱ��catch Exception�����͵���ϢΪname&port���Ϳͻ��˵�¼ʱ���͵���Ϣ��ʽһ�������ܰ���str
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

        //catch��3��1
        public void run() {
            try {
                while (isConnected) {
                    dataFromClient = userClient.receiveData();
                    userClientMsg = new UserClientMsg(dataFromClient);
                    clientInfo = userClientMsg.putClientInfo();//�ͻ������ֶ˿���Ϣ
                    name = userClientMsg.getName();
                    port = userClientMsg.getPort();
                    namePort = userClientMsg.buildNamePort(clientInfo);
                    boolean flag = false;
                    System.out.println("�û���" + userClientMsg.getClientInfo().size());

                    for (int j = 0; j < clientListModel.getSize(); j++) {
                        if (clientListModel.getElementAt(j).equals(name)) flag = true;
                    }
                    if (!flag) {
                        addClientListModelElement(name);
                        addClientRecord(name + "������" + "\n");

                        serverDom4j.createElement(name, port);
                        serverDom4j.saveXML(ServerDom4j.getClientDocument(), serverDom4j.getDirPath(), serverDom4j.getClientsListPath());
                        serverDom4j.createRecord(clientRecord.getText());
                        serverDom4j.saveXML(ServerDom4j.getRecordDocument(), serverDom4j.getDirPath(), serverDom4j.getServerRecordPath());

                    }
                    userClientList.sendMsg(dataFromClient, namePort);//���ʹӿͻ��˷�������Ϣ���Լ���װ���û���_�˿�
                    onlineCount.setText("��������" + ": " + clientListModel.getSize());
                    System.out.println("���յ�������" + dataFromClient);
                }
            } catch (IOException e) {
//                e.printStackTrace();
                userClientList.removeClients(this.userClient);//Listɾ�������û�
                removeClientListModelElement(this.name);//Frame��ɾ�������û�
                onlineCount.setText("��������" + ": " + clientListModel.getSize());//����������һ
                clientInfo = userClientMsg.removeClientInfo(this.name);//Map��ɾ��name_port���������û�������Ϣ
                namePort = userClientMsg.buildNamePort(clientInfo);//����name_port,����
                addClientRecord(name + "������" + "\n");

                serverDom4j.deleteElement(name);
                serverDom4j.saveXML(ServerDom4j.getClientDocument(), serverDom4j.getDirPath(), serverDom4j.getClientsListPath());
                serverDom4j.createRecord(clientRecord.getText());
                serverDom4j.saveXML(ServerDom4j.getRecordDocument(), serverDom4j.getDirPath(), serverDom4j.getServerRecordPath());

                //���ߺ�ģ�µ�¼ʱ���͵���Ϣ��ʽ
                if (clientListModel.getSize() != 0) {
                    try {
                        String dataFromClientWithoutStr = userClientMsg.buildMsg(userClientMsg.getName(), userClientMsg.getPort());
                        userClientList.sendMsg(dataFromClientWithoutStr, namePort);//���͵�strΪ"",�ͻ��˽����ж�
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                System.out.println("�ͻ��˹ر�1");
//            } catch (EOFException e) {
//                userClientList.removeClients(this.userClient);
//                removeClientListModelElement(this.name);
//                System.out.println("�ͻ��˹ر�2");
//            } catch (IOException e) {
//                System.out.println("�ͻ��˹ر�3");
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
 * ����XML���м�¼������2��XML�ļ��ֱ𱣴�Record��ClientsList
 */
//class ServerOperateXML extends OperateXML {
//    private static Document clientDocument;
//
//    private Element clientsListRoot;
//
//    private String clientsListPath;
//    private String serverRecordPath;
//
//    //��ʼ��ֱ�����ɿ��ļ�
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
//        //�г�ÿһ��clients��NodeList
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

    private String dirPath;
    private String clientsListPath;
    private String serverRecordPath;

    //��ʼ��ֱ�����ɿ��ļ�
    public ServerDom4j() {
        clientDocument = initDocument();
        clientsListRoot = clientDocument.addElement("content");

        this.dirPath = "D:/ChatServer";
        this.clientsListPath = dirPath + File.separator + "ClientsList.xml";
        this.serverRecordPath = dirPath + File.separator + "ServerRecord.xml";

        saveXML(clientDocument, dirPath, clientsListPath);
        saveXML(getRecordDocument(), dirPath, serverRecordPath);
    }

    public static Document getClientDocument() {
        return clientDocument;
    }

    public String getDirPath() {
        return dirPath;
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
        //�г�ÿһ��clients��List
        for (int i = 0; i < clientsList.size(); i++) {
            Element nameElement = clientsList.get(i).element("name");
            if (nameElement.getText().equals(clientName)) {
                clientsList.get(i).getParent().remove(clientsList.get(i));
            }
        }
    }
}
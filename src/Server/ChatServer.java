package Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;


/**
 * Created by zsc on 2015/3/9.
 */
public class ChatServer {

    private ServerFrame serverFrame = new ServerFrame();
    private UserClientList userClientList = new UserClientList();//�û�List,����ά����ǰ�û��������Ƿ�����Ϣ

    public static void main(String[] args) {
        new ChatServer().init();
    }

    private void init() {
        serverFrame.init();
    }

    //���������
    class Server implements Runnable {
        private ServerSocket serverSocket = null;
        private UserClient userClient;
        boolean start = false;

        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
                start = true;
            } catch (BindException e) {
                System.out.println("�˿�ʹ����");
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

    //���ܿͻ�����Ϣ
    class ReceiveMsg implements Runnable {
        private boolean bconnected;
        private UserClient userClient;
        private UserClientMsg userClientMsg;
        private Map<String, String> clientInfo = new HashMap<String, String>();
        private String data_from_client;
        private String name_port;
        private String name;
        private String port;

        ReceiveMsg(UserClient userClient) {
            this.userClient = userClient;
            bconnected = true;
        }

        public void run() {
            try {
                while (bconnected) {
                    data_from_client = userClient.receiveData();
                    userClientMsg = new UserClientMsg(data_from_client);
                    clientInfo = userClientMsg.putClientInfo();//
                    name = userClientMsg.getName();
                    port = userClientMsg.getPort();

                    name_port = userClientMsg.buildNamePort(clientInfo);
                    boolean flag = false;
                    System.out.println("�û���" + userClientMsg.getClientInfo().size());

                    for (int j = 0; j < serverFrame.getClientListModel().getSize(); j++) {
                        if (serverFrame. getClientListModel().getElementAt(j).equals(name)) flag = true;
                    }
                    if (!flag) {
                        serverFrame.addClientListModelElement(name);
                        serverFrame.addListModelElement(name + "������");
                    }
//                    for(int k =0;k<clientList.getItemCount();k++){
//                        name_port =name_port.append(clientList.getItem(k)).append(SEPARATOR).append(clientInfo.get(clientList.getItem(k))).append(DELIMITER);
//                    }
//                    String name_and_port=name_port.deleteCharAt(name_port.length()-1).toString();
//                    for(Map.Entry<String,String> entry : clientInfo.entrySet()){
//                        System.out.println("��Ϣlist" + entry.getKey() + "    " + entry.getValue());
//                    }
//                    for(int k =0;k<serverFrame.getClientList().getItemCount();k++){
//                        System.out.println("��Ϣlist" + serverFrame.getClientList().getItem(k));
//                    }
                    userClientList.sendMsg(data_from_client, name_port);//���ʹӿͻ��˷�������Ϣ���Լ���װ���û���_�˿�
                    serverFrame.getOnlineCount().setText("��������" + ": " + serverFrame.getClientListModel().getSize());
                    System.out.println("���յ�����" + data_from_client);
                }
            } catch (SocketException e) {
                userClientList.removeClients(this.userClient);//Listɾ�������û�
                serverFrame.removeClientListModelElement(this.name);//Frame��ɾ�������û�
                serverFrame.getOnlineCount().setText("��������" + ": " + serverFrame.getClientListModel().getSize());//����������һ
                clientInfo = userClientMsg.removeClientInfo(this.name);//Map��ɾ��name_port���������û�������Ϣ
                name_port = userClientMsg.buildNamePort(clientInfo);//����name_port,����
                serverFrame.addListModelElement(name + "������");

                if (serverFrame.getClientListModel().getSize() != 0) {
                    try {
                        userClientList.sendMsg(data_from_client, name_port);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                System.out.println("Client closed0");
            } catch (EOFException e) {
                userClientList.removeClients(this.userClient);
                serverFrame.removeClientListModelElement(this.name);
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

//����socket���ӣ����巢�ͺͽ�����Ϣ
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

//�û�List�ķ�װ����װ�˷�����Ϣ����
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

//�û���Ϣ��
class UserClientMsg {
    private String name = "";
    private String port = "";
    private static Map<String, String> clientInfo = new HashMap<String, String>();
    private String DELIMITER = "\f";
    private String SEPARATOR = "\r";

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
                name_port = name_port.append(entry.getKey()).append(SEPARATOR).append(entry.getValue()).append(DELIMITER);
            }
            String NamePort = name_port.deleteCharAt(name_port.length() - 1).toString();
            return NamePort;
        } else return null;
    }

    public String buildMsg(String name, String port, String str) {
        String buildMsg = name + DELIMITER + port + DELIMITER + str;
        return buildMsg;
    }
}

//������ʾ
class ServerFrame {
    private JFrame jFrame = new JFrame();

    //�����߼�¼
    private static DefaultListModel listModel = new DefaultListModel();
    private JList chatList = new JList(listModel);
    private JScrollPane jScrollPane = new JScrollPane(chatList);

    //��������
    private static JTextField onlineCount = new JTextField("��������");

    private static DefaultListModel clientListModel = new DefaultListModel();
    private JList clientList = new JList(clientListModel);
    private JScrollPane clientJScrollPane = new JScrollPane(clientList);

    //�̶���
    private JButton login = new JButton("����");
    private JLabel record = new JLabel("��¼");
    private JLabel online = new JLabel("�����û��б�");

    public JTextField getOnlineCount() {
        return onlineCount;
    }

    public DefaultListModel getClientListModel(){
        return clientListModel;
    }

    public void addClientListModelElement(String str) {
        clientListModel.addElement(str);
    }

    public void removeClientListModelElement(String str){
        clientListModel.removeElement(str);
    }

    public void addListModelElement(String str){
        listModel.addElement(str);
    }

    public void init() {
        jFrame.setTitle("�����");

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));

        JPanel leftPanel = new JPanel();
        jScrollPane.setSize(new Dimension(30,220));

        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(login);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(record);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftPanel.add(jScrollPane);


        JPanel rightPanel = new JPanel();
        clientJScrollPane.setPreferredSize(new Dimension(30,220));

        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(Box.createRigidArea(new Dimension(10, 10)));
        rightPanel.add(online);
        rightPanel.add(Box.createRigidArea(new Dimension(10, 5)));
        rightPanel.add(onlineCount);
        rightPanel.add(Box.createRigidArea(new Dimension(10, 5)));
        rightPanel.add(clientJScrollPane);

        jPanel.add(leftPanel);
        jPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        jPanel.add(rightPanel);


        jFrame = new JFrame("������");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setSize(new Dimension(220, 320));
        jFrame.add(jPanel);
        jFrame.setResizable(false);
        jFrame.setVisible(true);

        login.addActionListener(new loginListener());
    }

    private class loginListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            new Thread(new ChatServer().new Server()).start();
            login.setEnabled(false);
        }
    }
}

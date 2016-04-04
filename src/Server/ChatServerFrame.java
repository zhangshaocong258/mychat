package Server;

import sun.plugin.javascript.JSClassLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;


/**
 * Created by zsc on 2015/3/9.
 */
public class ChatServerFrame {

    private ChatServer chatServer = new ChatServer();

    private JFrame jFrame = new JFrame();

    //�̶���
    private JButton login = new JButton("����");
    private JLabel record = new JLabel("��¼");
    private JLabel online = new JLabel("�����û��б�");

    public static void main(String[] args) {
        new ChatServerFrame().init();
    }

    public void init() {
        jFrame.setTitle("�����");

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


        jFrame = new JFrame("������");
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
    }

    public String receiveData() throws IOException {
        return disWithClient.readUTF();
    }
}

//�û�List�ķ�װ����װ�˷�����Ϣ����
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

//�û���Ϣ��
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

//������
class ChatServer{
    private UserClientList userClientList = new UserClientList();//�û�List,����ά����ǰ�û��������Ƿ�����Ϣ

    //�����߼�¼
    private static DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> chatList = new JList<>(listModel);
    private JScrollPane jScrollPane = new JScrollPane(chatList);

    //��������
    private static JTextField onlineCount = new JTextField("��������");

    private static DefaultListModel<String> clientListModel = new DefaultListModel<>();
    private JList<String> clientList = new JList<>(clientListModel);
    private JScrollPane clientJScrollPane = new JScrollPane(clientList);

    public JTextField getOnlineCount(){
        return onlineCount;
    }
    public JScrollPane getJScrollPane(){
        return jScrollPane;
    }

    public JScrollPane getClientJScrollPane(){
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



    //���������
    class Server implements Runnable {
        private ServerSocket serverSocket = null;
        private UserClient userClient;

        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
            } catch (BindException e) {
                System.out.println("�˿�ʹ����...");
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
                    System.out.println("һ���ͻ��������ӣ�");
                    new Thread(client).start();
                }
            } catch (IOException e) {
                System.out.println("����˴���λ��");
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
        private boolean beConnected = false;
        private UserClient userClient;
        private UserClientMsg userClientMsg;
        private Map<String, String> clientInfo = new HashMap<>();
        private String dataFromClient = "";
        private String namePort = "";
        private String name = "";

        ReceiveMsg(UserClient userClient) {
            this.userClient = userClient;
            beConnected = true;
        }

        public void run() {
            try {
                while (beConnected) {
                    dataFromClient = userClient.receiveData();
                    userClientMsg = new UserClientMsg(dataFromClient);
                    clientInfo = userClientMsg.putClientInfo();//�ͻ������ֶ˿���Ϣ
                    name = userClientMsg.getName();
                    namePort = userClientMsg.buildNamePort(clientInfo);
                    boolean flag = false;
                    System.out.println("�û���" + userClientMsg.getClientInfo().size());

                    for (int j = 0; j < clientListModel.getSize(); j++) {
                        if (clientListModel.getElementAt(j).equals(name)) flag = true;
                    }
                    if (!flag) {
                        addClientListModelElement(name);
                        addListModelElement(name + "������");
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
                    userClientList.sendMsg(dataFromClient, namePort);//���ʹӿͻ��˷�������Ϣ���Լ���װ���û���_�˿�
                    onlineCount.setText("��������" + ": " + clientListModel.getSize());
                    System.out.println("���յ�������" + dataFromClient);
                }
            } catch (SocketException e) {
//                e.printStackTrace();
                userClientList.removeClients(this.userClient);//Listɾ�������û�
                removeClientListModelElement(this.name);//Frame��ɾ�������û�
                onlineCount.setText("��������" + ": " + clientListModel.getSize());//����������һ
                clientInfo = userClientMsg.removeClientInfo(this.name);//Map��ɾ��name_port���������û�������Ϣ
                namePort = userClientMsg.buildNamePort(clientInfo);//����name_port,����
                addListModelElement(name + "������");

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
            } catch (EOFException e) {
                userClientList.removeClients(this.userClient);
                removeClientListModelElement(this.name);
                System.out.println("�ͻ��˹ر�2");
            } catch (IOException e) {
                System.out.println("�ͻ��˹ر�3");
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

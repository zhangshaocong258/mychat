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
    ConnectServer connectServer = new ConnectServer();//�ͻ������ӷ����
    ClientData clientData = new ClientData();//�����û���¼��Ϣ����װ��buildMsg��send��receive
    ConnectPeerClient connectPeerClient = new ConnectPeerClient();//�ͻ�����Ϊ�����
    Frame f = new Frame();
//    Socket s = null;
//    Socket socketWithPeer = null;
//    ServerSocket clientServerSocket = null;
//    DataOutputStream dosWithServer = null;
//    DataInputStream disWithServer = null;
//    DataOutputStream peerDos = null;
//    DataInputStream peerDis = null;
//    Map<String, String> clientInfo = new HashMap<>();
//    String name = "";
//    String port = "";
//    String str = "";
//    String peer = "Ⱥ��";
//    String all = "";
//    String DELIMITER = "\f";
//    String SEPARATOR = "\r";
//    boolean bconnected = false;

//    boolean cClient = false;

    Label clientLabel = new Label("�û���");
    TextField clientName = new TextField(10);
    Button login = new Button("��¼");

    Label chatLabel = new Label("�����¼");
    TextArea ta = new TextArea(25, 20);
    TextArea content = new TextArea(2, 20);

    Label onlineLabel = new Label("���ߺ����б�");
    static TextField onlineCount = new TextField("��������");
    static List clientList = new List(30, false);

    Button ok = new Button("����");
    Button clear = new Button("���");

    public static void main(String[] args) {
        new ChatClient().init();
    }

    public void init() {
        f.setTitle("�ͻ���");

        //�û�
        Box client = Box.createHorizontalBox();
        client.add(clientLabel);
        //client.add(Box.createHorizontalGlue());
        client.add(clientName);
        client.add(Box.createHorizontalGlue());
        client.add(login);
        client.add(Box.createHorizontalGlue());
        //client.add(cancel);

        //�������
        Box bottom = Box.createHorizontalBox();
        bottom.add(ok);
        bottom.add(Box.createHorizontalGlue());
        bottom.add(clear);

        //���
        Box left = Box.createVerticalBox();
        left.add(Box.createVerticalStrut(10));
        left.add(client);
        left.add(chatLabel);
        left.add(ta);
        left.add(Box.createVerticalStrut(5));
        left.add(content);
        left.add(bottom);
        left.add(Box.createVerticalStrut(5));
        //�ұ�
        Box right = Box.createVerticalBox();
        right.add(Box.createVerticalStrut(10));
        right.add(onlineLabel);
        right.add(Box.createVerticalStrut(5));
        right.add(onlineCount);
        right.add(Box.createVerticalStrut(5));
        right.add(clientList);
        right.add(Box.createVerticalStrut(5));
        //�ϲ�
        Box all = Box.createHorizontalBox();
        all.add(left);
        all.add(Box.createHorizontalStrut(10));
        all.add(right);
        f.add(all);
        clientList.add("Ⱥ��");
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

    //�ͻ�����Ϊ�����
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
                System.out.println("�˿�ʹ����");
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

    //�ͻ��˽��տͻ���
    class ReceivePeerMsg implements Runnable {
        private ReceiveData receiveData;
        private PeerClient peerClient;

        public ReceivePeerMsg(PeerClient peerClient) {
            this.peerClient = peerClient;
            connectPeerClient.cClient = true;
        }

        public void run() {
            try {
                while (connectPeerClient.cClient) {
                    String data = clientData.receiveData(peerClient.disWithPeer);
                    receiveData = new ReceiveData(data);
//                    String str = peerClient.disWithPeer.readUTF();
                    //�ж���ϢΪ�գ��Լ��������Ĵ���
                    if (receiveData.getStr() != "") {
                        if (receiveData.getStr().split("��").length != 1)
                            ta.setText(ta.getText() + receiveData.getStr() + "\n");
                    }
                    System.out.println(data);
                }
            } catch (SocketException e) {
                connectPeerClient.cClient = false;
                System.out.println("Client closed0");
            } catch (EOFException e) {
                System.out.println("Client closed1");
            } catch (IOException e) {
                System.out.println("Client closed2");

            } finally {
                //�ر�Ҫ��װһ��
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

    //�ͻ������ӷ����
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

    //�ͻ������ӿͻ���
//    public void connectpeer(int peerport) {
//        try {
//            socketWithPeer = new Socket("127.0.0.1", peerport);
//            peerDos = new DataOutputStream(socketWithPeer.getOutputStream());
//            cClient = true;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    //�ͻ��˶Ͽ����ӿͻ���
//    public void disconnectpeer() {
//        connectPeerClient.cClient = false;
//        System.out.println("peer disconnected");
//    }

    //������Ϣ
    private void SendThread() {
        clientData.setStr(clientData.getName() + "˵��" + content.getText().trim());
        String all = clientData.buildMsg(clientData.getName(), clientData.getPort(), clientData.getStr());
        //ta.setText(str);
        System.out.println("����ȥ����" + clientData.getStr());
        content.setText(null);
        try {
            if (clientData.getStr().length() != clientData.getName().length() + 2) {
                if (!connectPeerClient.cClient) {
                    clientData.sendData(connectServer.dosWithServer, all);
                    connectServer.dosWithServer.flush();
                } else {
                    clientData.sendData(connectPeerClient.dosWithPeer, all);
                    System.out.println("���͵���Ϣ" + clientData.getStr());
                    if (clientData.getStr().length() != clientData.getName().length() + 2)
                        ta.setText(ta.getText() + clientData.getStr() + "\n");
                }
            }

            //dos.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    //���շ������Ϣ
    private class ReceiveServerMsg implements Runnable {
        ReceiveData receiveData;
        String data = null;
        String name_and_port = null;

        public void run() {
            while (connectServer.bconnected) {
                try {
                    data = clientData.receiveData(connectServer.disWithServer);
                    name_and_port = clientData.receiveData(connectServer.disWithServer);
                    receiveData = new ReceiveData(data, name_and_port);
//                    data_split = Arrays.asList(data.split(DELIMITER));
//                    java.util.List<String> listname = Arrays.asList(name_and_port.split(DELIMITER));
//                    String rname = data_split.get(3);
//                    String rport = data_split.get(1);
//                    if (receiveData.getStr().equals("")) {
//                        receiveData.clearClientInfo();
//                        clientList.removeAll();
//                        clientList.add("Ⱥ��");
//                        for (int j = 0; j < listname.size(); j++) {
//                            clientList.add(listname.get(j).split(SEPARATOR)[0]);
//                            receiveData.putClientInfo(listname.get(j).split(SEPARATOR)[0], listname.get(j).split(SEPARATOR)[1]);
//                        }
//                        System.out.println(clientInfo.size());
//
//                        onlineCount.setText("��������" + ": " + (clientList.getItemCount() - 1));
//                    }
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
                            ta.setText(ta.getText() + receiveData.getStr() + "\n");
                        }
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
            if(clientName.getText().trim().length() > 1) {
                login.setEnabled(false);
                clientName.setEnabled(false);
                connectServer.connect();//�ͻ������ӷ����
                clientData.setName(clientName.getText());
                clientData.setPort(String.valueOf(connectServer.clientSocket.getLocalPort() + 1));
                String all = clientData.buildMsg(clientData.getName(), clientData.getPort(), "");
                try {
                    clientData.sendData(connectServer.dosWithServer, all);//�ͻ��������˷��͵�¼��Ϣ
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                new Thread(new ClientServer()).start();//�����ͻ�����Ϊ����˵ķ���
                new Thread(new ReceiveServerMsg()).start();//����������Ϣ����
            }
        }
    }

    private class peerListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            String peer = clientList.getSelectedItem();
//            for (Map.Entry<String, String> entry : ReceiveData.getClientInfo().entrySet()) {
//                System.out.println("Map����  " + entry.getKey() + "    " + entry.getValue());
//            }
            if (!peer.equals("Ⱥ��")) {
                connectPeerClient.connectpeer(Integer.parseInt(ReceiveData.getClientInfo().get(peer)));
            } else {
                connectPeerClient.disconnectpeer();
            }
        }
    }
}

//�ͻ��˵Ŀͻ�����
class PeerClient {
    private Socket peerSocket = null;//�ͻ��˵ķ����
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

//�ͻ��˵Ŀͻ������ӿͻ��˵ķ������
class ConnectPeerClient {
    private Socket socketWithPeer = null;//�ͻ��˵Ŀͻ���
    boolean cClient;
    DataOutputStream dosWithPeer;

    //�ͻ������ӿͻ���
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

//�ͻ������ӷ������
class ConnectServer {
    Socket clientSocket = null;//Client�Լ���scoket
    DataOutputStream dosWithServer = null;
    DataInputStream disWithServer = null;
    static boolean bconnected = false;

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

//������Ϣ��
class ReceiveData {
    private String name = "";
    private String port = "";
    private String str = "";
    //    private String peer = "";
    private static Map<String, String> clientInfo = new HashMap<String, String>();

    private String DELIMITER = "\f";
    private String SEPARATOR = "\r";

    //�ͻ���֮��ͨ�Ź�������ʼ��
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

    //�ͻ��˽��շ������Ϣ��������ʼ��
    ReceiveData(String data_from_client, String name_and_port) {
        java.util.List<String> data_from_client_split = Arrays.asList(data_from_client.split(DELIMITER));
        java.util.List<String> listname = Arrays.asList(name_and_port.split(DELIMITER));
        this.name = data_from_client_split.get(0);
        this.port = data_from_client_split.get(1);
//        this.str = data_from_client_split.get(2);
//        this.peer = data_from_client_split.get(3);

        if (data_from_client_split.size() == 3) {
            this.str = data_from_client_split.get(2);
        } else {
            this.str = "";
        }
        System.out.println("str��ʲô" + data_from_client.length());
        System.out.println("Ⱥ��������" + str);
        if (str.equals("")) {
            clearClientInfo();
            ChatClient.clientList.removeAll();
            ChatClient.clientList.add("Ⱥ��");
            for (int j = 0; j < listname.size(); j++) {
                ChatClient.clientList.add(listname.get(j).split(SEPARATOR)[0]);
                putClientInfo(listname.get(j).split(SEPARATOR)[0], listname.get(j).split(SEPARATOR)[1]);
            }
            System.out.println("�б�����" + clientInfo.size());

            ChatClient.onlineCount.setText("��������" + ": " + (ChatClient.clientList.getItemCount() - 1));
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

//�û���¼��Ϣ��װ�࣬�������ͽ�����Ϣ����
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
////��Ϣ��װ��
//class ClientMsg{
//    ClientData clientData;
//    ConnectPeerClient connectPeerClient;
//    ClientMsg(ClientData clientData, ConnectPeerClient connectPeerClient){
//        this.clientData = clientData;
//        this.connectPeerClient = connectPeerClient;
//    }
//    public void SendThread() {
//        clientData.setStr(clientData.getName() + "˵��" + content.getText().trim());
//        String all = clientData.buildMsg(clientData.getName(), clientData.getPort(),clientData.getStr(),clientData.getPeer());
//        //ta.setText(str);
//        content.setText(null);
//        try {
//            if (!connectPeerClient.cClient) {
//                clientData.sendData(connectServer.dosWithServer, all);
//                connectServer.dosWithServer.flush();
//            } else {
//                clientData.sendData(connectPeerClient.dosWithPeer, clientData.getStr());
//                System.out.println("���͵���Ϣ" + clientData.getStr());
//                if (!clientData.getStr().split("��")[1].equals(""))
//                    ta.setText(ta.getText() + clientData.getStr() + "\n");
//            }
//            //dos.close();
//        } catch (IOException e1) {
//            e1.printStackTrace();
//        }
//    }
//}


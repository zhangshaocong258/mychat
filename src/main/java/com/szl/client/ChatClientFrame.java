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
 * ����ChatClient���󡢴��м����¼���Swing��Frame��ʼ���������ࡢmain����
 */

public class ChatClientFrame {
    private ChatClient chatClient = new ChatClient();

    private JLabel clientLabel = new JLabel("�û���");

    private JLabel chatLabel = new JLabel("�����¼");

    private JLabel onlineLabel = new JLabel("���ߺ����б�");

    private JButton send = new JButton("����");
    private JButton clear = new JButton("���");


    public static void main(String[] args) {
        new ChatClientFrame().init();
    }

    public void init() {
        JPanel jPanel = chatClient.getjPanel();
        jPanel.setBorder(BorderFactory.createTitledBorder("�ͻ��˴���"));
        jPanel.setLayout(new GridBagLayout());

        chatClient.getChatRecord().setEditable(false);
        chatClient.getOnlineCount().setEditable(false);

        chatClient.initModel();//��ʼ����ӡ�Ⱥ�ġ�

        //�û���
        jPanel.add(clientLabel, new PropertiesGBC(0, 0, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(0, 0).setInsets(0, 5, 5, 0));

        //�û��������
        jPanel.add(chatClient.getClientName(), new PropertiesGBC(1, 0, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(1, 0).setInsets(0, 0, 5, 0));

        //��¼
        jPanel.add(chatClient.getBtnConnect(), new PropertiesGBC(2, 0, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(0, 0).setInsets(0, 0, 5, 5));

//        //�˳�
//        jPanel.add(chatClient.getExit(), new PropertiesGBC(3, 0, 1, 1).
//                setFill(PropertiesGBC.BOTH).setWeight(0, 0).setInsets(0, 0, 5, 5));

        //�����¼
        jPanel.add(chatLabel, new PropertiesGBC(0, 1, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(0, 0).setInsets(0, 5, 5, 5));


        //�����¼��
        jPanel.add(chatClient.getChatRecordJScrollPane(), new PropertiesGBC(0, 2, 3, 1).
                setFill(PropertiesGBC.BOTH).setWeight(1, 1).setInsets(0, 5, 5, 5));

        //���Ϳ�
        jPanel.add(chatClient.getChatBoxJScrollPane(), new PropertiesGBC(0, 3, 3, 1).
                setFill(PropertiesGBC.BOTH).setWeight(1, 0).setInsets(0, 5, 5, 5).setIpad(0, 20));

        //���
        jPanel.add(clear, new PropertiesGBC(0, 5, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(0, 0).setInsets(0, 5, 5, 5));

        //�ļ�
        jPanel.add(chatClient.getFile(), new PropertiesGBC(1, 5, 1, 1).
                setAnchor(PropertiesGBC.EAST).setWeight(0, 0).setInsets(0, 5, 5, 5));
        chatClient.getFile().setEnabled(false);

        //����
        jPanel.add(send, new PropertiesGBC(2, 5, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(0, 0).setInsets(0, 5, 5, 5));


        //���ߺ����б�
        jPanel.add(onlineLabel, new PropertiesGBC(3, 0, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(0, 0).setInsets(0, 5, 5, 5));

        //��������
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
        //���պ��ļ�����
        chatClient.getFile().addActionListener(new fileListener());
        chatClient.getReceive().addActionListener(new receiveListener());
        chatClient.getCancel().addActionListener((new cancelListener()));
        jFrame.setVisible(true);
    }

    //����ȫ�����ⲿʵ��
    //����
    private class sendListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            chatClient.send();
        }
    }

    //���
    private class clearListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            chatClient.getChatBox().setText(null);

        }
    }

    //�ļ�
    private class fileListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            chatClient.openFileChooser();
        }
    }

    //����
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


    //��¼�˳�
    private class clientLoginListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String btnText = chatClient.getBtnConnect().getText();
            if (btnText.equals("��¼")) {
                chatClient.clientLogin();
            } else if (btnText.equals("�˳�")) {
                chatClient.clientExit();
            }
        }
    }

    //Listѡ�����
    private class p2pListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            chatClient.connectPeer();
        }
    }
}

/**
 * �ⲿ��
 */

/**
 * �ͻ��˵Ŀͻ����࣬��Ҫ���ReceivePeerMsgʹ�ã�����������Ϣ;��������������ķ�װ
 * ���ϲ�ر�
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

//out���ڴ����ļ�������ʹ��
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
 * �ͻ��˵Ŀͻ������ӿͻ��˵ķ�����ֻ࣬������Ϣ
 */
//in���ڴ����ļ�������ʹ��
class ClientConnectPeerClient {
    //������־λ
    private static boolean sendToClient = false;
    private static boolean receiveFromClient = false;
    private IOUtil ioUtil = new IOUtil();

    //�ͻ������ӿͻ���
    public void connectPeerClient(int peerClientPort) {
        try {
            ioUtil.setSocket(new Socket("127.0.0.1", peerClientPort));
            ioUtil.setDataInputStream(new DataInputStream(ioUtil.getSocket().getInputStream()));
            ioUtil.setDataOutputStream(new DataOutputStream(ioUtil.getSocket().getOutputStream()));
            sendToClient = true;
            System.out.println("�����ϵĶ˿ں�" + ioUtil.getSocket().getLocalPort());
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
 * �ͻ������ӷ������
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
            System.out.println("�ͻ���������");
        } catch (UnknownHostException e) {
            System.out.println("�����δ����");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("�����δ����");
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
 * ������Ϣ�࣬�ӷ���˺Ϳͻ��˽��գ�����
 */
class ReceiveData {
    private String name = "";
    private String str = "";
    private String file = "";
    private String fileStr = "";
    private static Map<String, String> clientInfo = new HashMap<>();//name��port
    private List<String> namePortList = null;//List��ʼ�����⣿

    private String DELIMITER = "\f";
    private String FILE_DELIMITER = "\f\r\f";

    //�ͻ���֮��ͨ�Ź�������ʼ�����ͻ�peer���յ���Ϣ������Ϊ��
    ReceiveData(String data_from_client) {
        if (data_from_client.contains(FILE_DELIMITER)) {
            System.out.println("�����ļ����չ���");
            List<String> data_from_client_split = Arrays.asList(data_from_client.split(FILE_DELIMITER));
            this.file = data_from_client_split.get(1);
            this.fileStr = data_from_client_split.get(0);
        } else {
            List<String> data_from_client_split = Arrays.asList(data_from_client.split(DELIMITER));
            this.name = data_from_client_split.get(0);
            this.str = data_from_client_split.get(2);
        }
    }

    //�ͻ��˽��շ������Ϣ��������ʼ������Ϣ����Ϊname&port����¼���˳���Ϣ��ʽ������name&port&str����ͨ��Ϣ��ʽ��
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

    //����ˢ�������б�
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

    //map���ڽ���P2P���ӣ�name��Ӧport
    public static Map<String, String> getClientInfo() {
        return clientInfo;
    }
}

/**
 * ������Ϣ��,�û���¼��Ϣ��װ�࣬�������ͽ�����Ϣ����,build��ͨ��Ϣ����¼��Ϣ����
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
        return sender + " �����ļ� " + str + " �����" + FILE_DELIMITER + "file";
    }

    public String buildMsg(String name, String port, String str) {
        String DELIMITER = "\f";
        return name + DELIMITER + port + DELIMITER + str;
    }

    public String buildMsg(String name, String port) {
        String DELIMITER = "\f";
        return name + DELIMITER + port;
    }

    //��receiverΪ��ʱ��StringValueof����"null"�ַ���
    public String buildStr(String sender, String receiver, String str) {
        if (receiver.equals("null") || receiver.equals("Ⱥ��")) {
            receiver = "������";
        }
        return sender + " �� " + receiver + " ˵��" +
                " -- " + new DayTime().getDateString() + " --" +
                "\n  " + str;
    }

    //�滻�س���replace��"\n"���滻"\n"����"\\n"�����̻س���"\n"��replaceAll��ת���ַ�����˵
    public String formatStr(String str) {
        return str.replace("\n", "\n  ");
    }

    //���ַ���תΪAscii���õ����з���Ascii�룬�ж����ĸ�ת���ַ�
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
 * ���ͻ��ˣ���װ������Swing��XML��·����������Ϣ���������ּ�������
 * �ڲ���
 */
//���ͻ���
class ChatClient {
    private ClientServer clientServer = new ClientServer();//�ͻ�����Ϊ�����
    private ReceiveServerMsg receiveServerMsg = new ReceiveServerMsg();//���շ������Ϣ
    //JList�¼�����һ����־λlistener�����������Ƿ������ӣ���Ϊˢ��clientslist�б�ʱ��ʼ�ռ���������ˢ��֮ǰ��Ϊtrue����ֹ���ӳ���
    private static boolean listener = false;
    private ClientConnectServer clientConnectServer = new ClientConnectServer();//�ͻ������ӷ����
    private ClientData clientData = new ClientData();//�����û���¼��Ϣ����װ��buildMsg��send��receive
    private ClientConnectPeerClient clientConnectPeerClient = new ClientConnectPeerClient();//�ͻ������ӿͻ��˷���ˣ������ǿͻ��˵Ŀͻ���
    private ClientConnectPeerClient clientConnectPeerClientFile = new ClientConnectPeerClient();
//    private PeerClient peerClient;
//    private PeerClient peerClientFile;

    private FileTransmit fileTransmit = new FileTransmit();

    private static JFrame jFrame = new JFrame("�ͻ���");
    private static JPanel jPanel = new JPanel();

    private JTextField clientName = new JTextField(10);
    private JButton btnConnect = new JButton("��¼");

    private static JButton file = new JButton("�ļ�");
    private static JButton receive = new JButton("����");
    private static JLabel rate = new JLabel("���:  0%");
    private static JButton cancel = new JButton("ȡ��");

    private static JTextArea chatRecord = new JTextArea();
    private JTextArea chatBox = new JTextArea();
    private JScrollPane chatBoxJScrollPane = new JScrollPane(chatBox);
    private JScrollPane chatRecordJScrollPane = new JScrollPane(chatRecord);


    private static JTextArea onlineCount = new JTextArea("��������");
    private JScrollPane onlineCountJScrollPane = new JScrollPane(onlineCount);

    private static DefaultListModel<String> listModel = new DefaultListModel<>();
    private static JList<String> clientList = new JList<>(listModel);
    private JScrollPane jScrollPane = new JScrollPane(clientList);

    public void initModel() {
        listModel.addElement("Ⱥ��");
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


    //send�ļ���������Ϣ�ֿ�
    public void send() {
        if (fileTransmit.getSend()) {
            sendFile();
        } else {
            sendMsg();
        }
    }

    //�����ļ�
    public void sendFile() {
//        chatBox.setText(null);//����������ļ���Ϣ
        sendMsg();
        fileTransmit.connect(clientConnectPeerClientFile.getDisWithPeer(), clientConnectPeerClientFile.getDosWithPeer());
        //�����ļ�
        fileTransmit.sendRunnable();
        fileTransmit.setIsSend(false);
        file.setEnabled(false);
        fileTransmit.addRateCancel();
    }

    //������Ϣ
    public void sendMsg() {
        boolean isNull = true;
        String msg;
        if (chatBox.getText().trim().length() > 0) {
            isNull = false;
        }
        //����ʱ���ж��Ƿ�Ϊ�գ����жϸ�����˻��ǿͻ��˷��ͣ������Ӧ����ΪĬ�ϣ�
        //������˷�����Ϣʱ��Record����append���Ƚ��շ���˷��ص���Ϣʱappend����peer������Ϣʱ����Ҫappend
        try {
            //�ж���Ϣ�Ƿ�Ϊ�գ�����name + "��" + "˵"�ĳ����ж�
            if (!isNull) {
                if (fileTransmit.getSend()) {
                    msg = clientData.buildFileMsg(clientData.getName(), chatBox.getText().trim());
                    chatBox.setText(null);
                    clientData.sendData(clientConnectPeerClient.getDosWithPeer(), msg);
                } else {
                    clientData.setStr(clientData.getName(),
                            String.valueOf(clientList.getSelectedValue()), clientData.formatStr(chatBox.getText().trim()));
                    //ת��ΪAscii�������ж�
//                System.out.println(clientData.stringToAscii(chatBox.getText().trim()));
                    msg = clientData.buildMsg(clientData.getName(),
                            clientData.getPort(), clientData.getStr());
                    chatBox.setText(null);
                    //�жϸ�����������peer�˷�����Ϣ
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
                chatRecord.append("���͵����ݲ���Ϊ��\n");
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    //��¼����������XML·��
    public void clientLogin() {
        clientConnectServer.connectServer();//�ͻ������ӷ����
        //��¼������Ϊ�գ�Ϊ����set null����������
        if ((clientName.getText().trim().length() >= 1) && clientDom4j.queryElement(clientName.getText())) {
//            btnConnect.setEnabled(false);
            btnConnect.setText("�˳�");
            clientName.setEnabled(false);
            clientData.setName(clientName.getText());
            clientData.setPort(String.valueOf(clientConnectServer.getClientSocket().getLocalPort() + 1));
            String msg = clientData.buildMsg(clientData.getName(), clientData.getPort());
            dirPath = "D:/" + clientData.getName() + "-" + new DayTime().getDateString();
            filePath = dirPath + File.separator + clientData.getName() + "-" + "ChatRecord.xml";
            try {
                clientData.sendData(clientConnectServer.getDosWithServer(), msg);//�ͻ��������˷��͵�¼��Ϣ
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            new Thread(clientServer).start();//�����ͻ�����Ϊ����˵ķ���
            new Thread(receiveServerMsg).start();//����������Ϣ����
        } else {
            if (clientName.getText().trim().length() == 0) {
                chatRecord.append("�û�������Ϊ��\n");
            } else if (!clientDom4j.queryElement(clientName.getText())) {
                chatRecord.append("�û����Ѵ���\n");
            }
            clientName.setText(null);
        }
    }

    //��Ҫ�ر�����socket�Լ����̣���Ϊfalse
    public void clientExit() {
        btnConnect.setText("��¼");
        clientName.setEnabled(true);
        //�ͻ��˶Ͽ������˵�����
        clientConnectServer.close();
        //�ͻ��˿ͻ��˶Ͽ���ͻ��˷���˵�����
        clientConnectPeerClient.close();
        //�ͻ�����Ϊ����˹رգ�����whileѭ��
        clientServer.close();
        //�ͻ��˽��տͻ�����Ϣ�رգ�����whileѭ��
        receiveServerMsg.close();

        //תΪlambda���ʽ,����listener���μ���
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                listener = true;
                listModel.removeAllElements();
                listModel.addElement("Ⱥ��");
                clientList.setModel(listModel);
                listener = false;
                onlineCount.setText("��������" + ": " + (listModel.getSize() - 1));
            }
        });
    }

    //����peer����
    public void connectPeer() {
        //����һ��listener��־λ�����ߺ�JList���м���������ȡ�κζ���
        if (!listener) {
            String peer = String.valueOf(clientList.getSelectedValue());
//            for (Map.Entry<String, String> entry : ReceiveData.getClientInfo().entrySet()) {
//                System.out.println("Map����  " + entry.getKey() + "    " + entry.getValue());
//            }
            System.out.println("ѡ����" + peer);
            if (peer != null && !peer.equals("Ⱥ��")) {
                //���peer֮��ֱ�ӿ�2��·
                clientConnectPeerClient.connectPeerClient(Integer.parseInt(ReceiveData.getClientInfo().get(peer)));
                if (!peer.equals(clientData.getName().trim())) {
                    clientConnectPeerClientFile.connectPeerClient(Integer.parseInt(ReceiveData.getClientInfo().get(peer)));
                    file.setEnabled(true);
                } else {
                    clientConnectPeerClientFile.setReceiveFromClient(false);
                    file.setEnabled(false);
                }
            } else {
                //�Ͽ�P2P���ӣ�Ĭ��Ⱥ��,˳��Ҫ��
                clientConnectPeerClient.setSendToClient(false);
            }
        }
    }

    public void openFileChooser() {
        //�����ļ�����true
        fileTransmit.chooseFile();
        //��һ���µ�����
        //�򿪺��Ϳ���ʾ·����������ʶ���ַ����������շ������շ��ݴ��жϣ��򿪽���
        chatBox.setText(fileTransmit.getFolderPath());
    }

    public void saveFileChooser() {
        fileTransmit.saveFile();
        //�򿪱���Ի���ȷ��·�����������߳�
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

    //�ͻ�����Ϊ�����
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
                System.out.println("�Լ��Ķ˿�" + clientConnectServer.getClientSocket().getLocalPort());
                start = true;
            } catch (BindException e) {
                System.out.println("�˿�ʹ����");
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                //ÿ������2��·
                while (start) {
                    Socket socket = clientServerSocket.accept();
                    Socket fileSocket = clientServerSocket.accept();

                    peerClient = new PeerClient(socket);
                    peerClientFile = new PeerClient(fileSocket);
                    receivePeerMsg = new ReceivePeerMsg(peerClient);
                    receivePeerFile = new ReceivePeerFile(peerClientFile);
                    System.out.println("�ͻ���������");
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

    //�����ļ���Ϣ
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


    //�ͻ��˽��տͻ�����Ϣ��socket������װ��PeerClient
    class ReceivePeerMsg implements Runnable {
        private ReceiveData receiveData;
        private PeerClient peerClient;

        public ReceivePeerMsg(PeerClient peerClient) {
            this.peerClient = peerClient;
            clientConnectPeerClient.setReceiveFromClient(true);//���ձ�־λ���б��ڷ��ͱ�־λ
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
                    //ֻ���ж��Ƿ��Ǹ����˷�����Ϣ�����ݲ�����Ϊ�գ���Ϊ����ʱ���ж����ݲ���Ϊ�գ�����Ǹ����˷��͵ģ�Record��set��Ϣ
                    if (!(receiveData.getName().equals(clientData.getName()))) {
                        if (receiveData.getFile().equals("file")) {
                            chatRecord.append(receiveData.getFileStr() + "\n");
                            //����
                            jPanel.add(receive, new PropertiesGBC(2, 1, 1, 1).
                                    setAnchor(PropertiesGBC.EAST).setWeight(0, 0).setInsets(0, 5, 5, 5));
                            jFrame.validate();//ˢ��
                        } else {
                            chatRecord.append(receiveData.getStr() + "\n");

                            clientDom4j.createRecord(chatRecord.getText());
                            clientDom4j.saveXML(Dom4jXML.getRecordDocument(), dirPath, filePath);
                        }
                    }
                }
            } catch (SocketException e) {
                clientConnectPeerClient.setReceiveFromClient(false);//������λfalse��ֹͣ����
                System.out.println("�ͻ��˹ر�1");
            } catch (EOFException e) {
                System.out.println("�ͻ��˹ر�2");
            } catch (IOException e) {
                System.out.println("�ͻ��˹ر�3");
            } finally {
                peerClient.close();
            }
        }
    }

    //���շ������Ϣ,û��finally����ΪconnectServer���еط��ر���
    class ReceiveServerMsg implements Runnable {
        ReceiveData receiveData = null;
        String data = "";
        String namePort = "";

        //ˢ�������б�
        public void addLists() {
            listener = true;
            receiveData.clearClientInfo();
            listModel.removeAllElements();
            listModel.addElement("Ⱥ��");
            for (String listName : receiveData.getNamePortList()) {
                String SEPARATOR = "\r";
                listModel.addElement(listName.split(SEPARATOR)[0]);
                receiveData.putClientInfo(listName.split(SEPARATOR)[0], listName.split(SEPARATOR)[1]);
            }
            clientList.setModel(listModel);
            System.out.println("�б�����" + ReceiveData.getClientInfo().size());
            onlineCount.setText("��������" + ": " + (listModel.getSize() - 1));
            listener = false;
        }

        public void close() {
            clientConnectServer.setConnectWithServer(false);
        }

        //while�ŵ�try���棬setSendToClientFalse()�ŵ�if����
        @Override
        public void run() {
            try {
                while (clientConnectServer.getConnectWithServer()) {
                    data = clientData.receiveData(clientConnectServer.getDisWithServer());
                    namePort = clientData.receiveData(clientConnectServer.getDisWithServer());
                    receiveData = new ReceiveData(data, namePort);
                    //Swing���̣߳��ж�str�����Ƿ�Ϊ�գ�invokeLater���������û�з�Ӧ�����µ�¼�����߲ſ��ָܻ�������bug
                    if (receiveData.getStr().length() == 0) {
                        EventQueue.invokeLater(this::addLists);
//                    EventQueue.invokeLater(new Runnable() {
//                        public void run(){
//                            receiveData.addLists();
//                        }
//                    });
                        clientConnectPeerClient.setSendToClient(false);//���ߺ�JListȫ����գ�Ĭ��Ⱥ��
                    }
                    //�ж�����ͨ��Ϣ����ע����Ϣ����ͨ��Ϣ������Ϊ�գ�ע����ϢΪ��
                    if (receiveData.getStr().length() != 0) {
                        chatRecord.append(receiveData.getStr() + "\n");
                        clientDom4j.createRecord(chatRecord.getText());
                        clientDom4j.saveXML(Dom4jXML.getRecordDocument(), dirPath, filePath);
                    }
                }
            } catch (SocketException e) {
                System.out.println("����˹ر�1");
                //�����ж��ǿͻ��˶Ͽ����Ƿ���˶Ͽ�������˶Ͽ�Ϊtrue���ͻ��˶Ͽ�Ϊfalse
                if (clientConnectServer.getConnectWithServer()) {
                    System.exit(0);
                }
            } catch (EOFException e) {
                System.out.println("����˹ر�2");
//                    System.exit(0);
            } catch (IOException e) {
                System.out.println("����˹ر�3");
            }
        }
    }
}

/**
 * �����߳�����������շ������ļ��������շ����յ��ļ������������ȴ�saveFile����·����
 * ����·���󣬻�������̣߳����ͷ�����Agree��Ȼ��ֱ���������գ����ͷ����յ�Agree��
 * ִ�з��ͣ������̵߳��������ڷ����̣߳�
 */

class FileTransmit {
    private static String folderPath = "";
    private static String folderName = "";
    private String useTime = "";
    private String speed = "";
    private boolean firstTime = true;//����ȡ����ʾ��ȷ�ļ��ļ���
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

    //set �ļ���Ϊ���������ļ��������ڽ��ղ��洢
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
        //��ɰٷֱ�
        ChatClient.getjPanel().add(ChatClient.getRate(), new PropertiesGBC(1, 1, 1, 1).
                setFill(PropertiesGBC.BOTH).setWeight(0, 0).setInsets(0, 5, 5, 5));

        ChatClient.getRate().setText("���:  0%");

        //ȡ��
        ChatClient.getjPanel().add(ChatClient.getCancel(), new PropertiesGBC(2, 1, 1, 1).
                setAnchor(PropertiesGBC.EAST).setWeight(0, 0).setInsets(0, 5, 5, 5));
        ChatClient.getjFrame().validate();//ˢ��
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
            useTime = time / 1000 / 60 + " ����";
        } else {
            if (time / 1000 == 0) {
                useTime = "1 ����";
            } else {
                useTime = time / 1000 + " ����";
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
        //����򿪺󣬷��Ϳ���ʾ��ַ���㷢�ͣ����շ����յ���Ϣ����ʾ���հ�ť�����º����ý��յ�ַ���ڽ���
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jFileChooser.getSelectedFile();
            //����ļ���С
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
        //����򿪺󣬷��Ϳ���ʾ��ַ���㷢�ͣ����շ����յ���Ϣ����ʾ���հ�ť�����º����ý��յ�ַ���ڽ���
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jFileChooser.getSelectedFile();
            //����ļ���С
            long l = file.length();
            //�����ļ����ֲ��䣬·������
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
                            totalLen = folder.length();//�ļ�����
                            dosWithPeer.writeLong(totalLen);
                            sendFile(folder);
                        } else {
                            getFolderTotalLen(folderPath);//�õ�totalLen
                            dosWithPeer.writeLong(totalLen);
                            sendFolder(folder);
                        }
                    }
                    endTime = System.currentTimeMillis();
                    dosWithPeer.writeUTF("endTransmit");
                    useTime = getUseTime(endTime - beginTime);
                    speed = getSpeed(endTime - beginTime, totalLen);
                    ChatClient.appendChatMsg("�ļ���" + folderName + "���������, ������ʱ: "
                            + useTime + ",�ٶ�: " + speed
                            + " !\n");
                    ChatClient.getFile().setEnabled(true);
                    removeRateCancel();
                } catch (IOException e) {
                    ChatClient.appendChatMsg("�ļ� " + folderName + " ȡ������\n");
                    ChatClient.getFile().setEnabled(true);
                    removeRateCancel();
                    e.printStackTrace();
                }

            }

            private void sendFolder(File folder) {
                String selectFolderPath = folder.getAbsolutePath().substring(index);//ѡ����ļ�������
                try {
                    dosWithPeer.writeUTF("sendFolder");
                    dosWithPeer.writeUTF(selectFolderPath);//�������ļ���
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
                //ת��Ϊforeach
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
                    dosWithPeer.writeUTF(file.getName());//�����ļ���������д
                    //�����ļ�
                    //ÿ�ζ�Ҫ����length�����жϵ����ļ���ʼ�ͽ���������ȫ���ļ���д��һ���ļ���
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
                    System.out.println("���ͷ�����ѭ��" + length);

                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("�����ļ�����");
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
                ChatClient.getRate().setText("���:  " + rate + "%");
                ChatClient.getjFrame().validate();
//                dataPanel.getLblInfo().setText("���:  " + rate +"%");
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
                        finalFolderPath = folderPath;//����ʱ�ֶ�����ѡ��Ķ���·��
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
                            receiveFile(finalFolderPath);//���ļ�
                        } else if (firstRead.equals("sendFolder")) {
                            subFolder = disWithPeer.readUTF();//���ͷ���selectFolderPath��Ŀ¼
                            finalFolderPath = folderPath + File.separator + subFolder;//���ڴ����ļ��У�����receiveFile��Ϊ����
                            //������Ŀ¼
                            File file = new File(finalFolderPath);
                            file.mkdirs();
                            //��¼�ļ���������ȡ��
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
                    ChatClient.appendChatMsg("�ļ���" + finalFileName + "���������, ������ʱ: "
                            + useTime + ",�ٶ�: " + speed
                            + " !\n");
                    removeRateCancel();
                    firstTime = true;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    System.out.println("ͬ����");
                    e.printStackTrace();
                }
            }

            private void receiveFile(String finalFolderPath) {
                byte[] receiveBuffer = new byte[BUF_LEN];
                int length;
                try {
                    folderName = disWithPeer.readUTF();//�õ�Ҫд���ļ����ļ���
                    //��¼�ļ���������ȡ��
                    if (firstTime) {
                        finalFileName = folderName;
                        firstTime = false;
                    }
                    //�����finalFilePath�Ѿ����������ļ��У������ļ�������
                    String finalFilePath = finalFolderPath + File.separator + folderName;
                    fileOutputStream = new FileOutputStream(new File(finalFilePath));
                    length = disWithPeer.readInt();
                    while (length > 0) {
                        disWithPeer.readFully(receiveBuffer, 0, length);//read��length�ŷ��أ�����read�����ܲ���length�ͷ���
                        fileOutputStream.write(receiveBuffer, 0, length);
                        fileOutputStream.flush();
                        haveSendLen += length;
                        setTransferRate(haveSendLen, totalLen);
                        length = disWithPeer.readInt();
                    }
                    System.out.println("���շ�����ѭ��");
                } catch (IOException e) {
                    System.out.println("�ļ��������ر�");
                    ChatClient.appendChatMsg("�ļ� " + finalFileName + " ȡ������\n");
                    removeRateCancel();
                    ChatClient.getjPanel().add(ChatClient.getReceive(), new PropertiesGBC(2, 1, 1, 1).
                            setAnchor(PropertiesGBC.EAST).setWeight(0, 0).setInsets(0, 5, 5, 5));
                    ChatClient.getjFrame().validate();//ˢ��
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
                ChatClient.getRate().setText("���:  " + rate + "%");
                ChatClient.getjFrame().validate();
//                dataPanel.getLblInfo().setText("���:  " + rate +"%");
            }
        };
        new Thread(receive).start();
    }


}

/**
 * ����XML���м�¼������XML�ļ�����Record
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
            //�õ�clients��List�����б����ж�
            List clientsList = rootElement.elements("clients");
            //foreach����
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
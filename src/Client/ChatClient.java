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
public class ChatClient{

    Frame f = new Frame();
    Socket s = null;
    Socket socketWithPeer = null;
    ServerSocket clientServerSocket = null;
    DataOutputStream dosWithServer = null;
    DataInputStream disWithServer = null;
    DataOutputStream peerDos = null;
    DataInputStream peerDis = null;
    Map<String,String> clientInfo= new HashMap<>();
    String name = "";
    String port = "";
    String str = "";
    String peer = "群聊";
    String all = "";
    String DELIMITER ="\f";
    String SEPARATOR ="\r";
    boolean bconnected = false;
    boolean cClient = false;

    Label clientLabel = new Label("用户名");
    TextField clientName = new TextField(10);
    Button login = new Button("登录");

    Label chatLabel = new Label("聊天记录");
    TextArea ta = new TextArea(25,20);
    TextArea content = new TextArea(2,20);

    Label onlineLabel = new Label("在线好友列表");
    TextField onlineCount = new TextField("在线人数");
    List clientList = new List(30,false);

    Button ok = new Button("发送");
    Button clear = new Button("清除");

    public static void main(String[] args) {
        new ChatClient().init();
    }

    public void init(){
        f.setTitle("客户端");

        //用户
        Box client = Box.createHorizontalBox();
        client.add(clientLabel);
        //client.add(Box.createHorizontalGlue());
        client.add(clientName);
        client.add(Box.createHorizontalGlue());
        client.add(login);
        client.add(Box.createHorizontalGlue());
        //client.add(cancel);

        //发送清除
        Box bottom = Box.createHorizontalBox();
        bottom.add(ok);
        bottom.add(Box.createHorizontalGlue());
        bottom.add(clear);

        //左边
        Box left = Box.createVerticalBox();
        left.add(Box.createVerticalStrut(10));
        left.add(client);
        left.add(chatLabel);
        left.add(ta);
        left.add(Box.createVerticalStrut(5));
        left.add(content);
        left.add(bottom);
        left.add(Box.createVerticalStrut(5));
        //右边
        Box right = Box.createVerticalBox();
        right.add(Box.createVerticalStrut(10));
        right.add(onlineLabel);
        right.add(Box.createVerticalStrut(5));
        right.add(onlineCount);
        right.add(Box.createVerticalStrut(5));
        right.add(clientList);
        right.add(Box.createVerticalStrut(5));
        //合并
        Box all = Box.createHorizontalBox();
        all.add(left);
        all.add(Box.createHorizontalStrut(10));
        all.add(right);
        f.add(all);
        clientList.add("群聊");
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
    //客户端服务
    class ClientServer implements Runnable {
        boolean start = false;
        public void run() {
            try {
                clientServerSocket = new ServerSocket(s.getLocalPort()+1);
                System.out.println(s.getLocalPort());
                start = true;
            } catch (BindException e) {
                System.out.println("端口使用中");
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                while (start) {
                    Socket sss = clientServerSocket.accept();
                    cClient c = new cClient(sss);
                    System.out.println("a client connected!");
                    new Thread(c).start();
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

    //客户端接收客户端
    class cClient implements Runnable{
        private Socket s;

        public cClient(Socket s ){
            this.s=s;
            try {
                peerDis = new DataInputStream(s.getInputStream());
                cClient = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void run(){
            try {
                while(cClient) {
                    String str = peerDis.readUTF();
                    ta.setText(ta.getText() + str + "\n");
                    System.out.println(str);
                }
            }
            catch (SocketException e){
                cClient = false;
                System.out.println("Client closed0");
            }
            catch (EOFException e){
                System.out.println("Client closed1");
            }catch (IOException e){
                System.out.println("Client closed2");

            }finally {
                try {
                    if(peerDis !=null) peerDis.close();
                    if(s !=null) s.close();

                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        }
    }
    //客户端连接服务端
    public void connect(){
        try {
            s = new Socket("127.0.0.1", 8888);
            dosWithServer = new DataOutputStream(s.getOutputStream());
            disWithServer = new DataInputStream(s.getInputStream());

            name = clientName.getText();
            port = String.valueOf(s.getLocalPort()+1);
            peer = name;
            all = name +DELIMITER + port + DELIMITER+ str + DELIMITER +peer;
            dosWithServer.writeUTF(all);

            System.out.println("connected");
            bconnected = true;
        } catch (UnknownHostException e) {
            System.out.println("sever not start");
            e.printStackTrace();
        }catch (IOException e){
            System.out.println("sever not start");
            System.exit(1);
            e.printStackTrace();
        }
    }

    public void disconnect(){
        try {
            dosWithServer.close();
            s.close();
            bconnected = false;
        }catch (IOException e){
            System.exit(0);
        }
    }
    //客户端连接客户端
    public void connectpeer(int peerport){
        try {
            socketWithPeer = new Socket("127.0.0.1",peerport);
            peerDos = new DataOutputStream(socketWithPeer.getOutputStream());
            cClient = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //客户端断开连接客户端
    public void disconnectpeer(){
        cClient = false;
        System.out.println("peer disconnected");
    }

    //发送信息
    private void SendThread() {
        str = name + "说：" + content.getText().trim();
        all = name +DELIMITER + port + DELIMITER+ str + DELIMITER + peer;
        //ta.setText(str);
        content.setText(null);
        try {
            if(!cClient){
                dosWithServer.writeUTF(all);
                dosWithServer.flush();
            }
            else{
                peerDos.writeUTF(str);
                System.out.println("CCCCCCC" + str);
                peerDos.flush();
                if(!str.split("：")[1].equals(""))
                    ta.setText(ta.getText() + str + "\n");
            }
            //dos.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    //接收信息
    private class RecvThread implements Runnable{
        String data = null;
        java.util.List<String> data_split =null;
        String name_and_port = null;
        public void run(){
            while(bconnected){
                try {
                    data = disWithServer.readUTF();
                    name_and_port = disWithServer.readUTF();
                    System.out.println(name_and_port);

                    data_split = Arrays.asList(data.split(DELIMITER));
                    java.util.List<String> listname = Arrays.asList(name_and_port.split(DELIMITER));
                    String rname = data_split.get(3);
                    String rport = data_split.get(1);
                    if(data_split.get(2).equals("")){
                        clientInfo.clear();
                        clientList.removeAll();
                        clientList.add("群聊");
                        for(int j =0;j<listname.size();j++){
                            clientList.add(listname.get(j).split(SEPARATOR)[0]);
                            clientInfo.put(listname.get(j).split(SEPARATOR)[0],listname.get(j).split(SEPARATOR)[1]);
                        }
                        System.out.println(clientInfo.size());

                        onlineCount.setText("在线人数" + ": " + (clientList.getItemCount()-1));
                    }

                } catch (SocketException e1){
                    System.out.println("Server closed" );
                    System.exit(0);
                }catch (EOFException e2){
                    System.exit(0);
                }
                catch (IOException e) {
                    System.out.println("Server closed" );

                }
                String str = data_split.get(2);
                try {
                    //str = b.get(2);
                    System.out.println(str);
                    if(!str.split("：")[1].equals("")){
                        ta.setText(ta.getText() + str + "\n");
                    }
                } catch (Exception e) {
                    System.out.println("client closed" );
                }
            }
        }
    }
    private class okListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            SendThread();
        }
    }

    private class clearListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            content.getText().trim();
            content.setText(null);

        }
    }

    private class clientloginListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            login.setEnabled(false);
            clientName.setEnabled(false);
            connect();
            new Thread(new ClientServer()).start();
            new Thread(new RecvThread()).start();
        }
    }

    private class peerListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            System.out.println(clientList.getSelectedItem());
            peer = clientList.getSelectedItem();
            if(!peer.equals("群聊")){
                connectpeer(Integer.parseInt(clientInfo.get(peer)));
            }
            else{
                disconnectpeer();
            }
        }
    }
}


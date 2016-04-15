package com.szl.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

/**
 * Created by zsc on 2016/4/15.
 */
class OperateXML {
//    private static Document clientDocument;
    protected static Document recordDocument;

//    private Element clientsListRoot;
    private Element recordRoot;
    private TransformerFactory transformerFactory = TransformerFactory.newInstance();

    public OperateXML(String root){
        recordDocument = initDocument();
        recordRoot = recordDocument.createElement(root);
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

//    public void createElement(String clientName,String clientPort){
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
//
//
//        saveXML(clientDocument,"D:/ClientsList.xml");
//    }

    public void deleteElement(String clientName){
//        NodeList clientsList = clientDocument.getElementsByTagName("clients");
//        //列出每一个clients的NodeList
//        for(int i = 0;i< clientsList.getLength();i++){
//            NodeList clientsChildList = clientsList.item(i).getChildNodes();
//            if(clientsChildList.item(0).getTextContent().trim().equals(clientName)){
//                clientsList.item(i).getParentNode().removeChild(clientsList.item(i));
//            }
//        }
//
//        saveXML(clientDocument,"D:/ClientsList.xml");
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

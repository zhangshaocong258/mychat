package com.szl.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
import java.io.*;

/**
 * Created by zsc on 2016/4/15.
 * <p>
 * 作为父类，仅包含record，server端再增加content内容
 */
public class OperateXML {
    private static Document recordDocument;

    private Element recordRoot;
    private TransformerFactory transformerFactory = TransformerFactory.newInstance();

    public OperateXML() {
        recordDocument = initDocument();
        recordRoot = recordDocument.createElement("content");
        recordDocument.appendChild(recordRoot);
    }

    //初始化，要生成多个xml文件，必须多次初始化
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

    //得到Document作为saveXML的参数
    public static Document getRecordDocument() {
        return recordDocument;
    }

    //第一次添加时，生成record，以后的添加不生成record，直接更改内容
    public void createRecord(String chatRecord) {
        NodeList chatRecordList = recordDocument.getElementsByTagName("record");
        if (chatRecordList.getLength() == 0) {
            Element record = recordDocument.createElement("record");
            record.appendChild((recordDocument.createTextNode(chatRecord)));
            recordRoot.appendChild(record);
        } else {
            for (int i = 0; i < chatRecordList.getLength(); i++) {
                Node chatRecordNode = chatRecordList.item(i);
                chatRecordNode.setTextContent(chatRecord);
            }
        }
    }

    //保存文件
    public void saveXML(Document document, String path) {
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

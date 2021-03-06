package com.szl.utils;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by zsc on 2016/4/23.
 */
public class Dom4jXML {
    private static Document recordDocument;
    private Element recordRoot;

    public Dom4jXML() {
        recordDocument = initDocument();
        recordRoot = recordDocument.addElement("content");
    }

    public Document initDocument() {
        DocumentFactory documentFactory = new DocumentFactory();
        Document document = documentFactory.createDocument();
        return document;
    }

    public static Document getRecordDocument() {
        return recordDocument;
    }

    public void createRecord(String chatRecord) {
//        Element chatRecordList = recordDocument.getRootElement();
        List chatRecordList = recordRoot.elements();
        //格式化，第一行加换行符
        String formatChatRecord = "\n" + chatRecord;
        if (chatRecordList.size() == 0) {
            Element record = recordRoot.addElement("record");
            record.setText(formatChatRecord);
        } else {
            Element record = recordRoot.element("record");
            record.setText(formatChatRecord);
        }
    }

    //先创建文件夹，在创建文件
    public void saveXML(Document document, String dirPath, String filePath) {
        FileWriter fileWriter = null;
        File file = null;
        try {
            file = new File(dirPath);
            if (!file.exists()) {
                file.mkdirs();
            }
            OutputFormat outputFormat = new OutputFormat("   ", true, "UTF-8");
            fileWriter = new FileWriter(filePath);
            XMLWriter xmlWriter = new XMLWriter(fileWriter, outputFormat);
            xmlWriter.write(document);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}

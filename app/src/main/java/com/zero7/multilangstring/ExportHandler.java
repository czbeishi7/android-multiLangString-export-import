package com.zero7.multilangstring;

import com.alibaba.excel.EasyExcel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class ExportHandler {
  public static void main(String[] args) {
    new ExportHandler().export("");
  }

  private final String PREFIX_STRING = "atr_";
  private final String PREFIX_ARRAY = "arr_";

  /** 导出默认string.xml */
  public void export(String projectPath) {
    System.out.println("Start exporting => " + projectPath);
    final String valueFilePath = "/src/main/res/values/strings.xml";

    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      Document document = dbFactory.newDocumentBuilder().parse(projectPath + valueFilePath);
      List<ExportRow> rows = new ArrayList<>();

      // handle string node
      System.out.println("Step1 : read string label value....");
      NodeList nodeList = document.getElementsByTagName("string");
      int len = nodeList.getLength();
      for (int i = 0; i < len; i++) {
        Element element = (Element) nodeList.item(i);
        String translate = element.getAttribute("translatable");
        if ("false".equalsIgnoreCase(translate)) {
          continue;
        }
        String stubKey = PREFIX_STRING + rows.size();
        rows.add(new ExportRow(stubKey, element.getTextContent().replace("\'", "'")));
        element.setTextContent(stubKey);
      }

      // handle string-array node
      System.out.println("Step2 : read string-array label value....");
      NodeList arrayList = document.getElementsByTagName("string-array");
      len = arrayList.getLength();
      int arrayIndex = 1;
      for (int i = 0; i < len; i++) {
        Element element = (Element) arrayList.item(i);
        String translate = element.getAttribute("translatable");
        if ("false".equalsIgnoreCase(translate)) {
          continue;
        }
        NodeList itemList = element.getElementsByTagName("item");
        int itemLen = itemList.getLength();
        for (int ii = 0; ii < itemLen; ii++) {
          Element item = (Element) itemList.item(ii);
          String stubKey = PREFIX_ARRAY + arrayIndex++;
          rows.add(new ExportRow(stubKey, item.getTextContent().replace("\'", "'")));
          item.setTextContent(stubKey);
        }
      }

      // output dir
      File outDir = new File(System.getProperty("user.dir") + "/output");
      outDir.mkdirs();
      System.out.println("Step3 : create output dir " + outDir);

      // write xml
      System.out.println("Step4 : write values format.ml ....");
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // 设置输出格式为缩进
      DOMSource source = new DOMSource(document);
      StreamResult result = new StreamResult(new File(outDir, "format.xml")); // 指定输出文件路径
      transformer.transform(source, result);

      // write xml
      System.out.println("Step4 : write values data into xml ....");
      System.out.println("data size = " + rows.size());
      EasyExcel.write(new File(outDir, "data.xlsx"), ExportRow.class).sheet("string-data").doWrite(rows);
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("Finish exporting => " + projectPath);
  }

  public void exportAll(String projectPath) {
    final String valueDir = "/src/main/res";
    File file = new File(projectPath + valueDir);
    File[] files = file.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        if (pathname.isDirectory() && pathname.getName().startsWith("values")) {
          return new File(pathname, "strings.xml").exists();
        }
        return false;
      }
    });
    if (files == null) {
      return;
    }
    List<List<String>> header = new ArrayList<>();
    for (File fileTmp : files) {
      header.add(Arrays.asList(fileTmp.getName().contains("-") ? fileTmp.getName().split("-")[1] : "en"));
    }

    List<List<String>> allData = new ArrayList<>();
    for (List<String> lang : header) {
      String langFlag = lang.get(0).equals("en") ? "values" : ("values-" + lang.get(0));
      String valueFilePath = projectPath + valueDir + File.separator + langFlag + "/strings.xml";
      System.out.println("Start parse => " + valueFilePath);
      try {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        Document document = dbFactory.newDocumentBuilder().parse(valueFilePath);
        List<String> langData = new ArrayList<>();

        // handle string node
        System.out.println("Step1 : read string label value....");
        NodeList nodeList = document.getElementsByTagName("string");
        int len = nodeList.getLength();
        for (int i = 0; i < len; i++) {
          Element element = (Element) nodeList.item(i);
          String translate = element.getAttribute("translatable");
          if ("false".equalsIgnoreCase(translate)) {
            continue;
          }
          langData.add(element.getTextContent().replace("\'", "'"));
        }

        // handle string-array node
        System.out.println("Step2 : read string-array label value....");
        NodeList arrayList = document.getElementsByTagName("string-array");
        len = arrayList.getLength();
        for (int i = 0; i < len; i++) {
          Element element = (Element) arrayList.item(i);
          String translate = element.getAttribute("translatable");
          if ("false".equalsIgnoreCase(translate)) {
            continue;
          }
          NodeList itemList = element.getElementsByTagName("item");
          int itemLen = itemList.getLength();
          for (int ii = 0; ii < itemLen; ii++) {
            Element item = (Element) itemList.item(ii);
            langData.add(item.getTextContent().replace("\'", "'"));
          }
        }
        System.out.println("lang data size : " + langData.size());
        allData.add(langData);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    // output dir
    File outDir = new File(System.getProperty("user.dir") + "/output");
    outDir.mkdirs();
    System.out.println("Create output dir " + outDir);

    // data rows - column switch
    List<List<String>> resultData = new ArrayList<>();
    for (int i = 0; i < allData.get(0).size(); i++) {
      List<String> row = new ArrayList<>();
      for (int j = 0; j < allData.size(); j++) {
        row.add(allData.get(j).get(i));
      }
      resultData.add(row);
    }
    System.out.println("header size : " + header.size());
    System.out.println("data size : " + resultData.size());

    EasyExcel.write(new File(outDir, "multi-data.xlsx")).head(header).sheet("string-data").doWrite(resultData);
  }

}

package com.zero7.multilangstring;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellData;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class ImportHandler extends AnalysisEventListener<Map<Integer, String>> {
  public static void main(String[] args) {
    new ImportHandler().importMultiStrings(System.getProperty("user.dir") + "/output/multi-data.xlsx");
  }

  private Map<Integer, Map<String, String>> multiDataMap = new HashMap();
  private Map<Integer, String> langMap = new HashMap();

  public void importMultiStrings(String fileName) {
    EasyExcel.read(fileName, this).headRowNumber(1).sheet().doRead();
  }

  @Override
  public void invokeHead(Map<Integer, CellData> headMap, AnalysisContext context) {
    super.invokeHead(headMap, context);
    Set<Integer> keys = headMap.keySet();
    for (Integer key : keys) {
      if (0 != key) {
        multiDataMap.put(key, new HashMap());
        langMap.put(key, headMap.get(key).getStringValue());
      }
    }
    System.out.println(multiDataMap);
  }

  @Override
  public void invoke(Map<Integer, String> data, AnalysisContext context) {
    Set<Integer> keys = data.keySet();
    String rowKey = data.get(0);
    for (Integer key : keys) {
      if (0 != key) {
        multiDataMap.get(key).put(rowKey, data.get(key));
      }
    }
  }

  @Override
  public void doAfterAllAnalysed(AnalysisContext context) {
    System.out.println("doAfterAllAnalysed");
    Set<Integer> keys = langMap.keySet();
    for (Integer key : keys) {
      String langKey = langMap.get(key);
      System.out.println("handle lang : " + langKey);
      Map<String, String> langData = multiDataMap.get(key);
      System.out.println(langData);

      String outputPath = System.getProperty("user.dir") + "/output/";
      String outputResPath = outputPath + "res";
      new File(outputResPath).mkdirs();

      try {
        File xmlFile = new File(outputPath + "/format.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        Document document = dbFactory.newDocumentBuilder().parse(xmlFile);

        // handle string node
        NodeList nodeList = document.getElementsByTagName("string");
        int len = nodeList.getLength();
        for (int i = 0; i < len; i++) {
          Element element = (Element) nodeList.item(i);
          String translate = element.getAttribute("translatable");
          if ("false".equalsIgnoreCase(translate)) {
            continue;
          }
          element.setTextContent(langData.get(element.getTextContent()).replace("'", "\'"));
        }

        // handle string-array node
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
            item.setTextContent(langData.get(item.getTextContent()).replace("'", "\'"));
          }
        }
        // write xml
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // 设置输出格式为缩进
        DOMSource source = new DOMSource(document);
        String valueDir = outputResPath + File.separator + (langKey.equals("en") ? "values" : "values-" + langKey);
        System.out.println("output path : " + valueDir);
        new File(valueDir).mkdirs();
        StreamResult result = new StreamResult(new File(valueDir + "/string.xml")); // 指定输出文件路径
        transformer.transform(source, result);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}

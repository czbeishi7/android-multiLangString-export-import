package com.zero7.multilangstring;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;

@ContentRowHeight(18)
@HeadRowHeight(20)
public class ExportRow {
  @ExcelProperty(value = "flag", index = 0)
  @ColumnWidth(8)
  public String key;

  @ExcelProperty(value = "en", index = 1)
  @ColumnWidth(60)
  public String value;

  public ExportRow(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

}

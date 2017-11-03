package com.thinkgem.jeesite.common.mapper.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * Java对转换为Xml
 * 
 * @author Json
 *
 */
/*
 * @XmlType(name = "MapConvertor")  
 * 生成xml名字为MapConvertor
 * 
 * @XmlAccessorType  定义映射这个类中的何种类型需要映射到XML。可接收四个参数，分别是：
　　XmlAccessType.FIELD：映射这个类中的所有字段到XML
　　XmlAccessType.PROPERTY：映射这个类中的属性（get/set方法）到XML
　　XmlAccessType.PUBLIC_MEMBER：将这个类中的所有public的field或property同时映射到XML（默认）
　　XmlAccessType.NONE：不映射
 *
 */
@XmlType(name = "MapConvertor")  
@XmlAccessorType(XmlAccessType.FIELD)  
public class MapConvertor {
	
    private List<MapEntry> entries = new ArrayList<MapEntry>();  
  
    public void addEntry(MapEntry entry) {  
        entries.add(entry);  
    }  
  
    public List<MapEntry> getEntries() {  
        return entries;  
    }  
      
    public static class MapEntry {  
  
        private String key;  
  
        private Object value;  
          
        public MapEntry() {  
            super();  
        }  
  
        public MapEntry(Map.Entry<String, Object> entry) {  
            super();  
            this.key = entry.getKey();  
            this.value = entry.getValue();  
        }  
  
        public MapEntry(String key, Object value) {  
            super();  
            this.key = key;  
            this.value = value;  
        }  
  
        public String getKey() {  
            return key;  
        }  
  
        public void setKey(String key) {  
            this.key = key;  
        }  
  
        public Object getValue() {  
            return value;  
        }  
  
        public void setValue(Object value) {  
            this.value = value;  
        }  
    }  
}  
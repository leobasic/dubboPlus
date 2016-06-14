package com.dubbo.apps.test;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.dubbo.common.utils.ConcurrentHashSet;

public class DataBean implements Serializable {

	private static final long serialVersionUID = 1494098721169784276L;

	private String name;
	private ConcurrentHashSet<ConcurrentHashMap<String, String>> mapSet = new ConcurrentHashSet<ConcurrentHashMap<String, String>>();
	private ConcurrentHashMap<String, String> ccm = new ConcurrentHashMap<String, String>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	//1111
	public Iterator<ConcurrentHashMap<String, String>> getMapSet() {
		return mapSet.iterator();
	}

	public void addMapSet(ConcurrentHashMap<String, String> map) {
		this.mapSet.add(map);
	}
	//2222
	public void put(String k,String v) {
		this.ccm.put(k, v);
	}

	public String get(String k) {
		return this.ccm.get(k);
	}
	
	
	
	
	
	
	
	
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.server.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Administrator
 */
public class SessionManager<T> {
    private static SessionManager _instance;
    
    public static SessionManager getInstance() {
        if (_instance == null) {
            _instance = new SessionManager();
        }
        return _instance;
    }

    private final ConcurrentHashMap<Integer,String> _map;

    private SessionManager() {
        this._map = new ConcurrentHashMap<>();
    }
    public ConcurrentHashMap<Integer,String> getMap() {
        return _map;
    }
    

    /**
     * 获得实例
     *
     * @param key
     * @return 实例对象
     */
    public int get(String value) {
         for (Map.Entry<Integer, String> entrySet : _map.entrySet()) {
            if (entrySet.getValue() == null ? value == null : entrySet.getValue().equals(value)) {
                return entrySet.getKey();
            }
        }
        return 0;
    }
    
    /**
     * 获得实例
     *
     * @param value
     * @return 实例对象
     */
//    public String getValue(int value) {
//        
//        for(String key:_map.keySet()) {
//            if (Objects.equals(_map.get(key), value)) {
//                return key;
//            }
//        }
//        return null;
//    }

    /**
     * 设置实例
     *
     * @param key
     * @param value 实例对象
     */
    public void set(int key,String value) {
        _map.put(key, value);
    }
    
//    public void setValue(String key,int value){
//        _map.remove(key);
//        _map.put(key, value);
//    }

    /**
     * 删除实例
     *
     * @param key
     * @return 删除的值
     */
    public Object remove(int key) {
        return _map.remove(key);
    }
    
}

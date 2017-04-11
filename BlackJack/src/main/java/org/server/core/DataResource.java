/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.server.core;

import java.util.List;

/**
 * 游戏数据资源表
 *
 * @author Administrator
 * @param <T> 资源类型
 */
public class DataResource<T> {
    
    private final String version;
    private final List<T> datas;
    
    public DataResource(String version, List<T> datas) {
        this.version = version;
        this.datas = datas;
    }

    public String getVersion() {
        return version;
    }

    public List<T> getDatas() {
        return datas;
    }
}

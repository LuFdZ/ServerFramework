package org.server.core.model;
// Generated 2015-7-10 15:37:19 by Hibernate Tools 4.3.1



/**
 * SysSequence generated by hbm2java
 */
public class SysSequence  implements java.io.Serializable {


     private String name;
     private long currentValue;
     private long increment;

    public SysSequence() {
    }

    public SysSequence(String name, long currentValue, long increment) {
       this.name = name;
       this.currentValue = currentValue;
       this.increment = increment;
    }
   
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    public long getCurrentValue() {
        return this.currentValue;
    }
    
    public void setCurrentValue(long currentValue) {
        this.currentValue = currentValue;
    }
    public long getIncrement() {
        return this.increment;
    }
    
    public void setIncrement(long increment) {
        this.increment = increment;
    }




}


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lobzik.smspi.tests;
import java.util.Properties;
/**
 *
 * @author lobzik
 */
public class PropsTest {
    public static void main(String[] args) { 
        Properties props = System.getProperties();
        for (Object key: props.keySet()){
            System.out.println(key + "=" + props.get(key));
        }
       
    }
    
}


package com.aotain.smmsapi.task.client.cxf.tj;

import javax.xml.ws.Endpoint;

/**
 * This class was generated by Apache CXF 2.7.12
 * 2015-12-29T12:41:40.964+08:00
 * Generated source version: 2.7.12
 * 
 */
 
public class CommandackPortType_CommandackHttpPort_Server{

    protected CommandackPortType_CommandackHttpPort_Server() throws java.lang.Exception {
        System.out.println("Starting Server");
        Object implementor = new CommandackPortTypeImpl();
        String address = "http://60.29.111.230:8002/IDCWebService_smms/commandack";
        Endpoint.publish(address, implementor);
    }
    
    public static void main(String args[]) throws java.lang.Exception { 
        new CommandackPortType_CommandackHttpPort_Server();
        System.out.println("Server ready..."); 
        
        Thread.sleep(5 * 60 * 1000); 
        System.out.println("Server exiting");
        System.exit(0);
    }
}
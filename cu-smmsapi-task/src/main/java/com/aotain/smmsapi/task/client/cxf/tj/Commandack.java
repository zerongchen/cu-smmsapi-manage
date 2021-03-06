package com.aotain.smmsapi.task.client.cxf.tj;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.7.12
 * 2015-12-29T12:41:40.968+08:00
 * Generated source version: 2.7.12
 * 
 */
@WebServiceClient(name = "commandack", 
                  wsdlLocation = "http://60.29.111.230:8002/IDCWebService_smms/commandack?wsdl",
                  targetNamespace = "http://company.service.webservice.smms.ncs.com") 
public class Commandack extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://company.service.webservice.smms.ncs.com", "commandack");
    public final static QName CommandackHttpPort = new QName("http://company.service.webservice.smms.ncs.com", "commandackHttpPort");
    static {
        URL url = null;
        try {
            url = new URL("http://60.29.111.230:8002/IDCWebService_smms/commandack?wsdl");
        } catch (MalformedURLException e) {
            java.util.logging.Logger.getLogger(Commandack.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "http://60.29.111.230:8002/IDCWebService_smms/commandack?wsdl");
        }
        WSDL_LOCATION = url;
    }

    public Commandack(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public Commandack(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public Commandack() {
        super(WSDL_LOCATION, SERVICE);
    }
    
    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public Commandack(WebServiceFeature ... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public Commandack(URL wsdlLocation, WebServiceFeature ... features) {
        super(wsdlLocation, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public Commandack(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     *
     * @return
     *     returns CommandackPortType
     */
    @WebEndpoint(name = "commandackHttpPort")
    public CommandackPortType getCommandackHttpPort() {
        return super.getPort(CommandackHttpPort, CommandackPortType.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns CommandackPortType
     */
    @WebEndpoint(name = "commandackHttpPort")
    public CommandackPortType getCommandackHttpPort(WebServiceFeature... features) {
        return super.getPort(CommandackHttpPort, CommandackPortType.class, features);
    }

}

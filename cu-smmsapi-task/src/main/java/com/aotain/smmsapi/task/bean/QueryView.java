//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.06.14 at 02:35:40 下午 CST 
//


package com.aotain.smmsapi.task.bean;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="commandId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="idcId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="content" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="queryTime" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="timeStamp" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "commandId",
    "idcId",
    "type",
    "content",
    "queryTime",
    "timeStamp"
})
@XmlRootElement(name = "queryView")
public class QueryView {

    protected long commandId;
    @XmlElement(required = true)
    protected String idcId;
    @XmlElement(required = true)
    protected BigInteger type;
    @XmlElement(required = true)
    protected String content;
    @XmlElement(required = true)
    protected String queryTime;
    @XmlElement(required = true)
    protected String timeStamp;

    /**
     * Gets the value of the commandId property.
     * 
     */
    public long getCommandId() {
        return commandId;
    }

    /**
     * Sets the value of the commandId property.
     * 
     */
    public void setCommandId(long value) {
        this.commandId = value;
    }

    /**
     * Gets the value of the idcId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdcId() {
        return idcId;
    }

    /**
     * Sets the value of the idcId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdcId(String value) {
        this.idcId = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setType(BigInteger value) {
        this.type = value;
    }

    /**
     * Gets the value of the content property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the value of the content property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContent(String value) {
        this.content = value;
    }

    /**
     * Gets the value of the queryTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQueryTime() {
        return queryTime;
    }

    /**
     * Sets the value of the queryTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQueryTime(String value) {
        this.queryTime = value;
    }

    /**
     * Gets the value of the timeStamp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimeStamp() {
        return timeStamp;
    }

    /**
     * Sets the value of the timeStamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimeStamp(String value) {
        this.timeStamp = value;
    }

}

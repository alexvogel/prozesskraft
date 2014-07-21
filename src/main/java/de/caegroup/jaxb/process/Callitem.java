//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.07.21 at 03:57:21 PM CEST 
//


package de.caegroup.jaxb.process;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *       &lt;attribute name="loop" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="del" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="par" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="sequence" type="{http://www.w3.org/2001/XMLSchema}integer" default="0" />
 *       &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "callitem")
public class Callitem {

    @XmlAttribute(name = "loop")
    protected String loop;
    @XmlAttribute(name = "del")
    protected String del;
    @XmlAttribute(name = "par", required = true)
    protected String par;
    @XmlAttribute(name = "sequence")
    protected BigInteger sequence;
    @XmlAttribute(name = "val")
    protected String val;

    /**
     * Gets the value of the loop property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLoop() {
        return loop;
    }

    /**
     * Sets the value of the loop property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLoop(String value) {
        this.loop = value;
    }

    /**
     * Gets the value of the del property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDel() {
        return del;
    }

    /**
     * Sets the value of the del property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDel(String value) {
        this.del = value;
    }

    /**
     * Gets the value of the par property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPar() {
        return par;
    }

    /**
     * Sets the value of the par property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPar(String value) {
        this.par = value;
    }

    /**
     * Gets the value of the sequence property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSequence() {
        if (sequence == null) {
            return new BigInteger("0");
        } else {
            return sequence;
        }
    }

    /**
     * Sets the value of the sequence property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSequence(BigInteger value) {
        this.sequence = value;
    }

    /**
     * Gets the value of the val property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVal() {
        return val;
    }

    /**
     * Sets the value of the val property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVal(String value) {
        this.val = value;
    }

}

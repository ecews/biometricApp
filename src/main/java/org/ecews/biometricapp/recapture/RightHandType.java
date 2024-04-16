//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2023.05.26 at 09:32:03 AM WAT 
//


package org.ecews.biometricapp.recapture;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for rightHandType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="rightHandType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RightThumb" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HashedRightThumb" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RightThumbQuality" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="RightIndex" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HashedRightIndex" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RightIndexQuality" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="RightMiddle" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HashedRightMiddle" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RightMiddleQuality" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="RightWedding" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HashedRightWedding" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RightWeddingQuality" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="RightSmall" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HashedRightSmall" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RightSmallQuality" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "rightHandType", propOrder = {
    "rightThumb",
    "hashedRightThumb",
    "rightThumbQuality",
    "rightIndex",
    "hashedRightIndex",
    "rightIndexQuality",
    "rightMiddle",
    "hashedRightMiddle",
    "rightMiddleQuality",
    "rightWedding",
    "hashedRightWedding",
    "rightWeddingQuality",
    "rightSmall",
    "hashedRightSmall",
    "rightSmallQuality"
})
public class RightHandType {

    @XmlElement(name = "RightThumb")
    protected String rightThumb;
    @XmlElement(name = "HashedRightThumb")
    protected String hashedRightThumb;
    @XmlElement(name = "RightThumbQuality")
    protected Integer rightThumbQuality;
    @XmlElement(name = "RightIndex")
    protected String rightIndex;
    @XmlElement(name = "HashedRightIndex")
    protected String hashedRightIndex;
    @XmlElement(name = "RightIndexQuality")
    protected Integer rightIndexQuality;
    @XmlElement(name = "RightMiddle")
    protected String rightMiddle;
    @XmlElement(name = "HashedRightMiddle")
    protected String hashedRightMiddle;
    @XmlElement(name = "RightMiddleQuality")
    protected Integer rightMiddleQuality;
    @XmlElement(name = "RightWedding")
    protected String rightWedding;
    @XmlElement(name = "HashedRightWedding")
    protected String hashedRightWedding;
    @XmlElement(name = "RightWeddingQuality")
    protected Integer rightWeddingQuality;
    @XmlElement(name = "RightSmall")
    protected String rightSmall;
    @XmlElement(name = "HashedRightSmall")
    protected String hashedRightSmall;
    @XmlElement(name = "RightSmallQuality")
    protected Integer rightSmallQuality;

    /**
     * Gets the value of the rightThumb property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRightThumb() {
        return rightThumb;
    }

    /**
     * Sets the value of the rightThumb property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRightThumb(String value) {
        this.rightThumb = value;
    }

    /**
     * Gets the value of the hashedRightThumb property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHashedRightThumb() {
        return hashedRightThumb;
    }

    /**
     * Sets the value of the hashedRightThumb property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHashedRightThumb(String value) {
        this.hashedRightThumb = value;
    }

    /**
     * Gets the value of the rightThumbQuality property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getRightThumbQuality() {
        return rightThumbQuality;
    }

    /**
     * Sets the value of the rightThumbQuality property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setRightThumbQuality(Integer value) {
        this.rightThumbQuality = value;
    }

    /**
     * Gets the value of the rightIndex property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRightIndex() {
        return rightIndex;
    }

    /**
     * Sets the value of the rightIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRightIndex(String value) {
        this.rightIndex = value;
    }

    /**
     * Gets the value of the hashedRightIndex property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHashedRightIndex() {
        return hashedRightIndex;
    }

    /**
     * Sets the value of the hashedRightIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHashedRightIndex(String value) {
        this.hashedRightIndex = value;
    }

    /**
     * Gets the value of the rightIndexQuality property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getRightIndexQuality() {
        return rightIndexQuality;
    }

    /**
     * Sets the value of the rightIndexQuality property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setRightIndexQuality(Integer value) {
        this.rightIndexQuality = value;
    }

    /**
     * Gets the value of the rightMiddle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRightMiddle() {
        return rightMiddle;
    }

    /**
     * Sets the value of the rightMiddle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRightMiddle(String value) {
        this.rightMiddle = value;
    }

    /**
     * Gets the value of the hashedRightMiddle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHashedRightMiddle() {
        return hashedRightMiddle;
    }

    /**
     * Sets the value of the hashedRightMiddle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHashedRightMiddle(String value) {
        this.hashedRightMiddle = value;
    }

    /**
     * Gets the value of the rightMiddleQuality property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getRightMiddleQuality() {
        return rightMiddleQuality;
    }

    /**
     * Sets the value of the rightMiddleQuality property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setRightMiddleQuality(Integer value) {
        this.rightMiddleQuality = value;
    }

    /**
     * Gets the value of the rightWedding property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRightWedding() {
        return rightWedding;
    }

    /**
     * Sets the value of the rightWedding property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRightWedding(String value) {
        this.rightWedding = value;
    }

    /**
     * Gets the value of the hashedRightWedding property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHashedRightWedding() {
        return hashedRightWedding;
    }

    /**
     * Sets the value of the hashedRightWedding property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHashedRightWedding(String value) {
        this.hashedRightWedding = value;
    }

    /**
     * Gets the value of the rightWeddingQuality property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getRightWeddingQuality() {
        return rightWeddingQuality;
    }

    /**
     * Sets the value of the rightWeddingQuality property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setRightWeddingQuality(Integer value) {
        this.rightWeddingQuality = value;
    }

    /**
     * Gets the value of the rightSmall property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRightSmall() {
        return rightSmall;
    }

    /**
     * Sets the value of the rightSmall property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRightSmall(String value) {
        this.rightSmall = value;
    }

    /**
     * Gets the value of the hashedRightSmall property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHashedRightSmall() {
        return hashedRightSmall;
    }

    /**
     * Sets the value of the hashedRightSmall property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHashedRightSmall(String value) {
        this.hashedRightSmall = value;
    }

    /**
     * Gets the value of the rightSmallQuality property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getRightSmallQuality() {
        return rightSmallQuality;
    }

    /**
     * Sets the value of the rightSmallQuality property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setRightSmallQuality(Integer value) {
        this.rightSmallQuality = value;
    }

}

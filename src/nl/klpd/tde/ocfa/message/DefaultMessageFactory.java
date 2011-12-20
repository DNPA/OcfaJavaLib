package nl.klpd.tde.ocfa.message;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


import nl.klpd.tde.ocfa.misc.OcfaException;
import nl.klpd.tde.ocfa.misc.XmlUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
/**
 *  Messagefactory that parses the current implementation of messages. An example is:
 *  
 * <l2wrapper id="0" type="user" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="ocfa.xsd">
 *
 * <message prio="0">
 *   <sender host="localhost" instance="notset" module="anycast" namespace="anycast"/>
 *   <unicast host="127.0.0.1" instance="Inst35016" module="digest" namespace="default"/>
 *   <moduleinstance subject="127.0.0.1:digest:default:Inst35016"></moduleinstance>
 * </message>
 *
 *</l2wrapper>
 * 
 * 
 * @author joep
 * @codereview jochen
 */
public class DefaultMessageFactory {
	//Dom stuff for parsing xml into domtree.
	private DocumentBuilderFactory factory;
	private DocumentBuilder builder;
	private static Log logger = LogFactory.getLog(DefaultMessageFactory.class);

	private ModuleInstance sender;
	public ModuleInstance getSender() {
		return sender;
	}
	public void setSender(ModuleInstance sender) {
		this.sender = sender;
	}
	public DefaultMessageFactory() throws ParserConfigurationException, TransformerConfigurationException{
		
		 factory = DocumentBuilderFactory.newInstance();
		 builder = factory.newDocumentBuilder();		 
	}
	
	/**
	 * Creates a message from a string.
	 * @param inString
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 */
	public Message parseMessage(String inString) throws SAXException, IOException{
		
	
		Document document = builder.parse(new InputSource(new StringReader(inString)));
		DefaultMessage message = new DefaultMessage();
		
		String typeString = document.getDocumentElement().getAttribute("type");
		String idString = document.getDocumentElement().getAttribute("id");
		if (idString != null){
			
			message.setId(Integer.parseInt(idString));
		}
		message.setMessageType(MessageType.getMessageType(typeString));
		
		
		Element element = (Element)document.getElementsByTagName("message").item(0);
		if (element.getAttribute("prio") != null){
			
			message.setPrio(Integer.parseInt(element.getAttribute("prio")));
		}
		NodeList nodes = element.getChildNodes();
		for (int x = 0; x < nodes.getLength(); x++){
			//iterate over all child nodes
			if (nodes.item(x).getNodeType() == Node.ELEMENT_NODE){
				processNode(nodes.item(x), message);
			}
		}

		return message;
		
	}
	/**
	 * Converts a message back into a string ready to be sent over a sockt.
	 * @param inMessage
	 * @return
	 * @throws OcfaException
	 */
	public String asPlainText(Message inMessage) throws OcfaException{
		
		Document document = builder.newDocument();
		Element wrapElement = document.createElement("l2wrapper");
		wrapElement.setAttribute("type", inMessage.getMessageType().toString());
		wrapElement.setAttribute("id", Integer.toString(inMessage.getId()));
		
		document.appendChild(wrapElement);
		Element messageElement = document.createElement("message");
		messageElement.setAttribute("prio", Integer.toString(inMessage.getPrio()));
		wrapElement.appendChild(messageElement);
		addModuleInstanceNode(messageElement, document.createElement("sender"), getSender());	
		switch (inMessage.getCastType()){
		
			case ANYCAST:
				addModuleInstanceNode(messageElement, document.createElement("anycast"), inMessage.getReceiver());
				break;
			case BROADCAST:
				addModuleInstanceNode(messageElement, document.createElement("broadcast"), inMessage.getReceiver());
				break;
			case UNICAST:
				addModuleInstanceNode(messageElement, document.createElement("unicast"), inMessage.getReceiver());
				break;		
		}
		if (inMessage.getContentType() != null){
				
			addContentNode(messageElement, document.createElement(inMessage.getContentType().getType()),
						inMessage.getSubject(), inMessage.getContent());
			
			
		}
		try {
			return XmlUtil.domToText(document);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new OcfaException(e.getMessage());
		}
		
		
	}
	/**
	 * inits and adds the content node.
	 * @param wrapElement
	 * @param createElement
	 * @param subject
	 * @param content
	 */
	private void addContentNode(Element wrapElement, Element inContentElement,
			String subject, String content) {
		
		if (subject != null){
			
			inContentElement.setAttribute("subject", subject);
		}
		if (content != null){
			
			inContentElement.setTextContent(content);
		}
		wrapElement.appendChild(inContentElement);
		
	}
	
	/**
	 * inits and adds the module elment.
	 * @param wrapElement
	 * @param moduleElement
	 * @param inInstance
	 */
	private void addModuleInstanceNode(Element wrapElement,
			Element moduleElement, ModuleInstance inInstance) {
		
		addOptionalAttribute(moduleElement, "namespace", inInstance.getNamespace());
		addOptionalAttribute(moduleElement, "module", inInstance.getModuleName());
		addOptionalAttribute(moduleElement, "host", inInstance.getHost());
		addOptionalAttribute(moduleElement, "instance", inInstance.getInstance());

		wrapElement.appendChild(moduleElement);
	}
	
	/**
	 * Helper method. adds value as an attributer to inElmeentName if value is not null.
	 * @param moduleElement
	 * @param inElementName
	 * @param value
	 */
	private void addOptionalAttribute(Element moduleElement, String inElementName,
			String value) {
		
		if (value != null){
			
			moduleElement.setAttribute(inElementName, value);
		}
	}
	
	private void processNode(Node item, DefaultMessage outMessage) {
		
		String nodeName = item.getNodeName();
		logger.info("node name is " + nodeName);
		if (nodeName.equals("sender")){
			
			outMessage.setSender(parseModuleInstance((Element)item));
		} else if (nodeName.equals("unicast")){
			
			outMessage.setCastType(CastType.UNICAST);
			outMessage.setReceiver(parseModuleInstance((Element) item));
		} else if (nodeName.equals("evidence")){
			
			outMessage.setContentType(ContentType.EVIDENCE);
			outMessage.setContent(((Element) item).getTextContent());
			outMessage.setSubject(((Element) item).getAttribute("subject"));
		} else {
			
			ContentType type = ContentType.getContentType(nodeName);
			if (type != null){
				
				outMessage.setContentType(type);
				Element elem = (Element) item;
				if (elem.hasAttribute("subject")){
					
					outMessage.setSubject(elem.getAttribute("subject"));
				} 
				if (elem.getTextContent() != null){
					
					outMessage.setContent(elem.getTextContent());
				}
			} else {
				
				logger.warn("can not understand message type " + nodeName);
			}
		}
	}

	public Message createMessage(){
		
		
		return new DefaultMessage();
	}
	
	
	private ModuleInstance parseModuleInstance(Element item) {
		
		
		String host = item.getAttribute("host");
		String instance = item.getAttribute("instance");
		String namespace = item.getAttribute("namespace");
		String moduleName = item.getAttribute("module");
		ModuleInstance modInstance = new ModuleInstance();
		modInstance.setHost(host);
		modInstance.setInstance(instance);
		modInstance.setModuleName(moduleName);
		modInstance.setNamespace(namespace);
		if (item.getAttribute("port") != null && item.getAttribute("port").length() > 1){
		
			modInstance.setPort(Integer.parseInt(item.getAttribute("port")));
		}
		return modInstance;
	}
	
	
}

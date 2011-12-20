package nl.klpd.tde.ocfa.misc;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
/**
 * Some xml utilities which were used in messaging and store.
 * @author joep
 * @codereview jochen
 */
public class XmlUtil {
	
	private static Transformer transformer;

	static {
		
		 TransformerFactory tFactory = TransformerFactory.newInstance();
		 try {
			transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			
			
		}
		 transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		
	}
	
	
	public static  String domToText(Document inDocument) throws TransformerException{

		StringWriter output = new StringWriter();
		DOMSource source = new DOMSource(inDocument);
		StreamResult result = new StreamResult(output);
		transformer.transform(source, result); 
		//this.getWatcher().setProgressText("saved " + getEditedFile().getAbsolutePath());

		return output.toString();

	}
}

package at.tugraz.ist.debugging.modelbased.evaluation;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface IXmlLoggable extends Serializable {
	public Element getXMLNode(Document document);
}

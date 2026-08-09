package it.gov.pagopa.decoupler.service.middleware.mapper;

import it.gov.pagopa.decoupler.controller.middleware.exception.XMLParseException;
import it.gov.pagopa.decoupler.service.model.XMLContent;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.StringReader;
import java.util.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

@ApplicationScoped
public class XMLParser {

  private final XMLInputFactory xmlInputFactory;

  public XMLParser() {
    this.xmlInputFactory = XMLInputFactory.newInstance();
    this.xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> parse(String xml) throws XMLParseException {

    Map<String, Object> xmlAsMap = null;

    try {
      // Create a new XML stream reader from XMLInputFactory
      XMLStreamReader reader = this.xmlInputFactory.createXMLStreamReader(new StringReader(xml));

      // Defining the structures that contains hierarchy of XML tags during parsing
      Deque<Map<String, Object>> tagHierarchy = new ArrayDeque<>();
      Deque<String> tagNames = new ArrayDeque<>();

      // Defining a new buffer that will include the content of current tag
      StringBuilder currentTagBuffer = new StringBuilder();

      // Iterating untin there are more parsing events
      while (reader.hasNext()) {

        switch (reader.next()) {

          // If the event is related to a new open tag, extract its name
          // and add a new empty map in order to include the element
          case XMLStreamConstants.START_ELEMENT -> {
            String name = reader.getName().toString();
            Map<String, Object> currentTag = new LinkedHashMap<>();

            // Extract tag attributes with structure @attr
            for (int i = 0; i < reader.getAttributeCount(); i++) {
              String attrPrefix = reader.getAttributePrefix(i);
              attrPrefix = attrPrefix == null || attrPrefix.isBlank() ? "" : attrPrefix + ":";
              String attrName = reader.getAttributeLocalName(i);
              String attrValue = reader.getAttributeValue(i);
              currentTag.put(XMLContent.TAG_ATTRIBUTE_PREFIX + attrPrefix + attrName, attrValue);
            }

            // Extract tag prefix with structure #prefix
            if (name.contains(":")) {
              String[] elements = name.split(":", 2);
              currentTag.put(XMLContent.TAG_PREFIX_IDENTIFIER, elements[0]);
              name = elements[1];
            }

            tagHierarchy.push(currentTag);
            tagNames.push(name);
          }

          // If the event is related to content in tags, extract text from
          // current tag, incrementing the buffer
          case XMLStreamConstants.CHARACTERS -> {

            // currentTagBuffer = reader.getText().trim();
            if (reader.getText() != null) {
              currentTagBuffer.append(reader.getText().trim());
            }
          }

          // If the event is related to a tag closure, generate its value and
          // add it to hierarchical map
          case XMLStreamConstants.END_ELEMENT -> {

            // Extract the structure and the tag name
            String tagName = tagNames.pop();
            Map<String, Object> tagStructure = tagHierarchy.pop();

            // If the content buffer is not empty, include this value in
            // the map representing the tag element, setting the key as TAG_CONTENT_IDENTIFIER
            String text = currentTagBuffer.toString().trim();
            if (!text.isEmpty()) {
              tagStructure.put(XMLContent.TAG_CONTENT_IDENTIFIER, text);
            }
            currentTagBuffer.delete(0, currentTagBuffer.length());

            // If the hierarchy is empty, it is a root node. So define a new
            // map to the final structure
            if (tagHierarchy.isEmpty()) {

              xmlAsMap = Map.of(tagName, tagStructure);

            } else {

              // If the hierarchy is not empty, some other tags were added
              // in previous steps. So, take the parent of the extracted tag
              // and check if the tag element was already inserted in a previous step.
              Map<String, Object> tagParent = tagHierarchy.peek();
              Object tagValue = tagParent.get(tagName);

              // If the tag does not exist, add it as simple object
              if (tagValue == null) {
                tagParent.put(tagName, tagStructure);
              }

              // If the tag exists as a list, add it as new object of the list
              else if (tagValue instanceof List<?>) {
                ((List<Object>) tagValue).add(tagStructure);
              }

              // If the tag exists as a single element, replace it as a new list
              else {

                List<Object> list = new ArrayList<>();
                list.add(tagValue);
                list.add(tagStructure);
                tagParent.put(tagName, list);
              }
            }
          }
        }
      }
    } catch (XMLStreamException e) {
      throw new XMLParseException(e);
    }

    return xmlAsMap;
  }
}

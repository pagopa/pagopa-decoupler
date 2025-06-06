package it.gov.pagopa.decoupler.service.model;

import it.gov.pagopa.decoupler.controller.middleware.exception.XMLParseException;
import it.gov.pagopa.decoupler.service.middleware.mapper.XMLParser;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.mapstruct.ap.internal.util.Strings;

public class XMLContent {

  public static final String TAG_STATIC_PREFIX = "%";
  public static final String TAG_ATTRIBUTE_PREFIX = "@";
  public static final List<String> TAG_SPECIAL_PREFIXES =
      List.of(TAG_STATIC_PREFIX, TAG_ATTRIBUTE_PREFIX);
  public static final String TAG_CONTENT_IDENTIFIER = TAG_STATIC_PREFIX + "value";
  public static final String TAG_PREFIX_IDENTIFIER = TAG_STATIC_PREFIX + "prefix";

  private Map<String, Object> parsedXML;

  @Getter private Map<String, String> headers;

  private XMLParser parserRef;

  private Map<RequestProp, Object> requestProps;

  public static XMLContent fromRaw(XMLParser parser, String rawXML) throws XMLParseException {

    XMLContent xmlContent = new XMLContent();
    xmlContent.parsedXML = parser.parse(rawXML);
    xmlContent.parserRef = parser;
    return xmlContent;
  }

  public XMLContent withHeaders(HttpHeaders rawHeaders) {

    this.headers = new HashMap<>();
    MultivaluedMap<String, String> multivaluedRequestHeaders = rawHeaders.getRequestHeaders();
    for (String key : multivaluedRequestHeaders.keySet()) {
      List<String> values = multivaluedRequestHeaders.get(key);
      String singleValue = Strings.join(values, ",");
      this.headers.put(key.toLowerCase(), singleValue);
    }
    this.requestProps = new HashMap<>();
    return this;
  }

  public XMLContent withProp(RequestProp property, Object value) {

    setProp(property, value);
    return this;
  }

  public XMLContent build() {

    // Set primitive name from SOAPAction header
    String soapAction = this.headers.get("soapaction");
    if (soapAction != null) {
      this.withProp(RequestProp.PRIMITIVE_NAME, soapAction);
    }
    return this;
  }

  /**
   * @return
   */
  public String asRawString() {

    StringBuilder builder = new StringBuilder();
    this.parsedXML.forEach((key, value) -> extractXMLTag(builder, key, value));
    return builder.toString();
  }

  public void addHeader(String key, String value) {
    String existingValue = this.headers.get(key);
    if (existingValue != null) {
      existingValue += "," + value;
      this.headers.put(key, existingValue);
    } else {
      this.headers.put(key, value);
    }
  }

  public String getHeader(String key) {
    return this.headers.get(key);
  }

  public void setProp(RequestProp property, Object value) {
    this.requestProps.put(property, value);
  }

  public <T> T getProp(RequestProp property, Class<T> clazz) {
    T value;
    try {
      Object rawValue = this.requestProps.get(property);
      value = clazz.cast(rawValue);
    } catch (ClassCastException e) {
      value = null;
    }
    return value;
  }

  /**
   * The method permits to set the value of a field in the parsed XML. If the field does not exist,
   * this method will add it.<br>
   * This method <u>cannot be used</u> for add single element in a list.
   *
   * @param field
   * @param value
   */
  @SuppressWarnings("unchecked")
  public void setField(String field, Object value, int insertPosition) {

    // Split the path in parts
    String[] fieldStep = field.split("\\.");

    if (this.parsedXML != null && !this.parsedXML.isEmpty()) {

      Map<String, Object> currentMap = this.parsedXML;

      for (int i = 0; i < fieldStep.length - 1; i++) {
        String key = fieldStep[i];
        Object next = currentMap.get(key);

        if (next instanceof Map) {
          currentMap = (Map<String, Object>) next;
        } else {
          Map<String, Object> newMap = new LinkedHashMap<>();
          currentMap.put(key, newMap);
          currentMap = newMap;
        }
      }

      String lastKey = fieldStep[fieldStep.length - 1];

      LinkedHashMap<String, Object> newMap = new LinkedHashMap<>();
      int index = 0;
      boolean inserted = false;

      for (Map.Entry<String, Object> entry : currentMap.entrySet()) {

        String entryKey = entry.getKey();
        if (entryKey.contains(XMLContent.TAG_ATTRIBUTE_PREFIX)
            || XMLContent.TAG_PREFIX_IDENTIFIER.equals(entryKey)) {
          newMap.put(entryKey, entry.getValue());
        } else {
          if (!inserted && (index == insertPosition || entryKey.equals(lastKey))) {
            newMap.put(lastKey, Map.of(XMLContent.TAG_CONTENT_IDENTIFIER, value));
            inserted = true;
          }
          newMap.put(entryKey, entry.getValue());
          index++;
        }
      }

      if (!inserted) {
        newMap.put(lastKey, Map.of(XMLContent.TAG_CONTENT_IDENTIFIER, value));
      }

      currentMap.clear();
      currentMap.putAll(newMap);
    }
  }

  /**
   * The method permits to retrieve a field from the <u>original XML content</u>, returning it as
   * string value.
   *
   * @param field the required field in `path.to.field` format.
   * @return the field value as String.
   */
  public String getFieldAsString(String field) {

    String value = null;
    Object fieldValue = getFieldValue(field);
    if (fieldValue != null) {
      value = fieldValue.toString();
    }
    return value;
  }

  /**
   * The method permits to retrieve a field from the <u>original XML content</u>, returning it as
   * integer value.
   *
   * @param field the required field in `path.to.field` format.
   * @return the field value as Integer.
   */
  public Integer getFieldAsInteger(String field) {

    Integer value = null;
    Object fieldValue = getFieldValue(field);
    if (fieldValue != null) {
      value = Integer.parseInt(fieldValue.toString());
    }
    return value;
  }

  /**
   * The method permits to retrieve a field from the <u>original XML content</u>, returning it as
   * floating point value.
   *
   * @param field the required field in `path.to.field` format.
   * @return the field value as Double.
   */
  public Double getFieldAsDouble(String field) {

    Double value = null;
    Object fieldValue = getFieldValue(field);
    if (fieldValue != null) {
      value = Double.parseDouble(fieldValue.toString());
    }
    return value;
  }

  /**
   * The method permits to retrieve a field from the <u>original XML content</u>, returning it as
   * boolean value.
   *
   * @param field the required field in `path.to.field` format.
   * @return the field value as Boolean.
   */
  public Boolean getFieldAsBoolean(String field) {

    Boolean value = null;
    Object fieldValue = getFieldValue(field);
    if (fieldValue != null) {
      if ("1".equals(fieldValue)) {
        value = Boolean.TRUE;
      } else {
        value = Boolean.parseBoolean(fieldValue.toString());
      }
    }
    return value;
  }

  /**
   * The method permits to retrieve a field from the <u>original XML content</u>, returning it as
   * list of object values.
   *
   * @param field the required field in `path.to.field` format.
   * @return the field value as list of Object.
   */
  @SuppressWarnings("unchecked")
  public List<Object> getFieldAsList(String field) {

    List<Object> value = null;
    Object fieldValue = getFieldValue(field);
    if (fieldValue instanceof List<?> fieldValueAsList) {
      value = (List<Object>) fieldValueAsList;
    }
    return value;
  }

  @SuppressWarnings("unchecked")
  private Object getFieldValue(String field) {

    // Split the whole field in parts by dot
    String[] fieldStep = field.split("\\.");

    // Return null if the parsed XML is null
    if (this.parsedXML == null || this.parsedXML.isEmpty()) {
      return null;
    }
    Map<String, Object> fieldMap = this.parsedXML;

    //
    int currentIndex = 0;
    for (String step : fieldStep) {
      currentIndex++;

      String[] stepComponents = step.split("\\[");
      step = stepComponents[0];

      Object parentTag = fieldMap.get(step);
      if (parentTag instanceof Map<?, ?> parentTagAsMap) {
        fieldMap = (Map<String, Object>) parentTagAsMap;
      } else if (parentTag instanceof List<?> parentTagAsList) {
        if (stepComponents.length > 1) {
          String rawIndex = stepComponents[1];
          int index = Integer.parseInt(rawIndex.substring(0, rawIndex.length() - 1));
          fieldMap = (Map<String, Object>) ((List<Object>) parentTagAsList).get(index);
        } else if (currentIndex == fieldStep.length) {
          return parentTagAsList;
        }
      } else {
        return null;
      }
    }

    return fieldMap.get(XMLContent.TAG_CONTENT_IDENTIFIER);
  }

  private void extractXMLTag(StringBuilder contentBuilder, String tagName, Object node) {

    if (!(node instanceof Map<?, ?> tagNode)) {
      return;
    }

    // Defining tag value including prefix if present
    String prefix = (String) tagNode.get(XMLContent.TAG_PREFIX_IDENTIFIER);
    String fullTag = (prefix != null ? prefix + ":" : "") + tagName;

    // If present, extract attributes for the current tag.
    // The attributes are defined with TAG_ATTRIBUTE_PREFIX prefix char
    Map<String, String> attributes = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : tagNode.entrySet()) {
      String tagNodeComponent = entry.getKey().toString();
      if (tagNodeComponent.startsWith(XMLContent.TAG_ATTRIBUTE_PREFIX)) {
        attributes.put(tagNodeComponent.substring(1), entry.getValue().toString());
      }
    }

    // After the extraction of the attributes, it is time to include it
    // in XML tag with full tag content.
    contentBuilder.append("<").append(fullTag);
    attributes.forEach(
        (attributeName, attributeValue) ->
            contentBuilder
                .append(" ")
                .append(attributeName)
                .append("=\"")
                .append(escapeXml(attributeValue))
                .append("\""));
    contentBuilder.append(">");

    // After, the content value must be inserted between the tag delimiters
    Object value = tagNode.get(XMLContent.TAG_CONTENT_IDENTIFIER);
    if (value instanceof String strVal) {
      contentBuilder.append(escapeXml(strVal));
    }

    // But before closing this tag, it is required to add all nested tags!
    // In order to do so, this same method is invoked recursively on all
    // children tag nodes that did not start with special character.
    for (Map.Entry<?, ?> entry : tagNode.entrySet()) {
      String tagNodeComponent = entry.getKey().toString();
      if (!TAG_SPECIAL_PREFIXES.contains(tagNodeComponent.substring(0, 1))) {
        Object childTag = entry.getValue();
        if (childTag instanceof List<?> childrenTags) {
          for (Object tag : childrenTags) {
            extractXMLTag(contentBuilder, tagNodeComponent, tag);
          }
        } else {
          extractXMLTag(contentBuilder, tagNodeComponent, childTag);
        }
      }
    }

    // Finally, close this tag content.
    contentBuilder.append("</").append(fullTag).append(">");
  }

  private static String escapeXml(String input) {
    return input
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;");
  }
}

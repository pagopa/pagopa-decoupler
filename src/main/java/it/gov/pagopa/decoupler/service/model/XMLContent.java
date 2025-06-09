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

  private Map<RequestProp, Object> requestProps;

  /**
   * @param parser
   * @param rawXML
   * @return
   * @throws XMLParseException
   */
  public static XMLContent fromRaw(XMLParser parser, String rawXML) throws XMLParseException {

    XMLContent xmlContent = new XMLContent();
    xmlContent.parsedXML = parser.parse(rawXML);
    return xmlContent;
  }

  /**
   * @param rawHeaders
   * @return
   */
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

  /**
   * @param property
   * @param value
   * @return
   */
  public XMLContent withProp(RequestProp property, Object value) {

    setProp(property, value);
    return this;
  }

  /**
   * @return
   */
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

  /**
   * @param key
   * @param value
   */
  public void addHeader(String key, String value) {
    String existingValue = this.headers.get(key);
    if (existingValue != null) {
      existingValue += "," + value;
      this.headers.put(key, existingValue);
    } else {
      this.headers.put(key, value);
    }
  }

  /**
   * @param key
   * @return
   */
  public String getHeader(String key) {
    return this.headers.get(key);
  }

  /**
   * @param property
   * @param value
   */
  public void setProp(RequestProp property, Object value) {
    this.requestProps.put(property, value);
  }

  /**
   * @param property
   * @param clazz
   * @param <T>
   * @return
   */
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
   * This method <u>cannot be used</u> for add single element in a list, nor to populate the parsed
   * XML from zero.
   *
   * @param field
   * @param value
   */
  @SuppressWarnings("unchecked")
  public void setField(String field, Object value, int insertPosition) {

    // Split the whole path in section by dot
    String[] fieldStep = field.split("\\.");

    // Return if the parsed XML is null: this function must not be used
    // to populate the map from zero!
    if (this.parsedXML == null || this.parsedXML.isEmpty()) {
      return;
    }
    Map<String, Object> fieldMap = this.parsedXML;

    // For each single step (minus the last) extracted from path, it is required to
    // traverse through existing map, analyzing each node
    for (String step : fieldStep) {

      // If the extracted node, related to subpath step, is defined as
      // a map, set directly this node to the current analyzing node.
      Object childTag = fieldMap.get(step);
      if (childTag instanceof Map<?, ?> childTagAsMap) {
        fieldMap = (Map<String, Object>) childTagAsMap;
      }

      // WARNING: list case is not evaluated here, so currently no field
      // in list can be added!

      // If the extracted tag is null, create a new node in order to
      // generate a new field path to traverse
      else if (childTag == null) {
        Map<String, Object> newChildTag = new LinkedHashMap<>();
        fieldMap.put(step, newChildTag);
        fieldMap = newChildTag;
      }
    }

    // Initialize the element to be added in the last section of the map
    String lastField = fieldStep[fieldStep.length - 1];
    LinkedHashMap<String, Object> newChildTag = new LinkedHashMap<>();

    // Now it is time to check where to put the new value: the last field
    // is evaluated and, when the required insert position is reached or
    // an existing field is found, include the required value. Meanwhile,
    // re-generate the node map in the correct order.
    int index = 0;
    boolean inserted = false;
    for (Map.Entry<String, Object> entry : fieldMap.entrySet()) {

      // If the found element is an attribute or a prefix, simply re-add it
      // on the new map and don't count it on index calculation. Avoiding
      // index calculation on this case permits to not cause strange behavior
      // on sorted insertion.
      String entryKey = entry.getKey();
      if (entryKey.contains(XMLContent.TAG_ATTRIBUTE_PREFIX)
          || XMLContent.TAG_PREFIX_IDENTIFIER.equals(entryKey)) {
        newChildTag.put(entryKey, entry.getValue());
      }

      // If no insertion was previously made and the required position is
      // reached or a field with same name is found, directly set the value
      // on the same field. Otherwise, continue to re-insert elements in map.
      else {

        if (!inserted && (index == insertPosition || entryKey.equals(lastField))) {
          newChildTag.put(lastField, Map.of(XMLContent.TAG_CONTENT_IDENTIFIER, value));
          inserted = true;
        }
        newChildTag.put(entryKey, entry.getValue());
        index++;
      }
    }

    // If a field is not inserted nor an existing field is updated, then add it
    // anyway at the end of the node map.
    if (!inserted) {
      newChildTag.put(lastField, Map.of(XMLContent.TAG_CONTENT_IDENTIFIER, value));
    }

    // Finally, refresh the node map including the newly sorted node.
    fieldMap.clear();
    fieldMap.putAll(newChildTag);
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
    Object fieldValue = getField(field);
    if (fieldValue != null) {
      value = fieldValue.toString();
    }
    return value;
  }

  public String getFieldAsString(String field, String defaultValue) {
    String value = getFieldAsString(field);
    return value != null ? value : defaultValue;
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
    Object fieldValue = getField(field);
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
    Object fieldValue = getField(field);
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
    Object fieldValue = getField(field);
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
    Object fieldValue = getField(field);
    if (fieldValue instanceof List<?> fieldValueAsList) {
      value = (List<Object>) fieldValueAsList;
    }
    return value;
  }

  /**
   * @param field
   * @return
   */
  @SuppressWarnings("unchecked")
  public Object getField(String field) {

    // Split the whole path in section by dot
    String[] fieldStep = field.split("\\.");

    // Return null if the parsed XML is null
    if (this.parsedXML == null || this.parsedXML.isEmpty()) {
      return null;
    }
    Map<String, Object> fieldMap = this.parsedXML;

    // For each single step extracted from path, it is required to
    // navigate through existing map, analyzing each node
    int currentIndex = 0;
    for (String step : fieldStep) {

      // Extract the atomic components from single step, splitting
      // field name from array index (if existing) in case is required
      // to pass from array-based node.
      currentIndex++;
      String[] stepComponents = step.split("\\[");
      step = stepComponents[0];

      // If the extracted node, related to subpath step, is defined as
      // a map, set directly this node to the current analyzing node.
      Object childTag = fieldMap.get(step);
      if (childTag instanceof Map<?, ?> childTagAsMap) {
        fieldMap = (Map<String, Object>) childTagAsMap;
      }

      // If the extracted node, related to subpath step, is defined as
      // a list, then analyze different circumstances.
      else if (childTag instanceof List<?> childTagAsList) {

        // If the passed field is defined in format "path.to.field[index]",
        // extract the node at the required index. If the index is out-of-bound
        // i.e. the index is over the list size, return a null value.
        if (stepComponents.length > 1) {
          String rawIndex = stepComponents[1];
          int index = Integer.parseInt(rawIndex.substring(0, rawIndex.length() - 1));
          if (childTagAsList.size() <= index) {
            return null;
          }
          fieldMap = (Map<String, Object>) ((List<Object>) childTagAsList).get(index);
        }

        // If the passed field is not defined in format "path.to.field[index]",
        // directly return the list object.
        else if (currentIndex == fieldStep.length) {
          return childTagAsList;
        }

        // If none of the previous cases, automatically extract the node at the first
        // position of the list. If the list is empty, return a null value.
        else {
          if (childTagAsList.isEmpty()) {
            return null;
          }
          fieldMap = (Map<String, Object>) ((List<Object>) childTagAsList).get(0);
        }
      }

      // In other circumstances, the node is malformed so a null value is returned.
      else {
        return null;
      }
    }

    // Finally, return the content at TAG_CONTENT_IDENTIFIER key for the retrieved node
    return fieldMap.get(XMLContent.TAG_CONTENT_IDENTIFIER);
  }

  /**
   * @param contentBuilder
   * @param tagName
   * @param node
   */
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
                .append(escapeCharInXML(attributeValue))
                .append("\""));
    contentBuilder.append(">");

    // After, the content value must be inserted between the tag delimiters
    Object value = tagNode.get(XMLContent.TAG_CONTENT_IDENTIFIER);
    if (value instanceof String strVal) {
      contentBuilder.append(escapeCharInXML(strVal));
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

  /**
   * @param input
   * @return
   */
  private static String escapeCharInXML(String input) {
    return input
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;");
  }
}

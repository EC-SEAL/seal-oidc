package gr.uagean.loginWebApp.model.pojo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class AttributeNameList {

    private static Logger LOG = Logger.getLogger(AttributeNameList.class);

    private List<AttributeNameType> attributes;

    public AttributeNameList() {
        this.attributes = null;
    }

    public AttributeNameList(List<AttributeNameType> attributes) {
        this.attributes = attributes;
    }

    public List<AttributeNameType> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AttributeNameType> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "AttributeNameList{" +
                "attributes=" + attributes +
                '}';
    }

    public void fromJson(String jsonArray) throws IOException {
        //Parse json string, set as the expected list // TODO:
        LOG.info("JSON attribute list: " + jsonArray);
        ObjectMapper mapper = new ObjectMapper();
        List<AttributeNameType> attributes = mapper.readValue(jsonArray, new TypeReference<List<AttributeNameType>>() {
        });
        LOG.info("Unmarshalled attribute list: " + attributes.toString());
        this.attributes = attributes;
    }

    public void fromJsonFile(String filename) throws IOException {

        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(filename);
        //Open the stream and read the json from there // TODO:
        InputStream inputStream = resource.getInputStream();

        byte[] bdata = FileCopyUtils.copyToByteArray(inputStream);
        String jsonString = new String(bdata, StandardCharsets.UTF_8);
        LOG.info("read json attributes file: " + jsonString);

        this.fromJson(jsonString);
    }

    //Given an attribute name/friendlyName/claim, return the
    // AttributeNameType object that matches it
    public AttributeNameType find(String attribute) {
        for (AttributeNameType att : this.attributes) {
            if (att.match(attribute))
                return att;
        }
        return null;
    }

    //Given an attribute name/friendlyName/claim, return true if some attr matches it
    public Boolean exists(String attribute) {
        AttributeNameType attr = this.find(attribute);
        if (attr == null)
            return false;
        return true;
    }
}
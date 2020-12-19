import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.uagean.authenticators.SpmsEsmoAuthenticator;
import gr.uagean.loginWebApp.model.pojo.AttributeNameList;
import gr.uagean.loginWebApp.model.pojo.AttributeNameType;
import gr.uagean.loginWebApp.model.pojo.AttributeType;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import static org.junit.Assert.*;


public class AttributesTest {


    @Test
    public void basicTest() throws IOException {
        assertEquals("aaa", "aaa");
    }

    @Test
    public void readFileTest() throws IOException {
        AttributeNameList list = new AttributeNameList();
        list.fromJsonFile("classpath:attributes.json");
        //If no exceptions arise, it is successful
    }

    @Test
    public void AttributeNameFindTest() throws IOException {
        AttributeNameList list = new AttributeNameList();
        list.fromJsonFile("classpath:attributes.json");

        AttributeNameType att = null;

        att = list.find("notanactualattributename");
        assertNull(att);

        att = list.find("cn");
        assertEquals("2.5.4.3", att.getName());

        att = list.find("2.5.4.3");
        assertEquals("eocn", att.getClaim());

        att = list.find("eocn");
        assertEquals("cn", att.getFriendlyName());

        assertFalse(list.exists("notanactualattributename"));

        assertTrue(list.exists("http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier"));
        assertTrue(list.exists("PersonIdentifier"));
        assertTrue(list.exists("epi"));
    }

    @Test
    public void AttributeNameMatchTest() throws Exception {
        AttributeNameList list = new AttributeNameList();
        list.fromJsonFile("classpath:attributes.json");

        AttributeNameType att = null;

        att = list.find("cn");

        assertTrue(att.match("cn"));
        assertTrue(att.match("2.5.4.3"));
        assertTrue(att.match("eocn"));
        assertFalse(att.match("notthenamefriendlyorclaim"));

        assertEquals("2.5.4.3", att.resolveAttName("cn"));
        assertEquals("cn", att.resolveAttFriendlyName("cn"));
        assertEquals("eocn", att.resolveAttClaim("cn"));
        try {
            att.resolveAttName("failstring");
        } catch (Exception e) {
            assertTrue(true);
        }
        try {
            att.resolveAttFriendlyName("failstring");
        } catch (Exception e) {
            assertTrue(true);
        }
        try {
            att.resolveAttClaim("failstring");
        } catch (Exception e) {
            assertTrue(true);
        }
    }


    protected String resolveAttName(AttributeNameList acceptedAttributes, String claim) {
        AttributeNameType attr = acceptedAttributes.find(claim);
        if (attr != null)
            return attr.getName();
        return null;
    }

    protected String resolveAttFriendlyName(AttributeNameList acceptedAttributes, String claim) {
        AttributeNameType attr = acceptedAttributes.find(claim);
        if (attr != null)
            return attr.getFriendlyName();
        return null;
    }

    @Test
    public void esmoAuthenticatorTest1() throws IOException {
        AttributeNameList acceptedAttributes = new AttributeNameList();
        acceptedAttributes.fromJsonFile("classpath:attributes.json");

        List<AttributeNameType> allAttributes = acceptedAttributes.getAttributes();

        AttributeType[] attrType = new AttributeType[allAttributes.size()];
        String[] values = new String[1];


        int count = 0;
        for(AttributeNameType attName: allAttributes) {
            AttributeType att = new AttributeType(
                    attName.getName(),
                    attName.getFriendlyName(),
                    "UTF-8",
                    "en",
                    true, values
            );

            attrType[count] = att;
            count++;
        }

        assertEquals(attrType[0].getName(), acceptedAttributes.getAttributes().get(0).getName());
        assertEquals(attrType[0].getFriendlyName(), acceptedAttributes.getAttributes().get(0).getFriendlyName());

        String nameStr = this.resolveAttName(acceptedAttributes, "sho");
        String friendlyStr = this.resolveAttFriendlyName(acceptedAttributes, "sho");
        assertEquals("1.3.6.1.4.1.25178.1.2.9", nameStr);
        assertEquals("schacHomeOrganization", friendlyStr);
    }


    protected String getIdAttribute(Map<String, String> attributes, String[] identifiers){
        String value = null;
        for (int i = 0; i < identifiers.length; i++) {
            value = attributes.get(identifiers[i]);
            if (value != null)
                return value;
        }
        // Use a default
        return "SEAL_DefaultID";
    }

    protected AttributeType[] loadAttributes(String filename) throws IOException {

        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(filename);
        InputStream inputStream = resource.getInputStream();

        byte[] bdata = FileCopyUtils.copyToByteArray(inputStream);
        String jsonString = new String(bdata, StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        List<AttributeType> attributes = mapper.readValue(jsonString, new TypeReference<List<AttributeType>>() {});
        return attributes.toArray(new AttributeType[attributes.size()]);
    }


    @Test
    public void esmoAuthenticatorTest2() throws IOException {
        String ID_ATTRIBUTES = "schacPersonalUniqueID,schacPersonalUniqueCode,eduPersonTargetedID,eduPersonPrincipalName";

        AttributeNameList acceptedAttributes = new AttributeNameList();
        acceptedAttributes.fromJsonFile("classpath:attributes.json");

        Map<String, String> attributes = null;

        String[] identifiers =ID_ATTRIBUTES.split(",");
        assertEquals(4, identifiers.length);

        attributes = new HashMap<String, String>();

        AttributeType[] sealAttrs = this.loadAttributes("classpath:testAttributes.json");
        assertEquals(3, sealAttrs.length);

        for(AttributeType attr: sealAttrs) {
            System.out.println("---->" + attr.getName() + "," + attr.getFriendlyName());
        }

        for (AttributeType at : sealAttrs) {

            AttributeNameType attrName = acceptedAttributes.find(at.getName());
            if (attrName == null)
                attrName = acceptedAttributes.find(at.getFriendlyName());

            //Attribute not found in accepted attributes list
            if (attrName == null) {
                System.out.println("Attribute " + at.getName() + ", " + at.getFriendlyName() + "Not in accepted list");
                continue;
            }

            //Found. Storing value.
            attributes.put(attrName.getFriendlyName(), at.getValues()[0]);
        }
        System.out.println("+++----> len?: " + attributes.size());
        assertEquals(3, attributes.size());

        assertEquals("jdoe@nowhere.no", attributes.get("mail"));
        assertNull(attributes.get("notanexistingattr"));


        for (Map.Entry<String, String> pair : attributes.entrySet()) {
            String friendlyName = pair.getKey();
            String value = pair.getValue();
            assertNotNull(friendlyName);
            assertNotNull(value);
            System.out.println("+++---->" + friendlyName + ": " + value);
        }

        String idAttribute = this.getIdAttribute(attributes, identifiers);
        System.out.println("+++----> ID: " + idAttribute);
        assertEquals("12345678A", idAttribute);

        if(attributes != null)
            attributes.clear();
        System.out.println("+++----> len?: " + attributes.size());
        assertEquals(0, attributes.size());
        assertNull(attributes.get("mail"));

        System.out.flush();
    }
}

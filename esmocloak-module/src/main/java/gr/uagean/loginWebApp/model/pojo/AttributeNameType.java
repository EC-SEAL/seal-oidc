package gr.uagean.loginWebApp.model.pojo;

import java.util.Objects;

public class AttributeNameType {

    private String name;
    private String friendlyName;
    private String claim;

    public AttributeNameType() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getClaim() {
        return claim;
    }

    public void setClaim(String claim) {
        this.claim = claim;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttributeNameType)) return false;
        AttributeNameType that = (AttributeNameType) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getFriendlyName(), that.getFriendlyName()) &&
                Objects.equals(getClaim(), that.getClaim());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getFriendlyName(), getClaim());
    }

    @Override
    public String toString() {
        return "AttributeNameType{" +
                "name='" + name + '\'' +
                ", friendlyName='" + friendlyName + '\'' +
                ", claim='" + claim + '\'' +
                '}';
    }

    // If a string equals any of the fields, then return true
    public boolean match(String attr) {
        if (attr == null || attr.equals("")) return false;
        return attr.equals(this.friendlyName)
                || attr.equals(this.name)
                || attr.equals(this.claim);
    }

    // Given any of the fields values, return the name (if any
    // other string is passed, it will fail)
    public String resolveAttName(String attr) throws Exception {
        if (!this.match(attr))
            throw new Exception("Attr: " + attr + "is not represented in this instance");
        // If compares, then it's this attribute
        return this.getName();
    }

    // Given any of the fields values, return the friendlyName (if any
    // other string is passed, it will fail)
    public String resolveAttFriendlyName(String attr) throws Exception {
        if (!this.match(attr))
            throw new Exception("Attr: " + attr + "is not represented in this instance");
        // If compares, then it's this attribute
        return this.getFriendlyName();
    }

    // Given any of the fields values, return the claim (if any
    // other string is passed, it will fail)
    public String resolveAttClaim(String attr) throws Exception {
        if (!this.match(attr))
            throw new Exception("Attr: " + attr + "is not represented in this instance");
        // If compares, then it's this attribute
        return this.getClaim();
    }

}

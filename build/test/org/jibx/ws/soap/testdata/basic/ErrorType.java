package org.jibx.ws.soap.testdata.basic;

public class ErrorType
{
    private String type;

    /**
     * @param type
     */
    public ErrorType(String type) {
        this.type = type;
    }

    /**
     * Get type.
     *
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ErrorType other = (ErrorType) obj;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}

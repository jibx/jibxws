package org.jibx.ws.soap.testdata.basic;

public class ErrorMessage
{
    private String message;

    /**
     * @param message
     */
    public ErrorMessage(String message) {
        this.message = message;
    }

    /**
     * Get message.
     *
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((message == null) ? 0 : message.hashCode());
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
        final ErrorMessage other = (ErrorMessage) obj;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        return true;
    }
    
    
}

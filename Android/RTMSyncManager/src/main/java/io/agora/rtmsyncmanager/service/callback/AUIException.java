package io.agora.rtmsyncmanager.service.callback;

import androidx.annotation.NonNull;

/**
 * Class representing an exception in the Agora RTM Sync Manager.
 * <p>
 * This class extends the Exception class and adds a code property to represent the error code.
 */
public class AUIException extends Exception {
    /**
     * The error code associated with this exception.
     */
    public final int code;

    /**
     * Constructor for creating an AUIException.
     *
     * @param code The error code for this exception.
     * @param message The message for this exception.
     */
    public AUIException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Returns a string representation of this exception.
     * <p>
     * The string representation includes the error code and the message.
     *
     * @return A string representation of this exception.
     */
    @NonNull
    @Override
    public String toString() {
        return "AUIException{"
                + "code="
                + code
                + "message="
                + getMessage()
                + "}";
    }
}
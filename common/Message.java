package common;

import java.util.HashMap;
import java.util.Map;


public class Message {

   
    public enum Type {
        NAME, // Set player name
        READY, // Join matchmaking queue
        START, // Start game in room
        MOVE, // Submit move choice
        RESULT, // Round/game result
        STATUS, // Status message
        WELCOME, // Welcome message with client ID
        ERROR, // Error message
        DISCONNECT // Disconnect notification
    }

    public Type type;
    public Map<String, String> fields = new HashMap<>();


    public Message(Type type) {
        this.type = type;
    }

    @Deprecated
    public Message(MessageType oldType) {
        this.type = convertFromOldType(oldType);
    }

   
    public String toWireString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.name());

        if (!fields.isEmpty()) {
            sb.append("|");
            boolean first = true;
            for (Map.Entry<String, String> e : fields.entrySet()) {
                if (!first)
                    sb.append(";");
                sb.append(escape(e.getKey()));
                sb.append("=");
                sb.append(escape(e.getValue()));
                first = false;
            }
        }

        return sb.toString();
    }

    
    public static Message parse(String line) {
        // Handle null/empty
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        // Split into type and payload
        String[] parts = line.split("\\|", 2);

        // Parse message type
        Type type;
        try {
            type = Type.valueOf(parts[0].trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            // Unknown type -> return ERROR message
            Message errorMsg = new Message(Type.ERROR);
            errorMsg.fields.put("message", "Unknown message type: " + parts[0]);
            return errorMsg;
        }

        Message msg = new Message(type);

        // Parse fields if present
        if (parts.length > 1 && !parts[1].isEmpty()) {
            String payload = parts[1];
            String[] pairs = payload.split(";");

            for (String pair : pairs) {
                if (pair.isEmpty())
                    continue;

                int eqIndex = pair.indexOf('=');
                if (eqIndex <= 0)
                    continue;

                String key = unescape(pair.substring(0, eqIndex));
                String value = unescape(pair.substring(eqIndex + 1));
                msg.fields.put(key, value);
            }
        }

        return msg;
    }


    
    public Type getType() {
        return this.type;
    }

   
    public String getContent() {
        if (fields == null || fields.isEmpty()) {
            return null;
        }

        // Try common field names
        if (fields.containsKey("content")) {
            return fields.get("content");
        }
        if (fields.containsKey("message")) {
            return fields.get("message");
        }
        if (fields.containsKey("text")) {
            return fields.get("text");
        }

        return null;
    }

    
    public String getField(String key) {
        return fields.get(key);
    }


    public String getField(String key, String defaultValue) {
        return fields.getOrDefault(key, defaultValue);
    }

   
    public void setField(String key, String value) {
        fields.put(key, value);
    }

    
    public boolean hasField(String key) {
        return fields.containsKey(key);
    }

    
    private static String escape(String s) {
        if (s == null)
            return "";

        return s.replace("\\", "\\\\") // Must be first!
                .replace("\n", "\\n") // Newline
                .replace("\r", "\\r") // Carriage return
                .replace("\t", "\\t") // Tab
                .replace(";", "\\s") // Semicolon
                .replace("=", "\\e") // Equals
                .replace("|", "\\p"); // Pipe
    }

    private static String unescape(String s) {
        if (s == null)
            return "";

        return s.replace("\\p", "|") // Pipe
                .replace("\\e", "=") // Equals
                .replace("\\s", ";") // Semicolon
                .replace("\\t", "\t") // Tab
                .replace("\\r", "\r") // Carriage return
                .replace("\\n", "\n") // Newline
                .replace("\\\\", "\\"); // Must be last!
    }

    
    @Deprecated
    private static Type convertFromOldType(MessageType oldType) {
        switch (oldType) {
            case NAME:
                return Type.NAME;
            case READY:
                return Type.READY;
            case START:
                return Type.START;
            case MOVE:
                return Type.MOVE;
            case RESULT:
                return Type.RESULT;
            case STATUS:
                return Type.STATUS;
            case WELCOME:
                return Type.WELCOME;
            case ERROR:
                return Type.ERROR;
            case DISCONNECT:
                return Type.DISCONNECT;
            default:
                return Type.ERROR;
        }
    }

    
    @Deprecated
    public MessageType toOldType() {
        switch (this.type) {
            case NAME:
                return MessageType.NAME;
            case READY:
                return MessageType.READY;
            case START:
                return MessageType.START;
            case MOVE:
                return MessageType.MOVE;
            case RESULT:
                return MessageType.RESULT;
            case STATUS:
                return MessageType.STATUS;
            case WELCOME:
                return MessageType.WELCOME;
            case ERROR:
                return MessageType.ERROR;
            case DISCONNECT:
                return MessageType.DISCONNECT;
            default:
                return MessageType.ERROR;
        }
    }


    @Override
    public String toString() {
        return "Message{type=" + type + ", fields=" + fields + "}";
    }
}

/**
 * Old MessageType enum for backwards compatibility
 * 
 * @deprecated Use Message.Type instead
 */
@Deprecated
enum MessageType {
    NAME, READY, START, MOVE, RESULT, STATUS, WELCOME, ERROR, DISCONNECT
}
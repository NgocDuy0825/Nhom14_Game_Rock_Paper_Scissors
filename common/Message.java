package common;
 
import java.util.HashMap;
import java.util.Map;

public class Message {
    public MessageType type;
    public Map<String, String> fields = new HashMap<>();

    public Message(MessageType type) {
        this.type = type;
    }

    public String toWireString() {
        // TYPE|k1=v1;k2=v2
        StringBuilder sb = new StringBuilder();
        sb.append(type.name());
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
        return sb.toString();
    }

    public static Message parse(String line) {
    
        if (line == null || line.trim().isEmpty())
            return null;
        String[] parts = line.split("\\|", 2);
        MessageType type;
        try {
            type = MessageType.valueOf(parts[0]);
        } catch (Exception ex) {
             
            Message m = new Message(MessageType.ERROR);
            m.fields.put("message", "Unknown message type: " + parts[0]);
            return m;
        }
        Message m = new Message(type);
        if (parts.length == 1)
            return m;
        String payload = parts[1];
        String[] pairs = payload.split(";");
        for (String p : pairs) {
            if (p.isEmpty())
                continue;
            int eq = p.indexOf('=');
            if (eq <= 0)
                continue;
            String k = unescape(p.substring(0, eq));
            String v = unescape(p.substring(eq + 1));
            m.fields.put(k, v);
        }
        return m;
    }

    private static String escape(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\").replace(";", "\\s").replace("=", "\\e").replace("|", "\\p");
    }

    private static String unescape(String s) {
        if (s == null)
            return "";
        return s.replace("\\p", "|").replace("\\e", "=").replace("\\s", ";").replace("\\\\", "\\");
    }

     
    public MessageType getType() {
        return this.type;  

    public String getContent() {
         
        if (this.fields != null) {
            if (this.fields.containsKey("content"))
                return this.fields.get("content");
            if (this.fields.containsKey("message"))
                return this.fields.get("message");
             
        }
         
        return null;

    }
}

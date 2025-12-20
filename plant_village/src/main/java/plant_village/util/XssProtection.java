package plant_village.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * XSS Protection Utility
 * Sanitizes user input to prevent XSS attacks
 */
@Component
public class XssProtection {

    // Dangerous patterns
    private static final Pattern[] XSS_PATTERNS = {
        Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("onerror(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("onclick(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
    };

    /**
     * Sanitize string input to prevent XSS
     */
    public String sanitize(String value) {
        if (value == null) {
            return null;
        }

        // Remove null characters
        value = value.replaceAll("\0", "");

        // HTML encode dangerous characters
        value = htmlEncode(value);

        // Remove dangerous patterns
        for (Pattern pattern : XSS_PATTERNS) {
            value = pattern.matcher(value).replaceAll("");
        }

        return value;
    }

    /**
     * HTML encode special characters
     */
    private String htmlEncode(String value) {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;");
    }

    /**
     * Validate email format (basic check)
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.matches(emailRegex, email);
    }

    /**
     * Validate password strength
     */
    public boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        // At least 8 characters, 1 uppercase, 1 lowercase, 1 digit
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");
    }

    /**
     * Check if string contains potentially dangerous content
     */
    public boolean isDangerous(String value) {
        if (value == null) {
            return false;
        }

        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(value).find()) {
                return true;
            }
        }

        return false;
    }
}

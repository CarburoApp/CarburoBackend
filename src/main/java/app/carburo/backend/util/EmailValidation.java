package app.carburo.backend.util;

public final class EmailValidation {

    private EmailValidation() { /**/ }

    // Conserva la validación básica de los formularios con un recorrido lineal.
    public static boolean isValid(String email) {
        if (email == null)
            return false;

        int at = -1;
        boolean hasDomainSeparator = false;

        for (int i = 0; i < email.length(); i++) {
            char character = email.charAt(i);
            if (character == '@') {
                if (at >= 0) return false;
                at = i;
            } else if (character == '.' && at >= 0 && i > at + 1 && i < email.length() - 1) {
                hasDomainSeparator = true;
            } else if (character == ' ' || (character >= '\t' && character <= '\r')) {
                return false;
            }
        }
        return at > 0 && hasDomainSeparator;
    }
}

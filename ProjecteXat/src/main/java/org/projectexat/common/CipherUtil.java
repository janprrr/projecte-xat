package org.projectexat.common;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class CipherUtil {

    // Clau AES de 16 caràcters (128 bits)
    private static final String CLAU = "ProjecteXat2025!";
    private static final String ALGORISME = "AES";

    public static String xifrar(String text) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(CLAU.getBytes(), ALGORISME);
            Cipher cipher = Cipher.getInstance(ALGORISME);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] xifrat = cipher.doFinal(text.getBytes());
            return Base64.getEncoder().encodeToString(xifrat);
        } catch (Exception e) {
            throw new RuntimeException("Error xifrant: " + e.getMessage());
        }
    }

    public static String desxifrar(String textXifrat) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(CLAU.getBytes(), ALGORISME);
            Cipher cipher = Cipher.getInstance(ALGORISME);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] desxifrat = cipher.doFinal(Base64.getDecoder().decode(textXifrat));
            return new String(desxifrat);
        } catch (Exception e) {
            throw new RuntimeException("Error desxifrant: " + e.getMessage());
        }
    }
}
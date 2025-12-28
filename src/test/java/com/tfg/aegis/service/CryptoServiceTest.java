package com.tfg.aegis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.AEADBadTagException;
import java.security.SecureRandom;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CryptoServiceTest {

    private CryptoService cryptoService;
    private static final String TEST_KEY = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="; // 32 bytes en Base64

    @BeforeEach
    void setUp() {
        cryptoService = new CryptoService();
        // Configurar la clave base64 y llamar a init
        ReflectionTestUtils.setField(cryptoService, "base64Key", TEST_KEY);
        cryptoService.init();
    }

    @Test
    void init_validBase64Key_initializesKeySuccessfully() {
        // Given
        CryptoService service = new CryptoService();
        String validKey = Base64.getEncoder().encodeToString(new byte[32]);
        ReflectionTestUtils.setField(service, "base64Key", validKey);

        // When/Then - no debe lanzar excepción
        assertDoesNotThrow(service::init);
    }

    @Test
    void encrypt_validInput_returnsEncryptedData() throws Exception {
        // Given
        byte[] plaintext = "Hello, World!".getBytes();
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);

        // When
        byte[] encrypted = cryptoService.encrypt(plaintext, iv);

        // Then
        assertNotNull(encrypted);
        assertTrue(encrypted.length > 0);
        assertFalse(java.util.Arrays.equals(plaintext, encrypted));
    }

    @Test
    void encrypt_emptyInput_returnsEncryptedData() throws Exception {
        // Given
        byte[] plaintext = new byte[0];
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);

        // When
        byte[] encrypted = cryptoService.encrypt(plaintext, iv);

        // Then
        assertNotNull(encrypted);
        assertTrue(encrypted.length > 0); // GCM añade el tag de autenticación
    }

    @Test
    void encrypt_largeInput_returnsEncryptedData() throws Exception {
        // Given
        byte[] plaintext = new byte[10000];
        new SecureRandom().nextBytes(plaintext);
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);

        // When
        byte[] encrypted = cryptoService.encrypt(plaintext, iv);

        // Then
        assertNotNull(encrypted);
        assertTrue(encrypted.length > plaintext.length);
    }

    @Test
    void decrypt_validEncryptedData_returnsOriginalPlaintext() throws Exception {
        // Given
        byte[] plaintext = "Test message".getBytes();
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        byte[] encrypted = cryptoService.encrypt(plaintext, iv);

        // When
        byte[] decrypted = cryptoService.decrypt(encrypted, iv);

        // Then
        assertNotNull(decrypted);
        assertArrayEquals(plaintext, decrypted);
    }

    @Test
    void decrypt_emptyEncrypted_throwsException() {
        // Given
        byte[] emptyEncrypted = new byte[0];
        byte[] iv = new byte[12];

        // When/Then
        assertThrows(Exception.class, () ->
            cryptoService.decrypt(emptyEncrypted, iv));
    }

    @Test
    void decrypt_wrongIv_throwsException() throws Exception {
        // Given
        byte[] plaintext = "Secret data".getBytes();
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        byte[] encrypted = cryptoService.encrypt(plaintext, iv);

        byte[] wrongIv = new byte[12];
        new SecureRandom().nextBytes(wrongIv);

        // When/Then
        assertThrows(AEADBadTagException.class, () ->
            cryptoService.decrypt(encrypted, wrongIv));
    }

    @Test
    void decrypt_tamperedCiphertext_throwsException() throws Exception {
        // Given
        byte[] plaintext = "Important message".getBytes();
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        byte[] encrypted = cryptoService.encrypt(plaintext, iv);

        // Modificar el ciphertext (tamper)
        encrypted[0] = (byte) (encrypted[0] ^ 0xFF);

        // When/Then
        assertThrows(AEADBadTagException.class, () ->
            cryptoService.decrypt(encrypted, iv));
    }

    @Test
    void encryptDecrypt_roundTrip_preservesData() throws Exception {
        // Given
        String[] testMessages = {
            "Hello World",
            "Test123!@#",
            "Unicode: ñáéíóú 中文 🚀",
            "",
            "a",
            "Very long message that contains multiple words and special characters !@#$%^&*()_+-=[]{}|;:',.<>?/~`"
        };

        for (String message : testMessages) {
            // Given
            byte[] plaintext = message.getBytes();
            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);

            // When
            byte[] encrypted = cryptoService.encrypt(plaintext, iv);
            byte[] decrypted = cryptoService.decrypt(encrypted, iv);

            // Then
            assertArrayEquals(plaintext, decrypted, "Failed for message: " + message);
            assertEquals(message, new String(decrypted));
        }
    }

    @Test
    void encrypt_sameDataDifferentIv_producesDifferentCiphertext() throws Exception {
        // Given
        byte[] plaintext = "Same data".getBytes();
        byte[] iv1 = new byte[12];
        byte[] iv2 = new byte[12];
        new SecureRandom().nextBytes(iv1);
        new SecureRandom().nextBytes(iv2);

        // When
        byte[] encrypted1 = cryptoService.encrypt(plaintext, iv1);
        byte[] encrypted2 = cryptoService.encrypt(plaintext, iv2);

        // Then
        assertFalse(java.util.Arrays.equals(encrypted1, encrypted2));
    }

    @Test
    void encrypt_nullPlaintext_throwsException() {
        // Given
        byte[] iv = new byte[12];

        // When/Then
        assertThrows(Exception.class, () ->
            cryptoService.encrypt(null, iv));
    }

    @Test
    void decrypt_nullCiphertext_throwsException() {
        // Given
        byte[] iv = new byte[12];

        // When/Then
        assertThrows(Exception.class, () ->
            cryptoService.decrypt(null, iv));
    }

    @Test
    void encrypt_nullIv_throwsException() {
        // Given
        byte[] plaintext = "Test".getBytes();

        // When/Then
        assertThrows(Exception.class, () ->
            cryptoService.encrypt(plaintext, null));
    }

    @Test
    void decrypt_nullIv_throwsException() {
        // Given
        byte[] ciphertext = new byte[16];

        // When/Then
        assertThrows(Exception.class, () ->
            cryptoService.decrypt(ciphertext, null));
    }

    @Test
    void decrypt_invalidIvLength_throwsException() {
        // Given
        byte[] ciphertext = new byte[16];
        byte[] longIv = new byte[16]; // IV debe ser 12 bytes para GCM

        // When/Then
        assertThrows(Exception.class, () ->
            cryptoService.decrypt(ciphertext, longIv));
    }

    @Test
    void init_invalidBase64Key_throwsException() {
        // Given
        CryptoService service = new CryptoService();
        ReflectionTestUtils.setField(service, "base64Key", "invalid-base64!");

        // When/Then
        assertThrows(IllegalArgumentException.class, service::init);
    }

    @Test
    void encryptDecrypt_binaryData_preservesData() throws Exception {
        // Given
        byte[] binaryData = new byte[256];
        for (int i = 0; i < 256; i++) {
            binaryData[i] = (byte) i;
        }
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);

        // When
        byte[] encrypted = cryptoService.encrypt(binaryData, iv);
        byte[] decrypted = cryptoService.decrypt(encrypted, iv);

        // Then
        assertArrayEquals(binaryData, decrypted);
    }

    @Test
    void encrypt_multipleCallsSameData_producesDifferentOutputWithDifferentIv() throws Exception {
        // Given
        byte[] plaintext = "Consistent data".getBytes();

        // When
        byte[] iv1 = new byte[12];
        byte[] iv2 = new byte[12];
        byte[] iv3 = new byte[12];
        new SecureRandom().nextBytes(iv1);
        new SecureRandom().nextBytes(iv2);
        new SecureRandom().nextBytes(iv3);

        byte[] encrypted1 = cryptoService.encrypt(plaintext, iv1);
        byte[] encrypted2 = cryptoService.encrypt(plaintext, iv2);
        byte[] encrypted3 = cryptoService.encrypt(plaintext, iv3);

        // Then - todos deben ser diferentes
        assertFalse(java.util.Arrays.equals(encrypted1, encrypted2));
        assertFalse(java.util.Arrays.equals(encrypted2, encrypted3));
        assertFalse(java.util.Arrays.equals(encrypted1, encrypted3));

        // Pero todos deben descifrarse al mismo plaintext
        assertArrayEquals(plaintext, cryptoService.decrypt(encrypted1, iv1));
        assertArrayEquals(plaintext, cryptoService.decrypt(encrypted2, iv2));
        assertArrayEquals(plaintext, cryptoService.decrypt(encrypted3, iv3));
    }
}

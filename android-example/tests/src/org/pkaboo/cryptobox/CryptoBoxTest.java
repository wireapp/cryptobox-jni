package org.pkaboo.cryptobox;

import java.nio.charset.Charset;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.*;

import static junit.framework.Assert.*;

public class CryptoBoxTest extends TestCase {
    private Charset   utf8     = Charset.forName("UTF-8");
    private CryptoBox aliceBox = null;
    private CryptoBox bobBox   = null;
    private PreKey[] aliceKeys = null;
    private PreKey[] bobKeys   = null;

    private String mkTmpDir(String name) throws IOException {
        File tmpDir = File.createTempFile(name, "");
        tmpDir.delete();
        tmpDir.mkdir();
        return tmpDir.getAbsolutePath();
    }

    public void setUp() throws CryptoException, IOException {
        String aliceDir = mkTmpDir("cryptobox-alice");
        aliceBox = CryptoBox.open(aliceDir);
        aliceKeys = aliceBox.newPreKeys(0, 10);

        String bobDir = mkTmpDir("cryptobox-bob");
        bobBox = CryptoBox.open(bobDir);
        bobKeys = bobBox.newPreKeys(0, 10);
    }

    public void tearDown() {
        aliceBox.close();
        bobBox.close();
    }

    public void testEncryptDecrypt() {
        CryptoSession alice = null;
        CryptoSession bob   = null;
        try {
            alice = aliceBox.initSessionFromPreKey("alice", bobKeys[0]);
            byte[] helloBob = "Hello Bøb!".getBytes(utf8);
            byte[] helloBobCipher = alice.encrypt(helloBob);

            assertFalse( "Encrypted text not equal to original",
                         helloBob.equals(new String(helloBobCipher, utf8)));

            SessionMessage smgs = bobBox.initSessionFromMessage("bob", helloBobCipher);
            bob = smgs.getSession();
            byte[] helloBobPlain = smgs.getMessage();

            assertEquals( "Correct decrypted text",
                          new String(helloBob, utf8),
                          new String(helloBobPlain, utf8) );

            alice.save();
            bob.save();

            assertEquals("Correct session ID", "alice", alice.id);
            assertEquals("Correct session ID", "bob", bob.id);

            assertTrue(aliceBox.getSession(alice.id) == alice);
            assertTrue(bobBox.getSession(bob.id) == bob);

            assertTrue("Fingerprint mismatch", Arrays.equals(alice.getLocalFingerprint(), bob.getRemoteFingerprint()));
            assertTrue("Fingerprint mismatch", Arrays.equals(bob.getLocalFingerprint(), alice.getRemoteFingerprint()));
        } catch (CryptoException ex) {
            fail(ex.toString());
        }
    }

    public void testSessionClosedException() {
        CryptoSession alice = null;
        try {
            alice = aliceBox.initSessionFromPreKey("alice", bobKeys[0]);
            byte[] helloBob  = "Hello Bøb!".getBytes(utf8);
            byte[] helloBob1 = alice.encrypt(helloBob);

            aliceBox.closeSession(alice);
            assertTrue(alice.isClosed());
            byte[] helloBob2 = alice.encrypt(helloBob);

            fail("Expected an exception for use-after-close.");
        } catch (IllegalStateException ex) {
            // expected
        } catch (CryptoException ex) {
            fail(ex.toString());
        } finally {
            if (alice != null) {
                aliceBox.closeSession(alice);
            }
        }
    }

    public void testBoxClosedException() {
        try {
            aliceBox.close();
            assertTrue(aliceBox.isClosed());
            aliceBox.initSessionFromPreKey("alice", bobKeys[0]);

            fail("Expected an exception for use-after-close.");
        } catch (CryptoException ex) {
            fail(ex.toString());
        } catch (IllegalStateException ex) {
            // expected
        }
    }
}

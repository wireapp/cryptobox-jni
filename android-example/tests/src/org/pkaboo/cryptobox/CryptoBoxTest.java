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

            assertTrue("Fingerprint mismatch", Arrays.equals(aliceBox.getLocalFingerprint(), bob.getRemoteFingerprint()));
            assertTrue("Fingerprint mismatch", Arrays.equals(bobBox.getLocalFingerprint(), alice.getRemoteFingerprint()));
        } catch (CryptoException ex) {
            fail(ex.toString());
        }
    }

    public void testPreKeyGeneration() {
        try {
            aliceKeys = aliceBox.newPreKeys(0xFFFC, 5);
            int[] expected = { 0xFFFC, 0xFFFD, 0xFFFE, 0x0000, 0x0001 };
            for (int i = 0; i < expected.length; ++i) {
                assertEquals("Incorrect prekey ID", expected[i], aliceKeys[i].id);
            }
        } catch (CryptoException ex) {
            fail(ex.toString());
        }
    }

    public void testLastPreKey() {
        CryptoSession alice = null;
        CryptoSession bob   = null;
        try {
            PreKey bobLastKey = bobBox.newLastPreKey();
            alice = aliceBox.initSessionFromPreKey("alice", bobLastKey);
            byte[] helloBob = "Hello Bøb!".getBytes(utf8);
            byte[] helloBobCipher = alice.encrypt(helloBob);
            for (int i = 0; i < 3; ++i) {
                SessionMessage smgs = bobBox.initSessionFromMessage("bob", helloBobCipher);
                bob = smgs.getSession();
                byte[] helloBobPlain = smgs.getMessage();
                assertEquals( "Correct decrypted text",
                              new String(helloBob, utf8),
                              new String(helloBobPlain, utf8) );
                bob.save(); // does not remove Bob's last prekey
            }
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

    public void testDeleteSession() {
        CryptoSession alice = null;
        CryptoSession bob = null;
        try {
            // Setup Alice
            alice = aliceBox.initSessionFromPreKey("alice", bobKeys[0]);
            byte[] helloBob = "Hello Bøb!".getBytes(utf8);
            byte[] helloBobCipher = alice.encrypt(helloBob);
            alice.save();

            // Setup Bob
            SessionMessage smgs = bobBox.initSessionFromMessage("bob", helloBobCipher);
            bob = smgs.getSession();

            // Alice "loses" the session
            aliceBox.closeSession(alice);
            alice = aliceBox.getSession("alice");
            aliceBox.deleteSession(alice.id);
            assertTrue(alice.isClosed());
            assertNull(aliceBox.tryGetSession("alice"));

            // Bob sends a message
            byte[] helloAlice = "Hello Bøb!".getBytes(utf8);
            byte[] helloAliceCipher = bob.encrypt(helloAlice);

            // Since Alice has no session, she tries to initialise one
            // when receiving a message from Bob.
            try {
                aliceBox.initSessionFromMessage("alice", helloAliceCipher);
            } catch (CryptoException ex) {
                if (ex.code != CryptoException.Code.INVALID_MESSAGE) {
                    fail(ex.toString());
                }
            }

            // Alice initialises a new session with Bob
            alice = aliceBox.initSessionFromPreKey("alice", bobKeys[0]);
            byte[] helloAgainBobCipher = alice.encrypt(helloBob);

            // Since Bob still has the session, he will try to use it.
            try {
                bob.decrypt(helloAgainBobCipher);
            } catch (CryptoException ex) {
                if (ex.code != CryptoException.Code.INVALID_MESSAGE) {
                    fail(ex.toString());
                }
            }
        } catch (CryptoException ex) {
            fail(ex.toString());
        } finally {
            if (alice != null) {
                aliceBox.closeSession(alice);
            }
            if (bob != null) {
                bobBox.closeSession(bob);
            }
        }
    }
}

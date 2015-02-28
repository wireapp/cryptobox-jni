package org.pkaboo.cryptobox;

import android.app.Activity;
import android.widget.TextView;
import android.os.Bundle;
import android.util.Log;

import java.nio.charset.Charset;

public class CryptoBoxExample extends Activity {
    private static final String TAG = "CryptoBoxExample";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Charset       utf8     = Charset.forName("UTF-8");
        CryptoBox     aliceBox = null;
        CryptoBox     bobBox   = null;
        CryptoSession alice    = null;
        CryptoSession bob      = null;
        try {
            String aliceDir = getDir("cryptobox-alice", MODE_PRIVATE).getAbsolutePath();
            aliceBox = CryptoBox.open(aliceDir);

            String bobDir = getDir("cryptobox-bob", MODE_PRIVATE).getAbsolutePath();
            bobBox = CryptoBox.open(bobDir);
            PreKey[] pks = bobBox.newPreKeys(0, 10);

            alice = aliceBox.initSessionFromPreKey("alice", pks[0]);
            byte[] helloBobCipher = alice.encrypt("Hello Bøb!".getBytes(utf8));
            Log.v(TAG, "Encrypted message: " + new String(helloBobCipher));

            SessionMessage smgs = bobBox.initSessionFromMessage("bob", helloBobCipher);
            bob = smgs.getSession();
            byte[] helloBobPlain = smgs.getMessage();
            Log.v(TAG, "Decrypted message: " + new String(helloBobPlain, utf8));

            alice.save();
            bob.save();

            TextView tv = new TextView(this);
            tv.setText( "This is a CryptoBox example.\n\n"
                      + "Generated " + pks.length + " prekeys for Bob, "
                      + "each of length " + pks[0].data.length + ".\n\n"
                      + "Initialised session " + alice.id + " from the first prekey of " + bob.id + ".\n\n"
                      + "Enrypted a message from " + alice.id + " to " + bob.id + ".\n\n"
                      + "The encrypted message: " + (new String(helloBobCipher)) + "\n\n"
                      + "Initialised session " + bob.id + " from " + alice.id + "'s first message.\n\n"
                      + "The decrypted message: " + (new String(helloBobPlain, utf8))
                      );
            setContentView(tv);
        } catch (CryptoException e) {
            Log.e(TAG, "Cryptography is hard", e);
        } finally {
            if (aliceBox != null) aliceBox.close();
            if (bobBox != null) bobBox.close();
        }
    }
}

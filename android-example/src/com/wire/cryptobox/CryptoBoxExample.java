// Copyright (C) 2015 Wire Swiss GmbH <support@wire.com>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.wire.cryptobox;

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

        Charset   utf8     = Charset.forName("UTF-8");
        CryptoBox aliceBox = null;
        CryptoBox bobBox   = null;
        try {
            String aliceDir = getDir("cryptobox-alice", MODE_PRIVATE).getAbsolutePath();
            aliceBox = CryptoBox.open(aliceDir);

            String bobDir = getDir("cryptobox-bob", MODE_PRIVATE).getAbsolutePath();
            bobBox = CryptoBox.open(bobDir);
            PreKey[] pks = bobBox.newPreKeys(0, 10);

            CryptoSession alice   = aliceBox.initSessionFromPreKey("alice", pks[0]);
            byte[] helloBobCipher = alice.encrypt("Hello Bøb!".getBytes(utf8));

            Log.v(TAG, "Message length: " + "Hello Bøb!".getBytes(utf8).length);
            Log.v(TAG, "Encrypted prekey message length: " + helloBobCipher.length);
            Log.v(TAG, "Encrypted prekey message: " + new String(helloBobCipher));

            SessionMessage smgs  = bobBox.initSessionFromMessage("bob", helloBobCipher);
            CryptoSession bob    = smgs.getSession();
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

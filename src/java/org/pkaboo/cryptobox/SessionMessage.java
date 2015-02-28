// This Source Code Form is subject to the terms of
// the Mozilla Public License, v. 2.0. If a copy of
// the MPL was not distributed with this file, You
// can obtain one at http://mozilla.org/MPL/2.0/.

package org.pkaboo.cryptobox;

final public class SessionMessage {
    private final CryptoSession session;
    private final byte[] message;

    SessionMessage(CryptoSession sess, byte[] msg) {
        this.session = sess;
        this.message = msg;
    }

    public CryptoSession getSession() {
        return this.session;
    }

    public byte[] getMessage() {
        return this.message;
    }
}

// This Source Code Form is subject to the terms of
// the Mozilla Public License, v. 2.0. If a copy of
// the MPL was not distributed with this file, You
// can obtain one at http://mozilla.org/MPL/2.0/.

package org.pkaboo.cryptobox;

import java.util.HashMap;

final public class CryptoBox {
    static {
        System.loadLibrary("cryptobox");
        System.loadLibrary("cryptobox-jni");
    }

    private static final Object OPEN_LOCK = new Object();

    private long ptr;
    private final Object lock = new Object();
    private final HashMap<String, CryptoSession> sessions = new HashMap<String, CryptoSession>();

    private CryptoBox(long ptr) {
        this.ptr = ptr;
    }

    public static CryptoBox open(String dir) throws CryptoException {
        synchronized (OPEN_LOCK) {
            return jniOpen(dir);
        }
    }

    public byte[] getLocalFingerprint() {
        synchronized (lock) {
            errorIfClosed();
            return jniGetLocalFingerprint(this.ptr);
        }
    }

    public PreKey[] newPreKeys(int start, int num) throws CryptoException {
        synchronized (lock) {
            errorIfClosed();
            return jniNewPreKeys(this.ptr, start, num);
        }
    }

    public CryptoSession initSessionFromPreKey(String sid, PreKey prekey) throws CryptoException {
        synchronized (lock) {
            errorIfClosed();
            CryptoSession sess = sessions.get(sid);
            if (sess != null) {
                return sess;
            }
            sess = jniInitSessionFromPreKey(this.ptr, sid, prekey.data);
            sessions.put(sess.id, sess);
            return sess;
        }
    }

    public SessionMessage initSessionFromMessage(String sid, byte[] message) throws CryptoException {
        synchronized (lock) {
            errorIfClosed();
            CryptoSession sess = sessions.get(sid);
            if (sess != null) {
                return new SessionMessage(sess, sess.decrypt(message));
            }
            SessionMessage smsg = jniInitSessionFromMessage(this.ptr, sid, message);
            sessions.put(smsg.getSession().id, smsg.getSession());
            return smsg;
        }
    }

    public CryptoSession getSession(String sid) throws CryptoException {
        synchronized (lock) {
            errorIfClosed();
            CryptoSession sess = sessions.get(sid);
            if (sess == null) {
                sess = jniGetSession(this.ptr, sid);
                sessions.put(sid, sess);
            }
            return sess;
        }
    }

    public CryptoSession tryGetSession(String sid) throws CryptoException {
        try { return getSession(sid); }
        catch (CryptoException ex) {
            if (ex.code == CryptoException.Code.NO_SESSION) {
                return null;
            }
            throw ex;
        }
    }

    public void closeSession(CryptoSession sess) {
        synchronized (lock) {
            errorIfClosed();
            sessions.remove(sess.id);
            sess.close();
        }
    }

    public void closeAllSessions() {
        synchronized (lock) {
            errorIfClosed();
            for (CryptoSession s : sessions.values()) {
                s.close();
            }
            sessions.clear();
        }
    }

    public void close() {
        synchronized (lock) {
            if (isClosed()) {
                return;
            }
            closeAllSessions();
            jniClose(this.ptr);
            ptr = 0;
        }
    }

    public boolean isClosed() {
        synchronized (lock) {
            return ptr == 0;
        }
    }

    private void errorIfClosed() {
        if (isClosed()) {
            throw new IllegalStateException("Invalid operation on a closed CryptoBox.");
        }
    }

    @Override protected void finalize() throws Throwable {
        close();
    }

    private native static CryptoBox jniOpen(String dir) throws CryptoException;
    private native static void jniClose(long ptr);
    private native static PreKey[] jniNewPreKeys(long ptr, int start, int num) throws CryptoException;
    private native static byte[] jniGetLocalFingerprint(long ptr);
    private native static CryptoSession jniInitSessionFromPreKey(long ptr, String sid, byte[] prekey) throws CryptoException;
    private native static SessionMessage jniInitSessionFromMessage(long ptr, String sid, byte[] message) throws CryptoException;
    private native static CryptoSession jniGetSession(long ptr, String sid) throws CryptoException;
}

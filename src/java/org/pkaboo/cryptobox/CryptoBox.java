// This Source Code Form is subject to the terms of
// the Mozilla Public License, v. 2.0. If a copy of
// the MPL was not distributed with this file, You
// can obtain one at http://mozilla.org/MPL/2.0/.

package org.pkaboo.cryptobox;

import java.util.HashMap;

/**
 * A <tt>CryptoBox</tt> is an opaque container of all the necessary
 * key material of single needed for exchanging ent-to-end encrypted
 * messages with peers for a single, logical client or device.
 *
 * <p>Every cryptographic session with a peer is represented by
 * a {@link CryptoSession}. These sessions are pooled by a <tt>CryptoBox</tt>,
 * i.e. if a session with the same session ID is requested multiple times,
 * the same instance is returned.
 * </p>
 *
 * <p>A <tt>CryptoBox</tt> is thread-safe.</p>
 *
 * @see CryptoSession
 */
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

    /**
     * Open a <tt>CryptoBox</tt> that operates on the given directory.
     * The directory must exist and be writeable.
     *
     * <p>Note: Do not open multiple boxes that operate on the same or
     * overlapping directories. Doing so results in undefined behaviour.</p>
     *
     * @param dir The root storage directory of the box.
     */
    public static CryptoBox open(String dir) throws CryptoException {
        synchronized (OPEN_LOCK) {
            return jniOpen(dir);
        }
    }

    /**
     * Get the local fingerprint as a hex-encoded byte array.
     */
    public byte[] getLocalFingerprint() {
        synchronized (lock) {
            errorIfClosed();
            return jniGetLocalFingerprint(this.ptr);
        }
    }

    /**
     * Generate a range of prekeys.
     *
     * @param start The ID of the first prekey to generate.
     * @param num The total number of prekeys to generate.
     */
    public PreKey[] newPreKeys(int start, int num) throws CryptoException {
        synchronized (lock) {
            errorIfClosed();
            return jniNewPreKeys(this.ptr, start, num);
        }
    }

    /**
     * Initialise a {@link CryptoSession} using the prekey of a peer.
     *
     * <p>This is the entry point for the initiator of a session, i.e.
     * the side that wishes to send the first message.</p>
     *
     * <p>Note: The acquired session must eventually be released / closed
     * either through {@link #closeSession} or {@link #closeAllSessions},
     * otherwise resource leaks occur.</p>
     *
     * @param sid The ID of the new session.
     * @param prekey The prekey of the peer.
     */
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

    /**
     * Initialise a {@link CryptoSession} using a received encrypted message.
     *
     * <p>This is the entry point for the recipient of an encrypted message.</p>
     *
     * <p>Note: The acquired session must eventually be released / closed
     * either through {@link #closeSession} or {@link #closeAllSessions},
     * otherwise resource leaks occur.</p>
     *
     * @param sid The ID of the new session.
     * @param message The encrypted (prekey) message.
     */
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

    /**
     * Get an existing session.
     *
     * <p>If the session does not exist, a {@link CryptoException} is thrown
     * with the code {@link CryptoException.Code#NO_SESSION}.</p>
     *
     * <p>Note: The acquired session must eventually be released / closed
     * either through {@link #closeSession} or {@link #closeAllSessions},
     * otherwise resource leaks occur.</p>
     *
     * @param sid The ID of the session to get.
     */
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

    /**
     * Try to get an existing session.
     *
     * <p>Equivalent to {@link #getSession}, except that <tt>null</tt>
     * is returned if the session does not exist.</p>
     *
     * <p>Note: The acquired session must eventually be released / closed
     * either through {@link #closeSession} or {@link #closeAllSessions},
     * otherwise resource leaks occur.</p>
     *
     * @param sid The ID of the session to get.
     */
    public CryptoSession tryGetSession(String sid) throws CryptoException {
        try { return getSession(sid); }
        catch (CryptoException ex) {
            if (ex.code == CryptoException.Code.NO_SESSION) {
                return null;
            }
            throw ex;
        }
    }

    /**
     * Close a session.
     *
     * <p>Note: After a session has been closed, any operation other than
     * <tt>closeSession</tt> are considered programmer error and result in
     * an {@link IllegalStateException}.</p>
     *
     * <p>If the session is already closed, this is a no-op.</p>
     *
     * @param sess The session to close.
     */
    public void closeSession(CryptoSession sess) {
        synchronized (lock) {
            errorIfClosed();
            sessions.remove(sess.id);
            sess.close();
        }
    }

    /**
     * Close all open sessions.
     *
     * @see #closeSession
     */
    public void closeAllSessions() {
        synchronized (lock) {
            errorIfClosed();
            for (CryptoSession s : sessions.values()) {
                s.close();
            }
            sessions.clear();
        }
    }

    /**
     * Close the <tt>CryptoBox</tt>.
     *
     * <p>Note: After a box has been closed, any operation other than
     * <tt>close</tt> are considered programmer error and result in
     * an {@link IllegalStateException}.</p>
     *
     * <p>If the box is already closed, this is a no-op.</p>
     */
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

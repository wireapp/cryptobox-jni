// This Source Code Form is subject to the terms of
// the Mozilla Public License, v. 2.0. If a copy of
// the MPL was not distributed with this file, You
// can obtain one at http://mozilla.org/MPL/2.0/.

package org.pkaboo.cryptobox;

final public class CryptoSession {
    private long ptr;
    private final Object lock = new Object();

    public final String id;

    private CryptoSession(long ptr, String id) {
        this.ptr = ptr;
        this.id  = id;
    }

    public void save() throws CryptoException {
        synchronized (lock) {
            errorIfClosed();
            jniSave(this.ptr);
        }
    }

    public byte[] encrypt(byte[] plaintext) throws CryptoException {
        synchronized (lock) {
            errorIfClosed();
            return jniEncrypt(this.ptr, plaintext);
        }
    }

    public byte[] decrypt(byte[] ciphertext) throws CryptoException {
        synchronized (lock) {
            errorIfClosed();
            return jniDecrypt(this.ptr, ciphertext);
        }
    }

    public byte[] getRemoteFingerprint() throws CryptoException {
        synchronized (lock) {
            errorIfClosed();
            return jniGetRemoteFingerprint(this.ptr);
        }
    }

    void close() {
        synchronized (lock) {
            if (isClosed()) {
                return;
            }
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
            throw new IllegalStateException("Invalid operation on a closed CryptoSession.");
        }
    }

    @Override protected void finalize() throws Throwable {
        close();
    }

    private native static void   jniSave(long ptr) throws CryptoException;
    private native static byte[] jniEncrypt(long ptr, byte[] plaintext) throws CryptoException;
    private native static byte[] jniDecrypt(long ptr, byte[] ciphertext) throws CryptoException;
    private native static byte[] jniGetRemoteFingerprint(long ptr);
    private native static void   jniClose(long ptr);
}

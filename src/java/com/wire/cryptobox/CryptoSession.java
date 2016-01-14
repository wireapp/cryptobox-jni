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

/**
 * A <tt>CryptoSession</tt> represents a cryptographic session with a peer
 * (e.g. client or device) and is used to encrypt and decrypt messages sent
 * and received, respectively.
 *
 * <p>A <tt>CryptoSession</tt> is thread-safe.</p>
 */
final public class CryptoSession {
    private final long boxPtr;
    private long ptr;
    private final Object lock = new Object();

    public final String id;

    private CryptoSession(long boxPtr, long ptr, String id) {
        this.boxPtr = boxPtr;
        this.ptr    = ptr;
        this.id     = id;
    }

    /**
     * Save the session, persisting any changes made to the underlying
     * key material as a result of any {@link #encrypt} and {@link #decrypt}
     * operations since the last save.
     */
    public void save() throws CryptoException {
        synchronized (lock) {
            errorIfClosed();
            try {
                jniSave(this.boxPtr, this.ptr);
            } catch (CryptoException e) {
                if (e.code == CryptoException.Code.PANIC) {
                    this.close();
                }
                throw e;
            }
        }
    }

    /**
     * Encrypt a byte array containing plaintext.
     *
     * @param plaintext The plaintext to encrypt.
     * @return A byte array containing the ciphertext.
     */
    public byte[] encrypt(byte[] plaintext) throws CryptoException {
        synchronized (lock) {
            errorIfClosed();
            try {
                return jniEncrypt(this.ptr, plaintext);
            } catch (CryptoException e) {
                if (e.code == CryptoException.Code.PANIC) {
                    this.close();
                }
                throw e;
            }
        }
    }

    /**
     * Decrypt a byte array containing ciphertext.
     *
     * @param ciphertext The ciphertext to decrypt.
     * @return A byte array containing the plaintext.
     */
    public byte[] decrypt(byte[] ciphertext) throws CryptoException {
        synchronized (lock) {
            errorIfClosed();
            try {
                return jniDecrypt(this.ptr, ciphertext);
            } catch (CryptoException e) {
                if (e.code == CryptoException.Code.PANIC) {
                    this.close();
                }
                throw e;
            }
        }
    }

    /**
     * Get the remote fingerprint as a hex-encoded byte array.
     */
    public byte[] getRemoteFingerprint() throws CryptoException {
        synchronized (lock) {
            errorIfClosed();
            try {
                return jniGetRemoteFingerprint(this.ptr);
            } catch (CryptoException e) {
                if (e.code == CryptoException.Code.PANIC) {
                    this.close();
                }
                throw e;
            }
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

    private native static void   jniSave(long boxPtr, long ptr) throws CryptoException;
    private native static byte[] jniEncrypt(long ptr, byte[] plaintext) throws CryptoException;
    private native static byte[] jniDecrypt(long ptr, byte[] ciphertext) throws CryptoException;
    private native static byte[] jniGetRemoteFingerprint(long ptr) throws CryptoException;
    private native static void   jniClose(long ptr);
}

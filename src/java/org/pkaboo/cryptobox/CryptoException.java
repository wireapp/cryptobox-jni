// This Source Code Form is subject to the terms of
// the Mozilla Public License, v. 2.0. If a copy of
// the MPL was not distributed with this file, You
// can obtain one at http://mozilla.org/MPL/2.0/.

package org.pkaboo.cryptobox;

final public class CryptoException extends Exception {
    public final Code code;

    private CryptoException(int code) {
        this(fromNativeCode(code));
    }

    public CryptoException(Code code) {
        super(code.toString());
        this.code = code;
    }

    private static Code fromNativeCode(int code) {
        switch (code) {
            case  1: return Code.STORAGE_ERROR;
            case  2: return Code.NO_SESSION;
            case  3: return Code.DECODE_ERROR;
            case  4: return Code.REMOTE_IDENTITY_CHANGED;
            case  5: return Code.INVALID_SIGNATURE;
            case  6: return Code.INVALID_MESSAGE;
            case  7: return Code.DUPLICATE_MESSAGE;
            case  8: return Code.TOO_DISTANT_FUTURE;
            case  9: return Code.OUTDATED_MESSAGE;
            default: return Code.UNKNOWN_ERROR;
        }
    }

    public enum Code {
        /** A requested session was not found. */
        NO_SESSION,

        /** The remote identity of a session changed.
         *
         * <p>Usually the user should be informed and the session reinitialised.
         * If the remote fingerprint was previously verified, it will need to be
         * verified anew in order to exclude any potential MITM.</p>
         */
        REMOTE_IDENTITY_CHANGED,

        /** The signature of a decrypted message is invalid.
         *
         * <p>The message being decrypted is incomplete or has otherwise been
         * tampered with.</p>
         */
        INVALID_SIGNATURE,

        /** A message is invalid.
         *
         * <p>The message being decrypted is in some way invalid and cannot
         * be understood.</p>
         */
        INVALID_MESSAGE,

        /** A message is a duplicate.
         *
         * <p>The message being decrypted is a duplicate of a message that has
         * previously been decrypted with the same session. The message can
         * be safely discarded.</p>
         */
        DUPLICATE_MESSAGE,

        /** A message is too recent.
         *
         * <p>There is an unreasonably large gap between the last decrypted
         * message and the message being decrypted, i.e. there are too many
         * intermediate messages missing. The message should be dropped.</p>
         */
        TOO_DISTANT_FUTURE,

        /** A message is too old.
         *
         * <p>The message being decrypted is unreasonably old and cannot
         * be decrypted any longer due to the key material no longer being available.
         * The message should be dropped.</p>
         */
        OUTDATED_MESSAGE,

        /** A message or key could not be decoded.
         *
         * <p>The message or key being decoded is either malformed or
         * otherwise encoded in a way that it cannot be processed.</p>
         */
        DECODE_ERROR,

        /** An internal storage error occurred.
         *
         * <p>An error occurred while loading or persisting key material.
         * The operation may be retried a limited number of times.</p>
         */
        STORAGE_ERROR,

        /** An unspecified error occurred. */
        UNKNOWN_ERROR
    }
}

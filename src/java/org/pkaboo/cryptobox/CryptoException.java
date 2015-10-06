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
            case  2: return Code.SESSION_NOT_FOUND;
            case  3: return Code.DECODE_ERROR;
            case  4: return Code.REMOTE_IDENTITY_CHANGED;
            case  5: return Code.INVALID_SIGNATURE;
            case  6: return Code.INVALID_MESSAGE;
            case  7: return Code.DUPLICATE_MESSAGE;
            case  8: return Code.TOO_DISTANT_FUTURE;
            case  9: return Code.OUTDATED_MESSAGE;
            case 13: return Code.IDENTITY_ERROR;
            case 14: return Code.PREKEY_NOT_FOUND;
            default: return Code.UNKNOWN_ERROR;
        }
    }

    public enum Code {
        /** A requested session was not found. */
        SESSION_NOT_FOUND,

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
         * <p>The message is well-formed but cannot be decrypted, e.g.
         * because the message is used to initialise a session but does not
         * contain a {@link PreKey} or the used session does not contain the
         * appropriate key material for decrypting the message. The problem
         * should be reported to the user, as it might be necessary for both
         * peers to re-initialise their sessions.</p>
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
         * otherwise encoded in a way such it cannot be understood.</p>
         */
        DECODE_ERROR,

        /** An internal storage error occurred.
         *
         * <p>An error occurred while loading or persisting key material.
         * The operation may be retried a limited number of times.</p>
         */
        STORAGE_ERROR,

        /** A CBox has been opened with an incomplete or mismatching identity
         * using {@link CryptoBox#openWith}.
         *
         * <p>This is typically a programmer error.</p>
         */
        IDENTITY_ERROR,

        /** An attempt was made to initialise a new session using {@link CryptoBox#initSessionFromMessage}
         * whereby the prekey corresponding to the prekey ID in the message could not be found.
         */
        PREKEY_NOT_FOUND,

        /** An unspecified error occurred. */
        UNKNOWN_ERROR
    }
}

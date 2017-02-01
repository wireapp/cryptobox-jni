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
            case 10: return Code.INVALID_STRING;
            case 11: return Code.INVALID_STRING;
            case 13: return Code.IDENTITY_ERROR;
            case 14: return Code.PREKEY_NOT_FOUND;
            case 15: return Code.PANIC;
            case 16: return Code.INIT_ERROR;
            case 17: return Code.DEGENERATED_KEY;
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

        /** A panic occurred. This is a last resort error raised form native code to
         * signal a severe problem, like a violation of a critical invariant, that
         * would otherwise have caused a crash. Client code may choose to handle
         * these errors more gracefully, preventing the application from crashing.
         * In that case, the CryptoBox involved in the panic should be closed and
         * discarded.
         */
        PANIC,

        /**
         * Initialisation of the underlying cryptographic libraries failed.
         * This error may only happen as a result of {@link CryptoBox#open} or
         * {@link CryptoBox#openWith} and indicates a severe problem.
         * Initialisation of a new CryptoBox may be retried a limited number
         * of times upon receiving this error.
         */
        INIT_ERROR,

        /**
         * Unsafe key material was detected during initialisation of a session,
         * encryption or decryption. If this error occurred as a result of
         * {@link CryptoBox#initSessionFromPreKey}, client code may retry
         * session initialisation a limited number of times with different prekeys.
         * If this error occurred as a result of {@link CryptoSession#decrypt},
         * the message should be considered invalid.
         */
        DEGENERATED_KEY,

        /**
         * An invalid string argument (e.g. file path or session ID) was provided.
         * The string may either exceed a size limit or may contain illegal
         * characters. It should be considered a programmer error.
         */
        INVALID_STRING,

        /** An unspecified error occurred. */
        UNKNOWN_ERROR
    }
}

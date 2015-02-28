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
        NO_SESSION,
        REMOTE_IDENTITY_CHANGED,
        INVALID_SIGNATURE,
        INVALID_MESSAGE,
        DUPLICATE_MESSAGE,
        TOO_DISTANT_FUTURE,
        OUTDATED_MESSAGE,
        DECODE_ERROR,
        STORAGE_ERROR,
        UNKNOWN_ERROR
    }
}

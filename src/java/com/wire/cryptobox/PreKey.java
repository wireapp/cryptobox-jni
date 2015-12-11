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
 * A <tt>PreKey</tt> contains all the necessary public key material
 * for a remote peer to initiate a session with the owner of the prekey.
 */
final public class PreKey {
    public final int id;
    public final byte[] data;

    public PreKey(int id, byte[] data) {
        this.id   = id;
        this.data = data;
    }
}

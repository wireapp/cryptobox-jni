// This Source Code Form is subject to the terms of
// the Mozilla Public License, v. 2.0. If a copy of
// the MPL was not distributed with this file, You
// can obtain one at http://mozilla.org/MPL/2.0/.

package org.pkaboo.cryptobox;

final public class PreKey {
    public final int id;
    public final byte[] data;

    public PreKey(int id, byte[] data) {
        this.id   = id;
        this.data = data;
    }
}

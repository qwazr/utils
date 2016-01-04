/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.utils;

import java.nio.ByteBuffer;
import java.nio.charset.CoderResult;

public class FastUTF8Encoder {

	private int character;
	private boolean isPair;
	private CoderResult error;
	private int lastSp;
	private int lastDp;

	public final static int MaxBytesPerChar = 4;

	public FastUTF8Encoder() {
		reset();
	}

	final public void reset() {
		error = CoderResult.UNDERFLOW;
	}

	final public CoderResult getError() {
		return error;
	}

	final private int parse(final char c, final char[] ia, final int ip, final int il) {
		assert (ia[ip] == c);
		if (Character.isHighSurrogate(c)) {
			if (il - ip < 2) {
				error = CoderResult.UNDERFLOW;
				return -1;
			}
			char d = ia[ip + 1];
			if (Character.isLowSurrogate(d)) {
				character = Character.toCodePoint(c, d);
				isPair = true;
				error = null;
				return character;
			}
			error = CoderResult.malformedForLength(1);
			return -1;
		}
		if (Character.isLowSurrogate(c)) {
			error = CoderResult.malformedForLength(1);
			return -1;
		}
		character = c;
		isPair = false;
		error = null;
		return character;
	}

	/**
	 * @param sa
	 *            source char array
	 * @param sl
	 *            source array length/limit
	 * @param da
	 *            destination address
	 * @param dp
	 *            destination position
	 * @param dl
	 *            destination limit
	 * @return UNDERFLOW is successful, OVERFLOW/ERROR otherwise
	 */
	final private CoderResult encode(char[] sa, int sp, int sl, byte[] da, int dp, int dl) {
		lastSp = sp;
		int dlASCII = dp + Math.min(sl - lastSp, dl - dp);

		while (dp < dlASCII && sa[lastSp] < 128)
			da[dp++] = (byte) sa[lastSp++];

		while (lastSp < sl) {
			int c = sa[lastSp];
			if (c < 128) {
				da[dp++] = (byte) c;
			} else if (c < 2048) {
				da[dp++] = (byte) (0xC0 | (c >> 6));
				da[dp++] = (byte) (0x80 | (c & 0x3F));
			} else if (Character.MIN_SURROGATE <= c && c <= Character.MAX_SURROGATE) {
				int uc = parse((char) c, sa, lastSp, sl);
				if (uc < 0) {
					lastDp = dp;
					return error;
				}
				da[(dp++)] = (byte) (0xF0 | uc >> 18);
				da[(dp++)] = (byte) (0x80 | uc >> 12 & 0x3F);
				da[(dp++)] = (byte) (0x80 | uc >> 6 & 0x3F);
				da[(dp++)] = (byte) (0x80 | uc & 0x3F);
				++lastSp;
			} else {
				da[(dp++)] = (byte) (0xE0 | c >> 12);
				da[(dp++)] = (byte) (0x80 | c >> 6 & 0x3F);
				da[(dp++)] = (byte) (0x80 | c & 0x3F);
			}
			++lastSp;
		}
		lastDp = dp;
		return CoderResult.UNDERFLOW;
	}

	final public CoderResult encode(final char[] charArray, final int charLength, final ByteBuffer byteBuffer) {
		CoderResult res = encode(charArray, 0, charLength, byteBuffer.array(), byteBuffer.position(),
						byteBuffer.limit());
		byteBuffer.position(lastDp);
		return res;
	}

}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.qpid.proton4j.codec.primitives;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.qpid.proton4j.buffer.ProtonBuffer;
import org.apache.qpid.proton4j.buffer.ProtonByteBufferAllocator;
import org.apache.qpid.proton4j.codec.CodecTestSupport;
import org.apache.qpid.proton4j.codec.EncodingCodes;
import org.apache.qpid.proton4j.codec.TypeDecoder;
import org.apache.qpid.proton4j.codec.decoders.primitives.ShortTypeDecoder;
import org.apache.qpid.proton4j.codec.encoders.primitives.ShortTypeEncoder;
import org.junit.Test;

public class ShortTypeCodecTest extends CodecTestSupport {

    @Test
    public void testLookupTypeDecoderForType() throws Exception {
        TypeDecoder<?> result = decoder.getTypeDecoder(Short.valueOf((short) 127));

        assertNotNull(result);
        assertEquals(Short.class, result.getTypeClass());
    }

    @Test
    public void testDecoderThrowsWhenAskedToReadWrongTypeAsThisType() throws Exception {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        buffer.writeByte(EncodingCodes.UINT);
        buffer.writeByte(EncodingCodes.UINT);

        try {
            decoder.readShort(buffer, decoderState);
            fail("Should not allow read of integer type as this type");
        } catch (IOException e) {}

        try {
            decoder.readShort(buffer, decoderState, (short) 0);
            fail("Should not allow read of integer type as this type");
        } catch (IOException e) {}
    }

    @Test
    public void testReadUByteFromEncodingCode() throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        buffer.writeByte(EncodingCodes.SHORT);
        buffer.writeShort((short) 42);
        buffer.writeByte(EncodingCodes.SHORT);
        buffer.writeShort((short) 43);
        buffer.writeByte(EncodingCodes.NULL);
        buffer.writeByte(EncodingCodes.NULL);

        assertEquals(42, decoder.readShort(buffer, decoderState).shortValue());
        assertEquals(43, decoder.readShort(buffer, decoderState, (short) 42));
        assertNull(decoder.readShort(buffer, decoderState));
        assertEquals(42, decoder.readShort(buffer, decoderState, (short) 42));
    }

    @Test
    public void testGetTypeCode() {
        assertEquals(EncodingCodes.SHORT, (byte) new ShortTypeDecoder().getTypeCode());
    }

    @Test
    public void testGetTypeClass() {
        assertEquals(Short.class, new ShortTypeEncoder().getTypeClass());
        assertEquals(Short.class, new ShortTypeDecoder().getTypeClass());
    }

    @Test
    public void testReadShortFromEncodingCode() throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        buffer.writeByte(EncodingCodes.SHORT);
        buffer.writeShort((short) 42);

        assertEquals(42, decoder.readShort(buffer, decoderState).intValue());
    }

    @Test
    public void testSkipValue() throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        for (int i = 0; i < 10; ++i) {
            encoder.writeShort(buffer, encoderState, Short.MAX_VALUE);
            encoder.writeShort(buffer, encoderState, (short) 16);
        }

        short expected = 42;

        encoder.writeObject(buffer, encoderState, expected);

        for (int i = 0; i < 10; ++i) {
            TypeDecoder<?> typeDecoder = decoder.readNextTypeDecoder(buffer, decoderState);
            assertEquals(Short.class, typeDecoder.getTypeClass());
            typeDecoder.skipValue(buffer, decoderState);
            typeDecoder = decoder.readNextTypeDecoder(buffer, decoderState);
            assertEquals(Short.class, typeDecoder.getTypeClass());
            typeDecoder.skipValue(buffer, decoderState);
        }

        final Object result = decoder.readObject(buffer, decoderState);

        assertNotNull(result);
        assertTrue(result instanceof Short);

        Short value = (Short) result;
        assertEquals(expected, value.shortValue());
    }
}

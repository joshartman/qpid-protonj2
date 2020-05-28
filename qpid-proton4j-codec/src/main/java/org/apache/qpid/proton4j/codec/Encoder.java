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
package org.apache.qpid.proton4j.codec;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.qpid.proton4j.amqp.Binary;
import org.apache.qpid.proton4j.amqp.Decimal128;
import org.apache.qpid.proton4j.amqp.Decimal32;
import org.apache.qpid.proton4j.amqp.Decimal64;
import org.apache.qpid.proton4j.amqp.DeliveryTag;
import org.apache.qpid.proton4j.amqp.DescribedType;
import org.apache.qpid.proton4j.amqp.Symbol;
import org.apache.qpid.proton4j.amqp.UnsignedByte;
import org.apache.qpid.proton4j.amqp.UnsignedInteger;
import org.apache.qpid.proton4j.amqp.UnsignedLong;
import org.apache.qpid.proton4j.amqp.UnsignedShort;
import org.apache.qpid.proton4j.buffer.ProtonBuffer;
import org.apache.qpid.proton4j.codec.encoders.DescribedTypeEncoder;

/**
 * Encode AMQP types into binary streams
 */
public interface Encoder {

    EncoderState newEncoderState();

    void writeNull(ProtonBuffer buffer, EncoderState state) throws EncodeException;

    void writeBoolean(ProtonBuffer buffer, EncoderState state, boolean value) throws EncodeException;

    void writeBoolean(ProtonBuffer buffer, EncoderState state, Boolean value) throws EncodeException;

    void writeUnsignedByte(ProtonBuffer buffer, EncoderState state, UnsignedByte value) throws EncodeException;

    void writeUnsignedByte(ProtonBuffer buffer, EncoderState state, byte value) throws EncodeException;

    void writeUnsignedShort(ProtonBuffer buffer, EncoderState state, UnsignedShort value) throws EncodeException;

    void writeUnsignedShort(ProtonBuffer buffer, EncoderState state, short value) throws EncodeException;

    void writeUnsignedShort(ProtonBuffer buffer, EncoderState state, int value) throws EncodeException;

    void writeUnsignedInteger(ProtonBuffer buffer, EncoderState state, UnsignedInteger value) throws EncodeException;

    void writeUnsignedInteger(ProtonBuffer buffer, EncoderState state, byte value) throws EncodeException;

    void writeUnsignedInteger(ProtonBuffer buffer, EncoderState state, int value) throws EncodeException;

    void writeUnsignedInteger(ProtonBuffer buffer, EncoderState state, long value) throws EncodeException;

    void writeUnsignedLong(ProtonBuffer buffer, EncoderState state, UnsignedLong value) throws EncodeException;

    void writeUnsignedLong(ProtonBuffer buffer, EncoderState state, byte value) throws EncodeException;

    void writeUnsignedLong(ProtonBuffer buffer, EncoderState state, long value) throws EncodeException;

    void writeByte(ProtonBuffer buffer, EncoderState state, byte value) throws EncodeException;

    void writeByte(ProtonBuffer buffer, EncoderState state, Byte value) throws EncodeException;

    void writeShort(ProtonBuffer buffer, EncoderState state, short value) throws EncodeException;

    void writeShort(ProtonBuffer buffer, EncoderState state, Short value) throws EncodeException;

    void writeInteger(ProtonBuffer buffer, EncoderState state, int value) throws EncodeException;

    void writeInteger(ProtonBuffer buffer, EncoderState state, Integer value) throws EncodeException;

    void writeLong(ProtonBuffer buffer, EncoderState state, long value) throws EncodeException;

    void writeLong(ProtonBuffer buffer, EncoderState state, Long value) throws EncodeException;

    void writeFloat(ProtonBuffer buffer, EncoderState state, float value) throws EncodeException;

    void writeFloat(ProtonBuffer buffer, EncoderState state, Float value) throws EncodeException;

    void writeDouble(ProtonBuffer buffer, EncoderState state, double value) throws EncodeException;

    void writeDouble(ProtonBuffer buffer, EncoderState state, Double value) throws EncodeException;

    void writeDecimal32(ProtonBuffer buffer, EncoderState state, Decimal32 value) throws EncodeException;

    void writeDecimal64(ProtonBuffer buffer, EncoderState state, Decimal64 value) throws EncodeException;

    void writeDecimal128(ProtonBuffer buffer, EncoderState state, Decimal128 value) throws EncodeException;

    void writeCharacter(ProtonBuffer buffer, EncoderState state, char value) throws EncodeException;

    void writeCharacter(ProtonBuffer buffer, EncoderState state, Character value) throws EncodeException;

    void writeTimestamp(ProtonBuffer buffer, EncoderState state, long value) throws EncodeException;

    void writeTimestamp(ProtonBuffer buffer, EncoderState state, Date value) throws EncodeException;

    void writeUUID(ProtonBuffer buffer, EncoderState state, UUID value) throws EncodeException;

    void writeBinary(ProtonBuffer buffer, EncoderState state, Binary value) throws EncodeException;

    /**
     * Writes the contents of the given {@link ProtonBuffer} value into the provided {@link ProtonBuffer}
     * instance as an AMQP Binary type.  This method does not modify the read index of the value given such
     * that is can be read later or written again without needing to reset the read index manually.
     * <p>
     * If the provided value to write is null an AMQP null type is encoded into the target buffer.
     *
     * @param buffer
     *      the target buffer where the binary value is to be encoded
     * @param state
     *      the {@link EncoderState} instance that manages the calling threads state tracking.
     * @param value
     *      the {@link ProtonBuffer} value to be encoded as an AMQP binary instance.
     *
     * @throws EncodeException if an error occurs while performing the encode
     */
    void writeBinary(ProtonBuffer buffer, EncoderState state, ProtonBuffer value) throws EncodeException;

    void writeBinary(ProtonBuffer buffer, EncoderState state, byte[] value) throws EncodeException;

    void writeString(ProtonBuffer buffer, EncoderState state, String value) throws EncodeException;

    void writeSymbol(ProtonBuffer buffer, EncoderState state, Symbol value) throws EncodeException;

    void writeSymbol(ProtonBuffer buffer, EncoderState state, String value) throws EncodeException;

    <T> void writeList(ProtonBuffer buffer, EncoderState state, List<T> value) throws EncodeException;

    <K, V> void writeMap(ProtonBuffer buffer, EncoderState state, Map<K, V> value) throws EncodeException;

    /**
     * Writes the contents of the given {@link DeliveryTag} value into the provided {@link ProtonBuffer}
     * instance as an AMQP Binary type.
     * <p>
     * If the provided value to write is null an AMQP null type is encoded into the target buffer.
     *
     * @param buffer
     *      the target buffer where the binary value is to be encoded
     * @param state
     *      the {@link EncoderState} instance that manages the calling threads state tracking.
     * @param value
     *      the {@link DeliveryTag} value to be encoded as an AMQP binary instance.
     *
     * @throws EncodeException if an error occurs while performing the encode
     */
    void writeDeliveryTag(ProtonBuffer buffer, EncoderState state, DeliveryTag value) throws EncodeException;

    void writeDescribedType(ProtonBuffer buffer, EncoderState state, DescribedType value) throws EncodeException;

    void writeObject(ProtonBuffer buffer, EncoderState state, Object value) throws EncodeException;

    void writeArray(ProtonBuffer buffer, EncoderState state, boolean[] value) throws EncodeException;

    void writeArray(ProtonBuffer buffer, EncoderState state, byte[] value) throws EncodeException;

    void writeArray(ProtonBuffer buffer, EncoderState state, short[] value) throws EncodeException;

    void writeArray(ProtonBuffer buffer, EncoderState state, int[] value) throws EncodeException;

    void writeArray(ProtonBuffer buffer, EncoderState state, long[] value) throws EncodeException;

    void writeArray(ProtonBuffer buffer, EncoderState state, float[] value) throws EncodeException;

    void writeArray(ProtonBuffer buffer, EncoderState state, double[] value) throws EncodeException;

    void writeArray(ProtonBuffer buffer, EncoderState state, char[] value) throws EncodeException;

    void writeArray(ProtonBuffer buffer, EncoderState state, Object[] value) throws EncodeException;

    void writeArray(ProtonBuffer buffer, EncoderState state, Decimal32[] value) throws EncodeException;

    void writeArray(ProtonBuffer buffer, EncoderState state, Decimal64[] value) throws EncodeException;

    void writeArray(ProtonBuffer buffer, EncoderState state, Decimal128[] value) throws EncodeException;

    void writeArray(ProtonBuffer buffer, EncoderState state, Symbol[] value) throws EncodeException;

    void writeArray(ProtonBuffer buffer, EncoderState state, UnsignedByte[] value) throws EncodeException;

    void writeArray(ProtonBuffer buffer, EncoderState state, UnsignedShort[] value) throws EncodeException;

    void writeArray(ProtonBuffer buffer, EncoderState state, UnsignedInteger[] value) throws EncodeException;

    void writeArray(ProtonBuffer buffer, EncoderState state, UnsignedLong[] value) throws EncodeException;

    void writeArray(ProtonBuffer buffer, EncoderState state, UUID[] value) throws EncodeException;

    <V> Encoder registerDescribedTypeEncoder(DescribedTypeEncoder<V> encoder) throws EncodeException;

    TypeEncoder<?> getTypeEncoder(Object value);

    TypeEncoder<?> getTypeEncoder(Class<?> typeClass);

}

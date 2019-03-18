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
package org.apache.qpid.proton4j.amqp.driver.codec;

import java.nio.ByteBuffer;

import org.apache.qpid.proton4j.amqp.Decimal64;

class Decimal64Element extends AtomicElement<Decimal64> {

    private final Decimal64 value;

    Decimal64Element(Element<?> parent, Element<?> prev, Decimal64 d) {
        super(parent, prev);
        value = d;
    }

    @Override
    public int size() {
        return isElementOfArray() ? 8 : 9;
    }

    @Override
    public Decimal64 getValue() {
        return value;
    }

    @Override
    public Data.DataType getDataType() {
        return Data.DataType.DECIMAL64;
    }

    @Override
    public int encode(ByteBuffer b) {
        int size = size();
        if (b.remaining() >= size) {
            if (size == 9) {
                b.put((byte) 0x84);
            }
            b.putLong(value.getBits());
            return size;
        } else {
            return 0;
        }
    }
}

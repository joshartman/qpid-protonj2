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
package org.apache.qpid.proton4j.transport.impl;

import java.io.IOException;

import org.apache.qpid.proton4j.amqp.security.SaslPerformative;
import org.apache.qpid.proton4j.amqp.transport.AMQPHeader;
import org.apache.qpid.proton4j.amqp.transport.Performative;
import org.apache.qpid.proton4j.buffer.ProtonBuffer;
import org.apache.qpid.proton4j.buffer.ProtonBufferAllocator;
import org.apache.qpid.proton4j.transport.Transport;
import org.apache.qpid.proton4j.transport.TransportListener;
import org.apache.qpid.proton4j.transport.TransportPipeline;

/**
 * The default Proton-J Transport implementation.
 */
public class ProtonTransport implements Transport {

    private final ProtonTransportPipeline pipeline;

    private ProtonBufferAllocator bufferAllocator;
    private TransportListener listener;

    public ProtonTransport() {
        this.pipeline = new ProtonTransportPipeline(this);
    }

    //----- Transport configuration ------------------------------------------//

    @Override
    public void setBufferAllocator(ProtonBufferAllocator allocator) {
        this.bufferAllocator = allocator;
    }

    @Override
    public ProtonBufferAllocator getBufferAllocator() {
        return bufferAllocator;
    }

    @Override
    public TransportPipeline getPipeline() {
        return pipeline;
    }

    @Override
    public void setTransportListener(TransportListener listener) {
        this.listener = listener;
    }

    @Override
    public TransportListener getTransportListener() {
        return listener;
    }

    //----- Transport interactions -------------------------------------------//

    @Override
    public void processIncoming(ProtonBuffer buffer) throws IOException {
        pipeline.fireRead(buffer);
    }

    @Override
    public void write(AMQPHeader header) throws IOException {
        pipeline.fireWrite(header);
    }

    @Override
    public void write(Performative performative, short channel, ProtonBuffer payload, Runnable payloadToLarge) throws IOException {
        pipeline.fireWrite(performative, channel, payload, payloadToLarge);
    }

    @Override
    public void write(SaslPerformative performative) throws IOException {
        pipeline.fireWrite(performative);
    }
}

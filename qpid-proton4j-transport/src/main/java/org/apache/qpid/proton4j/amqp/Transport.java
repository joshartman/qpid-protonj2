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
package org.apache.qpid.proton4j.amqp;

import java.io.IOException;

import org.apache.qpid.proton4j.buffer.ProtonBuffer;

/**
 * AMQP Transport interface.
 */
public interface Transport {

    /**
     * Feeds incoming bytes to the transport for processing.
     *
     * @param buffer
     *      The buffer containing incoming bytes.
     *
     * @throws IOException if an error occurs processing the data.
     */
    void processIncoming(ProtonBuffer buffer) throws IOException;

    /**
     * Sets the ProtocolTracer used to log frames entering and leaving the Transport.
     *
     * @param tracer
     *      The tracer instance to use, or null to disable any tracing.
     */
    void setProtocolTracer(ProtocolTracer tracer);

    /**
     * Returns the currently set ProtocolTracer, or null if none in use.
     *
     * @return the currently set ProtocolTracer, or null if none in use.
     */
    ProtocolTracer getProtocolTracer();

}

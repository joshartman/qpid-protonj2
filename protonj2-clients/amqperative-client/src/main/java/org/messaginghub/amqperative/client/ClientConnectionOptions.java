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
package org.messaginghub.amqperative.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.qpid.proton4j.amqp.Symbol;
import org.apache.qpid.proton4j.engine.Connection;
import org.messaginghub.amqperative.ConnectionOptions;

/**
 * Connection Options for the ProtonConnection implementation
 */
public final class ClientConnectionOptions extends ConnectionOptions {

    public ClientConnectionOptions(String hostname, int port) {
        this(hostname, port, null);
    }

    public ClientConnectionOptions(String hostname, int port, ConnectionOptions options) {
        super(hostname, port, options);
    }

    @Override
    public ClientConnectionOptions copyInto(ConnectionOptions options) {
        super.copyInto(options);
        return this;
    }

    /**
     * @return a URI indicating the remote peer to connect to.
     */
    public URI getRemoteURI() {
        try {
            return new URI(null, null, getHostname(), getPort(), null, null, null);
        } catch (URISyntaxException uriEx) {
            throw new IllegalArgumentException("Could not create URI from provided host and port");
        }
    }

    Connection configureConnection(Connection protonConnection) {
        protonConnection.setChannelMax(getChannelMax());
        protonConnection.setMaxFrameSize(getMaxFrameSize());
        protonConnection.setHostname(getHostname());
        protonConnection.setIdleTimeout((int) getIdleTimeout());

        // TODO - Will need this for all endpoint types so create some utilities to convert.

        if (getOfferedCapabilities() != null && getOfferedCapabilities().length > 0) {
            Symbol[] offeredCapabilities = new Symbol[getOfferedCapabilities().length];
            for (int i = 0; i < getOfferedCapabilities().length; ++i) {
                offeredCapabilities[i] = Symbol.valueOf(getOfferedCapabilities()[i]);
            }
        }

        if (getDesiredCapabilities() != null && getDesiredCapabilities().length > 0) {
            Symbol[] desiredCapabilities = new Symbol[getDesiredCapabilities().length];
            for (int i = 0; i < getDesiredCapabilities().length; ++i) {
                desiredCapabilities[i] = Symbol.valueOf(getDesiredCapabilities()[i]);
            }
        }

        if (getProperties() != null && !getProperties().isEmpty()) {
            Map<Symbol, Object> properties = new HashMap<>(getProperties().size());
            for (Entry<String, Object> entry : getProperties().entrySet()) {
                properties.put(Symbol.valueOf(entry.getKey()), entry.getValue());
            }
        }

        return protonConnection;
    }
}

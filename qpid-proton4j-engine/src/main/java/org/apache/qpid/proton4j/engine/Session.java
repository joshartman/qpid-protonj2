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
package org.apache.qpid.proton4j.engine;

import java.util.Set;

/**
 * AMQP Session interface
 */
public interface Session extends Endpoint<Session> {

    /**
     * @return the local session state
     */
    SessionState getState();

    /**
     * @return the parent {@link Connection} for this Session.
     */
    Connection getConnection();

    /**
     * @return the parent {@link Connection} of the {@link Link}
     */
    @Override
    Connection getParent();

    /**
     * Returns a {@link Set} of all {@link Sender} and {@link Receiver} instances that are being tracked by
     * this {@link Session}.
     *
     * @return a set of Sender and Receiver instances tracked by this session.
     */
    Set<Link<?>> links();

    /**
     * Returns a {@link Set} of {@link Sender} instances that are being tracked by this {@link Session}.
     *
     * @return a set of Sender instances tracked by this session.
     */
    Set<Sender> senders();

    /**
     * Returns a {@link Set} of {@link Receiver} instances that are being tracked by this {@link Session}.
     *
     * @return a set of Receiver instances tracked by this session.
     */
    Set<Receiver> receivers();

    //----- Session sender and receiver factory methods

    /**
     * Create a new {@link Sender} link using the provided name.
     *
     * @param name
     *      The name to assign to the created {@link Sender}
     *
     * @return a newly created {@link Sender} instance.
     *
     * @throws IllegalStateException if the {@link Session} has already been closed.
     */
    Sender sender(String name) throws IllegalStateException;

    /**
     * Create a new {@link Receiver} link using the provided name
     *
     * @param name
     *      The name to assign to the created {@link Receiver}
     *
     * @return a newly created {@link Receiver} instance.
     *
     * @throws IllegalStateException if the {@link Session} has already been closed.
     */
    Receiver receiver(String name) throws IllegalStateException;

    //----- Configure the local end of the Session

    /**
     * Sets the maximum number of bytes this session can be sent from the remote.
     *
     * @param incomingCapacity
     *      maximum number of incoming bytes this session will allow
     *
     * @return this Session
     *
     * @throws IllegalStateException if the {@link Session} has already been closed.
     */
    Session setIncomingCapacity(int incomingCapacity) throws IllegalStateException;

    /**
     * @return the current incoming capacity of this session.
     */
    int getIncomingCapacity();

    //----- View the remote end of the Session configuration

    /**
     * @return the remote session state (as last communicated)
     */
    SessionState getRemoteState();

    //----- Remote events for AMQP Session resources

    /**
     * Sets a {@link EventHandler} for when an AMQP Attach frame is received from the remote peer for a sending link.
     *
     * Used to process remotely initiated sending link.  Locally initiated links have their own EventHandler
     * invoked instead.  This method is Typically used by servers to listen for remote Receiver creation.
     * If an event handler for remote sender open is registered on this Session for a link scoped to it then
     * this handler will be invoked instead of the variant in the Connection API.
     *
     * @param remoteSenderOpenEventHandler
     *          the EventHandler that will be signaled when a sender link is remotely opened.
     *
     * @return this session for chaining
     */
    Session senderOpenHandler(EventHandler<Sender> remoteSenderOpenEventHandler);

    /**
     * Sets a {@link EventHandler} for when an AMQP Attach frame is received from the remote peer for a receiving link.
     *
     * Used to process remotely initiated receiving link.  Locally initiated links have their own EventHandler
     * invoked instead.  This method is Typically used by servers to listen for remote Sender creation.
     * If an event handler for remote sender open is registered on this Session for a link scoped to it then
     * this handler will be invoked instead of the variant in the Connection API.
     *
     * @param remoteReceiverOpenEventHandler
     *          the EventHandler that will be signaled when a receiver link is remotely opened.
     *
     * @return this session for chaining
     */
    Session receiverOpenHandler(EventHandler<Receiver> remoteReceiverOpenEventHandler);

}

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

import org.apache.qpid.proton4j.amqp.transport.DeliveryState;

/**
 * Root of an AMQP Delivery
 */
public interface Delivery {

    /**
     * @return the link that this {@link Delivery} is bound to.
     */
    Link<?> getLink();

    //----- Delivery state

    /**
     * @return the Delivery tag assigned to this Delivery.
     */
    byte[] getTag();

    /**
     * @return the {@link DeliveryState} at the local side of this Delivery.
     */
    DeliveryState getLocalState();

    /**
     * @return the {@link DeliveryState} at the remote side of this Delivery.
     */
    DeliveryState getRemoteState();

    /**
     * Gets the message-format for this Delivery, representing the 32bit value using an int.
     *
     * The default value is 0 as per the message format defined in the core AMQP 1.0 specification.<p>
     *
     * See the following for more details:<br>
     * <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transport-v1.0-os.html#type-transfer">
     *          http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transport-v1.0-os.html#type-transfer</a><br>
     * <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transport-v1.0-os.html#type-message-format">
     *          http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transport-v1.0-os.html#type-message-format</a><br>
     * <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#section-message-format">
     *          http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#section-message-format</a><br>
     * <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#definition-MESSAGE-FORMAT">
     *          http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#definition-MESSAGE-FORMAT</a><br>
     *
     * @return the message-format for this Delivery.
     */
    int getMessageFormat();

    /**
     * Check for whether the delivery is still partial.
     *
     * For a receiving Delivery, this means the delivery does not hold
     * a complete message payload as all the content hasn't been
     * received yet. Note that an {@link #isAborted() aborted} delivery
     * will also be considered partial and the full payload won't
     * be received.
     *
     * For a sending Delivery, this means that the application has not marked
     * the delivery as complete yet.
     *
     * @return true if the delivery is partial
     *
     * @see #isAborted()
     */
    public boolean isPartial();

    /**
     * @return true if the delivery has been aborted.
     */
    boolean isAborted();

    /**
     * @return true if the delivery has been settled locally.
     */
    boolean isSettled();

    /**
     * @return true if the delivery has been settled by the remote.
     */
    boolean isRemotelySettled();

    //----- Delivery operations

    /**
     * updates the state of the delivery
     *
     * @param state the new delivery state
     */
    public void disposition(DeliveryState state);

    /**
     * Settles this delivery.
     *
     * TODO - Fully document the result of this call.
     *
     * @return this delivery instance.
     */
    public Delivery settle();

    /**
     * Settles this delivery with the given disposition.
     *
     * TODO - Fully document the result of this call.
     *
     * @param state the new delivery state
     *
     * @return this delivery instance.
     */
    public Delivery settle(DeliveryState state);

}

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
package org.apache.qpid.proton4j.engine.impl;

import org.apache.qpid.proton4j.amqp.Binary;
import org.apache.qpid.proton4j.amqp.transport.Begin;
import org.apache.qpid.proton4j.amqp.transport.Disposition;
import org.apache.qpid.proton4j.amqp.transport.Flow;
import org.apache.qpid.proton4j.amqp.transport.Role;
import org.apache.qpid.proton4j.amqp.transport.Transfer;
import org.apache.qpid.proton4j.buffer.ProtonBuffer;
import org.apache.qpid.proton4j.engine.exceptions.ProtocolViolationException;
import org.apache.qpid.proton4j.engine.util.SplayMap;

/**
 * Holds Session level credit window information.
 */
@SuppressWarnings("unused")
public class ProtonSessionOutgoingWindow {

    private static final int DEFAULT_WINDOW_SIZE = Integer.MAX_VALUE; // biggest legal value

    private final ProtonSession session;
    private final ProtonEngine engine;

    // This is used for the delivery-id actually stamped in each transfer frame of a given message delivery.
    private int outgoingDeliveryId = 0;

    // These are used for the session windows communicated via Begin/Flow frames
    // and the conceptual transfer-id relating to updating them.
    private long outgoingWindow = DEFAULT_WINDOW_SIZE;
    private int nextOutgoingId = 0;

    private long remoteIncomingWindow;
    private int remoteNextIncomingId = nextOutgoingId;

    private int outgoingBytes;

    // Obtained from the connection after the session is opened as that point in time
    // marks when this value is set in stone.
    private long maxFrameSize;

    // TODO - Better if this is a primitive keyed data structure
    private final SplayMap<ProtonOutgoingDelivery> unsettled = new SplayMap<>();

    public ProtonSessionOutgoingWindow(ProtonSession session) {
        this.session = session;
        this.engine = session.getConnection().getEngine();
    }

    /**
     * Initialize the session level window values on the outbound Begin
     *
     * @param begin
     *      The {@link Begin} performative that is about to be sent.
     *
     * @return the configured performative
     */
    Begin configureOutbound(Begin begin) {
        maxFrameSize = session.getConnection().getMaxFrameSize();

        begin.setNextOutgoingId(nextOutgoingId);
        begin.setOutgoingWindow(outgoingWindow);

        return begin;
    }

    int getAndIncrementNextDeliveryId() {
        return outgoingDeliveryId++;
    }

    //----- Handle incoming performatives relevant to the session.

    /**
     * Update the session level window values based on remote information.
     *
     * @param begin
     *      The {@link Begin} performative received from the remote.
     *
     * @return the given performative for chaining
     */
    Begin handleBegin(Begin begin) {
        remoteIncomingWindow = begin.getIncomingWindow();
        return begin;
    }

    /**
     * Update the session window state based on an incoming {@link Flow} performative
     *
     * @param flow
     *      the incoming {@link Flow} performative to process.
     */
    Flow handleFlow(Flow flow) {
        if (flow.hasNextIncomingId()) {
            remoteNextIncomingId = (int) flow.getNextIncomingId();
            remoteIncomingWindow = (flow.getNextIncomingId() + flow.getIncomingWindow()) - nextOutgoingId;
        } else {
            remoteIncomingWindow = flow.getIncomingWindow();
        }

        return flow;
    }

    /**
     * Update the session window state based on an incoming {@link Transfer} performative
     *
     * @param transfer
     *      the incoming {@link Transfer} performative to process.
     */
    Transfer handleTransfer(Transfer transfer, ProtonBuffer payload) {
        return transfer;
    }

    /**
     * Update the state of any sent Transfers that are indicated in the disposition
     * with the state information conveyed therein.
     *
     * @param disposition
     *      The {@link Disposition} performative to process
     *
     * @return the {@link Disposition}
     */
    Disposition handleDisposition(Disposition disposition) {
        final long first = disposition.getFirst();
        final long last = disposition.hasLast() ? disposition.getLast() : first;

        if (last < first) {
            engine.engineFailed(new ProtocolViolationException(
                "Received Disposition with mismatched first and last delivery Ids: [" + first + ", " + last + "]"));
        }

        long index = first;

        // TODO - Common case will be one element so optimize for that.

        do {
            // TODO - Here is a chance for optimization, if the map containing the unsettled
            //        is navigable then we can use a sub-map to limit the range to the first
            //        and last elements and then simply walk next until the end without checking
            //        each index between for its presence.
            // TODO - The casting required due to long to integer indexing is messy
            ProtonOutgoingDelivery delivery = unsettled.get((int) index);
            if (delivery != null) {
                if (disposition.getSettled()) {
                    unsettled.remove((int) index);
                }

                delivery.getLink().handleDisposition(disposition, delivery);
            }
        } while (index++ <= last);

        return disposition;
    }

    void writeFlow(ProtonLink<?> link) {
        session.writeFlow(link);
    }

    //----- Handle sender link actions in the session window context

    void processSend(ProtonSender sender, ProtonOutgoingDelivery delivery, ProtonBuffer payload) {
        // For a transfer that hasn't completed but has no bytes in the final transfer write we want
        // to allow a transfer to go out with the more flag as false.

        boolean wasThereMore = delivery.isPartial();

        if (!delivery.isSettled()) {
            // TODO - Casting is ugly
            unsettled.put((int) delivery.getDeliveryId(), delivery);
        }

        do {
            // TODO - Can we cache or pool these to not generate garbage on each send ?
            Transfer transfer = new Transfer();

            transfer.setDeliveryId(delivery.getDeliveryId());
            // TODO - Delivery Tag improvements, have our own DeliveryTag type perhaps that pools etc.
            // TODO - If we track number of transfers for a larger delivery we could omit this on continuations
            transfer.setDeliveryTag(new Binary(delivery.getTag()));
            transfer.setMore(wasThereMore);
            transfer.setResume(false);
            transfer.setAborted(false);
            transfer.setBatchable(false);
            transfer.setRcvSettleMode(null);
            transfer.setHandle(sender.getHandle());
            transfer.setSettled(delivery.isSettled());
            transfer.setState(delivery.getLocalState());

            // TODO - Write up to session window limits or until done.
            try {
                engine.pipeline().fireWrite(transfer, session.getLocalChannel(), payload, () -> transfer.setMore(true));
            } finally {
                delivery.afterTransferWritten();
            }

            // Update session window tracking
            nextOutgoingId++;
            remoteIncomingWindow--;
        } while (payload.isReadable());
    }

    void processDisposition(ProtonSender sender, ProtonOutgoingDelivery delivery) {
        // TODO - Can we cache or pool these to not generate garbage on each send ?
        Disposition disposition = new Disposition();

        disposition.setFirst(delivery.getDeliveryId());
        disposition.setLast(delivery.getDeliveryId());
        disposition.setRole(Role.SENDER);
        disposition.setSettled(delivery.isSettled());
        disposition.setBatchable(false);
        disposition.setState(delivery.getLocalState());

        // TODO - Casting is ugly but our ID values are longs
        unsettled.remove((int) delivery.getDeliveryId());

        engine.pipeline().fireWrite(disposition, session.getLocalChannel(), null, null);
    }

    void processAbort(ProtonSender sender, ProtonOutgoingDelivery delivery) {
        // TODO - Can we cache or pool these to not generate garbage on each send ?
        Transfer transfer = new Transfer();

        transfer.setDeliveryId(delivery.getDeliveryId());
        transfer.setDeliveryTag(new Binary(delivery.getTag()));
        transfer.setMore(delivery.isPartial());
        transfer.setState(null);
        transfer.setSettled(false);
        transfer.setResume(false);
        transfer.setAborted(false);
        transfer.setBatchable(false);
        transfer.setRcvSettleMode(null);
        transfer.setHandle(sender.getHandle());

        // Ensure we don't track the aborted delivery any longer.
        unsettled.remove((int) delivery.getDeliveryId());

        engine.pipeline().fireWrite(transfer, session.getLocalChannel(), null, null);
    }

    //----- Access to internal state useful for tests

    long getOutgoingBytes() {
        return outgoingBytes;
    }

    int getNextOutgoingId() {
        return nextOutgoingId;
    }

    long getOutgoingWindow() {
        return outgoingWindow;
    }

    int getRemoteNextIncomingId() {
        return remoteNextIncomingId;
    }

    long getRemoteIncomingWindow() {
        return remoteIncomingWindow;
    }
}

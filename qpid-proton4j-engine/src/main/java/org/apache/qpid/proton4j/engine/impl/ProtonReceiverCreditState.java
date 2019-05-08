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

import org.apache.qpid.proton4j.amqp.transport.Attach;
import org.apache.qpid.proton4j.amqp.transport.Disposition;
import org.apache.qpid.proton4j.amqp.transport.Flow;
import org.apache.qpid.proton4j.amqp.transport.Transfer;
import org.apache.qpid.proton4j.buffer.ProtonBuffer;
import org.apache.qpid.proton4j.engine.exceptions.ProtocolViolationException;
import org.apache.qpid.proton4j.engine.util.DeliveryIdTracker;
import org.apache.qpid.proton4j.engine.util.SplayMap;

/**
 * Credit state handler for {@link Receiver} links.
 */
public class ProtonReceiverCreditState implements ProtonLinkCreditState<ProtonIncomingDelivery> {

    private final ProtonReceiver receiver;
    private final ProtonSessionIncomingWindow sessionWindow;

    private int credit;
    private int deliveryCount;

    private final DeliveryIdTracker currentDeliveryId = new DeliveryIdTracker();

    // TODO - Better if this is a primitive keyed data structure
    private final SplayMap<ProtonIncomingDelivery> unsettled = new SplayMap<>();

    public ProtonReceiverCreditState(ProtonReceiver parent, ProtonSessionIncomingWindow sessionWindow) {
        this.sessionWindow = sessionWindow;
        this.receiver = parent;
    }

    @Override
    public int getCredit() {
        return credit;
    }

    @Override
    public int getDeliveryCount() {
        return deliveryCount;
    }

    public void setCredit(int credit) {
        if (this.credit != credit) {
            this.credit = credit;
            if (receiver.isRemotelyOpened()) {
                sessionWindow.writeFlow(receiver);
            }
        }
    }

    @Override
    public Attach handleAttach(Attach attach) {
        if (credit > 0) {
            sessionWindow.writeFlow(receiver);
        }

        return attach;
    }

    @Override
    public Flow handleFlow(Flow flow) {
        if (flow.getDrain()) {
            deliveryCount = (int) flow.getDeliveryCount();
            credit = (int) flow.getLinkCredit();
            if (credit != 0) {
                throw new IllegalArgumentException("Receiver read flow with drain set but credit was not zero");
            }

            // TODO - Error on credit being non-zero for drain response ?

            receiver.signalReceiverDrained();
        }
        return flow;
    }

    @Override
    public Transfer handleTransfer(Transfer transfer, ProtonBuffer payload) {
        final ProtonIncomingDelivery delivery;

        boolean isFirstTransfer = true;

        if (!currentDeliveryId.isEmpty() && (!transfer.hasDeliveryId() || currentDeliveryId.equals((int) transfer.getDeliveryId()))) {
            // TODO - Casting is ugly but our ID values are longs
            delivery = unsettled.get(currentDeliveryId.intValue());
            isFirstTransfer = false;
        } else {
            verifyNewDeliveryIdSequence(transfer, currentDeliveryId);

            delivery = new ProtonIncomingDelivery(receiver, transfer.getDeliveryId(), transfer.getDeliveryTag());
            delivery.setMessageFormat((int) transfer.getMessageFormat());

            // TODO - Casting is ugly but our ID values are longs
            unsettled.put((int) transfer.getDeliveryId(), delivery);
        }

        if (transfer.hasState()) {
            delivery.remoteState(transfer.getState());
        }

        if (transfer.getSettled() || transfer.getAborted()) {
            delivery.remotelySettled();
        }

        delivery.appendToPayload(payload);

        boolean done = transfer.getAborted() || !transfer.getMore();
        if (done) {
            if (transfer.getAborted()) {
                delivery.aborted();
            } else {
                delivery.completed();
            }

            credit = Math.min(credit - 1, 0);
            deliveryCount++;
            currentDeliveryId.reset();
        } else {
            currentDeliveryId.set((int) transfer.getDeliveryId());
        }

        if (isFirstTransfer) {
            receiver.signalDeliveryReceived(delivery);
        } else {
            receiver.signalDeliveryUpdated(delivery);
        }

        return transfer;
    }

    private void verifyNewDeliveryIdSequence(Transfer transfer, DeliveryIdTracker currentDeliveryId) {
        // TODO - Fail engine, session, or link ?
        // TODO - Move to session window once fully worked out

        if (!transfer.hasDeliveryId()) {
            receiver.getSession().getConnection().getEngine().engineFailed(
                 new ProtocolViolationException("No delivery-id specified on first Transfer of new delivery"));
        }

        // Doing a primitive comparison, uses intValue() since its a uint sequence
        // and we need the primitive values to wrap appropriately during comparison.
        if (sessionWindow.incrementNextDeliveryId() != transfer.getDeliveryId()) {
            receiver.getSession().getConnection().getEngine().engineFailed(
                new ProtocolViolationException("Expected delivery-id " + sessionWindow.getNextDeliveryId() +
                                               ", got " + transfer.getDeliveryId()));
        }

        if (!currentDeliveryId.isEmpty()) {
            receiver.getSession().getConnection().getEngine().engineFailed(
                new ProtocolViolationException("Illegal multiplex of deliveries on same link with delivery-id " +
                                               currentDeliveryId + " and " + transfer.getDeliveryId()));
        }
    }

    @Override
    public Disposition handleDisposition(Disposition disposition, ProtonIncomingDelivery delivery) {
        boolean updated = false;

        if (disposition.getState() != null && !disposition.getState().equals(delivery.getRemoteState())) {
            updated = true;
            delivery.remoteState(disposition.getState());
        }

        if (disposition.getSettled() && !delivery.isRemotelySettled()) {
            updated = true;
            // TODO - Casting is ugly but right now our unsigned integers are longs
            unsettled.remove((int) delivery.getDeliveryId());
            delivery.remotelySettled();
        }

        if (updated) {
            delivery.getLink().signalDeliveryUpdated(delivery);
        }

        return disposition;
    }

    @Override
    public ProtonReceiverCreditState snapshot() {
        ProtonReceiverCreditState snapshot = new ProtonReceiverCreditState(receiver, sessionWindow);
        snapshot.credit = credit;
        snapshot.deliveryCount = deliveryCount;
        return snapshot;
    }

    //----- Actions invoked from Delivery instances

    void disposition(ProtonIncomingDelivery delivery) {
        if (delivery.isSettled()) {
            // TODO - Casting is ugly but right now our unsigned integers are longs
            unsettled.remove((int) delivery.getDeliveryId());
        }

        sessionWindow.processDisposition(receiver, delivery);
    }
}

/*
 * NOTE: This copyright does *not* cover user programs that use Hyperic
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 * "derived work".
 *
 * Copyright (C) [2004-2013], VMware, Inc.
 * This file is part of Hyperic.
 *
 * Hyperic is free software; you can redistribute it and/or modify
 * it under the terms version 2 of the GNU General Public License as
 * published by the Free Software Foundation. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */
package org.hyperic.hq.notifications;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.hq.notifications.model.BaseNotification;
import org.hyperic.hq.notifications.model.InternalResourceDetailsType;
import org.hyperic.util.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

@Component
public class EndpointQueue {
    private final Log log = LogFactory.getLog(EndpointQueue.class);
// XXX should make this configurable in some way
    protected final static int QUEUE_LIMIT = 10000;
    private static final long TASK_INTERVAL = 30000;
    @Autowired
    private ThreadPoolTaskScheduler notificationExecutor;

    // TODO~ change to write through versioning (each node would have versioning -
    // write on one version, read another, then sync between them), w/o will pose problems in scale
    private final Map<Long, AccumulatedRegistrationData> registrationData = new HashMap<Long, AccumulatedRegistrationData>();
    private final AtomicInteger numConsumers = new AtomicInteger(0);

    public void register(NotificationEndpoint endpoint, Transformer<InternalNotificationReport, String> transformer) {
        register(endpoint,null,transformer);
    }
    
    public int getNumConsumers() {
        return numConsumers.get();
    }

    public void register(NotificationEndpoint endpoint, InternalResourceDetailsType resourceDetailsType,
                         Transformer<InternalNotificationReport, String> transformer) {
        final boolean debug = log.isDebugEnabled();
        final AccumulatedRegistrationData data =
            new AccumulatedRegistrationData(endpoint, QUEUE_LIMIT, resourceDetailsType);
        synchronized (registrationData) {
            if (registrationData.containsKey(endpoint.getRegistrationId())) {
                if (debug) log.debug("can not register endpoint=" + endpoint + " twice");
                return;
            }
            final long regId = endpoint.getRegistrationId();
            registrationData.put(regId, data);
            numConsumers.incrementAndGet();
            schedule(endpoint, data, transformer);
        }
        if (log.isDebugEnabled()) {
            log.debug("new notification registration=" + endpoint);
        }
    }
    
    private void schedule(final NotificationEndpoint endpoint, AccumulatedRegistrationData data,
                          final Transformer<InternalNotificationReport, String> transformer) {
        if (!endpoint.canPublish()) {
            return;
        }
        final Runnable task = new Runnable() {
            public void run() {
                try {
                    final long registrationId = endpoint.getRegistrationId();
                    final InternalNotificationReport report = poll(registrationId);
                    final String message = transformer.transform(report);
                    endpoint.publishMessage(message);
                } catch (Throwable t) {
                    log.error(t, t);
                }
            }
        };
        ScheduledFuture<?> schedule = notificationExecutor.scheduleWithFixedDelay(task, TASK_INTERVAL);
        data.setSchedule(schedule);
    }

    public NotificationEndpoint unregister(long registrationID) {
        AccumulatedRegistrationData data = null;
        synchronized (registrationData) {
            // don't delete the data, we want to be able to access the endpoint by registrationId
            data = registrationData.get(registrationID);
            numConsumers.decrementAndGet();
            data.markInvalid();
            data.clear();
        }
        final ScheduledFuture<?> schedule = data.getSchedule();
        if (schedule != null) {
            schedule.cancel(true);
        }
        if (log.isDebugEnabled()) { 
            String s =  "there is no queue assigned for destination";
            String msg = (data == null) ? s : "removing the queue assigned for regId " + registrationID;
            log.debug(msg);
        }
        return data == null ? null : data.getNotificationEndpoint();
    }

    public InternalNotificationReport poll(long registrationId) {
        final InternalNotificationReport rtn = new InternalNotificationReport();
        final List<BaseNotification> notifications = new ArrayList<BaseNotification>();
        synchronized (registrationData) {
            AccumulatedRegistrationData data = registrationData.get(registrationId);
            if (data == null || !data.isValid()) {
                return rtn;
            }
            data.drainTo(notifications);
            rtn.setNotifications(notifications);
            rtn.setResourceDetailsType(data.getResourceContentType());
            return rtn;
        }
    }
    
    public <T extends BaseNotification> void publishAsync(Map<NotificationEndpoint, Collection<T>> map) {
        synchronized (registrationData) {
            for (final Entry<NotificationEndpoint, Collection<T>> entry : map.entrySet()) {
                final NotificationEndpoint endpoint = entry.getKey();
                final Collection<T> list = entry.getValue();
                final AccumulatedRegistrationData data = registrationData.get(endpoint.getRegistrationId());
                if (data != null) {
                    data.addAll(list);
                }
            }
        }
    }
    
    public NotificationEndpoint getEndpoint(Long registrationId) {
        if (registrationId == null) {
            return null;
        }
        synchronized (registrationData) {
            AccumulatedRegistrationData data = registrationData.get(registrationId);
            if (data != null) {
                return data.getNotificationEndpoint();
            }
        }
        return null;
    }

}
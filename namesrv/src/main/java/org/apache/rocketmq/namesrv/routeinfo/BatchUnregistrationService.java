/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.namesrv.routeinfo;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.rocketmq.common.ServiceThread;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.namesrv.NamesrvConfig;
import org.apache.rocketmq.common.protocol.header.namesrv.UnRegisterBrokerRequestHeader;
import org.apache.rocketmq.logging.InternalLogger;
import org.apache.rocketmq.logging.InternalLoggerFactory;

/**
 * BatchUnregistrationService provides a mechanism to unregister brokers in batch manner, which speeds up broker-offline
 * process.
 */
public class BatchUnregistrationService extends ServiceThread {
    private final RouteInfoManager routeInfoManager;
    private BlockingQueue<UnRegisterBrokerRequestHeader> unregistrationQueue;
    private static final InternalLogger log = InternalLoggerFactory.getLogger(LoggerName.NAMESRV_LOGGER_NAME);

    public BatchUnregistrationService(RouteInfoManager routeInfoManager, NamesrvConfig namesrvConfig) {
        this.routeInfoManager = routeInfoManager;
        this.unregistrationQueue = new LinkedBlockingQueue<>(namesrvConfig.getUnRegisterBrokerQueueCapacity());
    }

    /**
     * Submits an unregister request to this queue.
     *
     * @param unRegisterRequest the request to submit
     * @return {@code true} if the request was added to this queue, else {@code false}
     */
    public boolean submit(UnRegisterBrokerRequestHeader unRegisterRequest) {
        return unregistrationQueue.offer(unRegisterRequest);
    }

    @Override
    public String getServiceName() {
        return BatchUnregistrationService.class.getName();
    }

    /**
     * 启动线程时执行run方法
     */
    @Override
    public void run() {
        while (!this.isStopped()) {
            try {
                //获取队列头部元素
                final UnRegisterBrokerRequestHeader request = unregistrationQueue.take();
                Set<UnRegisterBrokerRequestHeader> unregistrationRequests = new HashSet<>();
                //移除unregistrationQueue队列中所有的元素，并加入到unregistrationRequests集合中
                unregistrationQueue.drainTo(unregistrationRequests);

                // Add polled request 将队头元素也加入其中（思考：为什么要这样繁琐呢？不能直接省去队头元素的出队和加入集合这两步吗？）
                unregistrationRequests.add(request);
                //将注销队列中的所有请求都传给注销接口
                this.routeInfoManager.unRegisterBroker(unregistrationRequests);
            } catch (Throwable e) {
                log.error("Handle unregister broker request failed", e);
            }
        }
    }

    // For test only
    int queueLength() {
        return this.unregistrationQueue.size();
    }
}

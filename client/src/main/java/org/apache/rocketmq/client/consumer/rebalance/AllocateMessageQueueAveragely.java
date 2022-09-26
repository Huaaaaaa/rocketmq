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
package org.apache.rocketmq.client.consumer.rebalance;

import java.util.ArrayList;
import java.util.List;
import org.apache.rocketmq.client.log.ClientLogger;
import org.apache.rocketmq.logging.InternalLogger;
import org.apache.rocketmq.common.message.MessageQueue;

/**
 * Average Hashing queue algorithm
 */
public class AllocateMessageQueueAveragely extends AbstractAllocateMessageQueueStrategy {

    public AllocateMessageQueueAveragely() {
        log = ClientLogger.getLog();
    }

    public AllocateMessageQueueAveragely(InternalLogger log) {
        super(log);
    }

    /**
     * @param consumerGroup current consumer group
     * @param currentCID current consumer id
     * @param mqAll message queue set in current topic
     * @param cidAll consumer set in current consumer group
     * @return
     */
    @Override
    public List<MessageQueue> allocate(String consumerGroup, String currentCID, List<MessageQueue> mqAll,
        List<String> cidAll) {

        List<MessageQueue> result = new ArrayList<MessageQueue>();
        //1.校验消费者、队列等信息
        if (!check(consumerGroup, currentCID, mqAll, cidAll)) {
            return result;
        }

        //2.获取当前消费者的位置索引
        int index = cidAll.indexOf(currentCID);
        //3.队列个数 与 消费者个数 取余
        int mod = mqAll.size() % cidAll.size();
        //4.确定每个消费者分配到的队列个数：
        //1）当队列个数小于消费者数时，每个消费者只能分到一个
        //2）否则：索引小于余数的消费者可以分配到除数+1个；索引大于余数的消费者可以分配到除数个；
        int averageSize =
            mqAll.size() <= cidAll.size() ? 1 : (mod > 0 && index < mod ? mqAll.size() / cidAll.size()
                + 1 : mqAll.size() / cidAll.size());
        //5.获取要取的队列起始位置
        int startIndex = (mod > 0 && index < mod) ? index * averageSize : index * averageSize + mod;
        //6.获取要从队列中取的范围：平均个数与队列总数-起始索引
        int range = Math.min(averageSize, mqAll.size() - startIndex);
        //7.将指定范围内的队列加入到list中返回，即为当前消费者分配的队列
        for (int i = 0; i < range; i++) {
            result.add(mqAll.get((startIndex + i) % mqAll.size()));
        }
        return result;
    }

    @Override
    public String getName() {
        return "AVG";
    }
}

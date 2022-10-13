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

/**
 * $Id: NamesrvConfig.java 1839 2013-05-16 02:12:02Z vintagewang@apache.org $
 */
package org.apache.rocketmq.common.namesrv;

import java.io.File;
import org.apache.rocketmq.common.MixAll;

public class NamesrvConfig {

    /**
     * RocketMQ主目录，默认用户主目录
     */
    private String rocketmqHome = System.getProperty(MixAll.ROCKETMQ_HOME_PROPERTY, System.getenv(MixAll.ROCKETMQ_HOME_ENV));
    /**
     * kv配置文件路径，包含顺序消息主题的配置信息
     */
    private String kvConfigPath = System.getProperty("user.home") + File.separator + "namesrv" + File.separator + "kvConfig.json";
    /**
     * NameServer配置文件路径，建议使用-c指定NameServer配置文件路径
     */
    private String configStorePath = System.getProperty("user.home") + File.separator + "namesrv" + File.separator + "namesrv.properties";
    private String productEnvName = "center";
    /**
     * 是否支持集群测试，默认为false
     */
    private boolean clusterTest = false;
    /**
     * 是否支持顺序消息，默认为false
     */
    private boolean orderMessageEnable = false;
    private boolean returnOrderTopicConfigToBroker = true;

    /**
     * 处理客户端请求的线程数
     * Indicates the nums of thread to handle client requests, like GET_ROUTEINTO_BY_TOPIC.
     */
    private int clientRequestThreadPoolNums = 8;
    /**
     * 处理broker请求的线程数
     * Indicates the nums of thread to handle broker or operation requests, like REGISTER_BROKER.
     */
    private int defaultThreadPoolNums = 16;
    /**
     * 处理客户端请求的线程池最大队列数，默认50000
     * Indicates the capacity of queue to hold client requests.
     */
    private int clientRequestThreadPoolQueueCapacity = 50000;
    /**
     * 处理broker请求的线程池最大队列数，默认10000
     * Indicates the capacity of queue to hold broker or operation requests.
     */
    private int defaultThreadPoolQueueCapacity = 10000;
    /**
     * 扫描未激活broker的时间间隔，默认5秒，即每过5秒就扫描一次未活跃的broker
     * Interval of periodic scanning for non-active broker;
     */
    private long scanNotActiveBrokerInterval = 5 * 1000;

    /**
     * 未注册的broker队列长度
     */
    private int unRegisterBrokerQueueCapacity = 3000;

    /**
     * 是否支持slave扮演（升级）为master
     * Support acting master or not.
     * 当主节点下线之后从节点可以扮演主节点并支持以下操作
     * The slave can be an acting master when master node is down to support following operations:
     * 1. support lock/unlock message queue operation. 支持锁定/解锁消息队列
     * 2. support searchOffset, query maxOffset/minOffset operation. 支持搜索消息位点、查询最小和最大消息位点
     * 3. support query earliest msg store time. 支持查询最早的消息存储时间
     */
    private boolean supportActingMaster = false;

    private volatile boolean enableAllTopicList = true;


    private volatile boolean enableTopicList = true;

    private volatile boolean notifyMinBrokerIdChanged = false;

    /**
     * Is startup the controller in this name-srv
     * 是否在当前nameserver中启动controller：DLedger Controller以插件的模式内嵌到NameServer进行部署
     */
    private boolean enableControllerInNamesrv = false;


    public boolean isOrderMessageEnable() {
        return orderMessageEnable;
    }

    public void setOrderMessageEnable(boolean orderMessageEnable) {
        this.orderMessageEnable = orderMessageEnable;
    }

    public String getRocketmqHome() {
        return rocketmqHome;
    }

    public void setRocketmqHome(String rocketmqHome) {
        this.rocketmqHome = rocketmqHome;
    }

    public String getKvConfigPath() {
        return kvConfigPath;
    }

    public void setKvConfigPath(String kvConfigPath) {
        this.kvConfigPath = kvConfigPath;
    }

    public String getProductEnvName() {
        return productEnvName;
    }

    public void setProductEnvName(String productEnvName) {
        this.productEnvName = productEnvName;
    }

    public boolean isClusterTest() {
        return clusterTest;
    }

    public void setClusterTest(boolean clusterTest) {
        this.clusterTest = clusterTest;
    }

    public String getConfigStorePath() {
        return configStorePath;
    }

    public void setConfigStorePath(final String configStorePath) {
        this.configStorePath = configStorePath;
    }

    public boolean isReturnOrderTopicConfigToBroker() {
        return returnOrderTopicConfigToBroker;
    }

    public void setReturnOrderTopicConfigToBroker(boolean returnOrderTopicConfigToBroker) {
        this.returnOrderTopicConfigToBroker = returnOrderTopicConfigToBroker;
    }

    public int getClientRequestThreadPoolNums() {
        return clientRequestThreadPoolNums;
    }

    public void setClientRequestThreadPoolNums(final int clientRequestThreadPoolNums) {
        this.clientRequestThreadPoolNums = clientRequestThreadPoolNums;
    }

    public int getDefaultThreadPoolNums() {
        return defaultThreadPoolNums;
    }

    public void setDefaultThreadPoolNums(final int defaultThreadPoolNums) {
        this.defaultThreadPoolNums = defaultThreadPoolNums;
    }

    public int getClientRequestThreadPoolQueueCapacity() {
        return clientRequestThreadPoolQueueCapacity;
    }

    public void setClientRequestThreadPoolQueueCapacity(final int clientRequestThreadPoolQueueCapacity) {
        this.clientRequestThreadPoolQueueCapacity = clientRequestThreadPoolQueueCapacity;
    }

    public int getDefaultThreadPoolQueueCapacity() {
        return defaultThreadPoolQueueCapacity;
    }

    public void setDefaultThreadPoolQueueCapacity(final int defaultThreadPoolQueueCapacity) {
        this.defaultThreadPoolQueueCapacity = defaultThreadPoolQueueCapacity;
    }

    public long getScanNotActiveBrokerInterval() {
        return scanNotActiveBrokerInterval;
    }

    public void setScanNotActiveBrokerInterval(long scanNotActiveBrokerInterval) {
        this.scanNotActiveBrokerInterval = scanNotActiveBrokerInterval;
    }

    public int getUnRegisterBrokerQueueCapacity() {
        return unRegisterBrokerQueueCapacity;
    }

    public void setUnRegisterBrokerQueueCapacity(final int unRegisterBrokerQueueCapacity) {
        this.unRegisterBrokerQueueCapacity = unRegisterBrokerQueueCapacity;
    }

    public boolean isSupportActingMaster() {
        return supportActingMaster;
    }

    public void setSupportActingMaster(final boolean supportActingMaster) {
        this.supportActingMaster = supportActingMaster;
    }

    public boolean isEnableAllTopicList() {
        return enableAllTopicList;
    }

    public void setEnableAllTopicList(boolean enableAllTopicList) {
        this.enableAllTopicList = enableAllTopicList;
    }

    public boolean isEnableTopicList() {
        return enableTopicList;
    }

    public void setEnableTopicList(boolean enableTopicList) {
        this.enableTopicList = enableTopicList;
    }

    public boolean isNotifyMinBrokerIdChanged() {
        return notifyMinBrokerIdChanged;
    }

    public void setNotifyMinBrokerIdChanged(boolean notifyMinBrokerIdChanged) {
        this.notifyMinBrokerIdChanged = notifyMinBrokerIdChanged;
    }

    public boolean isEnableControllerInNamesrv() {
        return enableControllerInNamesrv;
    }

    public void setEnableControllerInNamesrv(boolean enableControllerInNamesrv) {
        this.enableControllerInNamesrv = enableControllerInNamesrv;
    }
}

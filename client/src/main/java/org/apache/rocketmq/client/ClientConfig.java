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
package org.apache.rocketmq.client;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.NamespaceUtil;
import org.apache.rocketmq.common.utils.NameServerAddressUtils;
import org.apache.rocketmq.remoting.common.RemotingUtil;
import org.apache.rocketmq.remoting.netty.TlsSystemConfig;
import org.apache.rocketmq.remoting.protocol.LanguageCode;
import org.apache.rocketmq.remoting.protocol.RequestType;

/**
 * Client Common configuration
 */
public class ClientConfig {
    public static final String SEND_MESSAGE_WITH_VIP_CHANNEL_PROPERTY = "com.rocketmq.sendMessageWithVIPChannel";
    public static final String DECODE_READ_BODY = "com.rocketmq.read.body";
    public static final String DECODE_DECOMPRESS_BODY = "com.rocketmq.decompress.body";
    /**
     * NameServer 的地址列表
     */
    private String namesrvAddr = NameServerAddressUtils.getNameServerAddresses();
    /**
     * 客户端IP
     */
    private String clientIP = RemotingUtil.getLocalAddress();
    /**
     * 客户端实例名称，是客户端标识 CID 的组成部分
     */
    private String instanceName = System.getProperty("rocketmq.client.name", "DEFAULT");
    /**
     * 客户端 public 回调的线程池线程数量，默认为 CPU 核数，不建议改变该值
     */
    private int clientCallbackExecutorThreads = Runtime.getRuntime().availableProcessors();
    /**
     * 客户端命名空间，从 4.5.1 版本被引入
     */
    protected String namespace;
    /**
     * 是否初始化namespace，默认为false
     */
    private boolean namespaceInitialized = false;
    /**
     * 访问渠道：本地或云
     */
    protected AccessChannel accessChannel = AccessChannel.LOCAL;

    /**
     * Pulling topic information interval from the named server
     * 从namesever获取topic的时间间隔，默认值 30s，就是 Producer、Consumer 会每隔 30s 向 NameServer 更新 Topic 的路由信息，该值不建议修改。
     */
    private int pollNameServerInterval = 1000 * 30;
    /**
     * Heartbeat interval in microseconds with message broker
     * 客户端向 Broker 发送心跳包的时间间隔，默认为 30s，该值不建议修改。
     */
    private int heartbeatBrokerInterval = 1000 * 30;
    /**
     * Offset persistent interval for consumer
     * 客户端持久化消息消费进度的间隔，默认为 5s，该值不建议修改。
     */
    private int persistConsumerOffsetInterval = 1000 * 5;
    /**
     * 当出现异常时重新拉取的延时
     */
    private long pullTimeDelayMillsWhenException = 1000;
    private boolean unitMode = false;
    /**
     * 定义一个单元，主要用途：客户端 CID 的组成部分；如果获取 NameServer 的地址是通过 URL 进行动态更新的话，会将该值附加到当中，即可以区分不同的获取 NameServer 地址的服务
     */
    private String unitName;
    private boolean decodeReadBody = Boolean.parseBoolean(System.getProperty(DECODE_READ_BODY, "true"));
    private boolean decodeDecompressBody = Boolean.parseBoolean(System.getProperty(DECODE_DECOMPRESS_BODY, "true"));
    private boolean vipChannelEnabled = Boolean.parseBoolean(System.getProperty(SEND_MESSAGE_WITH_VIP_CHANNEL_PROPERTY, "false"));

    private boolean useTLS = TlsSystemConfig.tlsEnable;

    private int mqClientApiTimeout = 3 * 1000;

    private LanguageCode language = LanguageCode.JAVA;

    /**
     * Enable stream request type will inject a RPCHook to add corresponding request type to remoting layer.
     * And it will also generate a different client id to prevent unexpected reuses of MQClientInstance.
     */
    protected boolean enableStreamRequestType = false;

    public String buildMQClientId() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClientIP());

        sb.append("@");
        sb.append(this.getInstanceName());
        if (!UtilAll.isBlank(this.unitName)) {
            sb.append("@");
            sb.append(this.unitName);
        }

        if (enableStreamRequestType) {
            sb.append("@");
            sb.append(RequestType.STREAM);
        }

        return sb.toString();
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public void changeInstanceNameToPID() {
        if (this.instanceName.equals("DEFAULT")) {
            this.instanceName = UtilAll.getPid() + "#" + System.nanoTime();
        }
    }

    public String withNamespace(String resource) {
        return NamespaceUtil.wrapNamespace(this.getNamespace(), resource);
    }

    public Set<String> withNamespace(Set<String> resourceSet) {
        Set<String> resourceWithNamespace = new HashSet<String>();
        for (String resource : resourceSet) {
            resourceWithNamespace.add(withNamespace(resource));
        }
        return resourceWithNamespace;
    }

    public String withoutNamespace(String resource) {
        return NamespaceUtil.withoutNamespace(resource, this.getNamespace());
    }

    public Set<String> withoutNamespace(Set<String> resourceSet) {
        Set<String> resourceWithoutNamespace = new HashSet<String>();
        for (String resource : resourceSet) {
            resourceWithoutNamespace.add(withoutNamespace(resource));
        }
        return resourceWithoutNamespace;
    }

    public MessageQueue queueWithNamespace(MessageQueue queue) {
        if (StringUtils.isEmpty(this.getNamespace())) {
            return queue;
        }
        return new MessageQueue(withNamespace(queue.getTopic()), queue.getBrokerName(), queue.getQueueId());
    }

    public Collection<MessageQueue> queuesWithNamespace(Collection<MessageQueue> queues) {
        if (StringUtils.isEmpty(this.getNamespace())) {
            return queues;
        }
        Iterator<MessageQueue> iter = queues.iterator();
        while (iter.hasNext()) {
            MessageQueue queue = iter.next();
            queue.setTopic(withNamespace(queue.getTopic()));
        }
        return queues;
    }

    public void resetClientConfig(final ClientConfig cc) {
        this.namesrvAddr = cc.namesrvAddr;
        this.clientIP = cc.clientIP;
        this.instanceName = cc.instanceName;
        this.clientCallbackExecutorThreads = cc.clientCallbackExecutorThreads;
        this.pollNameServerInterval = cc.pollNameServerInterval;
        this.heartbeatBrokerInterval = cc.heartbeatBrokerInterval;
        this.persistConsumerOffsetInterval = cc.persistConsumerOffsetInterval;
        this.pullTimeDelayMillsWhenException = cc.pullTimeDelayMillsWhenException;
        this.unitMode = cc.unitMode;
        this.unitName = cc.unitName;
        this.vipChannelEnabled = cc.vipChannelEnabled;
        this.useTLS = cc.useTLS;
        this.namespace = cc.namespace;
        this.language = cc.language;
        this.mqClientApiTimeout = cc.mqClientApiTimeout;
        this.decodeReadBody = cc.decodeReadBody;
        this.decodeDecompressBody = cc.decodeDecompressBody;
        this.enableStreamRequestType = cc.enableStreamRequestType;
    }

    public ClientConfig cloneClientConfig() {
        ClientConfig cc = new ClientConfig();
        cc.namesrvAddr = namesrvAddr;
        cc.clientIP = clientIP;
        cc.instanceName = instanceName;
        cc.clientCallbackExecutorThreads = clientCallbackExecutorThreads;
        cc.pollNameServerInterval = pollNameServerInterval;
        cc.heartbeatBrokerInterval = heartbeatBrokerInterval;
        cc.persistConsumerOffsetInterval = persistConsumerOffsetInterval;
        cc.pullTimeDelayMillsWhenException = pullTimeDelayMillsWhenException;
        cc.unitMode = unitMode;
        cc.unitName = unitName;
        cc.vipChannelEnabled = vipChannelEnabled;
        cc.useTLS = useTLS;
        cc.namespace = namespace;
        cc.language = language;
        cc.mqClientApiTimeout = mqClientApiTimeout;
        cc.decodeReadBody = decodeReadBody;
        cc.decodeDecompressBody = decodeDecompressBody;
        cc.enableStreamRequestType = enableStreamRequestType;
        return cc;
    }

    public String getNamesrvAddr() {
        if (StringUtils.isNotEmpty(namesrvAddr) && NameServerAddressUtils.NAMESRV_ENDPOINT_PATTERN.matcher(namesrvAddr.trim()).matches()) {
            return NameServerAddressUtils.getNameSrvAddrFromNamesrvEndpoint(namesrvAddr);
        }
        return namesrvAddr;
    }

    /**
     * Domain name mode access way does not support the delimiter(;), and only one domain name can be set.
     *
     * @param namesrvAddr name server address
     */
    public void setNamesrvAddr(String namesrvAddr) {
        this.namesrvAddr = namesrvAddr;
        this.namespaceInitialized = false;
    }

    public int getClientCallbackExecutorThreads() {
        return clientCallbackExecutorThreads;
    }

    public void setClientCallbackExecutorThreads(int clientCallbackExecutorThreads) {
        this.clientCallbackExecutorThreads = clientCallbackExecutorThreads;
    }

    public int getPollNameServerInterval() {
        return pollNameServerInterval;
    }

    public void setPollNameServerInterval(int pollNameServerInterval) {
        this.pollNameServerInterval = pollNameServerInterval;
    }

    public int getHeartbeatBrokerInterval() {
        return heartbeatBrokerInterval;
    }

    public void setHeartbeatBrokerInterval(int heartbeatBrokerInterval) {
        this.heartbeatBrokerInterval = heartbeatBrokerInterval;
    }

    public int getPersistConsumerOffsetInterval() {
        return persistConsumerOffsetInterval;
    }

    public void setPersistConsumerOffsetInterval(int persistConsumerOffsetInterval) {
        this.persistConsumerOffsetInterval = persistConsumerOffsetInterval;
    }

    public long getPullTimeDelayMillsWhenException() {
        return pullTimeDelayMillsWhenException;
    }

    public void setPullTimeDelayMillsWhenException(long pullTimeDelayMillsWhenException) {
        this.pullTimeDelayMillsWhenException = pullTimeDelayMillsWhenException;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public boolean isUnitMode() {
        return unitMode;
    }

    public void setUnitMode(boolean unitMode) {
        this.unitMode = unitMode;
    }

    public boolean isVipChannelEnabled() {
        return vipChannelEnabled;
    }

    public void setVipChannelEnabled(final boolean vipChannelEnabled) {
        this.vipChannelEnabled = vipChannelEnabled;
    }

    public boolean isUseTLS() {
        return useTLS;
    }

    public void setUseTLS(boolean useTLS) {
        this.useTLS = useTLS;
    }

    public LanguageCode getLanguage() {
        return language;
    }

    public void setLanguage(LanguageCode language) {
        this.language = language;
    }

    public boolean isDecodeReadBody() {
        return decodeReadBody;
    }

    public void setDecodeReadBody(boolean decodeReadBody) {
        this.decodeReadBody = decodeReadBody;
    }

    public boolean isDecodeDecompressBody() {
        return decodeDecompressBody;
    }

    public void setDecodeDecompressBody(boolean decodeDecompressBody) {
        this.decodeDecompressBody = decodeDecompressBody;
    }

    public String getNamespace() {
        if (namespaceInitialized) {
            return namespace;
        }

        if (StringUtils.isNotEmpty(namespace)) {
            return namespace;
        }

        if (StringUtils.isNotEmpty(this.namesrvAddr)) {
            if (NameServerAddressUtils.validateInstanceEndpoint(namesrvAddr)) {
                namespace = NameServerAddressUtils.parseInstanceIdFromEndpoint(namesrvAddr);
            }
        }
        namespaceInitialized = true;
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
        this.namespaceInitialized = true;
    }

    public AccessChannel getAccessChannel() {
        return this.accessChannel;
    }

    public void setAccessChannel(AccessChannel accessChannel) {
        this.accessChannel = accessChannel;
    }

    public int getMqClientApiTimeout() {
        return mqClientApiTimeout;
    }

    public void setMqClientApiTimeout(int mqClientApiTimeout) {
        this.mqClientApiTimeout = mqClientApiTimeout;
    }

    public boolean isEnableStreamRequestType() {
        return enableStreamRequestType;
    }

    public void setEnableStreamRequestType(boolean enableStreamRequestType) {
        this.enableStreamRequestType = enableStreamRequestType;
    }

    @Override
    public String toString() {
        return "ClientConfig [namesrvAddr=" + namesrvAddr + ", clientIP=" + clientIP + ", instanceName=" + instanceName
            + ", clientCallbackExecutorThreads=" + clientCallbackExecutorThreads + ", pollNameServerInterval=" + pollNameServerInterval
            + ", heartbeatBrokerInterval=" + heartbeatBrokerInterval + ", persistConsumerOffsetInterval=" + persistConsumerOffsetInterval
            + ", pullTimeDelayMillsWhenException=" + pullTimeDelayMillsWhenException + ", unitMode=" + unitMode + ", unitName=" + unitName + ", vipChannelEnabled="
            + vipChannelEnabled + ", useTLS=" + useTLS + ", language=" + language.name() + ", namespace=" + namespace + ", mqClientApiTimeout=" + mqClientApiTimeout
            + ", decodeReadBody=" + decodeReadBody + ", decodeDecompressBody=" + decodeDecompressBody
            + ", enableStreamRequestType=" + enableStreamRequestType + "]";
    }
}

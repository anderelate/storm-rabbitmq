/*
 * Copyright 2017 Andrey Burov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.burov4j.storm.rabbitmq;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 *
 * @author Andrey Burov
 */
public class RabbitMqChannelFactoryTest extends RabbitMqTest {

    @Test
    public void createConnectionFactory() {
        int requestedHeartbeat = 121,
                port = 8889;
        String host = "testHost",
                password = "testPassword",
                username = "testUsername",
                virtualHost = "testVirtualHost";
        RabbitMqConfig rabbitMqConfig = new RabbitMqConfigBuilder()
                .setRequestedHeartbeat(requestedHeartbeat)
                .setHost(host)
                .setPassword(password)
                .setPort(port)
                .setUsername(username)
                .setVirtualHost(virtualHost)
                .build();
        RabbitMqChannelFactory rabbitMqChannelFactory = new RabbitMqChannelFactory(rabbitMqConfig);
        ConnectionFactory connectionFactory = rabbitMqChannelFactory.createConnectionFactory();
        assertEquals(requestedHeartbeat, connectionFactory.getRequestedHeartbeat());
        assertEquals(port, connectionFactory.getPort());
        assertEquals(host, connectionFactory.getHost());
        assertEquals(password, connectionFactory.getPassword());
        assertEquals(username, connectionFactory.getUsername());
        assertEquals(virtualHost, connectionFactory.getVirtualHost());
    }

    @Test
    public void prepareWithAddresses() throws IOException, TimeoutException {
        String addresses = "10.189.21.119:8080,10.189.21.118:8181";
        RabbitMqConfig rabbitMqConfig = new RabbitMqConfigBuilder()
                .setAddresses(addresses)
                .build();
        RabbitMqChannelFactory rabbitMqChannelFactory = Mockito.spy(new RabbitMqChannelFactory(rabbitMqConfig));
        doReturn(mockConnectionFactory).when(rabbitMqChannelFactory).createConnectionFactory();
        rabbitMqChannelFactory.prepare();
        verify(mockConnectionFactory, times(1)).newConnection(Address.parseAddresses(addresses));
    }

    @Test
    public void cleanupWithoutPrepare() throws Exception {
        RabbitMqChannelFactory rabbitMqChannelFactory = new RabbitMqChannelFactory();
        rabbitMqChannelFactory.cleanup();
    }

    @Test
    public void prepareAndUse() throws Exception {
        RabbitMqChannelFactory rabbitMqChannelFactory = Mockito.spy(RabbitMqChannelFactory.class);
        doReturn(mockConnectionFactory).when(rabbitMqChannelFactory).createConnectionFactory();
        rabbitMqChannelFactory.prepare();
        rabbitMqChannelFactory.createChannel();
        rabbitMqChannelFactory.cleanup();
    }

    @Test
    public void closedConnectionPrepare() throws Exception {
        RabbitMqChannelFactory rabbitMqChannelFactory = Mockito.spy(RabbitMqChannelFactory.class);
        doReturn(mockConnectionFactory).when(rabbitMqChannelFactory).createConnectionFactory();
        doReturn(true).when(mockConnection).isOpen();
        rabbitMqChannelFactory.prepare();
        rabbitMqChannelFactory.prepare();

        verify(mockConnection, times(1)).isOpen();
        verify(mockConnectionFactory, times(1)).newConnection();
    }

    @Test
    public void equals() {
        RabbitMqChannelFactory factory1 = new RabbitMqChannelFactory(),
                factory2 = new RabbitMqChannelFactory();
        assertEquals(factory1, factory2);
    }

    @Test
    public void notEquals() {
        Map<String, Object> rabbitMqConf = Collections.singletonMap(RabbitMqConfig.KEY_HOST, "anotherHost");
        RabbitMqConfig rabbitMqConfig = new RabbitMqConfig(rabbitMqConf);
        RabbitMqChannelFactory factory1 = new RabbitMqChannelFactory(),
                factory2 = new RabbitMqChannelFactory(rabbitMqConfig);
        assertNotEquals(factory1, factory2);
    }

    @Test
    public void hashCodeEquals() {
        RabbitMqChannelFactory factory1 = new RabbitMqChannelFactory(),
                factory2 = new RabbitMqChannelFactory();
        assertEquals(factory1.hashCode(), factory2.hashCode());
    }

    @Test
    public void hashCodeNotEquals() {
        Map<String, Object> rabbitMqConf = Collections.singletonMap(RabbitMqConfig.KEY_HOST, "anotherHost");
        RabbitMqConfig rabbitMqConfig = new RabbitMqConfig(rabbitMqConf);
        RabbitMqChannelFactory factory1 = new RabbitMqChannelFactory(),
                factory2 = new RabbitMqChannelFactory(rabbitMqConfig);
        assertNotEquals(factory1.hashCode(), factory2.hashCode());
    }

    @Test
    public void withStormConfig() {
        Map<String, Object> stormConf = Collections.singletonMap(RabbitMqConfig.KEY_USERNAME, "withStormConfig test user");
        RabbitMqChannelFactory factory1 = RabbitMqChannelFactory.withStormConfig(stormConf),
                factory2 = RabbitMqChannelFactory.withStormConfig(stormConf);
        assertSame(factory1, factory2);
    }
}

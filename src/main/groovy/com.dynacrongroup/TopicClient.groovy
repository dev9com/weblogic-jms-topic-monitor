package com.dynacrongroup

import org.apache.commons.lang3.time.StopWatch

import javax.jms.MessageListener
import javax.jms.Session
import javax.jms.Topic
import javax.jms.TopicConnection
import javax.jms.TopicConnectionFactory
import javax.jms.TopicSession
import javax.jms.TopicSubscriber
import javax.naming.Context
import javax.naming.InitialContext

class TopicClient {

    SettingsConf conf = new SettingsConf().getSoleInstance()
    TopicConnection connection
    TopicSession session
    TopicSubscriber subscriber
    StopWatch stopWatch = new StopWatch()
    def final REFRESH_INTERVAL_IN_SECONDS = 1200

    def run() {

        System.out.println()
        System.out.println(String.format("Destination: %s", conf.destination))
        System.out.println(String.format("URL: %s", conf.url))
        System.out.println(String.format("Connection Factory: %s", conf.connectionFactory))
        System.out.println(String.format("Initial Context Factory: %s", conf.initialContextFactory))
        System.out.println(String.format("Keystore Path: %s", conf.keystorePath))
        System.out.println()

        this.startMonitoringJmsTopic()
        System.out.println("****Now monitoring JMS topic****")

        stopWatch.reset()
        stopWatch.start()
        while (true) {
            if (stopWatch.getTime() > REFRESH_INTERVAL_IN_SECONDS * 1000) {
                stopWatch.stop()
                stopWatch.reset()
                stopWatch.start()
                System.out.println("Refreshing JMS topic connection")
                this.closeConnections()
                this.startMonitoringJmsTopic()
            }
        }
    }

    def startMonitoringJmsTopic() {
        System.setProperty("javax.net.ssl.trustStore", conf.keystorePath)
        System.setProperty("javax.net.ssl.trustStorePassword", conf.keystorePass)
        Properties props = new Properties()
        props.put(Context.INITIAL_CONTEXT_FACTORY, conf.initialContextFactory)
        props.put(Context.PROVIDER_URL, conf.url)
        InitialContext initContext = new InitialContext(props)

        TopicConnectionFactory factory =
            (TopicConnectionFactory) initContext.lookup(conf.connectionFactory)
        Topic topic = (Topic) initContext.lookup(conf.destination)
        initContext.close()
        connection = factory.createTopicConnection()
        session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE)
        subscriber = session.createSubscriber(topic)
        connection.start()
        MessageListener listener = new TopicMessageListener();
        subscriber.setMessageListener(listener);
    }

    def closeConnections() {
        subscriber.close()
        session.close()
        connection.close()
    }
}

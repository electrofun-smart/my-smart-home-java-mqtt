/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * The Eclipse Public License is available at
 * https://www.eclipse.org/legal/epl-2.0
 * and the Eclipse Distribution License is available at
 * https://www.eclipse.org/org/documents/edl-v10.php
 * Contributors:
 * Dave Locke - initial API and implementation and/or initial documentation
 */

package com.example;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.actions.api.smarthome.SmartHomeApp;
import com.google.auth.oauth2.GoogleCredentials;

/**
 * A sample application that demonstrates how to use the Paho MQTT v3.1 Client blocking API.
 * It can be run from the command line in one of two modes:
 * - as a publisher, sending a single message to a topic on the server
 * - as a subscriber, listening for messages from the server
 * There are three versions of the sample that implement the same features
 * but do so using using different programming styles:
 * <ol>
 * <li>Sample (this one) which uses the API which blocks until the operation completes</li>
 * <li>SampleAsyncWait shows how to use the asynchronous API with waiters that block until
 * an action completes</li>
 * <li>SampleAsyncCallBack shows how to use the asynchronous API where events are
 * used to notify the application when an action completes
 * <li>
 * </ol>
 * If the application is run with the -h parameter then info is displayed that
 * describes all of the options / parameters.
 */
public class MyMqtt implements MqttCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(MySmartHomeApp.class);
    private final SmartHomeApp actionsApp = new MySmartHomeApp();
    private String mqttuser, mqttpwd, mqttbroker, mqttclientid, mqttcleansession, mqttquietmode;
    InputStream inputStream;

    {
        try {
            GoogleCredentials credentials =
                    GoogleCredentials.fromStream(getClass().getResourceAsStream("/smart-home-key.json"));
            actionsApp.setCredentials(credentials);
        } catch (Exception e) {
            LOGGER.error("couldn't load credentials");
        }
    }
    /**
     * This is just for test purpose, just to verify connectivity
     * with your mqtt broker
     */

    public static void main(String args[]) throws Exception {

        InputStreamReader r = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(r);
        String name = "";

        MyMqtt myMqttClient = new MyMqtt();
        myMqttClient.subscribe("hello", 0);
        while (!name.equals("stop")) {
            System.out.println("Enter data: ");
            name = br.readLine();
            System.out.println("data is: " + name);
            mqttPublish(myMqttClient, name);
        }

        br.close();
        r.close();
    }

    public static void mqttPublish(MyMqtt myMqttClient, String msg) {

        // Default settings:
        String action = "publish";
        String topic = "hello";
        int qos = 0;

        // With a valid set of arguments, the real work of
        // driving the client API can begin
        try {
            // Create an instance of this class

            // Perform the requested action
            if (action.equals("publish")) {
                myMqttClient.publish(topic, qos, msg.getBytes());
            } else if (action.equals("subscribe")) {
                myMqttClient.subscribe(topic, qos);
            }
        } catch (MqttException me) {
            // Display full details of any exception that occurs
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    } // end publish

    // Private instance variables
    private MqttClient client;
    private String brokerUrl;
    private boolean quietMode;
    private MqttConnectOptions conOpt;
    private boolean clean;
    private String password;
    private String userName;

    /**
     * Constructs an instance of the sample client wrapper
     *
     */
    public MyMqtt()
            throws MqttException, IOException {

        try {
            Properties prop = new Properties();
            String propFileName = "mqtt.properties";

            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

            // get the property value and print it out
            mqttbroker = prop.getProperty("broker");
            mqttclientid = prop.getProperty("clientid");
            mqttcleansession = prop.getProperty("cleansession");
            mqttquietmode = prop.getProperty("quietmode");
            mqttuser = prop.getProperty("user");
            mqttpwd = prop.getProperty("pwd");

        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            inputStream.close();
        }

        this.brokerUrl = mqttbroker;
        this.quietMode = Boolean.valueOf(mqttquietmode);
        clean = Boolean.valueOf(mqttcleansession);
        this.password = mqttpwd;
        this.userName = mqttuser;
        // This sample stores in a temporary directory... where messages temporarily
        // stored until the message has been delivered to the server.
        // ..a real application ought to store them somewhere
        // where they are not likely to get deleted or tampered with
        String tmpDir = System.getProperty("java.io.tmpdir");
        MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);

        try {
            // Construct the connection options object that contains connection parameters
            // such as cleanSession and LWT
            conOpt = new MqttConnectOptions();
            conOpt.setCleanSession(clean);
            if (password != null) {
                conOpt.setPassword(this.password.toCharArray());
            }
            if (userName != null) {
                conOpt.setUserName(this.userName);
            }

            // Construct an MQTT blocking mode client
            client = new MqttClient(this.brokerUrl, mqttclientid, dataStore);

            // Set this wrapper as the callback handler
            client.setCallback(this);

        } catch (MqttException e) {
            e.printStackTrace();
            log("Unable to set up client: " + e.toString());
            System.exit(1);
        }
    }

    /**
     * Publish / send a message to an MQTT server
     * 
     * @param topicName the name of the topic to publish to
     * @param qos the quality of service to delivery the message at (0,1,2)
     * @param payload the set of bytes to send to the MQTT server
     * @throws MqttException
     */
    public void publish(String topicName, int qos, byte[] payload) throws MqttException {

        // Connect to the MQTT server
        log("Connecting to " + brokerUrl + " with client ID " + client.getClientId());
        if (!client.isConnected()) {
            client.connect(conOpt);
            log("Connected first time");
        } else {
            log("Already Connected");
        }

        String time = new Timestamp(System.currentTimeMillis()).toString();
        log("Publishing at: " + time + " to topic \"" + topicName + "\" qos " + qos);

        // Create and configure a message
        MqttMessage message = new MqttMessage(payload);
        message.setQos(qos);

        // Send the message to the server, control is not returned until
        // it has been delivered to the server meeting the specified
        // quality of service.
        client.publish(topicName, message);

        // Disconnect the client
        // client.disconnect();
        // log("Disconnected");
    }

    /**
     * Subscribe to a topic on an MQTT server
     * Once subscribed this method waits for the messages to arrive from the server
     * that match the subscription. It continues listening for messages until the enter key is
     * pressed.
     * 
     * @param topicName to subscribe to (can be wild carded)
     * @param qos the maximum quality of service to receive messages at for this subscription
     * @throws MqttException
     */
    public void subscribe(String topicName, int qos) throws MqttException {

        // Connect to the MQTT server
        client.connect(conOpt);
        log("Connected to " + brokerUrl + " with client ID " + client.getClientId());

        // Subscribe to the requested topic
        // The QoS specified is the maximum level that messages will be sent to the client at.
        // For instance if QoS 1 is specified, any messages originally published at QoS 2 will
        // be downgraded to 1 when delivering to the client but messages published at 1 and 0
        // will be received at the same level they were published at.
        log("Subscribing to topic \"" + topicName + "\" qos " + qos);
        client.subscribe(topicName, qos);

        // Disconnect the client from the server
        // client.disconnect();
        // log("Disconnected");
    }

    /**
     * Utility method to handle logging. If 'quietMode' is set, this method does nothing
     * 
     * @param message the message to log
     */
    private void log(String message) {
        if (!quietMode) {
            System.out.println(message);
        }
    }

    /****************************************************************/
    /* Methods to implement the MqttCallback interface */
    /****************************************************************/

    /**
     * @see MqttCallback#connectionLost(Throwable)
     */
    @Override
    public void connectionLost(Throwable cause) {
        // Called when the connection to the server has been lost.
        // An application may choose to implement reconnection
        // logic at this point. This sample simply exits.
        log("Connection to " + brokerUrl + " lost!" + cause);
        System.exit(1);
    }

    /**
     * @see MqttCallback#deliveryComplete(IMqttDeliveryToken)
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Called when a message has been delivered to the
        // server. The token passed in here is the same one
        // that was passed to or returned from the original call to publish.
        // This allows applications to perform asynchronous
        // delivery without blocking until delivery completes.
        //
        // This sample demonstrates asynchronous deliver and
        // uses the token.waitForCompletion() call in the main thread which
        // blocks until the delivery has completed.
        // Additionally the deliveryComplete method will be called if
        // the callback is set on the client
        //
        // If the connection to the server breaks before delivery has completed
        // delivery of a message will complete after the client has re-connected.
        // The getPendingTokens method will provide tokens for any messages
        // that are still to be delivered.
    }

    /**
     * @see MqttCallback#messageArrived(String, MqttMessage)
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws MqttException {
        // Called when a message arrives from the server that matches any
        // subscription made by the client
        String time = new Timestamp(System.currentTimeMillis()).toString();
        System.out.println("Time:\t" + time +
                "  Topic:\t" + topic +
                "  Message:\t" + new String(message.getPayload()) +
                "  QoS:\t" + message.getQos());

    }

    /****************************************************************/
    /* End of MqttCallback methods */
    /****************************************************************/

}

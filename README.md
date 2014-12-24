# Simple JMS 2.0 Sample 

Inspired from a post written by: _arungupta on Dec 26, 2012_

## @JMSDestinationDefinition and Synchronous Message Receive: (TOTD #191) 

[source](https://blogs.oracle.com/arungupta/entry/simple_jms_2_0_sample)

JMS 2.0 Early Draft - Simplified API Sample Code explained some of the changes that are made in JMS 2.0 to "catch up" with all the changes in the Java SE and EE platform over the past few years. The main goals of JMS 2.0 are:

    Changes to improve ease-of-development
    Clarification of relationship between JMS and other Java EE specifications
    Definition of a new mandatory API to allow any JMS provider to be integrated with any other Java EE Application Server
    Other enhancements as requested by the community

This Tip Of The Day (TOTD) will explain a simple sample showing how to send a message and receive it synchronously. The complete source code for the sample can be downloaded here.

This is a Stateless EJB that has a single method to send a message.

	@Stateless
	public class MessageSender {
	
	 @Inject
	 JMSContext context;
	 
	 @Resource(mappedName="java:global/jms/myQueue")
	 Queue queue;
	
	 public void sendMessage(String message) {
	 context.createProducer().send(queue, message);
	 }
	}

In this code:

- JMSContext is a new interface introduced in the simplified API in JMS 2.0. This combines in a single object the functionality of two separate objects from the JMS 1.1 API: a Connection and a Session.

- A JMSContext may be injected in the Java EE web and EJB containers using the @Inject annotation. A JMSContext created in this way is described as being container-managed. A container-managed JMSContext will be closed automatically by the container. Applications running in the Java EE web and EJB containers are not permitted to create more than one active session on a connection. This allows to combine them in a single object offering a simpler API. This is the recommended way for creating JMS context in Java EE applications.

- A JMSContext may be created by calling one of the createContext methods on a ConnectionFactory. A JMSContext that is created this way is described as being application-managed. An application-managed JMSContext must be closed when no longer needed by calling its close method. Applications running in a Java SE environment or in the Java EE application client container are permitted to create multiple active sessions on the same connection. This allows the same physical connection to be used in multiple threads simultaneously. The createContext method is recommended to create JMSContext in such applications.

The Java EE Platform requires a preconfigured JMS ConnectionFactory accessible to the application under the JNDI name:

	java:comp/DefaultJMSConnectionFactory

The annotation @JMSConnectionFactory may be used to specify the JNDI lookup name of the ConnectionFactory used to create the JMSContext as:

    @Inject
    @JMSConnectionFactory("java:comp/DefaultJMSConnectionFactory")
    JMSContext context;

If no lookup name is specified or the @JMSConnectionFactory is omitted then the platform default JMS connection factory will be used. The above code fragment is equivalent to:

    @Inject
    JMSContext context;

- @Resource defines a dependency on a resource needed by the application. In this case, it specifies the JNDI name of the destination to which the message needs to be sent.
When an application needs to send messages it uses the createProducer method to create JMSProducer which provides methods to configure and send messages. The various setProperty methods are used to set property on the message being sent. There are several other methods like setPriority, setDeliveryDelay, and setTimeToLive to set other quality-of-service attributes for the message being sent.

Messages may be sent synchronously or asynchronously. Synchronous messages are sent using one of the send methods. Asynchronous messages are sent using setAsync method and assigning a CompletionListener. When the message has been successfully sent the JMS provider invokes the callback method onCompletion on the CompletionListener object. If the message is not sent for some reason or an acknowledgement from the remote JMS server is not received then onException callback method is called. An asynchronous send is not permitted in a Java EE application.


This is a Stateless EJB that has a single method to receive the message synchronously.

	@Stateless
	public class MessageReceiverSync {
	
		@Inject
		private JMSContext context;
		
		@Resource(mappedName="java:global/jms/myQueue")
		Queue myQueue;
		
		public String receiveMessage() {
		    String message = context.createConsumer(myQueue).receiveBody(String.class, 1000);
		    return "Received " + message;
		}
    }


In this code:

- JMSContext referring to the preconfigured JMS ConnectionFactory is injected by the container.
- @Resource defines a dependency on the JMS destination on which the message is received.

When an application needs to receive messages it uses one of the several createConsumer or createDurableConsumer methods to create a JMSConsumer. createConsumer creates a non-durable subscription on the specified destination. This means that a client will only see the messages published on the destination when the subscriber is active. If the subscriber is not active, it is missing messages published on the destination. createDurableConsumer creates an unshared durable subscription of a specified topic and creates a consumer on that subscription. This allows the subscriber will receive all messages published on a topic, including the ones published when there is no active consumer associated with it. The JMS provider retains a record of this durable subscription and ensures that all messages from the topic's publishers are retained until they are delivered to, and acknowledged by, a consumer on this durable subscription or until they have expired.

A JMSConsumer provides methods to receive messages either synchronously or asynchronously. receive methods are used for synchronous delivery of the messages. A MessageListener object may be registered with the client for asynchronous delivery of the messages. onMessage method of the MessageListener object are called as messages are received. Asynchronous delivery of messages will not work until MQ-264 is fixed.

Next is a Servlet that ties all the pieces together. It defines the JMS Destination and send and receive the message using the previously defined EJBs.

		@JMSDestinationDefinition(name = "java:global/jms/myQueue",
		    resourceAdapterName = "jmsra",
		    className = "javax.jms.Queue",
		    destinationName="queue1234",
		    description="My Queue")
		@WebServlet(urlPatterns = {"/TestServlet"})
		
		public class TestServlet extends HttpServlet {
		 
		    @EJB MessageSender sender;
		    @EJB MessageReceiverSync receiver;
				
		    void doGet(HttpServletRequest request, HttpServletResponse response) {
		 		...
				String m = "Hello there";
				sender.sendMessage(m);
				out.format("Message sent: %1$s.<br>", m);
				out.println("Receiving message...<br>");
				String message = receiver.receiveMessage();
				out.println("Message received: " + message);
				...
		    }

In this code:

- @JMSDestinationDefinition defines the JMS destination. The name attribute defines the JNDI name of the destination being defined, destinationName attribute defines the name of the queue or topic, and className attribute defines the JMS destination implementation class name.
    
- doGet method uses the injected EJBs to send and receive the message.

The complete source code for the sample can be downloaded here. "mvn package" and deploy the generated WAR file.
Download GlassFish 4 build 68 onwards and try this sample today!
The latest progress on JMS 2.0 can be tracked at:

jms-spec.java.net
JSR 343 EG archive at jsr343-experts
Discussion at users@jms-spec
JSR 343
    Latest Specification
    Latest Javadocs (download, online)

Help us make JMS 2.0 better, simpler, easier to use. Join users@jms-spec and contribute!



		 * asadmin
		 * create-jms-resource --restype javax.jms.ConnectionFactory jms/TestConnectionFactory
		 * create-jms-resource --restype javax.jms.Queue jms/testQueue
		 * list-jms-resources

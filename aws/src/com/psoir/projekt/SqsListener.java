package com.psoir.projekt;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;
import org.joda.time.DateTime;
import com.amazonaws.regions.*;
import java.util.ArrayList;
import java.util.List;

public class SqsListener {


	public static final String DATE = "Date";
	public static final String ITEM = "Item";
	private final ImageEditor imageEditor;
	private final CreateQueueRequest createQueueRequest;
	private final ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
	private final AmazonSQS sqs;


	public SqsListener() {
		try {
			credentialsProvider.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. " +
							"Please make sure that your credentials file is at the correct " +
							"location (~/.aws/credentials), and is in valid format.",
					e);
		}

		sqs = AmazonSQSClientBuilder.standard()
							.withCredentials(credentialsProvider)
							.withRegion(Regions.EU_CENTRAL_1)
							.build();

		createQueueRequest = new CreateQueueRequest("psoir_test_queue");
		this.imageEditor = new ImageEditor(credentialsProvider.getCredentials());


	}

	public void listen() throws InterruptedException {
		while (true) {
			List<Message> messagesFromQueue = getMessagesFromQueue(getQueueUrl());

				if (messagesFromQueue.size() > 0) {

						Message message = messagesFromQueue.get(0);
					try {
						List<ReplaceableAttribute> attributes = new ArrayList<>();
						attributes.add(new ReplaceableAttribute().withName(ITEM).withValue(message.getBody()));
						attributes.add(new ReplaceableAttribute().withName(DATE).withValue(DateTime.now().toString()));
						imageEditor.rotateImage(message.getBody());
					} catch (Exception e)
					{
						e.getCause();
						e.getStackTrace();
						deleteMessageFromQueue(getQueueUrl(), message);
					}
				deleteMessageFromQueue(getQueueUrl(), message);

			} else {
				Thread.sleep(2000);
			}
		}
	}

	private List<Message> getMessagesFromQueue(String queueUrl) {
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
		List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
		return messages;

	}

	private String getQueueUrl() {
		return sqs.createQueue(createQueueRequest).getQueueUrl();
	}

	private void deleteMessageFromQueue(String queueUrl, Message message) {
		String messageRecieptHandle = message.getReceiptHandle();
		System.out.println("Delete message : " + message.getBody());
		sqs.deleteMessage(new DeleteMessageRequest(queueUrl, messageRecieptHandle));

	}

}

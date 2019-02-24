package com.psoir.projekt;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class SqsListener {


	public static final String DATE = "Date";
	private static final String ITEM_NAME = "Item_Name";
	public static final String HTTPS_SQS_EU_CENTRAL_1_AMAZONAWS_COM = "https://sqs.eu-central-1.amazonaws.com/938778484620/arwspsoirsqs";
	public static final String ITEM = "Item";
	private final String queName;
	private final AmazonSQSClient sqs;
	private final ImageEditor imageEditor;

	public SqsListener() {
		AWSCredentials credentials = new BasicAWSCredentials("",
				"");
		this.queName = "arwspsoirsqs";
		this.sqs = new AmazonSQSClient(credentials);
		this.sqs.setEndpoint(HTTPS_SQS_EU_CENTRAL_1_AMAZONAWS_COM);
		this.imageEditor = new ImageEditor(credentials);


	}

	public void listen() throws InterruptedException {
		while (true) {
			List<Message> messagesFromQueue = getMessagesFromQueue(getQueueUrl(this.queName));
			if (messagesFromQueue.size() > 0) {
				Message message = messagesFromQueue.get(0);
				List<ReplaceableAttribute> attributes = new ArrayList<>();
				attributes.add(new ReplaceableAttribute().withName(ITEM).withValue(message.getBody()));
				attributes.add(new ReplaceableAttribute().withName(DATE).withValue(DateTime.now().toString()));
				imageEditor.rotateImage(message.getBody());
				deleteMessageFromQueue(getQueueUrl(this.queName), message);

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

	private String getQueueUrl(String queueName) {
		GetQueueUrlRequest getQueueUrlRequest = new GetQueueUrlRequest(queueName);
		return this.sqs.getQueueUrl(getQueueUrlRequest).getQueueUrl();
	}

	private void deleteMessageFromQueue(String queueUrl, Message message) {
		String messageRecieptHandle = message.getReceiptHandle();
		System.out.println("Delete message : " + message.getBody());
		sqs.deleteMessage(new DeleteMessageRequest(queueUrl, messageRecieptHandle));

	}

}

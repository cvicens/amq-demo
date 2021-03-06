/*
 * Copyright 2016-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.openshift.booster.service;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.streams.KafkaStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Path("/wordcount")
@Component
public class WordcountEndpoint {
	private final static Logger LOGGER = (Logger) LoggerFactory.getLogger(WordcountEndpoint.class);
	
	private Long _count = 0L;
	
	@Autowired
	Producer<Long,String> producer;
	
	@Autowired
	@Qualifier("wordcountConsumer")
	Consumer<String, Long> consumer;
	
	@Autowired
	KafkaStreams streams;
	
	@Value("${kafka.wordcount-in-topic}")
	private String worcountInTopic;
	
	@Value("${kafka.wordcount-out-topic}")
	private String worcountOutTopic;
	
	@PostConstruct
    public void init() {
		System.out.println(">>> init() with topics = " + worcountOutTopic);
    	Runnable runnable = () -> {
    		final int giveUp = 100000; int noRecordsCount = 0;
    		
    		// Subscribe to Topics
    		consumer.subscribe(Collections.singletonList(worcountOutTopic));
    		
            while (true) {
                final ConsumerRecords<String, Long> consumerRecords = consumer.poll(Duration.ofMillis(1000));
                if (consumerRecords.count() == 0) {
                    noRecordsCount++;
                    if (noRecordsCount > giveUp) break;
                    else continue;
                }
                consumerRecords.forEach(record -> {
                    System.out.printf("\n->New message received at Wordcount Out Topic! Consumer Record:(%s, %d, %d, %d)\n",
                            record.key(), record.value(),
                            record.partition(), record.offset());
                });
                consumer.commitAsync();
            }
            consumer.close();
            System.out.println("DONE");
    	};
    	
    	Thread thread = new Thread(runnable);
    	thread.start();
    	
    	// Start streams! 
        streams.start();
    }
	
    @GET
    @Produces("application/json")
    public ResponseEntity<String> greeting(@QueryParam("message") @DefaultValue("A long time ago in a galaxy far, far away....") String message) {    	
        return _sendToTopic(worcountInTopic, message);
    }
    
    private ResponseEntity<String> _sendToTopic(String topicName, String message) {
		ProducerRecord<Long, String> record = new ProducerRecord<>(topicName, _count++, message);

		// Producer
		RecordMetadata metadata = null;
		try {			
			metadata = producer.send(record).get();
			System.out.printf("\nRecord sent to partition %s with offset %s", metadata.partition(), metadata.offset());
		} catch (Exception e) {
			System.out.printf("\nError in sending record", e.getMessage());
			e.printStackTrace();
		} finally {
			producer.flush();
			//producer.close();
		}

		if (metadata != null) {
			return ResponseEntity.ok(String.format("Record sent to partition '%s' with offset '%s'", 
					metadata.partition(),
					metadata.offset()));	
		}
		
		return ResponseEntity.ok(String.format("Error while sending to topic '%s'", topicName));
	}
}

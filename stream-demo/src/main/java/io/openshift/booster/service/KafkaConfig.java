package io.openshift.booster.service;

import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class KafkaConfig {
	
	@Value("${kafka.bootstrap-servers}")
	private String kafkaBrokers;
	
	@Value("${kafka.clientId}")
	private String clientId;

	@Value("${kafka.groupId}")
	private String groupId;

	@Value("${kafka.topic}")
	private String topicName;
	
	@Value("${kafka.wordcount-in-topic}")
	private String worcountInTopic;
	
	@Value("${kafka.wordcount-out-topic}")
	private String worcountOutTopic;

	@Value("${kafka.ssl.truststore.location}")
	private String sslTruststoreLocation;

	@Value("${kafka.ssl.truststore.password}")
	private String sslTruststorePassword;

	@Value("${kafka.ssl.keystore.location}")
	private String sslKeystoreLocation;

	@Value("${kafka.ssl.keystore.password}")
	private String sslKeystorePassword;

	@Bean
	@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public Producer<Long, String> createProducer() {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers);
		//props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
		
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, sslTruststoreLocation);
		props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslTruststorePassword);

		props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, sslKeystoreLocation);
		props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, sslKeystorePassword);

		//props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");

		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");

		
		return new KafkaProducer<>(props);
	}
	
	@Bean(name = "greetingConsumer")
	@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public Consumer<Long, String> createGreetingConsumer() {
		System.out.println("greetingConsumer");
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

		props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, sslTruststoreLocation);
		props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslTruststorePassword);

		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");

		Consumer<Long, String> consumer = new KafkaConsumer<>(props);
				
		return consumer;
	}
	
	@Bean(name = "wordcountConsumer")
	@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public Consumer<String, Long> createWordcountConsumer() {
		System.out.println("wordcountConsumer");
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
		
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

		props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, sslTruststoreLocation);
		props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslTruststorePassword);

		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");

		Consumer<String, Long> consumer = new KafkaConsumer<>(props);
				
		return consumer;
	}
	
	@Bean
	@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public KafkaStreams createStreams() {
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		
		props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, sslTruststoreLocation);
		props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslTruststorePassword);

		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
		
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-application");
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		
		props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 500L);

		StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> textLines = builder.stream(worcountInTopic);
        KTable<String, Long> wordCounts = textLines
            .flatMapValues(textLine -> Arrays.asList(textLine.toLowerCase().split("\\W+")))
            .groupBy((key, word) -> word)
            .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("counts-store"));
        wordCounts.toStream().to(worcountOutTopic, Produced.with(Serdes.String(), Serdes.Long()));
 
		return new KafkaStreams(builder.build(), props);
	}
}
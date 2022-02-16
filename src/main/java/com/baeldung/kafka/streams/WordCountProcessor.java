package com.baeldung.kafka.streams;

import java.util.Arrays;
import java.util.Locale;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class WordCountProcessor {

    private static final Serde<String> STRING_SERDE = Serdes.String();

    @Autowired
    void buildPipeline(StreamsBuilder streamsBuilder) {
        KStream<String, String> messageStream = streamsBuilder
            .stream("input-topic", Consumed.with(STRING_SERDE, STRING_SERDE));



        KTable<String, Long> wordCounts = messageStream.mapValues(a ->{
            log.info("Value={}", a);
            if(!StringUtils.isEmpty(a))
                return a.toLowerCase(Locale.ROOT);
            return "EMPTY";
                        })
            //.mapValues((ValueMapper<String, String>) String::toLowerCase)
            .flatMapValues(value -> Arrays.asList(value.split("\\W+")))
            .groupBy((key, word) -> {
                log.info("key:{},word:{}",key,word);
                return word;
            }, Grouped.with(STRING_SERDE, STRING_SERDE))
            .count(Materialized.as("contador"));

        wordCounts.toStream().foreach((key,value) -> {
            log.info("-----------------RESULT: key:{}, value:{}", key, value);
        });
            wordCounts.toStream().to("output-topic");
    }

}

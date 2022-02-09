package com.baeldung.monitoring;

import com.baeldung.monitoring.service.LagAnalyzerService;
import com.baeldung.monitoring.service.LiveLagAnalyzerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;


@Slf4j
@SpringBootTest(classes = {LagAnalyzerApplication.class})
@Testcontainers
public class LiveLagAnalyzerSeriveLive2Test {


    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withStartupTimeout(Duration.of(600, SECONDS))
            //.withLogConsumer(new Slf4jLogConsumer(log))
            .waitingFor(Wait.forLogMessage(".*Started.*", 1));

    @SpyBean
    LiveLagAnalyzerService liveLagAnalyzerService;

    @SpyBean
    LagAnalyzerService lagAnalyzerService;



    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("monitor.kafka.bootstrap.config" , kafka::getBootstrapServers);

    }
    @Test
    public void test() throws ExecutionException, InterruptedException {
        Map<TopicPartition, Long> lag = new HashMap<>();
        lag.put(new TopicPartition("test", 0), 1L);
        //log.error(lagAnalyzerService.analyzeLag("baeldung").toString());
        ResultCaptor<Map<TopicPartition, Long>> resultCaptor = new ResultCaptor<>();

        //verify(lagAnalyzerService, atLeast(2)).analyzeLag(anyString())
      log.info(kafka.getBootstrapServers());
        await()
                .atMost(Duration.of(12,SECONDS))
                .untilAsserted(() -> verify(liveLagAnalyzerService, atLeast(2)).liveLagAnalysis());
        await()
                .atMost(Duration.of(12,SECONDS))
                .untilAsserted(() -> {
                    verify(lagAnalyzerService, atLeast(2)).analyzeLag(anyString());
                    doAnswer(resultCaptor).when(lagAnalyzerService).analyzeLag(anyString());

                    assertThat(resultCaptor.getResult())
                            .isNotNull()
                            .isInstanceOf(Map.class);

                });
    }

    private class ResultCaptor<T> implements Answer {
        private T result = null;
        public T getResult() {
            return result;
        }

        @Override
        public T answer(InvocationOnMock invocationOnMock) throws Throwable {
            result = (T) invocationOnMock.callRealMethod();
            return result;
        }
    }

}

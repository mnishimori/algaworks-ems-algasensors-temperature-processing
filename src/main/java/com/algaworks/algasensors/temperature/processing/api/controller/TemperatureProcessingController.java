package com.algaworks.algasensors.temperature.processing.api.controller;

import static com.algaworks.algasensors.temperature.processing.infrastructure.rabbitmq.RabbitMqConfig.FANOUT_EXCHANGE_TEMPERATURE_PROCESSING_TEMPERATURE_RECEIVED_V_1_E;

import com.algaworks.algasensors.temperature.processing.api.model.TemperatureLogOutput;
import com.algaworks.algasensors.temperature.processing.common.IdGenerator;
import io.hypersistence.tsid.TSID;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/sensors/{sensorId}/temperatures/data")
@Slf4j
@RequiredArgsConstructor
public class TemperatureProcessingController {

  private final RabbitTemplate rabbitTemplate;

  @PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE)
  public void data(@PathVariable TSID sensorId, @RequestBody String input) {
    if (input == null || input.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    double temperature;
    try {
      temperature = Double.parseDouble(input);
    } catch (NumberFormatException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    TemperatureLogOutput logOutput = TemperatureLogOutput.builder()
        .id(IdGenerator.generateTimeBasedUUID())
        .sensorId(sensorId)
        .value(temperature)
        .registeredAt(OffsetDateTime.now())
        .build();

    MessagePostProcessor messagePostProcessor = message -> {
      message.getMessageProperties().setContentType("application/json");
      message.getMessageProperties().setHeader("sensorId", sensorId.toString());
      return message;
    };
    rabbitTemplate.convertAndSend(FANOUT_EXCHANGE_TEMPERATURE_PROCESSING_TEMPERATURE_RECEIVED_V_1_E, "", logOutput,
        messagePostProcessor);
    log.info(logOutput.toString());
  }
}

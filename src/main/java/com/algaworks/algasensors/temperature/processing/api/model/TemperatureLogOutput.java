package com.algaworks.algasensors.temperature.processing.api.model;

import io.hypersistence.tsid.TSID;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TemperatureLogOutput {

  private UUID id;
  private TSID sensorId;
  private OffsetDateTime registeredAt;
  private Double value;

}

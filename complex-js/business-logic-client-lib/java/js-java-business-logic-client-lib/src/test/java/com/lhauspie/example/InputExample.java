package com.lhauspie.example;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record InputExample(
    Fence fence,
    Materials materials
) {

  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public record Fence(
      int fenceLength,
      int nbSegments,
      int nbSegmentsGreaterThanRollLength,
      float anotherField
  ) {
  }

  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public record Materials(
      int wireMeshRollLength,
      float wireMeshRollHeight,
      int tensionWireRollLength
  ) {
  }
}

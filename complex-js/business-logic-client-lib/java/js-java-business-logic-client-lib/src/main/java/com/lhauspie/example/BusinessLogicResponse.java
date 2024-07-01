package com.lhauspie.example;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;
import java.util.Optional;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record BusinessLogicResponse(
    Result result,
    List<Error> errors,
    List<Warning> warnings
) {

  public static BusinessLogicResponse success(Result result) {
    return new BusinessLogicResponse(result, List.of(), List.of());
  }

  public static BusinessLogicResponse error(List<Error> errors) {
    return new BusinessLogicResponse(Result.empty(), errors, List.of());
  }

  public static BusinessLogicResponse warning(List<Warning> warnings) {
    return new BusinessLogicResponse(Result.empty(), List.of(), warnings);
  }

  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public record Result(
      Optional<Integer> nbWireMeshRolls,
      Optional<Integer> nbStacks,
      Optional<Integer> nbStruts,
      Optional<Integer> nbTensionBars,
      Optional<Integer> nbTensioners,
      Optional<Integer> nbTensionWireRolls,
      Optional<Integer> nbStaples,
      Optional<Integer> nbTensionWires
  ) {
    public static Result empty() {
      return new Result(
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty()
      );
    }
  }

  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public record Error(
      String code,
      Optional<String> path,
      String message
  ) {
  }

  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public record Warning(
      String code,
      String message
  ) {
  }
}

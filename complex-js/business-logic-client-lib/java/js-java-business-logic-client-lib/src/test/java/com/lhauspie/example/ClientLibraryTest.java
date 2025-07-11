package com.lhauspie.example;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class ClientLibraryTest {

  private final WireMockServer wiremockServer = new WireMockServer(0);

  @BeforeEach
  void setup() {
    wiremockServer.start();

    wiremockServer.stubFor(
        get(urlEqualTo("/business-logic.js"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/javascript")
                .withHeader("Last-Modified", "Wed, 21 Oct 2015 07:28:00 GMT")
                .withBody("""
                    function execute(input) {
                        console.log("Log from Business Logic : " + input);
                        return {
                            result: {
                                nb_wire_mesh_rolls: 4,
                                nb_stacks: 41,
                                nb_struts: 8,
                                nb_tension_bars: 8,
                                nb_tensioners: 16,
                                nb_tension_wire_rolls: 9,
                                nb_staples: 120000,
                                nb_tension_wires: 8
                            },
                            errors: [],
                            warnings: []
                        };
                    }
                    
                    module.exports = {execute};
                    """)
            )
    );
  }

  @AfterEach
  void teardown() {
    wiremockServer.stop();
  }

  @Test
  void executingBusinessLogicProvideResultWithoutErrorsNorWarnings() {
    // Given
    wiremockServer.stubFor(
        get(urlEqualTo("/business-logic.js"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/javascript")
                .withHeader("Last-Modified", "Wed, 21 Oct 2015 07:28:00 GMT")
                .withBody("""
                    function execute(input) {
                        return {
                            result: {
                                nb_wire_mesh_rolls: 4,
                                nb_stacks: 41,
                                nb_struts: 8,
                                nb_tension_bars: 8,
                                nb_tensioners: 16,
                                nb_tension_wire_rolls: 9,
                                nb_staples: 120000,
                                nb_tension_wires: 8
                            },
                            errors: [],
                            warnings: []
                        };
                    }
                    
                    module.exports = {execute};
                    """)
            )
    );

    ClientLibrary clientLibrary = new ClientLibrary(wiremockServer.baseUrl(), Duration.ofMinutes(1));

    // When
    var input = new InputExample(
        new InputExample.Fence(10, 4, 2, 1.5f),
        new InputExample.Materials(50, 2.5f, 100)
    );
    var businessLogicResponse = clientLibrary.executeBusinessLogic(input);

    // Then
    Assertions.assertThat(businessLogicResponse).isNotNull();
    Assertions.assertThat(businessLogicResponse.result()).isNotNull();
    Assertions.assertThat(businessLogicResponse.result().nbWireMeshRolls()).isNotEmpty().hasValue(4);
    Assertions.assertThat(businessLogicResponse.result().nbStacks()).isNotEmpty().hasValue(41);
    Assertions.assertThat(businessLogicResponse.result().nbStruts()).isNotEmpty().hasValue(8);
    Assertions.assertThat(businessLogicResponse.result().nbTensionBars()).isNotEmpty().hasValue(8);
    Assertions.assertThat(businessLogicResponse.result().nbTensioners()).isNotEmpty().hasValue(16);
    Assertions.assertThat(businessLogicResponse.result().nbTensionWireRolls()).isNotEmpty().hasValue(9);
    Assertions.assertThat(businessLogicResponse.result().nbStaples()).isNotEmpty().hasValue(120000);
    Assertions.assertThat(businessLogicResponse.result().nbTensionWires()).isNotEmpty().hasValue(8);
    Assertions.assertThat(businessLogicResponse.errors()).isNotNull().isEmpty();
    Assertions.assertThat(businessLogicResponse.warnings()).isNotNull().isEmpty();
  }

  @Test
  void executingBusinessLogicProvideEmptyResultWithErrors() {
    // Given
    wiremockServer.stubFor(
        get(urlEqualTo("/business-logic.js"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/javascript")
                .withHeader("Last-Modified", "Wed, 21 Oct 2015 07:28:00 GMT")
                .withBody("""
                    function execute(input) {
                        return {
                            result: {
                                nb_wire_mesh_rolls: null,
                                nb_stacks: null,
                                nb_struts: null,
                                nb_tension_bars: null,
                                nb_tensioners: null,
                                nb_tension_wire_rolls: null,
                                nb_staples: null,
                                nb_tension_wires: null
                            },
                            errors: [
                                {code: "ERROR_CODE_1", path: "path.to.first.field", message: "Error message 1"},
                                {code: "ERROR_CODE_2", path: "path.to.second.field", message: "Error message 2"}
                            ],
                            warnings: []
                        };
                    }
                    
                    module.exports = {execute};
                    """)
            )
    );

    ClientLibrary clientLibrary = new ClientLibrary(wiremockServer.baseUrl(), Duration.ofMinutes(1));

    // When
    var input = new InputExample(
        new InputExample.Fence(10, 4, 2, 1.5f),
        new InputExample.Materials(50, 2.5f, 100)
    );
    var businessLogicResponse = clientLibrary.executeBusinessLogic(input);

    // Then
    Assertions.assertThat(businessLogicResponse).isNotNull();
    Assertions.assertThat(businessLogicResponse.result()).isNotNull();
    Assertions.assertThat(businessLogicResponse.result().nbWireMeshRolls()).isEmpty();
    Assertions.assertThat(businessLogicResponse.result().nbStacks()).isEmpty();
    Assertions.assertThat(businessLogicResponse.result().nbStruts()).isEmpty();
    Assertions.assertThat(businessLogicResponse.result().nbTensionBars()).isEmpty();
    Assertions.assertThat(businessLogicResponse.result().nbTensioners()).isEmpty();
    Assertions.assertThat(businessLogicResponse.result().nbTensionWireRolls()).isEmpty();
    Assertions.assertThat(businessLogicResponse.result().nbStaples()).isEmpty();
    Assertions.assertThat(businessLogicResponse.result().nbTensionWires()).isEmpty();
    Assertions.assertThat(businessLogicResponse.errors()).isNotNull().isNotEmpty().hasSize(2).contains(
        new BusinessLogicResponse.Error("ERROR_CODE_1", Optional.of("path.to.first.field"), "Error message 1"),
        new BusinessLogicResponse.Error("ERROR_CODE_2", Optional.of("path.to.second.field"), "Error message 2")
    );
    Assertions.assertThat(businessLogicResponse.warnings()).isNotNull().isEmpty();
  }

  @Test
  void executingBusinessLogicProvideNoResultWithErrorsAndWarnings() {
    // Given
    wiremockServer.stubFor(
        get(urlEqualTo("/business-logic.js"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/javascript")
                .withHeader("Last-Modified", "Wed, 21 Oct 2015 07:28:00 GMT")
                .withBody("""
                    function execute(input) {
                        return {
                            errors: [
                                {code: "ERROR_CODE_1", path: "path.to.first.field", message: "Error message 1"},
                            ],
                            warnings: [
                                {code: "WARNING_CODE_1", path: "path.to.first.field", message: "Warning message 1"},
                                {code: "WARNING_CODE_1", path: "path.to.second.field", message: "Warning message 2"}
                            ]
                        };
                    }
                    
                    module.exports = {execute};
                    """)
            )
    );

    ClientLibrary clientLibrary = new ClientLibrary(wiremockServer.baseUrl(), Duration.ofMinutes(1));

    // When
    var input = new InputExample(
        new InputExample.Fence(10, 4, 2, 1.5f),
        new InputExample.Materials(50, 2.5f, 100)
    );
    var businessLogicResponse = clientLibrary.executeBusinessLogic(input);

    // Then
    Assertions.assertThat(businessLogicResponse).isNotNull();
    Assertions.assertThat(businessLogicResponse.result()).isNull();
    Assertions.assertThat(businessLogicResponse.errors()).isNotNull().isNotEmpty().hasSize(1).contains(
        new BusinessLogicResponse.Error("ERROR_CODE_1", Optional.of("path.to.first.field"), "Error message 1")
    );
    Assertions.assertThat(businessLogicResponse.warnings()).isNotNull().isNotEmpty().hasSize(2).contains(
        new BusinessLogicResponse.Warning("WARNING_CODE_1", "Warning message 1"),
        new BusinessLogicResponse.Warning("WARNING_CODE_1", "Warning message 2")
    );
  }

  @Test
  void doesNotFailIfBusinessLogicResponseIsNotGood() {
    // Given
    wiremockServer.stubFor(
        get(urlEqualTo("/business-logic.js"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/javascript")
                .withHeader("Last-Modified", "Wed, 21 Oct 2015 07:28:00 GMT")
                .withBody("""
                    function execute(input) {
                        return {
                            toto: {
                                field_a: "FIELD-A",
                                field_b: "f-i-e-l-d-B",
                                date: "2024-07-01",
                                datetime: "2024-07-01T12:34:56Z"
                            },
                            titi: [
                                {code: "WARNING_CODE_1", path: "path.to.first.field", message: "Warning message 1"},
                                {code: "WARNING_CODE_1", path: "path.to.second.field", message: "Warning message 2"}
                            ]
                        };
                    }
                    
                    module.exports = {execute};
                    """)
            )
    );

    ClientLibrary clientLibrary = new ClientLibrary(wiremockServer.baseUrl(), Duration.ofMinutes(1));

    // When
    var input = new InputExample(
        new InputExample.Fence(10, 4, 2, 1.5f),
        new InputExample.Materials(50, 2.5f, 100)
    );
    var businessLogicResponse = clientLibrary.executeBusinessLogic(input);

    // Then
    Assertions.assertThat(businessLogicResponse).isNotNull();
    Assertions.assertThat(businessLogicResponse.result()).isNull();
    Assertions.assertThat(businessLogicResponse.errors()).isNull();
    Assertions.assertThat(businessLogicResponse.warnings()).isNull();
  }

  @Test
  void getBackInputParametersAsWarningsToCheckInputIsCorrectlyInterpreted() {
    // Given
    wiremockServer.stubFor(
        get(urlEqualTo("/business-logic.js"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/javascript")
                .withHeader("Last-Modified", "Wed, 21 Oct 2015 07:28:00 GMT")
                .withBody("""
                    function execute(input) {
                        return {
                            result: {},
                            errors: [],
                            warnings: [
                                {code: "INPUT_PARAMETER",                                            message: JSON.stringify(input)},
                                {code: "INPUT_PARAMETER.fence",                                      message: JSON.stringify(input.fence)},
                                {code: "INPUT_PARAMETER.fence.fence_length",                         message: "" + input.fence.fence_length},
                                {code: "INPUT_PARAMETER.fence.nb_segments",                          message: "" + input.fence.nb_segments},
                                {code: "INPUT_PARAMETER.fence.nb_segments_greater_than_roll_length", message: "" + input.fence.nb_segments_greater_than_roll_length},
                                {code: "INPUT_PARAMETER.fence.another_field",                        message: "" + input.fence.another_field},
                                {code: "INPUT_PARAMETER.materials",                                  message: JSON.stringify(input.materials)},
                                {code: "INPUT_PARAMETER.materials.wire_mesh_roll_length",            message: "" + input.materials.wire_mesh_roll_length},
                                {code: "INPUT_PARAMETER.materials.wire_mesh_roll_height",            message: "" + input.materials.wire_mesh_roll_height},
                                {code: "INPUT_PARAMETER.materials.tension_wire_roll_length",         message: "" + input.materials.tension_wire_roll_length},
                            ]
                        };
                    }
                    
                    module.exports = {execute};
                    """)
            )
    );

    ClientLibrary clientLibrary = new ClientLibrary(wiremockServer.baseUrl(), Duration.ofMinutes(1));

    // When
    var input = new InputExample(
        new InputExample.Fence(10, 4, 2, 1.5f),
        new InputExample.Materials(50, 2.5f, 100)
    );
    var businessLogicResponse = clientLibrary.executeBusinessLogic(input);

    // Then
    Assertions.assertThat(businessLogicResponse).isNotNull();
    Assertions.assertThat(businessLogicResponse.result()).isNotNull();
    Assertions.assertThat(businessLogicResponse.errors()).isNotNull().isEmpty();
    Assertions.assertThat(businessLogicResponse.warnings()).isNotNull().hasSize(10).contains(
        new BusinessLogicResponse.Warning("INPUT_PARAMETER", "{\"fence\":{\"fence_length\":10,\"nb_segments\":4,\"nb_segments_greater_than_roll_length\":2,\"another_field\":1.5},\"materials\":{\"wire_mesh_roll_length\":50,\"wire_mesh_roll_height\":2.5,\"tension_wire_roll_length\":100}}"),
        new BusinessLogicResponse.Warning("INPUT_PARAMETER.fence", "{\"fence_length\":10,\"nb_segments\":4,\"nb_segments_greater_than_roll_length\":2,\"another_field\":1.5}"),
        new BusinessLogicResponse.Warning("INPUT_PARAMETER.fence.fence_length", "10"),
        new BusinessLogicResponse.Warning("INPUT_PARAMETER.fence.nb_segments", "4"),
        new BusinessLogicResponse.Warning("INPUT_PARAMETER.fence.nb_segments_greater_than_roll_length", "2"),
        new BusinessLogicResponse.Warning("INPUT_PARAMETER.fence.another_field", "1.5"),
        new BusinessLogicResponse.Warning("INPUT_PARAMETER.materials", "{\"wire_mesh_roll_length\":50,\"wire_mesh_roll_height\":2.5,\"tension_wire_roll_length\":100}"),
        new BusinessLogicResponse.Warning("INPUT_PARAMETER.materials.wire_mesh_roll_length", "50"),
        new BusinessLogicResponse.Warning("INPUT_PARAMETER.materials.wire_mesh_roll_height", "2.5"),
        new BusinessLogicResponse.Warning("INPUT_PARAMETER.materials.tension_wire_roll_length", "100")
    );
  }
}

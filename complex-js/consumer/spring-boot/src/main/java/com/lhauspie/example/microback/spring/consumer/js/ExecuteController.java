package com.lhauspie.example.microback.spring.consumer.js;

import com.lhauspie.example.BusinessLogicResponse;
import com.lhauspie.example.ClientLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/execute")
public class ExecuteController {
  private final ClientLibrary clientLibrary;

  @Autowired
  public ExecuteController(BusinessLogicProviderConfig config) {
    this.clientLibrary = new ClientLibrary(config.url(), config.cacheTtl());
  }

  @PostMapping
  public BusinessLogicResponse execute(@RequestBody Object input) {
    return clientLibrary.executeBusinessLogic(input);
  }
}
package io.sandbox.entities;

import io.sandbox.entities.entities.EntityLoader;
import net.fabricmc.api.ClientModInitializer;

public class MainClient implements ClientModInitializer {

  @Override
  public void onInitializeClient() {
    EntityLoader.initClient();
  }
}

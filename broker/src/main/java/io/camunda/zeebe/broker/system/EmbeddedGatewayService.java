/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.broker.system;

import io.camunda.identity.sdk.IdentityConfiguration;
import io.camunda.zeebe.broker.system.configuration.BrokerCfg;
import io.camunda.zeebe.gateway.Gateway;
import io.camunda.zeebe.gateway.Loggers;
import io.camunda.zeebe.gateway.impl.broker.BrokerClient;
import io.camunda.zeebe.gateway.impl.stream.JobStreamClient;
import io.camunda.zeebe.scheduler.ActorSchedulingService;
import io.camunda.zeebe.scheduler.ConcurrencyControl;
import io.camunda.zeebe.scheduler.future.ActorFuture;
import org.agrona.CloseHelper;

public final class EmbeddedGatewayService implements AutoCloseable {
  private final Gateway gateway;
  private final BrokerClient brokerClient;
  private final JobStreamClient jobStreamClient;
  private final ConcurrencyControl concurrencyControl;

  public EmbeddedGatewayService(
      final BrokerCfg configuration,
      final IdentityConfiguration identityConfiguration,
      final ActorSchedulingService actorScheduler,
      final ConcurrencyControl concurrencyControl,
      final JobStreamClient jobStreamClient,
      final BrokerClient brokerClient) {
    this.concurrencyControl = concurrencyControl;
    this.brokerClient = brokerClient;
    this.jobStreamClient = jobStreamClient;
    gateway =
        new Gateway(
            configuration.getGateway(),
            identityConfiguration,
            brokerClient,
            actorScheduler,
            jobStreamClient.streamer());
  }

  @Override
  public void close() {
    CloseHelper.closeAll(
        error ->
            Loggers.GATEWAY_LOGGER.warn(
                "Error occurred while shutting down embedded gateway", error),
        gateway,
        brokerClient,
        jobStreamClient);
  }

  public Gateway get() {
    return gateway;
  }

  public ActorFuture<Gateway> start() {
    // before we can add the job stream client as a topology listener, we need to wait for the
    // topology to be set up, otherwise the callback may be lost
    concurrencyControl.runOnCompletion(
        jobStreamClient.start(),
        (ok, error) -> brokerClient.getTopologyManager().addTopologyListener(jobStreamClient));

    return gateway.start();
  }
}

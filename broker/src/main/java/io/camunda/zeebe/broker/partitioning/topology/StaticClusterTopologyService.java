/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.broker.partitioning.topology;

import io.camunda.zeebe.broker.bootstrap.BrokerStartupContext;
import io.camunda.zeebe.scheduler.future.ActorFuture;
import io.camunda.zeebe.scheduler.future.CompletableActorFuture;

public class StaticClusterTopologyService implements ClusterTopologyService {

  private PartitionDistribution partitionDistribution;

  @Override
  public PartitionDistribution getPartitionDistribution() {
    return partitionDistribution;
  }

  @Override
  public ActorFuture<Void> start(final BrokerStartupContext brokerStartupContext) {
    try {
      partitionDistribution =
          new PartitionDistributionResolver()
              .resolvePartitionDistribution(
                  brokerStartupContext.getBrokerConfiguration().getExperimental().getPartitioning(),
                  brokerStartupContext.getBrokerConfiguration().getCluster());
    } catch (final Exception e) {
      return CompletableActorFuture.completedExceptionally(e);
    }

    return CompletableActorFuture.completed(null);
  }

  @Override
  public ActorFuture<Void> closeAsync() {
    partitionDistribution = null;
    return CompletableActorFuture.completed(null);
  }
}
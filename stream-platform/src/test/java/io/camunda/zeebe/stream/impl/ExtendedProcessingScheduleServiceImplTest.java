/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.stream.impl;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import io.camunda.zeebe.scheduler.ConcurrencyControl;
import io.camunda.zeebe.stream.api.scheduling.SimpleProcessingScheduleService;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

final class ExtendedProcessingScheduleServiceImplTest {
  @Test
  void shouldNotScheduleAsyncIfDisabled() {
    // given
    final var sync = mock(SimpleProcessingScheduleService.class);
    final var async = mock(SimpleProcessingScheduleService.class);
    final var concurrencyControl = mock(ConcurrencyControl.class);
    final var schedulingService =
        new ExtendedProcessingScheduleServiceImpl(sync, async, concurrencyControl, false);

    // when
    schedulingService.runDelayed(Duration.ZERO, () -> {});

    // then
    Mockito.verify(sync, Mockito.times(1))
        .runDelayed(Mockito.eq(Duration.ZERO), Mockito.<Runnable>any());
  }

  @Test
  void shouldAlwaysScheduleAsyncIfEnabled() {
    // given
    final var sync = mock(SimpleProcessingScheduleService.class);
    final var async = mock(SimpleProcessingScheduleService.class);
    final var concurrencyControl = mock(ConcurrencyControl.class);
    doAnswer(
            invocation -> {
              final var runnable = (Runnable) invocation.getArgument(0);
              runnable.run();
              return null;
            })
        .when(concurrencyControl)
        .run(Mockito.any());

    final var schedulingService =
        new ExtendedProcessingScheduleServiceImpl(sync, async, concurrencyControl, true);

    // when
    schedulingService.runDelayed(Duration.ZERO, () -> {});

    // then
    Mockito.verify(async, Mockito.times(1))
        .runDelayed(Mockito.eq(Duration.ZERO), Mockito.<Runnable>any());
  }
}

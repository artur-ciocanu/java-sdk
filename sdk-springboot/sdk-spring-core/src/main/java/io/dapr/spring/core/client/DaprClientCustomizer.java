/*
 * Copyright 2024 The Dapr Authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
limitations under the License.
*/

package io.dapr.spring.core.client;

import io.dapr.client.DaprClientBuilder;

/**
 * Callback interface that can be used to customize a {@link DaprClientBuilder}.
 */
@FunctionalInterface
public interface DaprClientCustomizer {

  /**
   * Callback to customize a {@link DaprClientBuilder} instance.
   *
   * @param daprClientBuilder the client builder to customize
   */
  void customize(DaprClientBuilder daprClientBuilder);

}

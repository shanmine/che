/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core;

import com.google.inject.Provider;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.selenium.core.client.TestUserServiceClient;
import org.eclipse.che.selenium.core.client.user.TestUserServiceClientImpl;

/** @author Anton Korneta */
public class TestUserServiceClientProvider implements Provider<TestUserServiceClient> {

  private final boolean isMultiUser;
  private final TestUserServiceClient cheTestUserServiceClient;

  @Inject
  public TestUserServiceClientProvider(
      @Named("che.multiuser") boolean isMultiUser,
      TestUserServiceClientImpl cheTestUserServiceClient) {
    this.isMultiUser = isMultiUser;
    this.cheTestUserServiceClient = cheTestUserServiceClient;
  }

  @Override
  public TestUserServiceClient get() {
    return isMultiUser ? cheTestUserServiceClient : cheTestUserServiceClient;
  }
}

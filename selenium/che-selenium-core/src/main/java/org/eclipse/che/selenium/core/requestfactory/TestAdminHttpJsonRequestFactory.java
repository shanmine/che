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
package org.eclipse.che.selenium.core.requestfactory;

import static java.lang.String.format;

import com.google.inject.name.Named;
import javax.inject.Inject;
import org.eclipse.che.selenium.core.client.TestAuthServiceClient;

/** @author Dmytro Nochevnov */
public class TestAdminHttpJsonRequestFactory extends TestHttpJsonRequestFactory {

  private final String adminEmail;
  private final String adminPassword;
  private final TestAuthServiceClient authServiceClient;

  @Inject
  public TestAdminHttpJsonRequestFactory(
      TestAuthServiceClient authServiceClient,
      @Named("che.admin_user.email") String adminEmail,
      @Named("che.admin_user.password") String adminPassword) {
    this.adminEmail = adminEmail;
    this.adminPassword = adminPassword;
    this.authServiceClient = authServiceClient;
  }

  protected String getAuthToken() {
    try {
      return authServiceClient.login(adminEmail, adminPassword);
    } catch (Exception ex) {
      throw new RuntimeException(format("Failed to get access token for user '%s'", adminEmail));
    }
  }
}

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
package org.eclipse.che.multiuser.keycloak.server;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.AUTH_SERVER_URL_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.CLIENT_ID_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.GITHUB_ENDPOINT_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.LOGOUT_ENDPOINT_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.OSO_ENDPOINT_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.PASSWORD_ENDPOINT_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.PROFILE_ENDPOINT_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.REALM_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.TOKEN_ENDPOINT_SETTING;

import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Endpoint which provides keycloak public client authentication information (such as URL, realm,
 * cliend_id).
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
@Singleton
@Path("/keycloak")
public class KeycloakConfigurationService extends Service {

  @Inject
  public KeycloakConfigurationService(
      @Named(AUTH_SERVER_URL_SETTING) String serverURL,
      @Named(REALM_SETTING) String realm,
      @Named(CLIENT_ID_SETTING) String clientId,
      @Nullable @Named(OSO_ENDPOINT_SETTING) String osoEndpoint,
      @Nullable @Named(GITHUB_ENDPOINT_SETTING) String gitHubEndpoint) {
    Map<String, String> settings = Maps.newHashMap();
    settings.put(AUTH_SERVER_URL_SETTING, serverURL);
    settings.put(CLIENT_ID_SETTING, clientId);
    settings.put(REALM_SETTING, realm);
    settings.put(PROFILE_ENDPOINT_SETTING, serverURL + "/realms/" + realm + "/account");
    settings.put(PASSWORD_ENDPOINT_SETTING, serverURL + "/realms/" + realm + "/account/password");
    settings.put(
        TOKEN_ENDPOINT_SETTING, serverURL + "/realms/" + realm + "/protocol/openid-connect/token");
    settings.put(
        LOGOUT_ENDPOINT_SETTING,
        serverURL + "/realms/" + realm + "/protocol/openid-connect/logout");
    settings.put(OSO_ENDPOINT_SETTING, osoEndpoint);
    settings.put(GITHUB_ENDPOINT_SETTING, gitHubEndpoint);
    KeycloakSettings.set(Collections.unmodifiableMap(settings));
  }

  @GET
  @Path("/settings")
  @Produces(APPLICATION_JSON)
  public Map<String, String> settings() {
    return KeycloakSettings.get();
  }
}

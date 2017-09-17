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
package org.eclipse.che.api.deploy;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import javax.sql.DataSource;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.multiuser.api.permission.server.account.AccountPermissionsChecker;
import org.eclipse.che.multiuser.permission.account.PersonalAccountPermissionsChecker;

@DynaModule
public class MultiUserCheWsMasterModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(DataSource.class).toProvider(org.eclipse.che.core.db.JndiDataSourceProvider.class);
    install(new org.eclipse.che.multiuser.api.permission.server.jpa.SystemPermissionsJpaModule());
    install(new org.eclipse.che.multiuser.api.permission.server.PermissionsModule());
    install(
        new org.eclipse.che.multiuser.permission.workspace.server.WorkspaceApiPermissionsModule());
    install(
        new org.eclipse.che.multiuser.permission.workspace.server.jpa
            .MultiuserWorkspaceJpaModule());

    //Permission filters
    bind(org.eclipse.che.multiuser.permission.system.SystemServicePermissionsFilter.class);
    bind(org.eclipse.che.multiuser.permission.user.UserProfileServicePermissionsFilter.class);
    bind(org.eclipse.che.multiuser.permission.user.UserServicePermissionsFilter.class);
    bind(org.eclipse.che.multiuser.permission.factory.FactoryPermissionsFilter.class);
    bind(org.eclipse.che.plugin.activity.ActivityPermissionsFilter.class);

    Multibinder.newSetBinder(binder(), AccountPermissionsChecker.class)
        .addBinding()
        .to(PersonalAccountPermissionsChecker.class);
  }
}
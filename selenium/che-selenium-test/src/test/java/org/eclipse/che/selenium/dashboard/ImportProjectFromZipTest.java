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
package org.eclipse.che.selenium.dashboard;

import com.google.inject.Inject;
import java.util.concurrent.ExecutionException;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.constant.TestStacksConstants;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class ImportProjectFromZipTest {
  private final String WORKSPACE = NameGenerator.generate("ImptPrjFromZip", 4);
  private static final String PROJECT_NAME = "master";

  @Inject private Dashboard dashboard;
  @Inject private DashboardWorkspace dashboardWorkspace;
  @Inject private Loader loader;
  @Inject private ProjectExplorer explorer;
  @Inject private NavigationBar navigationBar;
  @Inject private CreateWorkspace createWorkspace;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TestUser defaultTestUser;

  @BeforeClass
  public void setUp() {
    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE, defaultTestUser.getName());
  }

  @Test
  public void importProjectFromZipTest() throws ExecutionException, InterruptedException {
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(NavigationBar.MenuItem.WORKSPACES);

    dashboardWorkspace.clickOnNewWorkspaceBtn();
    createWorkspace.waitToolbar();
    createWorkspace.selectStack(TestStacksConstants.JAVA.getId());
    createWorkspace.typeWorkspaceName(WORKSPACE);

    projectSourcePage.clickAddOrImportProjectButton();

    projectSourcePage.selectSourceTab(ProjectSourcePage.Sources.ZIP);

    projectSourcePage.typeZipLocation(
        "https://github.com/iedexmain1/multimodule-project/archive/master.zip");
    projectSourcePage.skipRootFolder();
    projectSourcePage.clickAdd();

    createWorkspace.clickCreate();

    seleniumWebDriver.switchFromDashboardIframeToIde();

    loader.waitOnClosed();
    explorer.waitItem(PROJECT_NAME);
    explorer.selectItem(PROJECT_NAME);
    explorer.openContextMenuByPathSelectedItem(PROJECT_NAME);

    /* TODO when bug with project type is solved:
    explorer.clickOnItemInContextMenu(ProjectExplorerContextMenuConstants.MAVEN);
    explorer.clickOnItemInContextMenu(ProjectExplorer.PROJECT_EXPLORER_CONTEXT_MENU_MAVEN.REIMPORT);
    loader.waitOnClosed();

    explorer.openItemByPath(PROJECT_NAME);

    explorer.openContextMenuByPathSelectedItem(PROJECT_NAME + "/my-lib");
    explorer.clickOnItemInContextMenu(ProjectExplorerContextMenuConstants.MAVEN);
    explorer.clickOnItemInContextMenu(ProjectExplorer.PROJECT_EXPLORER_CONTEXT_MENU_MAVEN.REIMPORT);
    loader.waitOnClosed();

    explorer.openContextMenuByPathSelectedItem(PROJECT_NAME + "/my-webapp");
    explorer.clickOnItemInContextMenu(ProjectExplorerContextMenuConstants.MAVEN);
    explorer.clickOnItemInContextMenu(ProjectExplorer.PROJECT_EXPLORER_CONTEXT_MENU_MAVEN.REIMPORT);*/
  }
}

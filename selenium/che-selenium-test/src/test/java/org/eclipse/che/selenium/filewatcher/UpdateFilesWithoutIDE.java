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
package org.eclipse.che.selenium.filewatcher;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.pageobject.InjectPageObject;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class UpdateFilesWithoutIDE {

  private static final String PROJECT_NAME = NameGenerator.generate("project", 6);

  @Inject private TestWorkspace ws;
  @Inject private TestUser user;

  @InjectPageObject(driverId = 1)
  private Ide ide1;

  @InjectPageObject(driverId = 1)
  private ProjectExplorer projectExplorer1;

  @InjectPageObject(driverId = 1)
  private CodenvyEditor editor1;

  @InjectPageObject(driverId = 1)
  private Events events1;

  @InjectPageObject(driverId = 2)
  private Ide ide2;

  @InjectPageObject(driverId = 2)
  private CodenvyEditor editor2;

  @InjectPageObject(driverId = 2)
  private NotificationsPopupPanel notifications2;

  @InjectPageObject(driverId = 2)
  private ProjectExplorer projectExplorer2;

  @InjectPageObject(driverId = 2)
  private Events events2;

  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/spring-project-for-file-watcher-tabs");
    testProjectServiceClient.importProject(
        ws.getId(), Paths.get(resource.toURI()), PROJECT_NAME, ProjectTemplates.MAVEN_SPRING);

    ide1.open(ws);
    ide2.open(ws);

    events1.clickProjectEventsTab();
    events2.clickProjectEventsTab();
  }

  @Test
  public void checkEditingFileWithoutIDE() throws Exception {
    String nameFiletxt2 = "file2.txt";
    String nameFiletxt3 = "file3.txt";
    String expectedMessage2 = "File '" + nameFiletxt2 + "' is updated";
    String expectedMessage3 = "File '" + nameFiletxt3 + "' is updated";
    projectExplorer1.openItemByPath(PROJECT_NAME);
    projectExplorer2.openItemByPath(PROJECT_NAME);
    projectExplorer1.openItemByPath(PROJECT_NAME + "/" + nameFiletxt2);
    editor1.waitActiveEditor();
    projectExplorer2.openItemByPath(PROJECT_NAME + "/" + nameFiletxt2);
    editor1.waitActiveEditor();

    testProjectServiceClient.updateFile(
        ws.getId(), PROJECT_NAME + "/" + nameFiletxt2, Long.toString(System.currentTimeMillis()));

    events1.waitExpectedMessage(expectedMessage2, 10);
    projectExplorer1.openItemByPath(PROJECT_NAME + "/" + nameFiletxt3);
    editor1.waitActiveEditor();
    projectExplorer2.openItemByPath(PROJECT_NAME + "/" + nameFiletxt3);
    editor2.waitActiveEditor();

    String currentTimeInMs = Long.toString(System.currentTimeMillis());
    testProjectServiceClient.updateFile(
        ws.getId(), PROJECT_NAME + "/" + nameFiletxt3, Long.toString(System.currentTimeMillis()));

    editor1.waitTextIntoEditor(currentTimeInMs, 5);
    editor2.waitTextIntoEditor(currentTimeInMs, 5);
    events1.waitExpectedMessage(expectedMessage3, 10);
    events2.waitExpectedMessage(expectedMessage3, 10);
  }
}

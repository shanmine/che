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
package org.eclipse.che.ide.ui.multisplitpanel.actions;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.ui.multisplitpanel.SubPanel;

/**
 * Action for vertically splitting the given {@link SubPanel} on the two sub-panels.
 *
 * @author Artem Zatsarynnyi
 */
public class SplitVerticallyAction extends Action {

  private final SubPanel subPanel;

  public SplitVerticallyAction(SubPanel subPanel) {
    super("Split Pane In Two Columns", "Split Pane In Two Columns", null, null);
    this.subPanel = subPanel;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    subPanel.splitVertically();
  }
}

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
package org.eclipse.che.api.core.rest.shared.dto;

import java.util.Map;
import org.eclipse.che.dto.shared.DTO;

/**
 * Extended error which contains error code and optional attributes.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@DTO
public interface ExtendedError extends ServiceError {

  /**
   * Get error code.
   *
   * @return error code
   */
  int getErrorCode();

  /**
   * Set error code.
   *
   * @param errorCode error code
   */
  void setErrorCode(int errorCode);

  ExtendedError withErrorCode(int errorCode);

  /**
   * Get error attributes.
   *
   * @return attributes of the error if any
   */
  Map<String, String> getAttributes();

  /**
   * Set error attributes.
   *
   * @param attributes error attributes
   */
  void setAttributes(Map<String, String> attributes);

  ExtendedError withAttributes(Map<String, String> attributes);

  @Override
  ExtendedError withMessage(String message);
}

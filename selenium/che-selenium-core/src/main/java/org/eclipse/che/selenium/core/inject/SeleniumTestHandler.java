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
package org.eclipse.che.selenium.core.inject;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.constant.TestBrowser;
import org.eclipse.che.selenium.core.pageobject.InjectPageObject;
import org.eclipse.che.selenium.core.pageobject.PageObjectsInjector;
import org.eclipse.che.selenium.core.user.InjectTestUser;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProvider;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IConfigurationListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestException;

/**
 * Tests lifecycle handler.
 *
 * <p>Adding {@link IConfigurationListener} brings bug when all methods of the listener will be
 * invoked twice.
 *
 * @author Anatolii Bazko
 */
public abstract class SeleniumTestHandler
    implements ITestListener, ISuiteListener, IInvokedMethodListener {

  private static final Logger LOG = LoggerFactory.getLogger(SeleniumTestHandler.class);

  @Inject
  @Named("tests.screenshot_dir")
  private String screenshotDir;

  @Inject private PageObjectsInjector pageObjectsInjector;

  @Inject
  @Named("sys.browser")
  private TestBrowser browser;

  @Inject
  @Named("sys.driver.port")
  private String webDriverPort;

  @Inject
  @Named("sys.grid.mode")
  private boolean gridMode;

  @Inject
  @Named("sys.driver.version")
  private String webDriverVersion;

  @Inject private TestUser defaultTestUser;
  @Inject private TestWorkspaceProvider testWorkspaceProvider;

  private final Map<Long, Object> runningTests = new ConcurrentHashMap<>();

  @Override
  public void onTestStart(ITestResult result) {}

  @Override
  public void onTestSuccess(ITestResult result) {
    onTestFinish(result);
  }

  @Override
  public void onTestFailure(ITestResult result) {
    onTestFinish(result);
  }

  @Override
  public void onTestSkipped(ITestResult result) {
    onTestFinish(result);
  }

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    onTestFinish(result);
  }

  @Override
  public void onStart(ITestContext context) {
    checkWebDriverSessionCreation();
  }

  @Override
  public void onFinish(ITestContext context) {}

  @Override
  public void onStart(ISuite suite) {
    Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

    Injector injector = createParentInjector();
    injector.injectMembers(this);

    suite.setParentInjector(injector);
  }

  /** Check if webdriver session can be created without errors. */
  private void checkWebDriverSessionCreation() {
    SeleniumWebDriver seleniumWebDriver = null;
    try {
      seleniumWebDriver = new SeleniumWebDriver(browser, webDriverPort, gridMode, webDriverVersion);
    } finally {
      Optional.ofNullable(seleniumWebDriver)
          .ifPresent(SeleniumWebDriver::quit); // finish webdriver session
    }
  }

  @Override
  public void onFinish(ISuite suite) {}

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    Object testInstance = method.getTestMethod().getInstance();
    if (runningTests.containsValue(testInstance)) {
      return;
    }

    long currentThreadId = Thread.currentThread().getId();
    if (isNewTestInProgress(testInstance)) {
      preDestroy(runningTests.remove(currentThreadId));
    }

    String testName = testInstance.getClass().getName();

    try {
      LOG.info("Dependencies injection in {}", testName);
      injectDependencies(testResult.getTestContext(), testInstance);
    } catch (Exception e) {
      String errorMessage = "Failed to inject fields in " + testName;
      LOG.error(errorMessage, e);
      throw new TestException(errorMessage, e);
    } finally {
      runningTests.put(currentThreadId, testInstance);
    }
  }

  private boolean isNewTestInProgress(Object testInstance) {
    Thread currentThread = Thread.currentThread();

    return runningTests.containsKey(currentThread.getId())
        && runningTests.get(currentThread.getId()) != testInstance;
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult result) {}

  /** Injects dependencies into the given test class using {@link Guice} and custom injectors. */
  private void injectDependencies(ITestContext testContext, Object testInstance) throws Exception {
    Injector injector = testContext.getSuite().getParentInjector();

    Injector classInjector = injector.createChildInjector(new SeleniumClassModule());
    classInjector.injectMembers(testInstance);

    pageObjectsInjector.injectMembers(testInstance);
  }

  /** Is invoked when test or configuration is finished. */
  private void onTestFinish(ITestResult result) {
    if (result.getStatus() == ITestResult.FAILURE) {
      ofNullable(result.getThrowable()).ifPresent(e -> LOG.error("" + e.getMessage(), e));

      captureScreenshot(result);
    }
  }

  /** Releases resources by invoking methods annotated with {@link PreDestroy} */
  private void preDestroy(Object testInstance) {
    LOG.info("Processing @PreDestroy annotation in {}", testInstance.getClass().getName());

    for (Field field : testInstance.getClass().getDeclaredFields()) {
      field.setAccessible(true);

      Object obj;
      try {
        obj = field.get(testInstance);
      } catch (IllegalAccessException e) {
        LOG.error(
            "Field {} is unaccessable in {}.", field.getName(), testInstance.getClass().getName());
        continue;
      }

      if (obj == null || !hasInjectAnnotation(field)) {
        continue;
      }

      for (Method m : obj.getClass().getMethods()) {
        if (m.isAnnotationPresent(PreDestroy.class)) {
          try {
            m.invoke(obj);
          } catch (Exception e) {
            LOG.error(
                format(
                    "Failed to invoke method %s annotated with @PreDestroy in %s. Test instance: %s",
                    m.getName(), obj.getClass().getName(), testInstance.getClass().getName()),
                e);
          }
        }
      }
    }
  }

  private boolean hasInjectAnnotation(AccessibleObject f) {
    return f.isAnnotationPresent(com.google.inject.Inject.class)
        || f.isAnnotationPresent(javax.inject.Inject.class)
        || f.isAnnotationPresent(InjectTestUser.class)
        || f.isAnnotationPresent(InjectTestWorkspace.class)
        || f.isAnnotationPresent(InjectPageObject.class);
  }

  private void captureScreenshot(ITestResult result) {
    Set<SeleniumWebDriver> webDrivers = new HashSet<>();
    Object testInstance = result.getInstance();

    collectInjectedWebDrivers(testInstance, webDrivers);
    webDrivers.forEach(webDriver -> captureScreenshot(result, webDriver));
  }

  /**
   * Iterates recursively throw all fields and collects instances of {@link SeleniumWebDriver}.
   *
   * @param testInstance the based object to examine
   * @param webDrivers as the result of the method will contain all {@link WebDriver}
   */
  private void collectInjectedWebDrivers(Object testInstance, Set<SeleniumWebDriver> webDrivers) {
    for (Field field : testInstance.getClass().getDeclaredFields()) {
      field.setAccessible(true);

      Object obj;
      try {
        obj = field.get(testInstance);
      } catch (IllegalAccessException e) {
        LOG.error(
            "Field {} is unaccessable in {}.", field.getName(), testInstance.getClass().getName());
        continue;
      }

      if (obj == null) {
        continue;
      }

      Optional<Constructor<?>> injectedConstructor =
          Stream.of(obj.getClass().getConstructors()).filter(this::hasInjectAnnotation).findAny();

      if (!hasInjectAnnotation(field) && !injectedConstructor.isPresent()) {
        continue;
      }

      if (obj instanceof com.google.inject.Provider || obj instanceof javax.inject.Provider) {
        continue;
      }

      if (obj instanceof SeleniumWebDriver) {
        webDrivers.add((SeleniumWebDriver) obj);
      } else {
        collectInjectedWebDrivers(obj, webDrivers);
      }
    }
  }

  private void captureScreenshot(ITestResult result, SeleniumWebDriver webDriver) {
    String testName = result.getTestClass().getName() + "." + result.getMethod().getMethodName();
    String filename = NameGenerator.generate(testName + "_", 8) + ".png";

    try {
      byte[] data = webDriver.getScreenshotAs(OutputType.BYTES);
      Path screenshot = Paths.get(screenshotDir, filename);

      Files.createDirectories(screenshot.getParent());
      Files.copy(new ByteArrayInputStream(data), screenshot);
    } catch (WebDriverException | IOException e) {
      LOG.error(format("Can't capture screenshot for test %s", testName), e);
    }
  }

  /** Cleans up test environment. */
  private void shutdown() {
    LOG.info("Cleaning up test environment...");

    for (Object testInstance : runningTests.values()) {
      preDestroy(testInstance);
    }

    if (testWorkspaceProvider != null) {
      testWorkspaceProvider.shutdown();
    }

    if (defaultTestUser != null) {
      defaultTestUser.delete();
    }
  }

  /** Returns parent injector. */
  public abstract Injector createParentInjector();
}

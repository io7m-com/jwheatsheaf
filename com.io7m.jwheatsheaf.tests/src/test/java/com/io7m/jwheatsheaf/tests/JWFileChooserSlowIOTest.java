/*
 * Copyright © 2020 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.jwheatsheaf.tests;

import com.io7m.jwheatsheaf.api.JWFileChooserAction;
import com.io7m.jwheatsheaf.api.JWFileChooserConfiguration;
import com.io7m.jwheatsheaf.api.JWFileChooserEventType;
import com.io7m.jwheatsheaf.api.JWFileChooserType;
import com.io7m.jwheatsheaf.api.JWFileChoosersType;
import com.io7m.jwheatsheaf.ui.JWFileChoosers;
import com.io7m.jwheatsheaf.ui.internal.JWFileChoosersTesting;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.framework.junit5.Stop;
import org.testfx.matcher.base.NodeMatchers;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@ExtendWith(ApplicationExtension.class)
public final class JWFileChooserSlowIOTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(JWFileChooserSlowIOTest.class);

  private JWTestFilesystems filesystems;
  private FileSystem dosFilesystem;
  private JWFileChooserType chooser;
  private List<JWFileChooserEventType> events;
  private JWFileChoosersType choosers;

  @Start
  public void start(final Stage stage)
    throws Exception
  {
    this.events = Collections.synchronizedList(new ArrayList<>());

    this.filesystems = JWTestFilesystems.create();
    final var systems = this.filesystems.filesystems();
    this.dosFilesystem = systems.get("ExampleDOS");

    final var configuration =
      JWFileChooserConfiguration.builder()
        .setAllowDirectoryCreation(true)
        .setAction(JWFileChooserAction.OPEN_EXISTING_MULTIPLE)
        .setFileSystem(this.dosFilesystem)
        .build();

    this.choosers =
      JWFileChoosers.createWithTesting(
        Executors.newSingleThreadExecutor(),
        JWFileChoosersTesting.builder()
          .setIoDelay(Duration.of(1L, ChronoUnit.SECONDS))
          .build(),
        Locale.getDefault()
      );

    this.chooser = this.choosers.create(stage, configuration);
    this.chooser.setEventListener(event -> this.events.add(event));
    this.chooser.show();
  }

  @Stop
  public void stop()
    throws IOException
  {
    this.choosers.close();
    this.chooser.cancel();
  }

  /**
   * Clicking the first row of the directory table yields a directory, and
   * clicking the OK button selects it.
   *
   * @param robot The FX test robot
   */

  @Test
  public void test_SelectItem_SingleClickFirstRow_CandidateSelected(
    final FxRobot robot,
    final TestInfo info)
    throws TimeoutException
  {
    JWFileWindowTitles.setTitle(this.chooser, info);

    final var delegate = new JWRobotDelegate(robot);

    final var okButton =
      robot.lookup("#fileChooserOKButton")
        .queryButton();

    final var root =
      robot.lookup(".fileChooserSourceList")
        .query();

    final var directoryTable =
      robot.lookup(".fileChooserDirectoryTable")
        .query();

    delegate.waitUntil(() -> Boolean.valueOf(!root.isDisabled()));
    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());

    robot.doubleClickOn("Z:\\");
    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());

    delegate.waitUntil(() -> Boolean.valueOf(!root.isDisabled()));
    robot.clickOn(directoryTable);
    delegate.pauseBriefly();
    robot.clickOn("USERS");

    FxAssert.verifyThat(okButton, NodeMatchers.isEnabled());
    robot.clickOn(okButton);

    Assertions.assertEquals(
      List.of("Z:\\USERS"),
      this.chooser.result()
        .stream()
        .map(Path::toString)
        .collect(Collectors.toList())
    );
    Assertions.assertEquals(0, this.events.size());
  }
}

/*
 * Copyright © 2020 Mark Raynsford <code@io7m.com> http://io7m.com
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
import com.io7m.jwheatsheaf.ui.JWFileChoosers;
import javafx.scene.Node;
import javafx.scene.control.TableRow;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ExtendWith(ApplicationExtension.class)
public final class JWFileChooserTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(JWFileChooserTest.class);

  private JWTestFilesystems filesystems;
  private FileSystem dosFilesystem;
  private FileSystem brokenFilesystem;
  private FileSystem brokenFilesFilesystem;
  private JWFileChooserType chooser;
  private List<Path> selected;
  private List<JWFileChooserEventType> events;

  @Start
  public void start(final Stage stage)
    throws Exception
  {
    this.events = Collections.synchronizedList(new ArrayList<>());

    this.filesystems = JWTestFilesystems.create();
    final var systems = this.filesystems.filesystems();
    this.dosFilesystem = systems.get("ExampleDOS");
    this.brokenFilesystem = systems.get("Broken");
    this.brokenFilesFilesystem = systems.get("BrokenFiles");

    final var configuration =
      JWFileChooserConfiguration.builder()
        .setAllowDirectoryCreation(true)
        .setAction(JWFileChooserAction.OPEN_EXISTING_MULTIPLE)
        .setFileSystem(this.dosFilesystem)
        .build();

    final var choosers = JWFileChoosers.create();
    this.chooser = choosers.create(stage, configuration);
    this.chooser.setEventListener(event -> this.events.add(event));
    this.chooser.show();
  }

  /**
   * Clicking the cancel button yields nothing.
   */

  @Test
  public void testCancel(final FxRobot robot)
  {
    robot.clickOn("#fileChooserCancelButton");
    Assertions.assertEquals(List.of(), this.chooser.result());
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * Clicking the first row of the directory table yields a directory, and
   * clicking the OK button selects it.
   */

  @Test
  public void testDirectorySelect(final FxRobot robot)
  {
    final TableRow<?> row = robot.lookup(".table-row-cell").nth(1).query();
    robot.clickOn(row);
    robot.clickOn("#fileChooserOKButton");

    Assertions.assertEquals(
      List.of("Z:\\USERS\\GROUCH\\DATA.XML"),
      this.chooser.result()
        .stream()
        .map(Path::toString)
        .collect(Collectors.toList()));
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * Navigation via the path menu works.
   */

  @Test
  public void testPathMenuSelect(final FxRobot robot)
    throws Exception
  {
    // Test is fragile when run on Travis CI
    Assumptions.assumeFalse(isTravisCI());

    final var choice =
      robot.lookup("#fileChooserPathMenu").query();

    robot.clickOn(choice)
      .clickOn("Z:\\");

    final TableRow<?> row =
      robot.lookup(".table-row-cell")
        .nth(0)
        .query();

    robot.clickOn(row)
      .clickOn("#fileChooserOKButton");

    Assertions.assertEquals(
      List.of("Z:\\"),
      this.chooser.result()
        .stream()
        .map(Path::toString)
        .collect(Collectors.toList()));
    Assertions.assertEquals(0, this.events.size());
  }

  private static boolean isTravisCI()
  {
    return Objects.equals(System.getenv("TRAVIS"), "true")
      && Objects.equals(System.getenv("CI"), "true");
  }

  /**
   * Navigation via the filesystem root menu succeeds.
   */

  @Test
  public void testSourceMenuSelect(final FxRobot robot)
    throws Exception
  {
    robot.doubleClickOn("Z:\\");

    final TableRow<?> row =
      robot.lookup(".table-row-cell").nth(1).query();

    robot.clickOn(row)
      .clickOn("#fileChooserOKButton");

    Assertions.assertEquals(
      List.of("Z:\\USERS"),
      this.chooser.result()
        .stream()
        .map(Path::toString)
        .collect(Collectors.toList()));
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * Double clicking a directory navigates to that directory.
   */

  @Test
  public void testDirectoryDoubleClick(final FxRobot robot)
    throws IOException
  {
    Files.createDirectories(
      this.dosFilesystem.getPath("Y:\\", "EGG"));
    Files.writeString(
      this.dosFilesystem.getPath("Y:\\", "EGG", "EGG.TXT"),
      "EGG!");

    robot
      .doubleClickOn("Y:\\")
      .doubleClickOn("EGG")
      .clickOn("EGG.TXT")
      .clickOn("#fileChooserOKButton");

    Assertions.assertEquals(
      List.of("Y:\\EGG\\EGG.TXT"),
      this.chooser.result()
        .stream()
        .map(Path::toString)
        .collect(Collectors.toList()));
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * Creating directories works.
   */

  @Test
  public void testDirectoryCreate(final FxRobot robot)
    throws IOException
  {
    robot
      .doubleClickOn("X:\\")
      .clickOn("#fileChooserCreateDirectoryButton")
      .write("CREATED")
      .type(KeyCode.ENTER)
      .clickOn("CREATED")
      .clickOn("#fileChooserOKButton");

    Assertions.assertEquals(
      List.of("X:\\CREATED"),
      this.chooser.result()
        .stream()
        .map(Path::toString)
        .collect(Collectors.toList()));
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * Searching works.
   */

  @Test
  public void testDirectorySearch(final FxRobot robot)
    throws IOException
  {
    robot
      .clickOn("#fileChooserSearchField")
      .write("PHOTO.JPG")
      .clickOn((Node) robot.lookup("PHOTO.JPG").nth(2).query())
      .clickOn("#fileChooserOKButton");

    Assertions.assertEquals(
      List.of("Z:\\USERS\\GROUCH\\PHOTO.JPG"),
      this.chooser.result()
        .stream()
        .map(Path::toString)
        .collect(Collectors.toList()));
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * The up button works.
   */

  @Test
  public void testDirectoryUp(final FxRobot robot)
    throws IOException
  {
    robot
      .clickOn("#fileChooserUpButton")
      .clickOn("#fileChooserUpButton")
      .clickOn("USERS")
      .clickOn("#fileChooserOKButton");

    Assertions.assertEquals(
      List.of("Z:\\USERS"),
      this.chooser.result()
        .stream()
        .map(Path::toString)
        .collect(Collectors.toList()));
    Assertions.assertEquals(0, this.events.size());
  }
}

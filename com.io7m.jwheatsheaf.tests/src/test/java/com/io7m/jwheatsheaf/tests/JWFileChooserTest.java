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
import javafx.scene.control.TableCell;
import javafx.scene.input.KeyCode;
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
        .setHomeDirectory(this.dosFilesystem.getPath("Z:\\HOME"))
        .build();

    this.choosers = JWFileChoosers.create();
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
   * Clicking the cancel button yields nothing.
   *
   * @param robot The FX test robot
   */

  @Test
  public void testCancel(
    final FxRobot robot,
    final TestInfo info)
  {
    JWFileWindowTitles.setTitle(this.chooser, info);

    robot.clickOn("#fileChooserCancelButton");
    Assertions.assertEquals(List.of(), this.chooser.result());
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * Clicking the first row of the directory table yields a directory, and
   * clicking the OK button selects it.
   *
   * @param robot The FX test robot
   */

  @Test
  public void testDirectorySelect(
    final FxRobot robot,
    final TestInfo info)
  {
    JWFileWindowTitles.setTitle(this.chooser, info);

    final var okButton =
      robot.lookup("#fileChooserOKButton")
        .queryButton();

    final var targetCell =
      robot.lookup(n -> n instanceof TableCell)
        .queryAllAs(TableCell.class)
        .stream()
        .filter(cell -> Objects.equals(cell.getText(), "."))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
          "Unable to locate a '.' directory entry")
        );

    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());
    robot.clickOn(targetCell);
    robot.clickOn(targetCell);

    FxAssert.verifyThat(okButton, NodeMatchers.isEnabled());
    robot.clickOn(okButton);

    Assertions.assertEquals(
      List.of("Z:\\USERS\\GROUCH"),
      this.chooser.result()
        .stream()
        .map(Path::toString)
        .collect(Collectors.toList())
    );
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * Navigation via the path menu works.
   *
   * @param robot The FX test robot
   */

  @Test
  public void testPathMenuSelect(
    final FxRobot robot,
    final TestInfo info)
  {
    JWFileWindowTitles.setTitle(this.chooser, info);

    final var delegate = new JWRobotDelegate(robot);

    final var okButton =
      robot.lookup("#fileChooserOKButton")
        .queryButton();
    final var choice =
      robot.lookup("#fileChooserPathMenu")
        .query();

    robot.clickOn(choice);

    final var pathMenuItem =
      robot.lookup(".context-menu")
        .lookup("Z:\\")
        .query();

    LOG.debug("pathMenuItem: {}", pathMenuItem);
    robot.clickOn(pathMenuItem);
    delegate.pauseBriefly();

    final var targetCell =
      robot.lookup(n -> n instanceof TableCell)
        .queryAllAs(TableCell.class)
        .stream()
        .filter(cell -> Objects.equals(cell.getText(), "."))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
          "Unable to locate a '.' directory entry")
        );

    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());
    robot.clickOn(targetCell);
    robot.clickOn(targetCell);

    FxAssert.verifyThat(okButton, NodeMatchers.isEnabled());
    robot.clickOn(okButton);

    Assertions.assertEquals(
      List.of("Z:\\"),
      this.chooser.result()
        .stream()
        .map(Path::toString)
        .collect(Collectors.toList())
    );
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * Navigation via the filesystem root menu succeeds.
   *
   * @param robot The FX test robot
   */

  @Test
  public void testSourceMenuSelect(
    final FxRobot robot,
    final TestInfo info)
  {
    JWFileWindowTitles.setTitle(this.chooser, info);

    final var delegate = new JWRobotDelegate(robot);

    final var okButton =
      robot.lookup("#fileChooserOKButton")
        .queryButton();

    final var rootItem =
      robot.lookup(".fileChooserSourceList")
        .lookup("Z:\\")
        .query();

    LOG.debug("rootItem: {}", rootItem);
    robot.doubleClickOn(rootItem);
    delegate.pauseBriefly();

    final TableCell<?, ?> row =
      robot.lookup(".table-row-cell")
        .lookup("USERS")
        .query();

    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());
    robot.clickOn(row);

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

  /**
   * Double clicking a directory navigates to that directory.
   *
   * @param robot The FX test robot
   *
   * @throws Exception On errors
   */

  @Test
  public void testDirectoryDoubleClick(
    final FxRobot robot,
    final TestInfo info)
    throws Exception
  {
    JWFileWindowTitles.setTitle(this.chooser, info);

    final var delegate = new JWRobotDelegate(robot);

    final var okButton =
      robot.lookup("#fileChooserOKButton")
        .queryButton();

    Files.createDirectories(
      this.dosFilesystem.getPath("Y:\\", "EGG"));
    Files.writeString(
      this.dosFilesystem.getPath("Y:\\", "EGG", "EGG.TXT"),
      "EGG!");

    final var sourceList =
      robot.lookup(".fileChooserSourceList")
        .query();

    final var directoryTable =
      robot.lookup(".fileChooserDirectoryTable")
        .query();

    final var sourceItem =
      robot.lookup(".fileChooserSourceList")
        .lookup("Y:\\")
        .query();

    robot.doubleClickOn(sourceItem);
    delegate.waitUntil(() -> !sourceList.isDisabled());

    robot.doubleClickOn("EGG");
    delegate.waitUntil(() -> !sourceList.isDisabled());

    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());
    robot.clickOn(directoryTable);
    delegate.pauseBriefly();

    robot.clickOn("EGG.TXT");
    delegate.pauseBriefly();

    FxAssert.verifyThat(okButton, NodeMatchers.isEnabled());
    robot.clickOn(okButton);

    Assertions.assertEquals(
      List.of("Y:\\EGG\\EGG.TXT"),
      this.chooser.result()
        .stream()
        .map(Path::toString)
        .collect(Collectors.toList())
    );
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * Creating directories works.
   *
   * @param robot The FX test robot
   */

  @Test
  public void testDirectoryCreate(
    final FxRobot robot,
    final TestInfo info)
  {
    JWFileWindowTitles.setTitle(this.chooser, info);

    final var okButton =
      robot.lookup("#fileChooserOKButton")
        .queryButton();

    final var rootItem =
      robot.lookup(".fileChooserSourceList")
        .lookup("X:\\")
        .query();

    robot.doubleClickOn(rootItem);

    robot.clickOn("#fileChooserCreateDirectoryButton");
    robot.write("CREATED");
    robot.type(KeyCode.ENTER);

    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());
    robot.clickOn("CREATED");
    FxAssert.verifyThat(okButton, NodeMatchers.isEnabled());
    robot.clickOn("#fileChooserOKButton");

    Assertions.assertEquals(
      List.of("X:\\CREATED"),
      this.chooser.result()
        .stream()
        .map(Path::toString)
        .collect(Collectors.toList())
    );
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * Searching works.
   *
   * @param robot The FX test robot
   */

  @Test
  public void testDirectorySearch(
    final FxRobot robot,
    final TestInfo info)
  {
    JWFileWindowTitles.setTitle(this.chooser, info);

    final var delegate = new JWRobotDelegate(robot);

    final var okButton =
      robot.lookup("#fileChooserOKButton")
        .queryButton();

    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());

    final var searchField =
      robot.lookup("#fileChooserSearchField")
        .query();
    robot.clickOn(searchField);

    robot.write("PHOTO.JPG");

    final var tableItem =
      robot.lookup("PHOTO.JPG")
        .match(node -> node instanceof TableCell)
        .query();

    LOG.debug("testDirectorySearch: {}", tableItem);
    robot.clickOn(tableItem);
    delegate.pauseBriefly();
    FxAssert.verifyThat(okButton, NodeMatchers.isEnabled());
    robot.clickOn(okButton);

    Assertions.assertEquals(
      List.of("Z:\\USERS\\GROUCH\\PHOTO.JPG"),
      this.chooser.result()
        .stream()
        .map(Path::toString)
        .collect(Collectors.toList())
    );
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * The up button works.
   *
   * @param robot The FX test robot
   */

  @Test
  public void testDirectoryUp(
    final FxRobot robot,
    final TestInfo info)
  {
    JWFileWindowTitles.setTitle(this.chooser, info);

    final var delegate = new JWRobotDelegate(robot);

    final var okButton =
      robot.lookup("#fileChooserOKButton")
        .queryButton();

    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());

    robot.clickOn("#fileChooserUpButton");
    delegate.pauseBriefly();

    robot.clickOn("#fileChooserUpButton");
    delegate.pauseBriefly();

    final var targetCell =
      robot.lookup(n -> n instanceof TableCell)
        .queryAllAs(TableCell.class)
        .stream()
        .filter(cell -> Objects.equals(cell.getText(), "USERS"))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
          "Unable to locate a 'USERS' directory entry")
        );

    robot.clickOn(targetCell);
    delegate.pauseBriefly();
    robot.clickOn(targetCell);

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

  /**
   * The home button works.
   *
   * @param robot The FX test robot
   */

  @Test
  public void testDirectoryHome(
    final FxRobot robot,
    final TestInfo info)
  {
    JWFileWindowTitles.setTitle(this.chooser, info);

    final var delegate = new JWRobotDelegate(robot);

    final var okButton =
      robot.lookup("#fileChooserOKButton")
        .queryButton();

    final var homeButton =
      robot.lookup("#fileChooserHomeButton")
        .queryButton();

    robot.clickOn(homeButton);

    final var targetCell =
      robot.lookup(n -> n instanceof TableCell)
        .queryAllAs(TableCell.class)
        .stream()
        .filter(cell -> Objects.equals(cell.getText(), "FILE.TXT"))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
          "Unable to locate a 'FILE.TXT' directory entry")
        );

    robot.clickOn(targetCell);
    delegate.pauseBriefly();

    FxAssert.verifyThat(okButton, NodeMatchers.isEnabled());
    robot.clickOn(okButton);

    Assertions.assertEquals(
      List.of("Z:\\HOME\\FILE.TXT"),
      this.chooser.result()
        .stream()
        .map(Path::toString)
        .collect(Collectors.toList())
    );
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * Pressing escape closes the dialog without selecting a file.
   *
   * @param robot The FX test robot
   */

  @Test
  public void testEscapeCloses(
    final FxRobot robot,
    final TestInfo info)
  {
    JWFileWindowTitles.setTitle(this.chooser, info);

    final var rootItem =
      robot.lookup(".fileChooserSourceList")
        .query();

    robot.clickOn(rootItem);
    robot.type(KeyCode.ESCAPE);

    Assertions.assertEquals(
      List.of(),
      this.chooser.result()
    );
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * Sorting by type works.
   *
   * @param robot The FX test robot
   */

  @Test
  public void testSortType(
    final FxRobot robot,
    final TestInfo info)
  {
    JWFileWindowTitles.setTitle(this.chooser, info);

    final var delegate = new JWRobotDelegate(robot);

    final var okButton =
      robot.lookup("#fileChooserOKButton")
        .queryButton();

    final var column =
      robot.lookup("#fileChooserTableColumnType")
        .query();

    delegate.pauseBriefly();
    robot.clickOn(column);
    robot.clickOn(column);

    final var targetCell =
      robot.lookup(n -> n instanceof TableCell)
        .nth(0)
        .queryAs(TableCell.class);

    delegate.pauseBriefly();
    robot.clickOn(targetCell);
    delegate.pauseBriefly();

    FxAssert.verifyThat(okButton, NodeMatchers.isEnabled());
    robot.clickOn(okButton);

    Assertions.assertEquals(
      List.of("Z:\\USERS\\GROUCH\\README.TXT"),
      this.chooser.result()
        .stream()
        .map(Path::toString)
        .collect(Collectors.toList())
    );
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * Sorting by name works.
   *
   * @param robot The FX test robot
   */

  @Test
  public void testSortName(
    final FxRobot robot,
    final TestInfo info)
  {
    JWFileWindowTitles.setTitle(this.chooser, info);

    final var delegate = new JWRobotDelegate(robot);

    final var okButton =
      robot.lookup("#fileChooserOKButton")
        .queryButton();

    final var column =
      robot.lookup("#fileChooserTableColumnName")
        .query();

    delegate.pauseBriefly();
    robot.clickOn(column);
    robot.clickOn(column);

    final var targetCell =
      robot.lookup(n -> n instanceof TableCell)
        .nth(0)
        .queryAs(TableCell.class);

    delegate.pauseBriefly();
    robot.clickOn(targetCell);
    delegate.pauseBriefly();

    FxAssert.verifyThat(okButton, NodeMatchers.isEnabled());
    robot.clickOn(okButton);

    Assertions.assertEquals(
      List.of("Z:\\USERS\\GROUCH\\README.TXT"),
      this.chooser.result()
        .stream()
        .map(Path::toString)
        .collect(Collectors.toList())
    );
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * Sorting by time works.
   *
   * @param robot The FX test robot
   */

  @Test
  public void testSortTime(
    final FxRobot robot,
    final TestInfo info)
  {
    JWFileWindowTitles.setTitle(this.chooser, info);

    final var delegate = new JWRobotDelegate(robot);

    final var okButton =
      robot.lookup("#fileChooserOKButton")
        .queryButton();

    final var column =
      robot.lookup("#fileChooserTableColumnModified")
        .query();

    delegate.pauseBriefly();
    robot.clickOn(column);
    robot.clickOn(column);

    final var targetCell =
      robot.lookup(n -> n instanceof TableCell)
        .nth(0)
        .queryAs(TableCell.class);

    delegate.pauseBriefly();
    robot.clickOn(targetCell);
    delegate.pauseBriefly();

    FxAssert.verifyThat(okButton, NodeMatchers.isEnabled());
    robot.clickOn(okButton);

    Assertions.assertEquals(
      List.of("Z:\\USERS\\GROUCH\\PHOTO.JPG"),
      this.chooser.result()
        .stream()
        .map(Path::toString)
        .collect(Collectors.toList())
    );
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * Sorting by size works.
   *
   * @param robot The FX test robot
   */

  @Test
  public void testSortSize(
    final FxRobot robot,
    final TestInfo info)
  {
    JWFileWindowTitles.setTitle(this.chooser, info);

    final var delegate = new JWRobotDelegate(robot);

    final var okButton =
      robot.lookup("#fileChooserOKButton")
        .queryButton();

    final var column =
      robot.lookup("#fileChooserTableColumnSize")
        .query();

    delegate.pauseBriefly();
    robot.clickOn(column);
    robot.clickOn(column);

    final var targetCell =
      robot.lookup(n -> n instanceof TableCell)
        .nth(0)
        .queryAs(TableCell.class);

    delegate.pauseBriefly();
    robot.clickOn(targetCell);
    delegate.pauseBriefly();

    FxAssert.verifyThat(okButton, NodeMatchers.isEnabled());
    robot.clickOn(okButton);

    Assertions.assertEquals(
      List.of("Z:\\USERS\\GROUCH\\DATA.XML"),
      this.chooser.result()
        .stream()
        .map(Path::toString)
        .collect(Collectors.toList())
    );
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * Typing a name into the name field and pressing return selects an item and
   * requests focus for the OK button. Pressing return again presses the button.
   *
   * @param robot The FX test robot
   */

  @Test
  public void test_TypeNameIntoFieldAndReturn_CandidateSelected(
    final FxRobot robot,
    final TestInfo info)
  {
    JWFileWindowTitles.setTitle(this.chooser, info);

    final var delegate = new JWRobotDelegate(robot);

    final var okButton =
      robot.lookup("#fileChooserOKButton")
        .queryButton();

    final var fileField =
      robot.lookup("#fileChooserNameField")
        .query();

    robot.clickOn(fileField);
    robot.write("DATA.XML");
    robot.type(KeyCode.ENTER);
    delegate.pauseBriefly();

    FxAssert.verifyThat(okButton, NodeMatchers.isEnabled());
    robot.type(KeyCode.ENTER);

    Assertions.assertEquals(
      List.of("Z:\\USERS\\GROUCH\\DATA.XML"),
      this.chooser.result()
        .stream()
        .map(Path::toString)
        .collect(Collectors.toList())
    );
    Assertions.assertEquals(0, this.events.size());
  }
}

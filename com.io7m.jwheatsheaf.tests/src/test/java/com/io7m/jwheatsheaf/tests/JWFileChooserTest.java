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
import com.io7m.jwheatsheaf.api.JWFileChoosersType;
import com.io7m.jwheatsheaf.ui.JWFileChoosers;
import com.io7m.percentpass.extension.MinimumPassing;
import com.io7m.xoanon.commander.api.XCCommanderType;
import com.io7m.xoanon.commander.api.XCRobotType;
import com.io7m.xoanon.extension.XoExtension;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.input.KeyCode;
import javafx.stage.PopupWindow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.io7m.jwheatsheaf.tests.JWTestUtilities.TIMEOUT;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.assertIsSelected;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.createChooser;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.findCancelButton;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.findDirectoryCreateButton;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.findDirectoryTable;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.findHomeButton;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.findNameField;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.findOKButton;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.findPathMenu;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.findSearchField;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.findSourceList;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.findUpButton;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.writeString;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.stage.Window.getWindows;
import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(XoExtension.class)
public final class JWFileChooserTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(JWFileChooserTest.class);

  private JWTestFilesystems filesystems;
  private FileSystem dosFilesystem;
  private List<JWFileChooserEventType> events;
  private JWFileChoosersType choosers;
  private JWFileChooserConfiguration configuration;

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.events =
      Collections.synchronizedList(new ArrayList<>());

    this.filesystems =
      JWTestFilesystems.create();
    final var systems =
      this.filesystems.filesystems();
    this.dosFilesystem =
      systems.get("ExampleDOS");

    this.configuration =
      JWFileChooserConfiguration.builder()
        .setAllowDirectoryCreation(true)
        .setAction(JWFileChooserAction.OPEN_EXISTING_MULTIPLE)
        .setFileSystem(this.dosFilesystem)
        .setHomeDirectory(this.dosFilesystem.getPath("Z:\\HOME"))
        .build();

    this.choosers = JWFileChoosers.create();
  }

  @AfterEach
  public void tearDown()
    throws IOException
  {
    this.choosers.close();
  }

  /**
   * Clicking the cancel button yields nothing.
   */

  @MinimumPassing(executionCount = 5, passMinimum = 4)
  public void testCancel(
    final XCCommanderType commander,
    final XCRobotType robot)
    throws Exception
  {
    final var chooser =
      createChooser(this.choosers, this.configuration, commander);
    final var window =
      chooser.stage();

    robot.click(findCancelButton(robot, window));
    robot.waitForStageToClose(window, TIMEOUT);

    assertEquals(List.of(), chooser.result());
    assertEquals(0, this.events.size());
  }

  /**
   * Clicking the first row of the directory table yields a directory, and
   * clicking the OK button selects it.
   */

  @MinimumPassing(executionCount = 5, passMinimum = 4)
  public void testDirectorySelect(
    final XCCommanderType commander,
    final XCRobotType robot)
    throws Exception
  {
    final var chooser =
      createChooser(this.choosers, this.configuration, commander);
    final var window =
      chooser.stage();

    final var okButton =
      findOKButton(robot, window);
    final var directoryTable =
      findDirectoryTable(robot, window);

    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());

    final var targetCell =
      robot.findAll(TableCell.class, directoryTable)
        .stream()
        .filter(cell -> Objects.equals(cell.getText(), "."))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
          "Unable to locate a '.' directory entry")
        );

    robot.waitUntil(TIMEOUT, okButton::isDisabled);
    robot.click(targetCell);
    robot.click(targetCell);

    robot.waitUntil(TIMEOUT, () -> !okButton.isDisabled());
    robot.click(okButton);
    robot.waitForStageToClose(window, TIMEOUT);

    assertIsSelected(chooser, "Z:\\USERS\\GROUCH");
    assertEquals(0, this.events.size());
  }

  /**
   * Navigation via the path menu works.
   */

  @MinimumPassing(executionCount = 5, passMinimum = 4)
  public void testPathMenuSelect(
    final XCCommanderType commander,
    final XCRobotType robot)
    throws Exception
  {
    final var chooser =
      createChooser(this.choosers, this.configuration, commander);
    final var window =
      chooser.stage();

    final var okButton =
      findOKButton(robot, window);
    final var directoryTable =
      findDirectoryTable(robot, window);
    final var choice =
      findPathMenu(robot, window);

    robot.click(choice);

    final var popup =
      robot.evaluate(() -> {
        return getWindows()
          .stream()
          .filter(w -> w instanceof PopupWindow)
          .map(PopupWindow.class::cast)
          .findFirst()
          .orElseThrow()
          .getScene()
          .getRoot();
      });

    final var pathMenuItem =
      robot.findWithText(popup, "Z:\\");

    LOG.debug("pathMenuItem: {}", pathMenuItem);
    robot.click(pathMenuItem);

    final var targetCell =
      robot.findAll(TableCell.class, directoryTable)
        .stream()
        .filter(cell -> Objects.equals(cell.getText(), "."))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
          "Unable to locate a '.' directory entry")
        );

    robot.waitUntil(TIMEOUT, okButton::isDisabled);
    robot.click(targetCell);
    robot.click(targetCell);

    robot.waitUntil(TIMEOUT, () -> !okButton.isDisabled());
    robot.click(okButton);
    robot.waitForStageToClose(window, TIMEOUT);

    assertIsSelected(chooser, "Z:\\");
    assertEquals(0, this.events.size());
  }

  /**
   * Navigation via the filesystem root menu succeeds.
   */

  @MinimumPassing(executionCount = 5, passMinimum = 4)
  public void testSourceMenuSelect(
    final XCCommanderType commander,
    final XCRobotType robot)
    throws Exception
  {
    final var chooser =
      createChooser(this.choosers, this.configuration, commander);
    final var window =
      chooser.stage();

    final var okButton =
      findOKButton(robot, window);
    final var sourceList =
      findSourceList(robot, window);
    final var directoryTable =
      findDirectoryTable(robot, window);

    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());

    final var rootItem =
      robot.findWithText(sourceList, "Z:\\");

    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());

    LOG.debug("rootItem: {}", rootItem);
    robot.pointAt(rootItem);
    robot.doubleClick(rootItem);

    final TableCell<?, ?> row =
      robot.findWithText(TableCell.class, directoryTable, "USERS");

    robot.waitUntil(TIMEOUT, okButton::isDisabled);
    robot.pointAt(row);
    robot.click(row);

    robot.waitUntil(TIMEOUT, () -> !okButton.isDisabled());
    robot.click(okButton);
    robot.waitForStageToClose(window, TIMEOUT);

    assertIsSelected(chooser, "Z:\\USERS");
    assertEquals(0, this.events.size());
  }

  /**
   * Double-clicking a directory navigates to that directory.
   */

  @MinimumPassing(executionCount = 5, passMinimum = 4)
  public void testDirectoryDoubleClick(
    final XCCommanderType commander,
    final XCRobotType robot)
    throws Exception
  {
    final var chooser =
      createChooser(this.choosers, this.configuration, commander);
    final var window =
      chooser.stage();

    final var okButton =
      findOKButton(robot, window);
    final var sourceList =
      findSourceList(robot, window);
    final var directoryTable =
      findDirectoryTable(robot, window);

    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());

    createDirectories(
      this.dosFilesystem.getPath("Y:\\", "EGG"));
    writeString(
      this.dosFilesystem.getPath("Y:\\", "EGG", "EGG.TXT"),
      "EGG!");

    robot.waitUntil(TIMEOUT, () -> !sourceList.isDisabled());
    final var sourceItem = robot.findWithText(sourceList, "Y:\\");
    robot.pointAt(sourceItem);
    robot.doubleClick(sourceItem);

    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());
    final var egg = robot.findWithText(directoryTable, "EGG");
    robot.pointAt(egg);
    robot.doubleClick(egg);
    robot.waitUntil(TIMEOUT, () -> !sourceList.isDisabled());

    robot.waitUntil(TIMEOUT, okButton::isDisabled);

    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());
    robot.pointAt(directoryTable);
    robot.click(directoryTable);

    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());
    final var eggText = robot.findWithText(directoryTable, "EGG.TXT");
    robot.click(eggText);
    robot.waitUntil(TIMEOUT, () -> !okButton.isDisabled());
    robot.click(okButton);
    robot.waitForStageToClose(window, TIMEOUT);

    assertIsSelected(chooser, "Y:\\EGG\\EGG.TXT");
    assertEquals(0, this.events.size());
  }

  /**
   * Creating directories works.
   *
   * @param robot The FX test robot
   */

  @MinimumPassing(executionCount = 5, passMinimum = 4)
  public void testDirectoryCreate(
    final XCCommanderType commander,
    final XCRobotType robot)
    throws Exception
  {
    final var chooser =
      createChooser(this.choosers, this.configuration, commander);
    final var window =
      chooser.stage();

    final var okButton =
      findOKButton(robot, window);
    final var sourceList =
      findSourceList(robot, window);
    final var createButton =
      findDirectoryCreateButton(robot, window);
    final var directoryTable =
      findDirectoryTable(robot, window);

    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());

    final var rootItem = robot.findWithText(sourceList, "X:\\");
    robot.pointAt(rootItem);
    robot.doubleClick(rootItem);

    robot.waitUntil(TIMEOUT, () -> !createButton.isDisabled());
    robot.pointAt(createButton);
    robot.click(createButton);
    robot.typeText("CREATED");
    robot.typeRaw(ENTER);

    robot.waitUntil(TIMEOUT, okButton::isDisabled);
    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());

    final var created = robot.findWithText(directoryTable, "CREATED");
    robot.pointAt(created);
    robot.click(created);

    robot.waitUntil(TIMEOUT, () -> !okButton.isDisabled());
    robot.pointAt(okButton);
    robot.click(okButton);
    robot.waitForStageToClose(window, TIMEOUT);

    assertIsSelected(chooser, "X:\\CREATED");
    assertEquals(0, this.events.size());
  }

  /**
   * Searching works.
   */

  @MinimumPassing(executionCount = 5, passMinimum = 4)
  public void testDirectorySearch(
    final XCCommanderType commander,
    final XCRobotType robot)
    throws Exception
  {
    final var chooser =
      createChooser(this.choosers, this.configuration, commander);
    final var window =
      chooser.stage();

    final var okButton =
      findOKButton(robot, window);
    final var directoryTable =
      findDirectoryTable(robot, window);
    final var searchField =
      findSearchField(robot, window);

    robot.waitUntil(TIMEOUT, okButton::isDisabled);

    robot.click(searchField);
    robot.typeText(searchField, "PHOTO.JPG");

    final var tableItem =
      robot.findWithText(TableCell.class, directoryTable, "PHOTO.JPG");

    LOG.debug("testDirectorySearch: {}", tableItem);
    robot.click(tableItem);

    robot.waitUntil(TIMEOUT, () -> !okButton.isDisabled());
    robot.click(okButton);
    robot.waitForStageToClose(window, TIMEOUT);

    assertIsSelected(chooser, "Z:\\USERS\\GROUCH\\PHOTO.JPG");
    assertEquals(0, this.events.size());
  }

  /**
   * The up button works.
   */

  @MinimumPassing(executionCount = 5, passMinimum = 4)
  public void testDirectoryUp(
    final XCCommanderType commander,
    final XCRobotType robot)
    throws Exception
  {
    final var chooser =
      createChooser(this.choosers, this.configuration, commander);
    final var window =
      chooser.stage();

    final var okButton =
      findOKButton(robot, window);
    final var upButton =
      findUpButton(robot, window);
    final var directoryTable =
      findDirectoryTable(robot, window);

    robot.waitUntil(TIMEOUT, okButton::isDisabled);
    robot.click(upButton);
    robot.waitUntil(TIMEOUT, () -> !upButton.isDisabled());
    robot.click(upButton);

    final var targetCell =
      robot.findAll(TableCell.class, directoryTable)
        .stream()
        .filter(cell -> Objects.equals(cell.getText(), "USERS"))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
          "Unable to locate a 'USERS' directory entry")
        );

    robot.click(targetCell);

    robot.waitUntil(TIMEOUT, () -> !okButton.isDisabled());
    robot.waitUntil(TIMEOUT, () -> !okButton.isDisabled());
    robot.click(okButton);
    robot.waitForStageToClose(window, TIMEOUT);

    assertIsSelected(chooser, "Z:\\USERS");
    assertEquals(0, this.events.size());
  }

  /**
   * The home button works.
   */

  @MinimumPassing(executionCount = 5, passMinimum = 4)
  public void testDirectoryHome(
    final XCCommanderType commander,
    final XCRobotType robot)
    throws Exception
  {
    final var chooser =
      createChooser(this.choosers, this.configuration, commander);
    final var window =
      chooser.stage();

    final var okButton =
      findOKButton(robot, window);
    final var directoryTable =
      findDirectoryTable(robot, window);
    final var homeButton =
      findHomeButton(robot, window);

    robot.waitUntil(TIMEOUT, () -> !homeButton.isDisabled());
    robot.click(homeButton);

    final var targetCell =
      robot.findAll(TableCell.class, directoryTable)
        .stream()
        .filter(cell -> Objects.equals(cell.getText(), "FILE.TXT"))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
          "Unable to locate a 'FILE.TXT' directory entry")
        );

    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());
    robot.click(targetCell);

    robot.waitUntil(TIMEOUT, () -> !okButton.isDisabled());
    robot.click(okButton);
    robot.waitForStageToClose(window, TIMEOUT);

    assertIsSelected(chooser, "Z:\\HOME\\FILE.TXT");
    assertEquals(0, this.events.size());
  }

  /**
   * Pressing escape closes the dialog without selecting a file.
   */

  @MinimumPassing(executionCount = 5, passMinimum = 4)
  public void testEscapeCloses(
    final XCCommanderType commander,
    final XCRobotType robot)
    throws Exception
  {
    final var chooser =
      createChooser(this.choosers, this.configuration, commander);
    final var window =
      chooser.stage();

    final var sourceList =
      findSourceList(robot, window);

    robot.click(sourceList);
    robot.typeRaw(KeyCode.ESCAPE);
    robot.waitForStageToClose(window, TIMEOUT);

    assertEquals(List.of(), chooser.result());
    assertEquals(0, this.events.size());
  }

  /**
   * Sorting by type works.
   */

  @MinimumPassing(executionCount = 5, passMinimum = 4)
  public void testSortType(
    final XCCommanderType commander,
    final XCRobotType robot)
    throws Exception
  {
    final var chooser =
      createChooser(this.choosers, this.configuration, commander);
    final var window =
      chooser.stage();

    final var okButton =
      findOKButton(robot, window);
    final var directoryTable =
      findDirectoryTable(robot, window);

    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());

    final var column =
      robot.findWithId(directoryTable, "fileChooserTableColumnType");

    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());
    robot.pointAt(column);
    robot.click(column);

    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());
    robot.pointAt(column);
    robot.click(column);

    final var targetCell =
      robot.findAll(TableCell.class, directoryTable)
        .stream()
        .findFirst()
        .orElseThrow();

    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());
    robot.pointAt(targetCell);
    robot.click(targetCell);

    robot.waitUntil(TIMEOUT, () -> !okButton.isDisabled());
    robot.click(okButton);
    robot.waitForStageToClose(window, TIMEOUT);

    assertIsSelected(chooser, "Z:\\USERS\\GROUCH\\README.TXT");
    assertEquals(0, this.events.size());
  }

  /**
   * Sorting by name works.
   */

  @MinimumPassing(executionCount = 5, passMinimum = 4)
  public void testSortName(
    final XCCommanderType commander,
    final XCRobotType robot)
    throws Exception
  {
    final var chooser =
      createChooser(this.choosers, this.configuration, commander);
    final var window =
      chooser.stage();

    final var okButton =
      findOKButton(robot, window);
    final var directoryTable =
      findDirectoryTable(robot, window);

    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());

    final var column =
      robot.findWithId(directoryTable, "fileChooserTableColumnName");

    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());
    robot.click(column);
    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());
    robot.click(column);

    final var targetCell =
      robot.findAll(TableCell.class, directoryTable)
        .stream()
        .findFirst()
        .orElseThrow();

    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());
    robot.click(targetCell);

    robot.waitUntil(TIMEOUT, () -> !okButton.isDisabled());
    robot.click(okButton);
    robot.waitForStageToClose(window, TIMEOUT);

    assertIsSelected(chooser, "Z:\\USERS\\GROUCH\\README.TXT");
    assertEquals(0, this.events.size());
  }

  /**
   * Sorting by time works.
   */

  @MinimumPassing(executionCount = 5, passMinimum = 4)
  public void testSortTime(
    final XCCommanderType commander,
    final XCRobotType robot)
    throws Exception
  {
    final var chooser =
      createChooser(this.choosers, this.configuration, commander);
    final var window =
      chooser.stage();

    final var okButton =
      findOKButton(robot, window);
    final var directoryTable =
      findDirectoryTable(robot, window);

    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());

    final var column =
      robot.findWithId(directoryTable, "fileChooserTableColumnModified");

    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());
    robot.click(column);
    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());
    robot.click(column);

    final var targetCell =
      robot.findAll(TableCell.class, directoryTable)
        .stream()
        .findFirst()
        .orElseThrow();

    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());
    robot.click(targetCell);

    robot.waitUntil(TIMEOUT, () -> !okButton.isDisabled());
    robot.click(okButton);
    robot.waitForStageToClose(window, TIMEOUT);

    assertIsSelected(chooser, "Z:\\USERS\\GROUCH\\PHOTO.JPG");
    assertEquals(0, this.events.size());
  }

  /**
   * Sorting by size works.
   */

  @MinimumPassing(executionCount = 5, passMinimum = 4)
  public void testSortSize(
    final XCCommanderType commander,
    final XCRobotType robot)
    throws Exception
  {
    final var chooser =
      createChooser(this.choosers, this.configuration, commander);
    final var window =
      chooser.stage();

    final var okButton =
      findOKButton(robot, window);
    final var directoryTable =
      findDirectoryTable(robot, window);

    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());

    final var column =
      robot.findWithId(directoryTable, "fileChooserTableColumnSize");

    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());
    robot.click(column);
    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());
    robot.click(column);

    final var targetCell =
      robot.findAll(TableCell.class, directoryTable)
        .stream()
        .findFirst()
        .orElseThrow();

    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());
    robot.click(targetCell);

    robot.waitUntil(TIMEOUT, () -> !okButton.isDisabled());
    robot.click(okButton);
    robot.waitForStageToClose(window, TIMEOUT);

    assertIsSelected(chooser, "Z:\\USERS\\GROUCH\\DATA.XML");
    assertEquals(0, this.events.size());
  }

  /**
   * Typing a name into the name field and pressing return selects an item and
   * requests focus for the OK button. Pressing return again presses the button.
   */

  @MinimumPassing(executionCount = 5, passMinimum = 4)
  public void test_TypeNameIntoFieldAndReturn_CandidateSelected(
    final XCCommanderType commander,
    final XCRobotType robot)
    throws Exception
  {
    final var chooser =
      createChooser(this.choosers, this.configuration, commander);
    final var window =
      chooser.stage();

    final var okButton =
      findOKButton(robot, window);
    final var fileField =
      findNameField(robot, window);

    robot.click(fileField);
    robot.typeText(fileField, "DATA.XML");
    robot.typeRaw(ENTER);

    robot.waitUntil(TIMEOUT, () -> !okButton.isDisabled());
    robot.typeRaw(ENTER);
    robot.waitForStageToClose(window, TIMEOUT);

    assertIsSelected(chooser, "Z:\\USERS\\GROUCH\\DATA.XML");
    assertEquals(0, this.events.size());
  }
}

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
import javafx.scene.control.TableCell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.io7m.jwheatsheaf.tests.JWTestUtilities.TIMEOUT;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.assertIsSelected;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.assertNothingSelected;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.createChooser;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.findDirectoryTable;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.findOKButton;

/**
 * Verify that DOC, which represents a directory, may be selected.
 */

@ExtendWith(XoExtension.class)
public final class JWFileChooserDirectoryModeTest
{
  private List<JWFileChooserEventType> events;
  private JWFileChoosersType choosers;
  private JWFileChooserConfiguration configuration;

  @BeforeAll
  public static void beforeAll()
  {
    JWTestUtilities.publishApplicationInfo();
  }

  @BeforeEach
  public void setup()
    throws Exception
  {
    this.events = Collections.synchronizedList(new ArrayList<>());

    final JWTestFilesystems filesystems = JWTestFilesystems.create();
    final var systems = filesystems.filesystems();
    final FileSystem dosFilesystem = systems.get("ExampleDOS");

    this.configuration =
      JWFileChooserConfiguration.builder()
        .setAllowDirectoryCreation(true)
        .setAction(JWFileChooserAction.OPEN_EXISTING_SINGLE)
        .setFileSystem(dosFilesystem)
        .setFileSelectionMode(path -> path.getFileName().toString().equals("DOC"))
        .build();

    this.choosers = JWFileChoosers.create();
  }

  @AfterEach
  public void afterEach()
    throws IOException
  {
    this.choosers.close();
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * Clicking a file in the directory listing selects that file as a
   * candidate for opening.
   */

  @MinimumPassing(executionCount = 5, passMinimum = 4)
  public void test_DirectoryMode_SingleClickDirectory_CandidateSelected(
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

    robot.waitUntil(TIMEOUT, okButton::isDisabled);

    final var docCell =
      robot.findWithText(TableCell.class, directoryTable, "DOC");

    robot.click(directoryTable);
    robot.click(docCell);

    robot.waitUntil(TIMEOUT, () -> !okButton.isDisabled());
    robot.click(okButton);
    robot.waitForStageToClose(window, TIMEOUT);

    assertIsSelected(chooser, "Z:\\USERS\\GROUCH\\DOC");
  }

  /**
   * Double-clicking a file in the directory listing does nothing.
   */

  @MinimumPassing(executionCount = 5, passMinimum = 4)
  public void test_DirectoryMode_SingleClickFile_NothingSelected(
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
    robot.pointAt(directoryTable);
    robot.click(directoryTable);

    final var docCell =
      robot.findWithText(TableCell.class, directoryTable, "DATA.XML");

    robot.pointAt(docCell);
    robot.doubleClick(docCell);
    robot.waitUntil(TIMEOUT, okButton::isDisabled);
    robot.click(okButton);

    assertNothingSelected(chooser);
  }

  /**
   * Double-clicking a directory navigates to that directory.
   */

  @MinimumPassing(executionCount = 5, passMinimum = 4)
  public void test_DirectoryMode_DoubleClickDirectory_DirectoryNavigated(
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

    final var docCell =
      robot.findWithText(TableCell.class, directoryTable, "DOC");

    robot.pointAt(docCell);
    robot.doubleClick(docCell);
    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());

    final var dotCell =
      robot.findWithText(TableCell.class, directoryTable, ".");

    robot.pointAt(dotCell);
    robot.click(dotCell);

    robot.waitUntil(TIMEOUT, () -> !okButton.isDisabled());
    robot.click(okButton);
    robot.waitForStageToClose(window, TIMEOUT);

    assertIsSelected(chooser, "Z:\\USERS\\GROUCH\\DOC");
  }
}

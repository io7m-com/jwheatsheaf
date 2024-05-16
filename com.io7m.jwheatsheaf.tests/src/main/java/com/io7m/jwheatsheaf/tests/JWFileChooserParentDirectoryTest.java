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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.io7m.jwheatsheaf.tests.JWTestUtilities.TIMEOUT;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.assertIsSelected;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.createChooser;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.findDirectoryTable;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.findNameField;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.findOKButton;

@ExtendWith(XoExtension.class)
public final class JWFileChooserParentDirectoryTest
{
  private List<JWFileChooserEventType> events;
  private JWFileChoosersType choosers;
  private JWFileChooserConfiguration configuration;

  @BeforeEach
  public void setup()
    throws Exception
  {
    this.events = Collections.synchronizedList(new ArrayList<>());

    final JWTestFilesystems filesystems =
      JWTestFilesystems.create();
    final var systems =
      filesystems.filesystems();
    final FileSystem dosFilesystem =
      systems.get("ExampleDOS");

    this.configuration =
      JWFileChooserConfiguration.builder()
        .setAllowDirectoryCreation(true)
        .setShowParentDirectory(true)
        .setAction(JWFileChooserAction.OPEN_EXISTING_SINGLE)
        .setFileSystem(dosFilesystem)
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
   * Clicking the ".." directory entry allows for navigating to the parent
   * directory.
   */

  @MinimumPassing(executionCount = 5, passMinimum = 4)
  public void test_ParentDirectory_DoubleClickParent_NavigatesToParent(
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
    final var nameField =
      findNameField(robot, window);

    robot.waitUntil(TIMEOUT, window::isShowing);
    robot.waitUntil(TIMEOUT, window::isFocused);
    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());

    final var dotDot =
      robot.findWithText(directoryTable, "..");

    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());
    robot.pointAt(dotDot);
    robot.doubleClick(dotDot);
    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());

    final var dot =
      robot.findWithText(directoryTable, ".");

    robot.waitUntil(TIMEOUT, () -> !directoryTable.isDisabled());
    robot.pointAt(dot);
    robot.click(dot);
    robot.waitUntil(TIMEOUT, () -> Objects.equals(nameField.getText(), "."));
    robot.waitUntil(TIMEOUT, () -> !okButton.isDisabled());

    robot.click(okButton);
    robot.waitForStageToClose(window, TIMEOUT);

    assertIsSelected(chooser, "Z:\\USERS");
  }
}

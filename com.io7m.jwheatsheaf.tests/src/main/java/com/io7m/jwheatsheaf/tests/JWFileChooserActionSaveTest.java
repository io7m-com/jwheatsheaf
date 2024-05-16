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
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.stage.Window;
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
import java.util.Objects;

import static com.io7m.jwheatsheaf.tests.JWTestUtilities.TIMEOUT;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.assertIsSelected;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.createChooser;
import static javafx.scene.input.KeyCode.ENTER;

@ExtendWith(XoExtension.class)
public final class JWFileChooserActionSaveTest
{
  private JWTestFilesystems filesystems;
  private FileSystem dosFilesystem;
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

    this.filesystems =
      JWTestFilesystems.create();
    final var systems =
      this.filesystems.filesystems();
    this.dosFilesystem =
      systems.get("ExampleDOS");

    this.configuration =
      JWFileChooserConfiguration.builder()
        .setAllowDirectoryCreation(true)
        .setAction(JWFileChooserAction.CREATE)
        .setFileSystem(this.dosFilesystem)
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
   * In SAVE mode, entering a name unlocks the OK button.
   *
   * @param robot The FX test robot
   */

  @MinimumPassing(executionCount = 5, passMinimum = 4)
  public void test_NameField_ExplicitlyTypedName_CandidateSelected(
    final XCCommanderType commander,
    final XCRobotType robot)
    throws Exception
  {
    final var chooser =
      createChooser(this.choosers, this.configuration, commander);
    final var window =
      chooser.stage();

    final var okButton =
      JWTestUtilities.findOKButton(robot, window);
    final var nameField =
      JWTestUtilities.findNameField(robot, window);

    robot.waitUntil(TIMEOUT, () -> !nameField.isDisabled());
    robot.pointAt(nameField);
    robot.click(nameField);
    robot.typeText(nameField, "GCC.EXE");
    robot.waitUntil(TIMEOUT, () -> !okButton.isDisabled());
    robot.typeRaw(nameField, ENTER);

    robot.waitUntil(TIMEOUT, () -> !okButton.isDisabled());
    robot.pointAt(okButton);
    robot.click(okButton);
    robot.waitForStageToClose(window, TIMEOUT);

    assertIsSelected(chooser, "Z:\\USERS\\GROUCH\\GCC.EXE");
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * If the path entered into the "select direct" dialog is not a directory,
   * then the parent of that path's file name is set as the current directory,
   * and the file name is selected.
   *
   * @param robot The FX test robot
   */

  @MinimumPassing(executionCount = 5, passMinimum = 4)
  public void test_SelectDirectly_TargetIsNotFile_TargetSelected(
    final XCCommanderType commander,
    final XCRobotType robot)
    throws Exception
  {
    final var chooser =
      createChooser(this.choosers, this.configuration, commander);
    final var window =
      chooser.stage();

    final var okButton =
      JWTestUtilities.findOKButton(robot, window);
    final var selectButton =
      JWTestUtilities.findSelectButton(robot, window);

    robot.waitUntil(TIMEOUT, () -> !selectButton.isDisabled());
    robot.pointAt(selectButton);
    robot.click(selectButton);
    robot.waitUntil(TIMEOUT, () -> windowIsOpenWithTitle("Enter Path"));

    final var dialogField =
      robot.findWithIdInAnyStage(
        TextField.class, "fileChooserDialogSelectDirectTextField");

    /*
     * There is a problem with using '\' in some keyboard layouts, so we set
     * the text field directly here and then act as if the user typed it.
     */

    robot.pointAt(dialogField);
    robot.execute(() -> dialogField.setText("Y:\\NEWFILE.TXT"));
    robot.typeRaw(KeyCode.SPACE);
    robot.typeRaw(KeyCode.BACK_SPACE);
    robot.typeRaw(ENTER);

    robot.waitUntil(TIMEOUT, () -> !okButton.isDisabled());
    robot.pointAt(okButton);
    robot.click(okButton);
    robot.waitForStageToClose(window, TIMEOUT);

    assertIsSelected(chooser, "Y:\\NEWFILE.TXT");
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * If the path entered into the "select direct" dialog is not a directory,
   * then the parent of that path's file name is set as the current directory,
   * and the file name is selected.
   *
   * @param robot The FX test robot
   */

  @MinimumPassing(executionCount = 5, passMinimum = 4)
  public void test_SelectDirectly_TargetIsFile_TargetSelected(
    final XCCommanderType commander,
    final XCRobotType robot)
    throws Exception
  {
    final var chooser =
      createChooser(this.choosers, this.configuration, commander);
    final var window =
      chooser.stage();

    final var okButton =
      JWTestUtilities.findOKButton(robot, window);
    final var selectButton =
      JWTestUtilities.findSelectButton(robot, window);

    robot.waitUntil(TIMEOUT, () -> !selectButton.isDisabled());
    robot.pointAt(selectButton);
    robot.click(selectButton);
    robot.waitUntil(TIMEOUT, () -> windowIsOpenWithTitle("Enter Path"));

    final var dialogField =
      robot.findWithIdInAnyStage(
        TextField.class, "fileChooserDialogSelectDirectTextField");

    /*
     * There is a problem with using '\' in some keyboard layouts, so we set
     * the text field directly here and then act as if the user typed it.
     */

    robot.pointAt(dialogField);
    robot.execute(() -> dialogField.setText("Z:\\USERS\\GROUCH\\PHOTO.JPG"));
    robot.typeRaw(KeyCode.SPACE);
    robot.typeRaw(KeyCode.BACK_SPACE);
    robot.typeRaw(ENTER);

    robot.waitUntil(TIMEOUT, () -> !okButton.isDisabled());
    robot.pointAt(okButton);
    robot.click(okButton);
    robot.waitForStageToClose(window, TIMEOUT);

    assertIsSelected(chooser, "Z:\\USERS\\GROUCH\\PHOTO.JPG");
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * If the path entered into the "select direct" dialog is a directory,
   * then that path becomes the new current directory, and "." is selected.
   *
   * @param robot The FX test robot
   */

  @MinimumPassing(executionCount = 5, passMinimum = 4)
  public void test_SelectDirectly_TargetIsDirectory_TargetSelected(
    final XCCommanderType commander,
    final XCRobotType robot)
    throws Exception
  {
    final var chooser =
      createChooser(this.choosers, this.configuration, commander);
    final var window =
      chooser.stage();

    final var okButton =
      JWTestUtilities.findOKButton(robot, window);
    final var selectButton =
      JWTestUtilities.findSelectButton(robot, window);

    robot.waitUntil(TIMEOUT, () -> !selectButton.isDisabled());
    robot.pointAt(selectButton);
    robot.click(selectButton);
    robot.waitUntil(TIMEOUT, () -> windowIsOpenWithTitle("Enter Path"));

    final var dialogField =
      robot.findWithIdInAnyStage(
        TextField.class, "fileChooserDialogSelectDirectTextField");

    /*
     * There is a problem with using '\' in some keyboard layouts, so we set
     * the text field directly here and then act as if the user typed it.
     */

    robot.pointAt(dialogField);
    robot.execute(() -> dialogField.setText("Z:\\USERS\\"));
    robot.typeRaw(KeyCode.SPACE);
    robot.typeRaw(KeyCode.BACK_SPACE);
    robot.typeRaw(ENTER);

    robot.waitUntil(TIMEOUT, () -> !okButton.isDisabled());
    robot.pointAt(okButton);
    robot.click(okButton);
    robot.waitForStageToClose(window, TIMEOUT);

    assertIsSelected(chooser, "Z:\\USERS");
    Assertions.assertEquals(0, this.events.size());
  }

  private static boolean windowIsOpenWithTitle(
    final String title)
  {
    return Window.getWindows()
      .stream()
      .filter(w -> w instanceof Stage)
      .map(Stage.class::cast)
      .anyMatch(w -> Objects.equals(w.getTitle(), title));
  }
}

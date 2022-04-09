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
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.framework.junit5.Stop;
import org.testfx.matcher.base.NodeMatchers;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ExtendWith(ApplicationExtension.class)
public final class JWFileChooserActionSaveTest
{
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
        .setAction(JWFileChooserAction.CREATE)
        .setFileSystem(this.dosFilesystem)
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
   * In SAVE mode, entering a name unlocks the OK button.
   *
   * @param robot The FX test robot
   */

  @Test
  public void test_NameField_ExplicitlyTypedName_CandidateSelected(
    final FxRobot robot,
    final TestInfo info)
  {
    JWFileWindowTitles.setTitle(this.chooser, info);

    final var delegate = new JWRobotDelegate(robot);

    final var okButton =
      robot.lookup("#fileChooserOKButton")
        .queryButton();

    robot.clickOn("#fileChooserNameField");

    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());
    robot.write("GCC.EXE");
    delegate.pauseBriefly();
    FxAssert.verifyThat(okButton, NodeMatchers.isEnabled());

    robot.type(KeyCode.ENTER);
    FxAssert.verifyThat(okButton, NodeMatchers.isEnabled());

    FxAssert.verifyThat(okButton, NodeMatchers.isEnabled());
    robot.clickOn(okButton);

    Assertions.assertEquals(
      List.of("Z:\\USERS\\GROUCH\\GCC.EXE"),
      this.chooser.result()
        .stream()
        .map(Path::toString)
        .collect(Collectors.toList())
    );
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * If the path entered into the "select direct" dialog is not a directory,
   * then the parent of that path's file name is set as the current directory,
   * and the file name is selected.
   *
   * @param robot The FX test robot
   */

  @Test
  public void test_SelectDirectly_TargetIsNotFile_TargetSelected(
    final FxRobot robot,
    final TestInfo info)
  {
    JWFileWindowTitles.setTitle(this.chooser, info);

    final var okButton =
      robot.lookup("#fileChooserOKButton")
        .queryButton();

    final var selectButton =
      robot.lookup("#fileChooserSelectDirectButton")
        .queryButton();

    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());
    robot.clickOn(selectButton);

    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());
    robot.write("Y:\\NEWFILE.TXT");

    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());
    robot.type(KeyCode.ENTER);

    FxAssert.verifyThat(okButton, NodeMatchers.isEnabled());
    robot.clickOn(okButton);

    Assertions.assertEquals(
      List.of("Y:\\NEWFILE.TXT"),
      this.chooser.result()
        .stream()
        .map(Path::toString)
        .collect(Collectors.toList())
    );
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * If the path entered into the "select direct" dialog is not a directory,
   * then the parent of that path's file name is set as the current directory,
   * and the file name is selected.
   *
   * @param robot The FX test robot
   */

  @Test
  public void test_SelectDirectly_TargetIsFile_TargetSelected(
    final FxRobot robot,
    final TestInfo info)
  {
    JWFileWindowTitles.setTitle(this.chooser, info);

    final var okButton =
      robot.lookup("#fileChooserOKButton")
        .queryButton();

    final var selectButton =
      robot.lookup("#fileChooserSelectDirectButton")
        .queryButton();

    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());
    robot.clickOn(selectButton);

    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());
    robot.write("Z:\\USERS\\GROUCH\\PHOTO.JPG");

    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());
    robot.type(KeyCode.ENTER);

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
   * If the path entered into the "select direct" dialog is a directory,
   * then that path becomes the new current directory, and "." is selected.
   *
   * @param robot The FX test robot
   */

  @Test
  public void test_SelectDirectly_TargetIsDirectory_TargetSelected(
    final FxRobot robot,
    final TestInfo info)
    throws Exception
  {
    JWFileWindowTitles.setTitle(this.chooser, info);

    final var delegate = new JWRobotDelegate(robot);

    final var okButton =
      robot.lookup("#fileChooserOKButton")
        .queryButton();

    final var selectButton =
      robot.lookup("#fileChooserSelectDirectButton")
        .queryButton();

    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());
    robot.clickOn(selectButton);

    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());
    robot.write("Z:\\USERS\\");

    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());
    robot.type(KeyCode.ENTER);

    delegate.waitUntil(() -> !okButton.isDisabled());

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

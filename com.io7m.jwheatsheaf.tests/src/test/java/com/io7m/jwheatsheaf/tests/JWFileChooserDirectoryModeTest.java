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

import com.io7m.jwheatsheaf.api.*;
import com.io7m.jwheatsheaf.ui.JWFileChoosers;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
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

/**
 * Verify that DOC, which represents a directory, may be selected.
 */

@SuppressWarnings( {"unused", "SameParameterValue"} )
@ExtendWith(ApplicationExtension.class)
public final class JWFileChooserDirectoryModeTest
{
  private JWFileChooserType chooser;
  private List<JWFileChooserEventType> events;
  private JWFileChoosersType choosers;

  @Start
  public void start(final Stage stage)
    throws Exception
  {
    this.events = Collections.synchronizedList(new ArrayList<>());

    final JWTestFilesystems filesystems = JWTestFilesystems.create();
    final var systems = filesystems.filesystems();
    final FileSystem dosFilesystem = systems.get( "ExampleDOS" );

    final var configuration =
      JWFileChooserConfiguration.builder()
                                .setAllowDirectoryCreation(true)
                                .setAction(JWFileChooserAction.OPEN_EXISTING_SINGLE)
                                .setFileSystem(dosFilesystem)
                                .setFileSelectionMode( path -> path.getFileName().toString().equals( "DOC" ) )
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
  }

  @AfterEach
  public void afterEach() {
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * Clicking a file in the directory listing selects that file as a
   * candidate for opening.
   */

  @Test
  public void test_DirectoryMode_SingleClickDirectory_CandidateSelected(
    final FxRobot robot,
    final TestInfo info)
  {
    final var delegate = new JWRobotDelegate(robot);
    final var okButton = delegate.getOkButton();

    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());
    robot.clickOn(delegate.getTableCellFileName("DOC"));
    FxAssert.verifyThat(okButton, NodeMatchers.isEnabled());
    robot.clickOn(okButton);

    assertSelected("Z:\\USERS\\GROUCH\\DOC");
  }

  /**
   * Double clicking a file in the directory listing does nothing.
   */

  @Test
  public void test_DirectoryMode_SingleClickFile_NothingSelected(
    final FxRobot robot,
    final TestInfo info)
  {
    final var delegate = new JWRobotDelegate(robot);
    final var okButton = delegate.getOkButton();

    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());
    robot.clickOn(delegate.getTableCellFileName("DATA.XML"));
    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());
    robot.clickOn(okButton);

    assertSelected();
  }

  /**
   * Double clicking a directory navigates to that directory.
   */

  @Test
  public void test_DirectoryMode_DoubleClickDirectory_DirectoryNavigated(
    final FxRobot robot,
    final TestInfo info)
  {
    final var delegate = new JWRobotDelegate(robot);
    final var okButton = delegate.getOkButton();

    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());
    robot.doubleClickOn(delegate.getTableCellFileName("DOC"));
    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());
    robot.clickOn(delegate.getTableCellFileName("."));
    robot.clickOn(okButton);

    assertSelected("Z:\\USERS\\GROUCH\\DOC");
  }

  private void assertSelected(final String... selectedItems) {
    Assertions.assertEquals(
      List.of(selectedItems),
      this.chooser.result()
                  .stream()
                  .map(Path::toString)
                  .collect(Collectors.toList())
    );
  }
}

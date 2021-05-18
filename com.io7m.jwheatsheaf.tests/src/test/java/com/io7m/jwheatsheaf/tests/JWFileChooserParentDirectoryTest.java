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
import com.io7m.jwheatsheaf.api.JWFileChoosersType;
import com.io7m.jwheatsheaf.ui.JWFileChoosers;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
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
public final class JWFileChooserParentDirectoryTest
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
    final FileSystem dosFilesystem = systems.get("ExampleDOS");

    final var configuration =
      JWFileChooserConfiguration.builder()
        .setAllowDirectoryCreation(true)
        .setShowParentDirectory(true)
        .setAction(JWFileChooserAction.OPEN_EXISTING_SINGLE)
        .setFileSystem(dosFilesystem)
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

  @AfterEach
  public void afterEach()
  {
    Assertions.assertEquals(0, this.events.size());
  }

  /**
   * Clicking the ".." directory entry allows for navigating to the parent
   * directory.
   */

  @Test
  public void test_ParentDirectory_DoubleClickParent_NavigatesToParent(
    final FxRobot robot,
    final TestInfo info)
  {
    JWFileWindowTitles.setTitle(this.chooser, info);

    final var delegate = new JWRobotDelegate(robot);
    final var okButton = delegate.getOkButton();

    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());
    robot.doubleClickOn(delegate.getTableCellFileName(".."));
    delegate.pauseBriefly();

    robot.clickOn(delegate.getTableCellFileName("."));
    delegate.pauseBriefly();

    FxAssert.verifyThat(okButton, NodeMatchers.isEnabled());
    robot.clickOn(okButton);

    this.assertSelected("Z:\\USERS");
  }

  private void assertSelected(final String... selectedItems)
  {
    Assertions.assertEquals(
      List.of(selectedItems),
      this.chooser.result()
        .stream()
        .map(Path::toString)
        .collect(Collectors.toList())
    );
  }
}

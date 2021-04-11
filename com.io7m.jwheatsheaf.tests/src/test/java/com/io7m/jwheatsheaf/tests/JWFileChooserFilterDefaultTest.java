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
import org.testfx.matcher.control.TableViewMatchers;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.*;

@ExtendWith(ApplicationExtension.class)
public final class JWFileChooserFilterDefaultTest
{
  private JWTestFilesystems filesystems;
  private FileSystem dosFilesystem;
  private FileSystem brokenFilesystem;
  private FileSystem brokenFilesFilesystem;
  private JWFileChooserType chooser;
  private List<Path> selected;
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
    this.brokenFilesystem = systems.get("Broken");
    this.brokenFilesFilesystem = systems.get("BrokenFiles");

    final var filter =
      new JWFileFilterHideAll();

    final var configuration =
      JWFileChooserConfiguration.builder()
        .setAllowDirectoryCreation(true)
        .setAction(JWFileChooserAction.CREATE)
        .setFileSystem(this.dosFilesystem)
        .addFileFilters(filter)
        .setFileFilterDefault(filter)
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

  /**
   * A "hide everything" filter set as the default... Hides everything!
   *
   * @param robot The FX test robot
   */

  @Test
  public void testFilterDefaultHidesEverything(
    final FxRobot robot,
    final TestInfo info)
  {
    JWFileWindowTitles.setTitle(this.chooser, info);

    final var okButton =
      robot.lookup("#fileChooserOKButton")
        .queryButton();
    final var cancelButton =
      robot.lookup("#fileChooserCancelButton")
        .queryButton();

    robot.sleep(1L, SECONDS);
    FxAssert.verifyThat(okButton, NodeMatchers.isDisabled());

    final var tableView =
      robot.lookup("#fileChooserDirectoryTable")
        .queryTableView();

    FxAssert.verifyThat(tableView, TableViewMatchers.hasNumRows(0));
    robot.clickOn(cancelButton);

    Assertions.assertEquals(List.of(), this.chooser.result());
    Assertions.assertEquals(0, this.events.size());
  }
}

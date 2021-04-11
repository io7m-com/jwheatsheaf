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
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.testfx.util.WaitForAsyncUtils.waitFor;

@ExtendWith(ApplicationExtension.class)
public final class JWFileChooserSelectionModeTest
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
    this.events = Collections.synchronizedList( new ArrayList<>());

    this.filesystems = JWTestFilesystems.create();
    final var systems = this.filesystems.filesystems();
    this.dosFilesystem = systems.get("ExampleDOS");

    final var configuration =
      JWFileChooserConfiguration.builder()
                                .setAllowDirectoryCreation(true)
                                .setAction( JWFileChooserAction.OPEN_EXISTING_SINGLE)
                                .setFileSystem(this.dosFilesystem)
                                .setInitialFileName("README.TXT")
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
   * Supplying an initial filename allows instantly selecting a file.
   *
   * @param robot The FX test robot
   */

  @Test
  public void test(
    final FxRobot robot,
    final TestInfo info)
    throws TimeoutException
  {
    final var okButton =
      robot.lookup("#fileChooserOKButton")
           .queryButton();
    final var textField =
      robot.lookup("#fileChooserNameField")
           .queryTextInputControl();

    //waitFor(3L, SECONDS, () -> !textField.textProperty().get().isEmpty());
    FxAssert.verifyThat( okButton, NodeMatchers.isEnabled());
    robot.sleep(1L, SECONDS);
    robot.clickOn(okButton);

    Assertions.assertEquals(
      List.of( "Z:\\USERS\\GROUCH\\README.TXT"),
      this.chooser.result()
                  .stream()
                  .map( Path::toString)
                  .collect( Collectors.toList())
    );
    Assertions.assertEquals(0, this.events.size());
  }
}

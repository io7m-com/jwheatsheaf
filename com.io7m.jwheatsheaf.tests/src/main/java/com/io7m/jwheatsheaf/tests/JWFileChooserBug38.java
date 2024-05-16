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
import com.io7m.xoanon.commander.api.XCKey;
import com.io7m.xoanon.commander.api.XCRobotType;
import com.io7m.xoanon.extension.XoExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.io7m.jwheatsheaf.tests.JWTestUtilities.TIMEOUT;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.assertIsSelected;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.createChooser;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.findNameField;
import static com.io7m.jwheatsheaf.tests.JWTestUtilities.findOKButton;
import static javafx.scene.input.KeyCode.ENTER;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(XoExtension.class)
public final class JWFileChooserBug38
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
    throws IOException
  {
    this.events = Collections.synchronizedList(new ArrayList<>());

    this.filesystems = JWTestFilesystems.create();
    final var systems = this.filesystems.filesystems();
    this.dosFilesystem = systems.get("ExampleDOS");

    this.configuration =
      JWFileChooserConfiguration.builder()
        .setAction(JWFileChooserAction.CREATE)
        .setConfirmFileSelection(true)
        .setFileSystem(this.dosFilesystem)
        .setFileSelectionMode(path -> {
          return Boolean.valueOf(Files.isRegularFile(path));
        })
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
   * If CREATE and confirmFileSelection are used, the confirmation dialog is
   * always shown.
   */

  @Test
  public void test_CREATEConfirmFileSelection_OnlyShownIfFileExists(
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
    final var nameField =
      findNameField(robot, window);

    robot.click(nameField);
    robot.typeText(nameField, "THIS_DOES_NOT_EXIST.TXT");
    robot.type(nameField, List.of(new XCKey(ENTER, false, false, false)));
    robot.click(okButton);
    robot.waitForStageToClose(window, TIMEOUT);
  }
}

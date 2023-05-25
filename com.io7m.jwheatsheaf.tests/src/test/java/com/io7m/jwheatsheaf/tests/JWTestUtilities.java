/*
 * Copyright © 2023 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import com.io7m.jwheatsheaf.api.JWFileChooserConfiguration;
import com.io7m.jwheatsheaf.api.JWFileChooserType;
import com.io7m.jwheatsheaf.api.JWFileChoosersType;
import com.io7m.jwheatsheaf.api.JWVersion;
import com.io7m.xoanon.commander.api.XCApplicationInfo;
import com.io7m.xoanon.commander.api.XCCommanderType;
import com.io7m.xoanon.commander.api.XCFXThread;
import com.io7m.xoanon.commander.api.XCRobotType;
import com.io7m.xoanon.extension.XoExtension;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Assertions and functions for writing file chooser tests.
 */

public final class JWTestUtilities
{
  private JWTestUtilities()
  {

  }

  static final long TIMEOUT = 1_000L;

  static JWFileChooserType createChooser(
    final JWFileChoosersType choosers,
    final JWFileChooserConfiguration configuration,
    final XCCommanderType commander)
    throws Exception
  {
    final var robot =
      commander.robot()
        .get(TIMEOUT, MILLISECONDS);

    final var chooser =
      XCFXThread.runAndWait(
        TIMEOUT, MILLISECONDS, () -> choosers.create(configuration)
      );
    XCFXThread.runVWait(TIMEOUT, MILLISECONDS, chooser::show);

    robot.execute(() -> chooser.stage().requestFocus());
    robot.waitUntil(TIMEOUT, () -> chooser.stage().isShowing());
    robot.waitUntil(TIMEOUT, () -> chooser.stage().isFocused());
    return chooser;
  }

  static void assertIsSelected(
    final JWFileChooserType chooser,
    final String... selectedItems)
  {
    Assertions.assertEquals(
      List.of(selectedItems),
      chooser.result()
        .stream()
        .map(Path::toString)
        .collect(Collectors.toList())
    );
  }

  static void assertNothingSelected(
    final JWFileChooserType chooser)
  {
    Assertions.assertEquals(
      List.of(),
      chooser.result()
        .stream()
        .map(Path::toString)
        .collect(Collectors.toList())
    );
  }

  static Button findSelectButton(
    final XCRobotType robot,
    final Stage window)
    throws Exception
  {
    return robot.findWithId(
      Button.class,
      window,
      "fileChooserSelectDirectButton"
    );
  }

  static Button findOKButton(
    final XCRobotType robot,
    final Stage window)
    throws Exception
  {
    return robot.findWithId(
      Button.class,
      window,
      "fileChooserOKButton"
    );
  }

  static Button findUpButton(
    final XCRobotType robot,
    final Stage window)
    throws Exception
  {
    return robot.findWithId(
      Button.class,
      window,
      "fileChooserUpButton"
    );
  }

  static Button findHomeButton(
    final XCRobotType robot,
    final Stage window)
    throws Exception
  {
    return robot.findWithId(
      Button.class,
      window,
      "fileChooserHomeButton"
    );
  }

  static Button findCancelButton(
    final XCRobotType robot,
    final Stage window)
    throws Exception
  {
    return robot.findWithId(
      Button.class,
      window,
      "fileChooserCancelButton"
    );
  }

  static Button findDirectoryCreateButton(
    final XCRobotType robot,
    final Stage window)
    throws Exception
  {
    return robot.findWithId(
      Button.class,
      window,
      "fileChooserCreateDirectoryButton"
    );
  }

  static ChoiceBox<?> findPathMenu(
    final XCRobotType robot,
    final Stage window)
    throws Exception
  {
    return robot.findWithId(
      ChoiceBox.class,
      window,
      "fileChooserPathMenu"
    );
  }

  static ListView<?> findSourceList(
    final XCRobotType robot,
    final Stage window)
    throws Exception
  {
    return robot.findWithId(
      ListView.class,
      window,
      "fileChooserSourceList"
    );
  }

  static TextField findSearchField(
    final XCRobotType robot,
    final Stage window)
    throws Exception
  {
    return robot.findWithId(
      TextField.class,
      window,
      "fileChooserSearchField"
    );
  }

  static TextField findNameField(
    final XCRobotType robot,
    final Stage window)
    throws Exception
  {
    return robot.findWithId(
      TextField.class,
      window,
      "fileChooserNameField"
    );
  }

  static TableView<?> findDirectoryTable(
    final XCRobotType robot,
    final Stage window)
    throws Exception
  {
    return robot.findWithId(
      TableView.class,
      window,
      "fileChooserDirectoryTable"
    );
  }

  static TableCell<?, ?> findDirectoryTableFileCell(
    final XCRobotType robot,
    final Stage window,
    final String doc)
    throws Exception
  {
    final var table = findDirectoryTable(robot, window);
    return robot.findWithText(TableCell.class, table, doc);
  }

  static void publishApplicationInfo()
  {
    XoExtension.setApplicationInfo(new XCApplicationInfo(
      "com.io7m.jwheatsheaf",
      JWVersion.MAIN_VERSION,
      JWVersion.MAIN_BUILD
    ));
  }
}

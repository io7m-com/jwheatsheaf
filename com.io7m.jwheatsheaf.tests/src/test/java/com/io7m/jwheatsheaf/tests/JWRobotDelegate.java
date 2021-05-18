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

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextInputControl;
import org.testfx.api.FxRobot;
import org.testfx.service.query.NodeQuery;
import org.testfx.util.WaitForAsyncUtils;

import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Abstracts common unit test functionality with respect to retrieving widgets.
 */

@SuppressWarnings("unused")
final class JWRobotDelegate
{
  private final FxRobot robot;

  JWRobotDelegate(
    final FxRobot inRobot)
  {
    this.robot = Objects.requireNonNull(inRobot, "inRobot");
  }

  Button getOkButton()
  {
    return this.lookup("#fileChooserOKButton").queryButton();
  }

  TextInputControl getNameField()
  {
    return this.lookup("#fileChooserNameField").queryTextInputControl();
  }

  Node getSourceList()
  {
    return this.query(".fileChooserSourceList");
  }

  Node getDirectoryTable()
  {
    return this.query(".fileChooserDirectoryTable");
  }

  /**
   * @param columnName One of: {@code Size}, {@code Name}, {@code Modified}
   */

  Node getTableColumn(final String columnName)
  {
    return this.query("#fileChooserTableColumn" + columnName);
  }

  Node query(final String nodeName)
  {
    return this.lookup(nodeName).query();
  }

  NodeQuery lookup(final String widgetId)
  {
    return this.robot.lookup(widgetId);
  }

  TableCell<?, ?> getTableCellFileName(final String text)
  {
    return this.robot.lookup(n -> n instanceof TableCell)
      .queryAllAs(TableCell.class)
      .stream()
      .filter(cell -> Objects.equals(cell.getText(), text))
      .findFirst()
      .orElseThrow(() -> new IllegalStateException(
        String.format("Unable to locate a '%s' directory entry", text))
      );
  }

  void pauseBriefly()
  {
    this.robot.sleep(1L, SECONDS);
  }

  /**
   * Wait until the given condition is true, or until ten seconds have elapsed.
   *
   * @param condition The condition
   *
   * @throws TimeoutException If the time elapses before the condition becomes true
   */

  public void waitUntil(
    final BooleanSupplier condition)
    throws TimeoutException
  {
    WaitForAsyncUtils.waitFor(10L, SECONDS, () -> {
      return Boolean.valueOf(condition.getAsBoolean());
    });
  }
}

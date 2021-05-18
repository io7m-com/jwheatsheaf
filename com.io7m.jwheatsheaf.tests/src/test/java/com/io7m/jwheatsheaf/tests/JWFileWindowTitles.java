/*
 * Copyright © 2021 Mark Raynsford <code@io7m.com> http://io7m.com
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

import com.io7m.jwheatsheaf.api.JWFileChooserType;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.TestInfo;

import java.lang.reflect.Method;

public final class JWFileWindowTitles
{
  private JWFileWindowTitles()
  {

  }

  public static void setTitle(
    final JWFileChooserType chooser,
    final TestInfo title)
  {
    try {
      final var field =
        chooser.getClass()
          .getDeclaredField("window");

      field.setAccessible(true);
      final Stage window = (Stage) field.get(chooser);

      Platform.runLater(() -> {
        final var builder = new StringBuilder();
        builder.append(
          title.getTestClass().map(Class::getSimpleName).orElse(""));
        builder.append(":");
        builder.append(
          title.getTestMethod().map(Method::getName).orElse(""));
        builder.append(" (");
        builder.append(title.getDisplayName());
        builder.append(")");
        window.setTitle(builder.toString());
      });
    } catch (final IllegalAccessException | NoSuchFieldException e) {
      throw new IllegalStateException(e);
    }
  }
}

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

package com.io7m.jwheatsheaf.api;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.util.function.Function;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.YEAR;

/**
 * Default values used for file chooser configurations.
 */

public final class JWFileChooserConfigurationDefaults
{
  private JWFileChooserConfigurationDefaults()
  {

  }

  /**
   * The default date/time formatter. This is essentially ISO-8601 format
   * without microseconds.
   *
   * @return A date/time formatter
   */

  public static DateTimeFormatter fileTimeFormatter()
  {
    return new DateTimeFormatterBuilder()
      .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
      .appendLiteral('-')
      .appendValue(MONTH_OF_YEAR, 2)
      .appendLiteral('-')
      .appendValue(DAY_OF_MONTH, 2)
      .appendLiteral(' ')
      .appendValue(HOUR_OF_DAY, 2)
      .appendLiteral(':')
      .appendValue(MINUTE_OF_HOUR, 2)
      .optionalStart()
      .appendLiteral(':')
      .appendValue(SECOND_OF_MINUTE, 2)
      .appendOffsetId()
      .toFormatter();
  }

  /**
   * The default file selection mode. By default, any type of file (including
   * directories) may be selected.
   *
   * @return A function that dictates whether the selected items may be
   * returned.
   */

  public static Function<Path, Boolean> fileSelectionMode() {
    return (path) -> true;
  }

  /**
   * This is a basic file size formatter that displays fractional kilobyte,
   * megabyte, gigabyte, and terabyte values depending on sizes.
   *
   * @return A size formatter
   */

  public static JWFileSizeFormatterType fileSizeFormatter()
  {
    return size -> {
      if (size == -1L) {
        return "";
      }

      final var real = Double.parseDouble(Long.toUnsignedString(size));
      if (Long.compareUnsigned(size, 1_000L) < 0) {
        return Long.toUnsignedString(size) + "B";
      }
      if (Long.compareUnsigned(size, 1_000_000L) < 0) {
        return String.format(
          "%.2fkB", Double.valueOf(real / 1_000.0));
      }
      if (Long.compareUnsigned(size, 1_000_000_000L) < 0) {
        return String.format(
          "%.2fMB", Double.valueOf(real / 1_000_000.0));
      }
      if (Long.compareUnsigned(size, 1_000_000_000_000L) < 0) {
        return String.format(
          "%.2fGB", Double.valueOf(real / 1_000_000_000_000.0));
      }
      return String.format(
        "%.2fTB", Double.valueOf(real / 1_000_000_000_000.0));
    };
  }
}

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

package com.io7m.jwheatsheaf.ui.internal;

import javafx.scene.control.TableCell;

import java.nio.file.attribute.FileTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

final class JWFileItemTableTimeCell extends TableCell<JWFileItem, FileTime>
{
  private final DateTimeFormatter timeFormatter;

  JWFileItemTableTimeCell(
    final DateTimeFormatter inTimeFormatter)
  {
    this.timeFormatter =
      Objects.requireNonNull(inTimeFormatter, "inTimeFormatter");
  }

  private String formatTime(
    final FileTime item)
  {
    final var instant = item.toInstant();
    final var time = OffsetDateTime.ofInstant(instant, ZoneId.of("UTC"));
    return this.timeFormatter.format(time);
  }

  @Override
  protected void updateItem(
    final FileTime item,
    final boolean empty)
  {
    super.updateItem(item, empty);

    if (empty || item == null) {
      this.setGraphic(null);
      this.setText(null);
      return;
    }

    this.setText(this.formatTime(item));
    this.setGraphic(null);
  }
}

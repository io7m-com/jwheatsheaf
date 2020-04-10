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

import com.io7m.jwheatsheaf.api.JWFileImageSetType;
import javafx.scene.control.TableCell;

import java.util.Objects;

final class JWFileItemTableTypeCell extends TableCell<JWFileItem, JWFileItem>
{
  private final JWToolTips toolTips;
  private final JWFileImageSetType images;

  JWFileItemTableTypeCell(
    final JWFileImageSetType inImages,
    final JWToolTips inToolTips)
  {
    this.images =
      Objects.requireNonNull(inImages, "inImages");
    this.toolTips =
      Objects.requireNonNull(inToolTips, "inToolTips");
  }

  @Override
  protected void updateItem(
    final JWFileItem item,
    final boolean empty)
  {
    super.updateItem(item, empty);

    if (empty || item == null) {
      this.setGraphic(null);
      this.setText(null);
      this.setTooltip(null);
      return;
    }

    this.setGraphic(JWImages.imageOfKind(this.images, item.kind()));
    this.setText(null);
    this.setTooltip(this.toolTips.tooltipOf(item));
  }
}

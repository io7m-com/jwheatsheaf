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

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jwheatsheaf.api.JWFileImageSetType;
import javafx.application.Platform;
import javafx.scene.control.ListCell;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A source entry representing a filesystem root.
 */

public final class JWFileSourceEntryFilesystemRoot
  implements JWFileSourceEntryType
{
  private final boolean withParent;
  private final Path root;

  /**
   * Construct a source entry.
   *
   * @param inRoot       The root directory
   * @param inWithParent {@code true} if a parent directory should be shown
   */

  public JWFileSourceEntryFilesystemRoot(
    final boolean inWithParent,
    final Path inRoot)
  {
    this.withParent = inWithParent;
    this.root = Objects.requireNonNull(inRoot, "root");
  }

  @Override
  public void onListCell(
    final JWFileImageSetType images,
    final JWStrings strings,
    final ListCell<JWFileSourceEntryType> cell)
  {
    Preconditions.checkPreconditionV(
      Platform.isFxApplicationThread(),
      "Must be FX application thread");

    cell.setText(this.root.toString());
    cell.setGraphic(JWImages.imageView16x16Of(images.forFileSystem()));
  }

  @Override
  public Optional<Path> path()
  {
    return Optional.of(this.root);
  }

  @Override
  public List<JWFileItem> onFileItemsRequested()
    throws Exception
  {
    Preconditions.checkPreconditionV(
      !Platform.isFxApplicationThread(),
      "Must not be FX application thread");

    return JWFileItems.listDirectory(this.root, this.withParent);
  }
}

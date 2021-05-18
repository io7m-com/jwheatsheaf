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

import javafx.stage.Window;

import java.io.Closeable;

/**
 * A provider of file choosers.
 *
 * File chooser providers are expected to be application-scoped, and reused
 * to produce file choosers on demand.
 */

public interface JWFileChoosersType extends Closeable
{
  /**
   * Create a new file chooser.
   *
   * @param window        The owning window
   * @param configuration The file chooser configuration
   *
   * @return A new file chooser
   */

  JWFileChooserType create(
    Window window,
    JWFileChooserConfiguration configuration
  );

  /**
   * @return A filter that allows access to all files
   */

  JWFileChooserFilterType filterForAllFiles();

  /**
   * @return A filter that only allows access to directories
   */

  JWFileChooserFilterType filterForOnlyDirectories();
}

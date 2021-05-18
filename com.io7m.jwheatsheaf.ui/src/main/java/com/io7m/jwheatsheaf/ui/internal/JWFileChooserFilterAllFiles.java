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

import com.io7m.jwheatsheaf.api.JWFileChooserFilterType;

import java.nio.file.Path;
import java.util.Objects;

/**
 * A filter that trivially accepts all files.
 */

public final class JWFileChooserFilterAllFiles implements
  JWFileChooserFilterType
{
  private final String description;

  private JWFileChooserFilterAllFiles(
    final String inDescription)
  {
    this.description =
      Objects.requireNonNull(inDescription, "inDescription");
  }

  /**
   * Create a file chooser filter that accepts all files.
   *
   * @param strings The string resource provider
   *
   * @return A file filter
   */

  public static JWFileChooserFilterType create(
    final JWStrings strings)
  {
    return new JWFileChooserFilterAllFiles(
      strings.filterAllFilesDescription());
  }

  @Override
  public String description()
  {
    return this.description;
  }

  @Override
  public boolean isAllowed(final Path path)
  {
    return true;
  }
}

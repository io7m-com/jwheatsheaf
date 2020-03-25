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

package com.io7m.jwheatsheaf.examples;

import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.jwheatsheaf.api.JWFileImageSetType;
import com.io7m.jwheatsheaf.api.JWFileKind;
import com.io7m.jwheatsheaf.ui.JWFileImageDefaultSet;

import java.net.URL;
import java.util.Optional;

public final class ExampleInverseIconSet implements JWFileImageSetType
{
  /**
   * Construct a set of images.
   */

  public ExampleInverseIconSet()
  {

  }

  private static Optional<URL> forName(
    final String name)
  {
    return Optional.of(
      JWFileImageDefaultSet.class.getResource(
        String.format("/com/io7m/jwheatsheaf/examples/%s", name)
      )
    );
  }

  @Override
  public URL forDirectoryCreate()
  {
    return forName("directoryCreate.png").orElseThrow();
  }

  @Override
  public URL forDirectoryUp()
  {
    return forName("goUp.png").orElseThrow();
  }

  @Override
  public URL forRecentItems()
  {
    return forName("recentItems.png").orElseThrow();
  }

  @Override
  public URL forFileSystem()
  {
    return forName("filesystem.png").orElseThrow();
  }

  @Override
  public Optional<URL> forFileKind(
    final JWFileKind kind)
  {
    switch (kind) {
      case REGULAR_FILE:
        return forName("file.png");
      case DIRECTORY:
        return forName("directory.png");
      case SYMBOLIC_LINK:
        return forName("link.png");
      case UNKNOWN:
        return Optional.empty();
    }

    throw new UnreachableCodeException();
  }
}

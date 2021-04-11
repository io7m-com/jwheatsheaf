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

package com.io7m.jwheatsheaf.oxygen;

import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.jwheatsheaf.api.JWFileImageSetType;
import com.io7m.jwheatsheaf.api.JWFileKind;

import java.net.URL;
import java.util.Optional;

public final class JWOxygenIconSet implements JWFileImageSetType
{
  /**
   * Construct a set of images.
   */

  public JWOxygenIconSet()
  {

  }

  private static Optional<URL> forName(
    final String name)
  {
    return Optional.of(
      JWOxygenIconSet.class.getResource(
        String.format("/com/io7m/jwheatsheaf/oxygen/%s", name)
      )
    );
  }

  @Override
  public URL forSelectDirect()
  {
    return forName("go-bottom.png").orElseThrow();
  }

  @Override
  public URL forDirectoryCreate()
  {
    return forName("folder-new.png").orElseThrow();
  }

  @Override
  public URL forDirectoryUp()
  {
    return forName("go-up.png").orElseThrow();
  }

  @Override
  public URL forHome()
  {
    return forName("go-home.png").orElseThrow();
  }

  @Override
  public URL forRecentItems()
  {
    return forName("chronometer-lap.png").orElseThrow();
  }

  @Override
  public URL forFileSystem()
  {
    return forName("drive-harddisk.png").orElseThrow();
  }

  @Override
  public Optional<URL> forFileKind(
    final JWFileKind kind)
  {
    switch (kind) {
      case REGULAR_FILE:
        return forName("unknown.png");
      case DIRECTORY:
        return forName("inode-directory.png");
      case SYMBOLIC_LINK:
        return forName("emblem-symbolic-link.png");
      case UNKNOWN:
        return Optional.empty();
    }

    throw new UnreachableCodeException();
  }
}
